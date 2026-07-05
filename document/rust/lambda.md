# lambda headed by 'move'

In Rust, closures (a.k.a. lambdas) **don’t always need to be headed with `move`**, but there are specific scenarios where the `move` keyword is **required**. The example you provided is one such scenario, and here’s why:

---

### **1. What Does `move` Mean in Rust?**

When you prefix a closure with `move`, it means **the closure takes ownership** of the variables it uses from its enclosing scope. Instead of borrowing variables from its surrounding environment, the closure moves them into its own scope.

This is necessary in certain situations like multithreading, where the closure's lifetime may outlive the original scope.

In your case, the `move` is required because:
- The `thread::spawn` method takes a closure that must be **'static** (it must own all the data it captures, so it is independent of the enclosing scope).

---

### **2. Why is `move` Required in `thread::spawn`?**

The `thread::spawn` function starts a new thread. Because the thread runs independently of the main thread, any variables that the thread’s closure uses need to stay valid after the main thread executes.

- Without the `move`, the closure **borrows** variables from the outer scope, and when the main thread exits the scope, the borrowed variables are dropped. This would lead to a dangling reference in the spawned thread, which is unsafe.

By adding the `move` keyword:
- The closure takes **ownership** of the `list`, ensuring it lives long enough for the spawned thread to use it.
- Ownership transfer ensures the `list`'s memory doesn't get freed prematurely in the main thread.

---

### **Concrete Behavior Breakdown in Your Code**

#### Your Code:
```rust
let list = vec![1, 2, 3];
println!("Before defining closure: {list:?}");

thread::spawn(move || println!("From thread: {list:?}"))
    .join()
    .unwrap();
```

#### Without `move`:
```rust
thread::spawn(|| println!("From thread: {list:?}"))
    .join()
    .unwrap();
```

- **What happens?**
    - The closure tries to "borrow" the `list` from the outer scope.
    - When the spawned thread attempts to run, it could find that the main thread has already dropped the `list`, causing a dangling reference and undefined behavior.
    - Rust catches this borrowing issue at **compile time**.

- **Why?**
    - The `thread::spawn` function requires the closure to have a `'static` lifetime.
    - To achieve this, the closure must take **ownership** of any variables it captures, which is done by using the `move` keyword.

#### With `move`:
```rust
thread::spawn(move || println!("From thread: {list:?}"))
    .join()
    .unwrap();
```

- **What happens?**
    - The closure explicitly **takes ownership** of `list`.
    - The ownership of `list` is passed into the closure when it moves into the spawned thread.
    - This ensures the `list` lives long enough for the thread to safely access it.

---

### **3. When is `move` Not Required?**

Closure captures in Rust can also borrow variables. The `move` keyword is **not required** for closures that don't need to **own** the captured variables. For example:

#### Example Without `move`:
```rust
fn main() {
    let list = vec![1, 2, 3];
    let closure = || println!("{:?}", list); // Borrowing list
    closure(); // Works because `list` still exists in the scope
}
```

- This works because:
    - The outer scope owns the `list`.
    - The closure borrows `list` (it doesn’t move it or require `'static` lifetime).
    - `list` is still valid when the closure is executed.

---

### **4. When Should You Use `move`?**

Here are the common scenarios where `move` is required:
1. **Multithreading** (`thread::spawn` or similar async operations):
    - The closure must own its environment because it will run in a separate execution context that might outlive the current scope.

2. **Higher-order functions requiring `'static`:**
    - Functions like `tokio::spawn()` or `thread::spawn()` require closures to be `'static`.

3. **Closures that explicitly need to take ownership:**
    - When the closure is consuming variables and does not want to borrow from outer scope.

---

### **5. Key Points About `move` Closures**

- `move` makes the closure **take ownership** of all variables it captures from the outer scope.
- **Captured variables**:
    - By default:
        - A closure will borrow variables from its surrounding environment unless otherwise specified.
    - With `move`:
        - The closure owns all captured variables, preventing dangling references.

---

### **6. Summary**

- **Why is `move` required in `thread::spawn`?**
    - The closure must be `'static` to ensure safety in the detached thread. Using `move` allows the closure to own its captured variables, ensuring their lifetimes outlive the thread.

- **When is `move` not needed?**
    - If the closure only borrows variables and will execute synchronously within the same scope as the owning variables.

- **Key Rule for Rust Closures:**
    - Use `move` when closures need to own their captures, especially in multithreading or async scenarios.

