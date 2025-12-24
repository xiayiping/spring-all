Ah, I understand! You're using the gunicorn logger in your FastAPI app. Here's how to properly configure it to write to files:

## 1. In your main.py with gunicorn logger

**main.py:**
```python
from fastapi import FastAPI, Request
import logging

app = FastAPI()

# Get gunicorn loggers
gunicorn_error_logger = logging.getLogger("gunicorn.error")
gunicorn_access_logger = logging.getLogger("gunicorn.access")

# Set app logger to use gunicorn's error logger
app.logger = gunicorn_error_logger

@app.middleware("http")
async def log_requests(request: Request, call_next):
    # Process request
    response = await call_next(request)
    
    # Log using gunicorn access logger
    gunicorn_access_logger.info(
        f'{request.client.host} - "{request.method} {request.url.path}" {response.status_code}'
    )
    
    return response

@app.get("/")
def read_root():
    gunicorn_error_logger.info("Root endpoint called")
    return {"message": "Hello World"}
```

**gunicorn_conf.py:**
```python
import os
import multiprocessing

# Create logs directory
log_dir = "logs"
os.makedirs(log_dir, exist_ok=True)

bind = "0.0.0.0:8000"
workers = multiprocessing.cpu_count() * 2 + 1
worker_class = "uvicorn.workers.UvicornWorker"

# File paths (not "-" which means stdout)
accesslog = os.path.join(log_dir, "access.log")
errorlog = os.path.join(log_dir, "error.log")
loglevel = "info"

access_log_format = '%(h)s %(l)s %(u)s %(t)s "%(r)s" %(s)s %(b)s "%(f)s" "%(a)s"'
```

## 2. Alternative: Configure loggers in main.py startup

**main.py:**
```python
from fastapi import FastAPI, Request
import logging
from logging.handlers import RotatingFileHandler
import os

app = FastAPI()

@app.on_event("startup")
async def startup_event():
    # Create logs directory
    log_dir = "logs"
    os.makedirs(log_dir, exist_ok=True)
    
    # Configure gunicorn.error logger
    error_logger = logging.getLogger("gunicorn.error")
    error_handler = RotatingFileHandler(
        os.path.join(log_dir, "error.log"),
        maxBytes=10485760,
        backupCount=5
    )
    error_formatter = logging.Formatter(
        '%(asctime)s [%(process)d] [%(levelname)s] %(message)s',
        datefmt='[%Y-%m-%d %H:%M:%S %z]'
    )
    error_handler.setFormatter(error_formatter)
    error_logger.addHandler(error_handler)
    
    # Configure gunicorn.access logger
    access_logger = logging.getLogger("gunicorn.access")
    access_handler = RotatingFileHandler(
        os.path.join(log_dir, "access.log"),
        maxBytes=10485760,
        backupCount=5
    )
    access_formatter = logging.Formatter('%(message)s')
    access_handler.setFormatter(access_formatter)
    access_logger.addHandler(access_handler)

@app.get("/")
def read_root():
    logger = logging.getLogger("gunicorn.error")
    logger.info("Root endpoint accessed")
    return {"message": "Hello World"}
```

**gunicorn_conf.py:**
```python
import multiprocessing

bind = "0.0.0.0:8000"
workers = multiprocessing.cpu_count() * 2 + 1
worker_class = "uvicorn.workers.UvicornWorker"

# You can still set these, and main.py will add file handlers
loglevel = "info"
```

## 3. Best approach: Use logconfig_dict in gunicorn_conf.py

**main.py:**
```python
from fastapi import FastAPI, Request
import logging
import time

app = FastAPI()

# Get the loggers (they'll be configured by gunicorn)
error_logger = logging.getLogger("gunicorn.error")
access_logger = logging.getLogger("gunicorn.access")

@app.middleware("http")
async def log_middleware(request: Request, call_next):
    start_time = time.time()
    
    response = await call_next(request)
    
    process_time = time.time() - start_time
    
    # This will write to the file configured in gunicorn_conf.py
    access_logger.info(
        f'{request.client.host} - "{request.method} {request.url.path}" '
        f'{response.status_code} - {process_time:.3f}s'
    )
    
    return response

@app.get("/")
def read_root():
    error_logger.info("Root endpoint called")
    return {"message": "Hello World"}

@app.get("/test")
def test():
    error_logger.debug("Debug message")
    error_logger.info("Info message")
    error_logger.warning("Warning message")
    return {"status": "ok"}
```

**gunicorn_conf.py:**
```python
import os
import multiprocessing

log_dir = "logs"
os.makedirs(log_dir, exist_ok=True)

bind = "0.0.0.0:8000"
workers = multiprocessing.cpu_count() * 2 + 1
worker_class = "uvicorn.workers.UvicornWorker"

# Important: Don't set accesslog/errorlog here if using logconfig_dict
# accesslog = "-"  # Comment this out
# errorlog = "-"   # Comment this out

loglevel = "info"

# Use logconfig_dict to configure file logging
logconfig_dict = {
    'version': 1,
    'disable_existing_loggers': False,
    'formatters': {
        'generic': {
            'format': '%(asctime)s [%(process)d] [%(levelname)s] %(message)s',
            'datefmt': '[%Y-%m-%d %H:%M:%S %z]',
        },
        'access': {
            'format': '%(message)s',
        }
    },
    'handlers': {
        'error_file': {
            'class': 'logging.handlers.RotatingFileHandler',
            'formatter': 'generic',
            'filename': os.path.join(log_dir, 'error.log'),
            'maxBytes': 10485760,  # 10MB
            'backupCount': 5,
        },
        'access_file': {
            'class': 'logging.handlers.RotatingFileHandler',
            'formatter': 'access',
            'filename': os.path.join(log_dir, 'access.log'),
            'maxBytes': 10485760,
            'backupCount': 5,
        },
    },
    'loggers': {
        'gunicorn.error': {
            'handlers': ['error_file'],
            'level': 'INFO',
            'propagate': False,
        },
        'gunicorn.access': {
            'handlers': ['access_file'],
            'level': 'INFO',
            'propagate': False,
        }
    }
}
```

## 4. With both console and file output

**gunicorn_conf.py:**
```python
import os
import multiprocessing

log_dir = "logs"
os.makedirs(log_dir, exist_ok=True)

bind = "0.0.0.0:8000"
workers = multiprocessing.cpu_count() * 2 + 1
worker_class = "uvicorn.workers.UvicornWorker"

loglevel = "info"

logconfig_dict = {
    'version': 1,
    'disable_existing_loggers': False,
    'formatters': {
        'generic': {
            'format': '%(asctime)s [%(process)d] [%(levelname)s] %(message)s',
            'datefmt': '[%Y-%m-%d %H:%M:%S %z]',
        },
        'access': {
            'format': '%(message)s',
        }
    },
    'handlers': {
        'error_console': {
            'class': 'logging.StreamHandler',
            'formatter': 'generic',
            'stream': 'ext://sys.stderr',
        },
        'error_file': {
            'class': 'logging.handlers.RotatingFileHandler',
            'formatter': 'generic',
            'filename': os.path.join(log_dir, 'error.log'),
            'maxBytes': 10485760,
            'backupCount': 5,
        },
        'access_console': {
            'class': 'logging.StreamHandler',
            'formatter': 'access',
            'stream': 'ext://sys.stdout',
        },
        'access_file': {
            'class': 'logging.handlers.RotatingFileHandler',
            'formatter': 'access',
            'filename': os.path.join(log_dir, 'access.log'),
            'maxBytes': 10485760,
            'backupCount': 5,
        },
    },
    'loggers': {
        'gunicorn.error': {
            'handlers': ['error_console', 'error_file'],  # Both!
            'level': 'INFO',
            'propagate': False,
        },
        'gunicorn.access': {
            'handlers': ['access_console', 'access_file'],  # Both!
            'level': 'INFO',
            'propagate': False,
        }
    }
}
```

Now when you run:
```bash
gunicorn -c gunicorn_conf.py main:app
```

Your logs from `logging.getLogger("gunicorn.error")` and `logging.getLogger("gunicorn.access")` in main.py will be written to the files specified in `gunicorn_conf.py`.

The key is using **`logconfig_dict`** in your gunicorn config file - this configures the actual logging handlers that the loggers in your main.py will use.