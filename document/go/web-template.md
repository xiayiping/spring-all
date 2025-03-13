# Thymleaf

In the Go (Golang) ecosystem, there isn't a direct equivalent to Spring Boot combined with Thymeleaf for server-side rendering (SSR) web pages, but there are libraries and frameworks that can achieve similar functionality. Here's an overview:

---

### 1. **Gin + HTML Templates**
The [Gin](https://gin-gonic.com/) framework is one of the most popular web frameworks for Go. It provides functionality for rendering HTML templates using Go's `html/template` package (built into the standard library). While it's not as feature-rich as Thymeleaf, it allows you to write HTML templates and render them dynamically on the server.

**Example:**
```go
package main

import (
    "net/http"
    "github.com/gin-gonic/gin"
)

func main() {
    r := gin.Default()
    
    // Load HTML templates from the "templates" folder
    r.LoadHTMLGlob("templates/*")

    r.GET("/", func(c *gin.Context) {
        c.HTML(http.StatusOK, "index.html", gin.H{
            "title": "Hello, World!",
            "message": "This is server-side rendered content in Go!",
        })
    })

    r.Run() // Run on localhost:8080
}
```

Template file: `templates/index.html`
```html
<!DOCTYPE html>
<html>
<head>
    <title>{{ .title }}</title>
</head>
<body>
    <h1>{{ .message }}</h1>
</body>
</html>
```

This provides basic SSR capabilities, similar to Thymeleaf but without the additional features like form binding.

---

### 2. **Buffalo**
[Buffalo](https://gobuffalo.io/) is a full-stack Go framework that comes closer to Spring Boot in terms of functionality. It includes tools for web application development, including routing, ORM (via Pop), and HTML rendering.

Buffalo uses the [Plush](https://github.com/gobuffalo/plush) templating engine, which is more powerful than the standard library's `html/template`.

**Example:**

- Define a route:
```go
package actions

import "net/http"

func HomeHandler(c buffalo.Context) error {
    c.Set("message", "Welcome to Buffalo!")
    return c.Render(http.StatusOK, r.HTML("index.html"))
}
```

- Template file: `templates/index.html`
```html
<!DOCTYPE html>
<html>
<head>
    <title>Buffalo App</title>
</head>
<body>
    <h1><%= message %></h1>
</body>
</html>
```

Buffalo provides a more modern development experience but has a steeper learning curve than Gin.

---

### 3. **Echo + Templates**
[Echo](https://echo.labstack.com/) is another popular web framework in Go, similar to Gin but with slightly different design philosophies. Echo also supports HTML templates with the `html/template` package.

**Example:**
```go
package main

import (
    "html/template"
    "net/http"
    "github.com/labstack/echo/v4"
)

func main() {
    e := echo.New()

    // Define a custom renderer
    e.Renderer = &TemplateRenderer{
        templates: template.Must(template.ParseGlob("templates/*.html")),
    }

    e.GET("/", func(c echo.Context) error {
        return c.Render(http.StatusOK, "index.html", map[string]interface{}{
            "title": "Hello, Echo!",
            "message": "This is server-side rendered content in Echo!",
        })
    })

    e.Start(":8080")
}

// Custom template renderer
type TemplateRenderer struct {
    templates *template.Template
}

func (t *TemplateRenderer) Render(w http.ResponseWriter, name string, data interface{}, c echo.Context) error {
    return t.templates.ExecuteTemplate(w, name, data)
}
```

Template file: `templates/index.html`
```html
<!DOCTYPE html>
<html>
<head>
    <title>{{ .title }}</title>
</head>
<body>
    <h1>{{ .message }}</h1>
</body>
</html>
```

---

### 4. **Go Templating Engines**
If you want more advanced features comparable to Thymeleaf, you can explore third-party templating engines like:

- **Jet** ([github.com/CloudyKit/jet](https://github.com/CloudyKit/jet)): A fast and powerful templating engine for Go with a syntax similar to Django or Jinja.
- **Ace** ([github.com/yosssi/ace](https://github.com/yosssi/ace)): A lightweight templating language for Go, inspired by Jade/Pug.
- **Pongo2** ([github.com/flosch/pongo2](https://github.com/flosch/pongo2)): A Django-like templating language for Go.

---

### 5. **Fiber + HTML Templates**
[Fiber](https://gofiber.io/) is a lightweight, high-performance web framework inspired by Express.js. Fiber supports SSR with template engines like `html/template` or third-party engines.

Example using `html/template`:
```go
package main

import (
    "github.com/gofiber/fiber/v2"
    "github.com/gofiber/template/html"
)

func main() {
    // Load templates from the "views" folder
    engine := html.New("./views", ".html")

    app := fiber.New(fiber.Config{
        Views: engine,
    })

    app.Get("/", func(c *fiber.Ctx) error {
        return c.Render("index", fiber.Map{
            "Title":   "Hello, Fiber!",
            "Message": "This is server-side rendered content in Fiber!",
        })
    })

    app.Listen(":8080")
}
```

Template file: `views/index.html`
```html
<!DOCTYPE html>
<html>
<head>
    <title>{{ .Title }}</title>
</head>
<body>
    <h1>{{ .Message }}</h1>
</body>
</html>
```

---

### Conclusion
- If you want something minimal and lightweight, **Gin** or **Echo** with Go's `html/template` package is a good choice.
- If you're looking for more of a full-stack, Spring Boot-like framework, **Buffalo** is the closest match.
- For advanced templating features, consider libraries like **Jet**, **Pongo2**, or **Ace**.

# Choices

When evaluating Go web frameworks and libraries in terms of **user population** and **performance**, the following observations can be made:

---

### 1. **User Population**

#### **Most Popular Go Web Frameworks**
Popularity can be estimated based on GitHub stars, community activity, documentation, and adoption by developers. Here's a ranking of the most popular frameworks:

1. **Gin**
    - **Why it's popular:**
        - Gin is the most widely used Go web framework because of its simplicity, minimalism, and great performance.
        - It is often compared to frameworks like Express.js (Node.js) for its ease of use, and its API is intuitive and beginner-friendly.
        - It has extensive online resources, tutorials, and a large community.
    - **GitHub Stars (as of March 2025):** ~71k
    - **Use Cases:** REST APIs, microservices, and web applications.

2. **Echo**
    - **Why it's popular:**
        - Echo is also very popular, offering a feature set comparable to Gin, but with a slightly different design philosophy (e.g., middleware chaining).
        - Many developers prefer Echo for its flexibility and additional functionality, such as built-in data binding and validation.
    - **GitHub Stars:** ~27k
    - **Use Cases:** REST APIs, real-time applications, and microservices.

3. **Fiber**
    - **Why it's popular:**
        - Inspired by Express.js, Fiber is designed to be lightweight and fast, with a focus on performance and developer productivity.
        - It has been steadily gaining popularity in recent years due to its ease of use and performance benchmarks.
    - **GitHub Stars:** ~30k
    - **Use Cases:** APIs, server-side rendering (SSR), and lightweight applications.

4. **Buffalo**
    - **Why it's popular:**
        - Buffalo is a full-stack framework that includes tools for routing, database management, and templating, making it more comparable to Spring Boot.
        - However, its adoption is smaller compared to Gin, Echo, and Fiber because it is more opinionated and heavier.
    - **GitHub Stars:** ~7k
    - **Use Cases:** Full-stack web applications.

5. **Revel**
    - **Why it's popular:**
        - Revel was an early Go web framework, but it has lost momentum in recent years. Its opinionated design and lack of updates have contributed to its decline.
    - **GitHub Stars:** ~13k (but waning community support).
    - **Use Cases:** Older projects or legacy systems.

#### **Summary of Popularity**
- **Gin** leads in terms of user population and community size.
- **Echo** and **Fiber** are strong contenders in terms of popularity, with Fiber gaining momentum due to its performance focus.

---

### 2. **Performance**

#### **Performance Benchmarks**
Performance in Go web frameworks is typically measured using throughput (requests per second) and latency under load. Benchmarks can vary depending on the use case, but here is a general ranking based on performance:

1. **Fiber**
    - **Performance:**
        - Fiber is one of the fastest Go web frameworks because it is built on top of the [Fasthttp](https://github.com/valyala/fasthttp) library, which is significantly faster than Go's standard `net/http` package for certain use cases.
        - Fiber excels in handling high-concurrency workloads and has lower latency compared to other frameworks.
    - **Use Cases:** High-performance APIs, real-time applications, and microservices.

2. **Gin**
    - **Performance:**
        - Gin is built on Go's standard `net/http` package, making it very performant while maintaining simplicity. Although not as fast as Fiber, Gin offers an excellent trade-off between performance and usability.
    - **Use Cases:** APIs, web services, and microservices.

3. **Echo**
    - **Performance:**
        - Echo is also built on Go's `net/http` package, similar to Gin, but often performs slightly better in benchmarks because of optimizations in its middleware chain and router.
    - **Use Cases:** APIs, real-time applications, and web services.

4. **Buffalo**
    - **Performance:**
        - Buffalo is a full-stack framework, so it's heavier and slower compared to lightweight frameworks like Gin, Echo, and Fiber. It sacrifices some performance for features and developer productivity.
    - **Use Cases:** Full-stack web applications where performance is not the top priority.

5. **Revel**
    - **Performance:**
        - Revel is slower compared to modern frameworks like Gin and Fiber because it is older and less optimized for Go's concurrency model.
    - **Use Cases:** Legacy applications.

#### **Summary of Performance**
- **Fiber** is the fastest Go web framework, especially for high-concurrency scenarios.
- **Gin** and **Echo** offer a good balance of performance and usability.
- **Buffalo** is slower due to its full-stack nature but is acceptable for applications that don't require high performance.

---

### Combined Recommendations
If you're looking for:

1. **High User Population:**
    - **Gin** is the clear leader, followed by **Echo** and **Fiber**.

2. **High Performance:**
    - **Fiber** is the fastest framework, followed by **Gin** and **Echo**.

#### **Overall Recommendation**
- **Choose Gin** if you want a widely used, well-documented, and performant framework with a large community.
- **Choose Fiber** if performance is your top priority and you're building high-concurrency or real-time applications.
- **Choose Echo** if you prefer flexibility and features like data binding out of the box.
- **Choose Buffalo** if you want a full-stack framework similar to Spring Boot.

# FastHttp

The **`fasthttp`** library is faster than Go's standard `net/http` package because of several optimizations made specifically for high performance and low memory usage. While `net/http` is designed to be idiomatic, easy to use, and highly concurrent, `fasthttp` sacrifices some of the design simplicity and flexibility of `net/http` in exchange for raw speed and efficiency.

Here are the key reasons why `fasthttp` outperforms `net/http`:

---

### 1. **Minimized Memory Allocations**
- **`fasthttp`:**
    - Uses reusable objects and avoids frequent memory allocations. For example, it reuses request and response objects to reduce garbage collection (GC) pressure.
    - Implements custom memory management techniques for tasks like parsing headers, reducing the need to create new objects repeatedly.
    - This approach results in lower GC overhead and less frequent pauses, which improves performance under high load.

- **`net/http`:**
    - Relies heavily on Go’s default allocation mechanisms, which are simple and idiomatic but result in more frequent memory allocations.
    - Allocates new request and response objects for each HTTP transaction, increasing GC overhead.

---

### 2. **Custom HTTP Parsing**
- **`fasthttp`:**
    - Implements a highly optimized HTTP parser tailored for performance, based on raw byte slices (`[]byte`) instead of Go's higher-level abstractions like `string` or `map`.
    - Avoids using Go's standard `http.Header` (which is a map) and instead uses a custom header implementation with lower overhead.
    - This leads to faster request and response parsing, especially for large numbers of HTTP headers or cookies.

- **`net/http`:**
    - Uses a more general-purpose HTTP parser, which is flexible and easy to use but slightly slower because it prioritizes correctness and extensibility over raw performance.

---

### 3. **Reduced Abstraction Overhead**
- **`fasthttp`:**
    - Designed with fewer abstractions to reduce overhead. It focuses on raw speed and efficiency by exposing lower-level APIs (e.g., directly working with byte slices).
    - For example, `fasthttp` doesn't support HTTP/2 directly (although it can be used with HTTP/2 via a third-party library), which simplifies its internal architecture.

- **`net/http`:**
    - Includes abstractions that make it easier to use but come with some performance costs. For instance, it supports HTTP/1.x and HTTP/2 natively, which makes the implementation more complex and slightly slower for HTTP/1.x workloads.

---

### 4. **Optimized Concurrency**
- **`fasthttp`:**
    - Uses its own highly optimized concurrency model, which minimizes contention and locks when handling many simultaneous connections.
    - Implements custom worker pools and event loops to handle requests efficiently.

- **`net/http`:**
    - Relies on Go’s `goroutines` and built-in concurrency primitives, which are easy to use but slightly less optimized for high-concurrency scenarios.

---

### 5. **Focus on High-Concurrency Use Cases**
- **`fasthttp`:**
    - Was specifically designed for use cases where high performance and low latency are critical, such as high-traffic APIs, real-time applications, and microservices.
    - It can handle millions of requests per second with low system resource usage.

- **`net/http`:**
    - Is a more general-purpose package aimed at providing a robust and idiomatic HTTP implementation for a wide range of use cases.

---

### 6. **Streaming and File Transfers**
- **`fasthttp`:**
    - Optimized for tasks like file transfers, streaming large payloads, and processing massive amounts of requests efficiently.
    - Handles large payloads without unnecessary memory copying or allocations, which makes it a great choice for serving static content or handling large uploads.

- **`net/http`:**
    - Handles streaming and file transfers well but does not include the same level of low-level optimizations as `fasthttp`.

---

### 7. **Benchmarks**
Here are some typical benchmark results (numbers may vary depending on the use case):

| Metric                  | `fasthttp`          | `net/http`         |
|-------------------------|---------------------|---------------------|
| Requests Per Second     | Higher (~1.5-2x)   | Lower              |
| Latency Under Load      | Lower              | Higher             |
| Memory Usage            | Lower              | Higher             |
| GC Overhead             | Lower              | Higher             |

For example, in high-concurrency benchmarks (e.g., 100,000 concurrent connections), `fasthttp` can handle millions of requests per second with sub-millisecond latency, while `net/http` typically handles fewer requests with slightly higher latency.

---

### Trade-Offs of `fasthttp`
While `fasthttp` is faster, it comes with some trade-offs:

1. **Less Idiomatic:**
    - `fasthttp` deviates from Go’s standard `net/http` idioms, such as using `http.Request` and `http.Response`. Developers may need to learn new patterns and APIs.
    - If you need to integrate with libraries or frameworks that expect `net/http`-compatible code, you may need to write additional adapters.

2. **No Native HTTP/2 Support:**
    - `fasthttp` does not support HTTP/2 out of the box. You need third-party solutions (e.g., [fasthttp2](https://github.com/gramework/fasthttp2)) for HTTP/2, which increases complexity.

3. **Less Flexibility:**
    - `fasthttp` is more specialized and optimized for specific use cases, like high-concurrency APIs. For general-purpose web applications, `net/http` might be more suitable.

4. **Learning Curve:**
    - Developers familiar with `net/http` may find `fasthttp`’s lower-level API harder to use initially.

---

### When to Use `fasthttp`
- **Use `fasthttp`** if:
    - You are building APIs or services where performance and scalability are the top priorities.
    - Your application needs to handle millions of requests per second with low latency.
    - You are serving static files or performing high-concurrency operations.

- **Use `net/http`** if:
    - You need a more general-purpose HTTP server with broader compatibility.
    - Performance is not the most critical factor, and you prefer simplicity and maintainability.

---

### Conclusion
`fasthttp` is faster than `net/http` because it is specifically engineered for high-performance workloads, with optimizations for low memory usage, minimal GC overhead, and high concurrency. However, it sacrifices some flexibility, simplicity, and compatibility with Go’s ecosystem to achieve this performance.