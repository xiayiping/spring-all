package org.xyp.sample.spring.tracing;

import lombok.Data;
import lombok.val;

@Data
public class AuditRecorder implements AutoCloseable {

    private final AuditInfo selfInfo;
    private final AuditInfoStack stack;
    private final Runnable clear;

    public AuditRecorder(AuditInfoStack stack, Runnable additionalAction) {
        this.stack = stack;
        this.clear = additionalAction;
        val parentAuditInfo = stack.peek();
        if (null == parentAuditInfo) {
            this.selfInfo = AuditInfo.ofNewOne();
        } else {
            val myOrder = parentAuditInfo.childrenSize().getAndIncrement();
            this.selfInfo = AuditInfo.ofNewOne(stack.size(), myOrder);
        }
        stack.push(selfInfo);
    }

    @Override
    public void close() {
        if (null != this.selfInfo && this.selfInfo.equals(this.stack.peek())) {
            this.stack.pop();
        }
        clear.run();
    }
}
