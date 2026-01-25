# Naming

Great question! Here are the best practices for organizing and naming CSS without frameworks:

## 1. BEM (Block Element Modifier) - Most Popular

BEM is a naming convention that makes CSS scalable and maintainable.

**Structure**: `.block__element--modifier`

```css
/* Block: standalone component */
.card { }

/* Element: part of a block */
.card__header { }
.card__body { }
.card__footer { }
.card__title { }
.card__image { }

/* Modifier: variation of block or element */
.card--featured { }
.card--large { }
.card__title--highlighted { }

/* Example usage */
.button { 
    padding: 10px 20px;
    border: none;
    border-radius: 4px;
}

.button--primary {
    background: blue;
    color: white;
}

.button--secondary {
    background: gray;
    color: white;
}

.button--large {
    padding: 15px 30px;
    font-size: 18px;
}

.button__icon {
    margin-right: 8px;
}
```

**HTML Example:**
```html
<div class="card card--featured">
    <div class="card__header">
        <h2 class="card__title card__title--highlighted">Title</h2>
    </div>
    <div class="card__body">Content</div>
</div>

<button class="button button--primary button--large">
    <span class="button__icon">→</span>
    Click Me
</button>
```

## 2. File Organization Strategies

### Option A: Component-Based Structure
```
css/
├── base/
│   ├── reset.css          /* CSS reset or normalize */
│   ├── typography.css     /* Font definitions, headings */
│   └── variables.css      /* CSS custom properties */
├── layout/
│   ├── header.css
│   ├── footer.css
│   ├── sidebar.css
│   └── grid.css
├── components/
│   ├── button.css
│   ├── card.css
│   ├── modal.css
│   ├── navbar.css
│   └── form.css
├── utilities/
│   ├── spacing.css        /* Margins, paddings */
│   ├── colors.css         /* Color utilities */
│   └── helpers.css        /* Display, position utilities */
└── pages/
    ├── home.css
    └── about.css
```

### Option B: SMACSS (Scalable and Modular Architecture)
```
css/
├── base.css          /* Element defaults: body, h1, a */
├── layout.css        /* Major page sections: header, main, footer */
├── modules.css       /* Reusable components: buttons, cards */
├── state.css         /* State changes: is-active, is-hidden */
└── theme.css         /* Colors, fonts (optional) */
```

### Option C: ITCSS (Inverted Triangle CSS)
```
css/
├── 1-settings/       /* Variables, config */
├── 2-tools/          /* Mixins, functions */
├── 3-generic/        /* Reset, normalize */
├── 4-elements/       /* Bare HTML elements */
├── 5-objects/        /* Layout patterns */
├── 6-components/     /* UI components */
└── 7-utilities/      /* Helper classes */
```

## 3. Complete Example with Best Practices

**variables.css** (or at top of main.css):
```css
:root {
    /* Colors */
    --color-primary: #3b82f6;
    --color-secondary: #8b5cf6;
    --color-success: #10b981;
    --color-danger: #ef4444;
    --color-warning: #f59e0b;
    
    --color-text: #1f2937;
    --color-text-light: #6b7280;
    --color-bg: #ffffff;
    --color-bg-alt: #f3f4f6;
    
    /* Spacing */
    --spacing-xs: 0.25rem;
    --spacing-sm: 0.5rem;
    --spacing-md: 1rem;
    --spacing-lg: 1.5rem;
    --spacing-xl: 2rem;
    --spacing-2xl: 3rem;
    
    /* Typography */
    --font-family-base: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
    --font-family-heading: Georgia, serif;
    
    --font-size-sm: 0.875rem;
    --font-size-base: 1rem;
    --font-size-lg: 1.125rem;
    --font-size-xl: 1.25rem;
    --font-size-2xl: 1.5rem;
    --font-size-3xl: 2rem;
    
    /* Shadows */
    --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
    --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1);
    --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);
    
    /* Border radius */
    --radius-sm: 0.25rem;
    --radius-md: 0.5rem;
    --radius-lg: 1rem;
    --radius-full: 9999px;
    
    /* Transitions */
    --transition-fast: 150ms ease-in-out;
    --transition-base: 300ms ease-in-out;
    --transition-slow: 500ms ease-in-out;
}
```

**base.css**:
```css
/* CSS Reset */
*, *::before, *::after {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    font-family: var(--font-family-base);
    font-size: var(--font-size-base);
    line-height: 1.6;
    color: var(--color-text);
    background-color: var(--color-bg);
}

h1, h2, h3, h4, h5, h6 {
    font-family: var(--font-family-heading);
    line-height: 1.2;
    margin-bottom: var(--spacing-md);
}

h1 { font-size: var(--font-size-3xl); }
h2 { font-size: var(--font-size-2xl); }
h3 { font-size: var(--font-size-xl); }

a {
    color: var(--color-primary);
    text-decoration: none;
    transition: color var(--transition-fast);
}

a:hover {
    color: var(--color-secondary);
}

img {
    max-width: 100%;
    height: auto;
    display: block;
}
```

**layout.css**:
```css
/* Container */
.l-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 var(--spacing-lg);
}

.l-container--narrow {
    max-width: 800px;
}

.l-container--wide {
    max-width: 1400px;
}

/* Grid */
.l-grid {
    display: grid;
    gap: var(--spacing-lg);
}

.l-grid--2col {
    grid-template-columns: repeat(2, 1fr);
}

.l-grid--3col {
    grid-template-columns: repeat(3, 1fr);
}

.l-grid--4col {
    grid-template-columns: repeat(4, 1fr);
}

/* Flex utilities */
.l-flex {
    display: flex;
}

.l-flex--center {
    justify-content: center;
    align-items: center;
}

.l-flex--between {
    justify-content: space-between;
    align-items: center;
}

.l-flex--column {
    flex-direction: column;
}

/* Header & Footer */
.l-header {
    background: var(--color-bg);
    border-bottom: 1px solid var(--color-bg-alt);
    padding: var(--spacing-lg) 0;
}

.l-footer {
    background: var(--color-bg-alt);
    padding: var(--spacing-2xl) 0;
    margin-top: var(--spacing-2xl);
}

.l-main {
    min-height: calc(100vh - 200px);
    padding: var(--spacing-2xl) 0;
}
```

**components/button.css**:
```css
/* Base button */
.btn {
    display: inline-block;
    padding: var(--spacing-sm) var(--spacing-lg);
    font-size: var(--font-size-base);
    font-weight: 500;
    text-align: center;
    border: none;
    border-radius: var(--radius-md);
    cursor: pointer;
    transition: all var(--transition-fast);
    font-family: inherit;
}

.btn:hover {
    transform: translateY(-1px);
    box-shadow: var(--shadow-md);
}

.btn:active {
    transform: translateY(0);
}

.btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

/* Button variants */
.btn--primary {
    background: var(--color-primary);
    color: white;
}

.btn--primary:hover {
    background: #2563eb;
}

.btn--secondary {
    background: var(--color-secondary);
    color: white;
}

.btn--outline {
    background: transparent;
    border: 2px solid var(--color-primary);
    color: var(--color-primary);
}

.btn--outline:hover {
    background: var(--color-primary);
    color: white;
}

/* Button sizes */
.btn--small {
    padding: var(--spacing-xs) var(--spacing-md);
    font-size: var(--font-size-sm);
}

.btn--large {
    padding: var(--spacing-md) var(--spacing-xl);
    font-size: var(--font-size-lg);
}

/* Button with icon */
.btn__icon {
    margin-right: var(--spacing-sm);
}

.btn__icon--right {
    margin-right: 0;
    margin-left: var(--spacing-sm);
}
```

**components/card.css**:
```css
.card {
    background: var(--color-bg);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-md);
    overflow: hidden;
    transition: box-shadow var(--transition-base);
}

.card:hover {
    box-shadow: var(--shadow-lg);
}

.card__image {
    width: 100%;
    height: 200px;
    object-fit: cover;
}

.card__header {
    padding: var(--spacing-lg);
    border-bottom: 1px solid var(--color-bg-alt);
}

.card__title {
    font-size: var(--font-size-xl);
    margin: 0;
}

.card__body {
    padding: var(--spacing-lg);
}

.card__footer {
    padding: var(--spacing-lg);
    border-top: 1px solid var(--color-bg-alt);
    background: var(--color-bg-alt);
}

/* Card variants */
.card--featured {
    border: 2px solid var(--color-primary);
}

.card--horizontal {
    display: flex;
}

.card--horizontal .card__image {
    width: 40%;
    height: auto;
}
```

**utilities.css**:
```css
/* Spacing utilities */
.u-mt-0 { margin-top: 0; }
.u-mt-sm { margin-top: var(--spacing-sm); }
.u-mt-md { margin-top: var(--spacing-md); }
.u-mt-lg { margin-top: var(--spacing-lg); }
.u-mt-xl { margin-top: var(--spacing-xl); }

.u-mb-0 { margin-bottom: 0; }
.u-mb-sm { margin-bottom: var(--spacing-sm); }
.u-mb-md { margin-bottom: var(--spacing-md); }
.u-mb-lg { margin-bottom: var(--spacing-lg); }
.u-mb-xl { margin-bottom: var(--spacing-xl); }

/* Display utilities */
.u-block { display: block; }
.u-inline { display: inline; }
.u-inline-block { display: inline-block; }
.u-flex { display: flex; }
.u-grid { display: grid; }
.u-hidden { display: none; }

/* Text utilities */
.u-text-center { text-align: center; }
.u-text-left { text-align: left; }
.u-text-right { text-align: right; }

.u-text-bold { font-weight: bold; }
.u-text-normal { font-weight: normal; }

.u-text-sm { font-size: var(--font-size-sm); }
.u-text-lg { font-size: var(--font-size-lg); }
.u-text-xl { font-size: var(--font-size-xl); }

/* Color utilities */
.u-text-primary { color: var(--color-primary); }
.u-text-muted { color: var(--color-text-light); }
.u-bg-primary { background: var(--color-primary); }
.u-bg-alt { background: var(--color-bg-alt); }

/* State utilities */
.is-active { }
.is-disabled { opacity: 0.5; pointer-events: none; }
.is-hidden { display: none; }
.is-visible { display: block; }
.is-loading { cursor: wait; }
```

**main.css** (Import all):
```css
/* Import order matters! */
@import url('variables.css');
@import url('base.css');
@import url('layout.css');

@import url('components/button.css');
@import url('components/card.css');
@import url('components/modal.css');
@import url('components/navbar.css');

@import url('utilities.css');

@import url('pages/home.css');
```

## 4. HTML Usage Example

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <link rel="stylesheet" href="css/main.css">
</head>
<body>
    <header class="l-header">
        <div class="l-container l-flex--between">
            <h1>My Site</h1>
            <nav><!-- nav items --></nav>
        </div>
    </header>

    <main class="l-main">
        <div class="l-container">
            <div class="l-grid l-grid--3col">
                <div class="card card--featured">
                    <img src="image.jpg" alt="" class="card__image">
                    <div class="card__header">
                        <h3 class="card__title">Card Title</h3>
                    </div>
                    <div class="card__body">
                        <p>Card content goes here.</p>
                    </div>
                    <div class="card__footer">
                        <button class="btn btn--primary">
                            <span class="btn__icon">→</span>
                            Read More
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </main>

    <footer class="l-footer">
        <div class="l-container u-text-center">
            <p class="u-text-muted">© 2026 My Site</p>
        </div>
    </footer>
</body>
</html>
```

## 5. Naming Convention Summary

| Prefix           | Purpose         | Example                           |
|------------------|-----------------|-----------------------------------|
| `.l-`            | Layout          | `.l-container`, `.l-grid`         |
| `.c-` (optional) | Component       | `.c-card`, `.c-button`            |
| `.u-`            | Utility         | `.u-mt-lg`, `.u-text-center`      |
| `.is-`           | State           | `.is-active`, `.is-hidden`        |
| `.has-`          | Condition       | `.has-dropdown`, `.has-icon`      |
| `.js-`           | JavaScript hook | `.js-toggle` (don't style these!) |

**Key Principles:**
1. Use **BEM** for components
2. Use **prefixes** for different categories
3. Keep **specificity low** (avoid deep nesting)
4. Use **CSS variables** for theming
5. Organize files by **purpose**, not page
6. **Mobile-first** media queries
7. Comment your code!

This structure scales from small projects to large applications.


# Pseudo

Great question! The `:` indicates a **pseudo-class** in CSS. Let me explain:

## What is `:disabled`?

`:disabled` is a **pseudo-class selector** that targets elements with the `disabled` attribute.

### Pseudo-classes vs Regular Classes

```css
/* Regular class - you add this manually */
.btn { }

/* Pseudo-class - browser applies this automatically based on element state */
.btn:disabled { }
.btn:hover { }
.btn:focus { }
```

## How to Apply `disabled`

You add the `disabled` **attribute** to the HTML element:

```html
<!-- Method 1: disabled attribute (recommended) -->
<button class="btn" disabled>Can't Click Me</button>

<!-- Method 2: disabled="disabled" (also valid) -->
<button class="btn" disabled="disabled">Can't Click Me</button>

<!-- Method 3: disabled="true" (works but not standard) -->
<button class="btn" disabled="true">Can't Click Me</button>

<!-- Normal button (no disabled attribute) -->
<button class="btn">Click Me</button>
```

```css
/* This CSS automatically styles buttons with disabled attribute */
.btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    background-color: gray;
}
```

## Common Pseudo-Classes Reference

### User Interaction Pseudo-classes

```css
/* When mouse hovers over element */
a:hover {
    color: blue;
}

/* When element is clicked/pressed */
button:active {
    transform: scale(0.95);
}

/* When element has keyboard focus */
input:focus {
    border-color: blue;
    outline: 2px solid blue;
}

/* When element is disabled */
button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}
```

### Form Pseudo-classes

```css
/* When checkbox/radio is checked */
input:checked {
    background: green;
}

/* When form field is required */
input:required {
    border-left: 3px solid red;
}

/* When input has valid value */
input:valid {
    border-color: green;
}

/* When input has invalid value */
input:invalid {
    border-color: red;
}

/* When input is empty */
input:placeholder-shown {
    border-color: gray;
}
```

### Structural Pseudo-classes

```css
/* First child element */
li:first-child {
    font-weight: bold;
}

/* Last child element */
li:last-child {
    border-bottom: none;
}

/* Every odd row */
tr:nth-child(odd) {
    background: #f0f0f0;
}

/* Every even row */
tr:nth-child(even) {
    background: white;
}

/* Third element */
li:nth-child(3) {
    color: red;
}

/* Only child (no siblings) */
p:only-child {
    margin: 0;
}

/* Empty elements */
div:empty {
    display: none;
}
```

### Link Pseudo-classes

```css
/* Unvisited link */
a:link {
    color: blue;
}

/* Visited link */
a:visited {
    color: purple;
}

/* Link being clicked */
a:active {
    color: red;
}
```

## Complete Button Example with Disabled State

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <style>
        .btn {
            padding: 12px 24px;
            font-size: 16px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            background: #3b82f6;
            color: white;
            font-weight: 500;
            transition: all 0.2s;
        }

        /* When hovering (only works if NOT disabled) */
        .btn:hover {
            background: #2563eb;
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
        }

        /* When clicking */
        .btn:active {
            transform: translateY(0);
        }

        /* When disabled */
        .btn:disabled {
            background: #9ca3af;
            cursor: not-allowed;
            opacity: 0.6;
            transform: none;
            box-shadow: none;
        }

        /* Hover doesn't work on disabled buttons automatically! */
        .btn:disabled:hover {
            background: #9ca3af; /* Stays gray */
            transform: none;
        }
    </style>
</head>
<body>
    <h2>Button States</h2>
    
    <!-- Normal button -->
    <button class="btn">Normal Button</button>
    
    <!-- Disabled button -->
    <button class="btn" disabled>Disabled Button</button>
    
    <hr>
    
    <h2>Toggle Disabled with JavaScript</h2>
    <button class="btn" id="myButton">Click to Disable</button>
    
    <script>
        const btn = document.getElementById('myButton');
        
        btn.addEventListener('click', function() {
            // Toggle disabled state
            this.disabled = true;
            this.textContent = 'Button Disabled';
            
            // Re-enable after 3 seconds
            setTimeout(() => {
                this.disabled = false;
                this.textContent = 'Click to Disable';
            }, 3000);
        });
    </script>
</body>
</html>
```

## JavaScript Control of Disabled State

```javascript
const button = document.querySelector('.btn');

// Disable button
button.disabled = true;

// Enable button
button.disabled = false;

// Toggle disabled
button.disabled = !button.disabled;

// Check if disabled
if (button.disabled) {
    console.log('Button is disabled');
}

// Add disabled attribute
button.setAttribute('disabled', '');

// Remove disabled attribute
button.removeAttribute('disabled');
```

## Form Input Example

```html
<style>
    .input {
        padding: 10px;
        border: 2px solid #d1d5db;
        border-radius: 4px;
        font-size: 16px;
    }

    .input:focus {
        border-color: #3b82f6;
        outline: none;
    }

    .input:disabled {
        background: #f3f4f6;
        color: #6b7280;
        cursor: not-allowed;
    }

    .input:valid {
        border-color: #10b981;
    }

    .input:invalid {
        border-color: #ef4444;
    }
</style>

<!-- Normal input -->
<input type="email" class="input" placeholder="Enter email">

<!-- Disabled input -->
<input type="email" class="input" placeholder="Disabled" disabled>

<!-- Required input (will show :invalid when empty) -->
<input type="email" class="input" placeholder="Required email" required>
```

## Other Useful Pseudo-classes

```css
/* When element is NOT disabled */
button:not(:disabled) {
    cursor: pointer;
}

/* First element of its type */
p:first-of-type {
    font-size: 20px;
}

/* Last element of its type */
p:last-of-type {
    margin-bottom: 0;
}

/* Target element via URL fragment (#section1) */
:target {
    background: yellow;
}

/* Root element (usually <html>) */
:root {
    --main-color: blue;
}
```

## Summary

| Concept | Meaning |
|---------|---------|
| `.btn` | Class selector (you add class="btn") |
| `.btn:disabled` | Pseudo-class (browser adds when disabled attribute exists) |
| `disabled` | HTML attribute you add to element |
| `:hover`, `:focus`, `:active` | Other pseudo-classes for interaction states |

**Key Point:** You don't add pseudo-classes like `:disabled` manually - the browser applies them automatically based on the element's state or attributes!