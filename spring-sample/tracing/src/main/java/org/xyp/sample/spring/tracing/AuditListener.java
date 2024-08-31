package org.xyp.sample.spring.tracing;

import org.aspectj.lang.ProceedingJoinPoint;

public interface AuditListener {

    void onBegin(AuditRecorder recorder, ProceedingJoinPoint pjp);

    void onError(AuditRecorder auditRecorder, ProceedingJoinPoint pjp, Throwable throwable);

    void onSuccess(AuditRecorder recorder, ProceedingJoinPoint pjp, Object res);

}
