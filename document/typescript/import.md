# Braces

In TypeScript (and JavaScript), whether you need braces (`{}`) in an import depends on how the module exports its components or values. Let's break this down:

---

### 1. **Default Export vs Named Export**

- **Default Export**:
    - A module can export a single default value or component.
    - When using a default export, you do not use braces `{}` around the imported value.
    - You can name the imported value whatever you want.

  **Example:**
  ```typescript
  // `Button` is a default export from @mui/material/Button
  import Button from '@mui/material/Button';
  ```

  This works because `@mui/material/Button` is exporting `Button` as the default export:
  ```javascript
  export default Button;
  ```

  You could even rename it while importing:
  ```typescript
  import MyButton from '@mui/material/Button';
  ```

---

- **Named Export**:
    - A module can export multiple named values or components.
    - When using a named export, you **must use braces `{}`** around the imported value, and the name must match exactly.

  **Example:**
  ```typescript
  // `DataGrid` is a named export from @mui/x-data-grid
  import { DataGrid } from '@mui/x-data-grid';
  ```

  This works because `@mui/x-data-grid` is exporting `DataGrid` as a **named export**:
  ```javascript
  export const DataGrid = ...;
  ```

  If you try to import `DataGrid` without braces, you'll get an error because it's not the default export.

---

### 2. **Why This Happens in Material-UI**
Material-UI packages like `@mui/material` and `@mui/x-data-grid` use a combination of default and named exports:

- **`@mui/material/Button`**:
  Exports the `Button` component as a **default export**, so you can import it without braces:
  ```typescript
  import Button from '@mui/material/Button';
  ```

- **`@mui/x-data-grid`**:
  Exports `DataGrid` as a **named export**, so you must use braces:
  ```typescript
  import { DataGrid } from '@mui/x-data-grid';
  ```

---

### 3. **How to Identify Default vs Named Export**
When using a library, you can check whether a component is a default export or a named export by:

#### a) **Reading the Documentation**
Most libraries explicitly state how to import their components.

#### b) **Checking the Module Source Code**
If you look at the source code or TypeScript declaration files (`.d.ts`), you'll see something like:

- For **default exports**:
  ```typescript
  export default Button;
  ```

- For **named exports**:
  ```typescript
  export const DataGrid = ...;
  ```

#### c) **IDE Autocomplete**
Modern IDEs like VS Code often provide hints for imports. If braces are required, the IDE will suggest `{}` for named exports.

---

### 4. **Mixing Default and Named Exports**
Some modules export both a default value and named values. For example:

```javascript
// Example module
export const DataGrid = ...;
export const GridToolbar = ...;
export default MyGridComponent;
```

You can import them like this:

- Default export:
  ```typescript
  import MyGridComponent from './module';
  ```

- Named export:
  ```typescript
  import { DataGrid, GridToolbar } from './module';
  ```

You can also combine both in a single import:

```typescript
import MyGridComponent, { DataGrid, GridToolbar } from './module';
```

---

### 5. **Practical Examples**
#### a) `@mui/material/Button` (default export):
```typescript
import Button from '@mui/material/Button'; // Correct
import { Button } from '@mui/material/Button'; // Incorrect (Button is not a named export)
```

#### b) `@mui/x-data-grid` (named export):
```typescript
import { DataGrid } from '@mui/x-data-grid'; // Correct
import DataGrid from '@mui/x-data-grid'; // Incorrect (DataGrid is not a default export)
```

---

### Summary

- **Default Export**: No braces needed. You can name the import whatever you want.
- **Named Export**: Requires braces `{}` and must match the exported name exactly.
- **Material-UI** uses:
    - **Default exports** for most `@mui/material` components (e.g., `Button`, `TextField`, etc.).
    - **Named exports** for `@mui/x-data-grid` and other specialized libraries (e.g., `DataGrid`, `GridToolbar`).

Let me know if you need further clarification! 🚀