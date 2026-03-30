# Rust Caching Solutions (Equivalent to Caffeine)

## Quick Comparison

| Rust Crate      | Java Equivalent    | Best For                         |
|-----------------|--------------------|----------------------------------|
| **moka**        | Caffeine           | High-performance, feature-rich ⭐ |
| **cached**      | Simple cache       | Easy macros, simple use cases    |
| **mini-moka**   | Caffeine (lighter) | WASM, embedded                   |
| **quick_cache** | Caffeine           | Fastest, simpler API             |
| **stretto**     | Caffeine           | Admission policy                 |

---

## 1. **Moka** ⭐ (Most Similar to Caffeine)

> Created by the same concepts as Caffeine - **highly recommended**

### Cargo.toml

```toml
[dependencies]
moka = { version = "0.12", features = ["future"] }  # async support
tokio = { version = "1", features = ["full"] }
```

### Basic Usage

```rust
use moka::sync::Cache;
use std::time::Duration;

fn main() {
    // Create cache with max 10,000 entries
    let cache: Cache<String, String> = Cache::builder()
        .max_capacity(10_000)
        .time_to_live(Duration::from_secs(30 * 60))  // 30 minutes
        .time_to_idle(Duration::from_secs(5 * 60))   // 5 minutes idle
        .build();

    // Insert
    cache.insert("key1".to_string(), "value1".to_string());

    // Get
    if let Some(value) = cache.get(&"key1".to_string()) {
        println!("Found: {}", value);
    }

    // Get or insert with closure
    let value = cache.get_with("key2".to_string(), || {
        // Expensive computation
        "computed_value".to_string()
    });

    // Remove
    cache.invalidate(&"key1".to_string());

    // Clear all
    cache.invalidate_all();
}
```

### Async Usage (Tokio)

```rust
use moka::future::Cache;
use std::time::Duration;

#[tokio::main]
async fn main() {
    let cache: Cache<String, String> = Cache::builder()
        .max_capacity(10_000)
        .time_to_live(Duration::from_secs(60 * 30))
        .build();

    // Async insert
    cache.insert("user:1".to_string(), "John".to_string()).await;

    // Async get
    if let Some(value) = cache.get(&"user:1".to_string()).await {
        println!("Found: {}", value);
    }

    // Get or insert async
    let value = cache
        .get_with("user:2".to_string(), async {
            // Simulate async fetch from database
            fetch_from_db("user:2").await
        })
        .await;
}

async fn fetch_from_db(key: &str) -> String {
    // Simulate DB call
    tokio::time::sleep(Duration::from_millis(100)).await;
    format!("User data for {}", key)
}
```

### Complete Service Example

```rust
use moka::future::Cache;
use std::sync::Arc;
use std::time::Duration;
use serde::{Deserialize, Serialize};

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct User {
    pub id: i64,
    pub name: String,
    pub email: String,
}

#[derive(Clone)]
pub struct UserService {
    cache: Cache<i64, User>,
    // db: DatabasePool,
}

impl UserService {
    pub fn new() -> Self {
        let cache = Cache::builder()
            .max_capacity(10_000)
            .time_to_live(Duration::from_secs(60 * 15))    // 15 min TTL
            .time_to_idle(Duration::from_secs(60 * 5))     // 5 min idle
            .eviction_listener(|key, value, cause| {
                println!("Evicted: key={}, cause={:?}", key, cause);
            })
            .build();

        Self { cache }
    }

    pub async fn get_user(&self, user_id: i64) -> Option<User> {
        // Try cache first, otherwise fetch and cache
        self.cache
            .try_get_with(user_id, async move {
                self.fetch_user_from_db(user_id).await
            })
            .await
            .ok()
    }

    pub async fn update_user(&self, user: User) {
        let user_id = user.id;
        // Update database first
        // self.db.update_user(&user).await;
        
        // Then update cache
        self.cache.insert(user_id, user).await;
    }

    pub async fn delete_user(&self, user_id: i64) {
        // Delete from database
        // self.db.delete_user(user_id).await;
        
        // Invalidate cache
        self.cache.invalidate(&user_id).await;
    }

    async fn fetch_user_from_db(&self, user_id: i64) -> Result<User, String> {
        // Simulate database fetch
        tokio::time::sleep(Duration::from_millis(50)).await;
        Ok(User {
            id: user_id,
            name: format!("User {}", user_id),
            email: format!("user{}@example.com", user_id),
        })
    }
}
```

### With Actix-Web

```rust
use actix_web::{web, App, HttpServer, HttpResponse, get};
use moka::future::Cache;
use std::sync::Arc;
use std::time::Duration;

struct AppState {
    user_cache: Cache<i64, User>,
}

#[get("/users/{id}")]
async fn get_user(
    path: web::Path<i64>,
    state: web::Data<AppState>,
) -> HttpResponse {
    let user_id = path.into_inner();
    
    let user = state.user_cache
        .get_with(user_id, async move {
            // Fetch from database
            User {
                id: user_id,
                name: format!("User {}", user_id),
                email: format!("user{}@example.com", user_id),
            }
        })
        .await;
    
    HttpResponse::Ok().json(user)
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    let cache = Cache::builder()
        .max_capacity(10_000)
        .time_to_live(Duration::from_secs(300))
        .build();

    let app_state = web::Data::new(AppState { user_cache: cache });

    HttpServer::new(move || {
        App::new()
            .app_data(app_state.clone())
            .service(get_user)
    })
    .bind("127.0.0.1:8080")?
    .run()
    .await
}
```

---

## 2. **Cached** (Macro-based, Simpler)

### Cargo.toml

```toml
[dependencies]
cached = "0.46"
tokio = { version = "1", features = ["full"] }
```

### Usage with Macros

```rust
use cached::proc_macro::cached;
use cached::TimedSizedCache;

// Simple cached function
#[cached(size = 100, time = 300)]  // 100 entries, 5 min TTL
fn compute_expensive(input: u64) -> u64 {
    println!("Computing for {}...", input);
    // Expensive computation
    input * input
}

// Async cached function
#[cached(
    type = "TimedSizedCache<String, User>",
    create = "{ TimedSizedCache::with_size_and_lifespan(1000, 300) }",
    convert = r#"{ format!("{}", user_id) }"#
)]
async fn get_user_cached(user_id: i64) -> User {
    // This will only be called on cache miss
    fetch_user_from_db(user_id).await
}

// With Result return type
#[cached(
    size = 100,
    time = 60,
    result = true  // Cache only Ok results
)]
async fn get_user_result(user_id: i64) -> Result<User, String> {
    fetch_user_from_db(user_id).await.ok_or("Not found".to_string())
}

fn main() {
    // First call - computes
    let result1 = compute_expensive(5);
    
    // Second call - returns cached
    let result2 = compute_expensive(5);
    
    println!("Results: {}, {}", result1, result2);
}
```

### Manual Cache Control

```rust
use cached::SizedCache;
use std::sync::Mutex;

lazy_static::lazy_static! {
    static ref USER_CACHE: Mutex<SizedCache<i64, User>> = 
        Mutex::new(SizedCache::with_size(1000));
}

fn get_cached_user(user_id: i64) -> Option<User> {
    let mut cache = USER_CACHE.lock().unwrap();
    cache.cache_get(&user_id).cloned()
}

fn set_cached_user(user_id: i64, user: User) {
    let mut cache = USER_CACHE.lock().unwrap();
    cache.cache_set(user_id, user);
}

fn invalidate_user(user_id: i64) {
    let mut cache = USER_CACHE.lock().unwrap();
    cache.cache_remove(&user_id);
}
```

---

## 3. **quick_cache** (Fastest Performance)

### Cargo.toml

```toml
[dependencies]
quick_cache = "0.4"
```

### Usage

```rust
use quick_cache::sync::Cache;

fn main() {
    // Create cache
    let cache: Cache<String, String> = Cache::new(10_000);

    // Insert
    cache.insert("key".to_string(), "value".to_string());

    // Get
    if let Some(value) = cache.get(&"key".to_string()) {
        println!("Found: {}", value);
    }

    // Get or insert
    let value = cache.get_or_insert_with(&"key2".to_string(), || {
        "computed".to_string()
    });
}
```

---

## 4. **Feature Comparison**

| Feature            | Moka   | Cached | quick_cache |
|--------------------|--------|--------|-------------|
| TTL (Time-to-Live) | ✅      | ✅      | ❌           |
| TTI (Time-to-Idle) | ✅      | ❌      | ❌           |
| Max Size/Weight    | ✅      | ✅      | ✅           |
| Async Support      | ✅      | ✅      | ✅           |
| Eviction Listener  | ✅      | ❌      | ❌           |
| Entry-level Expiry | ✅      | ❌      | ❌           |
| Macro Support      | ❌      | ✅      | ❌           |
| Performance        | High   | Medium | Highest     |
| API Complexity     | Medium | Low    | Low         |

---

## 5. **Caffeine vs Moka Feature Mapping**

| Caffeine (Java)          | Moka (Rust)                   |
|--------------------------|-------------------------------|
| `Caffeine.newBuilder()`  | `Cache::builder()`            |
| `.maximumSize(n)`        | `.max_capacity(n)`            |
| `.expireAfterWrite(d)`   | `.time_to_live(d)`            |
| `.expireAfterAccess(d)`  | `.time_to_idle(d)`            |
| `.weigher(fn)`           | `.weigher(fn)`                |
| `.removalListener(fn)`   | `.eviction_listener(fn)`      |
| `.build()`               | `.build()`                    |
| `cache.get(key, loader)` | `cache.get_with(key, loader)` |
| `cache.put(key, value)`  | `cache.insert(key, value)`    |
| `cache.invalidate(key)`  | `cache.invalidate(key)`       |
| `cache.invalidateAll()`  | `cache.invalidate_all()`      |

---

## Final Recommendation

| Scenario                        | Recommendation            |
|---------------------------------|---------------------------|
| **Feature-rich, Caffeine-like** | **Moka** ⭐                |
| **Simple, macro-based**         | **Cached**                |
| **Maximum performance**         | **quick_cache**           |
| **Distributed cache**           | Redis (separate solution) |

**For most cases, use Moka** - it's the closest Rust equivalent to Caffeine with similar features and excellent
performance.

Would you like me to show integration with a specific Rust web framework (Actix, Axum, Rocket) or distributed caching
with Redis?