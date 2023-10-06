package org.xyp.test.demo.api.el.test;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

class ElTest {
    @Test
    void testEl() {
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression("'Hello World'");
        String message = (String) exp.getValue();
        Assertions.assertEquals("Hello World", message);

    }

    @Test
    void testContext() {
        GregorianCalendar c = new GregorianCalendar();
        c.set(1856, 7, 9);
        Inventor tesla = new Inventor("Nikola Tesla", c.getTime(), "Serbian");

        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression("name");
        EvaluationContext context = new StandardEvaluationContext(tesla);

        String name = (String) exp.getValue(context);
        Assertions.assertEquals("Nikola Tesla", name);
    }

    @Test
    void testContextObj1() {
        GregorianCalendar c = new GregorianCalendar();
        c.set(1856, 7, 9);
        Inventor tesla = new Inventor("Nikola Tesla", c.getTime(), "Serbian");

        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression("name");

        String name = (String) exp.getValue(tesla);
        Assertions.assertEquals("Nikola Tesla", name);
    }

    @Test
    void testContext1() {
        // Create an SpelExpressionParser
        SpelExpressionParser parser = new SpelExpressionParser();

        // Create a StandardEvaluationContext
        StandardEvaluationContext context = new StandardEvaluationContext();

        // Define variables in the context
        context.setVariable("name", "John");
        context.setVariable("age", 30);

        // Define an SpEL expression, '#' works for context.setVariable
        String expressionString = "#age";

        // Parse the expression
        Expression expression = parser.parseExpression(expressionString);

        // Evaluate the expression in the context
        String result = expression.getValue(context, String.class);

        // Print the result
        System.out.println(result);
    }

    @Test
    void testContext2() {
        Map<String, String> myMap = new HashMap<>();
        myMap.put("key1", "value1");
        myMap.put("key2", "value2");

        // Create an instance of SpelExpressionParser
        SpelExpressionParser parser = new SpelExpressionParser();

        // Define a SpEL expression to retrieve a value from the map
        String expressionString = "['key1']";

        // Parse the expression
        Expression expression = parser.parseExpression(expressionString);

        // Create an evaluation context and set the map as the root object
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setRootObject(myMap);

        // Evaluate the expression with the context
        String result = expression.getValue(context, String.class);

        // Print the result
        System.out.println(result); // Output: value1
    }

    @Test
    void testContext3() {
        var myMap = Map.of("key1", "value1", "key2", "value2");

        // Create an instance of SpelExpressionParser
        SpelExpressionParser parser = new SpelExpressionParser();

        // Define a SpEL expression to retrieve a value from the map
        String expressionString = "['key1'].length()";

        // Parse the expression
        Expression expression = parser.parseExpression(expressionString);

        // Create an evaluation context and set the map as the root object
        StandardEvaluationContext context = new StandardEvaluationContext(myMap);

        // Evaluate the expression with the context
        String result = expression.getValue(context, String.class);

        // Print the result
        System.out.println(result); // Output: value1
        Assertions.assertEquals("6", result);
    }

    @Test
    void testContext4() {
        var myMap = Map.of("key1", "value1", "key2", "value2");

        // Create an instance of SpelExpressionParser
        SpelExpressionParser parser = new SpelExpressionParser();

        // Define a SpEL expression to retrieve a value from the map
        String expressionString = "['key1'].length()";

        // Parse the expression
        Expression expression = parser.parseExpression(expressionString);

        // Create an evaluation context and set the map as the root object
        StandardEvaluationContext context = new StandardEvaluationContext(myMap);

        // Evaluate the expression with the context
        String result = expression.getValue(context, String.class);

        // Print the result
        System.out.println(result); // Output: value1
        Assertions.assertEquals("6", result);
    }

    @Test
    void testContext5() {
        var myMap = Map.of("key1", "value1", "key2", "value2");

        // Create an instance of SpelExpressionParser
        SpelExpressionParser parser = new SpelExpressionParser();

        // Define a SpEL expression to retrieve a value from the map
        String expressionString = "['key1'].length() + ['key1']";

        // Parse the expression
        Expression expression = parser.parseExpression(expressionString);

        // Create an evaluation context and set the map as the root object
        StandardEvaluationContext context = new StandardEvaluationContext(myMap);

        // Evaluate the expression with the context
        String result = expression.getValue(context, String.class);

        // Print the result
        System.out.println(result); // Output: value1
        Assertions.assertEquals("6value1", result);
    }

    @Test
    void testContextCondition() {
        var myMap = Map.of("key1", "value1", "key2", "value2"
                , "cond1", false,
                "inner", Map.of("cond2", true));

        // Create an instance of SpelExpressionParser
        SpelExpressionParser parser = new SpelExpressionParser();

        // Define a SpEL expression to retrieve a value from the map
        String expressionString = "['cond1'] ? 'Pass' : 'Fail'";
        // Parse the expression
        Expression expression = parser.parseExpression(expressionString);
        // Create an evaluation context and set the map as the root object
        StandardEvaluationContext context = new StandardEvaluationContext(myMap);
        // Evaluate the expression with the context
        String result = expression.getValue(context, String.class);
        // Print the result
        System.out.println(result); // Output: value1
        Assertions.assertEquals("Fail", result);
    }


    // https://docs.spring.io/spring-framework/docs/4.3.0.RELEASE/spring-framework-reference/html/expressions.html
    @Test
    void testContextCondition2() {
        var myMap = Map.of("key1", "value1", "key2", "value2"
                , "cond1", false,
                "inner", Map.of("cond2", true));

        // Create an instance of SpelExpressionParser
        SpelExpressionParser parser = new SpelExpressionParser();

        // ${inner.cond2} doesn't work
        // After parsing a valid expression, there is still more data in the expression: 'lcurly({)'
//        String expressionString = "${inner.cond2} ? 'Pass' : 'Fail'";

        // Property or field 'inner' cannot be found on object of type 'java.util.ImmutableCollections$MapN' -
//        String expressionString = "inner.cond2 ? 'Pass' : 'Fail'";

        // below only right way works for map
        String expressionString = "['inner']['cond2'] ? 'Pass' : 'Fail'";
        // Parse the expression
        Expression expression = parser.parseExpression(expressionString);
        // Create an evaluation context and set the map as the root object
        StandardEvaluationContext context = new StandardEvaluationContext(myMap);
        // Evaluate the expression with the context
        String result = expression.getValue(context, String.class);
        // Print the result
        System.out.println(result); // Output: value1
        Assertions.assertEquals("Pass", result);
    }

    // https://docs.spring.io/spring-framework/docs/4.3.0.RELEASE/spring-framework-reference/html/expressions.html
    @Test
    void testContextCondition3() {
        var myMap = Map.of("key1", "value1", "key2", "value2"
                , "cond1", false,
                "inner", Map.of("cond2", true));

        // Create an instance of SpelExpressionParser
        SpelExpressionParser parser = new SpelExpressionParser();

        // ${inner.cond2} doesn't work
        // After parsing a valid expression, there is still more data in the expression: 'lcurly({)'
//        String expressionString = "${inner.cond2} ? 'Pass' : 'Fail'";

        // Property or field 'inner' cannot be found on object of type 'java.util.ImmutableCollections$MapN' -
//        String expressionString = "inner.cond2 ? 'Pass' : 'Fail'";

        // below only right way works for map
        String expressionString = "#{['inner']['cond2'] ? 'Pass' : 'Fail'}";
        // Parse the expression
        Expression expression = parser.parseExpression(expressionString);
        // Create an evaluation context and set the map as the root object
        StandardEvaluationContext context = new StandardEvaluationContext(myMap);
        // Evaluate the expression with the context
        String result = expression.getValue(context, String.class);
        // Print the result
        System.out.println(result); // Output: value1
        Assertions.assertEquals("Pass", result);
    }

    @Test
    void testBeanUtils() {
        var myMap = Map.of("key1", "value1", "key2", "value2"
                , "cond1", false,
                "inner", Map.of("cond2", true, "key22", "value22"));
        PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess(myMap);

        var result = accessor.getPropertyValue("inner.key22");
        // Print the result
        System.out.println(result); // Output: value1
        Assertions.assertEquals("value22", result);
    }

    @Test
    void testPlaceHolder1() {
        // Create a PropertyPlaceholderHelper instance
        PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("${", "}");

        // Define a properties source (e.g., a properties file or map)
        String propertiesSource = "name=John\nage=30";

        // Define a string with a placeholder containing SpEL expression
        String input = "Hello, ${name}. You are ${a.b.c} ${age} #{${age}+5} years old.";

        // Parse and resolve placeholders
        String result = placeholderHelper.replacePlaceholders(input, placeholderName -> {
            // In this lambda, you can customize how to resolve placeholders
            if (placeholderName.equals("name")) {
                return "John";
            } else if (placeholderName.equals("age")) {
                return "30";
            } else {
                System.out.println("parsing " + placeholderName);
                return "??" + placeholderName +"??";
            }
        });

        // Print the result
        System.out.println(result);
    }

    @Test
    void testBeanProperty1() {

        GregorianCalendar c = new GregorianCalendar();
        c.set(1856, 7, 9);
        Inventor tesla = new Inventor("Nikola Tesla", c.getTime(), "Serbian");
        InventorWrapper iw = new InventorWrapper(tesla, "car");


        BeanWrapper wrapper = new BeanWrapperImpl(tesla);
        String city = (String) wrapper.getPropertyValue("name2");
        System.out.println(city);

        BeanWrapper wrapper2 = new BeanWrapperImpl(iw);
        String name2 = (String) wrapper2.getPropertyValue("cata");
        String name0 = (String) wrapper2.getPropertyValue("inventor.name");
        System.out.println(name2);
        System.out.println(name0);

    }

    @Test
    void testBeanProperty2() {

        final var tesla = Map.of("name", "Nikola Tesla"
        , "date", "2022-02-02"
        , "name2", "tesla2");
        final var iw = Map.of("inventor", "tesla"
                , "cata", "car");

        BeanWrapper wrapper = new BeanWrapperImpl(tesla);
        String city = (String) wrapper.getPropertyValue("name2");
        System.out.println(city);

        BeanWrapper wrapper2 = new BeanWrapperImpl(iw);
        String name2 = (String) wrapper2.getPropertyValue("cata");
        String name0 = (String) wrapper2.getPropertyValue("inventor.name");
        System.out.println(name2);
        System.out.println(name0);

    }


    record Inventor(String name, Date date, String name2) {
    }

    record InventorWrapper(Inventor inventor, String cata) {
    }

}
