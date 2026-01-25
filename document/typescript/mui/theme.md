# mui theme

# Customizing MUI Theme - Complete Guide

Let me show you how to create a highly customized theme in both **MUI (Material-UI)** and **MUI Toolpad** projects.

---

## **Part 1: MUI (Material-UI) Theme Customization**

### **Starting Point - Official Documentation**

📚 **Key Documentation:**
- [MUI Theming Overview](https://mui.com/material-ui/customization/theming/)
- [Default Theme Explorer](https://mui.com/material-ui/customization/default-theme/)
- [Theme Configuration](https://mui.com/material-ui/customization/palette/)
- [Dark Mode](https://mui.com/material-ui/customization/dark-mode/)

---

### **Step 1: Basic Theme Setup**

```tsx
// src/theme/theme.ts
import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
      light: '#42a5f5',
      dark: '#1565c0',
      contrastText: '#fff',
    },
    secondary: {
      main: '#dc004e',
      light: '#f73378',
      dark: '#9a0036',
      contrastText: '#fff',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h1: {
      fontSize: '2.5rem',
      fontWeight: 600,
    },
  },
  spacing: 8, // default spacing unit
  shape: {
    borderRadius: 8,
  },
});

export default theme;
```

**Apply theme to your app:**

```tsx
// src/App.tsx
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import theme from './theme/theme';

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline /> {/* Normalize CSS */}
      {/* Your app components */}
    </ThemeProvider>
  );
}

export default App;
```

---

### **Step 2: Highly Customized Theme**

```tsx
// src/theme/customTheme.ts
import { createTheme, ThemeOptions } from '@mui/material/styles';

// Define custom colors
const customColors = {
  brand: {
    primary: '#6366f1', // Indigo
    secondary: '#ec4899', // Pink
    tertiary: '#10b981', // Green
  },
  neutral: {
    50: '#fafafa',
    100: '#f5f5f5',
    200: '#e5e5e5',
    300: '#d4d4d4',
    400: '#a3a3a3',
    500: '#737373',
    600: '#525252',
    700: '#404040',
    800: '#262626',
    900: '#171717',
  },
  success: {
    light: '#4ade80',
    main: '#22c55e',
    dark: '#16a34a',
  },
  error: {
    light: '#f87171',
    main: '#ef4444',
    dark: '#dc2626',
  },
  warning: {
    light: '#fbbf24',
    main: '#f59e0b',
    dark: '#d97706',
  },
  info: {
    light: '#60a5fa',
    main: '#3b82f6',
    dark: '#2563eb',
  },
};

const themeOptions: ThemeOptions = {
  palette: {
    mode: 'light',
    primary: {
      main: customColors.brand.primary,
      light: '#818cf8',
      dark: '#4f46e5',
      contrastText: '#ffffff',
    },
    secondary: {
      main: customColors.brand.secondary,
      light: '#f472b6',
      dark: '#db2777',
      contrastText: '#ffffff',
    },
    error: customColors.error,
    warning: customColors.warning,
    info: customColors.info,
    success: customColors.success,
    background: {
      default: '#fafafa',
      paper: '#ffffff',
    },
    text: {
      primary: customColors.neutral[900],
      secondary: customColors.neutral[600],
      disabled: customColors.neutral[400],
    },
    divider: customColors.neutral[200],
  },

  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    fontSize: 14,
    fontWeightLight: 300,
    fontWeightRegular: 400,
    fontWeightMedium: 500,
    fontWeightBold: 700,
    
    h1: {
      fontSize: '3rem',
      fontWeight: 700,
      lineHeight: 1.2,
      letterSpacing: '-0.02em',
    },
    h2: {
      fontSize: '2.25rem',
      fontWeight: 700,
      lineHeight: 1.3,
      letterSpacing: '-0.01em',
    },
    h3: {
      fontSize: '1.875rem',
      fontWeight: 600,
      lineHeight: 1.4,
    },
    h4: {
      fontSize: '1.5rem',
      fontWeight: 600,
      lineHeight: 1.4,
    },
    h5: {
      fontSize: '1.25rem',
      fontWeight: 600,
      lineHeight: 1.5,
    },
    h6: {
      fontSize: '1rem',
      fontWeight: 600,
      lineHeight: 1.5,
    },
    subtitle1: {
      fontSize: '1rem',
      fontWeight: 500,
      lineHeight: 1.75,
    },
    subtitle2: {
      fontSize: '0.875rem',
      fontWeight: 500,
      lineHeight: 1.57,
    },
    body1: {
      fontSize: '1rem',
      lineHeight: 1.5,
    },
    body2: {
      fontSize: '0.875rem',
      lineHeight: 1.43,
    },
    button: {
      fontSize: '0.875rem',
      fontWeight: 600,
      textTransform: 'none', // Remove uppercase
      letterSpacing: '0.02em',
    },
    caption: {
      fontSize: '0.75rem',
      lineHeight: 1.66,
    },
    overline: {
      fontSize: '0.75rem',
      fontWeight: 600,
      letterSpacing: '0.08em',
      textTransform: 'uppercase',
    },
  },

  spacing: 8, // Base spacing unit (1 = 8px)

  shape: {
    borderRadius: 12, // More rounded corners
  },

  shadows: [
    'none',
    '0px 2px 4px rgba(0,0,0,0.05)',
    '0px 4px 8px rgba(0,0,0,0.08)',
    '0px 8px 16px rgba(0,0,0,0.1)',
    '0px 12px 24px rgba(0,0,0,0.12)',
    '0px 16px 32px rgba(0,0,0,0.14)',
    '0px 20px 40px rgba(0,0,0,0.16)',
    // ... continue up to 24 shadows
  ] as any,

  components: {
    // Button customization
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          padding: '10px 20px',
          fontWeight: 600,
          boxShadow: 'none',
          '&:hover': {
            boxShadow: '0px 4px 12px rgba(0,0,0,0.15)',
          },
        },
        contained: {
          '&:hover': {
            boxShadow: '0px 6px 16px rgba(0,0,0,0.2)',
          },
        },
        outlined: {
          borderWidth: '2px',
          '&:hover': {
            borderWidth: '2px',
          },
        },
        sizeLarge: {
          padding: '12px 28px',
          fontSize: '1rem',
        },
        sizeSmall: {
          padding: '6px 16px',
          fontSize: '0.813rem',
        },
      },
      defaultProps: {
        disableElevation: true,
      },
    },

    // Card customization
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          boxShadow: '0px 4px 12px rgba(0,0,0,0.08)',
          transition: 'transform 0.2s, box-shadow 0.2s',
          '&:hover': {
            transform: 'translateY(-4px)',
            boxShadow: '0px 8px 24px rgba(0,0,0,0.12)',
          },
        },
      },
    },

    // TextField customization
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: 8,
            '& fieldset': {
              borderWidth: '2px',
            },
            '&:hover fieldset': {
              borderColor: customColors.brand.primary,
            },
            '&.Mui-focused fieldset': {
              borderWidth: '2px',
            },
          },
        },
      },
      defaultProps: {
        variant: 'outlined',
      },
    },

    // Paper customization
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 12,
        },
        elevation1: {
          boxShadow: '0px 2px 8px rgba(0,0,0,0.06)',
        },
        elevation2: {
          boxShadow: '0px 4px 12px rgba(0,0,0,0.08)',
        },
      },
    },

    // Chip customization
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          fontWeight: 500,
        },
        filled: {
          backgroundColor: customColors.neutral[100],
          color: customColors.neutral[800],
          '&:hover': {
            backgroundColor: customColors.neutral[200],
          },
        },
      },
    },

    // AppBar customization
    MuiAppBar: {
      styleOverrides: {
        root: {
          boxShadow: '0px 1px 3px rgba(0,0,0,0.08)',
        },
      },
      defaultProps: {
        elevation: 0,
      },
    },

    // Drawer customization
    MuiDrawer: {
      styleOverrides: {
        paper: {
          borderRadius: 0,
          borderRight: `1px solid ${customColors.neutral[200]}`,
        },
      },
    },

    // Table customization
    MuiTableCell: {
      styleOverrides: {
        root: {
          borderBottom: `1px solid ${customColors.neutral[200]}`,
        },
        head: {
          fontWeight: 600,
          backgroundColor: customColors.neutral[50],
        },
      },
    },

    // Dialog customization
    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: 16,
          boxShadow: '0px 24px 48px rgba(0,0,0,0.2)',
        },
      },
    },

    // Switch customization
    MuiSwitch: {
      styleOverrides: {
        root: {
          padding: 8,
        },
        track: {
          borderRadius: 12,
        },
        thumb: {
          boxShadow: '0px 2px 4px rgba(0,0,0,0.2)',
        },
      },
    },
  },

  // Custom breakpoints (optional)
  breakpoints: {
    values: {
      xs: 0,
      sm: 640,
      md: 768,
      lg: 1024,
      xl: 1280,
    },
  },

  // Custom transitions (optional)
  transitions: {
    duration: {
      shortest: 150,
      shorter: 200,
      short: 250,
      standard: 300,
      complex: 375,
      enteringScreen: 225,
      leavingScreen: 195,
    },
    easing: {
      easeInOut: 'cubic-bezier(0.4, 0, 0.2, 1)',
      easeOut: 'cubic-bezier(0.0, 0, 0.2, 1)',
      easeIn: 'cubic-bezier(0.4, 0, 1, 1)',
      sharp: 'cubic-bezier(0.4, 0, 0.6, 1)',
    },
  },
};

const customTheme = createTheme(themeOptions);

export default customTheme;
```

---

### **Step 3: Theme with TypeScript Support**

```tsx
// src/theme/theme.d.ts
import '@mui/material/styles';

declare module '@mui/material/styles' {
  interface Palette {
    tertiary: Palette['primary'];
    brand: {
      primary: string;
      secondary: string;
      tertiary: string;
    };
  }

  interface PaletteOptions {
    tertiary?: PaletteOptions['primary'];
    brand?: {
      primary?: string;
      secondary?: string;
      tertiary?: string;
    };
  }
}

declare module '@mui/material/Button' {
  interface ButtonPropsColorOverrides {
    tertiary: true;
  }
}
```

---

### **Step 4: Dark Mode Support**

```tsx
// src/theme/themeProvider.tsx
import React, { createContext, useMemo, useState, useContext } from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';

type ThemeMode = 'light' | 'dark';

interface ThemeContextType {
  mode: ThemeMode;
  toggleTheme: () => void;
}

const ThemeContext = createContext<ThemeContextType>({
  mode: 'light',
  toggleTheme: () => {},
});

export const useThemeMode = () => useContext(ThemeContext);

export const CustomThemeProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [mode, setMode] = useState<ThemeMode>('light');

  const toggleTheme = () => {
    setMode((prevMode) => (prevMode === 'light' ? 'dark' : 'light'));
  };

  const theme = useMemo(
    () =>
      createTheme({
        palette: {
          mode,
          ...(mode === 'light'
            ? {
                // Light mode colors
                primary: {
                  main: '#6366f1',
                },
                background: {
                  default: '#fafafa',
                  paper: '#ffffff',
                },
              }
            : {
                // Dark mode colors
                primary: {
                  main: '#818cf8',
                },
                background: {
                  default: '#0f172a',
                  paper: '#1e293b',
                },
              }),
        },
        // ... rest of theme config
      }),
    [mode]
  );

  return (
    <ThemeContext.Provider value={{ mode, toggleTheme }}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        {children}
      </ThemeProvider>
    </ThemeContext.Provider>
  );
};
```

**Usage:**

```tsx
// src/App.tsx
import { CustomThemeProvider } from './theme/themeProvider';
import { ThemeSwitcher } from './components/ThemeSwitcher';

function App() {
  return (
    <CustomThemeProvider>
      <ThemeSwitcher />
      {/* Your app */}
    </CustomThemeProvider>
  );
}

// src/components/ThemeSwitcher.tsx
import { IconButton } from '@mui/material';
import { Brightness4, Brightness7 } from '@mui/icons-material';
import { useThemeMode } from '../theme/themeProvider';

export const ThemeSwitcher = () => {
  const { mode, toggleTheme } = useThemeMode();

  return (
    <IconButton onClick={toggleTheme} color="inherit">
      {mode === 'dark' ? <Brightness7 /> : <Brightness4 />}
    </IconButton>
  );
};
```

---

## **Part 2: MUI Toolpad Theme Customization**

### **Official Documentation**

📚 **Key Resources:**
- [Toolpad Theming Docs](https://mui.com/toolpad/core/react-use-theme/)
- [Toolpad Theme Provider](https://mui.com/toolpad/core/react-theme-provider/)

---

### **Toolpad Theme Setup**

```tsx
// toolpad/theme.tsx
import { createTheme } from '@mui/material/styles';

export const toolpadTheme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#6366f1',
    },
    secondary: {
      main: '#ec4899',
    },
    background: {
      default: '#f8fafc',
      paper: '#ffffff',
    },
  },
  
  typography: {
    fontFamily: '"Inter", "Roboto", sans-serif',
  },

  components: {
    // Toolpad-specific component overrides
    MuiDataGrid: {
      styleOverrides: {
        root: {
          border: 'none',
          '& .MuiDataGrid-cell': {
            borderBottom: '1px solid #e2e8f0',
          },
          '& .MuiDataGrid-columnHeaders': {
            backgroundColor: '#f1f5f9',
            fontWeight: 600,
            borderBottom: '2px solid #cbd5e1',
          },
        },
      },
    },
    
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundColor: '#1e293b',
          color: '#fff',
        },
      },
    },
  },
});
```

**Apply in Toolpad:**

```tsx
// toolpad.config.tsx (or your Toolpad app entry)
import { ThemeProvider } from '@mui/material/styles';
import { AppProvider } from '@toolpad/core/AppProvider';
import { toolpadTheme } from './theme';

export default function ToolpadApp({ children }: { children: React.ReactNode }) {
  return (
    <AppProvider theme={toolpadTheme}>
      {children}
    </AppProvider>
  );
}
```

---

## **Advanced Customization Techniques**

### **1. Custom Component Variants**

```tsx
// Add custom button variant
const theme = createTheme({
  components: {
    MuiButton: {
      variants: [
        {
          props: { variant: 'gradient' },
          style: {
            background: 'linear-gradient(45deg, #FE6B8B 30%, #FF8E53 90%)',
            border: 0,
            borderRadius: 12,
            boxShadow: '0 3px 5px 2px rgba(255, 105, 135, .3)',
            color: 'white',
            height: 48,
            padding: '0 30px',
            '&:hover': {
              background: 'linear-gradient(45deg, #FF8E53 30%, #FE6B8B 90%)',
            },
          },
        },
      ],
    },
  },
});

// TypeScript declaration
declare module '@mui/material/Button' {
  interface ButtonPropsVariantOverrides {
    gradient: true;
  }
}

// Usage
<Button variant="gradient">Gradient Button</Button>
```

### **2. Responsive Typography**

```tsx
const theme = createTheme({
  typography: {
    h1: {
      fontSize: '2.5rem',
      '@media (min-width:600px)': {
        fontSize: '3rem',
      },
      '@media (min-width:900px)': {
        fontSize: '3.5rem',
      },
    },
  },
});
```

### **3. Custom Utilities with `sx` prop**

```tsx
// Define custom spacing in theme
const theme = createTheme({
  spacing: (factor: number) => `${0.25 * factor}rem`,
});

// Use in components
<Box sx={{ 
  p: 4,  // 1rem padding
  m: 2,  // 0.5rem margin
  bgcolor: 'primary.main',
  color: 'primary.contrastText',
  borderRadius: 2,
  '&:hover': {
    bgcolor: 'primary.dark',
  }
}} />
```

---

## **Complete Working Example**

```tsx
// src/theme/index.ts
export { default as theme } from './customTheme';
export { CustomThemeProvider, useThemeMode } from './themeProvider';

// src/index.tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { CustomThemeProvider } from './theme';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <CustomThemeProvider>
      <App />
    </CustomThemeProvider>
  </React.StrictMode>
);

// src/App.tsx
import { Button, Card, CardContent, Typography, TextField } from '@mui/material';

function App() {
  return (
    <div style={{ padding: '2rem' }}>
      <Card>
        <CardContent>
          <Typography variant="h3" gutterBottom>
            Custom Themed App
          </Typography>
          <TextField fullWidth label="Email" margin="normal" />
          <Button variant="contained" size="large" sx={{ mt: 2 }}>
            Submit
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}

export default App;
```

---

## **Useful Tools & Resources**

### **Theme Generators:**
1. **[MUI Theme Creator](https://zenoo.github.io/mui-theme-creator/)** - Visual theme builder
2. **[Material Palette](https://materialpalette.com/)** - Color scheme generator
3. **[Coolors](https://coolors.co/)** - Color palette generator

### **Design Systems to Reference:**
- [Material Design 3](https://m3.material.io/)
- [Ant Design](https://ant.design/)
- [Chakra UI](https://chakra-ui.com/)

### **Testing Your Theme:**
```tsx
// Create a theme showcase page
import { Button, TextField, Card, Chip, Switch } from '@mui/material';

export function ThemeShowcase() {
  return (
    <div style={{ padding: '2rem', display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
      <Button variant="contained">Contained</Button>
      <Button variant="outlined">Outlined</Button>
      <Button variant="text">Text</Button>
      <TextField label="Input" />
      <Card>Card</Card>
      <Chip label="Chip" />
      <Switch />
    </div>
  );
}
```

---

## **Best Practices**

✅ **Start with theme object** - Don't use inline styles  
✅ **Use theme tokens** - `theme.spacing()`, `theme.palette.primary.main`  
✅ **Extend, don't replace** - Use `createTheme()` with existing theme  
✅ **Test in both light/dark modes**  
✅ **Document your design tokens** - Create a style guide  
✅ **Use TypeScript** - Get autocomplete for custom theme values  
✅ **Keep theme file separate** - Don't mix with component code

**The recommended starting point:** Copy the basic setup from Step 2, then gradually customize based on your design requirements. Use the [Default Theme Explorer](https://mui.com/material-ui/customization/default-theme/) to see what's available to override. 🎨


# share with toolpad

# MUI vs Toolpad - Styling Architecture

Great question! Let me explain the relationship between MUI and Toolpad's styling systems.

---

## **Short Answer**

**YES**, MUI Toolpad uses the **exact same underlying styling system** as MUI (Material-UI). Toolpad is **built on top of MUI**, not a separate UI framework.

```
┌─────────────────────────────────────┐
│         MUI Toolpad                 │
│  (Low-code app builder framework)   │
├─────────────────────────────────────┤
│         MUI Core Components         │
│  (Button, TextField, DataGrid, etc) │
├─────────────────────────────────────┤
│      @mui/system (Styling API)      │
│  (sx prop, styled, createTheme)     │
├─────────────────────────────────────┤
│          Emotion/JSS                │
│      (CSS-in-JS engine)             │
└─────────────────────────────────────┘
```

---

## **Detailed Breakdown**

### **1. Same Core Styling Engine**

Both use **@mui/system** and **Emotion** (or styled-components):

```tsx
// This works EXACTLY the same in both MUI and Toolpad
import { Box, Button } from '@mui/material';
import { styled } from '@mui/material/styles';

// sx prop - same in both
<Box sx={{ 
  p: 2, 
  bgcolor: 'primary.main',
  '&:hover': { bgcolor: 'primary.dark' }
}} />

// styled() API - same in both
const StyledButton = styled(Button)(({ theme }) => ({
  backgroundColor: theme.palette.primary.main,
  '&:hover': {
    backgroundColor: theme.palette.primary.dark,
  },
}));
```

### **2. Same Theme System**

```tsx
// Theme created with createTheme() works in both
import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    primary: { main: '#6366f1' },
    secondary: { main: '#ec4899' },
  },
  typography: {
    fontFamily: '"Inter", sans-serif',
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: { borderRadius: 8 }
      }
    }
  }
});

// Works in plain MUI app
import { ThemeProvider } from '@mui/material/styles';
<ThemeProvider theme={theme}>
  <App />
</ThemeProvider>

// ALSO works in Toolpad app
import { AppProvider } from '@toolpad/core/AppProvider';
<AppProvider theme={theme}>
  <ToolpadApp />
</AppProvider>
```

### **3. Same Component Library**

Toolpad uses **actual MUI components** under the hood:

```tsx
// Toolpad's components ARE MUI components
import { Button } from '@mui/material';        // MUI
import { Button } from '@toolpad/core/Button'; // Same component, just re-exported

// Both render the same <Button> component
// Both accept the same props
// Both use the same styling system
```

---

## **What Makes Toolpad Different?**

Toolpad **adds abstractions** on top of MUI, but doesn't change the styling system:

### **Toolpad = MUI + Additional Features**

```tsx
┌──────────────────────────────────────────┐
│ Toolpad Additions (on top of MUI):      │
├──────────────────────────────────────────┤
│ ✓ AppProvider (app shell, navigation)   │
│ ✓ DashboardLayout (pre-built layout)    │
│ ✓ DataGrid Pro (enhanced data grid)     │
│ ✓ useDialogs() (dialog management)      │
│ ✓ useNotifications() (toast system)     │
│ ✓ File upload components                │
│ ✓ Low-code page builder                 │
│ ✓ Data binding helpers                  │
└──────────────────────────────────────────┘
         ↓ Built on top of ↓
┌──────────────────────────────────────────┐
│ MUI Core (Material-UI):                 │
├──────────────────────────────────────────┤
│ ✓ All base components (Button, etc)     │
│ ✓ Theming system (createTheme)          │
│ ✓ Styling APIs (sx, styled)             │
│ ✓ @mui/system                            │
│ ✓ Emotion CSS-in-JS                      │
└──────────────────────────────────────────┘
```

---

## **Practical Example - Same Theme, Both Frameworks**

### **Scenario: You have a custom theme**

```tsx
// customTheme.ts
import { createTheme } from '@mui/material/styles';

export const myTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#6366f1',
      light: '#818cf8',
      dark: '#4f46e5',
    },
    background: {
      default: '#0f172a',
      paper: '#1e293b',
    },
  },
  typography: {
    fontFamily: '"Inter", "Roboto", sans-serif',
    button: {
      textTransform: 'none',
    },
  },
  shape: {
    borderRadius: 12,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          boxShadow: 'none',
          '&:hover': {
            boxShadow: '0 4px 12px rgba(99, 102, 241, 0.4)',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          backdropFilter: 'blur(20px)',
          backgroundColor: 'rgba(30, 41, 59, 0.8)',
        },
      },
    },
  },
});
```

### **Use in Plain MUI App:**

```tsx
// MUI app
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline, Button, Card } from '@mui/material';
import { myTheme } from './customTheme';

function MuiApp() {
  return (
    <ThemeProvider theme={myTheme}>
      <CssBaseline />
      <Card>
        <Button variant="contained">
          This uses your custom theme
        </Button>
      </Card>
    </ThemeProvider>
  );
}
```

### **Use in Toolpad App:**

```tsx
// Toolpad app - EXACT SAME THEME
import { AppProvider } from '@toolpad/core/AppProvider';
import { DashboardLayout } from '@toolpad/core/DashboardLayout';
import { Button, Card } from '@mui/material';
import { myTheme } from './customTheme'; // SAME theme file

function ToolpadApp() {
  return (
    <AppProvider theme={myTheme}>  {/* Same theme object */}
      <DashboardLayout>
        <Card>
          <Button variant="contained">
            This uses the SAME custom theme
          </Button>
        </Card>
      </DashboardLayout>
    </AppProvider>
  );
}
```

**Result:** Buttons, Cards, and all components look **identical** in both apps because they use the same styling engine and theme.

---

## **Key Differences (Not in Styling, but in Structure)**

### **MUI (Standalone)**
```tsx
import { Button } from '@mui/material';
import { ThemeProvider } from '@mui/material/styles';

// You build everything from scratch
<ThemeProvider theme={theme}>
  <YourCustomLayout>
    <YourNavigation />
    <YourContent>
      <Button>Click me</Button>
    </YourContent>
  </YourCustomLayout>
</ThemeProvider>
```

### **Toolpad**
```tsx
import { Button } from '@mui/material';
import { AppProvider } from '@toolpad/core/AppProvider';
import { DashboardLayout } from '@toolpad/core/DashboardLayout';

// Pre-built layout & navigation
<AppProvider theme={theme} navigation={[...]}>
  <DashboardLayout>
    <Button>Click me</Button>  {/* Same MUI Button */}
  </DashboardLayout>
</AppProvider>
```

**The `Button` is the exact same component. The theme system is identical. The styling APIs are identical.**

---

## **Component Styling Compatibility**

### **All these work the same in both:**

```tsx
// ✅ sx prop
<Box sx={{ p: 2, bgcolor: 'primary.main' }} />

// ✅ styled() API
const StyledDiv = styled('div')(({ theme }) => ({
  padding: theme.spacing(2),
  backgroundColor: theme.palette.primary.main,
}));

// ✅ Theme customization
components: {
  MuiButton: {
    styleOverrides: { root: { ... } },
    defaultProps: { disableRipple: true },
    variants: [{ props: { variant: 'custom' }, style: { ... } }]
  }
}

// ✅ CSS-in-JS with Emotion
const styles = css`
  padding: ${theme.spacing(2)};
  color: ${theme.palette.primary.main};
`;

// ✅ useTheme() hook
const theme = useTheme();
const color = theme.palette.primary.main;

// ✅ Theme tokens
theme.spacing(2)
theme.palette.primary.main
theme.typography.h1
theme.breakpoints.up('md')
theme.transitions.create(['background-color'])
```

---

## **Package Dependencies Comparison**

### **Plain MUI Project**
```json
{
  "dependencies": {
    "@mui/material": "^5.15.0",
    "@mui/icons-material": "^5.15.0",
    "@emotion/react": "^11.11.0",
    "@emotion/styled": "^11.11.0"
  }
}
```

### **Toolpad Project**
```json
{
  "dependencies": {
    "@toolpad/core": "^0.1.0",
    "@mui/material": "^5.15.0",      // ← Same package
    "@mui/icons-material": "^5.15.0", // ← Same package
    "@mui/x-data-grid-pro": "^6.18.0",
    "@emotion/react": "^11.11.0",     // ← Same styling engine
    "@emotion/styled": "^11.11.0"     // ← Same styling engine
  }
}
```

**Notice:** Toolpad **includes MUI as a dependency**, not a replacement.

---

## **Migration Between MUI and Toolpad**

### **Going from MUI to Toolpad**

```tsx
// Before (plain MUI)
import { ThemeProvider } from '@mui/material/styles';
import { Container, AppBar, Drawer, Box } from '@mui/material';

function App() {
  return (
    <ThemeProvider theme={myTheme}>
      <AppBar>...</AppBar>
      <Drawer>...</Drawer>
      <Container>
        <Box sx={{ p: 2 }}>Content</Box>
      </Container>
    </ThemeProvider>
  );
}

// After (Toolpad) - same theme, less boilerplate
import { AppProvider } from '@toolpad/core/AppProvider';
import { DashboardLayout } from '@toolpad/core/DashboardLayout';
import { Box } from '@mui/material';

function App() {
  return (
    <AppProvider theme={myTheme} navigation={[...]}>
      <DashboardLayout>
        <Box sx={{ p: 2 }}>Content</Box>  {/* Same Box, same sx */}
      </DashboardLayout>
    </AppProvider>
  );
}
```

**Your custom theme works without any changes.**

---

## **When to Use Each**

### **Use Plain MUI when:**
- ✅ Building custom UI from scratch
- ✅ Need maximum flexibility
- ✅ Public-facing websites/apps
- ✅ Custom layouts and navigation
- ✅ Want minimal dependencies

### **Use Toolpad when:**
- ✅ Building internal tools/dashboards
- ✅ Want pre-built layouts (DashboardLayout)
- ✅ Need rapid development
- ✅ Building CRUD/admin interfaces
- ✅ Want data grid, forms, navigation out-of-the-box

**Key insight:** You can use **both together** in the same project! Mix Toolpad's pre-built components with custom MUI components.

---

## **Can You Use Custom MUI Components in Toolpad?**

**Absolutely YES!** Since they share the same styling system:

```tsx
// Your custom MUI component
// CustomCard.tsx
import { Card, CardContent, styled } from '@mui/material';

const GradientCard = styled(Card)(({ theme }) => ({
  background: `linear-gradient(135deg, 
    ${theme.palette.primary.main}, 
    ${theme.palette.secondary.main})`,
  color: theme.palette.primary.contrastText,
}));

export function CustomCard({ children }) {
  return (
    <GradientCard>
      <CardContent>{children}</CardContent>
    </GradientCard>
  );
}

// Use in Toolpad app - works perfectly
import { AppProvider } from '@toolpad/core/AppProvider';
import { CustomCard } from './CustomCard';

function ToolpadApp() {
  return (
    <AppProvider theme={myTheme}>
      <DashboardLayout>
        <CustomCard>
          This custom MUI component works in Toolpad!
        </CustomCard>
      </DashboardLayout>
    </AppProvider>
  );
}
```

---

## **Summary**

| Feature | MUI | Toolpad |
|---------|-----|---------|
| **Styling Engine** | Emotion/@mui/system | ✅ **Same** (Emotion/@mui/system) |
| **Theme System** | createTheme() | ✅ **Same** (createTheme()) |
| **sx prop** | ✅ Yes | ✅ **Same** |
| **styled() API** | ✅ Yes | ✅ **Same** |
| **Components** | Base components | ✅ **Same** + extras |
| **Theme tokens** | theme.palette, etc | ✅ **Same** tokens |
| **Custom components** | Build from scratch | ✅ **Compatible** |

**Bottom line:**

🎯 **Toolpad IS MUI** with additional app-building features on top. They share 100% of the styling logic/code. Any theme or custom component you create for MUI works in Toolpad without modification.

Think of Toolpad as "MUI + batteries included" rather than a separate framework. Your MUI styling knowledge transfers completely. 🚀



# Merge Type

# TypeScript Module Augmentation for MUI Theme

This code **extends the MUI theme's TypeScript type definitions** to add custom properties. Let me break it down:

---

## **What Problem Does This Solve?**

By default, MUI's theme object has predefined properties like `palette`, `typography`, `spacing`, etc. If you try to add custom properties:

```tsx
const theme = createTheme({
  status: {  // ❌ TypeScript error: "status" doesn't exist on ThemeOptions
    danger: '#ff0000',
  },
});

// Later in your code:
const MyComponent = () => {
  const theme = useTheme();
  return <div style={{ color: theme.status.danger }} />; 
  // ❌ TypeScript error: Property 'status' does not exist on type 'Theme'
};
```

TypeScript will throw errors because it doesn't know about your custom `status` property.

---

## **What This Code Does**

### **Step-by-Step Breakdown:**

```tsx
declare module '@mui/material/styles' {
```
- **Tells TypeScript:** "I'm going to modify the type definitions from the `@mui/material/styles` package"
- This is called **module augmentation** or **declaration merging**

```tsx
  interface Theme {
    status: {
      danger: string;
    };
  }
```
- **Extends the `Theme` interface** (the type of the theme object you get from `useTheme()`)
- **Adds a new property:** `status.danger` of type `string`
- Now TypeScript knows that `theme.status.danger` exists when you access the theme

```tsx
  interface ThemeOptions {
    status?: {
      danger?: string;
    };
  }
```
- **Extends the `ThemeOptions` interface** (the type of the object you pass to `createTheme()`)
- **Adds the same property but as optional** (notice the `?` marks)
- This allows you to create themes with or without the `status` property

---

## **Practical Example - Before and After**

### **WITHOUT Type Declaration (TypeScript Errors):**

```tsx
import { createTheme, useTheme } from '@mui/material/styles';
import { Button } from '@mui/material';

// ❌ TypeScript error here
const theme = createTheme({
  status: {
    danger: '#e53e3e',
  },
});

function DangerButton() {
  const theme = useTheme();
  
  // ❌ TypeScript error: Property 'status' does not exist on type 'Theme'
  return (
    <Button sx={{ bgcolor: theme.status.danger }}>
      Delete
    </Button>
  );
}
```

### **WITH Type Declaration (No Errors):**

```tsx
// types/theme.d.ts
declare module '@mui/material/styles' {
  interface Theme {
    status: {
      danger: string;
    };
  }
  interface ThemeOptions {
    status?: {
      danger?: string;
    };
  }
}

// theme.ts
import { createTheme } from '@mui/material/styles';

// ✅ TypeScript now accepts this
const theme = createTheme({
  status: {
    danger: '#e53e3e',
  },
  palette: {
    primary: { main: '#1976d2' },
  },
});

export default theme;

// DangerButton.tsx
import { useTheme } from '@mui/material/styles';
import { Button } from '@mui/material';

function DangerButton() {
  const theme = useTheme();
  
  // ✅ TypeScript now provides autocomplete and type checking
  return (
    <Button sx={{ bgcolor: theme.status.danger }}>
      Delete
    </Button>
  );
}
```

---

## **Complete Real-World Example**

### **File Structure:**

```
src/
├── types/
│   └── theme.d.ts          ← Type declarations
├── theme/
│   └── customTheme.ts      ← Theme definition
└── components/
    └── AlertButton.tsx     ← Using custom theme
```

### **1. Type Declarations (`types/theme.d.ts`):**

```tsx
declare module '@mui/material/styles' {
  interface Theme {
    status: {
      danger: string;
      warning: string;
      success: string;
    };
  }
  
  interface ThemeOptions {
    status?: {
      danger?: string;
      warning?: string;
      success?: string;
    };
  }
}
```

### **2. Theme Definition (`theme/customTheme.ts`):**

```tsx
import { createTheme } from '@mui/material/styles';

const customTheme = createTheme({
  // Standard MUI theme properties
  palette: {
    primary: { main: '#1976d2' },
    secondary: { main: '#dc004e' },
  },
  
  // ✅ Custom property - now fully typed
  status: {
    danger: '#e53e3e',
    warning: '#ed8936',
    success: '#38a169',
  },
});

export default customTheme;
```

### **3. Using Custom Theme (`components/AlertButton.tsx`):**

```tsx
import { Button } from '@mui/material';
import { useTheme } from '@mui/material/styles';

function AlertButton() {
  const theme = useTheme();
  
  // ✅ Full TypeScript autocomplete for theme.status
  return (
    <>
      <Button sx={{ bgcolor: theme.status.danger, color: 'white' }}>
        Delete Account
      </Button>
      
      <Button sx={{ bgcolor: theme.status.warning, color: 'white' }}>
        Proceed with Caution
      </Button>
      
      <Button sx={{ bgcolor: theme.status.success, color: 'white' }}>
        Confirm
      </Button>
    </>
  );
}

export default AlertButton;
```

---

## **Why Use Optional Properties in ThemeOptions?**

Notice the difference:

```tsx
interface Theme {
  status: {           // ← No "?"
    danger: string;   // ← No "?"
  };
}

interface ThemeOptions {
  status?: {          // ← Optional "?"
    danger?: string;  // ← Optional "?"
  };
}
```

**Reason:**

- **`Theme`** (runtime object) - When you use `useTheme()`, you're guaranteed to get a complete theme object. MUI merges your custom theme with default values, so `status` will always exist after `createTheme()` processes it.

- **`ThemeOptions`** (input to `createTheme()`) - When creating a theme, you might not provide every custom property. Making it optional allows:

```tsx
// Valid - providing custom status
const theme1 = createTheme({
  status: { danger: '#ff0000' },
});

// Also valid - not providing custom status (uses defaults or undefined)
const theme2 = createTheme({
  palette: { primary: { main: '#000' } },
});
```

---

## **More Advanced Example - Nested Custom Properties**

```tsx
// types/theme.d.ts
declare module '@mui/material/styles' {
  interface Theme {
    customShadows: {
      card: string;
      button: string;
      dropdown: string;
    };
    customBorders: {
      light: string;
      medium: string;
      heavy: string;
    };
  }
  
  interface ThemeOptions {
    customShadows?: {
      card?: string;
      button?: string;
      dropdown?: string;
    };
    customBorders?: {
      light?: string;
      medium?: string;
      heavy?: string;
    };
  }
}

// theme.ts
import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  customShadows: {
    card: '0 4px 20px rgba(0, 0, 0, 0.1)',
    button: '0 2px 8px rgba(0, 0, 0, 0.15)',
    dropdown: '0 8px 24px rgba(0, 0, 0, 0.12)',
  },
  customBorders: {
    light: '1px solid rgba(0, 0, 0, 0.08)',
    medium: '2px solid rgba(0, 0, 0, 0.12)',
    heavy: '3px solid rgba(0, 0, 0, 0.2)',
  },
});

// Usage
import { Card } from '@mui/material';
import { useTheme } from '@mui/material/styles';

function CustomCard() {
  const theme = useTheme();
  
  return (
    <Card
      sx={{
        boxShadow: theme.customShadows.card,
        border: theme.customBorders.light,
      }}
    >
      Card content
    </Card>
  );
}
```

---

## **Benefits of This Approach**

### **1. Type Safety**
```tsx
const theme = useTheme();

// ✅ TypeScript knows about your custom properties
theme.status.danger  // autocomplete works

// ❌ TypeScript catches typos
theme.status.denger  // Error: Property 'denger' does not exist
```

### **2. Autocomplete in IDE**
When you type `theme.` your IDE shows:
- `palette`
- `typography`
- `spacing`
- `status` ← Your custom property

### **3. Refactoring Safety**
If you rename `danger` to `error`, TypeScript will show errors everywhere you used `theme.status.danger`, making refactoring safe.

### **4. Documentation**
The type definitions serve as documentation for what custom properties exist in your theme.

---

## **Common Use Cases**

### **1. Custom Color Palette**
```tsx
declare module '@mui/material/styles' {
  interface Theme {
    status: {
      danger: string;
      info: string;
      warning: string;
    };
  }
  interface ThemeOptions {
    status?: {
      danger?: string;
      info?: string;
      warning?: string;
    };
  }
}
```

### **2. Custom Spacing/Sizing System**
```tsx
declare module '@mui/material/styles' {
  interface Theme {
    customSpacing: {
      xs: number;
      sm: number;
      md: number;
      lg: number;
      xl: number;
    };
  }
  interface ThemeOptions {
    customSpacing?: {
      xs?: number;
      sm?: number;
      md?: number;
      lg?: number;
      xl?: number;
    };
  }
}
```

### **3. Brand-Specific Values**
```tsx
declare module '@mui/material/styles' {
  interface Theme {
    brand: {
      logo: string;
      primaryFont: string;
      secondaryFont: string;
      borderRadius: number;
    };
  }
  interface ThemeOptions {
    brand?: {
      logo?: string;
      primaryFont?: string;
      secondaryFont?: string;
      borderRadius?: number;
    };
  }
}
```

---

## **Summary**

| Aspect | Explanation |
|--------|-------------|
| **Purpose** | Add custom properties to MUI's theme with full TypeScript support |
| **`declare module`** | Tells TypeScript to modify existing type definitions |
| **`Theme` interface** | Runtime type - what you get from `useTheme()` |
| **`ThemeOptions` interface** | Input type - what you pass to `createTheme()` |
| **Optional `?`** | Makes properties optional in `ThemeOptions` but required in `Theme` |
| **Benefits** | Type safety, autocomplete, refactoring safety, documentation |

**In plain English:** This code tells TypeScript "Hey, MUI themes can now have a `status.danger` property. Don't show errors when I use it!" 🎯

# Module Augmentation/Declaration Merging - Origin

Great question! Let me clarify the origins:

---

## **Short Answer**

**Module augmentation and declaration merging are pure TypeScript features**, not introduced by React or MUI.

```
TypeScript ✅ (Language feature, part of TS since early versions)
React     ❌ (JavaScript library, doesn't deal with types)
MUI       ❌ (React component library, just uses TypeScript features)
```

---

## **Detailed Breakdown**

### **1. TypeScript Feature (Since ~2012)**

Declaration merging has been a **core TypeScript feature** since very early versions:

```tsx
// This is pure TypeScript, no React/MUI involved
interface User {
  name: string;
}

// Later in the same scope or different file
interface User {
  age: number;  // Merged with the previous declaration
}

// Result: User now has both name and age
const user: User = {
  name: 'Alice',
  age: 30,
};
```

**Module augmentation** was added in **TypeScript 1.8 (2016)** to extend third-party library types:

```tsx
// Extending a third-party library (pure TypeScript)
declare module 'some-library' {
  interface SomeInterface {
    newProperty: string;
  }
}
```

---

## **2. React's Relationship to This**

**React doesn't provide or require this feature.** React is a JavaScript library that can be used with or without TypeScript.

```jsx
// React works fine without TypeScript
import React from 'react';

function App() {
  return <div>Hello</div>;
}
```

However, when you use React **with TypeScript**, you get type definitions from `@types/react`:

```tsx
// React type definitions use TypeScript features
import React from 'react';

// These types come from @types/react, written in TypeScript
const MyComponent: React.FC<{ name: string }> = ({ name }) => {
  return <div>{name}</div>;
};
```

React's type definitions themselves can be augmented, but that's using TypeScript's feature:

```tsx
// Augmenting React's types (using TypeScript's feature)
declare module 'react' {
  interface CSSProperties {
    '--custom-var'?: string;  // Add CSS custom property support
  }
}

// Now you can do:
<div style={{ '--custom-var': 'red' }} />
```

---

## **3. MUI's Relationship to This**

**MUI doesn't provide this feature either.** MUI is just a React component library that:

1. **Uses TypeScript** for its own codebase
2. **Provides type definitions** for its components
3. **Documents** how to use TypeScript's module augmentation to customize their types

```tsx
// MUI's documentation shows you how to use TypeScript's feature
declare module '@mui/material/styles' {
  interface Theme {
    status: {
      danger: string;
    };
  }
  interface ThemeOptions {
    status?: {
      danger?: string;
    };
  }
}
```

**This is pure TypeScript syntax.** MUI just provides the interfaces (`Theme`, `ThemeOptions`) that you can extend.

---

## **Historical Timeline**

```
2012 - TypeScript 0.8 released
       └─ Declaration merging introduced

2013 - React released (JavaScript, no types)

2014 - DefinitelyTyped created
       └─ Community-driven type definitions for JS libraries

2016 - TypeScript 1.8
       └─ Module augmentation introduced

2016 - @types/react released
       └─ Official type definitions for React

2018 - Material-UI (now MUI) v3
       └─ Full TypeScript support added
       └─ Documentation on extending theme types
```

---

## **How Each Technology Uses Declaration Merging**

### **TypeScript (The Source)**

Provides the language feature:

```tsx
// Core TypeScript feature
interface Window {
  myCustomProperty: string;
}

// Now window.myCustomProperty is typed
window.myCustomProperty = 'hello';
```

### **React (Consumer of TypeScript)**

React's type definitions use TypeScript features:

```tsx
// From @types/react source code
declare namespace React {
  interface HTMLAttributes<T> {
    // ... properties
  }
  
  interface CSSProperties {
    // ... properties
  }
}

// You can augment these (using TypeScript's feature)
declare module 'react' {
  interface HTMLAttributes<T> {
    customAttribute?: string;
  }
}
```

### **MUI (Also a Consumer of TypeScript)**

MUI's type definitions are designed to be extensible:

```tsx
// From @mui/material/styles source code (simplified)
export interface Theme {
  palette: Palette;
  typography: Typography;
  spacing: Spacing;
  // ...
}

export interface ThemeOptions {
  palette?: PaletteOptions;
  typography?: TypographyOptions;
  spacing?: SpacingOptions;
  // ...
}

// MUI intentionally designs these interfaces to be augmented
// But the augmentation syntax is pure TypeScript
```

---

## **Other Libraries That Use the Same Pattern**

Many TypeScript-enabled libraries document module augmentation:

### **Express.js**
```tsx
// Extending Express types (TypeScript feature)
declare global {
  namespace Express {
    interface Request {
      user?: User;
    }
  }
}

// Now req.user is typed
app.get('/profile', (req, res) => {
  console.log(req.user?.name);
});
```

### **Vue.js**
```tsx
// Extending Vue types (TypeScript feature)
declare module 'vue' {
  interface ComponentCustomProperties {
    $myGlobalMethod: () => void;
  }
}

// Now this.$myGlobalMethod() is typed
```

### **styled-components**
```tsx
// Extending styled-components types (TypeScript feature)
declare module 'styled-components' {
  export interface DefaultTheme {
    colors: {
      primary: string;
      secondary: string;
    };
  }
}
```

---

## **Proof: Works Without React or MUI**

Here's module augmentation with **no React or MUI**:

```tsx
// math-utils.d.ts (third-party library types)
export interface MathUtils {
  add(a: number, b: number): number;
  subtract(a: number, b: number): number;
}

// Your code - augmenting the library
declare module './math-utils' {
  interface MathUtils {
    multiply(a: number, b: number): number;  // Add new method type
  }
}

// Usage
import { MathUtils } from './math-utils';

const utils: MathUtils = {
  add: (a, b) => a + b,
  subtract: (a, b) => a - b,
  multiply: (a, b) => a * b,  // ✅ TypeScript accepts this
};
```

**No React. No MUI. Pure TypeScript.**

---

## **Why MUI Documents This So Much**

MUI heavily documents module augmentation because:

1. **Customization is common** - Developers often extend themes
2. **TypeScript adoption** - MUI has strong TypeScript support
3. **Developer experience** - They want type safety for custom themes

But **MUI didn't invent this pattern** - they're just using TypeScript's built-in capability.

---

## **TypeScript Official Documentation**

From the TypeScript handbook:

**Declaration Merging:**
> "Declaration merging means that the compiler merges two separate declarations declared with the same name into a single definition."

**Module Augmentation:**
> "Although JavaScript modules do not support merging, you can patch existing objects by importing and then updating them."

Source: [TypeScript Handbook - Declaration Merging](https://www.typescriptlang.org/docs/handbook/declaration-merging.html)

---

## **Summary Table**

| Feature | Introduced By | When | Purpose |
|---------|---------------|------|---------|
| **Declaration Merging** | TypeScript | 2012 (v0.8) | Merge interface declarations |
| **Module Augmentation** | TypeScript | 2016 (v1.8) | Extend third-party types |
| **React** | Facebook | 2013 | UI library (JavaScript) |
| **@types/react** | DefinitelyTyped | 2016 | Type definitions for React |
| **MUI** | MUI Team | 2014 | Component library |
| **MUI TypeScript support** | MUI Team | 2018 (v3) | Type definitions using TS features |

**Key Points:**

✅ **Module augmentation = Pure TypeScript feature**  
✅ **Available for ANY TypeScript project**  
✅ **React/MUI just use this feature, didn't create it**  
✅ **Works with Node.js, Vue, Angular, vanilla TS, etc.**

Think of it like this: TypeScript invented the concept of "painting," and MUI provides you with a canvas (the `Theme` interface) that you can paint on (using module augmentation). But MUI didn't invent painting - TypeScript did! 🎨