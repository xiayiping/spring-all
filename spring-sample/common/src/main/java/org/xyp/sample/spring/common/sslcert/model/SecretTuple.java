package org.xyp.sample.spring.common.sslcert.model;

public record SecretTuple<T1, T2>(
    T1 t1, T2 t2
) {

    public T1 v1() {
        return t1;
    }

    public T2 v2() {
        return t2;
    }
}
