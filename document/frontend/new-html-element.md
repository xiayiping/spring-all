# new elements

Here are **modern HTML elements introduced since 2005**, organized by HTML5 specification waves and purpose:

## HTML5 (2008-2014) - Major Revolution

### Semantic Structure Elements
```html
<header>     <!-- Page/section header (2008) -->
<nav>        <!-- Navigation links (2008) -->
<main>       <!-- Main content of document (2013) -->
<article>    <!-- Self-contained content (2008) -->
<section>    <!-- Thematic grouping of content (2008) -->
<aside>      <!-- Sidebar/tangential content (2008) -->
<footer>     <!-- Page/section footer (2008) -->
<figure>     <!-- Self-contained illustration/diagram (2008) -->
<figcaption> <!-- Caption for <figure> (2008) -->
```

**Example:**
```html
<!DOCTYPE html>
<html>
<body>
    <header>
        <h1>My Blog</h1>
        <nav>
            <a href="#home">Home</a>
            <a href="#about">About</a>
        </nav>
    </header>
    
    <main>
        <article>
            <h2>Article Title</h2>
            <p>Article content...</p>
            <figure>
                <img src="chart.png" alt="Sales Chart">
                <figcaption>Figure 1: Sales Growth</figcaption>
            </figure>
        </article>
        
        <aside>
            <h3>Related Links</h3>
            <ul><li>Link 1</li></ul>
        </aside>
    </main>
    
    <footer>
        <p>&copy; 2026 My Blog</p>
    </footer>
</body>
</html>
```

---

### Multimedia Elements
```html
<video>   <!-- Embed video (2008) -->
<audio>   <!-- Embed audio (2008) -->
<source>  <!-- Multiple media sources (2008) -->
<track>   <!-- Text tracks for video/audio (2010) -->
<embed>   <!-- External content (2008, modernized) -->
```

**Example:**
```html
<!-- Video with multiple sources and subtitles -->
<video controls width="640" height="360" poster="thumbnail.jpg">
    <source src="movie.mp4" type="video/mp4">
    <source src="movie.webm" type="video/webm">
    <track src="subtitles_en.vtt" kind="subtitles" srclang="en" label="English">
    <track src="subtitles_es.vtt" kind="subtitles" srclang="es" label="Spanish">
    Your browser doesn't support video.
</video>

<!-- Audio with fallback -->
<audio controls>
    <source src="song.mp3" type="audio/mpeg">
    <source src="song.ogg" type="audio/ogg">
    Your browser doesn't support audio.
</audio>
```

---

### Interactive/Form Elements
```html
<details>  <!-- Disclosure widget (expandable) (2011) -->
<summary>  <!-- Summary/caption for <details> (2011) -->
<dialog>   <!-- Modal dialog box (2013, widely supported 2022) -->
<datalist> <!-- Predefined options for input (2008) -->
<output>   <!-- Result of calculation (2008) -->
<progress> <!-- Progress indicator (2008) -->
<meter>    <!-- Scalar measurement (2008) -->
```

**Example:**
```html
<!-- Expandable FAQ -->
<details>
    <summary>What is HTML5?</summary>
    <p>HTML5 is the latest version of HTML with new semantic elements and APIs.</p>
</details>

<!-- Modal Dialog -->
<dialog id="myDialog">
    <h2>Confirm Action</h2>
    <p>Are you sure you want to delete this item?</p>
    <button onclick="document.getElementById('myDialog').close()">Cancel</button>
    <button>Delete</button>
</dialog>
<button onclick="document.getElementById('myDialog').showModal()">Open Dialog</button>

<!-- Autocomplete Input -->
<label for="browser">Choose a browser:</label>
<input list="browsers" id="browser" name="browser">
<datalist id="browsers">
    <option value="Chrome">
    <option value="Firefox">
    <option value="Safari">
    <option value="Edge">
</datalist>

<!-- Progress Bar -->
<label for="file">Download progress:</label>
<progress id="file" value="70" max="100">70%</progress>

<!-- Meter (gauge) -->
<label for="fuel">Fuel level:</label>
<meter id="fuel" min="0" max="100" low="33" high="66" optimum="80" value="50">
    50 liters
</meter>

<!-- Output of Calculation -->
<form oninput="result.value=parseInt(a.value)+parseInt(b.value)">
    <input type="number" id="a" value="50"> +
    <input type="number" id="b" value="50"> =
    <output name="result" for="a b">100</output>
</form>
```

---

### Graphics & Canvas
```html
<canvas> <!-- Scriptable graphics (2008) -->
<svg>    <!-- Scalable Vector Graphics (2008, native support) -->
```

**Example:**
```html
<!-- Canvas for drawing -->
<canvas id="myCanvas" width="400" height="200"></canvas>
<script>
    const canvas = document.getElementById('myCanvas');
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = '#3b82f6';
    ctx.fillRect(50, 50, 150, 100);
    ctx.fillStyle = 'white';
    ctx.font = '20px Arial';
    ctx.fillText('Hello Canvas!', 70, 105);
</script>

<!-- Inline SVG -->
<svg width="200" height="200">
    <circle cx="100" cy="100" r="80" fill="#667eea" />
    <text x="100" y="110" text-anchor="middle" fill="white" font-size="20">
        SVG Circle
    </text>
</svg>
```

---

### Text-Level Semantics
```html
<mark>   <!-- Highlighted/marked text (2008) -->
<time>   <!-- Machine-readable date/time (2009) -->
<wbr>    <!-- Word break opportunity (2008) -->
<ruby>   <!-- Ruby annotations (Asian typography) (2008) -->
<rt>     <!-- Ruby text (pronunciation) (2008) -->
<rp>     <!-- Ruby parentheses (fallback) (2008) -->
<bdi>    <!-- Bi-directional isolation (2011) -->
<data>   <!-- Machine-readable data (2013) -->
```

**Example:**
```html
<!-- Highlighted search results -->
<p>Search results for <mark>HTML5</mark></p>

<!-- Dates and times -->
<p>Published on <time datetime="2026-01-23">January 23, 2026</time></p>
<p>Event starts at <time datetime="2026-01-23T14:00">2:00 PM</time></p>

<!-- Long URLs with break opportunities -->
<p>Visit: https://www.example<wbr>.com/very<wbr>/long<wbr>/path<wbr>/to<wbr>/page</p>

<!-- Ruby annotations (for Japanese/Chinese) -->
<ruby>
    漢 <rp>(</rp><rt>kan</rt><rp>)</rp>
    字 <rp>(</rp><rt>ji</rt><rp>)</rp>
</ruby>

<!-- Bi-directional text isolation -->
<p>User <bdi>إيان</bdi> posted a comment.</p>

<!-- Machine-readable data -->
<data value="12345">Product SKU: 12345</data>
```

---

### Form Input Types (New)
Not elements, but new `type` attributes for `<input>`:

```html
<input type="email">         <!-- Email validation (2008) -->
<input type="url">           <!-- URL validation (2008) -->
<input type="tel">           <!-- Telephone number (2008) -->
<input type="number">        <!-- Numeric input with spinners (2008) -->
<input type="range">         <!-- Slider control (2008) -->
<input type="date">          <!-- Date picker (2008) -->
<input type="time">          <!-- Time picker (2008) -->
<input type="datetime-local"><!-- Date and time picker (2008) -->
<input type="month">         <!-- Month picker (2008) -->
<input type="week">          <!-- Week picker (2008) -->
<input type="color">         <!-- Color picker (2008) -->
<input type="search">        <!-- Search field (2008) -->
```

**Example:**
```html
<form>
    <label>Email: <input type="email" required></label><br>
    <label>Website: <input type="url" placeholder="https://"></label><br>
    <label>Phone: <input type="tel" pattern="[0-9]{3}-[0-9]{3}-[0-9]{4}"></label><br>
    <label>Age: <input type="number" min="1" max="120"></label><br>
    <label>Volume: <input type="range" min="0" max="100" value="50"></label><br>
    <label>Birthday: <input type="date"></label><br>
    <label>Appointment: <input type="datetime-local"></label><br>
    <label>Favorite Color: <input type="color" value="#3b82f6"></label><br>
    <label>Search: <input type="search" placeholder="Search..."></label><br>
    <button type="submit">Submit</button>
</form>
```

---

## HTML 5.1 - 5.3 (2016-2021)

### New Elements
```html
<picture>  <!-- Responsive images with multiple sources (2016) -->
<template> <!-- Reusable HTML template (2013, standardized 2016) -->
<slot>     <!-- Web Components slot (2016) -->
```

**Example:**
```html
<!-- Responsive images - different images for different screens -->
<picture>
    <source media="(min-width: 1200px)" srcset="large.jpg">
    <source media="(min-width: 768px)" srcset="medium.jpg">
    <source media="(min-width: 320px)" srcset="small.jpg">
    <img src="fallback.jpg" alt="Responsive Image">
</picture>

<!-- Template for reusable content -->
<template id="productTemplate">
    <div class="product">
        <h3 class="product-name"></h3>
        <p class="product-price"></p>
        <button>Add to Cart</button>
    </div>
</template>

<script>
    const template = document.getElementById('productTemplate');
    const clone = template.content.cloneNode(true);
    clone.querySelector('.product-name').textContent = 'Widget';
    clone.querySelector('.product-price').textContent = '$19.99';
    document.body.appendChild(clone);
</script>

<!-- Web Component with slot -->
<template id="myCard">
    <div class="card">
        <h2><slot name="title">Default Title</slot></h2>
        <div><slot>Default content</slot></div>
    </div>
</template>
```

---

## HTML Living Standard (2019-2026)

### Recent Additions
```html
<search>   <!-- Search functionality wrapper (2023) -->
```

**Example:**
```html
<!-- Search section (new semantic element) -->
<search>
    <form action="/search" method="get">
        <label for="q">Search:</label>
        <input type="search" id="q" name="q" placeholder="Search our site...">
        <button type="submit">Go</button>
    </form>
</search>
```

---

## Complete Timeline

| Year | Elements Introduced |
|------|---------------------|
| **2008** | `<header>`, `<nav>`, `<article>`, `<section>`, `<aside>`, `<footer>`, `<figure>`, `<figcaption>`, `<video>`, `<audio>`, `<source>`, `<canvas>`, `<svg>`, `<mark>`, `<time>`, `<wbr>`, `<ruby>`, `<rt>`, `<rp>`, `<datalist>`, `<output>`, `<progress>`, `<meter>`, input types |
| **2009** | `<time>` (refined) |
| **2010** | `<track>` |
| **2011** | `<details>`, `<summary>`, `<bdi>` |
| **2013** | `<main>`, `<dialog>`, `<data>`, `<template>` |
| **2016** | `<picture>`, `<slot>` (Web Components) |
| **2023** | `<search>` |

---

## Deprecated/Removed Elements (Don't Use These!)

Elements **removed** since 2005:
```html
<!-- ❌ DEPRECATED - Don't use! -->
<acronym>   <!-- Use <abbr> instead -->
<applet>    <!-- Use <object> or <embed> -->
<basefont>  <!-- Use CSS -->
<big>       <!-- Use CSS -->
<blink>     <!-- Use CSS animations -->
<center>    <!-- Use CSS text-align -->
<dir>       <!-- Use <ul> -->
<font>      <!-- Use CSS -->
<frame>     <!-- Use <iframe> or modern layouts -->
<frameset>  <!-- Use <iframe> or modern layouts -->
<marquee>   <!-- Use CSS animations -->
<strike>    <!-- Use <del> or <s> -->
<tt>        <!-- Use <code> or CSS -->
```

---

## Most Useful Modern Elements

### Must-Know Essentials ⭐
```html
<header>, <nav>, <main>, <article>, <section>, <aside>, <footer>  <!-- Semantic structure -->
<video>, <audio>                                                    <!-- Multimedia -->
<canvas>                                                            <!-- Graphics -->
<details>, <summary>                                                <!-- Expandable content -->
<dialog>                                                            <!-- Modal dialogs -->
<progress>, <meter>                                                 <!-- Visual indicators -->
<picture>                                                           <!-- Responsive images -->
<template>                                                          <!-- Reusable content -->
```

### Input Types ⭐
```html
<input type="email">
<input type="date">
<input type="number">
<input type="range">
<input type="color">
```

---

## Full Modern HTML5 Example

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Modern HTML5 Demo</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 1200px; margin: 0 auto; padding: 20px; }
        header, footer { background: #3b82f6; color: white; padding: 20px; }
        nav a { color: white; margin: 0 10px; text-decoration: none; }
        main { display: grid; grid-template-columns: 3fr 1fr; gap: 20px; margin: 20px 0; }
        article { background: #f9fafb; padding: 20px; border-radius: 8px; }
        aside { background: #e5e7eb; padding: 20px; border-radius: 8px; }
        figure { margin: 20px 0; }
        figcaption { font-style: italic; color: #6b7280; }
        details { margin: 10px 0; }
        summary { cursor: pointer; font-weight: bold; }
        dialog { padding: 20px; border: none; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
        dialog::backdrop { background: rgba(0,0,0,0.5); }
    </style>
</head>
<body>
    <!-- Header with navigation -->
    <header>
        <h1>Modern HTML5 Website</h1>
        <nav>
            <a href="#home">Home</a>
            <a href="#about">About</a>
            <a href="#contact">Contact</a>
        </nav>
    </header>

    <!-- Main content area -->
    <main>
        <!-- Article section -->
        <article>
            <h2>Introduction to HTML5</h2>
            <p>Published on <time datetime="2026-01-23">January 23, 2026</time></p>
            
            <p>HTML5 introduced many <mark>important semantic elements</mark> that help structure web pages better.</p>

            <!-- Video -->
            <video controls width="100%">
                <source src="demo.mp4" type="video/mp4">
                Your browser doesn't support video.
            </video>

            <!-- Figure with caption -->
            <figure>
                <canvas id="chart" width="400" height="200"></canvas>
                <figcaption>Figure 1: Sample Chart</figcaption>
            </figure>

            <!-- Details/Summary (FAQ) -->
            <details>
                <summary>What's new in HTML5?</summary>
                <p>Semantic elements, multimedia support, canvas, and more!</p>
            </details>

            <details>
                <summary>Is HTML5 widely supported?</summary>
                <p>Yes! All modern browsers support HTML5.</p>
            </details>

            <!-- Form with modern inputs -->
            <h3>Contact Form</h3>
            <form>
                <label>Email: <input type="email" required></label><br><br>
                <label>Date: <input type="date"></label><br><br>
                <label>Rating: <input type="range" min="1" max="5" value="3"></label><br><br>
                <label>Color: <input type="color" value="#3b82f6"></label><br><br>
                <button type="button" onclick="document.getElementById('confirmDialog').showModal()">
                    Submit
                </button>
            </form>

            <!-- Progress indicator -->
            <p>Upload Progress:</p>
            <progress value="75" max="100">75%</progress>
        </article>

        <!-- Sidebar -->
        <aside>
            <h3>Related Articles</h3>
            <ul>
                <li><a href="#">CSS Grid Layout</a></li>
                <li><a href="#">JavaScript ES6</a></li>
                <li><a href="#">Web Components</a></li>
            </ul>

            <!-- Meter -->
            <h3>Storage Usage</h3>
            <meter value="60" min="0" max="100" low="30" high="80" optimum="20">
                60% used
            </meter>
        </aside>
    </main>

    <!-- Footer -->
    <footer>
        <p>&copy; 2026 Modern HTML5 Demo. All rights reserved.</p>
    </footer>

    <!-- Dialog (modal) -->
    <dialog id="confirmDialog">
        <h2>Form Submitted!</h2>
        <p>Thank you for your submission.</p>
        <button onclick="this.closest('dialog').close()">Close</button>
    </dialog>

    <!-- Template for reusable content -->
    <template id="commentTemplate">
        <div class="comment">
            <strong class="author"></strong>
            <p class="text"></p>
            <time class="date"></time>
        </div>
    </template>

    <script>
        // Canvas example
        const canvas = document.getElementById('chart');
        const ctx = canvas.getContext('2d');
        ctx.fillStyle = '#3b82f6';
        ctx.fillRect(50, 50, 100, 80);
        ctx.fillStyle = '#10b981';
        ctx.fillRect(180, 30, 100, 100);
        ctx.fillStyle = '#f59e0b';
        ctx.fillRect(310, 70, 100, 60);
    </script>
</body>
</html>
```

The biggest changes were in **2008-2014 (HTML5)** with semantic elements, multimedia, and form improvements. Since then, additions have been more incremental!