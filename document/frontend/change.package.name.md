# Change Package Name

Edit the `name` field in your `package.json` file.

## Method 1: Edit Directly

**`package.json`**
```json
{
  "name": "your-new-package-name",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  ...
}
```

## Method 2: Use npm command

```bash
npm pkg set name="your-new-package-name"
```

## Naming Rules

| Rule | Valid | Invalid |
|------|-------|---------|
| Lowercase only | `my-app` | `My-App` |
| No spaces | `my-app` | `my app` |
| Can use hyphens/underscores | `my-app`, `my_app` | |
| Can use dots | `my.app` | |
| Max 214 characters | | |
| No special characters | `my-app` | `my@app!` |

## Examples

```json
// ✅ Valid names
"name": "my-react-app"
"name": "company-dashboard"
"name": "page1-page2-project"
"name": "@myorg/my-app"        // Scoped package

// ❌ Invalid names
"name": "My React App"         // Has spaces and uppercase
"name": "my@app"               // Special character
```

## After Changing

```bash
# Reinstall dependencies (optional but recommended)
rm -rf node_modules
rm package-lock.json
npm install
```

That's it! The package name is just for identification and npm publishing - it won't affect your app's functionality.
