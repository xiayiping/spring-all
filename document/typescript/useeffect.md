# use effect

To fetch data based on a parameter from the URL when a React component loads, you can use the `useParams` hook from `react-router-dom` alongside the `useEffect` and `useState` hooks. Here's an example of how you can achieve this:

### Example: Fetch Data Using `useParams`

```jsx
import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';

const DataFetchingComponent = () => {
  const { id } = useParams(); // Retrieve the `id` parameter from the URL
  const [data, setData] = useState(null); // State to store the fetched data
  const [loading, setLoading] = useState(true); // State to manage loading state
  const [error, setError] = useState(null); // State to manage errors

  useEffect(() => {
    // Fetch data when the component is mounted or when `id` changes
    const fetchData = async () => {
      try {
        setLoading(true); // Start loading
        const response = await fetch(`https://api.example.com/data/${id}`); // Adjust the URL as needed
        if (!response.ok) {
          throw new Error(`Error: ${response.statusText}`);
        }
        const result = await response.json();
        setData(result); // Set the fetched data
      } catch (err) {
        setError(err.message); // Handle any errors
      } finally {
        setLoading(false); // Stop loading
      }
    };

    if (id) {
      fetchData(); // Only fetch data if `id` is defined
    }
  }, [id]); // Dependency array ensures the effect runs when `id` changes

  // Handle loading, error, and data states
  if (loading) return <p>Loading...</p>;
  if (error) return <p>Error: {error}</p>;
  if (!data) return <p>No data found.</p>;

  return (
    <div>
      <h1>Data for ID: {id}</h1>
      <pre>{JSON.stringify(data, null, 2)}</pre>
    </div>
  );
};

export default DataFetchingComponent;
```

### Key Points
1. **`useParams`**:
   - Extracts parameters from the current route. In this example, `id` is a route parameter defined in your route configuration (e.g., `/data/:id`).

2. **`useEffect`**:
   - Triggers the data-fetching logic when the component mounts or when the `id` parameter changes.

3. **`fetch`**:
   - Performs the HTTP request to fetch data based on the `id`.

4. **State Management**:
   - `data`: Stores the fetched data.
   - `loading`: Tracks whether the data is being loaded.
   - `error`: Captures any error that occurs during the fetch.

5. **Conditional Rendering**:
   - Displays the appropriate UI for loading, error, or successfully fetched data.

### Example Route Setup
Make sure you have a route that provides the `id` parameter:

```jsx
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import DataFetchingComponent from './DataFetchingComponent';

const App = () => (
  <Router>
    <Routes>
      <Route path="/data/:id" element={<DataFetchingComponent />} />
    </Routes>
  </Router>
);

export default App;
```

### How It Works
1. When the component is loaded at a route like `/data/123`, `useParams` retrieves the `id` value (`123`).
2. `useEffect` triggers the `fetchData` function, which fetches data from the API endpoint (e.g., `https://api.example.com/data/123`).
3. The fetched data is stored in the `data` state and displayed in the component.

This pattern is a common way to handle data fetching in React components based on URL parameters.