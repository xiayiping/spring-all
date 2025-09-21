# customized property

In CSS, `--abc: 50%;` is an example of a **CSS custom property** (also referred to as a CSS variable). Here's what it means:

### Explanation:
- `--abc` is the name of the custom property.
- `50%` is the value assigned to this custom property.
- Custom properties are defined using the `--` prefix and can be used throughout your stylesheets wherever you need them.

### How It Works:
1. **Defining the Custom Property**:
   Custom properties are typically defined within a `:root` selector (which applies globally) or within a specific scope.
   ```css
   :root {
       --abc: 50%;
   }
   ```

2. **Using the Custom Property**:
   You can use the `var()` function to reference the custom property wherever you need it. For example:
   ```css
   div {
       width: var(--abc);
   }
   ```

   This means the `div` element will have a width of `50%`.

3. **Overriding the Custom Property**:
   The value of a custom property can be overridden within a more specific scope:
   ```css
   :root {
       --abc: 50%;
   }

   .box {
       --abc: 30%;
   }

   div {
       width: var(--abc);
   }
   ```

   In this case:
    - For `.box` elements, `--abc` will have a value of `30%`.
    - For other elements, `--abc` will remain `50%`.

### Why Use Custom Properties?
1. **Reusability**: You can define a value once and use it in multiple places.
2. **Maintainability**: If you need to update a value (e.g., change `50%` to `60%`), you only need to change it in one location.
3. **Dynamic Updates**: Custom properties work well with JavaScript, allowing you to dynamically update styles at runtime.

### Example:
```css
:root {
    --main-color: #3498db;
    --padding: 20px;
    --width: 50%;
}

body {
    background-color: var(--main-color);
}

div {
    padding: var(--padding);
    width: var(--width);
}
```

Here, `--main-color`, `--padding`, and `--width` are custom properties that make the stylesheet reusable and easier to manage.


The **`rem`** (short for **"root em"**) is a CSS unit of measurement that is relative to the **root element's font size**. The root element in HTML is the `<html>` element.

---

### **How `rem` Works**
- `1rem` equals the font size of the root `<html>` element.
- By default, most browsers set the root font size to **16px** (which means `1rem = 16px` by default).
- If the root font size is changed, the value of `1rem` will adjust accordingly.

---

### **Example**

#### Default Root Font Size (16px)

```css
html {
  font-size: 16px; /* Default browser font size */
}

body {
  font-size: 1rem; /* 1rem = 16px */
}

h1 {
  font-size: 2rem; /* 2rem = 32px (2 × 16px) */
}

p {
  font-size: 0.875rem; /* 0.875rem = 14px (16px × 0.875) */
}
```

---

#### Custom Root Font Size

If you change the root font size, the `rem` unit scales accordingly:

```css
html {
  font-size: 20px; /* Root font size changed to 20px */
}

body {
  font-size: 1rem; /* 1rem = 20px */
}

h1 {
  font-size: 2rem; /* 2rem = 40px (2 × 20px) */
}

p {
  font-size: 0.875rem; /* 0.875rem = 17.5px (20px × 0.875) */
}
```

---

### **Difference Between `rem` and `em`**
- **`rem`**: Always relative to the root element's font size (`<html>`).
- **`em`**: Relative to the font size of the **parent element**.

#### Example of `em` vs `rem`:
```css
html {
  font-size: 16px;
}

.container {
  font-size: 20px; /* Parent font size */
}

.child {
  font-size: 1em;  /* 1em = 20px (relative to parent) */
  margin: 1rem;    /* 1rem = 16px (relative to root) */
}
```

---

### **Why Use `rem`?**
1. **Consistency**: Using `rem` ensures consistent scaling across the entire document, as all sizes are based on the root font size.
2. **Accessibility**: Users can change the browser's default font size (e.g., increasing it for readability). Using `rem` allows your design to scale proportionally.
3. **Simpler Maintenance**: You can adjust the root font size in one place (e.g., `<html>`) to scale everything in your design.

---

### **Practical Tips**
1. **Set a Scalable Base Font Size**:
   ```css
   html {
     font-size: 62.5%; /* 62.5% of 16px = 10px (easier math for rem units) */
   }

   body {
     font-size: 1.6rem; /* 1.6rem = 16px */
   }
   ```

2. **Use `rem` for Margins, Paddings, and Spacings**:
   ```css
   h1 {
     font-size: 2rem; /* 32px */
     margin-bottom: 1rem; /* 16px */
   }
   ```

---

### **Summary**
- `1rem` = root element's font size (default is 16px in most browsers).
- **Use `rem`** to create scalable and consistent layouts.
- Adjust the `<html>` font size to control the scaling of `rem` throughout your design.