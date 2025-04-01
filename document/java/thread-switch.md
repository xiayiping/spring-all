
# Switch Thread

When Java switches threads (a process known as a **context switch**), it involves several operations at the operating system and hardware level. This process is inherently expensive because it requires saving the state of the current running thread and restoring the state of another thread. Here's a detailed breakdown of what happens underneath and why it's performance-intensive.

---

### **What Happens During a Thread Switch?**

1. **Save the Current Thread's State**:
    - The operating system saves the state of the currently running thread, including:
        - **CPU registers**: General-purpose registers, program counter (PC), and stack pointer.
        - **Thread execution context**: This includes the thread's private data, local variables, and stack state.
        - **Instruction pointer**: The address of the next instruction to execute.
        - **CPU cache state**: Any data in the CPU cache associated with the thread (though this is not explicitly saved, it can be invalidated if the thread is moved between cores).

2. **Determine the Next Thread to Run**:
    - The OS scheduler decides which thread to run next based on the scheduling policy (e.g., priority, fairness, or time slicing).
    - This can involve looking up scheduling queues or priority lists, which adds overhead.

3. **Load the New Thread's State**:
    - The OS loads the state of the new thread, including:
        - Restoring the saved CPU registers, program counter, and stack pointer.
        - Setting up the memory context (e.g., mapping the new thread's stack and heap to the CPU's memory management unit).
        - Warming up the CPU cache (if the thread was previously running on a different core, the cache may need to be reloaded).

4. **Switch to the New Thread**:
    - The CPU starts executing the instructions of the new thread at the program counter saved during its last execution.

---

### **Which Part is the Most Performance-Consuming?**

The most performance-consuming parts of a thread context switch are:

1. **CPU Cache Misses**:
    - When the new thread starts running, the **CPU cache** may no longer hold the data it needs (since it was warmed up for the previous thread). This results in **cache misses**, which require fetching data from main memory (which is 100x-1000x slower than accessing the cache).
    - If the new thread runs on a different CPU core, the cost is even higher because the entire local cache may need to be rebuilt.

2. **Memory and Stack Context Switching**:
    - The OS needs to update the memory mappings for the new thread. This includes switching the stack and heap pointers, which can involve changes to the virtual memory page table, potentially causing **TLB (Translation Lookaside Buffer) misses**.
    - Modern CPUs use a TLB to cache memory mappings, but a context switch can flush this cache, leading to performance penalties when new mappings are accessed.

3. **Scheduler Overhead**:
    - The OS scheduler must decide which thread to run next. This involves looking up scheduling queues and applying scheduling algorithms. While this overhead is relatively small compared to other parts of the switch, it can still add up, especially if there are many threads or complex scheduling policies.

4. **System Call Overhead**:
    - A thread context switch often requires a system call (e.g., when a thread is blocked on I/O or waiting for a lock). System calls involve transitioning from **user mode** to **kernel mode**, which incurs a significant overhead due to privilege level changes and memory isolation.

5. **Synchronization Overhead**:
    - If the context switch is caused by mutexes, semaphores, or other synchronization primitives, the process of acquiring or releasing locks can add additional overhead. Contention between threads for shared resources can worsen this.

---

### **Performance Cost of Context Switches**

- The cost of a thread context switch can vary depending on the hardware, operating system, and workload, but typically:
    - A **lightweight context switch** (between threads of the same process) can take **tens of microseconds**.
    - A **heavier context switch** (between threads of different processes) can take **hundreds of microseconds** due to additional costs like switching memory mappings.
    - On modern CPUs, the cost of a context switch is dominated by **cache misses** and **TLB flushes**.

---

### **Why Context Switches are Expensive**

1. **Loss of CPU Cache Warmth**:
    - The CPU cache is optimized for the currently running thread. A context switch invalidates or replaces much of this cache, and rebuilding it for the new thread takes time.

2. **Frequent Mode Switching**:
    - Switching between **user mode** (where application code runs) and **kernel mode** (where the OS scheduler runs) incurs overhead due to privilege changes and memory isolation.

3. **Memory Management Overhead**:
    - Each thread has its own stack, heap, and memory mappings. Switching these contexts involves updates to the TLB and virtual memory system, which can slow down execution.

4. **Scheduler Complexity**:
    - Modern OS schedulers are designed to balance fairness, priority, and efficiency. Complex scheduling algorithms (e.g., for dynamic priority adjustment or load balancing across CPU cores) add computational overhead.

---

### **How to Reduce Context Switch Overhead in Java**

1. **Use Fewer Threads**:
    - Avoid creating more threads than there are available CPU cores. Use thread pools (`ExecutorService`) to manage the number of active threads.

2. **Use Non-blocking I/O**:
    - Avoid blocking threads on I/O operations. Use asynchronous or non-blocking I/O APIs (e.g., `CompletableFuture`, `NIO`, or frameworks like Netty).

3. **Minimize Lock Contention**:
    - Reduce the use of locks or use more efficient synchronization primitives (like `ReentrantLock` or `ReadWriteLock`) to avoid frequent thread blocking and waking.

4. **Leverage Virtual Threads (Project Loom)**:
    - In Java 19+, virtual threads (lightweight threads) drastically reduce the cost of context switching because they rely on fibers (user-mode threads) instead of traditional OS threads. Virtual threads avoid many expensive OS-level context switch operations.

5. **Optimize Thread Scheduling**:
    - If possible, design tasks to run longer on a thread to reduce frequent context switches (e.g., by batching small tasks).

---

### **Conclusion**

The most performance-consuming part of a thread context switch is **CPU cache misses** and **TLB flushes**, as they significantly slow down execution when the thread resumes. Other factors, like scheduler overhead and system call transitions, also contribute but are secondary in most cases. Reducing the number of context switches by using fewer threads, better synchronization techniques, or leveraging modern features like virtual threads can help improve performance in multithreaded applications.

# ReentryLock

`ReentrantLock` and `ReadWriteLock` do not completely avoid thread blocking, but they **reduce unnecessary blocking** and **improve performance** in multithreaded applications by offering more control and flexibility compared to traditional synchronization (e.g., `synchronized` blocks). Let’s break this down to understand how they minimize thread blocking:

---

### **1. ReentrantLock**
`ReentrantLock` is a lock implementation in the `java.util.concurrent.locks` package. It provides more advanced locking mechanisms compared to `synchronized`.

#### **How ReentrantLock Reduces Blocking**

1. **Fairness Option**:
    - `ReentrantLock` can be constructed with a **fairness policy** (`new ReentrantLock(true)`), meaning threads acquire the lock in the order they requested it (FIFO).
    - This prevents thread starvation, a problem with `synchronized`, where higher-priority threads or threads that frequently request locks may block others indefinitely.

   ```java
   ReentrantLock lock = new ReentrantLock(true); // Fair lock
   ```

2. **Non-blocking Try Lock (`tryLock`)**:
    - Unlike `synchronized`, which always blocks if the lock is unavailable, `ReentrantLock` provides the `tryLock()` method.
    - `tryLock()` allows a thread to attempt acquiring the lock without waiting indefinitely. If the lock is unavailable, the thread can perform other tasks or retry later.
    - This reduces unnecessary blocking and improves responsiveness in concurrent applications.

   ```java
   if (lock.tryLock()) {
       try {
           // Critical section
       } finally {
           lock.unlock();
       }
   } else {
       // Do something else if the lock is not available
   }
   ```

3. **Interruptible Lock Acquisition (`lockInterruptibly`)**:
    - With `lockInterruptibly()`, a thread can attempt to acquire the lock but can exit if it is interrupted. This reduces blocking when a thread is waiting for a lock that may take a long time to become available.
    - In contrast, `synchronized` does not allow interruption while waiting for a lock.

   ```java
   lock.lockInterruptibly();
   ```

4. **Reentrancy**:
    - A thread that already holds a `ReentrantLock` can reacquire it without blocking. This is useful for recursive methods or situations where a thread needs to enter the same critical section multiple times.

   ```java
   lock.lock();
   try {
       lock.lock(); // Reentrant acquisition
       // Critical section
   } finally {
       lock.unlock();
       lock.unlock();
   }
   ```

---

### **2. ReadWriteLock**
`ReadWriteLock` is a more sophisticated lock that minimizes blocking by **separating read and write operations**. It provides two types of locks:
- A **read lock** for shared access (multiple threads can acquire it simultaneously if no thread holds the write lock).
- A **write lock** for exclusive access (only one thread can acquire it, blocking all readers and other writers).

#### **How ReadWriteLock Reduces Blocking**

1. **Concurrent Reads**:
    - Multiple threads can acquire the read lock simultaneously as long as no thread holds the write lock. This allows concurrent reads, which are safe because they don't modify shared data.
    - In contrast, `synchronized` blocks all threads, even for read-only operations.

   ```java
   ReadWriteLock rwLock = new ReentrantReadWriteLock();
   rwLock.readLock().lock();
   try {
       // Perform read-only operation
   } finally {
       rwLock.readLock().unlock();
   }
   ```

2. **Exclusive Writes**:
    - The write lock ensures exclusive access to a shared resource. While a thread holds the write lock, all other threads (readers and writers) are blocked.
    - This prevents race conditions when modifying shared data.

   ```java
   rwLock.writeLock().lock();
   try {
       // Perform write operation
   } finally {
       rwLock.writeLock().unlock();
   }
   ```

3. **Avoiding Write-Starvation**:
    - The implementation of `ReentrantReadWriteLock` allows fair queueing of threads requesting the write lock. Writers are not starved by a continuous stream of readers.

4. **Optimized for Read-Heavy Workloads**:
    - `ReadWriteLock` is especially beneficial in scenarios with many readers and few writers. Readers can proceed without blocking each other, improving overall throughput.

---

### **Comparison of Blocking Reduction**

| Feature                            | `ReentrantLock`                                | `ReadWriteLock`                           |
|------------------------------------|-----------------------------------------------|------------------------------------------|
| **Fairness**                       | Can be configured to avoid thread starvation. | Ensures fair access for readers/writers. |
| **Concurrent Reads**               | Not supported; only one thread can hold lock. | Multiple threads can acquire read lock. |
| **Try Lock**                       | `tryLock()` allows non-blocking lock attempts.| Supported for both read and write locks. |
| **Interruptible Lock Acquisition** | `lockInterruptibly()` reduces unnecessary waits.| Supported for both read and write locks. |
| **Best Use Case**                  | General-purpose locking with advanced control.| Read-heavy workloads with rare writes. |

---

### **Examples**

#### **ReentrantLock Example**
```java
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockExample {
    private static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        Runnable task = () -> {
            if (lock.tryLock()) { // Non-blocking lock acquisition
                try {
                    System.out.println(Thread.currentThread().getName() + " acquired the lock.");
                } finally {
                    lock.unlock();
                }
            } else {
                System.out.println(Thread.currentThread().getName() + " could not acquire the lock.");
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();
    }
}
```

#### **ReadWriteLock Example**
```java
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockExample {
    private static final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private static int sharedData = 0;

    public static void main(String[] args) {
        Runnable readTask = () -> {
            rwLock.readLock().lock(); // Acquire read lock
            try {
                System.out.println(Thread.currentThread().getName() + " reading: " + sharedData);
            } finally {
                rwLock.readLock().unlock();
            }
        };

        Runnable writeTask = () -> {
            rwLock.writeLock().lock(); // Acquire write lock
            try {
                sharedData++;
                System.out.println(Thread.currentThread().getName() + " writing: " + sharedData);
            } finally {
                rwLock.writeLock().unlock();
            }
        };

        Thread t1 = new Thread(readTask);
        Thread t2 = new Thread(writeTask);
        Thread t3 = new Thread(readTask);

        t1.start();
        t2.start();
        t3.start();
    }
}
```

---

### **Conclusion**

- **`ReentrantLock`** reduces blocking by offering features like `tryLock`, interruptible locking, and fairness policies, which give threads more control over lock acquisition.
- **`ReadWriteLock`** minimizes blocking in read-heavy workloads by allowing multiple readers to proceed concurrently while ensuring exclusive access for writers.
- These locks are more flexible and efficient than `synchronized` for many advanced concurrency scenarios, reducing unnecessary thread blocking and improving application performance.

# AQS

The **AbstractQueuedSynchronizer (AQS)** is a foundational framework in Java's `java.util.concurrent.locks` package that is used to build many high-level synchronization constructs, such as `ReentrantLock`, `Semaphore`, `CountDownLatch`, `ReadWriteLock`, etc. AQS provides mechanisms to manage thread synchronization efficiently and **avoids unnecessary thread blocking** in several ways.

---

### **How AQS Avoids Thread Blocking**

AQS uses a combination of **non-blocking operations**, **efficient queuing**, and **state-based synchronization** to minimize thread blocking. Here's how it works:

---

### **1. Use of CAS (Compare-And-Swap) for Non-blocking State Updates**

- AQS relies heavily on **atomic operations**, specifically **CAS (Compare-And-Swap)**, to manage the synchronization state without blocking threads.
    - For example, a thread trying to acquire a lock will attempt to update the state (e.g., from `0` to `1`) using CAS.
    - If the CAS operation succeeds, the thread acquires the lock without getting blocked.
    - If the CAS operation fails (because another thread has already acquired the lock), the thread moves to a queue to wait.

**CAS reduces blocking by avoiding expensive thread suspension when contention is low.**

```java
// Pseudo-code for CAS-based state update
if (state == 0) {
    if (compareAndSetState(0, 1)) {
        // Lock acquired, no blocking needed
    } else {
        // Lock not acquired, thread will wait
    }
}
```

---

### **2. FIFO Wait Queues for Thread Coordination**

- When a thread cannot acquire a lock or synchronization resource, AQS places the thread in a **FIFO (First-In-First-Out)** queue.
- This queue is implemented as a doubly linked list and ensures that threads are resumed in the order they requested access.
- Threads in the queue are parked (using `LockSupport.park()`) to prevent them from actively spinning and wasting CPU resources.

**Threads in the queue remain in a "waiting" state and do not consume CPU cycles, avoiding busy-waiting.**

---

### **3. Avoiding Blocking with Spinning and Fast-Path Acquisitions**

- AQS provides a mechanism for threads to perform **spinning** before blocking, especially in cases where contention is low or the lock will be available soon.
- For example, when a thread tries to acquire a lock, it may first attempt to repeatedly check the state using CAS (this is called a **fast-path acquisition**).
    - If the lock becomes available during this spinning phase, the thread can acquire it without ever blocking.
    - If the thread cannot acquire the lock after spinning for a short time, it will then park itself in the wait queue.

**Spinning avoids blocking for short-term contention, improving performance when locks are held for very short durations.**

---

### **4. Parking Threads Instead of Busy-Waiting**

- If a thread fails to acquire the synchronization resource after trying to update the state with CAS, it does not keep spinning indefinitely (which would waste CPU cycles).
- Instead, the thread is **parked** using `LockSupport.park()`, which suspends the thread until it is explicitly unparked.
    - The thread will remain parked and consume no CPU time until it is signaled by another thread (via `LockSupport.unpark()`).

**Parking threads significantly reduces resource consumption compared to busy-waiting.**

---

### **5. Efficient Wake-up Mechanisms**

- When a thread releases a lock or synchronization resource, AQS efficiently wakes up the next thread in the queue using **LockSupport.unpark()**.
- Instead of waking up all waiting threads (as some older synchronization mechanisms might do), AQS only signals the next thread that is eligible to acquire the resource.
    - This avoids the "thundering herd problem," where multiple threads wake up simultaneously and compete for the resource.

**Efficient wake-up avoids unnecessary thread contention and improves performance.**

---

### **6. Shared vs. Exclusive Modes**

AQS supports two modes of operation:
- **Exclusive Mode**: Only one thread can acquire the resource at a time (e.g., `ReentrantLock`).
- **Shared Mode**: Multiple threads can acquire the resource simultaneously (e.g., `Semaphore`, `ReadWriteLock`).

- In **shared mode**, AQS minimizes blocking by allowing multiple threads to proceed concurrently if the resource permits it (e.g., multiple readers in a `ReadWriteLock`).
- Threads are only blocked if the current state does not allow them to proceed.

---

### **7. Fair vs. Non-Fair Acquisition**

- AQS allows developers to implement **fair** or **non-fair** locks:
    - **Fair locks** ensure threads acquire the lock in the order they requested it (FIFO queue).
    - **Non-fair locks** allow threads to "jump the queue" by immediately trying to acquire the lock via CAS, even if other threads are waiting in the queue.

- Non-fair locks avoid thread blocking in low-contention scenarios because a thread can acquire the lock before entering the wait queue.

---

### **8. Interruptible and Timed Locking**

- AQS supports interruptible and timed synchronization operations, reducing unnecessary blocking:
    - **Interruptible Locking**: A thread can be interrupted while waiting for a resource, allowing it to stop blocking and perform other tasks.
    - **Timed Locking**: A thread can attempt to acquire a lock for a specific time and give up if it cannot acquire the lock within the timeout.

**These features reduce the chances of threads being blocked indefinitely.**

---

### **Summary of Key Mechanisms**

| Mechanism                        | How It Avoids Blocking                                                                 |
|----------------------------------|---------------------------------------------------------------------------------------|
| **CAS (Compare-And-Swap)**       | Avoids blocking by atomically updating state without suspending threads.              |
| **Spinning**                     | Reduces blocking by allowing threads to retry acquiring the resource briefly.         |
| **Parking Threads**              | Suspends threads efficiently using `LockSupport.park()` instead of busy-waiting.      |
| **Wake-up Mechanisms**           | Only wakes the next eligible thread, avoiding unnecessary contention.                 |
| **Shared Mode**                  | Allows multiple threads to proceed concurrently, avoiding blocking in read-heavy scenarios. |
| **Interruptible and Timed Locking** | Reduces indefinite blocking by allowing threads to be interrupted or time out.        |

---

### **Example: AQS in Action**

Here’s a simplified example of how AQS works in a custom lock:

```java
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class SimpleLock {
    private final Sync sync = new Sync();

    private static class Sync extends AbstractQueuedSynchronizer {
        @Override
        protected boolean tryAcquire(int arg) {
            // Attempt to acquire the lock using CAS
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            // Release the lock
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }
    }

    public void lock() {
        sync.acquire(1); // Acquire lock
    }

    public void unlock() {
        sync.release(1); // Release lock
    }
}
```

In this example:
- Threads attempt to acquire the lock using `tryAcquire()`, which uses CAS.
- If CAS fails, the thread is added to the queue and parked, avoiding busy-waiting.

---

### **Conclusion**

AQS avoids thread blocking by leveraging **non-blocking CAS operations**, **efficient queuing**, and **thread parking**. It minimizes resource consumption during contention, uses spinning and fast-path acquisitions for short-term waits, and ensures fair and efficient wake-ups. These mechanisms make AQS a highly efficient foundation for building modern synchronization tools in Java.