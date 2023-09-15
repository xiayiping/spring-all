package org.xyp.demo.echo.check;

import org.xyp.demo.echo.pojo.User;

import java.util.Collections;
import java.util.List;

public class CheckTemplate<T> {
    private T local = null;

    public void add(T t) {
        this.local = t;
    }

    public T get() {
        return local;
    }

    public static <E extends User> List<E> setUser(E user, List<E> all) {
//        all.add(user);
        Collections.sort(null, null);
        User user1 = all.get(1);
        return all;
    }

    public static <F extends User> List<F> setUser2(F user, List<F> all) {
//        all.add(user);
        List<Object> lob = List.of();
        lob.add(new User());

//        List<? extends Object> lob2 = List.of();
//        lob2.add(new User());

//        List<User> ul = all;

        User user1 = all.get(1);
        return all;
    }

    public static <E extends User> List<E> setUser3(User user, List<E> all) {
//        all.add(user);
        Collections.sort(null, null);
        User user1 = all.get(1);
//        all.add(new CheckUserDao.SubUser());
        return all;
    }
    static class ABC<G extends User> {
    }
}
