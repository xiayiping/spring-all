# Migrating from @mui/base to Base UI

## Overview

```
@mui/base (deprecated) → @base-ui-components/react (new)
```

**Note**: The package name is `@base-ui-components/react`, not `@base-ui/react`

---

## Installation

```bash
# Remove old package
npm uninstall @mui/base

# Install new Base UI
npm install @base-ui-components/react
```

---

## Import Changes

### Old (@mui/base)

```jsx
import { Button } from '@mui/base/Button';
import { Menu } from '@mui/base/Menu';
import { MenuItem } from '@mui/base/MenuItem';
import { Modal } from '@mui/base/Modal';
import { Select } from '@mui/base/Select';
import { Slider } from '@mui/base/Slider';
import { Switch } from '@mui/base/Switch';
import { Tabs } from '@mui/base/Tabs';
import { Input } from '@mui/base/Input';
```

### New (@base-ui-components/react)

```jsx
import { Button } from '@base-ui-components/react/button';
import { Menu } from '@base-ui-components/react/menu';
import { Dialog } from '@base-ui-components/react/dialog';
import { Select } from '@base-ui-components/react/select';
import { Slider } from '@base-ui-components/react/slider';
import { Switch } from '@base-ui-components/react/switch';
import { Tabs } from '@base-ui-components/react/tabs';
import { Field } from '@base-ui-components/react/field';
```

---

## Component API Changes

### Button

```jsx
// Old @mui/base
import { Button } from '@mui/base/Button';

<Button
  slotProps={{
    root: { className: 'my-button' }
  }}
>
  Click me
</Button>

// New Base UI
import { Button } from '@base-ui-components/react/button';

<Button className="my-button">
  Click me
</Button>
```

### Menu

```jsx
// Old @mui/base
import { Menu } from '@mui/base/Menu';
import { MenuButton } from '@mui/base/MenuButton';
import { MenuItem } from '@mui/base/MenuItem';
import { Dropdown } from '@mui/base/Dropdown';

<Dropdown>
  <MenuButton>Open</MenuButton>
  <Menu>
    <MenuItem>Option 1</MenuItem>
    <MenuItem>Option 2</MenuItem>
  </Menu>
</Dropdown>

// New Base UI
import { Menu } from '@base-ui-components/react/menu';

<Menu.Root>
  <Menu.Trigger>Open</Menu.Trigger>
  <Menu.Portal>
    <Menu.Positioner>
      <Menu.Popup>
        <Menu.Item>Option 1</Menu.Item>
        <Menu.Item>Option 2</Menu.Item>
      </Menu.Popup>
    </Menu.Positioner>
  </Menu.Portal>
</Menu.Root>
```

### Modal → Dialog

```jsx
// Old @mui/base
import { Modal } from '@mui/base/Modal';

<Modal open={open} onClose={handleClose}>
  <div>Modal content</div>
</Modal>

// New Base UI
import { Dialog } from '@base-ui-components/react/dialog';

<Dialog.Root open={open} onOpenChange={setOpen}>
  <Dialog.Trigger>Open Dialog</Dialog.Trigger>
  <Dialog.Portal>
    <Dialog.Backdrop className="fixed inset-0 bg-black/50" />
    <Dialog.Popup className="fixed top-1/2 left-1/2 ...">
      <Dialog.Title>Title</Dialog.Title>
      <Dialog.Description>Description</Dialog.Description>
      <Dialog.Close>Close</Dialog.Close>
    </Dialog.Popup>
  </Dialog.Portal>
</Dialog.Root>
```

### Select

```jsx
// Old @mui/base
import { Select } from '@mui/base/Select';
import { Option } from '@mui/base/Option';

<Select defaultValue="1">
  <Option value="1">Option 1</Option>
  <Option value="2">Option 2</Option>
</Select>

// New Base UI
import { Select } from '@base-ui-components/react/select';

<Select.Root defaultValue="1">
  <Select.Trigger>
    <Select.Value />
  </Select.Trigger>
  <Select.Portal>
    <Select.Positioner>
      <Select.Popup>
        <Select.Item value="1">
          <Select.ItemText>Option 1</Select.ItemText>
        </Select.Item>
        <Select.Item value="2">
          <Select.ItemText>Option 2</Select.ItemText>
        </Select.Item>
      </Select.Popup>
    </Select.Positioner>
  </Select.Portal>
</Select.Root>
```

### Tabs

```jsx
// Old @mui/base
import { Tabs } from '@mui/base/Tabs';
import { TabsList } from '@mui/base/TabsList';
import { Tab } from '@mui/base/Tab';
import { TabPanel } from '@mui/base/TabPanel';

<Tabs defaultValue={0}>
  <TabsList>
    <Tab value={0}>Tab 1</Tab>
    <Tab value={1}>Tab 2</Tab>
  </TabsList>
  <TabPanel value={0}>Content 1</TabPanel>
  <TabPanel value={1}>Content 2</TabPanel>
</Tabs>

// New Base UI
import { Tabs } from '@base-ui-components/react/tabs';

<Tabs.Root defaultValue="tab1">
  <Tabs.List>
    <Tabs.Tab value="tab1">Tab 1</Tabs.Tab>
    <Tabs.Tab value="tab2">Tab 2</Tabs.Tab>
  </Tabs.List>
  <Tabs.Panel value="tab1">Content 1</Tabs.Panel>
  <Tabs.Panel value="tab2">Content 2</Tabs.Panel>
</Tabs.Root>
```

### Switch

```jsx
// Old @mui/base
import { Switch } from '@mui/base/Switch';

<Switch
  slotProps={{
    input: { 'aria-label': 'Toggle' }
  }}
/>

// New Base UI
import { Switch } from '@base-ui-components/react/switch';

<Switch.Root>
  <Switch.Thumb />
</Switch.Root>
```

### Slider

```jsx
// Old @mui/base
import { Slider } from '@mui/base/Slider';

<Slider defaultValue={50} />

// New Base UI
import { Slider } from '@base-ui-components/react/slider';

<Slider.Root defaultValue={50}>
  <Slider.Track>
    <Slider.Indicator />
    <Slider.Thumb />
  </Slider.Track>
</Slider.Root>
```

---

## Key Differences Summary

| Aspect | @mui/base | @base-ui-components/react |
|--------|-----------|---------------------------|
| **Package** | `@mui/base` | `@base-ui-components/react` |
| **Import path** | `@mui/base/Component` | `@base-ui-components/react/component` |
| **Component structure** | Flat imports | Compound components (`Menu.Root`, `Menu.Item`) |
| **Styling API** | `slotProps`, `slots` | Direct `className` on each part |
| **Positioning** | Built-in | Explicit `Positioner` component |
| **Portal** | Sometimes automatic | Explicit `Portal` component |

---

## Migration Checklist

```markdown
- [ ] Uninstall `@mui/base`
- [ ] Install `@base-ui-components/react`
- [ ] Update imports (lowercase paths)
- [ ] Convert to compound component pattern (`.Root`, `.Trigger`, etc.)
- [ ] Replace `slotProps` with direct `className`
- [ ] Add explicit `Portal` and `Positioner` where needed
- [ ] Update event handlers (`onClose` → `onOpenChange`)
- [ ] Test all components
```

---

## Adding Tailwind Styling

```jsx
import { Menu } from '@base-ui-components/react/menu';

function DropdownMenu({ children }) {
  return (
    <Menu.Root>
      <Menu.Trigger className="px-4 py-2 bg-blue-500 text-white rounded">
        Open Menu
      </Menu.Trigger>
      <Menu.Portal>
        <Menu.Positioner className="z-50">
          <Menu.Popup className="bg-white rounded-md shadow-lg p-1 min-w-[200px]">
            {children}
          </Menu.Popup>
        </Menu.Positioner>
      </Menu.Portal>
    </Menu.Root>
  );
}

function DropdownMenuItem({ children, ...props }) {
  return (
    <Menu.Item 
      className="px-3 py-2 text-sm rounded hover:bg-gray-100 cursor-pointer"
      {...props}
    >
      {children}
    </Menu.Item>
  );
}
```

---

## Available Components in Base UI

```jsx
import { Accordion } from '@base-ui-components/react/accordion';
import { AlertDialog } from '@base-ui-components/react/alert-dialog';
import { Checkbox } from '@base-ui-components/react/checkbox';
import { Collapsible } from '@base-ui-components/react/collapsible';
import { Dialog } from '@base-ui-components/react/dialog';
import { Field } from '@base-ui-components/react/field';
import { Form } from '@base-ui-components/react/form';
import { Menu } from '@base-ui-components/react/menu';
import { NumberField } from '@base-ui-components/react/number-field';
import { Popover } from '@base-ui-components/react/popover';
import { Progress } from '@base-ui-components/react/progress';
import { Radio } from '@base-ui-components/react/radio';
import { ScrollArea } from '@base-ui-components/react/scroll-area';
import { Select } from '@base-ui-components/react/select';
import { Separator } from '@base-ui-components/react/separator';
import { Slider } from '@base-ui-components/react/slider';
import { Switch } from '@base-ui-components/react/switch';
import { Tabs } from '@base-ui-components/react/tabs';
import { Toast } from '@base-ui-components/react/toast';
import { Toggle } from '@base-ui-components/react/toggle';
import { ToggleGroup } from '@base-ui-components/react/toggle-group';
import { Tooltip } from '@base-ui-components/react/tooltip';
```

---

## Resources

- **Documentation**: https://base-ui.com/
- **GitHub**: https://github.com/mui/base-ui
- **Migration Guide**: https://base-ui.com/guides/migration
