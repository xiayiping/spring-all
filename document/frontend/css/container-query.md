# Container Query

`@container` in CSS is part of the [CSS Container Queries module](https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Container_Queries). It allows you to apply styles to elements based on the size or properties of their containing element, instead of the viewport (which is how traditional media queries work).

This feature is particularly useful for creating responsive components that adapt to the context in which they are placed, as opposed to relying solely on the browser window's dimensions.

---

### Syntax of `@container`

```css
@container (condition) {
  /* Styles that apply when the parent container meets the condition */
  selector {
    property: value;
  }
}
```

---

### Key Parts of Using `@container`

1. **Container Declaration**:
   To use container queries, you must declare an element as a "container" using the `container` property. Here's an example:

   ```css
   .container {
     container: size;
   }
   ```

   The `container` property has the following values:
    - `size`: Allows querying based on the container's inline size and block size (width and height).
    - `inline-size`: Allows querying based only on the container's width.

2. **Container Query Conditions**:
   Inside `@container`, you specify the condition, such as width, height, or aspect ratio. These conditions are similar to media queries. For example:

   ```css
   @container (min-width: 500px) {
     .box {
       background-color: blue;
     }
   }
   ```

   In this example, if the container element's width is at least `500px`, the `.box` element inside it will have a blue background.

---

### Example Usage

Here’s a practical example:

```html
<div class="container">
  <div class="box">
    Responsive Box
  </div>
</div>

<style>
  /* Declare the container */
  .container {
    container: size;
    width: 100%;
    border: 1px solid black;
  }

  .box {
    background-color: lightgray;
    padding: 1rem;
  }

  /* Apply styles based on the container’s size */
  @container (min-width: 400px) {
    .box {
      background-color: lightblue;
      font-size: 1.5rem;
    }
  }
</style>
```

In this example:
- The `.box` changes its background color and font size based on the size of `.container`, not the viewport.

---

### Use Cases for `@container`

1. **Reusable Components**:
    - Components that adapt to their context without relying on viewport-based media queries.
2. **Nested Responsive Layouts**:
    - Enables responsiveness in designs where elements are nested and the enclosing container is the determining factor for a child's layout.
3. **Component-Scoped Responsiveness**:
    - Scope styles based solely on the containing element, promoting modular and consistent design patterns.

---

### Browser Support

As of now, support for `@container` is improving but is not universal across all browsers (e.g., IE does not support it). Check the [Can I Use](https://caniuse.com/css-container-queries) website for the latest compatibility details.

---

### Summary

`@container` is a powerful feature for creating context-aware and modular designs. Unlike traditional media queries that depend on the viewport, container queries respond to the size and properties of the parent container, enabling more granular and scalable responsive designs.


# Attention

you **cannot rename `@container`** like that. The `@container` rule is a standardized part of the CSS specification, and its name must remain exactly `@container`. Custom naming such as `@container_123` or any other variation is invalid and will not work.

The `@container` at-rule is explicitly defined in the CSS standards for container queries, and there's no flexibility for renaming it.

---

### Summary of What You Can and Cannot Do with `@container`:
- ✅ **Use `@container` exactly as it is defined in the CSS specification.**
- ❌ **You cannot rename `@container` to something else, like `@container_123`, because it is a reserved keyword in CSS.**
- ✅ Only define **custom names for containers** using the `container-name` property as part of advanced usage (e.g., when you have multiple containers and need to match specific names).
  Example:
   ```css
   .container {
     container-name: my-container;
     container-type: inline-size;
   }

   @container my-container (min-width: 500px) {
     .box {
       font-size: 2rem;
     }
   }
   ```

This allows you to name containers and use those names for targeting within container queries, but the `@container` keyword itself is fixed.

---

If you need advanced customization or more granularity, focus on techniques like **assigning container names** with `container-name`. Do not attempt to rename the `@container` syntax.


# Container Type

The `**container-type**` property in CSS is part of the **CSS Container Queries** specification. It defines the "type" of measurement for a container, determining what aspects of the container can be queried in a `@container` rule.

---

### **What is the `container-type` Property?**

The `container-type` property specifies the dimensional criteria that define a **container’s queryable size**. This size defines what aspects (e.g., width, height) of the container are taken into account when applying the corresponding `@container` rules based on size.

### Syntax:

```css
/* Values for container-type */
container-type: size;
container-type: inline-size;
container-type: block-size;

/* Shorthand to disable */
container-type: none; /* Default */
```

---

### **Values of `container-type`**
There are **three main values** (plus `none`) for the `container-type` property:

1. **`inline-size`**:
    - Only queries the **inline size** of the container (its width).
    - Use this when your component needs to react to changes in **horizontal width** only, as for a row layout.
    - Example:
      ```css
      .container {
        container-type: inline-size;
      }
 
      @container (min-width: 500px) {
        .box {
          background-color: lightblue;
        }
      }
      ```

2. **`size`**:
    - Queries both **inline size** (width) and **block size** (height).
    - This is useful if you want your styles to adapt to the **area** or dimensions of the container as a whole.
    - Example:
      ```css
      .container {
        container-type: size;
      }
 
      @container (min-width: 500px) {
        .box {
          font-size: 1.5rem;
        }
      }
 
      @container (min-height: 300px) {
        .box {
          background-color: lightcoral;
        }
      }
      ```

3. **`block-size`** (rarely seen in practical use cases):
    - Queries only the **block size** of the container (its height).
    - Use this for specific designs where height changes are driving the component's layout.
    - Example:
      ```css
      .container {
        container-type: block-size;
      }
 
      @container (min-height: 400px) {
        .box {
          color: red;
        }
      }
      ```

4. **`none`**:
    - The default value. No container query functionality is applied to the element.
    - Use this to **disable container queries** on a container.

---

### When Should You Use Each?

- **`inline-size`**:
    - Ideal for horizontally flexible layouts, such as text flowing in a responsive grid where only the width matters.
    - Example: Resizing card containers in a responsive layout when their **width** changes.

- **`size`**:
    - Best when both width and height matter. Use this for overall responsive components that need to react to the **total space** available (e.g., resizable charts, images, and flexible divs in a grid).
    - Example: A complex grid that may grow or shrink in **both width and height**.

- **`block-size`**:
    - Rarely used, but it’s useful in vertical layouts where available **vertical space** drives the layout.
    - Example: Changing styles of content blocks in vertically constrained contexts (e.g., sidebar widgets or banners).

---

### **Examples with Explanation**

#### **Defining a Container Query with `inline-size`**

```css
.container {
   container-type: inline-size;
}

@container (min-width: 500px) {
   .child {
      font-size: 18px;
      color: blue;
   }
}
```
- The `.child` styles will activate only when `.container`'s **width** is `500px` or larger. `inline-size` supports styles for horizontal responsiveness.

#### **Defining a Query that Reacts to `size`**

```css
.container {
   container-type: size;
}

@container (min-width: 400px) and (min-height: 200px) {
   .box {
      padding: 20px;
      background: lightgreen;
   }
}
```
- In this example, `.box` styles change only when the **width and height** of `.container` meet the specified conditions.

---

### How `container-type` Enhances Responsiveness

- **Component-Level Queries**: Instead of relying on global viewport media queries, you can target specific containers for nested, locally scoped responsiveness.
- **Granular Control**: Provides precise control over what aspects of the container (width, height, or both) affect the layout or appearance of child elements, leading to modular and reusable design.

---

### Default Value

The default value of `container-type` is `none`, meaning a container won’t apply container queries unless explicitly specified.

---

### A Complete Example

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Container Queries</title>
  <style>
    /* Define the container */
    .container {
      container-type: size; /* Reacts to both width and height */
      width: 100%;
      height: 400px;
      border: 2px solid black;
    }

    /* Child styles for large containers */
    @container (min-width: 600px) and (min-height: 400px) {
      .box {
        background-color: lightblue;
        font-size: 2rem;
      }
    }

    /* Default child styles */
    .box {
      background-color: lightgray;
      font-size: 1rem;
      padding: 1rem;
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="box">Resize the container!</div>
  </div>
</body>
</html>
```

---

### Browser Support

- As of now, **`container-type`** is supported in modern browsers like Chrome, Edge, and Safari.
- For the latest compatibility, check resources like [Can I Use](https://caniuse.com/).

---

### Summary

- **`container-type`** determines what aspects of a container are queryable: width (`inline-size`), height (`block-size`), or both (`size`).
- Choose the correct `container-type` based on the layout behavior you want to achieve.
- It’s part of the broader container query feature and makes creating modular, responsive components much easier.