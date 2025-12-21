# TaskGroup

`asyncio.TaskGroup` is a feature introduced in Python 3.11 to manage groups of asyncio tasks more easily. It provides a structured way to handle multiple asynchronous tasks, ensuring they are all completed before moving on.

### Key Features

- **Structured Concurrency**: Ensures that all tasks in the group are awaited and any exceptions are properly managed.
- **Context Management**: Use it with `async with` to automatically handle the lifecycle of the tasks.
- **Error Handling**: If one task raises an exception, the other tasks can be cancelled, and the exception can be managed.

### Basic Usage

```python
import asyncio

async def worker(name, delay):
    await asyncio.sleep(delay)
    print(f"Worker {name} finished")

async def main():
    async with asyncio.TaskGroup() as tg:
        tg.create_task(worker("A", 2))
        tg.create_task(worker("B", 1))
        tg.create_task(worker("C", 3))

asyncio.run(main())
```

### Explanation

- **Creating Tasks**: Use `tg.create_task()` to add tasks to the group.
- **Automatic Cleanup**: When the `async with` block exits, it ensures all tasks are completed, handling any exceptions that occur.

### Benefits

- Simplifies managing multiple tasks.
- Reduces boilerplate code compared to manually managing asyncio tasks.
- Provides better error propagation and handling.

### Considerations

- Requires Python 3.11 or later.
- Useful for scenarios where you need to manage a set of tasks with a clear start and finish.

By using `asyncio.TaskGroup`, you can write cleaner and more reliable asynchronous code.

# Multi Event Loop

**Yes, you can!** Each OS thread can have its own asyncio event loop. Here's how:

## 1. **Basic Pattern - Multiple Threads with Event Loops**

```python
import asyncio
import threading
from concurrent.futures import ThreadPoolExecutor

def run_in_thread(name):
    """Each thread gets its own event loop"""
    # Create new event loop for this thread
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    
    async def async_task():
        for i in range(5):
            print(f"Thread {name}: Task {i}")
            await asyncio.sleep(1)
        return f"{name} completed"
    
    # Run the event loop
    result = loop.run_until_complete(async_task())
    loop.close()
    return result

# Create multiple threads, each with its own event loop
threads = []
for i in range(3):
    thread = threading.Thread(target=run_in_thread, args=(f"Thread-{i}",))
    threads.append(thread)
    thread.start()

for thread in threads:
    thread.join()
```

## 2. **Using ThreadPoolExecutor**

```python
import asyncio
import threading
from concurrent.futures import ThreadPoolExecutor

async def async_work(thread_id, task_id):
    """Async work in thread-specific event loop"""
    print(f"Thread {thread_id}, Task {task_id} starting on {threading.current_thread().name}")
    await asyncio.sleep(1)
    return f"Thread {thread_id}, Task {task_id} done"

def run_event_loop_in_thread(thread_id):
    """Each thread runs its own event loop"""
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    
    # Run multiple async tasks in this thread's loop
    tasks = [async_work(thread_id, i) for i in range(3)]
    results = loop.run_until_complete(asyncio.gather(*tasks))
    
    loop.close()
    return results

# Run multiple event loops in different threads
with ThreadPoolExecutor(max_workers=4) as executor:
    futures = [executor.submit(run_event_loop_in_thread, i) for i in range(4)]
    
    for future in futures:
        print(future.result())
```

## 3. **Long-Running Event Loops in Threads**

```python
import asyncio
import threading
import time

class EventLoopThread:
    def __init__(self, name):
        self.name = name
        self.loop = None
        self.thread = None
        
    def start(self):
        """Start thread with dedicated event loop"""
        self.thread = threading.Thread(target=self._run_loop, daemon=True)
        self.thread.start()
        
        # Wait for loop to be set up
        while self.loop is None:
            time.sleep(0.01)
    
    def _run_loop(self):
        """Run event loop in this thread"""
        self.loop = asyncio.new_event_loop()
        asyncio.set_event_loop(self.loop)
        print(f"{self.name} event loop started on {threading.current_thread().name}")
        self.loop.run_forever()
    
    def run_coroutine(self, coro):
        """Schedule coroutine on this thread's event loop"""
        return asyncio.run_coroutine_threadsafe(coro, self.loop)
    
    def stop(self):
        """Stop the event loop and thread"""
        self.loop.call_soon_threadsafe(self.loop.stop)
        self.thread.join()

# Usage
async def my_task(name, delay):
    print(f"Task {name} starting")
    await asyncio.sleep(delay)
    print(f"Task {name} completed")
    return f"Result from {name}"

# Create multiple event loop threads
loop_thread_1 = EventLoopThread("Loop-1")
loop_thread_2 = EventLoopThread("Loop-2")

loop_thread_1.start()
loop_thread_2.start()

# Submit tasks to different event loops
future1 = loop_thread_1.run_coroutine(my_task("Task-A", 2))
future2 = loop_thread_2.run_coroutine(my_task("Task-B", 1))

# Get results
print(future1.result())
print(future2.result())

# Cleanup
loop_thread_1.stop()
loop_thread_2.stop()
```

## 4. **Using asyncio.run() in Each Thread** (Python 3.7+)

```python
import asyncio
import threading

def thread_worker(thread_id):
    """Each thread runs asyncio.run() with its own loop"""
    
    async def main():
        tasks = []
        for i in range(3):
            tasks.append(async_task(thread_id, i))
        results = await asyncio.gather(*tasks)
        return results
    
    async def async_task(tid, task_id):
        print(f"Thread {tid}, Task {task_id} on {threading.current_thread().name}")
        await asyncio.sleep(1)
        return f"T{tid}-Task{task_id}"
    
    # asyncio.run creates and manages event loop automatically
    results = asyncio.run(main())
    print(f"Thread {thread_id} results: {results}")

# Create threads
threads = [threading.Thread(target=thread_worker, args=(i,)) for i in range(4)]

for t in threads:
    t.start()

for t in threads:
    t.join()
```

## 5. **Worker Pool Pattern**

```python
import asyncio
import threading
import queue

class AsyncWorkerPool:
    def __init__(self, num_workers):
        self.num_workers = num_workers
        self.workers = []
        self.task_queue = queue.Queue()
        
    def start(self):
        for i in range(self.num_workers):
            worker = AsyncWorker(f"Worker-{i}", self.task_queue)
            worker.start()
            self.workers.append(worker)
    
    def submit(self, coro):
        """Submit coroutine to worker pool"""
        future = asyncio.Future()
        self.task_queue.put((coro, future))
        return future
    
    def shutdown(self):
        for worker in self.workers:
            worker.stop()

class AsyncWorker:
    def __init__(self, name, task_queue):
        self.name = name
        self.task_queue = task_queue
        self.loop = None
        self.thread = None
        self.running = True
        
    def start(self):
        self.thread = threading.Thread(target=self._run, daemon=True)
        self.thread.start()
        
    def _run(self):
        self.loop = asyncio.new_event_loop()
        asyncio.set_event_loop(self.loop)
        
        while self.running:
            try:
                coro, future = self.task_queue.get(timeout=0.1)
                result = self.loop.run_until_complete(coro)
                future.set_result(result)
            except queue.Empty:
                continue
            except Exception as e:
                if not future.done():
                    future.set_exception(e)
        
        self.loop.close()
    
    def stop(self):
        self.running = False
        self.thread.join()

# Usage
async def fetch_data(url):
    await asyncio.sleep(1)  # Simulate I/O
    return f"Data from {url}"

pool = AsyncWorkerPool(num_workers=3)
pool.start()

# Submit tasks
futures = []
for i in range(10):
    future = pool.submit(fetch_data(f"http://api.example.com/{i}"))
    futures.append(future)

# Get results (futures are regular threading.Future, not asyncio.Future)
for i, future in enumerate(futures):
    print(f"Task {i}: {future.result()}")

pool.shutdown()
```

## 6. **Communication Between Event Loops**

```python
import asyncio
import threading

class EventLoopBridge:
    def __init__(self):
        self.loop1 = None
        self.loop2 = None
        
    def start_loops(self):
        # Start loop 1
        thread1 = threading.Thread(target=self._run_loop1, daemon=True)
        thread1.start()
        
        # Start loop 2
        thread2 = threading.Thread(target=self._run_loop2, daemon=True)
        thread2.start()
        
        # Wait for loops to initialize
        import time
        time.sleep(0.1)
    
    def _run_loop1(self):
        self.loop1 = asyncio.new_event_loop()
        asyncio.set_event_loop(self.loop1)
        self.loop1.run_forever()
    
    def _run_loop2(self):
        self.loop2 = asyncio.new_event_loop()
        asyncio.set_event_loop(self.loop2)
        self.loop2.run_forever()
    
    def send_to_loop2(self, coro):
        """Run coroutine in loop2 from loop1"""
        return asyncio.run_coroutine_threadsafe(coro, self.loop2)

# Usage
async def task_in_loop2(data):
    print(f"Processing in loop2: {data}")
    await asyncio.sleep(1)
    return f"Processed: {data}"

bridge = EventLoopBridge()
bridge.start_loops()

# Schedule task from main thread to loop2
future = bridge.send_to_loop2(task_in_loop2("Hello"))
print(future.result())
```

## 7. **Best Practice: Async Queue for Inter-Thread Communication**

```python
import asyncio
import threading
import queue

class MultiLoopProcessor:
    def __init__(self):
        self.input_queue = queue.Queue()
        self.output_queue = queue.Queue()
        self.workers = []
        
    def start_workers(self, num_workers):
        for i in range(num_workers):
            thread = threading.Thread(target=self._worker_loop, args=(i,), daemon=True)
            thread.start()
            self.workers.append(thread)
    
    def _worker_loop(self, worker_id):
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        
        async def process_tasks():
            while True:
                try:
                    task = self.input_queue.get(timeout=0.1)
                    if task is None:  # Shutdown signal
                        break
                    
                    result = await self._process(task, worker_id)
                    self.output_queue.put(result)
                    
                except queue.Empty:
                    await asyncio.sleep(0.01)
        
        loop.run_until_complete(process_tasks())
        loop.close()
    
    async def _process(self, task, worker_id):
        print(f"Worker {worker_id} processing: {task}")
        await asyncio.sleep(1)  # Simulate async work
        return f"Worker {worker_id} completed: {task}"
    
    def submit(self, task):
        self.input_queue.put(task)
    
    def get_result(self, timeout=None):
        return self.output_queue.get(timeout=timeout)

# Usage
processor = MultiLoopProcessor()
processor.start_workers(3)

# Submit tasks
for i in range(10):
    processor.submit(f"Task-{i}")

# Get results
for i in range(10):
    result = processor.get_result()
    print(result)
```

## Important Points:

✅ **Each thread needs `asyncio.new_event_loop()`**  
✅ **Must call `asyncio.set_event_loop(loop)` in each thread**  
✅ **Use `asyncio.run_coroutine_threadsafe()` to submit tasks from other threads**  
✅ **Python GIL still applies** - good for I/O-bound, not CPU-bound work  
✅ **Each loop is independent** - no shared state by default

## When to Use:

- **I/O-bound work** with different event loops per service
- **Isolating async contexts** (e.g., different API clients)
- **Long-running background tasks** in separate loops
- **Avoiding blocking** the main event loop

## Performance Consideration:

For CPU-bound work, use `multiprocessing` instead. For I/O-bound work, multiple event loops in threads can improve concurrency beyond a single event loop's capabilities.