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

