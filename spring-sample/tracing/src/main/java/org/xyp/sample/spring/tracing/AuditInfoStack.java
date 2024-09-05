package org.xyp.sample.spring.tracing;

import java.util.LinkedList;

public class AuditInfoStack {
    private final LinkedList<AuditInfo> stack = new LinkedList<>();

    public AuditInfo pop() {
        return isEmpty() ? null : stack.pop();
    }

    public AuditInfo push(AuditInfo info) {
        stack.push(info);
        return info;
    }

    public AuditInfo peek() {
        return stack.peek();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }
}
