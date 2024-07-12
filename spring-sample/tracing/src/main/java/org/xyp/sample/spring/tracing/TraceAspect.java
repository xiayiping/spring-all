package org.xyp.sample.spring.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.xyp.function.Fun;
import org.xyp.function.ValueHolder;
import org.xyp.function.wrapper.ResultOrError;

import java.util.Optional;

import static org.xyp.sample.spring.tracing.TraceFlagNames.*;

@Slf4j
@Aspect
@Component
public class TraceAspect {
    private final ObservationRegistry observationRegistry;

    public TraceAspect(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
        log.info("create TraceAspect ......");
    }

    @Pointcut("@annotation(org.xyp.sample.spring.tracing.Trace)")
    public void startAuditTransactionPointcut() {
    }

    @Around("startAuditTransactionPointcut()")
    public Object startAuditTransactionAdvice(ProceedingJoinPoint pjp) throws Throwable {
        val opt = Optional.ofNullable(pjp)
            .map(JoinPoint::getSignature)
            .flatMap(Fun.cast(MethodSignature.class))
            .flatMap(m -> Optional.ofNullable(m.getMethod()).map(v -> v.getAnnotation(Trace.class)));
        if (opt.isPresent()) {
            return runWithAudit(pjp, opt.get());
        } else {
            if (null == pjp)
                return null;
            else {
                try {
                    return pjp.proceed();
                } catch (Throwable throwable) {
                    throw throwable;
                }
            }
        }
    }

    private Object runWithAudit(ProceedingJoinPoint pjp, Trace txAnn) throws Throwable {
        val observation = createEventObservation(txAnn);
        ValueHolder<Throwable> varHandle = new ValueHolder<>(null);
        val result = observation.observe(() -> ResultOrError.on(() -> {

            val ctx = observation.getContext();

            val depth = getCtxValueInteger(TRACE_DEPTH, ctx, 0);
            val step = getCtxValueInteger(TRACE_STEP_SAME_DEPTH, ctx, 0);

            val start = System.currentTimeMillis();

            try {
                log.info("<trace name={} depth={} step={} signature={}  >", txAnn.value(), depth, step, pjp.getSignature());
                return pjp.proceed();
            } catch (Throwable throwable) {
                varHandle.setValue(throwable);
                return null;
            } finally {
                val cost = System.currentTimeMillis() - start;
                log.info("</trace name={} depth={} step={} cost={} signature={} >", txAnn.value(), depth, step, cost, pjp.getSignature());
            }
        }).get());

        if (varHandle.value() != null) {
            throw varHandle.value();
        }

        return result;
    }

    private Integer getCtxValueInteger(String key, Observation.ContextView ctx, int defaultValue) {
        return Optional.ofNullable(ctx).map(c -> c.get(key))
            .flatMap(Fun.cast(Integer.class))
            .orElse(defaultValue);
    }

    private Observation createEventObservation(Trace txAnn) {
        val parent = Optional.ofNullable(observationRegistry.getCurrentObservation())
            .map(Observation::getContext).orElse(null);
        val name = createObsName(txAnn);
        return Observation.createNotStarted(name,
            () -> {
                val ctx = new Observation.Context();
                ctx.put(TraceFlagNames.TRACE_VALUE, txAnn.value());
                ctx.put(TRACE_DEPTH, getCtxValueInteger(TRACE_DEPTH, parent, -1) + 1);

                val step = getCtxValueInteger(TRACE_CHILD_STEP_COUNT, parent, -1) + 1;
                if (null != parent) {
                    parent.put(TRACE_CHILD_STEP_COUNT, step);
                }
                ctx.put(TRACE_STEP_SAME_DEPTH, step);

                return ctx;
            },
            observationRegistry);
    }


    public static String createObsName(Trace txAnn) {
        return txAnn.value();
    }
}
