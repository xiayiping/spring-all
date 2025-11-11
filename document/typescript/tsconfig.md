# Options

The `"jsx": "react"` option in the `tsconfig.json` file specifies how TypeScript should handle JSX syntax in your project. JSX is the syntax used in React to describe the structure of user interfaces, and this setting tells TypeScript how to compile it.

---

### **Purpose of `"jsx": "react"`**
The `"jsx"` option determines how JSX code (like `<div>Hello</div>`) is transformed into JavaScript during the build process. Specifically, it controls how TypeScript compiles JSX into function calls.

For example:
```tsx
const App = () => <div>Hello, world!</div>;
```

With `"jsx": "react"`, this JSX code is compiled into:
```js
const App = () => React.createElement("div", null, "Hello, world!");
```

In this case, TypeScript uses the `React.createElement` function (from the React library) to transform JSX into JavaScript.

---

### **Common Values for `"jsx"`**

Here are the most common values for the `"jsx"` option and their purposes:

| Value              | Description                                                                                                  |
|--------------------|--------------------------------------------------------------------------------------------------------------|
| `"react"`          | Transforms JSX into `React.createElement(...)`. Use this for React projects targeting older React versions (<17). |
| `"react-jsx"`      | Transforms JSX using the new JSX runtime introduced in React 17 (`_jsx` or `_jsxs` functions). No need to import React in every file. |
| `"react-jsxdev"`   | Similar to `"react-jsx"`, but optimized for development (includes debug info). Used with React 17+ and Vite. |
| `"preserve"`       | Leaves JSX untransformed. This is useful if the build tool (like Babel or Vite) will handle JSX transformation. |
| `"react-native"`   | Used for React Native projects. Transforms JSX into React Native-specific components. |

---

### **Which Value Should You Use?**

- **React 16 or Earlier**: Use `"jsx": "react"`. You must explicitly import React in every file that uses JSX.
- **React 17 or Later**: Use `"jsx": "react-jsx"`. With the new JSX runtime, you no longer need to import React in every file where JSX is used.
- **Development in React 17+**: Use `"jsx": "react-jsxdev"` for better debugging tools during development.
- **Using a Build Tool (like Vite)**: Use `"jsx": "preserve"` if you want Vite or Babel to handle JSX transformation.

---

### **Example: tsconfig.json Configuration**

For projects using React 17+ with Vite (modern setup):
```json
{
  "compilerOptions": {
    "target": "ES2015",
    "lib": ["DOM", "ESNext"],
    "module": "ESNext",
    "strict": true,
    "jsx": "react-jsx", // Optimized for React 17+ (no need to import React for JSX)
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true
  }
}
```

For older React versions (React 16 or earlier):
```json
{
  "compilerOptions": {
    "target": "ES5",
    "lib": ["DOM", "ES5"],
    "module": "ESNext",
    "strict": true,
    "jsx": "react", // Transforms JSX into React.createElement
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true
  }
}
```

---

### **React 17+ New JSX Runtime**

Since React 17, the new JSX runtime allows you to use JSX without importing React explicitly in every file. If you use `"jsx": "react-jsx"` or `"jsx": "react-jsxdev"`, the JSX transformation will automatically import helper functions (`_jsx` or `_jsxs`) from the React library.

For example, with `"jsx": "react-jsx"`, this code:
```tsx
const App = () => <div>Hello</div>;
```

Compiles to:
```js
import { jsx as _jsx } from "react/jsx-runtime";
const App = () => _jsx("div", { children: "Hello" });
```

---

### **When to Use `"jsx": "preserve"`**

If you are using a modern build tool like **Vite**, **Babel**, or **SWC** that already handles JSX transformation, you can use `"jsx": "preserve"`. In this case, TypeScript does not transform JSX and leaves it as-is for the build tool to process.

Example:
```json
{
  "compilerOptions": {
    "jsx": "preserve"
  }
}
```

This is especially useful in projects that want to use advanced JSX transformations through Babel plugins or other tools.

---

### **Conclusion**
- `"jsx": "react"` is used to compile JSX into `React.createElement`, which is necessary for React projects using versions **before React 17**.
- For React 17+, it’s better to use `"jsx": "react-jsx"` to take advantage of the new JSX runtime (no need to import React in every file).
- If your build tool (like Vite) handles JSX transformation, you can use `"jsx": "preserve"`.

Choose the appropriate setting based on the React version and build tools you're using!