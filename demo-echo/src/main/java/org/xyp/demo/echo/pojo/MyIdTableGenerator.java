package org.xyp.demo.echo.pojo;

public class MyIdTableGenerator {
    private MyIdTableGenerator() {}
    public static final String TABLE = "sequence_table";
    public static final String PK_COLUMN = "sequence_name";
    public static final String VALUE_COLUMN = "next_val";
}
