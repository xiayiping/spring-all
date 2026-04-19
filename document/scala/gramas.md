
# For loop

```scala

val result = for {
    i <- lst 
    if i < 6
} yield {
    i * i
}

println (result);

```

# partial applied function

```scala

val sum = (a: Int, b: Int, c: Int) => a + b + c;

val f = sum (10, 20, _: Int);

println(f(30))   // will print 60
```