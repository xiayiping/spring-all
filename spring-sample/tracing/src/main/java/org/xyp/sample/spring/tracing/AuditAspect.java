package org.xyp.sample.spring.tracing;

import com.tcghl.corej.util.fun.wrapper.ResultOrError;
import com.tcghl.corej.util.fun.wrapper.WithCloseable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Optional;

import static com.tcghl.corej.util.fun.Fun.checkAndCast;

@Slf4j
@AllArgsConstructor
@Aspect
public class AuditAspect {

    private final ThreadLocal<AuditInfoStack> infoStack = ThreadLocal.withInitial(AuditInfoStack::new);
    private final AuditListener auditListener;

    @Pointcut("@annotation(com.tcghl.corej.audit.aop.Audit)")
    public void startAuditTransactionPointcut() {
    }

    @Around("startAuditTransactionPointcut()")
    public Object startAuditTransactionAdvice(ProceedingJoinPoint pjp) {
        val auditAnnotation = Optional.ofNullable(pjp)
            .map(JoinPoint::getSignature)
            .flatMap(s -> checkAndCast(s, MethodSignature.class))
            .flatMap(m -> Optional.ofNullable(m.getMethod()).map(v -> v.getAnnotation(Audit.class)));

        if (auditAnnotation.isPresent()) {
            return auditAnnotation
                .map(txAnn -> runWithAudit(pjp, txAnn))
                .orElse(null);
        } else {
            if (null == pjp)
                return null;
            else
                return ResultOrError.on(pjp::proceed).get();
        }
    }

    private Object runWithAudit(ProceedingJoinPoint pjp, Audit txAnn) {

        val currentInfoStack = this.infoStack.get();
        val start = System.currentTimeMillis();
        val result = WithCloseable.open(
                () -> {
                    val recorder = new AuditRecorder(currentInfoStack);
                    ResultOrError.on(() -> {
                        log.info("<{}> begin", txAnn.value());
                        auditListener.onBegin(recorder, pjp);
                        return null;
                    }).getResult();
                    return recorder;
                },
                (closedRecorder, throwable) -> ResultOrError.on(() -> {
                    log.error("</{}> error {} {}", txAnn.value(), throwable.getClass().getName(), throwable.getMessage());
                    auditListener.onError(closedRecorder, pjp, throwable);
                    return closedRecorder;
                }).getResult(),
                closedRecorder -> {
                    if (closedRecorder.getStack().isEmpty()) {
                        this.infoStack.remove();
                    }
                }
            )
            .map(ignoredRecorder -> pjp.proceed())
            .mapWithCloseable((recorder, res) -> ResultOrError.on(() -> {
                        logFunctionFinished(txAnn, res, pjp, start);
                        auditListener.onSuccess(recorder, pjp, res);
                        return res;
                    }).getResult()
                    .getOrFallBackForError(ex -> res)
            )
            .closeAndGetResult();

        return result.get();
    }

    private static void logFunctionFinished(Audit txAnn, Object ignoredResult, ProceedingJoinPoint pjp, long start) {
        val cost = System.currentTimeMillis() - start;
        log.info("</{}> finished function {} within {} milliseconds hasResult? {}", txAnn.value(), pjp.getSignature(), cost, null != ignoredResult);
    }

}
