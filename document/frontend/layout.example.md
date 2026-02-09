# Responsive Layout with Tailwind CSS

Here's a complete responsive layout with navigation, brand bar, and content area.

---

## Full Code

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Responsive Layout</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <style>
    /* Custom scrollbar */
    ::-webkit-scrollbar {
      width: 6px;
    }
    ::-webkit-scrollbar-thumb {
      background-color: #cbd5e1;
      border-radius: 3px;
    }
  </style>
</head>
<body class="bg-gray-100">

  <div class="min-h-screen flex flex-col">
    
    <!-- ========== Brand Bar (Top) ========== -->
    <header class="bg-indigo-600 text-white shadow-lg z-30">
      <div class="px-4 py-3 flex items-center justify-between">
        
        <!-- Mobile Menu Button -->
        <button 
          id="menuToggle"
          class="lg:hidden p-2 rounded-md hover:bg-indigo-700 transition"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M4 6h16M4 12h16M4 18h16"/>
          </svg>
        </button>
        
        <!-- Brand Logo -->
        <div class="flex items-center space-x-3">
          <div class="w-8 h-8 bg-white rounded-lg flex items-center justify-center">
            <span class="text-indigo-600 font-bold text-xl">B</span>
          </div>
          <span class="text-xl font-bold hidden sm:block">Brand Name</span>
        </div>
        
        <!-- Right Side - User Menu -->
        <div class="flex items-center space-x-4">
          <!-- Notifications -->
          <button class="p-2 rounded-full hover:bg-indigo-700 transition relative">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
            </svg>
            <span class="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
          </button>
          
          <!-- User Avatar -->
          <div class="flex items-center space-x-2">
            <img 
              src="https://ui-avatars.com/api/?name=John+Doe&background=fff&color=4f46e5" 
              alt="User" 
              class="w-8 h-8 rounded-full border-2 border-white"
            >
            <span class="hidden md:block text-sm">John Doe</span>
          </div>
        </div>
        
      </div>
    </header>

    <!-- ========== Main Container ========== -->
    <div class="flex flex-1 overflow-hidden">
      
      <!-- ========== Sidebar Navigation ========== -->
      <aside 
        id="sidebar"
        class="fixed lg:static inset-y-0 left-0 z-20
               w-64 bg-white shadow-xl
               transform -translate-x-full lg:translate-x-0
               transition-transform duration-300 ease-in-out
               flex flex-col
               mt-[56px] lg:mt-0"
      >
        
        <!-- Navigation Menu -->
        <nav class="flex-1 px-4 py-6 overflow-y-auto">
          
          <!-- Menu Section -->
          <div class="mb-6">
            <h3 class="px-3 text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">
              Main Menu
            </h3>
            <ul class="space-y-1">
              <li>
                <a href="#" class="flex items-center px-3 py-2 text-gray-700 bg-indigo-50 
                                   rounded-lg border-l-4 border-indigo-600">
                  <svg class="w-5 h-5 mr-3 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>
                  </svg>
                  <span class="font-medium">Dashboard</span>
                </a>
              </li>
              <li>
                <a href="#" class="flex items-center px-3 py-2 text-gray-600 
                                   hover:bg-gray-100 rounded-lg transition">
                  <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                  </svg>
                  <span>Users</span>
                </a>
              </li>
              <li>
                <a href="#" class="flex items-center px-3 py-2 text-gray-600 
                                   hover:bg-gray-100 rounded-lg transition">
                  <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/>
                  </svg>
                  <span>Projects</span>
                </a>
              </li>
              <li>
                <a href="#" class="flex items-center px-3 py-2 text-gray-600 
                                   hover:bg-gray-100 rounded-lg transition">
                  <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
                  </svg>
                  <span>Analytics</span>
                </a>
              </li>
              <li>
                <a href="#" class="flex items-center px-3 py-2 text-gray-600 
                                   hover:bg-gray-100 rounded-lg transition">
                  <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                  </svg>
                  <span>Calendar</span>
                </a>
              </li>
            </ul>
          </div>
          
          <!-- Settings Section -->
          <div class="mb-6">
            <h3 class="px-3 text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">
              Settings
            </h3>
            <ul class="space-y-1">
              <li>
                <a href="#" class="flex items-center px-3 py-2 text-gray-600 
                                   hover:bg-gray-100 rounded-lg transition">
                  <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/>
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                  </svg>
                  <span>Settings</span>
                </a>
              </li>
              <li>
                <a href="#" class="flex items-center px-3 py-2 text-gray-600 
                                   hover:bg-gray-100 rounded-lg transition">
                  <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                  </svg>
                  <span>Help</span>
                </a>
              </li>
            </ul>
          </div>
          
        </nav>
        
        <!-- Sidebar Footer -->
        <div class="p-4 border-t">
          <a href="#" class="flex items-center px-3 py-2 text-red-600 hover:bg-red-50 rounded-lg transition">
            <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
            </svg>
            <span>Logout</span>
          </a>
        </div>
        
      </aside>

      <!-- ========== Overlay (Mobile) ========== -->
      <div 
        id="overlay"
        class="fixed inset-0 bg-black bg-opacity-50 z-10 lg:hidden hidden"
      ></div>

      <!-- ========== Main Content Area ========== -->
      <main class="flex-1 overflow-y-auto">
        
        <!-- Page Header -->
        <div class="bg-white shadow-sm">
          <div class="px-6 py-4">
            <h1 class="text-2xl font-bold text-gray-800">Dashboard</h1>
            <p class="text-gray-500 text-sm">Welcome back, John!</p>
          </div>
        </div>
        
        <!-- Content -->
        <div class="p-6">
          
          <!-- Stats Cards -->
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
            
            <div class="bg-white rounded-xl shadow-sm p-6 border-l-4 border-indigo-500">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-gray-500 text-sm">Total Users</p>
                  <p class="text-2xl font-bold text-gray-800">12,345</p>
                </div>
                <div class="w-12 h-12 bg-indigo-100 rounded-full flex items-center justify-center">
                  <svg class="w-6 h-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/>
                  </svg>
                </div>
              </div>
              <p class="text-green-500 text-sm mt-2">↑ 12% from last month</p>
            </div>
            
            <div class="bg-white rounded-xl shadow-sm p-6 border-l-4 border-green-500">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-gray-500 text-sm">Revenue</p>
                  <p class="text-2xl font-bold text-gray-800">$54,321</p>
                </div>
                <div class="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                  <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                  </svg>
                </div>
              </div>
              <p class="text-green-500 text-sm mt-2">↑ 8% from last month</p>
            </div>
            
            <div class="bg-white rounded-xl shadow-sm p-6 border-l-4 border-yellow-500">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-gray-500 text-sm">Orders</p>
                  <p class="text-2xl font-bold text-gray-800">1,234</p>
                </div>
                <div class="w-12 h-12 bg-yellow-100 rounded-full flex items-center justify-center">
                  <svg class="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"/>
                  </svg>
                </div>
              </div>
              <p class="text-red-500 text-sm mt-2">↓ 3% from last month</p>
            </div>
            
            <div class="bg-white rounded-xl shadow-sm p-6 border-l-4 border-red-500">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-gray-500 text-sm">Bounce Rate</p>
                  <p class="text-2xl font-bold text-gray-800">23.5%</p>
                </div>
                <div class="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center">
                  <svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"/>
                  </svg>
                </div>
              </div>
              <p class="text-green-500 text-sm mt-2">↓ 5% from last month</p>
            </div>
            
          </div>
          
          <!-- Main Content Grid -->
          <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            
            <!-- Left Column - Table -->
            <div class="lg:col-span-2 bg-white rounded-xl shadow-sm">
              <div class="p-6 border-b">
                <h2 class="text-lg font-semibold text-gray-800">Recent Orders</h2>
              </div>
              <div class="overflow-x-auto">
                <table class="w-full">
                  <thead class="bg-gray-50">
                    <tr>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Order ID</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Customer</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Amount</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-200">
                    <tr class="hover:bg-gray-50">
                      <td class="px-6 py-4 text-sm text-gray-800">#ORD-001</td>
                      <td class="px-6 py-4 text-sm text-gray-600">Alice Johnson</td>
                      <td class="px-6 py-4">
                        <span class="px-2 py-1 text-xs font-medium bg-green-100 text-green-800 rounded-full">Completed</span>
                      </td>
                      <td class="px-6 py-4 text-sm text-gray-800">$250.00</td>
                    </tr>
                    <tr class="hover:bg-gray-50">
                      <td class="px-6 py-4 text-sm text-gray-800">#ORD-002</td>
                      <td class="px-6 py-4 text-sm text-gray-600">Bob Smith</td>
                      <td class="px-6 py-4">
                        <span class="px-2 py-1 text-xs font-medium bg-yellow-100 text-yellow-800 rounded-full">Pending</span>
                      </td>
                      <td class="px-6 py-4 text-sm text-gray-800">$180.00</td>
                    </tr>
                    <tr class="hover:bg-gray-50">
                      <td class="px-6 py-4 text-sm text-gray-800">#ORD-003</td>
                      <td class="px-6 py-4 text-sm text-gray-600">Carol White</td>
                      <td class="px-6 py-4">
                        <span class="px-2 py-1 text-xs font-medium bg-blue-100 text-blue-800 rounded-full">Processing</span>
                      </td>
                      <td class="px-6 py-4 text-sm text-gray-800">$420.00</td>
                    </tr>
                    <tr class="hover:bg-gray-50">
                      <td class="px-6 py-4 text-sm text-gray-800">#ORD-004</td>
                      <td class="px-6 py-4 text-sm text-gray-600">David Brown</td>
                      <td class="px-6 py-4">
                        <span class="px-2 py-1 text-xs font-medium bg-red-100 text-red-800 rounded-full">Cancelled</span>
                      </td>
                      <td class="px-6 py-4 text-sm text-gray-800">$95.00</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
            
            <!-- Right Column - Activity -->
            <div class="bg-white rounded-xl shadow-sm">
              <div class="p-6 border-b">
                <h2 class="text-lg font-semibold text-gray-800">Recent Activity</h2>
              </div>
              <div class="p-6 space-y-4">
                
                <div class="flex items-start space-x-3">
                  <div class="w-2 h-2 mt-2 bg-indigo-500 rounded-full"></div>
                  <div>
                    <p class="text-sm text-gray-800">New user registered</p>
                    <p class="text-xs text-gray-500">2 minutes ago</p>
                  </div>
                </div>
                
                <div class="flex items-start space-x-3">
                  <div class="w-2 h-2 mt-2 bg-green-500 rounded-full"></div>
                  <div>
                    <p class="text-sm text-gray-800">Order #ORD-001 completed</p>
                    <p class="text-xs text-gray-500">15 minutes ago</p>
                  </div>
                </div>
                
                <div class="flex items-start space-x-3">
                  <div class="w-2 h-2 mt-2 bg-yellow-500 rounded-full"></div>
                  <div>
                    <p class="text-sm text-gray-800">Payment received</p>
                    <p class="text-xs text-gray-500">1 hour ago</p>
                  </div>
                </div>
                
                <div class="flex items-start space-x-3">
                  <div class="w-2 h-2 mt-2 bg-red-500 rounded-full"></div>
                  <div>
                    <p class="text-sm text-gray-800">Server alert triggered</p>
                    <p class="text-xs text-gray-500">3 hours ago</p>
                  </div>
                </div>
                
                <div class="flex items-start space-x-3">
                  <div class="w-2 h-2 mt-2 bg-gray-500 rounded-full"></div>
                  <div>
                    <p class="text-sm text-gray-800">Backup completed</p>
                    <p class="text-xs text-gray-500">5 hours ago</p>
                  </div>
                </div>
                
              </div>
            </div>
            
          </div>
          
        </div>
        
      </main>
      
    </div>
    
  </div>

  <!-- JavaScript for Mobile Menu Toggle -->
  <script>
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');

    function toggleSidebar() {
      sidebar.classList.toggle('-translate-x-full');
      overlay.classList.toggle('hidden');
    }

    menuToggle.addEventListener('click', toggleSidebar);
    overlay.addEventListener('click', toggleSidebar);

    // Close sidebar on window resize (when going to desktop view)
    window.addEventListener('resize', () => {
      if (window.innerWidth >= 1024) {
        sidebar.classList.add('-translate-x-full');
        overlay.classList.add('hidden');
      }
    });
  </script>

</body>
</html>
```

---

## Layout Structure Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    Brand Bar (Top)                       │
│  [☰]  [Logo] Brand Name              [🔔] [Avatar]      │
├───────────┬─────────────────────────────────────────────┤
│           │                                              │
│  Sidebar  │              Content Area                    │
│           │                                              │
│  - Menu 1 │   ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐      │
│  - Menu 2 │   │ Card │ │ Card │ │ Card │ │ Card │      │
│  - Menu 3 │   └──────┘ └──────┘ └──────┘ └──────┘      │
│  - Menu 4 │                                              │
│  - Menu 5 │   ┌─────────────────────┐ ┌────────────┐   │
│           │   │      Table          │ │  Activity  │   │
│  ──────── │   │                     │ │            │   │
│           │   │                     │ │            │   │
│  - Settings│   └─────────────────────┘ └────────────┘   │
│  - Help   │                                              │
│           │                                              │
│  ──────── │                                              │
│  [Logout] │                                              │
└───────────┴─────────────────────────────────────────────┘
```

---

## Responsive Breakpoints

| Screen Size | Sidebar | Layout |
|-------------|---------|--------|
| **Mobile** (< 1024px) | Hidden (toggle) | Single column |
| **Tablet** (sm - lg) | Hidden (toggle) | 2 columns |
| **Desktop** (≥ 1024px) | Always visible | Full layout |

---

## Key Tailwind Classes Used

### Responsive Classes

```html
<!-- Hidden on mobile, visible on desktop -->
<span class="hidden lg:block">Desktop Only</span>

<!-- Grid columns change based on screen -->
<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">

<!-- Sidebar positioning -->
<aside class="fixed lg:static transform -translate-x-full lg:translate-x-0">
```

### Layout Classes

```html
<!-- Flexbox layout -->
<div class="flex flex-col min-h-screen">

<!-- Sidebar width -->
<aside class="w-64">

<!-- Content takes remaining space -->
<main class="flex-1 overflow-y-auto">
```

---

## Features Included

| Feature | Description |
|---------|-------------|
| ✅ Brand Bar | Fixed top with logo and user menu |
| ✅ Sidebar | Collapsible navigation menu |
| ✅ Mobile Menu | Hamburger toggle with overlay |
| ✅ Responsive Grid | Adapts to screen size |
| ✅ Stats Cards | Dashboard statistics |
| ✅ Data Table | Recent orders |
| ✅ Activity Feed | Timeline of events |
| ✅ Smooth Transitions | CSS animations |

---

Save this as `index.html` and open in browser - it's fully functional! 🎉
