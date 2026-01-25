# basic

# The Underlying Principles of Routing in React

## **Core Concept: Declarative Routing with History Management**

All React routers (React Router, Expo Router, Next.js App Router, TanStack Router, etc.) share the same fundamental principles, but differ in implementation details based on their platform (web, native, SSR).

---

## **Part 1: The Foundation - History API & URL Synchronization**

### **The Universal Pattern**

Every router has these core responsibilities:

1. **Listen to location changes** (URL changes)
2. **Match the current location to routes** (pattern matching)
3. **Render the matched component** (component resolution)
4. **Provide navigation APIs** (programmatic navigation)
5. **Manage navigation state** (history stack)

### **Web Platform: The History API**

React Router (web) is built on the browser's **History API**:

```javascript
// What happens under the hood when you navigate

// Browser's native History API
window.history.pushState(state, title, url);  // Add new entry
window.history.replaceState(state, title, url);  // Replace current
window.history.back();  // Go back
window.history.forward();  // Go forward

// Listen for navigation events
window.addEventListener('popstate', (event) => {
  // User clicked back/forward button
  console.log('Location changed:', window.location.pathname);
});

// For hash routing (#/about)
window.addEventListener('hashchange', (event) => {
  console.log('Hash changed:', window.location.hash);
});
```

### **How React Router Uses This**

```javascript
// Simplified version of how React Router works internally

import { useState, useEffect, createContext, useContext } from 'react';

// 1. Create a history abstraction
function createBrowserHistory() {
  const listeners = [];
  
  return {
    // Current location
    get location() {
      return {
        pathname: window.location.pathname,
        search: window.location.search,
        hash: window.location.hash,
        state: window.history.state
      };
    },
    
    // Navigate to new URL
    push(path, state) {
      window.history.pushState(state, '', path);
      // Notify all listeners about the change
      listeners.forEach(fn => fn(this.location));
    },
    
    // Replace current URL
    replace(path, state) {
      window.history.replaceState(state, '', path);
      listeners.forEach(fn => fn(this.location));
    },
    
    // Listen to changes
    listen(fn) {
      listeners.push(fn);
      
      // Handle browser back/forward
      const onPopState = () => fn(this.location);
      window.addEventListener('popstate', onPopState);
      
      return () => {
        const index = listeners.indexOf(fn);
        listeners.splice(index, 1);
        window.removeEventListener('popstate', onPopState);
      };
    },
    
    // Navigation methods
    back: () => window.history.back(),
    forward: () => window.history.forward(),
    go: (n) => window.history.go(n)
  };
}

// 2. Create React context to share history
const HistoryContext = createContext(null);
const LocationContext = createContext(null);

// 3. Router component - provides history to tree
function Router({ children, history }) {
  const [location, setLocation] = useState(history.location);
  
  useEffect(() => {
    // Subscribe to location changes
    const unlisten = history.listen((newLocation) => {
      setLocation(newLocation);
    });
    
    return unlisten;
  }, [history]);
  
  return (
    <HistoryContext.Provider value={history}>
      <LocationContext.Provider value={location}>
        {children}
      </LocationContext.Provider>
    </HistoryContext.Provider>
  );
}

// 4. BrowserRouter - creates browser history
function BrowserRouter({ children }) {
  const historyRef = useRef();
  
  if (!historyRef.current) {
    historyRef.current = createBrowserHistory();
  }
  
  return <Router history={historyRef.current}>{children}</Router>;
}

// 5. useNavigate hook - programmatic navigation
function useNavigate() {
  const history = useContext(HistoryContext);
  
  return useCallback((to, options = {}) => {
    if (options.replace) {
      history.replace(to);
    } else {
      history.push(to);
    }
  }, [history]);
}

// 6. useLocation hook - get current location
function useLocation() {
  return useContext(LocationContext);
}

// 7. Route matching - pattern to component mapping
function matchPath(pattern, pathname) {
  // Convert /user/:id to regex
  const paramNames = [];
  const regexPattern = pattern
    .replace(/:[^/]+/g, (match) => {
      paramNames.push(match.slice(1)); // Remove ':'
      return '([^/]+)'; // Match anything except /
    })
    .replace(/\*/g, '(.*)'); // * matches everything
  
  const regex = new RegExp(`^${regexPattern}$`);
  const match = pathname.match(regex);
  
  if (!match) return null;
  
  // Extract params
  const params = {};
  paramNames.forEach((name, index) => {
    params[name] = match[index + 1];
  });
  
  return { params, path: pathname };
}

// 8. Routes component - matches location to routes
function Routes({ children }) {
  const location = useLocation();
  
  // Find first matching route
  for (const child of React.Children.toArray(children)) {
    if (child.type === Route) {
      const match = matchPath(child.props.path, location.pathname);
      
      if (match) {
        // Render matched route with params
        return (
          <RouteContext.Provider value={match}>
            {child.props.element}
          </RouteContext.Provider>
        );
      }
    }
  }
  
  // No match found
  return null;
}

// 9. Route component - declarative route definition
function Route({ path, element }) {
  // This component doesn't render directly
  // It's used by Routes to define route structure
  return null;
}

// 10. Link component - declarative navigation
function Link({ to, children, replace = false, ...props }) {
  const navigate = useNavigate();
  
  const handleClick = (event) => {
    event.preventDefault(); // Don't do full page reload
    navigate(to, { replace });
  };
  
  return (
    <a href={to} onClick={handleClick} {...props}>
      {children}
    </a>
  );
}

// 11. useParams hook - access route params
const RouteContext = createContext(null);

function useParams() {
  const match = useContext(RouteContext);
  return match?.params || {};
}
```

### **Usage Example**

```jsx
function App() {
  return (
    <BrowserRouter>
      <nav>
        <Link to="/">Home</Link>
        <Link to="/about">About</Link>
        <Link to="/user/123">User 123</Link>
      </nav>
      
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/about" element={<About />} />
        <Route path="/user/:id" element={<UserProfile />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </BrowserRouter>
  );
}

function UserProfile() {
  const { id } = useParams();
  const navigate = useNavigate();
  
  return (
    <div>
      <h1>User Profile: {id}</h1>
      <button onClick={() => navigate('/about')}>
        Go to About
      </button>
    </div>
  );
}
```

---

## **Part 2: Platform-Specific Implementations**

### **React Router (Web) - Three History Types**

```javascript
// 1. Browser History - uses HTML5 pushState
// URLs: /about, /user/123
import { createBrowserRouter } from 'react-router-dom';

const router = createBrowserRouter([
  { path: '/', element: <Home /> },
  { path: '/about', element: <About /> }
]);

// Under the hood:
// - Uses window.history.pushState()
// - Listens to 'popstate' events
// - Requires server configuration (all routes → index.html)

// 2. Hash History - uses URL hash
// URLs: /#/about, /#/user/123
import { createHashRouter } from 'react-router-dom';

const router = createHashRouter([
  { path: '/', element: <Home /> },
  { path: '/about', element: <About /> }
]);

// Under the hood:
// - Changes window.location.hash
// - Listens to 'hashchange' events
// - No server config needed (# never sent to server)
// - Used when you can't control server (GitHub Pages, etc.)

// 3. Memory History - in-memory only (no URL change)
// Used for: testing, React Native, non-browser environments
import { createMemoryRouter } from 'react-router-dom';

const router = createMemoryRouter([
  { path: '/', element: <Home /> }
], {
  initialEntries: ['/about'], // Initial location
  initialIndex: 0
});

// Under the hood:
// - Maintains location in JavaScript array
// - No browser APIs used
// - Perfect for testing (no DOM required)
```

---

### **Expo Router (React Native) - File-System Based**

Expo Router uses a **file-system routing** paradigm (like Next.js):

```javascript
// File structure defines routes:
// app/
//   index.js          → /
//   about.js          → /about
//   user/[id].js      → /user/:id
//   (tabs)/
//     home.js         → /home (tab group)
//     profile.js      → /profile (tab group)

// Under the hood, Expo Router:

// 1. Scans the app/ directory at build time
// 2. Generates route manifest
const routeManifest = {
  '/': { file: './app/index.js', component: HomeScreen },
  '/about': { file: './app/about.js', component: AboutScreen },
  '/user/:id': { file: './app/user/[id].js', component: UserScreen }
};

// 3. Uses React Navigation under the hood
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

// Expo Router wraps React Navigation
const Stack = createNativeStackNavigator();

function ExpoRouter() {
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen name="index" component={HomeScreen} />
        <Stack.Screen name="about" component={AboutScreen} />
        <Stack.Screen name="user/[id]" component={UserScreen} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

// 4. Navigation uses native navigation APIs
// iOS: UINavigationController
// Android: Fragment transactions

// Example usage:
import { router } from 'expo-router';

function HomeScreen() {
  return (
    <View>
      <Text>Home</Text>
      <Button 
        title="Go to About"
        onPress={() => router.push('/about')} 
      />
    </View>
  );
}

// app/user/[id].js
import { useLocalSearchParams } from 'expo-router';

export default function UserScreen() {
  const { id } = useLocalSearchParams();
  return <Text>User: {id}</Text>;
}
```

**Key Differences from React Router:**

```javascript
// React Router (Web)
// - URLs in browser address bar
// - HTML5 History API
// - DOM-based (links are <a> tags)

// Expo Router (Native)
// - No browser, no URL bar
// - Native navigation stack (push/pop animations)
// - Native components (TouchableOpacity, not <a>)
// - Deep linking support (opens app from URL)

// Both share the same concepts:
// ✓ Declarative routing
// ✓ Pattern matching (/user/:id)
// ✓ Nested routes
// ✓ Navigation hooks
// ✓ Route parameters
```

---

### **MUI Toolpad - Hybrid Approach**

MUI Toolpad combines file-system routing with a visual builder:

```javascript
// Toolpad uses a hybrid approach:

// 1. File-system routing (like Expo Router)
// toolpad/
//   pages/
//     index.tsx       → /
//     customers.tsx   → /customers
//     orders/
//       [id].tsx      → /orders/:id

// 2. Visual editor generates code
// The visual builder creates React components:

// Auto-generated from visual editor:
export default function CustomersPage() {
  const { data } = useQuery();
  
  return (
    <DataGrid
      rows={data}
      columns={[
        { field: 'id', headerName: 'ID' },
        { field: 'name', headerName: 'Name' }
      ]}
    />
  );
}

// 3. Uses React Router internally
// Toolpad wraps routes with its own context:

function ToolpadRouter({ pages }) {
  return (
    <BrowserRouter>
      <ToolpadProvider>
        <Routes>
          {pages.map(page => (
            <Route 
              key={page.path} 
              path={page.path} 
              element={<page.component />} 
            />
          ))}
        </Routes>
      </ToolpadProvider>
    </BrowserRouter>
  );
}

// 4. Adds data binding layer on top
// Toolpad extends routing with data management:

function useToolpadNavigation() {
  const navigate = useNavigate();
  
  // Wraps navigation with data fetching
  return (to, options) => {
    // Pre-fetch data for destination route
    prefetchRouteData(to);
    navigate(to, options);
  };
}
```

---

## **Part 3: Advanced Patterns Common to All Routers**

### **1. Nested Routing**

All routers support nested routes (routes within routes):

```jsx
// React Router
function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<Home />} />
        <Route path="about" element={<About />} />
        <Route path="users" element={<UsersLayout />}>
          <Route index element={<UserList />} />
          <Route path=":id" element={<UserDetail />} />
        </Route>
      </Route>
    </Routes>
  );
}

function Layout() {
  return (
    <div>
      <nav>Nav Bar</nav>
      <Outlet /> {/* Child routes render here */}
      <footer>Footer</footer>
    </div>
  );
}

// Expo Router (file-system)
// app/
//   _layout.tsx         → Root layout
//   index.tsx           → /
//   users/
//     _layout.tsx       → Users layout
//     index.tsx         → /users
//     [id].tsx          → /users/:id

// app/_layout.tsx
export default function RootLayout() {
  return (
    <Stack>
      <Stack.Screen name="index" />
      <Stack.Screen name="users" />
    </Stack>
  );
}

// app/users/_layout.tsx
export default function UsersLayout() {
  return (
    <Stack>
      <Stack.Screen name="index" options={{ title: 'Users' }} />
      <Stack.Screen name="[id]" options={{ title: 'User Detail' }} />
    </Stack>
  );
}
```

### **2. Route Guards / Protected Routes**

```jsx
// Pattern used by all routers
function ProtectedRoute({ children }) {
  const { user } = useAuth();
  const location = useLocation();
  
  if (!user) {
    // Redirect to login, save intended destination
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  
  return children;
}

// Usage in React Router
<Routes>
  <Route path="/login" element={<Login />} />
  <Route 
    path="/dashboard" 
    element={
      <ProtectedRoute>
        <Dashboard />
      </ProtectedRoute>
    } 
  />
</Routes>

// Expo Router version
// app/_layout.tsx
import { useAuth } from './auth';
import { Redirect } from 'expo-router';

export default function RootLayout() {
  const { user } = useAuth();
  
  if (!user) {
    return <Redirect href="/login" />;
  }
  
  return <Stack />;
}
```

### **3. Data Loading / Loaders**

Modern routers support data loading before rendering:

```javascript
// React Router v6.4+ loaders
const router = createBrowserRouter([
  {
    path: '/user/:id',
    element: <UserProfile />,
    loader: async ({ params }) => {
      // Fetch data BEFORE rendering
      const user = await fetch(`/api/users/${params.id}`);
      return user.json();
    }
  }
]);

function UserProfile() {
  const user = useLoaderData(); // Access loaded data
  return <div>{user.name}</div>;
}

// Under the hood:
// 1. Router matches route: /user/123
// 2. Calls loader({ params: { id: '123' } })
// 3. Waits for promise to resolve
// 4. Renders component with data
// 5. Shows loading state during fetch

// Expo Router equivalent
// app/user/[id].tsx
import { useLocalSearchParams } from 'expo-router';
import { useSuspenseQuery } from '@tanstack/react-query';

export default function UserScreen() {
  const { id } = useLocalSearchParams();
  
  // Uses React Suspense for loading
  const { data: user } = useSuspenseQuery({
    queryKey: ['user', id],
    queryFn: () => fetch(`/api/users/${id}`).then(r => r.json())
  });
  
  return <Text>{user.name}</Text>;
}

// Wrap with Suspense boundary
// app/_layout.tsx
export default function Layout() {
  return (
    <Suspense fallback={<Loading />}>
      <Stack />
    </Suspense>
  );
}
```

### **4. Search Params / Query Strings**

```jsx
// React Router
import { useSearchParams } from 'react-router-dom';

function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  
  const query = searchParams.get('q');
  const page = searchParams.get('page') || '1';
  
  const updateSearch = (newQuery) => {
    setSearchParams({ q: newQuery, page: '1' });
  };
  
  return (
    <div>
      <input 
        value={query} 
        onChange={(e) => updateSearch(e.target.value)} 
      />
      <p>Page: {page}</p>
    </div>
  );
}

// Expo Router
import { useLocalSearchParams, router } from 'expo-router';

export default function SearchScreen() {
  const { q, page = '1' } = useLocalSearchParams();
  
  const updateSearch = (newQuery) => {
    router.setParams({ q: newQuery, page: '1' });
  };
  
  return (
    <View>
      <TextInput value={q} onChangeText={updateSearch} />
      <Text>Page: {page}</Text>
    </View>
  );
}
```

---

## **Part 4: The Pattern Matching Algorithm**

All routers use similar path matching logic:

```javascript
// Core pattern matching implementation

function createMatcher(pattern) {
  // 1. Extract parameter names
  const paramNames = [];
  
  // 2. Convert pattern to regex
  // /user/:id/posts/:postId → /user/([^/]+)/posts/([^/]+)
  const regexPattern = pattern
    .split('/')
    .map(segment => {
      // Static segment
      if (!segment.startsWith(':') && segment !== '*') {
        return segment;
      }
      
      // Named parameter (:id)
      if (segment.startsWith(':')) {
        paramNames.push(segment.slice(1));
        return '([^/]+)'; // Match non-slash chars
      }
      
      // Wildcard (*)
      if (segment === '*') {
        paramNames.push('*');
        return '(.*)'; // Match anything
      }
    })
    .join('/');
  
  const regex = new RegExp(`^${regexPattern}$`);
  
  return (pathname) => {
    const match = pathname.match(regex);
    
    if (!match) return null;
    
    // Extract params
    const params = {};
    paramNames.forEach((name, index) => {
      params[name] = match[index + 1];
    });
    
    return {
      params,
      pathname,
      pattern
    };
  };
}

// Usage
const matcher = createMatcher('/user/:id/posts/:postId');

console.log(matcher('/user/123/posts/456'));
// { params: { id: '123', postId: '456' }, pathname: '...', pattern: '...' }

console.log(matcher('/user/123'));
// null (doesn't match)

// Route ranking (specificity)
// More specific routes match first:
function rankRoute(pattern) {
  const segments = pattern.split('/').filter(Boolean);
  let score = 0;
  
  for (const segment of segments) {
    if (segment === '*') {
      score += 1; // Wildcard lowest priority
    } else if (segment.startsWith(':')) {
      score += 10; // Dynamic segment medium priority
    } else {
      score += 100; // Static segment highest priority
    }
  }
  
  return score;
}

// Example ranking:
console.log(rankRoute('/'));                    // 0
console.log(rankRoute('/*'));                   // 1
console.log(rankRoute('/user/:id'));            // 110 (100 + 10)
console.log(rankRoute('/user/123'));            // 200 (100 + 100)
console.log(rankRoute('/user/:id/posts/:post')); // 220

// Routes are sorted by rank (highest first)
const routes = [
  { path: '/*', rank: 1 },
  { path: '/user/:id', rank: 110 },
  { path: '/user/profile', rank: 200 }
].sort((a, b) => b.rank - a.rank);

// When matching /user/profile:
// 1. Tries /user/profile → MATCH! (200)
// 2. Never tries /user/:id or /*
```

---

## **Part 5: State Management in Routing**

### **Location State (Passed via Navigation)**

```jsx
// React Router
function ProductList() {
  const navigate = useNavigate();
  
  return (
    <button onClick={() => 
      navigate('/product/123', { 
        state: { from: 'search', query: 'laptop' }
      })
    }>
      View Product
    </button>
  );
}

function ProductDetail() {
  const location = useLocation();
  const { from, query } = location.state || {};
  
  return (
    <div>
      <button onClick={() => navigate(-1)}>
        Back to {from} ({query})
      </button>
    </div>
  );
}

// Under the hood:
window.history.pushState(
  { from: 'search', query: 'laptop' }, // State object
  '',
  '/product/123'
);

// Accessed via:
window.history.state; // { from: 'search', query: 'laptop' }
```

### **Scroll Restoration**

```jsx
// React Router handles scroll position
function ScrollToTop() {
  const { pathname } = useLocation();
  
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);
  
  return null;
}

// Or use built-in ScrollRestoration
import { ScrollRestoration } from 'react-router-dom';

function App() {
  return (
    <>
      <ScrollRestoration />
      <Routes>...</Routes>
    </>
  );
}

// Under the hood:
// 1. Saves scroll position before navigation
const scrollPositions = new Map();

function saveScrollPosition(key) {
  scrollPositions.set(key, {
    x: window.scrollX,
    y: window.scrollY
  });
}

// 2. Restores on back/forward
function restoreScrollPosition(key) {
  const position = scrollPositions.get(key);
  if (position) {
    window.scrollTo(position.x, position.y);
  } else {
    window.scrollTo(0, 0);
  }
}
```

---

## **Part 6: Deep Linking (Mobile)**

Expo Router and React Navigation support deep linking:

```javascript
// Expo Router configuration
// app.json
{
  "expo": {
    "scheme": "myapp",
    "android": {
      "intentFilters": [
        {
          "action": "VIEW",
          "data": [
            { "scheme": "https", "host": "myapp.com" }
          ]
        }
      ]
    }
  }
}

// When user opens: myapp://user/123
// or https://myapp.com/user/123

// Expo Router automatically navigates to:
// app/user/[id].tsx

export default function UserScreen() {
  const { id } = useLocalSearchParams(); // "123"
  return <Text>User: {id}</Text>;
}

// Manual deep link handling:
import * as Linking from 'expo-linking';

Linking.addEventListener('url', ({ url }) => {
  // url: "myapp://user/123"
  const { hostname, path, queryParams } = Linking.parse(url);
  
  // Navigate based on URL
  router.push(path);
});
```

---

## **Summary: The Universal Routing Pattern**

```javascript
// Every React router follows this pattern:

class UniversalRouter {
  // 1. History Management
  history = {
    push(path, state) { /* Add to stack */ },
    replace(path, state) { /* Replace current */ },
    back() { /* Go back */ },
    listen(callback) { /* Subscribe to changes */ }
  };
  
  // 2. Location Tracking
  getCurrentLocation() {
    return {
      pathname: '/user/123',
      search: '?tab=posts',
      hash: '#comments',
      state: { from: 'home' }
    };
  }
  
  // 3. Route Matching
  matchRoute(pathname, routes) {
    for (const route of routes) {
      const match = this.matchPattern(route.path, pathname);
      if (match) return { route, match };
    }
    return null;
  }
  
  // 4. Component Resolution
  resolveComponent(match) {
    return match.route.component;
  }
  
  // 5. React Integration
  render() {
    const location = this.getCurrentLocation();
    const match = this.matchRoute(location.pathname, routes);
    const Component = this.resolveComponent(match);
    
    return (
      <LocationContext.Provider value={location}>
        <MatchContext.Provider value={match}>
          <Component />
        </MatchContext.Provider>
      </LocationContext.Provider>
    );
  }
}
```

**Platform Adaptations:**
- **Web (React Router)**: Uses `window.history` and `popstate` events
- **Native (Expo Router)**: Uses `react-navigation` stack and native navigation
- **SSR (Next.js)**: Handles server-side and client-side routing
- **Hybrid (Toolpad)**: Combines file-system with visual builder

**But the core principles remain the same:**
1. **Declarative route definitions** (what to show where)
2. **URL/path matching** (regex-based pattern matching)
3. **Component rendering** (mount matched component)
4. **Navigation APIs** (programmatic navigation hooks)
5. **Context propagation** (location, params, navigation state)

All routers are just different implementations of these five principles, adapted to their specific platform constraints and features!