# `@/` vs `./` Import Paths

## Quick Answer

| Path | Type | Resolves To |
|------|------|-------------|
| `@/components/example` | **Alias** (configured) | Usually project root `/src/components/example` |
| `./components/example` | **Relative** | Relative to current file |

---

## `./` - Relative Path

Standard JavaScript/Node.js relative imports.

```
project/
├── src/
│   ├── pages/
│   │   └── Home.jsx        ← You are here
│   └── components/
│       └── example.js
```

```js
// From Home.jsx
import { Example } from "./components/example"    // ❌ Wrong - looks in pages/components/
import { Example } from "../components/example"   // ✅ Correct - goes up, then into components/
```

---

## `@/` - Path Alias

**Not a JavaScript feature** - it's a **configured alias** in your build tool.

```js
// With @/ alias configured to /src
import { Example } from "@/components/example"

// Resolves to:
// /src/components/example
```

Works from **anywhere** in your project:

```
src/
├── pages/
│   └── deep/
│       └── nested/
│           └── Page.jsx    ← @/components/example still works!
└── components/
    └── example.js
```

---

## How to Configure `@/`

### Vite (`vite.config.js`)

```js
import { defineConfig } from 'vite'
import path from 'path'

export default defineConfig({
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
})
```

### TypeScript (`tsconfig.json`)

```json
{
  "compilerOptions": {
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  }
}
```

### Webpack (`webpack.config.js`)

```js
module.exports = {
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
}
```

### Next.js (`tsconfig.json`)

```json
{
  "compilerOptions": {
    "paths": {
      "@/*": ["./*"]
    }
  }
}
```

---

## Comparison Example

```
project/
└── src/
    ├── components/
    │   └── Button.jsx
    └── pages/
        └── dashboard/
            └── settings/
                └── Profile.jsx   ← Importing from here
```

```js
// From Profile.jsx

// Relative path - messy!
import { Button } from "../../../components/Button"

// Alias path - clean!
import { Button } from "@/components/Button"
```

---

## Common Aliases

| Alias | Usually Points To |
|-------|-------------------|
| `@/` | `/src` |
| `~/` | `/src` or project root |
| `@components/` | `/src/components` |
| `@utils/` | `/src/utils` |
| `@lib/` | `/src/lib` |

---

## Summary

| Feature | `./` Relative | `@/` Alias |
|---------|---------------|------------|
| Built-in JS | ✅ Yes | ❌ No |
| Needs config | ❌ No | ✅ Yes |
| Changes with file location | ✅ Yes | ❌ No |
| Clean deep imports | ❌ `../../../` | ✅ `@/` |
| Refactor-friendly | ❌ Paths break | ✅ Stable |

**Bottom line**: `@/` is just a shortcut you configure - it's not searching from "root" by default. You define where it points!


# How `export * as` Works

## The Flow

### Step 1: Source File (`index.parts.js`)

```js
// index.parts.js exports individual components
export const Root = () => { /* ... */ }
export const Trigger = () => { /* ... */ }
export const Content = () => { /* ... */ }
export const Item = () => { /* ... */ }
```

### Step 2: Re-export with Namespace (`index.js` or barrel file)

```js
// This line collects ALL exports from index.parts.js
// and bundles them under the name "Menu"
export * as Menu from "./index.parts.js";
```

This creates:

```js
Menu = {
  Root: ...,
  Trigger: ...,
  Content: ...,
  Item: ...
}
```

### Step 3: Import Elsewhere

```js
// Importing and renaming
import { Menu as MenuPrimitive } from "some-package";

// Now MenuPrimitive = Menu = {
//   Root: ...,
//   Trigger: ...,
//   Content: ...,
//   Item: ...
// }

// So you can use:
<MenuPrimitive.Root />
<MenuPrimitive.Trigger />
```

---

## Visual Diagram

```
┌─────────────────────────────────────────────────────────────┐
│  index.parts.js                                             │
│  ─────────────                                              │
│  export const Root = ...                                    │
│  export const Trigger = ...                                 │
│  export const Content = ...                                 │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│  barrel file (index.js)                                     │
│  ──────────────────────                                     │
│  export * as Menu from "./index.parts.js"                   │
│                                                             │
│  // Creates: Menu.Root, Menu.Trigger, Menu.Content          │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│  Your component file                                        │
│  ───────────────────                                        │
│  import { Menu as MenuPrimitive } from "package"            │
│                                                             │
│  // MenuPrimitive.Root = Menu.Root                          │
│  // MenuPrimitive.Trigger = Menu.Trigger                    │
│                                                             │
│  <MenuPrimitive.Root />  ✅ Works!                          │
└─────────────────────────────────────────────────────────────┘
```

---

## Key Concepts

| Syntax | What It Does |
|--------|--------------|
| `export * as X from "..."` | Re-exports all as namespace `X` |
| `import { X as Y }` | Imports `X` but renames to `Y` |

---

## Complete Example

```js
// ===== menu/index.parts.js =====
export const Root = (props) => <div {...props} />;
export const Item = (props) => <li {...props} />;

// ===== menu/index.js =====
export * as Menu from "./index.parts.js";

// ===== components/dropdown.jsx =====
import { Menu as MenuPrimitive } from "../menu";

function Dropdown() {
  return (
    <MenuPrimitive.Root data-slot="dropdown-menu">
      <MenuPrimitive.Item>Option 1</MenuPrimitive.Item>
    </MenuPrimitive.Root>
  );
}
```

---

## Why Use This Pattern?

1. **Organization** - Group related components
2. **Clean imports** - Single import for multiple components
3. **Naming flexibility** - Rename on import (`as MenuPrimitive`)
4. **Avoids conflicts** - Namespace prevents name collisions

---

## Summary

```js
export * as Menu from "./index.parts.js";  // Bundle exports as "Menu"
import { Menu as MenuPrimitive } from "pkg"; // Rename "Menu" → "MenuPrimitive"

<MenuPrimitive.Root />  // Access Root from the namespace
```

The `MenuPrimitive` name comes from the **import rename** (`as MenuPrimitive`), not from the source file!

