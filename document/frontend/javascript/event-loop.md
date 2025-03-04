# Event Loop

The **JavaScript Event Loop** is a fundamental concept that explains how JavaScript handles asynchronous operations and executes code, particularly in a single-threaded environment. It’s what allows JavaScript to perform non-blocking I/O operations even though it runs on a single thread.

---

### **How the Event Loop Works**

1. **JavaScript is Single-Threaded**:
    - JavaScript executes code in a single thread, meaning it can only do one task at a time in the **Call Stack**.
    - However, asynchronous operations (e.g., timers, network requests, DOM events) are offloaded, and their callbacks are queued for execution once the main thread is free.

2. **Call Stack**:
    - The **Call Stack** is a data structure where JavaScript keeps track of the function calls to be executed.
    - Functions are pushed onto the stack when called and popped off when they complete.

3. **Web APIs**:
    - When an asynchronous operation (e.g., `setTimeout`, `fetch`) is called, it is sent to the **Web APIs** provided by the browser (or Node.js APIs in a server environment).
    - These APIs handle the operation (e.g., waiting for a timer) and, once completed, send the callback to the **Callback Queue**.

4. **Callback Queue**:
    - The **Callback Queue** (or Task Queue) holds the callbacks from asynchronous operations (e.g., event listeners, timers, network requests) that are ready to be executed.

5. **Event Loop**:
    - The **Event Loop** constantly checks if the Call Stack is empty.
    - If the Call Stack is empty and there are tasks in the Callback Queue, the Event Loop pushes the first task from the Callback Queue into the Call Stack for execution.

---

### **Order of Execution**

1. **Synchronous Code**:
    - Executed first and immediately in the Call Stack.

2. **Microtasks**:
    - **Microtasks** (e.g., `Promises`, `queueMicrotask`, `MutationObserver`) have higher priority than tasks in the Callback Queue.
    - After the current Call Stack is cleared, the Event Loop processes all microtasks before moving to tasks in the Callback Queue.

3. **Tasks (Macro Tasks)**:
    - Tasks from the Callback Queue, such as `setTimeout`, `setInterval`, and `I/O callbacks`, are processed after the Call Stack and Microtasks are cleared.

---

### **Visualizing the Event Loop**

Let’s break down the process with an example:

#### Example Code:
```javascript
console.log("Start");

setTimeout(() => {
  console.log("Timeout Callback");
}, 0);

new Promise((res, rej) =>{
    console.log("promise call")
    res(1)
}).then(() => {
  console.log("Promise Callback");
});

console.log("End");
```

#### Execution Steps:
1. **Call Stack**:
    - `"Start"` is logged immediately.
    - `setTimeout` is called, and its callback is sent to the **Callback Queue**.
    - `Promise.res` ('promise call') is called in stack, **immediately**.
    - `Promise.resolve().then()` is called, and its callback is sent to the **Microtask Queue**.
    - `"End"` is logged immediately.

2. **Microtasks**:
    - After the synchronous code finishes, the Event Loop processes the **Promise Callback** from the Microtask Queue.

3. **Callback Queue**:
    - Finally, the Event Loop processes the `setTimeout` callback from the Callback Queue.

#### Output:
```
Start
End
Promise Callback
Timeout Callback
```

---

### **Detailed Breakdown of Queues**

1. **Microtask Queue**:
    - Contains high-priority tasks like `Promise callbacks` and `queueMicrotask`.
    - Processed before the Callback Queue (even if tasks in the Callback Queue are ready).

2. **Callback Queue**:
    - Contains lower-priority tasks like `setTimeout`, `setInterval`, and I/O callbacks.
    - Processed only after the Microtask Queue is empty.

---

### **Event Loop in Action: Another Example**

#### Example Code:
```javascript
console.log("Start");

setTimeout(() => console.log("Timeout 1"), 10);
setTimeout(() => console.log("Timeout 2"), 0);

Promise.resolve().then(() => {
  console.log("Promise 1");
  Promise.resolve().then(() => console.log("Promise 2"));
});

console.log("End");
```

#### Execution Steps:
1. **Synchronous Code**:
    - `"Start"` is logged.
    - `setTimeout` callbacks are sent to the Callback Queue for later execution.
    - `Promise.resolve().then()` adds its callback to the Microtask Queue.
    - `"End"` is logged.

2. **Microtasks**:
    - `Promise 1` is logged.
    - A new `Promise.resolve().then()` is added to the Microtask Queue.
    - `Promise 2` is logged.

3. **Callback Queue**:
    - `setTimeout` with `0ms` delay (`Timeout 2`) is logged.
    - `setTimeout` with `10ms` delay (`Timeout 1`) is logged.

#### Output:
```
Start
End
Promise 1
Promise 2
Timeout 2
Timeout 1
```

---

### **Key Takeaways**

1. **Synchronous Code First**:
    - All synchronous code runs first (Call Stack).

2. **Microtasks Before Callback Queue**:
    - Microtasks like `Promise` callbacks are processed before tasks in the Callback Queue.

3. **Order of `setTimeout`**:
    - The delay in `setTimeout` is the minimum time before the callback is added to the Callback Queue, not the exact execution time.

4. **Single-threaded Execution**:
    - JavaScript executes code in a single thread, but the Event Loop enables asynchronous behavior.

---

### **Common Interview Questions**

1. **What’s the difference between a microtask and a macrotask?**
    - **Microtask**: Higher priority tasks processed after the current stack (e.g., `Promise.then`, `queueMicrotask`).
    - **Macrotask**: Lower priority tasks processed after microtasks (e.g., `setTimeout`, `setInterval`).

2. **Why does `Promise` resolve before `setTimeout`?**
    - Promises are part of the Microtask Queue, which is processed before the Callback Queue (where `setTimeout` resides).

3. **What is the Event Loop?**
    - The Event Loop is a mechanism that processes the Call Stack, Microtask Queue, and Callback Queue to execute JavaScript code asynchronously.

---

### **Animated Visualization Tools**
If you'd like to experiment and visualize the Event Loop in action, you can try:
- **Loupe**: [http://latentflip.com/loupe/](http://latentflip.com/loupe/)  
  It’s an interactive tool that helps you understand the Event Loop step by step.

---

The **Event Loop** is central to JavaScript's asynchronous behavior, enabling it to handle tasks like timers, Promises, and I/O effectively in a single-threaded environment. Understanding its operation is key to mastering JavaScript!