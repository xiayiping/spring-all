package org.xyp.sample.spring.secret.pojo;

public class SecretTuple<T1, T2> {
    T1 v1;
    T2 v2;

    public SecretTuple(T1 v1, T2 v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public T1 v1() {
        return v1;
    }
    public T2 v2() {
        return v2;
    }
}

