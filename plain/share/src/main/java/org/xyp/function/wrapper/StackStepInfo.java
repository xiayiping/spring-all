package org.xyp.function.wrapper;


public class StackStepInfo {
    private final StackWalker.StackFrame stackFrame;
    private final StackStepInfo previous;
    private Object input;
    private Object output;

    public StackStepInfo(StackWalker.StackFrame stackFrame, StackStepInfo previous) {
        this.stackFrame = stackFrame;
        this.previous = previous;
    }

    public StackWalker.StackFrame getStackFrame() {
        return stackFrame;
    }

    public Object getInput() {
        return input;
    }

    void setInput(Object input) {
        this.input = input;
    }

    public Object getOutput() {
        return output;
    }

    void setOutput(Object output) {
        this.output = output;
    }

    public StackStepInfo getPrevious() {
        return previous;
    }
}
