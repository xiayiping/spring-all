# Get Path Parameter from URL in React Router

Yes! Use the `useParams` hook to extract the `123` from `/user/123`.

## Quick Example

### 1. Define Route

```jsx
// App.jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import User from './pages/User';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/user/:id" element={<User />} />
      </Routes>
    </BrowserRouter>
  );
}
```

### 2. Get the ID with `useParams`

```jsx
// pages/User.jsx
import { useParams } from 'react-router-dom';

const User = () => {
  const { id } = useParams();

  // URL: /user/123
  // id = "123" (string)

  return (
    <div>
      <h1>User ID: {id}</h1>
    </div>
  );
};

export default User;
```

## Important Notes

```jsx
import { useParams } from 'react-router-dom';

const User = () => {
  const { id } = useParams();

  // ⚠️ id is always a STRING
  console.log(typeof id);  // "string"

  // Convert to number if needed
  const userId = Number(id);
  // or
  const userId = parseInt(id, 10);

  return <h1>User ID: {userId}</h1>;
};
```

## Complete Working Example

```jsx
import { useParams, Link } from 'react-router-dom';
import { useState, useEffect } from 'react';

const User = () => {
  const { id } = useParams();
  const [user, setUser] = useState(null);

  useEffect(() => {
    // Fetch user data using the id
    fetch(`https://jsonplaceholder.typicode.com/users/${id}`)
      .then(res => res.json())
      .then(data => setUser(data));
  }, [id]);

  if (!user) return <p>Loading...</p>;

  return (
    <div>
      <h1>{user.name}</h1>
      <p>ID: {id}</p>
      <p>Email: {user.email}</p>
      
      {/* Link to other users */}
      <Link to={`/user/${Number(id) + 1}`}>Next User</Link>
    </div>
  );
};
```

That's it! Just use `useParams()` to get the ID from the URL. 🎉
