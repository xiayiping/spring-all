# Schedulers

In a **Reactive project** using Java (e.g., with **Project Reactor**), `Schedulers` are used to control the threading behavior of your reactive streams. Here's a breakdown of the commonly used schedulers you mentioned:

---

### 1. **Schedulers.single()**
- **Description**: Provides a **single-threaded scheduler** that executes tasks sequentially on the same thread. This scheduler is often used for tasks that should not run concurrently, such as managing shared resources or performing operations where concurrency can cause issues.
- **Threading**: A **dedicated single thread** shared across the application.
- **Use Case**:
    - Ideal for tasks that must be executed **serially** and should not overlap.
    - Useful when you need a **shared single-threaded executor** for specific operations.
- **Examples**:
  ```java
  Flux.range(1, 10)
      .subscribeOn(Schedulers.single())
      .doOnNext(i -> System.out.println(Thread.currentThread().getName() + " : " + i))
      .blockLast();
  ```
  Output (all tasks run on the same thread):
  ```
  single-1 : 1
  single-1 : 2
  ...
  single-1 : 10
  ```

---

### 2. **Schedulers.parallel()**
- **Description**: Provides a **thread pool scheduler** designed for **parallel computing**. The number of threads in the pool is equal to the number of available CPU cores.
- **Threading**: A **fixed-size thread pool** with threads equal to the number of CPU cores.
- **Use Case**:
    - Ideal for **CPU-intensive tasks** or tasks that can benefit from parallelization.
    - Use it when you want multiple tasks to execute concurrently on different threads.
- **Examples**:
  ```java
  Flux.range(1, 10)
      .subscribeOn(Schedulers.parallel())
      .doOnNext(i -> System.out.println(Thread.currentThread().getName() + " : " + i))
      .blockLast();
  ```
  Output (tasks distributed across multiple threads):
  ```
  parallel-1 : 1
  parallel-2 : 2
  parallel-3 : 3
  ...
  ```

---

### 3. **Schedulers.immediate()**
- **Description**: Executes tasks **on the current thread** immediately. It does not switch threads at all; instead, it runs everything directly on the thread that subscribes.
- **Threading**: Runs on the **current thread**, no context switching.
- **Use Case**:
    - Best for testing or when you want to avoid any asynchrony/thread switching.
    - Use it when you are debugging and want to keep everything simple and synchronous.
- **Examples**:
  ```java
  Flux.range(1, 10)
      .subscribeOn(Schedulers.immediate())
      .doOnNext(i -> System.out.println(Thread.currentThread().getName() + " : " + i))
      .blockLast();
  ```
  Output (all tasks run on the main thread):
  ```
  main : 1
  main : 2
  ...
  ```

---

### 4. **Schedulers.boundedElastic()**
- **Description**: Provides a scheduler with a **bounded elastic thread pool**. It creates new threads as needed (up to a maximum limit) and reuses idle threads. If the limit is reached, tasks are queued. This scheduler is suitable for I/O-bound or blocking operations.
- **Threading**: Dynamically creates threads (within a limit) and reuses idle ones.
- **Use Case**:
    - Ideal for **I/O-bound** tasks, such as database queries, file I/O, or network calls.
    - Use it when blocking operations are unavoidable, but you want to avoid exhausting resources.
- **Examples**:
  ```java
  Flux.range(1, 10)
      .subscribeOn(Schedulers.boundedElastic())
      .doOnNext(i -> System.out.println(Thread.currentThread().getName() + " : " + i))
      .blockLast();
  ```
  Output (tasks may run on different threads, dynamically created):
  ```
  boundedElastic-1 : 1
  boundedElastic-2 : 2
  ...
  ```
  If threads are reused:
  ```
  boundedElastic-1 : 3
  boundedElastic-2 : 4
  ```

---

### Summary Comparison Table

| Scheduler                       | Threading Model                    | Best For                                | Example Use Case                   |
|---------------------------------|------------------------------------|-----------------------------------------|------------------------------------|
| **Schedulers.single()**         | Single thread (shared)             | Serial tasks where concurrency is bad   | Managing shared resources          |
| **Schedulers.parallel()**       | Fixed thread pool (CPU core count) | CPU-intensive, parallel tasks           | Data processing, computation-heavy |
| **Schedulers.immediate()**      | Current thread                     | Synchronous or testing/debugging        | Simple or debug scenarios          |
| **Schedulers.boundedElastic()** | Dynamic thread pool (bounded)      | I/O-bound or blocking tasks             | Database/file/network operations   |

---

### Key Notes:
- **Choosing the right scheduler** depends on the nature of your task (CPU vs I/O vs sequential).
- Avoid using `Schedulers.parallel()` for I/O-bound tasks; use `Schedulers.boundedElastic()` instead.
- Use **`Schedulers.single()`** for serial tasks or when tasks must execute in a specific order.
- **`Schedulers.immediate()`** is rarely used in production but helpful during debugging.

# Scheduler.single()

Yes, if you have multiple `Flux` instances with `Schedulers.single()`, they will all share the **same single thread** provided by the `Schedulers.single()` scheduler. This is because `Schedulers.single()` creates a **shared singleton thread** across the entire application.

### Explanation:
- **Schedulers.single()** is designed to use a **single-threaded executor** that is shared globally. Every time you call `Schedulers.single()`, it doesn't create a new thread; instead, it reuses the same shared thread.
- This behavior ensures that all tasks submitted to `Schedulers.single()` are executed **sequentially** on the same thread, avoiding concurrency issues.

### Example:

```java
Flux.range(1, 5)
    .subscribeOn(Schedulers.single())
    .doOnNext(i -> System.out.println(Thread.currentThread().getName() + " : Flux1 -> " + i))
    .subscribe();

Flux.range(6, 5)
    .subscribeOn(Schedulers.single())
    .doOnNext(i -> System.out.println(Thread.currentThread().getName() + " : Flux2 -> " + i))
    .subscribe();
```

#### Expected Output:
The output will show that both `Flux` instances are executed on the same thread (`single-1`):

```
single-1 : Flux1 -> 1
single-1 : Flux1 -> 2
single-1 : Flux1 -> 3
single-1 : Flux1 -> 4
single-1 : Flux1 -> 5
single-1 : Flux2 -> 6
single-1 : Flux2 -> 7
single-1 : Flux2 -> 8
single-1 : Flux2 -> 9
single-1 : Flux2 -> 10
```

### Key Points:
1. **Single-threaded Execution**: All tasks submitted to `Schedulers.single()` are executed **one at a time** on the same thread.
2. **Shared Thread**: Multiple `Flux` instances using `Schedulers.single()` will **not create additional threads** but instead share the same thread.
3. **Order of Execution**:
    - If multiple tasks are scheduled on `Schedulers.single()`, they will be executed in the **order they are scheduled** (FIFO).
    - If tasks from one `Flux` are blocking or take a long time, subsequent `Flux` instances will have to wait.

### Use Case:
`Schedulers.single()` is ideal for scenarios where you need **strict sequential execution** and **no concurrency**, such as managing a shared resource or ensuring thread safety.

---

### Caveat:
If you want separate threads for different `Flux` instances (while still limiting each `Flux` to a single thread), you should use `Schedulers.newSingle()` instead of `Schedulers.single()`.

#### Example with `Schedulers.newSingle()`:
```java
Flux.range(1, 5)
    .subscribeOn(Schedulers.newSingle("flux1-thread"))
    .doOnNext(i -> System.out.println(Thread.currentThread().getName() + " : Flux1 -> " + i))
    .subscribe();

Flux.range(6, 5)
    .subscribeOn(Schedulers.newSingle("flux2-thread"))
    .doOnNext(i -> System.out.println(Thread.currentThread().getName() + " : Flux2 -> " + i))
    .subscribe();
```

#### Output:
In this case, each `Flux` gets its **own dedicated thread**:

```
flux1-thread : Flux1 -> 1
flux1-thread : Flux1 -> 2
flux1-thread : Flux1 -> 3
flux1-thread : Flux1 -> 4
flux1-thread : Flux1 -> 5
flux2-thread : Flux2 -> 6
flux2-thread : Flux2 -> 7
flux2-thread : Flux2 -> 8
flux2-thread : Flux2 -> 9
flux2-thread : Flux2 -> 10
```

---

### When to Use:
- **Schedulers.single()**: Use when all tasks can share the same thread and must run sequentially.
- **Schedulers.newSingle()**: Use when each `Flux` (or task group) requires a **dedicated thread**.