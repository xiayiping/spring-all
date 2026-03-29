# static with vite

To create a **pure static page** using **Tailwind CSS** and build it with **Vite** such that the result is entirely static and can be served by only **Nginx**, follow these steps:

---

## **Project Setup**

### **1. Initialize the Project**
Start by creating a new project directory for your static page and initializing `npm`:

```bash
mkdir static-tailwind-vite
cd static-tailwind-vite
npm init -y
```

This creates a `package.json` file for your project.

---

### **2. Install Dependencies**
Install **Vite**, **Tailwind CSS**, and the necessary PostCSS plugins:

```bash
npm install vite tailwindcss postcss autoprefixer
```

---

### **3. Configure Tailwind CSS**
Generate the Tailwind CSS configuration file:

```bash
npx tailwindcss init
```

This creates a basic `tailwind.config.js` file.

---

### **4. Project Structure**
Set up the project directory structure:

```plaintext
static-tailwind-vite/
├── index.html
├── src/
│   ├── main.css
│   └── main.js
├── vite.config.js
├── tailwind.config.js
├── package.json
└── node_modules/
```

---

### **5. Configure Tailwind CSS Config**
Update your `tailwind.config.js` to include the HTML and JavaScript files in the content array:

```javascript
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
};
```

The `content` section tells Tailwind which files to scan for class usage and ensures unused styles are purged from the final build.

---

### **6. Create Tailwind CSS Entry Point**
Inside the `src` directory, create the `main.css` file and import Tailwind CSS:

```css
/* src/main.css */
@tailwind base;
@tailwind components;
@tailwind utilities;
```

This file will serve as your entry point for Tailwind CSS styles.

---

### **7. Add JavaScript Entry Point**
If your page includes JavaScript (e.g., for interactivity), add a `main.js` file inside `src`:

```javascript
// src/main.js
console.log("Hello, Tailwind with Vite!");
```

---

### **8. Set Up Vite**
Create a `vite.config.js` file to configure Vite. Since this is a static page, we don’t need much customization:

```javascript
// vite.config.js
import { defineConfig } from 'vite'

export default defineConfig({
  build: {
    outDir: 'dist', // Output directory for the static files
    emptyOutDir: true,
  },
  css: {
    postcss: {
      plugins: [require('tailwindcss'), require('autoprefixer')],
    },
  },
});
```

- The `build.outDir` option ensures the static resources will be built into a `dist/` directory.
- Vite will automatically include PostCSS to process Tailwind CSS.

---

### **9. Create the HTML File**
Build your static HTML page in the `index.html` file. Include the `main.css` and `main.js` files:

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Static Page with Vite and Tailwind</title>
  <link rel="stylesheet" href="/src/main.css">
</head>
<body class="bg-gray-100 text-center py-10">
  <h1 class="text-4xl font-bold text-blue-600">Hello, Tailwind with Vite!</h1>
  <p class="mt-4 text-lg text-gray-700">This is a pure static page built with Tailwind CSS and Vite.</p>
  <button class="mt-6 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600">
    Click Me
  </button>
  <script type="module" src="/src/main.js"></script>
</body>
</html>
```

---

## **Development and Build Process**

### **10. Build and Preview**

#### **Run in Development Mode**
To check progress while developing, run:

```bash
npx vite
```

This starts a dev server at `http://localhost:5173/`.

---

### **11. Build for Production**
To build your static assets for production, run:

```bash
npx vite build
```

This generates the following structure in the `dist` folder:

```plaintext
dist/
├── index.html
├── assets/
│   ├── main-xxxxx.css
│   ├── main-xxxxx.js
```

The `xxxxx` in the filenames will be unique hashes for cache busting.

---

## **Deploy on Nginx**

Once you've built the project, the `dist/` folder contains static assets that can be directly served by **Nginx**.

### **12. Configure Nginx**
1. Install Nginx (if it isn’t already installed):

   ```bash
   sudo apt update
   sudo apt install nginx
   ```

2. Update the Nginx configuration file to serve the static files:

   Open the default Nginx configuration file:

   ```bash
   sudo nano /etc/nginx/sites-available/default
   ```

   Replace its content with:

   ```nginx
   server {
       listen 80;

       server_name your-domain.com;

       root /path/to/static-tailwind-vite/dist;
       index index.html;

       location / {
           try_files $uri $uri/ =404;
       }
   }
   ```

   Replace `/path/to/static-tailwind-vite/dist` with the absolute path to your `dist/` folder.

3. Restart Nginx:

   ```bash
   sudo systemctl restart nginx
   ```

---

## **Summary**
1. Install **Vite** and **Tailwind CSS** and configure them for your project.
2. Use `vite build` to generate static assets in the `dist/` folder.
3. Serve the resulting files using **Nginx**.

This workflow ensures your static page is entirely self-contained and can be served securely and efficiently by any static server like **Nginx**.