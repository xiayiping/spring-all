# Combining React Layout with Link, Outlet & Components

## Basic Structure with React Router

### 1. Create a Reusable Layout Component

```jsx
// layouts/MainLayout.jsx
import { Outlet, Link } from 'react-router-dom';

function MainLayout() {
  return (
    <div className="min-h-screen">
      {/* Header */}
      <header className="bg-gray-800 text-white p-4">
        <nav className="flex gap-4">
          <Link to="/">Home</Link>
          <Link to="/about">About</Link>
          <Link to="/dashboard">Dashboard</Link>
        </nav>
      </header>

      {/* Main Content - Child routes render here */}
      <main className="p-4">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="bg-gray-800 text-white p-4">
        © 2024 My App
      </footer>
    </div>
  );
}

export default MainLayout;
```

### 2. Set Up Routes

```jsx
// App.jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import MainLayout from './layouts/MainLayout';
import DashboardLayout from './layouts/DashboardLayout';
import Home from './pages/Home';
import About from './pages/About';
import Dashboard from './pages/Dashboard';
import Settings from './pages/Settings';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Routes using MainLayout */}
        <Route path="/" element={<MainLayout />}>
          <Route index element={<Home />} />
          <Route path="about" element={<About />} />
          
          {/* Nested layout inside MainLayout */}
          <Route path="dashboard" element={<DashboardLayout />}>
            <Route index element={<Dashboard />} />
            <Route path="settings" element={<Settings />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
```

### 3. Create Nested Layout

```jsx
// layouts/DashboardLayout.jsx
import { Outlet, Link } from 'react-router-dom';

function DashboardLayout() {
  return (
    <div className="flex">
      {/* Sidebar */}
      <aside className="w-64 bg-gray-100 min-h-screen p-4">
        <h2 className="font-bold mb-4">Dashboard</h2>
        <nav className="flex flex-col gap-2">
          <Link to="/dashboard">Overview</Link>
          <Link to="/dashboard/settings">Settings</Link>
        </nav>
      </aside>

      {/* Dashboard Content */}
      <div className="flex-1 p-4">
        <Outlet />
      </div>
    </div>
  );
}

export default DashboardLayout;
```

## Visual Structure

```
MainLayout
├── Header (with Links)
├── <Outlet /> ─────────────────┐
│                               │
│   Renders either:             │
│   - Home                      │
│   - About                     │
│   - DashboardLayout           │
│       ├── Sidebar             │
│       └── <Outlet /> ─────┐   │
│                           │   │
│           Renders:        │   │
│           - Dashboard     │   │
│           - Settings      │   │
│                               │
└── Footer
```

## Reusable Layout Pattern with Props

```jsx
// layouts/PageLayout.jsx
import { Outlet } from 'react-router-dom';

function PageLayout({ title, sidebar = null }) {
  return (
    <div className="flex">
      {sidebar && (
        <aside className="w-64 bg-gray-100 p-4">
          {sidebar}
        </aside>
      )}
      <div className="flex-1">
        {title && <h1 className="text-2xl font-bold p-4">{title}</h1>}
        <Outlet />
      </div>
    </div>
  );
}

export default PageLayout;
```

```jsx
// Usage in routes
<Route 
  path="admin" 
  element={<PageLayout title="Admin Panel" sidebar={<AdminSidebar />} />}
>
  <Route index element={<AdminHome />} />
  <Route path="users" element={<Users />} />
</Route>
```

## Key Concepts Summary

| Concept | Purpose |
|---------|---------|
| `<Outlet />` | Placeholder where child routes render |
| `<Link to="">` | Navigation without page reload |
| Layout Component | Wrapper with shared UI (header, footer, sidebar) |
| Nested Routes | Child routes inherit parent layout |

## File Structure

```
src/
├── layouts/
│   ├── MainLayout.jsx
│   ├── DashboardLayout.jsx
│   └── AuthLayout.jsx
├── pages/
│   ├── Home.jsx
│   ├── About.jsx
│   └── Dashboard.jsx
├── components/
│   ├── Navbar.jsx
│   └── Sidebar.jsx
└── App.jsx
```

This pattern allows you to:
- ✅ Reuse layouts across multiple pages
- ✅ Nest layouts within each other
- ✅ Keep navigation consistent
- ✅ Avoid code duplication

# Active Link Styling with React Router

## Solution: Use `NavLink` Instead of `Link`

React Router provides `NavLink` which automatically handles active states.

## Basic Usage

```jsx
import { NavLink } from 'react-router-dom';

function Navbar() {
  return (
    <nav>
      <NavLink 
        to="/" 
        className={({ isActive }) => isActive ? 'current-url' : ''}
      >
        Home
      </NavLink>
      
      <NavLink 
        to="/about" 
        className={({ isActive }) => isActive ? 'current-url' : ''}
      >
        About
      </NavLink>
      
      <NavLink 
        to="/dashboard" 
        className={({ isActive }) => isActive ? 'current-url' : ''}
      >
        Dashboard
      </NavLink>
    </nav>
  );
}
```

## Cleaner Approach with Helper Function

```jsx
import { NavLink } from 'react-router-dom';

// Helper function
const getNavClass = ({ isActive }) => 
  isActive ? 'nav-link current-url' : 'nav-link';

function Navbar() {
  return (
    <nav>
      <NavLink to="/" className={getNavClass}>Home</NavLink>
      <NavLink to="/about" className={getNavClass}>About</NavLink>
      <NavLink to="/dashboard" className={getNavClass}>Dashboard</NavLink>
    </nav>
  );
}
```

## Reusable Custom NavLink Component

```jsx
// components/AppNavLink.jsx
import { NavLink } from 'react-router-dom';

function AppNavLink({ to, children, className = '', ...props }) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) => 
        `${className} ${isActive ? 'current-url' : ''}`.trim()
      }
      {...props}
    >
      {children}
    </NavLink>
  );
}

export default AppNavLink;
```

```jsx
// Usage
import AppNavLink from './components/AppNavLink';

function Navbar() {
  return (
    <nav>
      <AppNavLink to="/" className="nav-link">Home</AppNavLink>
      <AppNavLink to="/about" className="nav-link">About</AppNavLink>
      <AppNavLink to="/dashboard" className="nav-link">Dashboard</AppNavLink>
    </nav>
  );
}
```

## Handle Nested Routes with `end` Prop

```jsx
<NavLink 
  to="/" 
  end  // Only active when EXACTLY at "/"
  className={({ isActive }) => isActive ? 'current-url' : ''}
>
  Home
</NavLink>

<NavLink 
  to="/dashboard"  // Active for /dashboard, /dashboard/settings, etc.
  className={({ isActive }) => isActive ? 'current-url' : ''}
>
  Dashboard
</NavLink>
```

## With Tailwind CSS

```jsx
import { NavLink } from 'react-router-dom';

function Navbar() {
  return (
    <nav className="flex gap-4">
      <NavLink
        to="/"
        end
        className={({ isActive }) =>
          `px-4 py-2 rounded ${
            isActive 
              ? 'bg-blue-500 text-white current-url' 
              : 'text-gray-600 hover:bg-gray-100'
          }`
        }
      >
        Home
      </NavLink>
    </nav>
  );
}
```

## Using `style` Prop Instead

```jsx
<NavLink
  to="/about"
  style={({ isActive }) => ({
    color: isActive ? 'red' : 'black',
    fontWeight: isActive ? 'bold' : 'normal'
  })}
>
  About
</NavLink>
```

## Summary

| Prop | Description |
|------|-------------|
| `className` | Function receiving `{ isActive, isPending }` |
| `style` | Function receiving `{ isActive, isPending }` |
| `end` | Only match exact path (not children) |

```jsx
// Quick reference
<NavLink 
  to="/path"
  end
  className={({ isActive }) => isActive ? 'current-url' : ''}
>
  Link Text
</NavLink>
```

