# what is export (default)

### **What is `export default` in JavaScript?**

In JavaScript (especially in ES6 modules), the `export default` keyword is used to **export a single value, function, class, or object** as the default export of a module. This allows other files to import it without using curly braces `{}` and often simplifies importing.

---

### **Key Features of `export default`**

1. **One Default Export per Module**:
    - Each module can have **only one default export**. This is the main value or component the module is designed to expose.
    - Example:
      ```javascript
      // math.js
      export default function add(a, b) {
        return a + b;
      }
      ```

2. **Simplified Syntax for Importing**:
    - When importing a default export, you can give it **any name**:
      ```javascript
      // Importing the default function
      import sum from './math.js';
      console.log(sum(2, 3)); // Output: 5
      ```

3. **No Requirement for Named Imports**:
    - Default exports are not imported using their original name in the source file. You can rename them freely while importing:
      ```javascript
      import myCustomName from './math.js';
      console.log(myCustomName(2, 3)); // Output: 5
      ```

---

### **What Happens If a Function Doesn’t Use `export default`?**

If a function (or variable, class, etc.) is not prefixed with `export default`, it must be exported using a **named export**. This means:
1. You explicitly define the name of the export.
2. When importing, you must use curly braces `{}` and the exact exported name.

---

### **Named Exports vs Default Exports**

#### **Named Export**
- A module can have **multiple named exports**.
- Functions, variables, or classes are explicitly exported with their names using `export`.
- When importing, you must refer to them by their exact exported name (with curly braces).

**Example using Named Export**:
```javascript
// utils.js
export function add(a, b) {
  return a + b;
}

export function subtract(a, b) {
  return a - b;
}
```

```javascript
// main.js
import { add, subtract } from './utils.js';
console.log(add(2, 3));       // Output: 5
console.log(subtract(5, 2));  // Output: 3
```

#### **Differences Between Named and Default Exports**
| Feature                  | Default Export                        | Named Export                     |
|--------------------------|---------------------------------------|----------------------------------|
| Export Syntax            | `export default value`               | `export { value }` or `export`  |
| Import Syntax            | `import anything from './file.js'`   | `import { exactName } from './file.js'` |
| Number of Exports         | Only one per module                  | Multiple per module              |
| Import Name Requirement  | Can rename freely                    | Must match the export name       |

---

### **What Happens If a Function Isn’t Exported (Default or Named)?**

If a function (or any value) is **not exported at all**, it is **private to the module**. This means it cannot be accessed or imported by any other file.

**Example**:
```javascript
// utils.js
function multiply(a, b) {
  return a * b;
}
export function add(a, b) {
  return a + b;
}
```

```javascript
// main.js
import { add } from './utils.js';
console.log(add(2, 3)); // Output: 5
console.log(multiply(2, 3)); // Error: multiply is not defined
```

In this example, `multiply` is private to `utils.js` because it is not exported.

---

### **Combining Default and Named Exports**

A module can have **both default and named exports** simultaneously. For example:
```javascript
// shapes.js
export default function circleArea(radius) {
  return Math.PI * radius * radius;
}

export function squareArea(side) {
  return side * side;
}
```

```javascript
// main.js
import circleArea, { squareArea } from './shapes.js';
console.log(circleArea(5));    // Output: 78.53981633974483
console.log(squareArea(4));    // Output: 16
```

Here:
- `circleArea` is the default export.
- `squareArea` is a named export.

---

### **Summary**

1. `export default` is used to export a single value from a module, which can be imported without using curly braces and renamed freely.
2. Without `export default`, you use **named exports** where values must be explicitly exported and imported using their exact names.
3. If a function or value is not exported at all, it remains private to its module and cannot be accessed from other files.

**When to Use:**
- Use **default exports** for the primary functionality of a module.
- Use **named exports** for utility functions or when exporting multiple values from a module.