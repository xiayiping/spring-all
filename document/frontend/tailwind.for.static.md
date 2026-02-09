# Tailwind CSS v4 Changes (No Config File!)

Great observation! **Shadcn/ui** now uses **Tailwind CSS v4**, which has a completely different configuration approach.

## Tailwind v3 vs v4

| Tailwind v3 | Tailwind v4 |
|-------------|-------------|
| `tailwind.config.js` | No config file needed |
| `postcss.config.js` | No PostCSS config needed |
| `@tailwind base/components/utilities` | `@import "tailwindcss"` |
| Config in JavaScript | Config in CSS using `@theme` |

## How Tailwind v4 Works

### 1. CSS File Only

**`src/styles/global.css`**
```css
@import "tailwindcss";

/* Content sources - tells Tailwind where to scan for classes */
@source "../**/*.{js,ts,jsx,tsx,html}";
@source "../../*.html";

/* Theme customization (replaces tailwind.config.js) */
@theme {
  --color-primary: #3490dc;
  --color-secondary: #ffed4a;
  
  --font-sans: "Inter", sans-serif;
  
  /* Custom spacing */
  --spacing-128: 32rem;
}

/* Custom components */
@layer components {
  .btn-primary {
    @apply px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition;
  }
  
  .card {
    @apply p-6 bg-white rounded-lg shadow-md;
  }
}
```

### 2. Vite Config (Simpler!)

**`vite.config.ts`**
```ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';  // New Vite plugin!
import { resolve } from 'path';

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),  // Just add this plugin!
  ],
  build: {
    rollupOptions: {
      input: {
        page1: resolve(__dirname, 'page1.html'),
        page2: resolve(__dirname, 'page2.html'),
      },
    },
  },
});
```

### 3. Package.json

```json
{
  "name": "multi-page-tailwind-v4",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0"
  },
  "devDependencies": {
    "@tailwindcss/vite": "^4.0.0",
    "@types/node": "^20.0.0",
    "@types/react": "^18.2.0",
    "@types/react-dom": "^18.2.0",
    "@vitejs/plugin-react": "^4.0.0",
    "tailwindcss": "^4.0.0",
    "typescript": "^5.0.0",
    "vite": "^5.0.0"
  }
}
```

## Complete Setup for Your Multi-Page Project

### Project Structure (Cleaner!)

```
project/
├── src/
│   ├── components/
│   │   ├── Component1.tsx
│   │   ├── Component2.tsx
│   │   ├── Component3.tsx
│   │   └── Component4.tsx
│   ├── styles/
│   │   └── global.css        # All Tailwind config lives here!
│   ├── main1.tsx
│   └── main2.tsx
├── page1.html
├── page2.html
├── vite.config.ts
├── tsconfig.json
└── package.json
                              # No tailwind.config.js!
                              # No postcss.config.js!
```

### Full `global.css` Example

**`src/styles/global.css`**
```css
@import "tailwindcss";

/* 
 * Tell Tailwind where to find classes
 * This replaces the 'content' array in tailwind.config.js
 */
@source "../**/*.{js,ts,jsx,tsx}";
@source "../../*.html";

/*
 * Theme customization
 * This replaces the 'theme' object in tailwind.config.js
 */
@theme {
  /* Colors */
  --color-primary: #3490dc;
  --color-primary-dark: #2779bd;
  --color-secondary: #ffed4a;
  --color-accent: #38c172;
  
  /* Fonts */
  --font-sans: "Inter", "Segoe UI", sans-serif;
  --font-mono: "Fira Code", monospace;
  
  /* Custom spacing */
  --spacing-18: 4.5rem;
  --spacing-128: 32rem;
  
  /* Custom border radius */
  --radius-xl: 1rem;
  --radius-2xl: 1.5rem;
  
  /* Shadows */
  --shadow-soft: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

/*
 * Base styles
 * Applies to HTML elements directly
 */
@layer base {
  html {
    @apply scroll-smooth;
  }
  
  body {
    @apply font-sans text-gray-800 antialiased;
  }
  
  h1 {
    @apply text-3xl font-bold text-gray-900;
  }
  
  h2 {
    @apply text-2xl font-semibold text-gray-800;
  }
  
  h3 {
    @apply text-xl font-medium text-gray-700;
  }
}

/*
 * Reusable component classes
 */
@layer components {
  .btn {
    @apply px-4 py-2 rounded font-medium transition-colors duration-200;
  }
  
  .btn-primary {
    @apply btn bg-primary text-white hover:bg-primary-dark;
  }
  
  .btn-secondary {
    @apply btn bg-secondary text-gray-800 hover:bg-yellow-400;
  }
  
  .btn-outline {
    @apply btn border-2 border-primary text-primary hover:bg-primary hover:text-white;
  }
  
  .card {
    @apply p-6 bg-white rounded-xl shadow-soft;
  }
  
  .input {
    @apply w-full px-4 py-2 border border-gray-300 rounded-lg 
           focus:ring-2 focus:ring-primary focus:border-transparent
           outline-none transition;
  }
  
  .container-narrow {
    @apply max-w-4xl mx-auto px-4;
  }
}

/*
 * Utility classes
 */
@layer utilities {
  .text-balance {
    text-wrap: balance;
  }
  
  .animate-fade-in {
    animation: fadeIn 0.3s ease-in-out;
  }
  
  @keyframes fadeIn {
    from { opacity: 0; transform: translateY(-10px); }
    to { opacity: 1; transform: translateY(0); }
  }
}
```

### Entry Files

**`src/main1.tsx`**
```tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import './styles/global.css';
import Component1 from './components/Component1';
import Component2 from './components/Component2';

const div1 = document.getElementById('div1');
if (div1) {
  ReactDOM.createRoot(div1).render(
    <React.StrictMode>
      <Component1 />
    </React.StrictMode>
  );
}

const div2 = document.getElementById('div2');
if (div2) {
  ReactDOM.createRoot(div2).render(
    <React.StrictMode>
      <Component2 />
    </React.StrictMode>
  );
}
```

### HTML with Static Content

**`page1.html`**
```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Page 1</title>
  </head>
  <body class="bg-gray-100 min-h-screen">
    
    <!-- Static content using Tailwind + custom classes -->
    <header class="bg-primary text-white py-4 shadow-lg">
      <div class="container mx-auto px-4">
        <h1 class="text-white">My Website</h1>
        <nav class="mt-2 flex gap-4">
          <a href="page1.html" class="hover:text-secondary">Page 1</a>
          <a href="page2.html" class="hover:text-secondary">Page 2</a>
        </nav>
      </div>
    </header>

    <main class="container mx-auto px-4 py-8">
      <!-- Static section -->
      <section class="mb-8 animate-fade-in">
        <h2>Welcome to Page 1</h2>
        <p class="text-gray-600 mt-2">
          This static content uses Tailwind CSS v4!
        </p>
        <div class="flex gap-3 mt-4">
          <button class="btn-primary">Primary</button>
          <button class="btn-secondary">Secondary</button>
          <button class="btn-outline">Outline</button>
        </div>
      </section>

      <!-- React components -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div id="div1" class="card"></div>
        <div id="div2" class="card"></div>
      </div>
    </main>

    <footer class="bg-gray-800 text-white py-6 mt-8">
      <p class="text-center">&copy; 2024 My Website</p>
    </footer>

    <script type="module" src="/src/main1.tsx"></script>
  </body>
</html>
```

### React Component Example

**`src/components/Component1.tsx`**
```tsx
const Component1 = () => {
  return (
    <div className="animate-fade-in">
      <h3 className="text-primary mb-4">React Component 1</h3>
      <p className="text-gray-600 mb-4">
        This component uses the same Tailwind classes!
      </p>
      <input 
        type="text" 
        className="input mb-4" 
        placeholder="Custom input class"
      />
      <div className="flex gap-2">
        <button className="btn-primary">Save</button>
        <button className="btn-outline">Cancel</button>
      </div>
    </div>
  );
};

export default Component1;
```

## Quick Comparison

```
┌─────────────────────────────────────────────────────────────┐
│                    TAILWIND v3 (OLD)                        │
├─────────────────────────────────────────────────────────────┤
│  tailwind.config.js    →  content: [...]                    │
│                        →  theme: { extend: {...} }          │
│  postcss.config.js     →  plugins: [tailwindcss]            │
│  global.css            →  @tailwind base/components/utils   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    TAILWIND v4 (NEW)                        │
├─────────────────────────────────────────────────────────────┤
│  global.css            →  @import "tailwindcss"             │
│                        →  @source "..."                     │
│                        →  @theme { ... }                    │
│  vite.config.ts        →  @tailwindcss/vite plugin          │
│                                                             │
│         ❌ No tailwind.config.js                            │
│         ❌ No postcss.config.js                             │
└─────────────────────────────────────────────────────────────┘
```

## Install Commands

```bash
# For Tailwind v4
npm install -D tailwindcss @tailwindcss/vite
```

That's it! Much simpler setup with Tailwind v4. Everything is configured in your CSS file now.
