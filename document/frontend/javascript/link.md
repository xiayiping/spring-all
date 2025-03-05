# preload

```html

<link rel="preload" href="/docs/latest/scss/style.min.65324e4de191c292ee8b6f4f0ebf01320e8c4bc8623b04e9dfad322ebb490b31.css" as="style">

<link href="/docs/latest/scss/style.min.65324e4de191c292ee8b6f4f0ebf01320e8c4bc8623b04e9dfad322ebb490b31.css" rel="stylesheet" integrity="">

```

The two `<link>` elements you provided serve different purposes and have distinct behaviors in terms of loading CSS files. Let’s break down what each does and the differences between them:

---

### **1. `<link rel="preload" as="style" href="...">`**
- **Purpose:** This `<link>` is used to preload the CSS file. Preloading allows the browser to fetch the resource (the CSS file) early in the loading process, even before it is needed for rendering.
- **Behavior:**
    - The browser downloads the file as soon as it encounters the `<link>` tag.
    - The **`as="style"`** attribute specifies that the file is a stylesheet, optimizing the browser's fetch logic.
    - **Preload does not apply the CSS** to the document by itself. To actually apply the preloaded CSS, it must later be referenced by a `<link rel="stylesheet">` tag elsewhere in the HTML.
- **Use Case:** Preloading is often used to improve performance by reducing the time it takes to fetch critical resources (like CSS files) that will be applied later, ensuring they are available when needed.

---

### **2. `<link rel="stylesheet" href="...">`**
- **Purpose:** This `<link>` directly applies the CSS to the document, enabling the styles defined in the file to affect the page layout and appearance.
- **Behavior:**
    - The browser downloads the CSS file and immediately applies it to the document.
    - The `rel="stylesheet"` tag is what tells the browser to parse and render the CSS.
    - If the browser encounters a blocking issue (such as a slow network), rendering may be delayed until the CSS is fully loaded.
- **Use Case:** This is the standard way to include and apply CSS styles to a webpage.

---

### **Key Differences**

| Feature                        | `<link rel="preload">`                               | `<link rel="stylesheet">`                          |
|--------------------------------|----------------------------------------------------|--------------------------------------------------|
| **Purpose**                    | Preloads the CSS file for later use.               | Applies the CSS file directly to the document.   |
| **When CSS is Applied**        | Not applied automatically; must be referenced later. | Applied immediately after the file is loaded.    |
| **Effect on Rendering**        | No immediate effect on rendering.                  | Affects rendering as soon as the file is loaded. |
| **Performance Impact**         | Optimizes loading by fetching early.               | Blocks rendering until loaded and applied.       |
| **Attribute Requirements**     | Requires `as="style"`.                             | No additional attributes required.               |
| **Use Case**                   | Preloading critical styles for faster load times.  | Applying styles directly to the document.        |

---

### **How They Work Together**
- You can use both tags together for optimal performance:
  ```html
  <link rel="preload" href="style.css" as="style">
  <link rel="stylesheet" href="style.css">
  ```
    - The first `<link>` preloads the CSS file.
    - The second `<link>` applies the CSS.
    - This combination ensures the CSS is fetched early while also being applied as soon as it is needed.

---

### **Conclusion**
- **`rel="preload"`** is a performance optimization tool that fetches the CSS file early but does not apply it.
- **`rel="stylesheet"`** is the standard way to load and apply CSS to a webpage.
- Use both together if you want to preload critical styles and apply them efficiently.