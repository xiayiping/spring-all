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