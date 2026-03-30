# Refine Layout Left Panel - Menu Item Groups & Sub Items

Yes, **Refine** supports menu item groups and nested sub-items in the left panel (Sider). Here's how to implement them:

---

## 1. **Basic Menu with Sub Items (Built-in)**

Refine automatically generates nested menus based on your resource definitions:

```tsx
// App.tsx
import {Refine} from "@refinedev/core";
import {ThemedLayoutV2} from "@refinedev/antd"; // or mui, chakra, mantine

function App() {
    return (
        <Refine
            resources={[
                {
                    name: "dashboard",
                    list: "/dashboard",
                    meta: {
                        icon: <DashboardOutlined/>,
                    },
                },
                // Nested menu items using parent
                {
                    name: "users",
                    list: "/users",
                    create: "/users/create",
                    edit: "/users/edit/:id",
                    meta: {
                        icon: <UserOutlined/>,
                        parent: "user-management",  // Parent group
                    },
                },
                {
                    name: "roles",
                    list: "/roles",
                    meta: {
                        icon: <SafetyOutlined/>,
                        parent: "user-management",  // Same parent = grouped
                    },
                },
                {
                    name: "permissions",
                    list: "/permissions",
                    meta: {
                        icon: <LockOutlined/>,
                        parent: "user-management",
                    },
                },
                // Define the parent group
                {
                    name: "user-management",
                    meta: {
                        icon: <TeamOutlined/>,
                        label: "User Management",
                    },
                },
                // Another group
                {
                    name: "products",
                    list: "/products",
                    meta: {
                        icon: <ShoppingOutlined/>,
                        parent: "inventory",
                    },
                },
                {
                    name: "categories",
                    list: "/categories",
                    meta: {
                        icon: <AppstoreOutlined/>,
                        parent: "inventory",
                    },
                },
                {
                    name: "inventory",
                    meta: {
                        icon: <DatabaseOutlined/>,
                        label: "Inventory",
                    },
                },
            ]}
        >
            <ThemedLayoutV2>
                {/* Your routes */}
            </ThemedLayoutV2>
        </Refine>
    );
}
```

---

## 2. **Custom Sider with Menu Groups (Ant Design)**

For more control, create a custom Sider:

```tsx
// components/CustomSider.tsx
import React, {useState} from "react";
import {Layout, Menu} from "antd";
import type {MenuProps} from "antd";
import {
    DashboardOutlined,
    UserOutlined,
    TeamOutlined,
    SettingOutlined,
    ShoppingOutlined,
    FileTextOutlined,
    SafetyOutlined,
    DatabaseOutlined,
} from "@ant-design/icons";
import {useMenu, useNavigation} from "@refinedev/core";

const {Sider} = Layout;

type MenuItem = Required<MenuProps>["items"][number];

export const CustomSider: React.FC = () => {
    const [collapsed, setCollapsed] = useState(false);
    const {push} = useNavigation();

    const menuItems: MenuItem[] = [
        {
            key: "dashboard",
            icon: <DashboardOutlined/>,
            label: "Dashboard",
            onClick: () => push("/dashboard"),
        },
        {
            key: "user-management",
            icon: <TeamOutlined/>,
            label: "User Management",
            children: [
                {
                    key: "users",
                    icon: <UserOutlined/>,
                    label: "Users",
                    onClick: () => push("/users"),
                },
                {
                    key: "roles",
                    icon: <SafetyOutlined/>,
                    label: "Roles",
                    onClick: () => push("/roles"),
                },
                {
                    key: "permissions",
                    icon: <LockOutlined/>,
                    label: "Permissions",
                    onClick: () => push("/permissions"),
                },
            ],
        },
        {
            key: "inventory",
            icon: <DatabaseOutlined/>,
            label: "Inventory",
            children: [
                {
                    key: "products",
                    icon: <ShoppingOutlined/>,
                    label: "Products",
                    onClick: () => push("/products"),
                },
                {
                    key: "categories",
                    icon: <AppstoreOutlined/>,
                    label: "Categories",
                    onClick: () => push("/categories"),
                },
                {
                    key: "stock",
                    icon: <StockOutlined/>,
                    label: "Stock",
                    onClick: () => push("/stock"),
                },
            ],
        },
        {
            type: "divider",  // Visual separator
        },
        {
            key: "reports",
            icon: <FileTextOutlined/>,
            label: "Reports",
            children: [
                {
                    key: "sales-report",
                    label: "Sales Report",
                    onClick: () => push("/reports/sales"),
                },
                {
                    key: "inventory-report",
                    label: "Inventory Report",
                    onClick: () => push("/reports/inventory"),
                },
            ],
        },
        {
            type: "group",  // Menu item group (non-collapsible header)
            label: "Settings",
            children: [
                {
                    key: "general-settings",
                    icon: <SettingOutlined/>,
                    label: "General",
                    onClick: () => push("/settings/general"),
                },
                {
                    key: "notifications",
                    icon: <BellOutlined/>,
                    label: "Notifications",
                    onClick: () => push("/settings/notifications"),
                },
            ],
        },
    ];

    return (
        <Sider
            collapsible
            collapsed={collapsed}
            onCollapse={(value) => setCollapsed(value)}
            style={{
                overflow: "auto",
                height: "100vh",
                position: "fixed",
                left: 0,
                top: 0,
                bottom: 0,
            }}
        >
            <div
                style={{
                    height: 32,
                    margin: 16,
                    background: "rgba(255, 255, 255, 0.2)",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    color: "white",
                }}
            >
                {collapsed ? "Logo" : "My Application"}
            </div>
            <Menu
                theme="dark"
                mode="inline"
                defaultSelectedKeys={["dashboard"]}
                defaultOpenKeys={["user-management", "inventory"]}
                items={menuItems}
            />
        </Sider>
    );
};
```

### Use Custom Sider in Layout

```tsx
// App.tsx
import {CustomSider} from "./components/CustomSider";

function App() {
    return (
        <Refine
            // ... other props
        >
            <ThemedLayoutV2
                Sider={() => <CustomSider/>}
            >
                {/* Routes */}
            </ThemedLayoutV2>
        </Refine>
    );
}
```

---

## 3. **Three-Level Nested Menu**

```tsx
const menuItems: MenuItem[] = [
    {
        key: "ecommerce",
        icon: <ShoppingCartOutlined/>,
        label: "E-Commerce",
        children: [
            {
                key: "products",
                icon: <ShoppingOutlined/>,
                label: "Products",
                children: [
                    {
                        key: "product-list",
                        label: "Product List",
                        onClick: () => push("/products"),
                    },
                    {
                        key: "add-product",
                        label: "Add Product",
                        onClick: () => push("/products/create"),
                    },
                    {
                        key: "product-categories",
                        label: "Categories",
                        onClick: () => push("/products/categories"),
                    },
                ],
            },
            {
                key: "orders",
                icon: <FileTextOutlined/>,
                label: "Orders",
                children: [
                    {
                        key: "order-list",
                        label: "Order List",
                        onClick: () => push("/orders"),
                    },
                    {
                        key: "order-details",
                        label: "Order Details",
                        onClick: () => push("/orders/details"),
                    },
                ],
            },
        ],
    },
];
```

---

## 4. **Menu Item Group (Non-Collapsible Header)**

```tsx
const menuItems: MenuItem[] = [
    {
        key: "dashboard",
        icon: <DashboardOutlined/>,
        label: "Dashboard",
    },
    {
        type: "group",  // This creates a group header
        label: "Management",
        children: [
            {
                key: "users",
                icon: <UserOutlined/>,
                label: "Users",
            },
            {
                key: "roles",
                icon: <SafetyOutlined/>,
                label: "Roles",
            },
        ],
    },
    {
        type: "group",
        label: "Content",
        children: [
            {
                key: "posts",
                icon: <FileTextOutlined/>,
                label: "Posts",
            },
            {
                key: "media",
                icon: <PictureOutlined/>,
                label: "Media",
            },
        ],
    },
    {
        type: "divider",  // Horizontal line
    },
    {
        key: "settings",
        icon: <SettingOutlined/>,
        label: "Settings",
    },
];
```

---

## 5. **Dynamic Menu with useMenu Hook**

```tsx
import {useMenu, useNavigation} from "@refinedev/core";
import {Menu} from "antd";

export const DynamicSider: React.FC = () => {
    const {menuItems, selectedKey, defaultOpenKeys} = useMenu();
    const {push} = useNavigation();

    const renderMenuItems = (items: typeof menuItems): MenuItem[] => {
        return items.map((item) => {
            if (item.children && item.children.length > 0) {
                return {
                    key: item.key,
                    icon: item.icon,
                    label: item.label,
                    children: renderMenuItems(item.children),
                };
            }

            return {
                key: item.key,
                icon: item.icon,
                label: item.label,
                onClick: () => {
                    if (item.route) {
                        push(item.route);
                    }
                },
            };
        });
    };

    // Add custom groups
    const enhancedMenuItems: MenuItem[] = [
        {
            type: "group",
            label: "Main",
            children: renderMenuItems(
                menuItems.filter((item) => ["dashboard", "home"].includes(item.key || ""))
            ),
        },
        {
            type: "group",
            label: "Management",
            children: renderMenuItems(
                menuItems.filter((item) =>
                    ["users", "roles", "permissions"].includes(item.key || "")
                )
            ),
        },
        {
            type: "divider",
        },
        ...renderMenuItems(
            menuItems.filter(
                (item) =>
                    !["dashboard", "home", "users", "roles", "permissions"].includes(
                        item.key || ""
                    )
            )
        ),
    ];

    return (
        <Menu
            theme="dark"
            mode="inline"
            selectedKeys={[selectedKey]}
            defaultOpenKeys={defaultOpenKeys}
            items={enhancedMenuItems}
        />
    );
};
```

---

## 6. **Complete Example with All Features**

```tsx
// components/AdvancedSider.tsx
import React, {useState} from "react";
import {Layout, Menu, Badge, Typography} from "antd";
import type {MenuProps} from "antd";
import {
    DashboardOutlined,
    UserOutlined,
    TeamOutlined,
    SettingOutlined,
    ShoppingOutlined,
    FileTextOutlined,
    SafetyOutlined,
    BellOutlined,
    QuestionCircleOutlined,
    LogoutOutlined,
} from "@ant-design/icons";
import {useNavigation, useLogout} from "@refinedev/core";

const {Sider} = Layout;
const {Text} = Typography;

export const AdvancedSider: React.FC = () => {
    const [collapsed, setCollapsed] = useState(false);
    const {push} = useNavigation();
    const {mutate: logout} = useLogout();

    const menuItems: MenuProps["items"] = [
        // Regular item
        {
            key: "dashboard",
            icon: <DashboardOutlined/>,
            label: "Dashboard",
            onClick: () => push("/dashboard"),
        },

        // Submenu (collapsible)
        {
            key: "user-management",
            icon: <TeamOutlined/>,
            label: "User Management",
            children: [
                {
                    key: "users",
                    icon: <UserOutlined/>,
                    label: "Users",
                    onClick: () => push("/users"),
                },
                {
                    key: "roles",
                    icon: <SafetyOutlined/>,
                    label: "Roles",
                    onClick: () => push("/roles"),
                },
            ],
        },

        // Submenu with badge
        {
            key: "orders",
            icon: <ShoppingOutlined/>,
            label: (
                <span>
          Orders
          <Badge count={5} style={{marginLeft: 8}}/>
        </span>
            ),
            children: [
                {
                    key: "pending-orders",
                    label: (
                        <span>
              Pending
              <Badge count={3} style={{marginLeft: 8}}/>
            </span>
                    ),
                    onClick: () => push("/orders?status=pending"),
                },
                {
                    key: "completed-orders",
                    label: "Completed",
                    onClick: () => push("/orders?status=completed"),
                },
            ],
        },

        // Divider
        {
            type: "divider",
        },

        // Group (non-collapsible header)
        {
            type: "group",
            label: collapsed ? null : "Settings & Support",
            children: [
                {
                    key: "settings",
                    icon: <SettingOutlined/>,
                    label: "Settings",
                    onClick: () => push("/settings"),
                },
                {
                    key: "notifications",
                    icon: <BellOutlined/>,
                    label: (
                        <span>
              Notifications
              <Badge dot style={{marginLeft: 8}}/>
            </span>
                    ),
                    onClick: () => push("/notifications"),
                },
                {
                    key: "help",
                    icon: <QuestionCircleOutlined/>,
                    label: "Help & Support",
                    onClick: () => push("/help"),
                },
            ],
        },

        // Another divider
        {
            type: "divider",
        },

        // Logout at bottom
        {
            key: "logout",
            icon: <LogoutOutlined/>,
            label: "Logout",
            danger: true,
            onClick: () => logout(),
        },
    ];

    return (
        <Sider
            collapsible
            collapsed={collapsed}
            onCollapse={setCollapsed}
            style={{
                overflow: "auto",
                height: "100vh",
                position: "sticky",
                top: 0,
                left: 0,
            }}
            width={250}
        >
            {/* Logo */}
            <div
                style={{
                    height: 64,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    borderBottom: "1px solid rgba(255,255,255,0.1)",
                }}
            >
                {collapsed ? (
                    <Text strong style={{color: "white", fontSize: 20}}>
                        A
                    </Text>
                ) : (
                    <Text strong style={{color: "white", fontSize: 18}}>
                        Admin Panel
                    </Text>
                )}
            </div>

            {/* Menu */}
            <Menu
                theme="dark"
                mode="inline"
                defaultSelectedKeys={["dashboard"]}
                defaultOpenKeys={collapsed ? [] : ["user-management", "orders"]}
                items={menuItems}
                style={{borderRight: 0}}
            />
        </Sider>
    );
};
```

---

## Menu Types Summary

| Type        | Description       | Collapsible        |
|-------------|-------------------|--------------------|
| **SubMenu** | `children: [...]` | ✅ Yes              |
| **Group**   | `type: "group"`   | ❌ No (header only) |
| **Divider** | `type: "divider"` | N/A                |
| **Item**    | Regular menu item | N/A                |

---

## Visual Representation

```
├── Dashboard                    # Regular item
├── User Management ▼            # SubMenu (collapsible)
│   ├── Users
│   ├── Roles
│   └── Permissions
├── Orders ▼                     # SubMenu with badge
│   ├── Pending (3)
│   └── Completed
├── ─────────────                # Divider
├── SETTINGS & SUPPORT           # Group header (non-collapsible)
│   ├── Settings
│   ├── Notifications •
│   └── Help & Support
├── ─────────────                # Divider
└── Logout                       # Danger item
```

Would you like me to show examples for **Material UI**, **Chakra UI**, or **Mantine** versions of Refine?