package org.xyp.shared.function.test;

import lombok.Getter;
import org.xyp.shared.function.wrapper.StackStepInfo;

@Getter
public class ValidateWithStackException extends RuntimeException {
    private final StackStepInfo<?> stackStepInfo;
    public ValidateWithStackException(String message, StackStepInfo<?> stackStepInfo) {
        super(message);
        this.stackStepInfo = stackStepInfo;
    }


}
