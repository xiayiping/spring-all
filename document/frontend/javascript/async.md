# Async


To answer your question, let's break it into parts:

---

### 1. **Where is the code inside an `async` method call executed?**

The code inside an `async` function is executed **synchronously** up until the first `await` keyword or the first `Promise`. This means:

- The synchronous part of the `async` function runs on the **main thread** just like regular code.
- When it hits an `await` (or an unresolved `Promise`), the function pauses, and the rest of the code is scheduled to run asynchronously.

---

### 2. **What happens to the return of an `async` method?**

The **return value** of an `async` function is always a **Promise**. When the `async` function completes, the resolved value of the `Promise` is placed in the **microtasks queue**.

---

### 3. **What happens when `await` is encountered?**

When an `await` keyword is encountered, the execution of the `async` function is paused until the `Promise` is resolved. The following happens:

1. The `async` function's execution is paused.
2. The JavaScript engine continues executing other synchronous tasks on the **main thread**.
3. Once the `Promise` is resolved, the continuation of the `async` function is placed in the **microtasks queue**.

---

### 4. **Macro Tasks vs Micro Tasks**

- **Microtasks Queue**:
    - Includes tasks like:
        - Resolved `Promises`
        - `MutationObserver` callbacks
        - `queueMicrotask`
    - Microtasks are executed **before** any tasks in the macro tasks queue, after the currently executing code completes.

- **Macro Tasks Queue**:
    - Includes tasks like:
        - `setTimeout`
        - `setInterval`
        - `setImmediate` (Node.js)
        - I/O operations
    - Macro tasks are executed **after** all currently queued microtasks are processed.

---

### Summary of Behavior:

1. **Code Inside `async`:**
    - Runs synchronously on the **main thread** until it hits the first `await`.

2. **Return of the `async` Function:**
    - The resolved value is placed in the **microtasks queue**.

3. **Await Continuation:**
    - The continuation of the `async` function after an `await` is placed in the **microtasks queue**.

---

### Example:

```javascript
console.log("Start");

async function asyncFunction() {
  console.log("Inside async function");
  await Promise.resolve(); // Resolving a promise
  console.log("After await");
}

asyncFunction();

console.log("End");
```

**Execution Order:**

1. `"Start"` is logged (synchronous).
2. `"Inside async function"` is logged (synchronous part of the `async` function).
3. The `await Promise.resolve()` pauses the `asyncFunction` and schedules `"After await"` in the **microtasks queue**.
4. `"End"` is logged (synchronous).
5. `"After await"` is logged (from the microtasks queue).

---

### Key Takeaways:

- The **code inside an `async` function** runs synchronously until the first `await`.
- Resolved `Promises` and the continuation of `async` functions are placed in the **microtasks queue**.