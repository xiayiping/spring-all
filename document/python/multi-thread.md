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