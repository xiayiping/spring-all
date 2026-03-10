# Modern Zipkin Integration for FastAPI

You're right - `py-zipkin` is quite old and not well-maintained. Here are the **modern alternatives**:

## 1. **OpenTelemetry** (Recommended ⭐)

**This is the modern standard** for distributed tracing. It supports Zipkin, Jaeger, and other backends.

### Why OpenTelemetry?

- ✅ Active development & community support
- ✅ Industry standard (CNCF project)
- ✅ Works with Zipkin, Jaeger, Tempo, and more
- ✅ Auto-instrumentation for FastAPI
- ✅ Rich ecosystem with many integrations

### Installation

```bash
pip install opentelemetry-api
pip install opentelemetry-sdk
pip install opentelemetry-instrumentation-fastapi
pip install opentelemetry-exporter-zipkin
```

### Basic Implementation

```python
# main.py
from fastapi import FastAPI
from opentelemetry import trace
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.zipkin.json import ZipkinExporter
from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor
from opentelemetry.sdk.resources import Resource

# Configure the tracer
resource = Resource(attributes={
    "service.name": "my-fastapi-service"
})

tracer_provider = TracerProvider(resource=resource)
trace.set_tracer_provider(tracer_provider)

# Configure Zipkin exporter
zipkin_exporter = ZipkinExporter(
    endpoint="http://localhost:9411/api/v2/spans",
)

# Add span processor
span_processor = BatchSpanProcessor(zipkin_exporter)
tracer_provider.add_span_processor(span_processor)

# Create FastAPI app
app = FastAPI()

# Auto-instrument FastAPI (this adds tracing automatically!)
FastAPIInstrumentor.instrument_app(app)

# Your routes
@app.get("/")
async def root():
    return {"message": "Hello World"}

@app.get("/users/{user_id}")
async def get_user(user_id: int):
    # This is automatically traced
    return {"user_id": user_id, "name": "John"}
```

### With Custom Spans

```python
from opentelemetry import trace
from fastapi import FastAPI
import httpx

tracer = trace.get_tracer(__name__)

@app.get("/process")
async def process_data():
    # Create custom span
    with tracer.start_as_current_span("process_data"):
        
        # Add attributes to span
        current_span = trace.get_current_span()
        current_span.set_attribute("user.id", "12345")
        current_span.set_attribute("processing.type", "batch")
        
        # Simulate work
        result = await do_work()
        
        # Create nested span
        with tracer.start_as_current_span("external_api_call"):
            async with httpx.AsyncClient() as client:
                response = await client.get("https://api.example.com/data")
                current_span.set_attribute("http.status_code", response.status_code)
        
        return {"result": result}

async def do_work():
    with tracer.start_as_current_span("do_work_internal"):
        # Your business logic
        return "processed"
```

### Complete Example with Database

```python
from fastapi import FastAPI, Depends
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from sqlalchemy.orm import sessionmaker
from opentelemetry import trace
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.zipkin.json import ZipkinExporter
from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor
from opentelemetry.instrumentation.sqlalchemy import SQLAlchemyInstrumentor
from opentelemetry.sdk.resources import Resource

# Setup tracing
resource = Resource(attributes={
    "service.name": "my-fastapi-service",
    "service.version": "1.0.0",
    "deployment.environment": "production"
})

tracer_provider = TracerProvider(resource=resource)
trace.set_tracer_provider(tracer_provider)

zipkin_exporter = ZipkinExporter(
    endpoint="http://localhost:9411/api/v2/spans",
)

tracer_provider.add_span_processor(BatchSpanProcessor(zipkin_exporter))

# Database setup
DATABASE_URL = "postgresql+asyncpg://user:pass@localhost/db"
engine = create_async_engine(DATABASE_URL)

# Instrument SQLAlchemy (auto-trace DB queries)
SQLAlchemyInstrumentor().instrument(
    engine=engine.sync_engine,
)

async_session = sessionmaker(
    engine, class_=AsyncSession, expire_on_commit=False
)

app = FastAPI()

# Auto-instrument FastAPI
FastAPIInstrumentor.instrument_app(app)

tracer = trace.get_tracer(__name__)

# Dependency
async def get_db():
    async with async_session() as session:
        yield session

@app.get("/users/{user_id}")
async def get_user(user_id: int, db: AsyncSession = Depends(get_db)):
    # Both HTTP request and DB query are automatically traced
    with tracer.start_as_current_span("fetch_user_from_db"):
        result = await db.execute(f"SELECT * FROM users WHERE id = {user_id}")
        user = result.fetchone()
    
    with tracer.start_as_current_span("process_user_data"):
        # Custom business logic
        processed = process_user(user)
    
    return processed
```

### Environment Configuration

```python
# config.py
import os
from opentelemetry import trace
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.zipkin.json import ZipkinExporter
from opentelemetry.sdk.resources import Resource

def setup_tracing(service_name: str):
    # Get config from environment
    zipkin_endpoint = os.getenv(
        "ZIPKIN_ENDPOINT", 
        "http://localhost:9411/api/v2/spans"
    )
    environment = os.getenv("ENVIRONMENT", "development")
    
    resource = Resource(attributes={
        "service.name": service_name,
        "deployment.environment": environment,
    })
    
    tracer_provider = TracerProvider(resource=resource)
    trace.set_tracer_provider(tracer_provider)
    
    zipkin_exporter = ZipkinExporter(endpoint=zipkin_endpoint)
    tracer_provider.add_span_processor(BatchSpanProcessor(zipkin_exporter))
    
    return trace.get_tracer(__name__)
```

```python
# main.py
from fastapi import FastAPI
from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor
from config import setup_tracing

# Setup tracing
tracer = setup_tracing("my-service")

app = FastAPI()
FastAPIInstrumentor.instrument_app(app)

@app.get("/")
async def root():
    return {"message": "Hello"}
```

## 2. **fastapi-tracing** (Simpler Alternative)

A lightweight wrapper specifically for FastAPI.

```bash
pip install fastapi-tracing[zipkin]
```

```python
from fastapi import FastAPI
from fastapi_tracing import TracingMiddleware

app = FastAPI()

# Add tracing middleware
app.add_middleware(
    TracingMiddleware,
    service_name="my-service",
    zipkin_url="http://localhost:9411/api/v2/spans",
    sample_rate=1.0,  # Trace 100% of requests
)

@app.get("/")
async def root():
    return {"message": "Hello World"}
```

## 3. **Starlette-Zipkin** (Lightweight)

Built on Starlette (FastAPI's foundation).

```bash
pip install starlette-zipkin
```

```python
from fastapi import FastAPI
from starlette_zipkin import ZipkinMiddleware

app = FastAPI()

app.add_middleware(
    ZipkinMiddleware,
    service_name="my-fastapi-service",
    zipkin_address="http://localhost:9411/api/v2/spans",
    sample_rate=1.0,
)

@app.get("/")
async def root():
    return {"message": "Hello World"}
```

## 4. **OpenTelemetry with Jaeger** (Alternative Backend)

If you're open to using Jaeger instead of Zipkin:

```bash
pip install opentelemetry-api
pip install opentelemetry-sdk
pip install opentelemetry-instrumentation-fastapi
pip install opentelemetry-exporter-jaeger
```

```python
from opentelemetry.exporter.jaeger.thrift import JaegerExporter
from opentelemetry.sdk.trace.export import BatchSpanProcessor

# Replace Zipkin exporter with Jaeger
jaeger_exporter = JaegerExporter(
    agent_host_name="localhost",
    agent_port=6831,
)

tracer_provider.add_span_processor(BatchSpanProcessor(jaeger_exporter))
```

## Comparison Table

| Library | Maintenance | Ease of Use | Features | Backend Support |
|---------|-------------|-------------|----------|-----------------|
| **OpenTelemetry** | ✅ Active | ⭐⭐⭐ Medium | ⭐⭐⭐⭐⭐ Comprehensive | Zipkin, Jaeger, Tempo, more |
| **fastapi-tracing** | ⚠️ Moderate | ⭐⭐⭐⭐⭐ Easy | ⭐⭐⭐ Basic | Zipkin, Jaeger |
| **starlette-zipkin** | ⚠️ Low | ⭐⭐⭐⭐ Easy | ⭐⭐ Basic | Zipkin only |
| **py-zipkin** | ❌ Archived | ⭐⭐ Hard | ⭐⭐ Basic | Zipkin only |

## My Recommendation

**Use OpenTelemetry** because:

1. **Future-proof**: Industry standard that's actively maintained
2. **Flexible**: Switch between Zipkin, Jaeger, or other backends without code changes
3. **Auto-instrumentation**: Minimal code needed
4. **Rich features**: Custom spans, attributes, events, context propagation
5. **Wide adoption**: Used by major companies and cloud providers

## Quick Start with Docker Compose

```yaml
# docker-compose.yml
version: '3'

services:
  zipkin:
    image: openzipkin/zipkin
    ports:
      - "9411:9411"
  
  app:
    build: .
    ports:
      - "8000:8000"
    environment:
      - ZIPKIN_ENDPOINT=http://zipkin:9411/api/v2/spans
    depends_on:
      - zipkin
```

```python
# main.py with OpenTelemetry
import os
from fastapi import FastAPI
from opentelemetry import trace
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.zipkin.json import ZipkinExporter
from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor
from opentelemetry.sdk.resources import Resource

# Setup
resource = Resource(attributes={"service.name": "my-service"})
tracer_provider = TracerProvider(resource=resource)
trace.set_tracer_provider(tracer_provider)

zipkin_exporter = ZipkinExporter(
    endpoint=os.getenv("ZIPKIN_ENDPOINT", "http://localhost:9411/api/v2/spans")
)
tracer_provider.add_span_processor(BatchSpanProcessor(zipkin_exporter))

app = FastAPI()
FastAPIInstrumentor.instrument_app(app)

@app.get("/")
async def root():
    return {"message": "Hello World"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
```

Run it:
```bash
docker-compose up -d zipkin
python main.py
# Visit http://localhost:9411 for Zipkin UI
```

**Bottom line**: Go with OpenTelemetry - it's the modern, standard way to do distributed tracing in 2026.


# Difference Between `__name__` and `'process_data'`

These two names serve **completely different purposes** in OpenTelemetry:

## Quick Answer

```python
tracer = trace.get_tracer(__name__)
#                         ^^^^^^^^
#                         TRACER NAME - identifies which component created the tracer

with tracer.start_as_current_span("process_data"):
#                                  ^^^^^^^^^^^^^^
#                                  SPAN NAME - describes what operation is being traced
```

## 1. `__name__` - Tracer Name (Component Identifier)

This identifies **which part of your application** is creating traces.

```python
tracer = trace.get_tracer(__name__)
#                         ^^^^^^^^
#                         This is the TRACER's identity
```

### What is `__name__`?

It's a Python built-in variable that contains the **module path**:

```python
# In file: app/services/user_service.py
print(__name__)  # Output: "app.services.user_service"

# In file: app/api/routes.py
print(__name__)  # Output: "app.api.routes"

# In main file:
print(__name__)  # Output: "__main__"
```

### Why Use `__name__` for Tracer?

It helps you **identify which module/component** created the spans when viewing traces:

```python
# app/services/user_service.py
from opentelemetry import trace

tracer = trace.get_tracer(__name__)  # Tracer name: "app.services.user_service"

def get_user(user_id: int):
    with tracer.start_as_current_span("fetch_user"):
        # In Zipkin, you'll see this span came from "app.services.user_service"
        pass
```

```python
# app/services/order_service.py
from opentelemetry import trace

tracer = trace.get_tracer(__name__)  # Tracer name: "app.services.order_service"

def create_order(user_id: int):
    with tracer.start_as_current_span("create_order"):
        # In Zipkin, you'll see this span came from "app.services.order_service"
        pass
```

### In Zipkin UI:

```
Trace View:
├─ [app.services.user_service] fetch_user (100ms)
└─ [app.services.order_service] create_order (200ms)
    └─ [app.services.payment] process_payment (150ms)
```

The tracer name helps you see **which component** in your codebase is responsible for each span.

## 2. `"process_data"` - Span Name (Operation Description)

This describes **what specific operation** is being performed.

```python
with tracer.start_as_current_span("process_data"):
#                                  ^^^^^^^^^^^^^^
#                                  This describes WHAT you're doing
```

### Examples of Span Names

Span names should be **descriptive of the operation**:

```python
from opentelemetry import trace

tracer = trace.get_tracer(__name__)  # Component: "app.api.routes"

@app.get("/users/{user_id}")
async def get_user(user_id: int):
    # Span name describes the operation
    with tracer.start_as_current_span("fetch_user_from_database"):
        user = await db.get_user(user_id)
    
    with tracer.start_as_current_span("validate_user_permissions"):
        is_valid = validate(user)
    
    with tracer.start_as_current_span("format_user_response"):
        response = format_response(user)
    
    return response
```

## Complete Example Showing Both

```python
# app/services/user_service.py
from opentelemetry import trace

# Get tracer for THIS module
tracer = trace.get_tracer(__name__)  
# tracer name = "app.services.user_service"

class UserService:
    async def get_user(self, user_id: int):
        # Span name describes the specific operation
        with tracer.start_as_current_span("get_user"):
            # Do work
            pass
    
    async def update_user(self, user_id: int, data: dict):
        with tracer.start_as_current_span("update_user"):
            # Validate input
            with tracer.start_as_current_span("validate_user_data"):
                validate(data)
            
            # Save to database
            with tracer.start_as_current_span("save_user_to_database"):
                await db.save(user_id, data)
            
            # Send notification
            with tracer.start_as_current_span("send_update_notification"):
                await notifications.send(user_id)
```

```python
# app/services/order_service.py
from opentelemetry import trace

# Different tracer for THIS module
tracer = trace.get_tracer(__name__)  
# tracer name = "app.services.order_service"

class OrderService:
    async def create_order(self, user_id: int, items: list):
        # Different span names, but from a different tracer
        with tracer.start_as_current_span("create_order"):
            with tracer.start_as_current_span("validate_order_items"):
                validate(items)
            
            with tracer.start_as_current_span("calculate_total"):
                total = calculate(items)
            
            with tracer.start_as_current_span("save_order_to_database"):
                await db.save_order(user_id, items, total)
```

## In Zipkin UI, You'll See:

```
Trace ID: abc123 (Full request trace)
Duration: 500ms

Service: my-fastapi-app
├─ [app.api.routes] GET /users/123/orders (500ms)
    │
    ├─ [app.services.user_service] get_user (100ms)
    │   └─ [app.services.user_service] validate_user_data (20ms)
    │
    └─ [app.services.order_service] create_order (350ms)
        ├─ [app.services.order_service] validate_order_items (50ms)
        ├─ [app.services.order_service] calculate_total (30ms)
        └─ [app.services.order_service] save_order_to_database (250ms)
            └─ [app.database] execute_query (240ms)
```

## Visual Comparison

```python
from opentelemetry import trace

# Module: app/api/users.py
tracer = trace.get_tracer(__name__)
#       ┌────────────────┴────────────────┐
#       │ Tracer Name: "app.api.users"    │
#       │ (Who is creating the spans?)    │
#       └─────────────────────────────────┘

@app.get("/users/{user_id}")
async def get_user_endpoint(user_id: int):
    with tracer.start_as_current_span("handle_get_user_request"):
    #                                  └──────────┬──────────┘
    #                                    Span Name: "handle_get_user_request"
    #                                    (What operation is being performed?)
        
        with tracer.start_as_current_span("fetch_from_cache"):
        #                                  └──────┬──────┘
        #                                   Describes cache lookup
            user = cache.get(user_id)
        
        if not user:
            with tracer.start_as_current_span("fetch_from_database"):
            #                                  └───────┬────────┘
            #                                   Describes DB query
                user = await db.get_user(user_id)
        
        return user
```

## Best Practices

### For Tracer Names (use `__name__`)

```python
# ✅ GOOD - Use __name__ to identify the module
tracer = trace.get_tracer(__name__)

# ❌ BAD - Hardcoded string loses module information
tracer = trace.get_tracer("my_tracer")

# ⚠️ OK - If you need custom naming (advanced use case)
tracer = trace.get_tracer("payment_processor", version="1.0.0")
```

### For Span Names (descriptive operations)

```python
# ✅ GOOD - Clear, descriptive operation names
with tracer.start_as_current_span("fetch_user_from_database"):
with tracer.start_as_current_span("validate_email_format"):
with tracer.start_as_current_span("calculate_order_total"):
with tracer.start_as_current_span("send_notification_email"):

# ❌ BAD - Vague or too generic
with tracer.start_as_current_span("do_stuff"):
with tracer.start_as_current_span("process"):
with tracer.start_as_current_span("handle"):

# ❌ BAD - Using variable values (creates too many unique span names)
with tracer.start_as_current_span(f"fetch_user_{user_id}"):  # DON'T DO THIS
# Instead, use attributes:
with tracer.start_as_current_span("fetch_user") as span:
    span.set_attribute("user.id", user_id)  # ✅ GOOD
```

## Real-World Example

```python
# app/services/payment_service.py
from opentelemetry import trace
import httpx

# Tracer identifies this component
tracer = trace.get_tracer(__name__)  # "app.services.payment_service"

class PaymentService:
    async def process_payment(self, order_id: str, amount: float):
        # Root span for this operation
        with tracer.start_as_current_span("process_payment") as span:
            # Add metadata to the span
            span.set_attribute("order.id", order_id)
            span.set_attribute("payment.amount", amount)
            
            # Sub-operation: validate
            with tracer.start_as_current_span("validate_payment_details"):
                if amount <= 0:
                    span.set_attribute("validation.error", "Invalid amount")
                    raise ValueError("Invalid amount")
            
            # Sub-operation: call external API
            with tracer.start_as_current_span("call_payment_gateway_api") as api_span:
                async with httpx.AsyncClient() as client:
                    response = await client.post(
                        "https://payment-gateway.com/charge",
                        json={"amount": amount}
                    )
                    api_span.set_attribute("http.status_code", response.status_code)
            
            # Sub-operation: update database
            with tracer.start_as_current_span("update_order_status"):
                await db.update_order(order_id, status="paid")
            
            return {"status": "success"}
```

**In Zipkin, you'll see:**

```
Tracer: app.services.payment_service
  ├─ Span: process_payment (300ms)
      ├─ Span: validate_payment_details (10ms)
      ├─ Span: call_payment_gateway_api (250ms)
      └─ Span: update_order_status (40ms)
```

## Summary Table

| Aspect | Tracer Name (`__name__`) | Span Name (`"operation"`) |
|--------|-------------------------|--------------------------|
| **Purpose** | Identifies component/module | Describes specific operation |
| **Scope** | One per module/file | Many per module |
| **Changes** | Rarely (module name) | Frequently (different operations) |
| **Best Practice** | Use `__name__` | Use descriptive strings |
| **Example** | `"app.services.user_service"` | `"fetch_user"`, `"update_email"` |
| **Visibility** | Shows in trace metadata | Shows as span name in UI |

**Think of it this way:**
- **Tracer name** = Which **team/department** is doing the work
- **Span name** = What **specific task** they're performing