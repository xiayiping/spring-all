# app.middleware

# `@app.middleware("http")` Explained

`@app.middleware("http")` is a FastAPI decorator that creates **HTTP middleware** - code that runs for **every request** before it reaches your endpoints and **every response** before it's sent back to the client.

## Basic Concept

```python
@app.middleware("http")
async def my_middleware(request: Request, call_next):
    # Code here runs BEFORE the endpoint
    print("Before request")
    
    response = await call_next(request)  # Call the actual endpoint
    
    # Code here runs AFTER the endpoint
    print("After request")
    
    return response
```

## Visual Flow

```
Client Request
     ↓
[Middleware 1 - Before]
     ↓
[Middleware 2 - Before]
     ↓
[Your Endpoint Handler]
     ↓
[Middleware 2 - After]
     ↓
[Middleware 1 - After]
     ↓
Client Response
```

## Complete Example

```python
from fastapi import FastAPI, Request
import time

app = FastAPI()

@app.middleware("http")
async def add_process_time_header(request: Request, call_next):
    # BEFORE: Request processing
    start_time = time.time()
    
    # Call the actual endpoint
    response = await call_next(request)
    
    # AFTER: Response processing
    process_time = time.time() - start_time
    response.headers["X-Process-Time"] = str(process_time)
    
    return response

@app.get("/")
async def root():
    return {"message": "Hello World"}

# When you visit "/", the middleware adds X-Process-Time header to response
```

## Common Use Cases

### 1. Logging Requests
```python
import logging

logger = logging.getLogger(__name__)

@app.middleware("http")
async def log_requests(request: Request, call_next):
    logger.info(f"Request: {request.method} {request.url.path}")
    
    response = await call_next(request)
    
    logger.info(f"Response: {response.status_code}")
    return response
```

### 2. Authentication/Authorization
```python
@app.middleware("http")
async def check_auth(request: Request, call_next):
    # Skip auth for public endpoints
    if request.url.path in ["/login", "/health"]:
        return await call_next(request)
    
    # Check authentication
    token = request.headers.get("Authorization")
    if not token or not is_valid_token(token):
        return JSONResponse(
            status_code=401,
            content={"error": "Unauthorized"}
        )
    
    # Continue to endpoint
    response = await call_next(request)
    return response
```

### 3. CORS Headers
```python
@app.middleware("http")
async def add_cors_headers(request: Request, call_next):
    response = await call_next(request)
    
    response.headers["Access-Control-Allow-Origin"] = "*"
    response.headers["Access-Control-Allow-Methods"] = "GET, POST, PUT, DELETE"
    response.headers["Access-Control-Allow-Headers"] = "Content-Type, Authorization"
    
    return response
```

### 4. Rate Limiting
```python
from collections import defaultdict
from datetime import datetime, timedelta

# Simple in-memory rate limiter
request_counts = defaultdict(list)

@app.middleware("http")
async def rate_limit(request: Request, call_next):
    client_ip = request.client.host
    now = datetime.now()
    
    # Clean old requests (older than 1 minute)
    request_counts[client_ip] = [
        req_time for req_time in request_counts[client_ip]
        if now - req_time < timedelta(minutes=1)
    ]
    
    # Check rate limit (max 100 requests per minute)
    if len(request_counts[client_ip]) >= 100:
        return JSONResponse(
            status_code=429,
            content={"error": "Too many requests"}
        )
    
    # Record this request
    request_counts[client_ip].append(now)
    
    response = await call_next(request)
    return response
```

### 5. Request ID Tracking
```python
import uuid

@app.middleware("http")
async def add_request_id(request: Request, call_next):
    # Generate or extract request ID
    request_id = request.headers.get("X-Request-ID", str(uuid.uuid4()))
    
    # Add to request state (accessible in endpoints)
    request.state.request_id = request_id
    
    response = await call_next(request)
    
    # Add to response headers
    response.headers["X-Request-ID"] = request_id
    
    return response

# Access in endpoint:
@app.get("/test")
async def test(request: Request):
    return {"request_id": request.state.request_id}
```

### 6. Error Handling
```python
@app.middleware("http")
async def catch_exceptions(request: Request, call_next):
    try:
        response = await call_next(request)
        return response
    except Exception as e:
        logger.error(f"Unhandled exception: {e}", exc_info=True)
        return JSONResponse(
            status_code=500,
            content={"error": "Internal server error", "detail": str(e)}
        )
```

### 7. Request/Response Modification
```python
@app.middleware("http")
async def modify_request_response(request: Request, call_next):
    # Modify request
    # (Note: request body modification is tricky, this is just headers)
    if "X-Custom-Header" not in request.headers:
        # You can't directly modify request, but can store in state
        request.state.custom_value = "default"
    
    response = await call_next(request)
    
    # Modify response
    response.headers["X-Server"] = "FastAPI"
    response.headers["X-Powered-By"] = "Python"
    
    return response
```

### 8. Database Session Management
```python
@app.middleware("http")
async def db_session_middleware(request: Request, call_next):
    # Create database session
    session = SessionLocal()
    request.state.db = session
    
    try:
        response = await call_next(request)
        return response
    finally:
        # Always close session
        session.close()

# Use in endpoint:
@app.get("/users")
async def get_users(request: Request):
    db = request.state.db
    users = db.query(User).all()
    return users
```

## Multiple Middlewares

```python
# They execute in order of definition (top to bottom for request)
# and reverse order for response (bottom to top)

@app.middleware("http")
async def first_middleware(request: Request, call_next):
    print("First - Before")
    response = await call_next(request)
    print("First - After")
    return response

@app.middleware("http")
async def second_middleware(request: Request, call_next):
    print("Second - Before")
    response = await call_next(request)
    print("Second - After")
    return response

@app.get("/")
async def root():
    print("Endpoint")
    return {"message": "Hello"}

# Output when accessing "/":
# First - Before
# Second - Before
# Endpoint
# Second - After
# First - After
```

## Your Zipkin Middleware

```python
@app.middleware("http")
async def zipkin_middleware(request: Request, call_next):
    """Middleware to add Zipkin tracing to all requests"""
    
    # BEFORE: Start tracing span for this request
    span_name = f"{request.method} {request.url.path}"
    
    with trace_context(request, span_name):
        # Record when request started
        start_time = time.time()
        
        # EXECUTE: Call the actual endpoint
        response = await call_next(request)
        
        # AFTER: Record metrics and finish span
        duration = time.time() - start_time
        
        # Span is automatically sent to Zipkin when context exits
    
    return response
```

## Key Points

1. **Runs for ALL requests** - affects every endpoint
2. **Order matters** - first defined = outermost layer
3. **Must call `call_next(request)`** - to continue to the next middleware/endpoint
4. **Can modify request and response** - headers, state, etc.
5. **Can short-circuit** - return early without calling `call_next()`
6. **Async** - use `async def` and `await`

## When to Use

✅ **Good for:**
- Logging
- Authentication
- Rate limiting
- Adding headers
- Timing requests
- Tracing/monitoring
- Error handling
- CORS

❌ **Not ideal for:**
- Endpoint-specific logic (use dependencies instead)
- Complex business logic
- Heavy computation (blocks all requests)

Middleware is powerful for cross-cutting concerns that apply to all or most of your API endpoints!


# more

# Middleware Parameter Types & Ordering

## Can the parameter only be 'http'?

**Yes, currently `"http"` is the only option** for `@app.middleware()` in FastAPI.

```python
@app.middleware("http")  # ✅ Valid - only option
async def my_middleware(request: Request, call_next):
    pass

@app.middleware("websocket")  # ❌ Not supported
@app.middleware("grpc")       # ❌ Not supported
```

### Why only "http"?

- FastAPI's middleware system is built on **Starlette**
- Starlette currently only supports HTTP middleware through this decorator
- For WebSocket middleware, you need different approaches (covered below)

### Alternative: BaseHTTPMiddleware

You can also create middleware using a class-based approach:

```python
from fastapi import FastAPI
from starlette.middleware.base import BaseHTTPMiddleware

class MyMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        # Before
        print("Before request")
        response = await call_next(request)
        # After
        print("After request")
        return response

app = FastAPI()
app.add_middleware(MyMiddleware)
```

## Middleware Ordering

Middlewares execute in **"onion" or "stack" pattern**:

### Execution Order

```python
from fastapi import FastAPI, Request

app = FastAPI()

@app.middleware("http")
async def first_middleware(request: Request, call_next):
    print("1️⃣ First - BEFORE")
    response = await call_next(request)
    print("1️⃣ First - AFTER")
    return response

@app.middleware("http")
async def second_middleware(request: Request, call_next):
    print("2️⃣ Second - BEFORE")
    response = await call_next(request)
    print("2️⃣ Second - AFTER")
    return response

@app.middleware("http")
async def third_middleware(request: Request, call_next):
    print("3️⃣ Third - BEFORE")
    response = await call_next(request)
    print("3️⃣ Third - AFTER")
    return response

@app.get("/test")
async def test_endpoint():
    print("🎯 ENDPOINT")
    return {"message": "Hello"}
```

### Output when calling `/test`:

```
1️⃣ First - BEFORE
2️⃣ Second - BEFORE
3️⃣ Third - BEFORE
🎯 ENDPOINT
3️⃣ Third - AFTER
2️⃣ Second - AFTER
1️⃣ First - AFTER
```

### Visual Diagram

```
REQUEST →
    ┌─────────────────────────────────┐
    │ First Middleware (Before)       │ ← Defined first = Outermost
    │  ┌───────────────────────────┐  │
    │  │ Second Middleware (Before)│  │
    │  │  ┌─────────────────────┐  │  │
    │  │  │ Third Middleware    │  │  │
    │  │  │    (Before)         │  │  │
    │  │  │  ┌───────────────┐  │  │  │
    │  │  │  │   ENDPOINT    │  │  │  │
    │  │  │  └───────────────┘  │  │  │
    │  │  │    (After)          │  │  │
    │  │  └─────────────────────┘  │  │
    │  │      (After)               │  │
    │  └───────────────────────────┘  │
    │         (After)                  │
    └─────────────────────────────────┘
← RESPONSE
```

## Complete Example with Ordering

```python
from fastapi import FastAPI, Request, Response
import time
from typing import Callable

app = FastAPI()

# Middleware 1: Request Logging (outermost)
@app.middleware("http")
async def log_middleware(request: Request, call_next: Callable):
    print(f"📝 [LOG] Request: {request.method} {request.url.path}")
    
    response = await call_next(request)
    
    print(f"📝 [LOG] Response: {response.status_code}")
    return response

# Middleware 2: Timing
@app.middleware("http")
async def timing_middleware(request: Request, call_next: Callable):
    start = time.time()
    print(f"⏱️  [TIMING] Started")
    
    response = await call_next(request)
    
    duration = time.time() - start
    print(f"⏱️  [TIMING] Completed in {duration:.3f}s")
    response.headers["X-Process-Time"] = str(duration)
    return response

# Middleware 3: Authentication
@app.middleware("http")
async def auth_middleware(request: Request, call_next: Callable):
    print(f"🔐 [AUTH] Checking authentication")
    
    # Skip auth for public paths
    if request.url.path in ["/public", "/health"]:
        print(f"🔐 [AUTH] Public endpoint, skipping auth")
        return await call_next(request)
    
    # Check token
    token = request.headers.get("Authorization")
    if not token:
        print(f"🔐 [AUTH] No token, rejecting")
        return Response(content="Unauthorized", status_code=401)
    
    print(f"🔐 [AUTH] Token valid, proceeding")
    response = await call_next(request)
    print(f"🔐 [AUTH] Request completed")
    return response

# Middleware 4: Add custom headers (innermost)
@app.middleware("http")
async def header_middleware(request: Request, call_next: Callable):
    print(f"📋 [HEADERS] Adding custom headers")
    
    response = await call_next(request)
    
    response.headers["X-Custom-Header"] = "MyValue"
    response.headers["X-Powered-By"] = "FastAPI"
    print(f"📋 [HEADERS] Headers added")
    return response

@app.get("/protected")
async def protected_endpoint():
    print("🎯 [ENDPOINT] Protected endpoint executing")
    return {"message": "Protected data"}

@app.get("/public")
async def public_endpoint():
    print("🎯 [ENDPOINT] Public endpoint executing")
    return {"message": "Public data"}
```

### Output for `/protected` with token:

```
📝 [LOG] Request: GET /protected
⏱️  [TIMING] Started
🔐 [AUTH] Checking authentication
🔐 [AUTH] Token valid, proceeding
📋 [HEADERS] Adding custom headers
🎯 [ENDPOINT] Protected endpoint executing
📋 [HEADERS] Headers added
🔐 [AUTH] Request completed
⏱️  [TIMING] Completed in 0.002s
📝 [LOG] Response: 200
```

### Output for `/protected` WITHOUT token:

```
📝 [LOG] Request: GET /protected
⏱️  [TIMING] Started
🔐 [AUTH] Checking authentication
🔐 [AUTH] No token, rejecting
⏱️  [TIMING] Completed in 0.001s
📝 [LOG] Response: 401
```

Note: Middlewares after auth are **never called** when auth rejects!

## Using `add_middleware()` Method

You can also add middleware using the `add_middleware()` method. **Order is reversed here**:

```python
from fastapi import FastAPI
from starlette.middleware.base import BaseHTTPMiddleware

app = FastAPI()

class FirstMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request, call_next):
        print("First - Before")
        response = await call_next(request)
        print("First - After")
        return response

class SecondMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request, call_next):
        print("Second - Before")
        response = await call_next(request)
        print("Second - After")
        return response

# ⚠️ IMPORTANT: Last added = First executed!
app.add_middleware(SecondMiddleware)  # Added second, executes first
app.add_middleware(FirstMiddleware)   # Added first, executes second

@app.get("/")
async def root():
    print("Endpoint")
    return {"msg": "Hello"}

# Output:
# Second - Before
# First - Before
# Endpoint
# First - After
# Second - After
```

## Built-in Middleware Order

```python
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware
from fastapi.middleware.trustedhost import TrustedHostMiddleware

app = FastAPI()

# Order matters!
app.add_middleware(GZipMiddleware, minimum_size=1000)        # Compress last
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
)
app.add_middleware(TrustedHostMiddleware, allowed_hosts=["example.com"])  # Check first

# Execution order:
# 1. TrustedHostMiddleware (security first)
# 2. CORSMiddleware
# 3. GZipMiddleware (compress response last)
```

## Best Practices for Ordering

```python
# Recommended order (outermost to innermost):

# 1. Logging/Monitoring (first to log everything)
@app.middleware("http")
async def logging_middleware(request, call_next): ...

# 2. Error Handling (catch all errors)
@app.middleware("http")
async def error_handling_middleware(request, call_next): ...

# 3. Security (CORS, Trusted Hosts, etc.)
@app.middleware("http")
async def security_middleware(request, call_next): ...

# 4. Authentication/Authorization
@app.middleware("http")
async def auth_middleware(request, call_next): ...

# 5. Rate Limiting
@app.middleware("http")
async def rate_limit_middleware(request, call_next): ...

# 6. Request Processing (timing, tracing, etc.)
@app.middleware("http")
async def processing_middleware(request, call_next): ...

# 7. Response Modification (add headers, compress, etc.)
@app.middleware("http")
async def response_middleware(request, call_next): ...
```

## Summary

| Aspect | Details |
|--------|---------|
| **Parameter** | Only `"http"` is supported |
| **Definition Order** | First defined = Outermost layer (executes first for requests) |
| **Execution** | LIFO for request, FIFO for response (onion pattern) |
| **`add_middleware()`** | Reverse order (last added = first executed) |
| **Short-circuit** | Return early to skip remaining middlewares |

The key is to think of middlewares as **layers of an onion** - the first one you define wraps around all the others!


# Controlling Middleware Order & Placement

## How to Control Execution Order

### Method 1: Using `@app.middleware("http")` - Order by Definition

The order is controlled by **where you define them in your code** (top to bottom):

```python
from fastapi import FastAPI, Request

app = FastAPI()

# ✅ FIRST executed middleware (outermost)
@app.middleware("http")
async def first(request: Request, call_next):
    print("1st - Before")
    response = await call_next(request)
    print("1st - After")
    return response

# ✅ SECOND executed middleware
@app.middleware("http")
async def second(request: Request, call_next):
    print("2nd - Before")
    response = await call_next(request)
    print("2nd - After")
    return response

# ✅ THIRD executed middleware (innermost)
@app.middleware("http")
async def third(request: Request, call_next):
    print("3rd - Before")
    response = await call_next(request)
    print("3rd - After")
    return response

@app.get("/test")
async def endpoint():
    print("Endpoint")
    return {"msg": "ok"}

# Output:
# 1st - Before
# 2nd - Before
# 3rd - Before
# Endpoint
# 3rd - After
# 2nd - After
# 1st - After
```

**Rule:** Define them in the order you want them to execute (top = first, bottom = last)

### Method 2: Using `app.add_middleware()` - Reverse Order

With `add_middleware()`, the order is **reversed** (last added = first executed):

```python
from fastapi import FastAPI
from starlette.middleware.base import BaseHTTPMiddleware

app = FastAPI()

class FirstMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request, call_next):
        print("1st - Before")
        response = await call_next(request)
        print("1st - After")
        return response

class SecondMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request, call_next):
        print("2nd - Before")
        response = await call_next(request)
        print("2nd - After")
        return response

# ⚠️ Add in REVERSE order of desired execution
app.add_middleware(SecondMiddleware)  # Executes SECOND
app.add_middleware(FirstMiddleware)   # Executes FIRST

# Output:
# 1st - Before
# 2nd - Before
# Endpoint
# 2nd - After
# 1st - After
```

**Rule:** Add them in **reverse order** of desired execution (bottom = first, top = last)

## Where to Place Middleware in Your Code

### Option 1: In `main.py` (Simple Projects)

```python
# main.py
from fastapi import FastAPI, Request

app = FastAPI()

# Place middlewares RIGHT AFTER creating the app instance
# BEFORE defining your routes

@app.middleware("http")
async def log_middleware(request: Request, call_next):
    print(f"Request: {request.url.path}")
    response = await call_next(request)
    return response

@app.middleware("http")
async def auth_middleware(request: Request, call_next):
    # auth logic
    response = await call_next(request)
    return response

# Then define your routes
@app.get("/")
async def root():
    return {"msg": "hello"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
```

### Option 2: Separate File (Better Organization)

```python
# middlewares.py
from fastapi import Request
import time

async def log_middleware(request: Request, call_next):
    print(f"📝 Request: {request.method} {request.url.path}")
    response = await call_next(request)
    print(f"📝 Response: {response.status_code}")
    return response

async def timing_middleware(request: Request, call_next):
    start = time.time()
    response = await call_next(request)
    duration = time.time() - start
    response.headers["X-Process-Time"] = str(duration)
    return response

async def auth_middleware(request: Request, call_next):
    token = request.headers.get("Authorization")
    if not token and request.url.path not in ["/public", "/health"]:
        from fastapi.responses import JSONResponse
        return JSONResponse({"error": "Unauthorized"}, status_code=401)
    response = await call_next(request)
    return response
```

```python
# main.py
from fastapi import FastAPI
from middlewares import log_middleware, timing_middleware, auth_middleware

app = FastAPI()

# Register middlewares in desired order
app.middleware("http")(log_middleware)      # First
app.middleware("http")(timing_middleware)   # Second
app.middleware("http")(auth_middleware)     # Third

# Or use decorator syntax in main.py
# @app.middleware("http")
# async def log_middleware(request, call_next):
#     ...

@app.get("/")
async def root():
    return {"msg": "hello"}
```

### Option 3: Modular Structure (Large Projects)

```
my_project/
├── main.py
├── middlewares/
│   ├── __init__.py
│   ├── logging.py
│   ├── auth.py
│   ├── timing.py
│   └── tracing.py
├── routers/
│   ├── users.py
│   └── products.py
└── config.py
```

```python
# middlewares/__init__.py
from .logging import log_middleware
from .auth import auth_middleware
from .timing import timing_middleware
from .tracing import tracing_middleware

# Define the order here
MIDDLEWARES = [
    log_middleware,      # 1st
    timing_middleware,   # 2nd
    auth_middleware,     # 3rd
    tracing_middleware,  # 4th
]
```

```python
# middlewares/logging.py
from fastapi import Request

async def log_middleware(request: Request, call_next):
    print(f"Request: {request.url.path}")
    response = await call_next(request)
    return response
```

```python
# middlewares/auth.py
from fastapi import Request
from fastapi.responses import JSONResponse

async def auth_middleware(request: Request, call_next):
    token = request.headers.get("Authorization")
    if not token and request.url.path not in ["/public"]:
        return JSONResponse({"error": "Unauthorized"}, status_code=401)
    response = await call_next(request)
    return response
```

```python
# main.py
from fastapi import FastAPI
from middlewares import MIDDLEWARES

app = FastAPI()

# Register all middlewares in order
for middleware in MIDDLEWARES:
    app.middleware("http")(middleware)

# Import routers
from routers import users, products
app.include_router(users.router)
app.include_router(products.router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
```

### Option 4: Configuration-Based (Most Flexible)

```python
# config.py
class Settings:
    ENABLE_LOGGING = True
    ENABLE_AUTH = True
    ENABLE_RATE_LIMIT = False
    ENABLE_TRACING = True

settings = Settings()
```

```python
# main.py
from fastapi import FastAPI, Request
from config import settings

app = FastAPI()

# Conditionally add middlewares based on config
if settings.ENABLE_LOGGING:
    @app.middleware("http")
    async def log_middleware(request: Request, call_next):
        print(f"Log: {request.url.path}")
        response = await call_next(request)
        return response

if settings.ENABLE_AUTH:
    @app.middleware("http")
    async def auth_middleware(request: Request, call_next):
        # auth logic
        response = await call_next(request)
        return response

if settings.ENABLE_TRACING:
    @app.middleware("http")
    async def tracing_middleware(request: Request, call_next):
        # tracing logic
        response = await call_next(request)
        return response

@app.get("/")
async def root():
    return {"msg": "hello"}
```

## Practical Example: Complete Middleware Setup

```python
# main.py
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
import time
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()

# ============================================
# MIDDLEWARE ORDER (Top to Bottom)
# ============================================

# 1️⃣ FIRST: Exception Handler (catch everything)
@app.middleware("http")
async def exception_handler(request: Request, call_next):
    try:
        response = await call_next(request)
        return response
    except Exception as e:
        logger.error(f"Unhandled error: {e}", exc_info=True)
        return JSONResponse(
            status_code=500,
            content={"error": "Internal server error"}
        )

# 2️⃣ SECOND: Request Logging (log all requests)
@app.middleware("http")
async def log_requests(request: Request, call_next):
    logger.info(f"📝 {request.method} {request.url.path}")
    response = await call_next(request)
    logger.info(f"📝 Status: {response.status_code}")
    return response

# 3️⃣ THIRD: Timing (measure performance)
@app.middleware("http")
async def add_timing(request: Request, call_next):
    start = time.time()
    response = await call_next(request)
    duration = time.time() - start
    response.headers["X-Process-Time"] = f"{duration:.3f}"
    logger.info(f"⏱️  Completed in {duration:.3f}s")
    return response

# 4️⃣ FOURTH: Authentication (check credentials)
@app.middleware("http")
async def authenticate(request: Request, call_next):
    # Skip auth for public endpoints
    public_paths = ["/", "/health", "/public"]
    if request.url.path in public_paths:
        return await call_next(request)
    
    # Check token
    token = request.headers.get("Authorization")
    if not token or token != "Bearer secret-token":
        logger.warning(f"🔐 Unauthorized access attempt to {request.url.path}")
        return JSONResponse(
            status_code=401,
            content={"error": "Unauthorized"}
        )
    
    logger.info(f"🔐 Authenticated request to {request.url.path}")
    response = await call_next(request)
    return response

# 5️⃣ FIFTH: Add Custom Headers (innermost)
@app.middleware("http")
async def add_headers(request: Request, call_next):
    response = await call_next(request)
    response.headers["X-Custom-Header"] = "MyApp"
    response.headers["X-Version"] = "1.0.0"
    return response

# ============================================
# ROUTES
# ============================================

@app.get("/")
async def root():
    return {"message": "Public endpoint"}

@app.get("/health")
async def health():
    return {"status": "healthy"}

@app.get("/protected")
async def protected():
    return {"message": "Protected data", "secret": "value"}

@app.get("/error")
async def error():
    raise ValueError("This is a test error")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
```

### Testing the Order:

```bash
# Public endpoint (no auth needed)
curl http://localhost:8000/

# Protected endpoint (needs token)
curl -H "Authorization: Bearer secret-token" http://localhost:8000/protected

# Unauthorized attempt
curl http://localhost:8000/protected

# Error handling
curl http://localhost:8000/error
```

## Summary

| Method | Order Control | Where to Place |
|--------|---------------|----------------|
| `@app.middleware("http")` | Top to bottom in code | After `app = FastAPI()`, before routes |
| `app.middleware("http")(func)` | Order of function calls | After `app = FastAPI()`, before routes |
| `app.add_middleware(Class)` | **Reverse** (bottom to top) | After `app = FastAPI()`, before routes |

**Key Points:**
1. ✅ Define middlewares **after** creating `app = FastAPI()`
2. ✅ Define middlewares **before** defining routes
3. ✅ Order matters - first defined = first executed (for `@app.middleware`)
4. ✅ For better organization, put middlewares in separate files
5. ✅ Use clear naming and comments to document order

The framework recognizes middleware as soon as you use the decorator on the `app` instance!


