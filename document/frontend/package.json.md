# Package.json Dependency Version Ranges

## Basic Syntax

```json
{
  "dependencies": {
    "package-name": "VERSION_RANGE"
  }
}
```

---

## Version Number Format (SemVer)

```
MAJOR.MINOR.PATCH
  │      │     │
  │      │     └── Bug fixes (backward compatible)
  │      └──────── New features (backward compatible)
  └─────────────── Breaking changes
```

Example: `2.4.1` = Major 2, Minor 4, Patch 1

---

## Version Range Specifiers

### 1. **Exact Version**

```json
{
  "lodash": "4.17.21"
}
```
Installs **exactly** `4.17.21`

---

### 2. **Caret `^` (Most Common)**

Allows **minor and patch** updates.

```json
{
  "react": "^18.2.0"
}
```

| Range | Allows |
|-------|--------|
| `^18.2.0` | `>=18.2.0 <19.0.0` |
| `^0.4.0` | `>=0.4.0 <0.5.0` (special case for 0.x) |
| `^0.0.3` | `>=0.0.3 <0.0.4` (special case for 0.0.x) |

```
^18.2.0
   │ │
   │ └── Patch can change (18.2.0, 18.2.1, 18.2.99...)
   └──── Minor can change (18.2.0, 18.3.0, 18.99.0...)
         Major is LOCKED (must be 18)
```

---

### 3. **Tilde `~`**

Allows only **patch** updates.

```json
{
  "express": "~4.18.0"
}
```

| Range | Allows |
|-------|--------|
| `~4.18.0` | `>=4.18.0 <4.19.0` |
| `~4.18` | `>=4.18.0 <4.19.0` |

```
~4.18.0
   │  │
   │  └── Patch can change (4.18.0, 4.18.1, 4.18.99...)
   └───── Minor is LOCKED (must be 18)
          Major is LOCKED (must be 4)
```

---

### 4. **Wildcards `*` `x`**

```json
{
  "pkg1": "*",        // Any version
  "pkg2": "4.x",      // Any 4.x.x
  "pkg3": "4.18.x"    // Any 4.18.x
}
```

---

### 5. **Ranges with Operators**

```json
{
  "pkg1": ">4.0.0",           // Greater than 4.0.0
  "pkg2": ">=4.0.0",          // Greater than or equal
  "pkg3": "<5.0.0",           // Less than 5.0.0
  "pkg4": "<=5.0.0",          // Less than or equal
  "pkg5": ">=4.0.0 <5.0.0",   // Between (AND)
  "pkg6": "4.0.0 || 5.0.0"    // Either version (OR)
}
```

---

### 6. **Hyphen Ranges**

```json
{
  "lodash": "4.0.0 - 4.17.0"
}
```
Equivalent to `>=4.0.0 <=4.17.0`

---

## Special Values

```json
{
  "pkg1": "latest",                                    // Latest published
  "pkg2": "next",                                      // Next tag (pre-release)
  "pkg3": "git+https://github.com/user/repo.git",     // Git URL
  "pkg4": "git+ssh://git@github.com/user/repo.git",   // Git SSH
  "pkg5": "user/repo#branch",                          // GitHub shorthand
  "pkg6": "file:../local-package",                     // Local file
  "pkg7": "npm:other-package@^2.0.0"                   // Alias
}
```

---

## Comparison Table

| Symbol | Name | Example | Matches |
|--------|------|---------|---------|
| (none) | Exact | `4.17.21` | Only `4.17.21` |
| `^` | Caret | `^4.17.21` | `4.17.21` to `<5.0.0` |
| `~` | Tilde | `~4.17.21` | `4.17.21` to `<4.18.0` |
| `*` | Wildcard | `*` | Any version |
| `>` | Greater | `>4.0.0` | Above `4.0.0` |
| `>=` | Greater/Equal | `>=4.0.0` | `4.0.0` and above |
| `<` | Less | `<5.0.0` | Below `5.0.0` |
| `<=` | Less/Equal | `<=5.0.0` | `5.0.0` and below |
| `-` | Range | `4.0.0 - 5.0.0` | Between inclusive |
| `\|\|` | Or | `4.x \|\| 5.x` | Either range |

---

## Practical Examples

```json
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "lodash": "~4.17.0",
    "typescript": ">=5.0.0 <6.0.0",
    "some-stable-lib": "2.1.3",
    "@company/internal": "file:../internal-pkg"
  },
  "devDependencies": {
    "vite": "^5.0.0",
    "eslint": "^8.0.0 || ^9.0.0"
  }
}
```

---

## Quick Decision Guide

| Situation | Use |
|-----------|-----|
| Most packages | `^` (caret) |
| Need stability | `~` (tilde) |
| Critical/sensitive | Exact version |
| Peer dependencies | Wide range `>=16.8.0` |
| Monorepo local packages | `workspace:*` or `file:` |

---

## Test Your Ranges

Use the [npm semver calculator](https://semver.npmjs.com/) to test ranges!
