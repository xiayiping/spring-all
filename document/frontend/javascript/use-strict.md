# use strict
**`'use strict';`** is a JavaScript directive that enables **strict mode** — a restricted variant of JavaScript that catches common coding errors and prevents unsafe actions.

## What It Does

Strict mode makes JavaScript more secure, cleaner, and easier to debug by:
1. **Catching silent errors** - Throws errors instead of failing silently
2. **Preventing bad syntax** - Disallows problematic JavaScript features
3. **Improving performance** - Allows engines to optimize code better
4. **Preparing for future JS** - Reserves keywords for future versions

## How to Use It

### Option 1: **Global Strict Mode** (Entire Script)

```javascript
'use strict';

// Everything below runs in strict mode
var x = 10;
function myFunc() {
    console.log('Strict mode enabled');
}
```

### Option 2: **Function-Level Strict Mode** (Only Inside Function)

```javascript
function myFunction() {
    'use strict';
    // Only this function runs in strict mode
    var x = 10;
}

// Outside the function is NOT strict mode
y = 20; // This is allowed (sloppy mode)
```

### Option 3: **Module Strict Mode** (Automatic)

```javascript
// ES6 modules are automatically in strict mode
export function myFunc() {
    // Already strict, no need for 'use strict';
}
```

## Key Differences: Strict vs Sloppy Mode

### 1. **Prevents Accidental Globals**

```javascript
// WITHOUT strict mode (sloppy mode)
function calculate() {
    result = 10 * 5; // Forgot 'var'/'let'/'const' - creates global!
    return result;
}
calculate();
console.log(result); // 50 (global variable created accidentally)

// WITH strict mode
'use strict';
function calculate() {
    result = 10 * 5; // ❌ ReferenceError: result is not defined
    return result;
}
```

### 2. **Prevents Duplicate Parameter Names**

```javascript
// WITHOUT strict mode
function sum(a, b, a) { // Duplicate 'a' allowed
    return a + b; // Only last 'a' is used
}
console.log(sum(1, 2, 3)); // 5 (uses 3 + 2)

// WITH strict mode
'use strict';
function sum(a, b, a) { // ❌ SyntaxError: Duplicate parameter name
    return a + b;
}
```

### 3. **Prevents Deleting Variables/Functions**

```javascript
// WITHOUT strict mode
var x = 10;
delete x; // Silently fails, x still exists
console.log(x); // 10

// WITH strict mode
'use strict';
var x = 10;
delete x; // ❌ SyntaxError: Delete of an unqualified identifier
```

### 4. **Makes `this` Undefined in Functions**

```javascript
// WITHOUT strict mode
function showThis() {
    console.log(this); // Window object (in browser)
}
showThis();

// WITH strict mode
'use strict';
function showThis() {
    console.log(this); // undefined (safer!)
}
showThis();
```

This prevents accidentally modifying global objects.

### 5. **Prevents Octal Syntax**

```javascript
// WITHOUT strict mode
var num = 010; // Octal (base 8) = 8 in decimal
console.log(num); // 8

// WITH strict mode
'use strict';
var num = 010; // ❌ SyntaxError: Octal literals are not allowed
```

### 6. **Prevents Writing to Read-Only Properties**

```javascript
// WITHOUT strict mode
var obj = {};
Object.defineProperty(obj, 'x', { value: 42, writable: false });
obj.x = 100; // Silently fails
console.log(obj.x); // 42 (unchanged)

// WITH strict mode
'use strict';
var obj = {};
Object.defineProperty(obj, 'x', { value: 42, writable: false });
obj.x = 100; // ❌ TypeError: Cannot assign to read only property
```

### 7. **Reserves Future Keywords**

```javascript
// WITHOUT strict mode
var implements = 10; // 'implements' is a reserved word
var interface = 20;  // Works in sloppy mode

// WITH strict mode
'use strict';
var implements = 10; // ❌ SyntaxError: Unexpected strict mode reserved word
var interface = 20;  // ❌ SyntaxError
```

Reserved words: `implements`, `interface`, `let`, `package`, `private`, `protected`, `public`, `static`, `yield`

### 8. **Prevents `with` Statement**

```javascript
// WITHOUT strict mode
var obj = { x: 10, y: 20 };
with (obj) { // Confusing and slow
    console.log(x); // 10
}

// WITH strict mode
'use strict';
with (obj) { // ❌ SyntaxError: Strict mode code may not include a with statement
    console.log(x);
}
```

### 9. **`eval()` Has Its Own Scope**

```javascript
// WITHOUT strict mode
eval('var x = 10');
console.log(x); // 10 (x leaked into outer scope!)

// WITH strict mode
'use strict';
eval('var x = 10');
console.log(x); // ❌ ReferenceError: x is not defined (safer!)
```

### 10. **`arguments` Object Behavior**

```javascript
// WITHOUT strict mode
function test(a) {
    a = 100;
    console.log(arguments[0]); // 100 (synced with parameter!)
}
test(1);

// WITH strict mode
'use strict';
function test(a) {
    a = 100;
    console.log(arguments[0]); // 1 (not synced, safer!)
}
test(1);
```

## Complete Comparison Table

| Feature | Sloppy Mode | Strict Mode |
|---------|-------------|-------------|
| Undeclared variables | Creates global | ❌ ReferenceError |
| Duplicate parameters | Allowed | ❌ SyntaxError |
| Delete variables | Silently fails | ❌ SyntaxError |
| `this` in functions | Global object | `undefined` |
| Octal literals (`077`) | Allowed | ❌ SyntaxError |
| Write to read-only | Silently fails | ❌ TypeError |
| Reserved keywords | Some allowed | ❌ SyntaxError |
| `with` statement | Allowed | ❌ SyntaxError |
| `eval()` scope | Leaks variables | Isolated scope |
| `arguments` sync | Synced with params | Not synced |

## Practical Examples

### Example 1: Preventing Common Mistakes

```javascript
'use strict';

// Typo in variable name - caught immediately!
let userName = 'John';
usrName = 'Jane'; // ❌ ReferenceError (typo caught!)

// Without strict mode, this creates a global 'usrName'
```

### Example 2: Safer Object Manipulation

```javascript
'use strict';

const obj = Object.freeze({ name: 'John' });

// Try to modify frozen object
obj.name = 'Jane';  // ❌ TypeError: Cannot assign to read only property
obj.age = 30;       // ❌ TypeError: Cannot add property

delete obj.name;    // ❌ TypeError: Cannot delete property
```

### Example 3: Class-Like Pattern (Common Use Case)

```javascript
'use strict';

function Person(name, age) {
    // Without strict mode, forgetting 'new' makes 'this' = window
    // With strict mode, 'this' is undefined, preventing bugs
    
    if (!(this instanceof Person)) {
        throw new Error('Constructor must be called with new');
    }
    
    this.name = name;
    this.age = age;
}

const person = Person('John', 30); // ❌ Error caught!
const person2 = new Person('Jane', 25); // ✅ Works correctly
```

### Example 4: IIFE (Immediately Invoked Function Expression)

```javascript
(function() {
    'use strict';
    
    // All code here is in strict mode
    // But outside this function isn't affected
    
    let privateVar = 'secret';
    
    window.myApp = {
        getData: function() {
            return privateVar;
        }
    };
})();

// Sloppy mode outside
undeclaredVar = 10; // This would work (creates global)
```

## When to Use Strict Mode?

### ✅ **Always Use It:**

1. **New projects** - Start with strict mode from day one
2. **Modern code** - It's best practice
3. **ES6 modules** - Already strict by default
4. **Working with classes** - Prevents common mistakes
5. **Production code** - More reliable and secure

### ⚠️ **Be Careful:**

1. **Old codebases** - May break existing code
2. **Third-party scripts** - Don't force strict mode on code you don't control
3. **Gradual migration** - Use function-level strict mode for incremental updates

## Browser Support

✅ **All modern browsers support strict mode:**
- Chrome: All versions
- Firefox: All versions
- Safari: All versions
- Edge: All versions
- IE: 10+ (IE 9 ignores it but doesn't error)

## Modern JavaScript (ES6+) Recommendation

```javascript
// ✅ BEST: Use ES6 modules (automatically strict)
// main.js
export function myFunction() {
    // Already in strict mode
}

// ✅ GOOD: Function-level in legacy code
function legacyCode() {
    'use strict';
    // Only this function is strict
}

// ⚠️ OK: Global strict in standalone scripts
'use strict';
// Entire file is strict

// ❌ AVOID: No strict mode in new code
function oldStyle() {
    // Sloppy mode - not recommended
}
```

## Real-World Example

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Strict Mode Demo