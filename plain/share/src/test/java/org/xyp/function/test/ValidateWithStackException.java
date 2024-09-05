package org.xyp.function.test;

import lombok.Getter;
import org.xyp.function.wrapper.StackStepInfo;

@Getter
public class ValidateWithStackException extends RuntimeException {
    private final StackStepInfo<?> stackStepInfo;
    public ValidateWithStackException(String message, StackStepInfo<?> stackStepInfo) {
        super(message);
        this.stackStepInfo = stackStepInfo;
    }


}
