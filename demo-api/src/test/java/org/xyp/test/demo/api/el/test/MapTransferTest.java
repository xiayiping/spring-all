package org.xyp.test.demo.api.el.test;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xyp.demo.api.maptransfer.MapTransfer;

import java.util.List;
import java.util.Map;

class MapTransferTest {
    @Test
    void test1() {
        var student = new Student("name1", "number1", 10);
        var transfer = new MapTransfer();
        var name1 = transfer.parseExpression(student, "${name}");
        var name2 = transfer.parseExpression(student, "name");
        var age1 = transfer.parseExpression(student, "${age}");
        var age2 = transfer.parseExpression(student, "#{${age}+1}");
        System.out.println(name1);
        System.out.println(name2);
        System.out.println(age1);
        System.out.println(age2);

        Assertions.assertEquals("10", age1);
        Assertions.assertEquals("11", age2);
        Assertions.assertEquals("name1", name1);
        Assertions.assertEquals("name", name2);
    }

    @Test
    void test2() {
        var student = new Student("name1", "number1", 10);
        var transfer = new MapTransfer();
        var name1 = transfer.parseExpression(student, "hello ${name}, your age is ${age}, next year you'll be #{${age}+1}.");
        System.out.println(name1);

        Assertions.assertEquals("hello name1, your age is 10, next year you'll be 11.", name1);
    }

    @Test
    void test3() {
        var student = Map.of("name", "name1"
                , "number", "number1"
                , "age", 10
                , "scores", List.of(
                        Map.of("cource", "c1", "score", 1),
                        Map.of("cource", "c2", "score", 3),
                        Map.of("cource", "c3", "score", 5)

                ));
        var transfer = new MapTransfer();
        var name1 = transfer.parseExpression(student, "hello ${name}, your age is ${age}, next year you'll be #{${age}+1}.");
        System.out.println(name1);
        var score = transfer.parseExpression(student, "${scores[1].score}");
        System.out.println(score);

        Assertions.assertEquals("hello name1, your age is 10, next year you'll be 11.", name1);
        Assertions.assertEquals("3", score);
    }

    record Student(String name, String number, int age) {
    }

}
