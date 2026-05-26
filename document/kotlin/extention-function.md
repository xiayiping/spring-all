# Extension Function

### What is an Extension Function in Kotlin?

An **extension function** in Kotlin is a feature that allows you to add new functions to existing classes **without modifying their source code**. You can define these functions outside the class, but they behave as if they are part of the class. This is useful when you want to add functionality to a class you don’t own, such as classes from the standard library or third-party libraries.

**Key Points:**
- Extension functions **do not modify the underlying class**. They are just a syntactic convenience.
- You define the extension function using the class name as a receiver type.
- Inside the extension function, you can use `this` to refer to the class instance.

**Example of an Extension Function**:
```kotlin
class App(val name: String)

// Define an extension function for the App class
fun App.greet() {
    println("Hello from $name")
}

fun main() {
    val app = App("MyApp")
    app.greet() // Output: Hello from MyApp
}
```

Here, `greet()` is an **extension function** for the `App` class. Even though `greet()` is not defined inside `App`, it behaves as if it were.

---

### How is it Different from Object and Companion Functions?

To understand the difference between **extension functions**, **object functions**, and **companion functions**, let’s break them down:

---

### 1. **Object Functions**
An **object function** is a function defined in an `object`. An `object` in Kotlin is a **singleton**, meaning it’s a class that can have only one instance and is instantiated automatically. Functions defined in an `object` are inherently static-like since they belong to the single instance of the object.

**Example**:
```kotlin
object App {
    fun greet() {
        println("Hello from App object")
    }
}

fun main() {
    App.greet() // Output: Hello from App object
}
```

**Key Features of Object Functions**:
- Belong to a singleton instance.
- Are inherently static-like: you call them using the object name.
- Useful for defining global utilities or managing single-instance behavior.

---

### 2. **Companion Functions**
A **companion function** is a function defined inside a `companion object` within a class. A `companion object` is a singleton associated with a class, and its functions can be called using the class name (similar to static methods in Java).

**Example**:
```kotlin
class App {
    companion object {
        fun greet() {
            println("Hello from companion object")
        }
    }
}

fun main() {
    App.greet() // Output: Hello from companion object
}
```

**Key Features of Companion Functions**:
- Belong to the `companion object` of a class.
- Behave like static methods in Java.
- Can access private members of the enclosing class.

---

### 3. **Extension Functions**
An **extension function** is a function that is not defined inside a class, object, or companion object but is instead defined outside of it. It is "added" to an existing class, allowing you to call it as if it were a member of that class.

**Example**:
```kotlin
class App(val name: String)

// Define an extension function for the App class
fun App.greet() {
    println("Hello from $name")
}

fun main() {
    val app = App("MyApp")
    app.greet() // Output: Hello from MyApp
}
```

**Key Features of Extension Functions**:
- Do not belong to the class but behave as if they do.
- Cannot access private or protected members of the class (because they are not actually part of the class).
- Useful for adding functionality to third-party or standard library classes.

---

### Difference Between the Three

| **Feature**                  | **Object Function**                  | **Companion Function**             | **Extension Function**             |
|------------------------------|---------------------------------------|-------------------------------------|-------------------------------------|
| **Definition**               | Inside an `object`                   | Inside a `companion object`         | Outside the class                  |
| **Call Syntax**              | Called via the `object` name         | Called via the class name           | Called via an instance of the class |
| **Static-Like Behavior**     | Yes                                  | Yes                                 | No                                 |
| **Access to Class Members**  | No (unless inside the same class)    | Yes (can access private members)    | No (cannot access private members) |
| **Use Case**                 | Singleton utilities                  | Static-like methods for the class   | Adding functionality to existing classes |

---

### Practical Example to Show the Differences

```kotlin
class App(val name: String) {
    companion object {
        fun companionGreet() {
            println("Hello from the companion object")
        }
    }
}

object AppSingleton {
    fun objectGreet() {
        println("Hello from the singleton object")
    }
}

// Extension function for the App class
fun App.extensionGreet() {
    println("Hello from $name (extension function)")
}

fun main() {
    // Companion function
    App.companionGreet() // Output: Hello from the companion object

    // Object function
    AppSingleton.objectGreet() // Output: Hello from the singleton object

    // Extension function
    val app = App("MyApp")
    app.extensionGreet() // Output: Hello from MyApp (extension function)
}
```

---

### Key Takeaways:
1. **Object functions** are static-like and belong to a singleton object.
2. **Companion functions** are static-like and belong to the companion object of a class.
3. **Extension functions** are syntactic sugar to add new functions to existing classes but are not truly part of the class. They cannot access private members of the class.

