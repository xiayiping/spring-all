# combine solution

**No, CSS does not support inheriting or referencing other classes directly.** You cannot do something like:

```css
/* ❌ This doesn't work - CSS has no inheritance between classes */
.class3 {
    extends: .class1, .class2;  /* Not valid CSS! */
}
```

## Solutions to Combine Styles

### Solution 1: **Multiple Classes in HTML** (Recommended)

Apply both classes to the same element:

```html
<style>
    .btn-base {
        padding: 10px 20px;
        border: none;
        border-radius: 4px;
        cursor: pointer;
    }

    .btn-primary {
        background: blue;
        color: white;
    }

    .btn-large {
        padding: 15px 30px;
        font-size: 18px;
    }
</style>

<!-- Use multiple classes on one element -->
<button class="btn-base btn-primary">Primary Button</button>
<button class="btn-base btn-primary btn-large">Large Primary Button</button>
```

This is the **standard CSS way** and most maintainable.

### Solution 2: **CSS Selector Grouping**

Group selectors that share the same styles:

```css
/* Both .class1 and .class3 get the same styles */
.class1,
.class3 {
    color: red;
    font-size: 16px;
}

/* Only .class1 gets this */
.class1 {
    background: blue;
}

/* Only .class3 gets this */
.class3 {
    background: green;
}
```

```html
<div class="class1">Blue background, red text</div>
<div class="class3">Green background, red text</div>
```

### Solution 3: **CSS Variables** (For Reusable Values)

Share values across classes using CSS custom properties:

```css
:root {
    --btn-padding: 10px 20px;
    --btn-radius: 4px;
    --color-primary: #3b82f6;
    --color-secondary: #8b5cf6;
}

.btn-primary {
    padding: var(--btn-padding);
    border-radius: var(--btn-radius);
    background: var(--color-primary);
    color: white;
}

.btn-secondary {
    padding: var(--btn-padding);
    border-radius: var(--btn-radius);
    background: var(--color-secondary);
    color: white;
}

/* Third class reuses the same variables */
.btn-custom {
    padding: var(--btn-padding);
    border-radius: var(--btn-radius);
    background: purple;
    color: white;
}
```

### Solution 4: **Cascade & Specificity**

Use more specific selectors to combine styles:

```css
.card {
    padding: 20px;
    border-radius: 8px;
    background: white;
}

.featured {
    border: 2px solid gold;
}

/* Combine both - requires both classes */
.card.featured {
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
    transform: scale(1.05);
}
```

```html
<!-- Gets only .card styles -->
<div class="card">Normal Card</div>

<!-- Gets .card + .featured + .card.featured styles -->
<div class="card featured">Featured Card</div>
```

## Complete Practical Example

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <style>
        /* CSS Variables for shared values */
        :root {
            --spacing-sm: 8px;
            --spacing-md: 16px;
            --spacing-lg: 24px;
            --radius: 6px;
            --transition: 0.3s ease;
        }

        /* Base button styles */
        .btn {
            display: inline-block;
            padding: var(--spacing-sm) var(--spacing-md);
            border: none;
            border-radius: var(--radius);
            cursor: pointer;
            font-size: 14px;
            font-weight: 500;
            transition: all var(--transition);
            text-align: center;
        }

        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
        }

        /* Color variants */
        .btn-primary {
            background: #3b82f6;
            color: white;
        }

        .btn-primary:hover {
            background: #2563eb;
        }

        .btn-success {
            background: #10b981;
            color: white;
        }

        .btn-danger {
            background: #ef4444;
            color: white;
        }

        /* Size variants */
        .btn-small {
            padding: 4px 12px;
            font-size: 12px;
        }

        .btn-large {
            padding: 12px 32px;
            font-size: 18px;
        }

        /* Style variants */
        .btn-outline {
            background: transparent;
            border: 2px solid currentColor;
        }

        .btn-outline.btn-primary {
            color: #3b82f6;
            border-color: #3b82f6;
        }

        .btn-outline.btn-primary:hover {
            background: #3b82f6;
            color: white;
        }

        /* Combination styles */
        .btn.btn-rounded {
            border-radius: 9999px;
        }

        .btn.btn-shadow {
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }

        /* Disabled state */
        .btn:disabled {
            opacity: 0.5;
            cursor: not-allowed;
            transform: none;
        }
    </style>
</head>
<body>
    <h2>Multiple Classes Approach</h2>
    
    <!-- Basic button -->
    <button class="btn btn-primary">Primary</button>
    
    <!-- Combine multiple classes -->
    <button class="btn btn-primary btn-large">Large Primary</button>
    
    <button class="btn btn-success btn-small">Small Success</button>
    
    <button class="btn btn-danger btn-rounded">Rounded Danger</button>
    
    <!-- Outline variant -->
    <button class="btn btn-outline btn-primary">Outline Primary</button>
    
    <!-- Multiple modifiers -->
    <button class="btn btn-primary btn-large btn-rounded btn-shadow">
        All Combined
    </button>
    
    <!-- Disabled -->
    <button class="btn btn-primary" disabled>Disabled</button>

    <hr>

    <h2>Card Example</h2>
    <style>
        .card {
            background: white;
            border-radius: var(--radius);
            padding: var(--spacing-lg);
            margin: var(--spacing-md);
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }

        .card-bordered {
            border: 2px solid #e5e7eb;
        }

        .card-featured {
            border-color: #3b82f6;
            background: #eff6ff;
        }

        /* Combining card + card-featured */
        .card.card-featured {
            box-shadow: 0 10px 20px rgba(59, 130, 246, 0.3);
        }

        .card-title {
            font-size: 20px;
            font-weight: bold;
            margin-bottom: var(--spacing-sm);
        }

        .card-content {
            color: #6b7280;
            line-height: 1.6;
        }
    </style>

    <!-- Normal card -->
    <div class="card">
        <h3 class="card-title">Normal Card</h3>
        <p class="card-content">This is a regular card.</p>
    </div>

    <!-- Bordered card -->
    <div class="card card-bordered">
        <h3 class="card-title">Bordered Card</h3>
        <p class="card-content">This card has a border.</p>
    </div>

    <!-- Featured card (combines multiple classes) -->
    <div class="card card-featured">
        <h3 class="card-title">Featured Card</h3>
        <p class="card-content">This card is featured with special styling.</p>
    </div>

    <hr>

    <h2>Dynamic Class Toggle</h2>
    <button class="btn btn-primary" id="toggleBtn">Toggle Size</button>

    <script>
        const btn = document.getElementById('toggleBtn');
        
        btn.addEventListener('click', function() {
            // Toggle the 'btn-large' class on/off
            this.classList.toggle('btn-large');
            
            // Check if class exists
            if (this.classList.contains('btn-large')) {
                this.textContent = 'I am Large!';
            } else {
                this.textContent = 'Toggle Size';
            }
        });
    </script>
</body>
</html>
```

## JavaScript Class Manipulation

```javascript
const element = document.querySelector('.my-element');

// Add a class
element.classList.add('new-class');

// Add multiple classes
element.classList.add('class1', 'class2', 'class3');

// Remove a class
element.classList.remove('old-class');

// Toggle a class (add if missing, remove if present)
element.classList.toggle('active');

// Replace a class
element.classList.replace('old-class', 'new-class');

// Check if class exists
if (element.classList.contains('active')) {
    console.log('Element is active');
}

// Get all classes as array
const classes = Array.from(element.classList);
```

## Comparison Table

| Approach | CSS Reference? | Use Case |
|----------|---------------|----------|
| **Multiple HTML classes** | ❌ No | Best for combining styles (recommended) |
| **CSS Selector Grouping** | ❌ No | Share styles across different classes |
| **CSS Variables** | ✅ Partial | Share values, not entire style blocks |
| **CSS Preprocessors (Sass)** | ✅ Yes | Can extend classes (requires build step) |

## If You Want True Inheritance: Use Sass/SCSS

If you need actual style inheritance, you'd need a CSS preprocessor:

```scss
// Sass/SCSS (not pure CSS - requires compilation)
.btn-base {
    padding: 10px 20px;
    border: none;
}

.btn-primary {
    @extend .btn-base;  // Inherits .btn-base styles
    background: blue;
    color: white;
}

.btn-large {
    @extend .btn-base;  // Also inherits .btn-base styles
    padding: 15px 30px;
    font-size: 18px;
}
```

This compiles to regular CSS with grouped selectors.

## **Recommendation**

For pure CSS without preprocessors, **use multiple classes in HTML**. It's:
- Simple
- Flexible
- Maintainable
- The standard approach
- No build tools needed

```html
<!-- This is the CSS way ✅ -->
<button class="btn btn-primary btn-large btn-shadow">Click Me</button>
```

