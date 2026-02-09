# Multiple HTML Pages with Different Entry Points in Vite

Here's how to set up multiple pages with different entry points.

## Project Structure

```
project/
├── src/
│   ├── components/
│   │   ├── Component1.tsx
│   │   ├── Component2.tsx
│   │   ├── Component3.tsx
│   │   └── Component4.tsx
│   ├── main1.tsx          # Entry for page1
│   └── main2.tsx          # Entry for page2
├── page1.html
├── page2.html
├── vite.config.ts
├── tsconfig.json
└── package.json
```

## 1. HTML Files

**`page1.html`**
```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Page 1</title>
  </head>
  <body>
    <h1>Page 1</h1>
    <div id="div1"></div>
    <div id="div2"></div>
    <script type="module" src="/src/main1.tsx"></script>
  </body>
</html>
```

**`page2.html`**
```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Page 2</title>
  </head>
  <body>
    <h1>Page 2</h1>
    <div id="div1"></div>
    <div id="div2"></div>
    <script type="module" src="/src/main2.tsx"></script>
  </body>
</html>
```

## 2. Entry Files

**`src/main1.tsx`**
```tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
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

**`src/main2.tsx`**
```tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import Component3 from './components/Component3';
import Component4 from './components/Component4';

const div1 = document.getElementById('div1');
if (div1) {
  ReactDOM.createRoot(div1).render(
    <React.StrictMode>
      <Component3 />
    </React.StrictMode>
  );
}

const div2 = document.getElementById('div2');
if (div2) {
  ReactDOM.createRoot(div2).render(
    <React.StrictMode>
      <Component4 />
    </React.StrictMode>
  );
}
```

## 3. Vite Config (Key Part!)

**`vite.config.ts`**
```ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';

export default defineConfig({
  plugins: [react()],
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

## 4. TypeScript Config

**`tsconfig.json`**
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true
  },
  "include": ["src"]
}
```

**`tsconfig.node.json`**
```json
{
  "compilerOptions": {
    "composite": true,
    "skipLibCheck": true,
    "module": "ESNext",
    "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true
  },
  "include": ["vite.config.ts"]
}
```

## 5. Package.json

```json
{
  "name": "multi-page-app",
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
    "@types/node": "^20.0.0",
    "@types/react": "^18.2.0",
    "@types/react-dom": "^18.2.0",
    "@vitejs/plugin-react": "^4.0.0",
    "typescript": "^5.0.0",
    "vite": "^5.0.0"
  }
}
```

## 6. Sample Components

**`src/components/Component3.tsx`**
```tsx
const Component3 = () => {
  return <div><h2>Component 3 (Page 2)</h2></div>;
};

export default Component3;
```

**`src/components/Component4.tsx`**
```tsx
const Component4 = () => {
  return <div><h2>Component 4 (Page 2)</h2></div>;
};

export default Component4;
```

## Usage

```bash
# Development - access pages at:
npm run dev
# http://localhost:5173/page1.html
# http://localhost:5173/page2.html

# Build for production
npm run build

# Preview production build
npm run preview
```

## Build Output Structure

```
dist/
├── assets/
│   ├── page1-[hash].js
│   ├── page2-[hash].js
│   └── [shared chunks].js
├── page1.html
└── page2.html
```

## Summary Table

| File | Purpose |
|------|---------|
| `vite.config.ts` | Define multiple HTML entry points in `rollupOptions.input` |
| `page1.html` | Links to `main1.tsx` |
| `page2.html` | Links to `main2.tsx` |
| `main1.tsx` | Entry point for page 1 |
| `main2.tsx` | Entry point for page 2 |

The key is the **`build.rollupOptions.input`** configuration that tells Vite to build multiple HTML pages!
