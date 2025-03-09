# explicit new thread for task

If you want to **explicitly ensure that your tasks run on different threads**, there are a few approaches you can use. By default, `Task.Run` schedules tasks to the **ThreadPool**, but the runtime doesn't guarantee that each task will run on a separate thread. To enforce this behavior, you need to manage the threads explicitly or configure a custom `TaskScheduler`.

---

### **Approaches to Ensure Tasks Run on Different Threads**

---

#### **1. Use `Task.Run` with Long-Running Option**
You can use the `TaskCreationOptions.LongRunning` option to hint to the runtime that a task should not use a `ThreadPool` thread. Instead, the runtime will create a **dedicated thread** for the task.

```csharp
Task task1 = Task.Factory.StartNew(() =>
{
    Console.WriteLine($"Task 1 running on thread {Thread.CurrentThread.ManagedThreadId}");
}, TaskCreationOptions.LongRunning);

Task task2 = Task.Factory.StartNew(() =>
{
    Console.WriteLine($"Task 2 running on thread {Thread.CurrentThread.ManagedThreadId}");
}, TaskCreationOptions.LongRunning);

Task.WaitAll(task1, task2);
```

**Explanation:**
- `TaskCreationOptions.LongRunning` tells the runtime to create a **dedicated thread** (not a `ThreadPool` thread) for each task.
- This ensures that each task runs on a separate thread.

**Output Example:**
```plaintext
Task 1 running on thread 3
Task 2 running on thread 4
```

---

#### **2. Explicitly Use `Thread`**
If you want **full control over threads**, you can bypass the `Task`-based model and use the `Thread` class directly. Each `Thread` you create will always run on a separate thread.

```csharp
Thread thread1 = new Thread(() =>
{
    Console.WriteLine($"Thread 1 running on thread {Thread.CurrentThread.ManagedThreadId}");
});
Thread thread2 = new Thread(() =>
{
    Console.WriteLine($"Thread 2 running on thread {Thread.CurrentThread.ManagedThreadId}");
});

thread1.Start();
thread2.Start();

thread1.Join();
thread2.Join();
```

**Explanation:**
- By using `Thread`, you explicitly create and manage threads yourself, ensuring that each task runs on a unique thread.

**Output Example:**
```plaintext
Thread 1 running on thread 3
Thread 2 running on thread 4
```

---

#### **3. Use a Custom `TaskScheduler`**
If you want more control over how tasks are scheduled and executed, you can create a **custom `TaskScheduler`**, where you manually allocate each task to a specific thread.

Here’s an example of creating a `TaskScheduler` with dedicated threads:

```csharp
using System;
using System.Collections.Concurrent;
using System.Threading;
using System.Threading.Tasks;

public class DedicatedThreadTaskScheduler : TaskScheduler
{
    private readonly Thread _thread;
    private readonly BlockingCollection<Task> _tasks = new BlockingCollection<Task>();

    public DedicatedThreadTaskScheduler()
    {
        _thread = new Thread(() =>
        {
            foreach (var task in _tasks.GetConsumingEnumerable())
            {
                TryExecuteTask(task);
            }
        });

        _thread.IsBackground = true;
        _thread.Start();
    }

    protected override IEnumerable<Task> GetScheduledTasks()
    {
        return _tasks.ToArray();
    }

    protected override void QueueTask(Task task)
    {
        _tasks.Add(task);
    }

    protected override bool TryExecuteTaskInline(Task task, bool taskWasPreviouslyQueued)
    {
        return false; // Prevent inline execution
    }
}

class Program
{
    static void Main()
    {
        var scheduler1 = new DedicatedThreadTaskScheduler();
        var scheduler2 = new DedicatedThreadTaskScheduler();

        var task1 = Task.Factory.StartNew(() =>
        {
            Console.WriteLine($"Task 1 running on thread {Thread.CurrentThread.ManagedThreadId}");
        }, CancellationToken.None, TaskCreationOptions.None, scheduler1);

        var task2 = Task.Factory.StartNew(() =>
        {
            Console.WriteLine($"Task 2 running on thread {Thread.CurrentThread.ManagedThreadId}");
        }, CancellationToken.None, TaskCreationOptions.None, scheduler2);

        Task.WaitAll(task1, task2);
    }
}
```

**Explanation:**
- Each custom `TaskScheduler` runs its tasks on a dedicated thread.
- This ensures that tasks assigned to different schedulers will always run on separate threads.

**Output Example:**
```plaintext
Task 1 running on thread 3
Task 2 running on thread 4
```

---

#### **4. Use `Parallel.Invoke` with Custom Threads**
Another approach is to use `Parallel.Invoke` with explicitly created threads, though this is less common and may not give you fine-grained control:

```csharp
Parallel.Invoke(
    () => {
        Thread t1 = new Thread(() =>
        {
            Console.WriteLine($"Task 1 running on thread {Thread.CurrentThread.ManagedThreadId}");
        });
        t1.Start();
        t1.Join();
    },
    () => {
        Thread t2 = new Thread(() =>
        {
            Console.WriteLine($"Task 2 running on thread {Thread.CurrentThread.ManagedThreadId}");
        });
        t2.Start();
        t2.Join();
    }
);
```

**Explanation:**
- While `Parallel.Invoke` itself doesn't guarantee separate threads, explicitly using `Thread` within each action ensures the tasks run on different threads.

---

#### **5. Verify Thread IDs**
To confirm that tasks are running on different threads, you can use `Thread.CurrentThread.ManagedThreadId` in your task logic:

```csharp
Task.Run(() =>
{
    Console.WriteLine($"Task 1 running on thread {Thread.CurrentThread.ManagedThreadId}");
});
Task.Run(() =>
{
    Console.WriteLine($"Task 2 running on thread {Thread.CurrentThread.ManagedThreadId}");
});
```

If you see the same thread ID for both tasks, the runtime scheduled them on the same thread. To ensure separation, use one of the methods above.

---

### **6. Summary**

| **Method**                                   | **Ensures Tasks Run on Separate Threads?** | **Use Case**                                                                 |
|---------------------------------------------|--------------------------------------------|-------------------------------------------------------------------------------|
| `Task.Run`                                   | No (uses `ThreadPool`)                     | For general-purpose task-based programming, but threads are not guaranteed.   |
| `Task.Factory.StartNew` with `LongRunning`  | Yes                                       | Ensures each task runs on a dedicated thread outside the `ThreadPool`.        |
| Explicit `Thread`                            | Yes                                       | When you want full control over thread creation and management.               |
| Custom `TaskScheduler`                      | Yes                                       | Advanced scenarios requiring custom task execution logic.                     |
| `Parallel.Invoke` + `Thread`                | Yes                                       | For parallelized workloads with explicit threads.                             |

For most scenarios, **`Task.Factory.StartNew` with `LongRunning`** or direct usage of `Thread` is sufficient if you want tasks to run on separate threads. Use a **custom TaskScheduler** for more advanced requirements.

