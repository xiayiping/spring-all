package org.xyp.sample.spring.db.id;

import org.xyp.exceptions.ValidateException;

public class IdValidatorLong {
    private IdValidatorLong() {}

    public static void validate(Long id) {
        if (null == id || id <= 0) {
            throw new ValidateException("id must be greater than 0");
        }
    }
}
