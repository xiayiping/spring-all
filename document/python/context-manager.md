# ContextManager

# `@contextmanager` Explained

`@contextmanager` is a decorator from Python's `contextlib` module that allows you to create **context managers** using generator functions instead of writing a full class.

## What are Context Managers?

Context managers are objects that define `__enter__()` and `__exit__()` methods, used with the `with` statement to ensure setup and cleanup code is executed:

```python
with some_context_manager():
    # Your code here
    pass
# Cleanup happens automatically
```

## Without @contextmanager (Traditional Way)

```python
class DatabaseConnection:
    def __enter__(self):
        print("Opening database connection")
        self.conn = create_connection()
        return self.conn
    
    def __exit__(self, exc_type, exc_val, exc_tb):
        print("Closing database connection")
        self.conn.close()
        return False

# Usage
with DatabaseConnection() as conn:
    conn.query("SELECT * FROM users")
```

## With @contextmanager (Simpler Way)

```python
from contextlib import contextmanager

@contextmanager
def database_connection():
    print("Opening database connection")
    conn = create_connection()
    try:
        yield conn  # This is where your 'with' block executes
    finally:
        print("Closing database connection")
        conn.close()

# Usage (same as above)
with database_connection() as conn:
    conn.query("SELECT * FROM users")
```

## How It Works

```python
@contextmanager
def my_context():
    # 1. SETUP CODE - runs when entering the 'with' block
    print("Before")
    resource = acquire_resource()
    
    try:
        # 2. YIELD - execution passes to your 'with' block
        yield resource
        # 3. Code here runs if 'with' block completes successfully
        print("Success")
    except Exception as e:
        # 4. Code here runs if 'with' block raises an exception
        print(f"Error: {e}")
        raise
    finally:
        # 5. CLEANUP CODE - always runs, even if exception occurs
        print("After")
        release_resource(resource)

# Usage
with my_context() as res:
    # Your code using 'res'
    do_something(res)
```

## Real-World Examples

### 1. File Handling
```python
@contextmanager
def open_file(filename, mode='r'):
    print(f"Opening {filename}")
    f = open(filename, mode)
    try:
        yield f
    finally:
        print(f"Closing {filename}")
        f.close()

with open_file('data.txt') as f:
    content = f.read()
# File automatically closed
```

### 2. Timer
```python
import time

@contextmanager
def timer(name):
    start = time.time()
    yield
    end = time.time()
    print(f"{name} took {end - start:.2f} seconds")

with timer("Database Query"):
    # Slow operation
    time.sleep(2)
# Prints: "Database Query took 2.00 seconds"
```

### 3. Temporary Directory
```python
import tempfile
import shutil

@contextmanager
def temporary_directory():
    temp_dir = tempfile.mkdtemp()
    try:
        yield temp_dir
    finally:
        shutil.rmtree(temp_dir)

with temporary_directory() as tmpdir:
    # Use tmpdir
    with open(f"{tmpdir}/file.txt", 'w') as f:
        f.write("temporary data")
# Directory automatically deleted
```

### 4. Database Transaction
```python
@contextmanager
def transaction(db_connection):
    try:
        yield db_connection
        db_connection.commit()  # Commit if successful
    except Exception:
        db_connection.rollback()  # Rollback on error
        raise

with transaction(db_conn) as conn:
    conn.execute("INSERT INTO users VALUES (...)")
    conn.execute("UPDATE accounts SET ...")
# Auto-commit or rollback
```

### 5. Lock Management
```python
import threading

@contextmanager
def acquire_lock(lock):
    lock.acquire()
    try:
        yield
    finally:
        lock.release()

lock = threading.Lock()
with acquire_lock(lock):
    # Critical section
    shared_resource.modify()
# Lock automatically released
```

## In Your Zipkin Code

```python
@contextmanager
def trace_context(request: Request, span_name: str):
    """Context manager for creating Zipkin spans"""
    trace_info = extract_trace_context(request)
    
    # SETUP: Start Zipkin span
    with zipkin_span(
        service_name=SERVICE_NAME,
        span_name=span_name,
        # ... other params
    ):
        yield  # Your traced code executes here
    # CLEANUP: Span automatically closed and sent to Zipkin

# Usage in middleware
with trace_context(request, span_name):
    # This code is traced
    response = await call_next(request)
# Span automatically finalized and sent
```

## Key Benefits

1. **Cleaner Code**: Less boilerplate than writing a full class
2. **Automatic Cleanup**: `finally` block ensures cleanup always happens
3. **Exception Handling**: Proper cleanup even when exceptions occur
4. **Readability**: Intent is clear - setup, use, cleanup
5. **Reusability**: Easy to create reusable context managers

## Common Pattern

```python
@contextmanager
def my_context():
    # Acquire resources
    resource = setup()
    try:
        yield resource
    finally:
        # Release resources (always executes)
        cleanup(resource)
```

This pattern ensures that resources are properly managed, making your code safer and more maintainable!