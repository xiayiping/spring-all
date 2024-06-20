package org.xyp.sample.spring.db.id;

public class IdValidatorLong {

    public static void validate(Long id) {
        if (null == id || id <= 0) {
            throw new RuntimeException("id must be greater than 0");
        }
    }
}
