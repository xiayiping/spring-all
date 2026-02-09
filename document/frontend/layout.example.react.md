# React Layout with Tailwind CSS

Here's the complete React project structure with layout and components.

---

## Project Structure

```
my-react-app/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   ├── layout/
│   │   │   ├── Layout.jsx
│   │   │   ├── Header.jsx
│   │   │   └── Sidebar.jsx
│   │   ├── Dashboard.jsx
│   │   └── User.jsx
│   ├── App.jsx
│   ├── main.jsx
│   └── index.css
├── package.json
└── tailwind.config.js
```

---

## 1. Setup Files

### `index.css`

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  :root {
    --brand-primary: 79 70 229;
    --brand-primary-dark: 67 56 202;
    --brand-primary-light: 99 102 241;
  }
}

/* Custom scrollbar */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 3px;
}
```

### `tailwind.config.js`

```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          DEFAULT: 'rgb(var(--brand-primary) / <alpha-value>)',
          dark: 'rgb(var(--brand-primary-dark) / <alpha-value>)',
          light: 'rgb(var(--brand-primary-light) / <alpha-value>)',
        }
      }
    },
  },
  plugins: [],
}
```

---

## 2. Main Entry Files

### `main.jsx`

```jsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
```

### `App.jsx`

```jsx
import { useState } from 'react'
import Layout from './components/layout/Layout'
import Dashboard from './components/Dashboard'
import User from './components/User'

function App() {
  const [currentPage, setCurrentPage] = useState('dashboard')

  const renderPage = () => {
    switch (currentPage) {
      case 'dashboard':
        return <Dashboard />
      case 'users':
        return <User />
      default:
        return <Dashboard />
    }
  }

  return (
    <Layout currentPage={currentPage} setCurrentPage={setCurrentPage}>
      {renderPage()}
    </Layout>
  )
}

export default App
```

---

## 3. Layout Components

### `components/layout/Layout.jsx`

```jsx
import { useState } from 'react'
import Header from './Header'
import Sidebar from './Sidebar'

function Layout({ children, currentPage, setCurrentPage }) {
  const [sidebarOpen, setSidebarOpen] = useState(false)

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen)
  }

  return (
    <div className="min-h-screen flex flex-col bg-gray-100">
      {/* Header */}
      <Header toggleSidebar={toggleSidebar} />

      {/* Main Container */}
      <div className="flex flex-1 overflow-hidden pt-14">
        {/* Sidebar */}
        <Sidebar 
          isOpen={sidebarOpen} 
          toggleSidebar={toggleSidebar}
          currentPage={currentPage}
          setCurrentPage={setCurrentPage}
        />

        {/* Overlay for mobile */}
        {sidebarOpen && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 z-10 lg:hidden"
            onClick={toggleSidebar}
          />
        )}

        {/* Main Content */}
        <main className="flex-1 overflow-y-auto">
          {children}
        </main>
      </div>
    </div>
  )
}

export default Layout
```

### `components/layout/Header.jsx`

```jsx
function Header({ toggleSidebar }) {
  return (
    <header className="bg-brand text-white shadow-lg z-30 fixed w-full">
      <div className="px-4 py-3 flex items-center justify-between">
        {/* Mobile Menu Button */}
        <button
          onClick={toggleSidebar}
          className="lg:hidden p-2 rounded-md hover:bg-brand-dark transition"
        >
          <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>

        {/* Brand Logo */}
        <div className="flex items-center space-x-3">
          <div className="w-8 h-8 bg-white rounded-lg flex items-center justify-center">
            <span className="text-brand font-bold text-xl">B</span>
          </div>
          <span className="text-xl font-bold hidden sm:block">Brand Name</span>
        </div>

        {/* Right Side - User Menu */}
        <div className="flex items-center space-x-4">
          {/* Notifications */}
          <button className="p-2 rounded-full hover:bg-brand-dark transition relative">
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
            </svg>
            <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
          </button>

          {/* User Avatar */}
          <div className="flex items-center space-x-2">
            <img
              src="https://ui-avatars.com/api/?name=John+Doe&background=fff&color=4f46e5"
              alt="User"
              className="w-8 h-8 rounded-full border-2 border-white"
            />
            <span className="hidden md:block text-sm">John Doe</span>
          </div>
        </div>
      </div>
    </header>
  )
}

export default Header
```

### `components/layout/Sidebar.jsx`

```jsx
function Sidebar({ isOpen, toggleSidebar, currentPage, setCurrentPage }) {
  const menuItems = [
    { id: 'dashboard', name: 'Dashboard', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
    { id: 'users', name: 'Users', icon: 'M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z' },
    { id: 'projects', name: 'Projects', icon: 'M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z' },
    { id: 'analytics', name: 'Analytics', icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z' },
  ]

  const settingsItems = [
    { id: 'settings', name: 'Settings', icon: 'M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z M15 12a3 3 0 11-6 0 3 3 0 016 0z' },
    { id: 'help', name: 'Help', icon: 'M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z' },
  ]

  const handleMenuClick = (id) => {
    setCurrentPage(id)
    // Close sidebar on mobile after selection
    if (window.innerWidth < 1024) {
      toggleSidebar()
    }
  }

  return (
    <aside
      className={`
        fixed lg:static inset-y-0 left-0 z-20
        w-64 bg-white shadow-xl
        transform transition-transform duration-300 ease-in-out
        flex flex-col
        ${isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
      `}
    >
      {/* Navigation Menu */}
      <nav className="flex-1 px-4 py-6 overflow-y-auto">
        {/* Main Menu */}
        <div className="mb-6">
          <h3 className="px-3 text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">
            Main Menu
          </h3>
          <ul className="space-y-1">
            {menuItems.map((item) => (
              <li key={item.id}>
                <button
                  onClick={() => handleMenuClick(item.id)}
                  className={`
                    w-full flex items-center px-3 py-2 rounded-lg transition
                    ${currentPage === item.id
                      ? 'text-brand bg-brand/10 border-l-4 border-brand'
                      : 'text-gray-600 hover:bg-gray-100'
                    }
                  `}
                >
                  <svg className="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d={item.icon} />
                  </svg>
                  <span className={currentPage === item.id ? 'font-medium' : ''}>
                    {item.name}
                  </span>
                </button>
              </li>
            ))}
          </ul>
        </div>

        {/* Settings Menu */}
        <div className="mb-6">
          <h3 className="px-3 text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">
            Settings
          </h3>
          <ul className="space-y-1">
            {settingsItems.map((item) => (
              <li key={item.id}>
                <button
                  onClick={() => handleMenuClick(item.id)}
                  className={`
                    w-full flex items-center px-3 py-2 rounded-lg transition
                    ${currentPage === item.id
                      ? 'text-brand bg-brand/10 border-l-4 border-brand'
                      : 'text-gray-600 hover:bg-gray-100'
                    }
                  `}
                >
                  <svg className="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d={item.icon} />
                  </svg>
                  <span>{item.name}</span>
                </button>
              </li>
            ))}
          </ul>
        </div>
      </nav>

      {/* Sidebar Footer */}
      <div className="p-4 border-t">
        <button className="w-full flex items-center px-3 py-2 text-red-600 hover:bg-red-50 rounded-lg transition">
          <svg className="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
          </svg>
          <span>Logout</span>
        </button>
      </div>
    </aside>
  )
}

export default Sidebar
```

---

## 4. Page Components

### `components/Dashboard.jsx`

```jsx
function Dashboard() {
  const stats = [
    { title: 'Total Users', value: '12,345', change: '+12%', positive: true, color: 'indigo', icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z' },
    { title: 'Revenue', value: '$54,321', change: '+8%', positive: true, color: 'green', icon: 'M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z' },
    { title: 'Orders', value: '1,234', change: '-3%', positive: false, color: 'yellow', icon: 'M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z' },
    { title: 'Bounce Rate', value: '23.5%', change: '-5%', positive: true, color: 'red', icon: 'M13 7h8m0 0v8m0-8l-8 8-4-4-6 6' },
  ]

  const orders = [
    { id: '#ORD-001', customer: 'Alice Johnson', status: 'Completed', statusColor: 'green', amount: '$250.00' },
    { id: '#ORD-002', customer: 'Bob Smith', status: 'Pending', statusColor: 'yellow', amount: '$180.00' },
    { id: '#ORD-003', customer: 'Carol White', status: 'Processing', statusColor: 'blue', amount: '$420.00' },
    { id: '#ORD-004', customer: 'David Brown', status: 'Cancelled', statusColor: 'red', amount: '$95.00' },
  ]

  const activities = [
    { text: 'New user registered', time: '2 minutes ago', color: 'indigo' },
    { text: 'Order #ORD-001 completed', time: '15 minutes ago', color: 'green' },
    { text: 'Payment received', time: '1 hour ago', color: 'yellow' },
    { text: 'Server alert triggered', time: '3 hours ago', color: 'red' },
    { text: 'Backup completed', time: '5 hours ago', color: 'gray' },
  ]

  const colorClasses = {
    indigo: { bg: 'bg-indigo-100', text: 'text-indigo-600', border: 'border-indigo-500' },
    green: { bg: 'bg-green-100', text: 'text-green-600', border: 'border-green-500' },
    yellow: { bg: 'bg-yellow-100', text: 'text-yellow-600', border: 'border-yellow-500' },
    red: { bg: 'bg-red-100', text: 'text-red-600', border: 'border-red-500' },
    blue: { bg: 'bg-blue-100', text: 'text-blue-600', border: 'border-blue-500' },
    gray: { bg: 'bg-gray-100', text: 'text-gray-600', border: 'border-gray-500' },
  }

  return (
    <div>
      {/* Page Header */}
      <div className="bg-white shadow-sm">
        <div className="px-6 py-4">
          <h1 className="text-2xl font-bold text-gray-800">Dashboard</h1>
          <p className="text-gray-500 text-sm">Welcome back, John!</p>
        </div>
      </div>

      {/* Content */}
      <div className="p-6">
        {/* Stats Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
          {stats.map((stat, index) => (
            <div
              key={index}
              className={`bg-white rounded-xl shadow-sm p-6 border-l-4 ${colorClasses[stat.color].border}`}
            >
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-gray-500 text-sm">{stat.title}</p>
                  <p className="text-2xl font-bold text-gray-800">{stat.value}</p>
                </div>
                <div className={`w-12 h-12 ${colorClasses[stat.color].bg} rounded-full flex items-center justify-center`}>
                  <svg className={`w-6 h-6 ${colorClasses[stat.color].text}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d={stat.icon} />
                  </svg>
                </div>
              </div>
              <p className={`text-sm mt-2 ${stat.positive ? 'text-green-500' : 'text-red-500'}`}>
                {stat.positive ? '↑' : '↓'} {stat.change} from last month
              </p>
            </div>
          ))}
        </div>

        {/* Main Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Table */}
          <div className="lg:col-span-2 bg-white rounded-xl shadow-sm">
            <div className="p-6 border-b">
              <h2 className="text-lg font-semibold text-gray-800">Recent Orders</h2>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Order ID</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Customer</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Amount</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {orders.map((order, index) => (
                    <tr key={index} className="hover:bg-gray-50">
                      <td className="px-6 py-4 text-sm text-gray-800">{order.id}</td>
                      <td className="px-6 py-4 text-sm text-gray-600">{order.customer}</td>
                      <td className="px-6 py-4">
                        <span className={`px-2 py-1 text-xs font-medium ${colorClasses[order.statusColor].bg} ${colorClasses[order.statusColor].text} rounded-full`}>
                          {order.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-800">{order.amount}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Activity */}
          <div className="bg-white rounded-xl shadow-sm">
            <div className="p-6 border-b">
              <h2 className="text-lg font-semibold text-gray-800">Recent Activity</h2>
            </div>
            <div className="p-6 space-y-4">
              {activities.map((activity, index) => (
                <div key={index} className="flex items-start space-x-3">
                  <div className={`w-2 h-2 mt-2 bg-${activity.color}-500 rounded-full`}></div>
                  <div>
                    <p className="text-sm text-gray-800">{activity.text}</p>
                    <p className="text-xs text-gray-500">{activity.time}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Dashboard
```

### `components/User.jsx`

```jsx
import { useState } from 'react'

function User() {
  const [users, setUsers] = useState([
    { id: 1, username: 'john_doe', birthday: '1990-05-15' },
    { id: 2, username: 'jane_smith', birthday: '1988-12-03' },
    { id: 3, username: 'bob_wilson', birthday: '1995-08-22' },
  ])

  const [newUser, setNewUser] = useState({ username: '', birthday: '' })
  const [editingId, setEditingId] = useState(null)

  // Format date for display
  const formatDate = (dateString) => {
    const options = { year: 'numeric', month: 'long', day: 'numeric' }
    return new Date(dateString).toLocaleDateString('en-US', options)
  }

  // Calculate age
  const calculateAge = (birthday) => {
    const today = new Date()
    const birthDate = new Date(birthday)
    let age = today.getFullYear() - birthDate.getFullYear()
    const monthDiff = today.getMonth() - birthDate.getMonth()
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--
    }
    return age
  }

  // Add new user
  const handleAddUser = (e) => {
    e.preventDefault()
    if (newUser.username && newUser.birthday) {
      setUsers([
        ...users,
        {
          id: Date.now(),
          username: newUser.username,
          birthday: newUser.birthday,
        },
      ])
      setNewUser({ username: '', birthday: '' })
    }
  }

  // Delete user
  const handleDeleteUser = (id) => {
    setUsers(users.filter((user) => user.id !== id))
  }

  // Start editing
  const handleEditUser = (user) => {
    setEditingId(user.id)
    setNewUser({ username: user.username, birthday: user.birthday })
  }

  // Save edit
  const handleSaveEdit = (id) => {
    setUsers(
      users.map((user) =>
        user.id === id
          ? { ...user, username: newUser.username, birthday: newUser.birthday }
          : user
      )
    )
    setEditingId(null)
    setNewUser({ username: '', birthday: '' })
  }

  // Cancel edit
  const handleCancelEdit = () => {
    setEditingId(null)
    setNewUser({ username: '', birthday: '' })
  }

  return (
    <div>
      {/* Page Header */}
      <div className="bg-white shadow-sm">
        <div className="px-6 py-4">
          <h1 className="text-2xl font-bold text-gray-800">Users</h1>
          <p className="text-gray-500 text-sm">Manage user information</p>
        </div>
      </div>

      {/* Content */}
      <div className="p-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Add User Form */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h2 className="text-lg font-semibold text-gray-800 mb-4">
                {editingId ? 'Edit User' : 'Add New User'}
              </h2>
              <form onSubmit={editingId ? (e) => { e.preventDefault(); handleSaveEdit(editingId) } : handleAddUser}>
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Username
                  </label>
                  <input
                    type="text"
                    value={newUser.username}
                    onChange={(e) => setNewUser({ ...newUser, username: e.target.value })}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-brand focus:border-transparent outline-none transition"
                    placeholder="Enter username"
                    required
                  />
                </div>
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Birthday
                  </label>
                  <input
                    type="date"
                    value={newUser.birthday}
                    onChange={(e) => setNewUser({ ...newUser, birthday: e.target.value })}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-brand focus:border-transparent outline-none transition"
                    required
                  />
                </div>
                <div className="flex space-x-2">
                  <button
                    type="submit"
                    className="flex-1 bg-brand text-white py-2 px-4 rounded-lg hover:bg-brand-dark transition"
                  >
                    {editingId ? 'Save Changes' : 'Add User'}
                  </button>
                  {editingId && (
                    <button
                      type="button"
                      onClick={handleCancelEdit}
                      className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
                    >
                      Cancel
                    </button>
                  )}
                </div>
              </form>
            </div>
          </div>

          {/* User List */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-xl shadow-sm">
              <div className="p-6 border-b">
                <h2 className="text-lg font-semibold text-gray-800">User List</h2>
                <p className="text-sm text-gray-500">{users.length} users total</p>
              </div>

              {users.length === 0 ? (
                <div className="p-6 text-center text-gray-500">
                  No users found. Add a new user to get started.
                </div>
              ) : (
                <div className="divide-y divide-gray-200">
                  {users.map((user) => (
                    <div
                      key={user.id}
                      className="p-6 flex items-center justify-between hover:bg-gray-50 transition"
                    >
                      <div className="flex items-center space-x-4">
                        {/* Avatar */}
                        <div className="w-12 h-12 bg-brand/10 rounded-full flex items-center justify-center">
                          <span className="text-brand font-bold text-lg">
                            {user.username.charAt(0).toUpperCase()}
                          </span>
                        </div>
                        {/* User Info */}
                        <div>
                          <h3 className="text-lg font-medium text-gray-800">
                            {user.username}
                          </h3>
                          <div className="flex items-center space-x-4 text-sm text-gray-500">
                            <span className="flex items-center">
                              <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                              </svg>
                              {formatDate(user.birthday)}
                            </span>
                            <span className="flex items-center">
                              <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                              </svg>
                              {calculateAge(user.birthday)} years old
                            </span>
                          </div>
                        </div>
                      </div>
                      {/* Actions */}
                      <div className="flex space-x-2">
                        <button
                          onClick={() => handleEditUser(user)}
                          className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition"
                          title="Edit"
                        >
                          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                          </svg>
                        </button>
                        <button
                          onClick={() => handleDeleteUser(user.id)}
                          className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition"
                          title="Delete"
                        >
                          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7
