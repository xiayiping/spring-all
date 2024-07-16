package org.xyp.sample.spring.common.test;

import org.junit.jupiter.api.Test;

class Test1 {
    @Test
    void test() {
        System.out.println(fab2(4));
    }

    long fab(int i) {
        if (i == 0) {
            return 1;
        }
        return i * fab(i - 1);
    }

    long fab1(int i) {
        return fab1(i, 1);
    }

    long fab1(int i, long result) {
        if (i == 0) {
            return result;
        }
        return fab1(i - 1, result * i);
    }

    long fab2(int i) {
        long result = 1;
        while (i > 0) {
            result = result * i;
            i = i-1;
        }
        return result;
    }
}
