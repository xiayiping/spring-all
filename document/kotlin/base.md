# Anonymous Inner class

Yes, in Kotlin, you create an **anonymous inner class implementation** using the `object : ClassName` syntax. This is used to create an instance of a class or an interface and override its methods (if applicable) on the fly, without explicitly creating a new subclass.

---

### **Syntax**
```kotlin
val instance = object : ClassName() {
    // Override methods or add additional functionality here
}
```

- If `ClassName` is an **interface** or an **abstract class**, you must implement its abstract methods in the `object` block.
- If `ClassName` is a **concrete class**, you can override its methods or properties if they are open.

---

### **Examples**

#### **1. Implementing an Interface**
For example, consider an interface `OnClickListener`:

```kotlin
interface OnClickListener {
    fun onClick()
}
```

You can create an anonymous inner class implementation using `object`:

```kotlin
val clickListener = object : OnClickListener {
    override fun onClick() {
        println("Button clicked!")
    }
}

// Use it
clickListener.onClick()
```

---

#### **2. Extending an Abstract Class**
For an abstract class, you must override its abstract methods:

```kotlin
abstract class Animal {
    abstract fun sound()
}

val dog = object : Animal() {
    override fun sound() {
        println("Woof!")
    }
}

// Use it
dog.sound()
```

---

#### **3. Extending a Concrete Class**
If the class is not abstract but has `open` methods or properties, you can override them:

```kotlin
open class Greeting {
    open fun sayHello() {
        println("Hello!")
    }
}

val customGreeting = object : Greeting() {
    override fun sayHello() {
        println("Hi there!")
    }
}

// Use it
customGreeting.sayHello()
```

---

#### **4. Providing an Anonymous Listener (Common Use Case)**
For example, in Android development, you often provide an anonymous `View.OnClickListener`:

```kotlin
button.setOnClickListener(object : View.OnClickListener {
    override fun onClick(v: View?) {
        println("Button clicked!")
    }
})
```

---

### **When to Use Anonymous Inner Classes**
Use anonymous inner classes when:
1. You need a one-off implementation of an interface or class.
2. The implementation is short and doesn't require a separate named class.
3. You want to pass behavior (e.g., listeners, callbacks) directly.

---

### **Key Points**
- The `object` keyword in Kotlin is used for creating **anonymous objects** for interfaces, abstract classes, or concrete classes.
- Anonymous objects are **not reusable** and are meant for short-lived, specific use cases.
- If an anonymous object is assigned to a variable with a non-specific type (e.g., `Any` or its superclass), its additional overridden methods or properties will not be accessible.

Example:
```kotlin
val obj: Any = object {
    val name = "Anonymous"
}
// obj.name // Error: 'name' is not accessible because the type is 'Any'
```

If you need the additional properties or methods, you must use the specific type of the anonymous object.

# Delegate

In Kotlin, the `by` keyword is used for **delegation**. It allows one object to delegate a property or functionality to another object. In the context of `private var abc by viewModels<Abc>()`, the `by` keyword is being used for **property delegation**.

---

### **What Does `private var abc by viewModels<Abc>()` Mean?**

In this specific case, `viewModels<Abc>()` is a **property delegate** provided by Android's Jetpack libraries for managing **ViewModels** in a lifecycle-aware way. The `by` keyword delegates the property `abc` to the result of `viewModels<Abc>()`.

The `viewModels()` function is a delegate that:
1. Lazily initializes the `ViewModel` instance when it is accessed for the first time.
2. Ensures the `ViewModel` instance is scoped to the lifecycle of the component (e.g., an `Activity` or `Fragment`).

---

### **Breakdown of the Components**

#### **1. `private var abc`**
This declares a private property `abc` for the given class (such as an `Activity` or `Fragment`).

#### **2. `by`**
The `by` keyword is used for **delegation**. It means that the `abc` property doesn't directly hold the `ViewModel` instance. Instead, the `viewModels<Abc>()` delegate manages it for you, and any access to `abc` is forwarded to this delegate.

#### **3. `viewModels<Abc>()`**
`viewModels<T>()` is a Jetpack **KTX extension function** used to obtain a `ViewModel` instance of type `T`. It ensures:
- The `ViewModel` is created only once.
- It is tied to the lifecycle of the component (e.g., the `Fragment` or `Activity`).

For example:
- In an `Activity`, the `ViewModel` will be scoped to the activity's lifecycle.
- In a `Fragment`, the `ViewModel` will be scoped to the fragment's lifecycle.

---

### **How It Works**

When you declare `private var abc by viewModels<Abc>()`, the following happens:
1. **Lazy Initialization**:
    - The `viewModels` delegate ensures that the `ViewModel` (`Abc`) is only created when it is accessed for the first time.
2. **Lifecycle Awareness**:
    - The `viewModels` delegate automatically manages the `ViewModel` lifecycle, ensuring that the same instance of the `ViewModel` is provided as long as the lifecycle owner (e.g., `Activity` or `Fragment`) exists.
3. **Delegation**:
    - The `abc` property delegates the responsibility of managing the `ViewModel` instance to the `viewModels` delegate.

---

### **Usage Example**

#### **In a Fragment**
```kotlin
class MyFragment : Fragment() {
    // Get the ViewModel scoped to the Fragment's lifecycle
    private val myViewModel by viewModels<MyViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Use the ViewModel
        myViewModel.someLiveData.observe(viewLifecycleOwner) { data ->
            // Update the UI
        }
    }
}
```

#### **In an Activity**
```kotlin
class MyActivity : AppCompatActivity() {
    // Get the ViewModel scoped to the Activity's lifecycle
    private val myViewModel by viewModels<MyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use the ViewModel
        myViewModel.someLiveData.observe(this) { data ->
            // Update the UI
        }
    }
}
```

---

### **Why Use `by viewModels()`?**

- **Simplifies ViewModel Initialization**:
  You don't need to manually use `ViewModelProvider` to get an instance of the `ViewModel`. The `viewModels()` delegate handles this for you.

- **Lifecycle Awareness**:
  The delegate ensures the `ViewModel` is tied to the correct lifecycle (activity or fragment).

- **Cleaner Code**:
  The use of `by` makes the code concise and leverages Kotlin's property delegation.

---

### **What Happens Without `by viewModels()`?**

Without using `viewModels()`, you would have to manually retrieve the `ViewModel` instance using `ViewModelProvider`, like this:

```kotlin
class MyFragment : Fragment() {
    private lateinit var myViewModel: MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myViewModel = ViewModelProvider(this).get(MyViewModel::class.java)
    }
}
```

This approach is more verbose and requires you to explicitly manage the `ViewModel`.

---

### **Conclusion**

- The `by` keyword in `private var abc by viewModels<Abc>()` is used for **property delegation**.
- The `viewModels()` function is a **delegate** that simplifies `ViewModel` initialization and ensures lifecycle-aware management.
- It is a cleaner and more idiomatic way to use `ViewModel` in Kotlin, especially in Android development.

Here’s how you can write your own **delegate** in Kotlin to log every `get` and `set` access to a property.

---

### **Custom Property Delegate Example**

#### **Creating the Delegate**
You can create a custom delegate by implementing the `ReadWriteProperty` interface (if the property is mutable) or `ReadOnlyProperty` (if it's immutable). Here, we'll create a `LoggingDelegate` that logs when the property is accessed or modified.

```kotlin
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LoggingDelegate<T>(private var value: T) : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        println("Getting value '${property.name}', current value: $value")
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        println("Setting value '${property.name}' to $value (was: ${this.value})")
        this.value = value
    }
}
```

---

#### **Using the Delegate**

Now, you can use the `LoggingDelegate` to delegate a property in a class.

```kotlin
class MyClass {
    var myProperty: String by LoggingDelegate("Initial Value")
    var number: Int by LoggingDelegate(0)
}

fun main() {
    val obj = MyClass()

    // Access and modify the property
    println(obj.myProperty) // Logs the get access
    obj.myProperty = "New Value" // Logs the set access
    println(obj.myProperty) // Logs the get access again

    // Access and modify another delegated property
    println(obj.number) // Logs the get access
    obj.number = 42 // Logs the set access
    println(obj.number) // Logs the get access again
}
```

---

#### **Output**

When you run the above code, you’ll see the following logs:

```
Getting value 'myProperty', current value: Initial Value
Initial Value
Setting value 'myProperty' to New Value (was: Initial Value)
Getting value 'myProperty', current value: New Value
New Value
Getting value 'number', current value: 0
0
Setting value 'number' to 42 (was: 0)
Getting value 'number', current value: 42
42
```

---

### **How It Works**

1. **Delegate Class (`LoggingDelegate`)**:
    - Implements the `ReadWriteProperty` interface.
    - Provides custom behavior for `getValue` (called when the property is accessed) and `setValue` (called when the property is assigned a new value).
    - Logs the property name and its value during `get` and `set`.

2. **Usage with `by`**:
    - The `by` keyword delegates the `myProperty` property in `MyClass` to the `LoggingDelegate` instance.
    - Every time the property is accessed or modified, the `LoggingDelegate`'s `getValue` or `setValue` methods are called.

---

### **Simpler Example Without Interfaces**

If you don't need to use `ReadWriteProperty`, you can just use a simple custom getter and setter in a backing property.

```kotlin
class MyClass {
    private var _myProperty: String = "Initial Value"

    var myProperty: String
        get() {
            println("Getting value of myProperty: $_myProperty")
            return _myProperty
        }
        set(value) {
            println("Setting value of myProperty to $value (was: $_myProperty)")
            _myProperty = value
        }
}

fun main() {
    val obj = MyClass()
    println(obj.myProperty) // Logs the get access
    obj.myProperty = "New Value" // Logs the set access
    println(obj.myProperty) // Logs the get access again
}
```

This is simpler but doesn't use delegation (`by`). The `LoggingDelegate` example is more idiomatic and reusable.

---

### **Why Use a Delegate?**
- **Code Reusability**: You can apply the same behavior (e.g., logging, caching, validation) to multiple properties without rewriting the logic every time.
- **Encapsulation**: The logic for `get` and `set` behavior is encapsulated in the delegate class, keeping your main class cleaner.

# Extension Function
```kotlin

fun MutableList<Int>.swap(index1: Int, index2: Int) {
    val tmp = this[index1] // 'this' corresponds to the list
    this[index1] = this[index2]
    this[index2] = tmp
}
```

Extensions do not actually modify the classes they extend. By defining an extension, you are not inserting new members into a class, only making new functions callable with the dot-notation on variables of this type.

Extension functions are dispatched statically. So which extension function is called is already known at compile time based on the receiver type. For example:

```kotlin
open class Shape
class Rectangle: Shape()

fun Shape.getName() = "Shape"
fun Rectangle.getName() = "Rectangle"

fun printClassName(s: Shape) {
    println(s.getName())
}

printClassName(Rectangle())

```
This example prints **Shape**, because the extension function called depends only on the declared type of the 
parameter s, which is the Shape class.

If a class has a member function, and an extension function is defined which has the same receiver type, the same name, 
and is applicable to given arguments, the member always wins. For example:

```kotlin
class Example {
    fun printFunctionType() { println("Class method") }
}

fun Example.printFunctionType() { println("Extension function") }

Example().printFunctionType()

```

This code prints **Class method**.

However, it's perfectly OK for extension functions to overload member functions that have the same name but a different signature:

```kotlin

class Example {
    fun printFunctionType() { println("Class method") }
}

fun Example.printFunctionType(i: Int) { println("Extension function #$i") }

Example().printFunctionType(1)

```

### Nullable receiver

Note that extensions can be defined with a nullable receiver type. 
These extensions can be called on an object variable even if its value is null. 
If the receiver is null, then this is also null. So when defining an extension with a nullable receiver type, 
we recommend performing a this == null check inside the function body to avoid compiler errors.

You can call toString() in Kotlin without checking for null, as the check already happens inside the extension function:

```kotlin
fun Any?.toString(): String {
    if (this == null) return "null"
    // After the null check, 'this' is autocast to a non-nullable type, so the toString() below
    // resolves to the member function of the Any class
    return toString()
}

```

### reference

for `Extention Properties`, `Companion object extensions`, `Scope of extensions` , `Declaring extensions as members`

https://kotlinlang.org/docs/extensions.html#scope-of-extensions


# By lazy and lateinit

In Kotlin, `by lazy` and `lateinit` are two different ways to handle non-null properties that are initialized later, but they have distinct use cases and behaviors. Here's a detailed comparison:

---

### **1. `by lazy`**
- **Definition**: `by lazy` is a property delegate used to initialize a property **lazily**, meaning the property will be initialized **only when it is accessed for the first time**.
- **Key Characteristics**:
    - The property is **read-only** (`val`).
    - The initialization block is executed only once and the value is cached for future access.
    - Thread-safe by default (unless explicitly set otherwise).
    - Useful for properties that are expensive to compute or may not always be needed.

- **Usage**:
  ```kotlin
  val lazyValue: String by lazy {
      println("Initializing lazyValue...")
      "Hello, Lazy Initialization"
  }

  fun main() {
      println("Before accessing lazyValue")
      println(lazyValue) // Initialization happens here
      println(lazyValue) // Cached value is used
  }

  // Output:
  // Before accessing lazyValue
  // Initializing lazyValue...
  // Hello, Lazy Initialization
  // Hello, Lazy Initialization
  ```

- **Use Case**:
    - When you want to initialize a property lazily and make it immutable (`val`).
    - For example, initializing a property that depends on a database query or a computation that may not always be needed.

---

### **2. `lateinit`**
- **Definition**: `lateinit` is a keyword that allows you to declare a **non-null `var` property** without initializing it at the time of declaration. The property must be initialized before it is accessed, otherwise, it will throw an exception.
- **Key Characteristics**:
    - The property is **mutable** (`var`).
    - You must manually initialize it before accessing it.
    - Cannot be used with `val` or primitive types (e.g., `Int`, `Double`).
    - Throws an exception (`UninitializedPropertyAccessException`) if accessed before initialization.

- **Usage**:
  ```kotlin
  lateinit var lateinitValue: String

  fun main() {
      println("Before initializing lateinitValue")
      lateinitValue = "Hello, Late Initialization"
      println(lateinitValue) // Access after manual initialization
  }
  ```

- **Use Case**:
    - When you need to initialize a mutable property (`var`) later, but you're sure it will be initialized before usage.
    - Commonly used in Android development for properties like views (e.g., `TextView`, `RecyclerView`) where initialization happens after `onCreate`.

---

### **Key Differences**

| Feature                 | `by lazy`                                       | `lateinit`                                      |
|-------------------------|------------------------------------------------|------------------------------------------------|
| **Property type**       | `val` (immutable, read-only)                   | `var` (mutable)                                |
| **Initialization**      | Lazy (on first access)                         | Manual (must be explicitly set before access)  |
| **Primitive types**     | Supported                                      | Not supported                                  |
| **Thread-safety**       | Thread-safe by default                         | Not thread-safe                                |
| **Exception Handling**  | No exceptions (value is computed on access)    | Throws `UninitializedPropertyAccessException` if accessed before initialization |
| **Use Case**            | For expensive computations or immutable properties | For mutable properties that must be initialized later |

---

### **When to Use What?**

- Use **`by lazy`** when:
    - The property is immutable (`val`).
    - The initialization is expensive or conditional and you want it to be initialized only when needed.
    - Thread-safety is important (e.g., shared resources).

- Use **`lateinit`** when:
    - The property is mutable (`var`).
    - You need to initialize it later in the lifecycle (e.g., in Android `onCreate()` or `onViewCreated()`).
    - You're certain that the property will be initialized before accessing it.

---

### **Example in Android**

- **`lateinit` Example**:
  ```kotlin
  class MainActivity : AppCompatActivity() {
      lateinit var textView: TextView

      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          setContentView(R.layout.activity_main)

          textView = findViewById(R.id.textView) // Initialize later
          textView.text = "Hello, World!"
      }
  }
  ```

- **`by lazy` Example**:
  ```kotlin
  class MainActivity : AppCompatActivity() {
      val database by lazy { Database.getInstance(this) } // Initialize lazily

      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          setContentView(R.layout.activity_main)

          database.queryData() // Database is initialized here
      }
  }
  ```

In summary, use `by lazy` for **lazy immutable properties** and `lateinit` for **mutable properties that are initialized later**. Choose based on the requirements of your use case!


# Callable Reference Operator

In Kotlin, the syntax `Class::class.java` is used to access the **Java `Class` object** of a Kotlin class. To understand this, let's break it down:

---

### **What Does `Class::class.java` Mean?**

1. **`Class`**: This represents the name of the Kotlin class.
2. **`::`**: This is the **callable reference operator** in Kotlin. It is used to refer to **class references**, **function references**, or **property references**.
3. **`Class::class`**: This refers to the Kotlin **KClass** object of the class `Class`.
    - `KClass` is a Kotlin-specific representation of a class, and it is part of the **Kotlin Reflection API**.
4. **`.java`**: This converts the Kotlin `KClass` object into a Java `Class` object.
    - The `java` property is a part of Kotlin's interop with Java.

---

### **Example Usage of `Class::class.java`**
Here’s an example to demonstrate its use:

```kotlin
class MyClass

fun main() {
    // Get the Java Class object of the Kotlin class
    val javaClass: Class<MyClass> = MyClass::class.java
    println(javaClass.name) // Output: MyClass
}
```

In this example:
- `MyClass::class` gives the Kotlin `KClass` object for `MyClass`.
- `MyClass::class.java` converts it into the corresponding Java `Class` object.

---

### **What Does `::` Mean in Kotlin?**

The `::` operator is called the **callable reference operator**. It is used to refer to:
1. **Class References**
2. **Function References**
3. **Property References**

Here’s a breakdown of its use cases:

---

#### **1. Class References**
The `::class` is used to get a **KClass** object for a class.

Example:
```kotlin
class MyClass

fun main() {
    val kClass = MyClass::class
    println(kClass.simpleName) // Output: MyClass
}
```

---

#### **2. Function References**
The `::` operator can be used to create a reference to a function. This is commonly used in higher-order functions.

Example:
```kotlin
fun greet(name: String) = "Hello, $name!"

fun main() {
    val greetFunction = ::greet
    println(greetFunction("Kotlin")) // Output: Hello, Kotlin!
}
```

Here:
- `::greet` creates a reference to the `greet` function.
- You can call the function using `greetFunction()`.

---

#### **3. Property References**
The `::` operator can also be used to refer to a **property**, allowing you to get or set its value reflectively.

Example:
```kotlin
var myProperty = "Kotlin"

fun main() {
    val propertyReference = ::myProperty
    println(propertyReference.get()) // Output: Kotlin
    propertyReference.set("New Value")
    println(myProperty) // Output: New Value
}
```

Here:
- `::myProperty` creates a reference to the `myProperty` variable.
- You can use `.get()` to retrieve its value and `.set()` to modify it.

---

### **Other Common Uses of `::`**

#### **1. Using `::` with Higher-Order Functions**
The callable reference operator (`::`) is often used when passing functions as arguments to higher-order functions.

Example:
```kotlin
fun isEven(number: Int) = number % 2 == 0

fun main() {
    val numbers = listOf(1, 2, 3, 4)
    val evenNumbers = numbers.filter(::isEven) // Pass function reference
    println(evenNumbers) // Output: [2, 4]
}
```

Here:
- `::isEven` is passed as a function reference to the `filter` function.

---

#### **2. Constructor References**
You can use `::` to refer to a class constructor.

Example:
```kotlin
class Person(val name: String)

fun main() {
    val createPerson = ::Person
    val person = createPerson("Kotlin")
    println(person.name) // Output: Kotlin
}
```

Here:
- `::Person` creates a reference to the constructor of the `Person` class.

---

#### **3. Bound Function References**
You can create a function reference that is bound to an object instance.

Example:
```kotlin
class Greeter {
    fun greet(name: String) = "Hello, $name!"
}

fun main() {
    val greeter = Greeter()
    val greetFunction = greeter::greet // Bound to the instance `greeter`
    println(greetFunction("Kotlin")) // Output: Hello, Kotlin!
}
```

Here:
- `greeter::greet` creates a reference to the `greet` method, bound to the `greeter` instance.

---

### **Summary**

- `::` is the **callable reference operator** in Kotlin.
- It has many uses, such as:
    - **Class references** (`Class::class` or `Class::class.java`).
    - **Function references** (`::functionName`).
    - **Property references** (`::propertyName`).
    - **Constructor references** (`::ClassName`).
    - **Bound references** (`instance::functionName`).

The `Class::class.java` syntax is specifically for obtaining the **Java `Class` object** of a Kotlin class, which is useful when working with Java-based libraries or frameworks.

