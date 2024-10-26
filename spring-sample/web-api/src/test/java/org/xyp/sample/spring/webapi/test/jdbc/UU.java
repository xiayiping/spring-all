package org.xyp.sample.spring.webapi.test.jdbc;

import lombok.val;

import java.util.HashSet;
import java.util.Set;

public class UU {
    public static void main(String[] args) {
        for (int i = 10; i <= 70; i++) {
            val v3 = i * i * i;
            val v4 = i * i * i * i;
            val v3s = Long.valueOf(v3).toString();
            val v4s = Long.valueOf(v4).toString();
            val set = new HashSet<Character>();

            if (v3s.length() != 4 || v4s.length() != 6) {
                continue;
            }
            for (int j = 0; j < v3s.length(); j++) {
                val c = v3s.charAt(j);
                set.add(c);
            }
            for (int j = 0; j < v4s.length(); j++) {
                set.add(v4s.charAt(j));
            }
            if (set.size() == 10) {
                System.out.println(i);
                System.out.println(set);
            } else {
                System.out.println(set);
            }
        }
    }
}
