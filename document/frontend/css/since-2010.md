Here are the major CSS features and styles introduced since 2010:

## CSS3 Features (2010-2015)

**Flexbox (2012)**
- `display: flex`
- `flex-direction`, `flex-wrap`, `justify-content`, `align-items`
- Revolutionary for layouts, replacing float-based designs

**CSS Grid (2017)**
- `display: grid`
- `grid-template-columns`, `grid-template-rows`
- `grid-gap` (now `gap`)
- Two-dimensional layout system

**Transforms (2011)**
- `transform: rotate()`, `scale()`, `translate()`, `skew()`
- `transform-origin`
- 3D transforms: `rotateX()`, `rotateY()`, `perspective()`

**Transitions (2012)**
- `transition-property`, `transition-duration`, `transition-timing-function`
- Smooth property changes

**Animations (2012)**
- `@keyframes` rule
- `animation-name`, `animation-duration`, `animation-iteration-count`

**Border & Background Enhancements**
- `border-radius` (rounded corners)
- `box-shadow`
- `background-size: cover/contain`
- Multiple backgrounds
- `background-clip`, `background-origin`

**Media Queries (2012)**
- `@media (min-width: 768px)`
- Responsive design foundation

**Custom Properties/CSS Variables (2015)**
```css
:root {
    --primary-color: #3498db;
}
.element {
    color: var(--primary-color);
}
```

## Modern CSS (2016-2025)

**CSS Grid Level 2 (2020)**
- `subgrid`
- Nested grid layouts

**Container Queries (2023)**
```css
@container (min-width: 400px) {
    .card { flex-direction: row; }
}
```

**:has() Selector (2023)**
```css
/* Parent selector - revolutionary! */
article:has(img) { background: gray; }
```

**Cascade Layers (2022)**
```css
@layer reset, base, components, utilities;
```

**Logical Properties (2018-2020)**
- `margin-inline-start` instead of `margin-left`
- `padding-block-end` instead of `padding-bottom`
- Better for internationalization

**CSS Nesting (2023)**
```css
.parent {
    color: blue;
    
    & .child {
        color: red;
    }
}
```

**Scroll-Driven Animations (2023)**
- `animation-timeline: scroll()`
- `animation-range`
- Animations triggered by scrolling

**color-mix() and Color Spaces (2023)**
```css
color: color-mix(in srgb, red 50%, blue);
color: oklch(60% 0.15 180);
```

**aspect-ratio (2021)**
```css
.box {
    aspect-ratio: 16 / 9;
}
```

**gap for Flexbox (2021)**
- Previously only for Grid
- Now works with Flexbox too

**Scroll Snap (2019)**
```css
scroll-snap-type: y mandatory;
scroll-snap-align: start;
```

**backdrop-filter (2019)**
```css
backdrop-filter: blur(10px);
```

**clamp(), min(), max() Functions (2020)**
```css
font-size: clamp(1rem, 2.5vw, 2rem);
```

**CSS Houdini APIs (2018-2020)**
- `@property` for custom properties with types
- Paint API, Layout API

**overscroll-behavior (2018)**
- Control scroll chaining behavior

**Position: sticky (2017)**
- Hybrid of relative and fixed positioning

**object-fit and object-position (2015)**
```css
img {
    object-fit: cover;
}
```

**Filter Effects (2013)**
```css
filter: blur(5px) brightness(1.2) contrast(1.5);
```

**CSS Shapes (2014)**
```css
shape-outside: circle(50%);
```

**CSS Scroll Behavior (2018)**
```css
scroll-behavior: smooth;
```

**inset Property (2020)**
```css
/* Shorthand for top, right, bottom, left */
inset: 10px;
```

**accent-color (2021)**
```css
input[type="checkbox"] {
    accent-color: hotpink;
}
```

**@supports (2013)**
```css
@supports (display: grid) {
    /* Grid-specific styles */
}
```

**Writing Modes (2015)**
```css
writing-mode: vertical-rl;
```

The pace of CSS innovation has dramatically accelerated, especially from 2020 onwards. Features like `:has()`, container queries, and native nesting represent fundamental shifts in how we write CSS, addressing pain points that previously required JavaScript or preprocessors.