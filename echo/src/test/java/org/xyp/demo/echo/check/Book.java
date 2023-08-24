package org.xyp.demo.echo.check;

public class Book {

    private String name;

    private final transient String author;

    public Book(String auth) {
        this.author = auth;
    }
}
