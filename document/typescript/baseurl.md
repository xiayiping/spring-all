In a React project using Vite, you can set a global base URL that can be easily applied throughout your project. Here’s how you can do it effectively:

---

### 1. **Define the Base URL in the Vite Config**
Vite allows you to define environment variables in `.env` files or directly in the `vite.config.js` file:

#### Add to `.env` or `.env.local`
Create a `.env` file in the root of your project and define a base URL variable:

```env
VITE_API_BASE_URL=https://example.com/api
```

> All environment variables used in Vite must be prefixed with `VITE_`.

---

### 2. **Access the Variable in Your Code**
You can access the environment variable using `import.meta.env` in your code:

```javascript
const baseUrl = import.meta.env.VITE_API_BASE_URL;
```

---

### 3. **Create a Utility for the Base URL**
To avoid importing and referencing `import.meta.env` in multiple places, you can create a utility file for the base URL.

#### Example: `src/config.js`
```javascript
const config = {
  baseUrl: import.meta.env.VITE_API_BASE_URL,
};

export default config;
```

Now, you can import `config` anywhere:

```javascript
import config from './config';

console.log(config.baseUrl); // Outputs the base URL
```

---

### 4. **Using the Base URL in Axios or Fetch**
If you’re using Axios, set the `baseURL` globally for all HTTP requests:

#### Example: `src/api/axiosInstance.js`
```javascript
import axios from 'axios';
import config from '../config';

const axiosInstance = axios.create({
  baseURL: config.baseUrl,
});

export default axiosInstance;
```

Now, you can use `axiosInstance` in your project:

```javascript
import axiosInstance from './api/axiosInstance';

axiosInstance.get('/users').then(response => {
  console.log(response.data);
});
```

---

### 5. **Dynamic Base URL for Different Environments**
You can define multiple environment files for different environments, like:

- `.env` for development
- `.env.production` for production

#### Example:
`.env`
```env
VITE_API_BASE_URL=http://localhost:3000/api
```

`.env.production`
```env
VITE_API_BASE_URL=https://example.com/api
```

Vite will automatically pick the correct file based on the `mode` (`development` or `production`) you’re running.

---

### 6. **Bonus: Shortcut for Import Paths**
If you want to simplify imports (e.g., `@/components/Button` instead of relative paths), set a `resolve.alias` in the `vite.config.js`:

#### Example: `vite.config.js`
```javascript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
});
```

Now, you can use the `@` alias for imports:

```javascript
import Button from '@/components/Button';
```

---

### Full Example
Here’s a complete example of a Vite project with:

- Base URL from `.env`
- Axios instance for HTTP requests
- Aliases for paths

#### `vite.config.js`
```javascript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
});
```

#### `.env`
```env
VITE_API_BASE_URL=https://example.com/api
```

#### `src/config.js`
```javascript
const config = {
  baseUrl: import.meta.env.VITE_API_BASE_URL,
};

export default config;
```

#### `src/api/axiosInstance.js`
```javascript
import axios from 'axios';
import config from '../config';

const axiosInstance = axios.create({
  baseURL: config.baseUrl,
});

export default axiosInstance;
```

#### Usage in a Component
```javascript
import React, { useEffect, useState } from 'react';
import axiosInstance from '@/api/axiosInstance';

const Users = () => {
  const [users, setUsers] = useState([]);

  useEffect(() => {
    axiosInstance.get('/users').then(response => {
      setUsers(response.data);
    });
  }, []);

  return (
    <div>
      <h1>Users</h1>
      <ul>
        {users.map(user => (
          <li key={user.id}>{user.name}</li>
        ))}
      </ul>
    </div>
  );
};

export default Users;
```

---

This approach ensures the base URL is configurable, reusable, and easy to maintain across your project!