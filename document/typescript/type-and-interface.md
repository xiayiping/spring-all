# Type and Interface

In TypeScript, both `interface` and `type` can be used to define the shape of an object or data structure. However, they are not exactly the same and have some key differences. Here's a comparison:

---

### **1. `interface Abc {}`**

- **Purpose**: Used to define the structure of objects, including their properties, methods, and relationships.
- **Extensibility**: Interfaces are extendable, meaning you can create new interfaces that inherit from existing ones using the `extends` keyword.
- **Declaration Merging**: If the same interface is declared multiple times, TypeScript automatically merges them into a single definition.

```typescript
interface Abc {
  name: string;
}

// Extending an interface
interface ExtendedAbc extends Abc {
  age: number;
}

// Declaration merging
interface Abc {
  age: number; // Merges with the original `Abc`
}

const example: Abc = {
  name: "John",
  age: 30, // Works due to declaration merging
};
```

---

### **2. `type Abc = {}`**

- **Purpose**: A `type` alias can be used to define a wide variety of types, including primitives, unions, intersections, tuples, and more. It is more flexible than `interface` in this regard.
- **Extensibility**: `type` aliases cannot be extended like interfaces, but you can create new types by combining them with unions or intersections.
- **No Declaration Merging**: Multiple `type` definitions with the same name will result in an error.

```typescript
type Abc = {
  name: string;
};

// Combining with intersections
type ExtendedAbc = Abc & {
  age: number;
};

// Union types
type Age = 20 | 30 | 40;

const example: ExtendedAbc = {
  name: "John",
  age: 30,
};
```

---

### **Key Differences**

| Feature                     | `interface`                   | `type`                          |
|-----------------------------|-------------------------------|----------------------------------|
| **Extensibility**           | Can be extended using `extends` | Can use intersections (`&`)     |
| **Declaration Merging**     | Supports declaration merging   | Does not allow merging          |
| **Advanced Types**          | Limited to objects             | Supports unions, intersections, tuples, etc. |
| **Usage**                   | Typically for objects and classes | More general-purpose           |

---

### **When to Use `interface` vs `type`**

- Use **`interface`** when:
    - You need to define the structure of objects or classes.
    - You want to take advantage of declaration merging.
    - You need to extend the type with `extends`.

- Use **`type`** when:
    - You need to define a union, tuple, or intersection type.
    - You want more flexibility in defining the type.
    - You don't need declaration merging or extension.

---

### **Conclusion**

Both `interface` and `type` are powerful tools in TypeScript, and in many cases, they can be used interchangeably. However, `interface` is generally preferred for defining object shapes, while `type` is better suited for more complex or flexible type definitions.
