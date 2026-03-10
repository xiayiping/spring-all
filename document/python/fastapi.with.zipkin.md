# FastAPI with Zipkin Tracing (Compatible with Spring Boot/Brave)

Here's a complete implementation for FastAPI that's compatible with Spring Boot's Zipkin tracing using Brave:

## Installation

```bash
pip install fastapi uvicorn py_zipkin requests
```

## Complete FastAPI Implementation

```python
# main.py
from fastapi import FastAPI, Request, Response
from fastapi.responses import JSONResponse
import time
import socket
import requests
from py_zipkin.zipkin import zipkin_span, create_http_headers_for_new_span
from py_zipkin.encoding import Encoding
from py_zipkin.transport import BaseTransportHandler
from contextlib import contextmanager
from typing import Optional
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="FastAPI Zipkin Service")

# Configuration
ZIPKIN_URL = "http://localhost:9411"  # Your Zipkin server URL
SERVICE_NAME = "fastapi-service"
HOST = socket.gethostbyname(socket.gethostname())
PORT = 8000


class HttpTransport(BaseTransportHandler):
    """Transport handler to send spans to Zipkin via HTTP"""
    
    def __init__(self, zipkin_url: str):
        self.zipkin_url = zipkin_url
        self.endpoint = f"{zipkin_url}/api/v2/spans"
    
    def get_max_payload_bytes(self):
        return None
    
    def send(self, encoded_span):
        try:
            headers = {'Content-Type': 'application/json'}
            response = requests.post(
                self.endpoint,
                data=encoded_span,
                headers=headers,
                timeout=5
            )
            if response.status_code != 202:
                logger.error(f"Failed to send span: {response.status_code}")
        except Exception as e:
            logger.error(f"Error sending span to Zipkin: {e}")


# Initialize transport
http_transport = HttpTransport(ZIPKIN_URL)


def extract_trace_context(request: Request):
    """Extract trace context from incoming headers (B3 propagation format)"""
    headers = request.headers
    
    # B3 single header format (used by Spring Boot/Brave)
    b3_header = headers.get('b3')
    if b3_header:
        parts = b3_header.split('-')
        if len(parts) >= 2:
            return {
                'trace_id': parts[0],
                'span_id': parts[1],
                'parent_span_id': parts[1],
                'is_sampled': parts[2] == '1' if len(parts) > 2 else True
            }
    
    # B3 multi-header format (fallback)
    trace_id = headers.get('x-b3-traceid')
    span_id = headers.get('x-b3-spanid')
    parent_span_id = headers.get('x-b3-parentspanid')
    sampled = headers.get('x-b3-sampled', '1')
    
    if trace_id and span_id:
        return {
            'trace_id': trace_id,
            'span_id': span_id,
            'parent_span_id': parent_span_id or span_id,
            'is_sampled': sampled == '1'
        }
    
    return None


@contextmanager
def trace_context(request: Request, span_name: str):
    """Context manager for creating Zipkin spans"""
    trace_info = extract_trace_context(request)
    
    if trace_info:
        # Continue existing trace
        with zipkin_span(
            service_name=SERVICE_NAME,
            span_name=span_name,
            transport_handler=http_transport,
            encoding=Encoding.V2_JSON,
            zipkin_attrs={
                'trace_id': trace_info['trace_id'],
                'span_id': trace_info['span_id'],
                'parent_span_id': trace_info['parent_span_id'],
                'is_sampled': trace_info['is_sampled'],
            },
            host=HOST,
            port=PORT,
        ):
            yield
    else:
        # Start new trace
        with zipkin_span(
            service_name=SERVICE_NAME,
            span_name=span_name,
            transport_handler=http_transport,
            encoding=Encoding.V2_JSON,
            sample_rate=100.0,  # Sample 100% of requests
            host=HOST,
            port=PORT,
        ):
            yield


@app.middleware("http")
async def zipkin_middleware(request: Request, call_next):
    """Middleware to add Zipkin tracing to all requests"""
    span_name = f"{request.method} {request.url.path}"
    
    with trace_context(request, span_name):
        # Add tags
        from py_zipkin.zipkin import zipkin_span as current_span
        if hasattr(current_span, 'update_binary_annotations'):
            current_span.update_binary_annotations({
                'http.method': request.method,
                'http.path': request.url.path,
                'http.host': request.url.hostname or 'unknown',
            })
        
        start_time = time.time()
        response = await call_next(request)
        duration = time.time() - start_time
        
        # Add response status
        if hasattr(current_span, 'update_binary_annotations'):
            current_span.update_binary_annotations({
                'http.status_code': str(response.status_code),
                'http.duration_ms': f"{duration * 1000:.2f}",
            })
        
        return response


def call_downstream_service(url: str, span_name: str):
    """Helper function to call downstream services with trace propagation"""
    headers = create_http_headers_for_new_span()
    
    # Create child span for the outgoing request
    with zipkin_span(
        service_name=SERVICE_NAME,
        span_name=span_name,
        transport_handler=http_transport,
        encoding=Encoding.V2_JSON,
        kind='CLIENT',
    ):
        response = requests.get(url, headers=headers, timeout=5)
        return response


# API Endpoints

@app.get("/")
async def root():
    return {"message": "FastAPI with Zipkin tracing", "service": SERVICE_NAME}


@app.get("/api/users/{user_id}")
async def get_user(user_id: int):
    """Sample endpoint that might call other services"""
    
    # Simulate some processing
    time.sleep(0.1)
    
    return {
        "user_id": user_id,
        "name": f"User {user_id}",
        "email": f"user{user_id}@example.com"
    }


@app.get("/api/call-spring")
async def call_spring_service():
    """Example of calling a Spring Boot service"""
    try:
        # Call your Spring Boot service
        spring_url = "http://localhost:8080/api/endpoint"  # Change to your Spring Boot URL
        
        response = call_downstream_service(
            spring_url,
            span_name="GET spring-boot-service"
        )
        
        return {
            "status": "success",
            "spring_response": response.json() if response.ok else None,
            "status_code": response.status_code
        }
    except Exception as e:
        logger.error(f"Error calling Spring service: {e}")
        return JSONResponse(
            status_code=500,
            content={"error": str(e)}
        )


@app.get("/api/chain")
async def chain_request(request: Request):
    """Example of chained requests to demonstrate trace propagation"""
    
    with trace_context(request, "chain-processing"):
        # Step 1
        with zipkin_span(
            service_name=SERVICE_NAME,
            span_name="step-1-database-query",
            transport_handler=http_transport,
            encoding=Encoding.V2_JSON,
        ):
            time.sleep(0.05)
            step1_result = "Database query completed"
        
        # Step 2
        with zipkin_span(
            service_name=SERVICE_NAME,
            span_name="step-2-cache-check",
            transport_handler=http_transport,
            encoding=Encoding.V2_JSON,
        ):
            time.sleep(0.03)
            step2_result = "Cache check completed"
        
        # Step 3
        with zipkin_span(
            service_name=SERVICE_NAME,
            span_name="step-3-business-logic",
            transport_handler=http_transport,
            encoding=Encoding.V2_JSON,
        ):
            time.sleep(0.02)
            step3_result = "Business logic executed"
    
    return {
        "steps": [step1_result, step2_result, step3_result],
        "total": "Chain completed"
    }


@app.get("/health")
async def health():
    return {"status": "healthy"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=PORT)
```

## Docker Compose for Testing (Optional)

```yaml
# docker-compose.yml
version: '3'

services:
  zipkin:
    image: openzipkin/zipkin
    ports:
      - "9411:9411"
    environment:
      - STORAGE_TYPE=mem
```

## Usage

1. **Start Zipkin:**
```bash
docker-compose up -d
# Or run Zipkin directly
docker run -d -p 9411:9411 openzipkin/zipkin
```

2. **Run FastAPI:**
```bash
python main.py
```

3. **Test the endpoints:**
```bash
# Simple request
curl http://localhost:8000/api/users/123

# Chain request with multiple spans
curl http://localhost:8000/api/chain

# Call with existing trace (simulating Spring Boot calling FastAPI)
curl -H "b3: 80f198ee56343ba864fe8b2a57d3eff7-05e3ac9a4f6e3b90-1" \
     http://localhost:8000/api/users/456
```

4. **View traces in Zipkin UI:**
    - Open http://localhost:9411
    - You should see traces from both FastAPI and Spring Boot services linked together

## Key Features

- **B3 Propagation**: Compatible with Spring Boot/Brave trace context propagation
- **Trace Continuation**: Automatically continues traces from Spring Boot
- **Child Spans**: Creates child spans for internal operations
- **Downstream Calls**: Propagates trace context to other services
- **Middleware**: Automatic tracing for all HTTP requests
- **Flexible**: Easy to add custom spans and annotations

The trace IDs will be shared between your Spring Boot and FastAPI services, allowing you to see the complete request flow in Zipkin UI.