# MultipleStateOf

### **What is `mutableStateOf` in Kotlin?**

`mutableStateOf` is part of **Jetpack Compose**, a modern UI toolkit for building declarative UIs in Android. It is used to hold and observe state in a **Composable function**. Specifically, `mutableStateOf` creates a state object that automatically notifies any Composables observing it whenever its value changes, allowing the UI to recompose with the updated state.

#### **Basic Syntax**
```kotlin
val myState = mutableStateOf(initialValue)
```

- `mutableStateOf` is commonly used in a **ViewModel** to manage UI state in a Jetpack Compose app.
- **Type of `mutableStateOf`**: It returns a `MutableState<T>` object, where `T` is the type of the value being stored.

---

### **Usage of `mutableStateOf` in a ViewModel**

In Jetpack Compose, a `ViewModel` often uses `mutableStateOf` to expose UI state. The `mutableStateOf` makes it easy to hold state in a reactive way, and whenever the state changes, the observing Composables automatically recompose to reflect the updated state.

#### **Example: Using `mutableStateOf` in a ViewModel**
Here's an example where a `ViewModel` manages a counter using `mutableStateOf`:

```kotlin
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class CounterViewModel : ViewModel() {
    // Expose a state object to manage the counter
    var counter = mutableStateOf(0)
        private set // Prevent external modification

    // Increment the counter
    fun increment() {
        counter.value += 1
    }

    // Decrement the counter
    fun decrement() {
        counter.value -= 1
    }
}
```

#### **Using the ViewModel in a Composable**
```kotlin
@Composable
fun CounterScreen(viewModel: CounterViewModel = viewModel()) {
    val count = viewModel.counter.value // Observe the state

    Column {
        Text(text = "Count: $count")
        Button(onClick = { viewModel.increment() }) {
            Text("Increment")
        }
        Button(onClick = { viewModel.decrement() }) {
            Text("Decrement")
        }
    }
}
```

Here:
- The `counter` state is exposed via `mutableStateOf`.
- When the `increment` or `decrement` function is called, the value of `counter` changes, and the `Text` Composable automatically recomposes.

---

### **Can I Use a Pure Type Like `Int` in a ViewModel?**

Yes, you **can** use a pure type like `Int` in a `ViewModel` to hold a value, but it will not automatically notify the UI of changes. Jetpack Compose relies on state objects like `mutableStateOf` for state observation and recomposition.

#### **Example: Pure `Int` in a ViewModel**
```kotlin
class CounterViewModel : ViewModel() {
    var counter = 0
        private set

    fun increment() {
        counter += 1
    }

    fun decrement() {
        counter -= 1
    }
}
```

#### **Using It in a Composable**
```kotlin
@Composable
fun CounterScreen(viewModel: CounterViewModel = viewModel()) {
    val count = viewModel.counter // This won't recompose automatically

    Column {
        Text(text = "Count: $count") // Won't update on state change
        Button(onClick = { viewModel.increment() }) {
            Text("Increment")
        }
        Button(onClick = { viewModel.decrement() }) {
            Text("Decrement")
        }
    }
}
```

**Problem**:
- The `Text` Composable does not automatically update when the `counter` value changes because Compose does not observe pure types like `Int`.

---

### **Why Use `mutableStateOf` Instead of Pure Types?**

Jetpack Compose is designed around **reactive programming**, where the UI reacts to state changes. Using `mutableStateOf` ensures that the state is observable and triggers recomposition whenever the state changes.

#### Key Differences:
| **Feature**                | **Pure Type (e.g., Int)**             | **`mutableStateOf`**                    |
|----------------------------|---------------------------------------|-----------------------------------------|
| **Recomposition**          | No (UI won't recompose automatically) | Yes (UI recomposes when the state changes) |
| **State Observation**      | Manual (e.g., via LiveData or Flow)   | Automatic (built-in Compose state management) |
| **Ease of Use in Compose** | Requires additional setup             | Native to Compose, easy to use          |

---

### **Best Practices**

1. **For Jetpack Compose Apps:**
    - Use `mutableStateOf` (or other state management solutions like `StateFlow` or `LiveData`) in a `ViewModel` to manage and expose state reactively.

2. **For Traditional XML-Based Views:**
    - Use `LiveData` or `Flow` in `ViewModel` to manage and observe state changes.

---

### **Other State Management Options in Jetpack Compose**

Apart from `mutableStateOf`, there are other state management approaches that can be used with Compose:

#### **1. `StateFlow` (Recommended for Complex State)**
- Use `StateFlow` from Kotlin's `Flow` API to manage state in a reactive way.
- Example:
  ```kotlin
  class CounterViewModel : ViewModel() {
      private val _counter = MutableStateFlow(0)
      val counter: StateFlow<Int> = _counter

      fun increment() {
          _counter.value += 1
      }

      fun decrement() {
          _counter.value -= 1
      }
  }
  ```

  In a Composable:
  ```kotlin
  @Composable
  fun CounterScreen(viewModel: CounterViewModel = viewModel()) {
      val count by viewModel.counter.collectAsState()
      Text("Count: $count")
  }
  ```

#### **2. `LiveData`**
- If you're using LiveData:
  ```kotlin
  class CounterViewModel : ViewModel() {
      private val _counter = MutableLiveData(0)
      val counter: LiveData<Int> = _counter

      fun increment() {
          _counter.value = (_counter.value ?: 0) + 1
      }
  }
  ```

  In a Composable:
  ```kotlin
  @Composable
  fun CounterScreen(viewModel: CounterViewModel = viewModel()) {
      val count by viewModel.counter.observeAsState(0)
      Text("Count: $count")
  }
  ```

---

### **Summary**

- **`mutableStateOf`** is part of Compose's state management and is used to hold observable state that triggers recomposition in the UI.
- You **can** use a pure type like `Int` in a `ViewModel`, but it won't automatically notify Compose to recompose the UI.
- For Compose apps, using `mutableStateOf`, `StateFlow`, or `LiveData` is the preferred way to manage and expose state reactively.
