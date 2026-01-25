# Best Practice 

# Best Practices for Using `None` in Python

`None` is Python's singleton object representing the absence of a value. Here are the best practices:

---

## **1. Checking for None - Use `is` and `is not`**

### ✅ **Correct Way**
```python
# Use identity operators
if value is None:
    print("Value is None")

if value is not None:
    print("Value exists")
```

### ❌ **Incorrect Way**
```python
# Don't use equality operators
if value == None:  # Bad practice
    print("Value is None")

if value != None:  # Bad practice
    print("Value exists")
```

**Why?**
- `None` is a singleton - there's only one `None` object in memory
- `is` checks object identity (same object in memory), which is faster
- `==` checks value equality, which can be overridden by custom classes

```python
class Weird:
    def __eq__(self, other):
        return True  # Always returns True

weird = Weird()
print(weird == None)   # True (misleading!)
print(weird is None)   # False (correct!)
```

---

## **2. Default Function Arguments**

### ✅ **Use None for Mutable Defaults**
```python
def add_item(item, items=None):
    """Correct way to handle mutable default arguments."""
    if items is None:
        items = []
    items.append(item)
    return items

# Works correctly
list1 = add_item('apple')
list2 = add_item('banana')
print(list1)  # ['apple']
print(list2)  # ['banana']
```

### ❌ **Don't Use Mutable Objects as Defaults**
```python
def add_item(item, items=[]):  # Dangerous!
    """This creates a shared list across all calls."""
    items.append(item)
    return items

# Unexpected behavior
list1 = add_item('apple')
list2 = add_item('banana')
print(list1)  # ['apple', 'banana'] - Unexpected!
print(list2)  # ['apple', 'banana'] - Same list!
```

**Common Pattern:**
```python
def process_data(data, options=None):
    if options is None:
        options = {}
    # Use options...

def create_user(name, tags=None):
    if tags is None:
        tags = []
    # Use tags...
```

---

## **3. Return None Explicitly for Clarity**

### ✅ **Explicit is Better**
```python
def find_user(user_id):
    """Find user by ID."""
    user = database.get(user_id)
    if user:
        return user
    return None  # Explicit - clearly indicates "not found"

def validate_input(data):
    """Validate input data."""
    if not data:
        return None  # Explicit failure indication
    # Process data...
    return processed_data
```

### ⚠️ **Implicit None (Less Clear)**
```python
def find_user(user_id):
    """Find user by ID."""
    user = database.get(user_id)
    if user:
        return user
    # Implicitly returns None - less obvious

# This works but is less clear for readers
```

**Note:** Python functions implicitly return `None` if no return statement is executed, but being explicit improves code readability.

---

## **4. Type Hints with Optional**

### ✅ **Modern Way (Python 3.10+)**
```python
def greet(name: str | None = None) -> str:
    """Greet a person by name."""
    if name is None:
        return "Hello, stranger!"
    return f"Hello, {name}!"

def find_user(user_id: int) -> dict | None:
    """Find user, returns None if not found."""
    user = database.get(user_id)
    return user if user else None
```

### ✅ **Legacy Way (Python 3.5-3.9)**
```python
from typing import Optional

def greet(name: Optional[str] = None) -> str:
    """Greet a person by name."""
    if name is None:
        return "Hello, stranger!"
    return f"Hello, {name}!"

def find_user(user_id: int) -> Optional[dict]:
    """Find user, returns None if not found."""
    user = database.get(user_id)
    return user if user else None
```

**Key Points:**
- `Optional[T]` is equivalent to `T | None`
- Makes it clear the function can return `None`
- Helps with static type checking (mypy, pyright)

---

## **5. Distinguish None from False, 0, Empty Collections**

### ✅ **Explicit None Checks**
```python
def process_config(config):
    """Process configuration."""
    # Check specifically for None
    if config is None:
        config = get_default_config()
    
    # This allows empty dict {} to be valid
    return config

def get_page(page_num):
    """Get page number, 0 is valid."""
    if page_num is None:
        page_num = 1  # Default to first page
    
    # page_num can be 0 (valid page number)
    return fetch_page(page_num)
```

### ❌ **Conflating None with Falsy Values**
```python
def process_config(config):
    """Problematic - treats empty dict like None."""
    if not config:  # This is True for None, {}, [], 0, False, ""
        config = get_default_config()
    
    # Empty dict {} will be replaced, which might not be intended
    return config
```

**Falsy Values in Python:**
```python
# All of these are falsy
bool(None)    # False
bool(0)       # False
bool("")      # False
bool([])      # False
bool({})      # False
bool(False)   # False

# But they're not all None!
0 is None     # False
"" is None    # False
[] is None    # False
```

---

## **6. Using None as a Sentinel Value**

### ✅ **Sentinel Pattern**
```python
_MISSING = object()  # Unique sentinel object

def update_user(user_id, name=_MISSING, email=_MISSING):
    """Update user fields. Distinguishes between None and 'not provided'."""
    updates = {}
    
    if name is not _MISSING:
        updates['name'] = name  # Can set to None explicitly
    
    if email is not _MISSING:
        updates['email'] = email  # Can set to None explicitly
    
    database.update(user_id, updates)

# Usage:
update_user(123, name=None)  # Explicitly set name to None
update_user(123, email="new@email.com")  # Don't touch name field
```

**Why Not Use None?**
```python
def update_user(user_id, name=None, email=None):
    """Can't distinguish between 'not provided' and 'set to None'."""
    if name is not None:
        # Problem: Can't explicitly set name to None in database
        database.update_name(user_id, name)
```

---

## **7. Avoid Returning Different Types Including None**

### ❌ **Inconsistent Return Types**
```python
def get_data(source):
    """Bad: Returns different types."""
    if source == 'api':
        return {'data': [1, 2, 3]}  # Returns dict
    elif source == 'cache':
        return [1, 2, 3]  # Returns list
    else:
        return None  # Returns None
    
# Caller must handle multiple types
result = get_data('unknown')
if isinstance(result, dict):
    data = result['data']
elif isinstance(result, list):
    data = result
else:
    data = []
```

### ✅ **Consistent Return Types**
```python
def get_data(source):
    """Good: Always returns dict or None."""
    if source == 'api':
        return {'data': [1, 2, 3], 'source': 'api'}
    elif source == 'cache':
        return {'data': [1, 2, 3], 'source': 'cache'}
    else:
        return None

# Simpler handling
result = get_data('unknown')
if result is None:
    data = []
else:
    data = result['data']
```

### ✅ **Or Use Exceptions**
```python
def get_data(source):
    """Raises exception instead of returning None."""
    if source == 'api':
        return {'data': [1, 2, 3], 'source': 'api'}
    elif source == 'cache':
        return {'data': [1, 2, 3], 'source': 'cache'}
    else:
        raise ValueError(f"Unknown source: {source}")

# Clear error handling
try:
    result = get_data('unknown')
    data = result['data']
except ValueError as e:
    print(f"Error: {e}")
    data = []
```

---

## **8. Using None with Collections**

### ✅ **Filter Out None Values**
```python
# Remove None values from list
items = [1, None, 2, None, 3]

# Method 1: List comprehension
filtered = [x for x in items if x is not None]

# Method 2: filter()
filtered = list(filter(lambda x: x is not None, items))

# Method 3: filter() with None (removes all falsy values)
filtered = list(filter(None, items))  # Removes None, 0, "", False, etc.

print(filtered)  # [1, 2, 3]
```

### ✅ **Dictionary with None Values**
```python
# Remove None values from dict
data = {'name': 'Alice', 'age': None, 'city': 'NYC', 'email': None}

# Dictionary comprehension
cleaned = {k: v for k, v in data.items() if v is not None}
print(cleaned)  # {'name': 'Alice', 'city': 'NYC'}
```

---

## **9. None in Conditional Expressions**

### ✅ **Using None with Ternary Operator**
```python
# Get value or default
result = value if value is not None else default

# Shorter alternative (but be careful with falsy values!)
result = value or default  # Replaces any falsy value, not just None
```

### ✅ **Walrus Operator with None Check (Python 3.8+)**
```python
# Traditional way
user = find_user(user_id)
if user is not None:
    print(user.name)

# With walrus operator
if (user := find_user(user_id)) is not None:
    print(user.name)
```

---

## **10. Documentation and Type Hints**

### ✅ **Document None Returns**
```python
def find_user(user_id: int) -> dict | None:
    """
    Find a user by ID.
    
    Args:
        user_id: The user's unique identifier.
    
    Returns:
        User dictionary if found, None otherwise.
    
    Example:
        >>> user = find_user(123)
        >>> if user is not None:
        ...     print(user['name'])
    """
    return database.get(user_id)
```

---

## **11. Common Patterns and Idioms**

### **Pattern 1: Get or Default**
```python
# Using dict.get()
config = settings.get('database', None)  # Returns None if key missing

# Or provide default
config = settings.get('database', {})  # Returns {} if key missing
```

### **Pattern 2: Chain of None Checks**
```python
# Traditional way
if user is not None:
    if user.profile is not None:
        if user.profile.settings is not None:
            theme = user.profile.settings.theme

# Using getattr with default
theme = getattr(
    getattr(getattr(user, 'profile', None), 'settings', None),
    'theme',
    None
)

# Or using try-except
try:
    theme = user.profile.settings.theme
except AttributeError:
    theme = None
```

### **Pattern 3: Optional Chaining (Python 3.11+ with typing)**
```python
from typing import Optional

def get_theme(user: Optional[dict]) -> Optional[str]:
    """Safely get theme from nested structure."""
    if user is None:
        return None
    
    profile = user.get('profile')
    if profile is None:
        return None
    
    settings = profile.get('settings')
    if settings is None:
        return None
    
    return settings.get('theme')
```

---

## **12. Testing for None**

### ✅ **In Unit Tests**
```python
import unittest

class TestUserFunctions(unittest.TestCase):
    def test_find_user_returns_none_when_not_found(self):
        """Test that find_user returns None for invalid ID."""
        result = find_user(999)
        self.assertIsNone(result)  # Use assertIsNone
    
    def test_find_user_returns_user_when_found(self):
        """Test that find_user returns user dict when found."""
        result = find_user(1)
        self.assertIsNotNone(result)  # Use assertIsNotNone
        self.assertEqual(result['id'], 1)
```

---

## **Common Mistakes to Avoid**

### ❌ **Mistake 1: Using `== None`**
```python
if x == None:  # Bad
    pass
```

### ❌ **Mistake 2: Mutable Default Arguments**
```python
def func(items=[]):  # Bad - shared across calls
    pass
```

### ❌ **Mistake 3: Treating None Like False**
```python
if not value:  # Ambiguous - could be None, 0, "", [], etc.
    pass
```

### ❌ **Mistake 4: Not Type Hinting None Returns**
```python
def func() -> dict:  # Bad - can actually return None
    return None
```

---

## **Summary Checklist**

✅ Use `is None` and `is not None` for comparisons  
✅ Use `None` for mutable default arguments  
✅ Return `None` explicitly when appropriate  
✅ Use type hints: `T | None` or `Optional[T]`  
✅ Distinguish `None` from other falsy values  
✅ Document when functions can return `None`  
✅ Use sentinel objects when `None` is a valid value  
✅ Keep return types consistent  
✅ Use `assertIsNone()` and `assertIsNotNone()` in tests  
✅ Consider exceptions instead of returning `None` for errors

These practices will make your Python code more robust, readable, and maintainable!

# With Optional

# Python `Optional` Best Practices & Underlying Types

## **Part 1: What is `Optional` Exactly?**

### **The Truth: `Optional` is Just a Type Alias**

```python
from typing import Optional, Union

# These are EXACTLY the same:
Optional[str]  # Syntactic sugar
Union[str, None]  # What Optional actually means
str | None  # Modern syntax (Python 3.10+)

# Proof:
print(Optional[str] == Union[str, None])  # True
print(Optional[str])  # typing.Union[str, None]
```

**Key Insight:** `Optional[T]` is purely a **type hint** for static type checkers. It has **zero runtime effect**.

```python
def greet(name: Optional[str]) -> str:
    return f"Hello, {name}"

# At runtime, this is just:
def greet(name) -> str:
    return f"Hello, {name}"

# The type hints are completely ignored by Python interpreter
```

---

## **Part 2: What is the Underlying Type of `None`?**

### **`None` is a Singleton of Type `NoneType`**

```python
# None is an instance of NoneType
print(type(None))  # <class 'NoneType'>

# There's only ONE None object in the entire Python process
a = None
b = None
print(a is b)  # True - same object in memory
print(id(a) == id(b))  # True - same memory address

# NoneType cannot be instantiated
NoneType = type(None)
try:
    another_none = NoneType()  # TypeError
except TypeError as e:
    print(e)  # "cannot create 'NoneType' instances"
```

### **Memory Representation**

```python
import sys

# None is a singleton - always same memory address
print(id(None))  # e.g., 4307394512 (same across your program)

# Size of None object
print(sys.getsizeof(None))  # 16 bytes (on 64-bit Python)

# None is immutable
try:
    None.x = 5  # AttributeError
except AttributeError:
    print("None is immutable")
```

### **None in CPython Source Code**

In CPython (the standard Python implementation), `None` is defined in C:

```c
// From CPython source: Objects/object.c
PyObject _Py_NoneStruct = {
  _PyObject_EXTRA_INIT
  1, &_PyNone_Type  // Reference count of 1, pointer to NoneType
};
```

**Key Points:**
- `None` is implemented as a global C struct
- It's created once when Python starts
- All references to `None` point to the same object
- That's why `is None` is the correct way to check (identity check)

---

## **Part 3: Runtime Behavior of Optional**

### **Optional Has NO Runtime Effect**

```python
from typing import Optional
import timeit

# These functions are IDENTICAL at runtime
def func1(x: Optional[str]) -> Optional[int]:
    if x is None:
        return None
    return len(x)

def func2(x):
    if x is None:
        return None
    return len(x)

# Performance is identical
time1 = timeit.timeit(lambda: func1("hello"), number=1000000)
time2 = timeit.timeit(lambda: func2("hello"), number=1000000)
print(f"With types: {time1:.4f}s")
print(f"Without types: {time2:.4f}s")
# Times are essentially the same

# Type hints are stored in __annotations__
print(func1.__annotations__)
# {'x': typing.Union[str, NoneType], 'return': typing.Union[int, NoneType]}

# But they don't affect execution
print(func1(123))  # No error! Returns None (wrong but runs)
```

### **Type Checkers Catch Errors (Static Analysis)**

```python
from typing import Optional

def greet(name: Optional[str]) -> str:
    # Type checker (mypy) will warn about this
    return name.upper()  # Error: name might be None
    
def greet_safe(name: Optional[str]) -> str:
    # Type checker is happy
    if name is None:
        return "Hello, stranger"
    return name.upper()  # OK: name is str here

# Run mypy:
# $ mypy script.py
# script.py:4: error: Item "None" of "Optional[str]" has no attribute "upper"
```

---

## **Part 4: Best Practices for Using `Optional`**

### **Practice 1: Use Modern Syntax (Python 3.10+)**

```python
# ✅ Modern and clean (Python 3.10+)
def process(data: str | None) -> int | None:
    if data is None:
        return None
    return len(data)

# ⚠️ Legacy but still valid (Python 3.5-3.9)
from typing import Optional

def process(data: Optional[str]) -> Optional[int]:
    if data is None:
        return None
    return len(data)

# ❌ Don't mix syntaxes unnecessarily
def process(data: Optional[str]) -> int | None:  # Inconsistent
    pass
```

**Migration Guide:**
```python
# Old code (Python 3.5-3.9)
from typing import Optional, Union, List, Dict

def func(
    name: Optional[str],
    items: Union[List[int], None],
    config: Optional[Dict[str, str]]
) -> Optional[str]:
    pass

# New code (Python 3.10+)
def func(
    name: str | None,
    items: list[int] | None,
    config: dict[str, str] | None
) -> str | None:
    pass
```

---

### **Practice 2: Always Check None Before Using**

```python
from typing import Optional

# ❌ Unsafe - will crash if name is None
def greet_bad(name: Optional[str]) -> str:
    return f"Hello, {name.upper()}"  # AttributeError if name is None

# ✅ Safe - explicit None check
def greet_good(name: Optional[str]) -> str:
    if name is None:
        return "Hello, stranger"
    return f"Hello, {name.upper()}"

# ✅ Safe - using or operator (if None is not a valid value)
def greet_with_default(name: Optional[str]) -> str:
    actual_name = name or "stranger"
    return f"Hello, {actual_name.upper()}"

# ⚠️ Be careful with or operator - it replaces ALL falsy values
def process(value: Optional[int]) -> int:
    return value or 0  # Problem: 0 is falsy, but might be valid!

# ✅ Better - explicit None check
def process_safe(value: Optional[int]) -> int:
    return value if value is not None else 0
```

---

### **Practice 3: Use Type Narrowing**

Python type checkers understand control flow and narrow types:

```python
from typing import Optional

def process_user(user_id: Optional[int]) -> str:
    # Type checker knows user_id could be None here
    
    if user_id is None:
        return "No user"
    
    # Type checker knows user_id is int here (not None)
    return f"User {user_id * 2}"  # OK to use as int
    
def process_data(data: str | None) -> int:
    # Type narrowing with assert
    assert data is not None, "Data cannot be None"
    # Type checker knows data is str here
    return len(data)

def get_value(items: list[int] | None) -> int:
    if not items:  # Careful: also True for empty list []
        return 0
    return items[0]

# Better - explicit None check
def get_value_safe(items: list[int] | None) -> int:
    if items is None:
        return 0
    if len(items) == 0:
        return 0
    return items[0]
```

---

### **Practice 4: Don't Overuse Optional**

```python
# ❌ Too many Optional parameters make API unclear
def create_user(
    name: str | None = None,
    email: str | None = None,
    age: int | None = None,
    address: str | None = None,
    phone: str | None = None
) -> dict:
    """Too many optional parameters - unclear what's required."""
    pass

# ✅ Better - make required params explicit
def create_user(
    name: str,  # Required
    email: str,  # Required
    age: int | None = None,  # Optional
    phone: str | None = None  # Optional
) -> dict:
    """Clear which parameters are required."""
    pass

# ✅ Or use a config object
from dataclasses import dataclass

@dataclass
class UserConfig:
    age: int | None = None
    address: str | None = None
    phone: str | None = None

def create_user(name: str, email: str, config: UserConfig | None = None) -> dict:
    """Cleaner API with optional config object."""
    if config is None:
        config = UserConfig()
    # Process...
```

---

### **Practice 5: Document Why Something Can Be None**

```python
from typing import Optional

# ❌ Unclear why None is returned
def find_user(user_id: int) -> Optional[dict]:
    pass

# ✅ Document the None case
def find_user(user_id: int) -> dict | None:
    """
    Find a user by ID.
    
    Args:
        user_id: The user's unique identifier
        
    Returns:
        User dictionary if found, None if user doesn't exist
        
    Example:
        >>> user = find_user(123)
        >>> if user is not None:
        ...     print(user['name'])
    """
    result = database.query(user_id)
    return result if result else None
```

---

### **Practice 6: Consider Alternatives to Optional**

#### **Alternative 1: Raise Exceptions**

```python
# Instead of returning None for errors
def find_user_optional(user_id: int) -> dict | None:
    """Returns None if not found."""
    user = database.get(user_id)
    return user if user else None

# Usage requires None check everywhere
user = find_user_optional(123)
if user is None:
    print("Not found")
else:
    print(user['name'])

# ✅ Better for exceptional cases - raise exception
class UserNotFoundError(Exception):
    pass

def find_user_raises(user_id: int) -> dict:
    """Raises UserNotFoundError if not found."""
    user = database.get(user_id)
    if not user:
        raise UserNotFoundError(f"User {user_id} not found")
    return user

# Usage is cleaner for the happy path
try:
    user = find_user_raises(123)
    print(user['name'])
except UserNotFoundError:
    print("Not found")
```

#### **Alternative 2: Use Sentinel Values**

```python
from typing import Literal

# When None is a valid value
UNSET = object()  # Unique sentinel

def update_field(value: str | None | object = UNSET) -> None:
    """
    Update a field.
    
    Args:
        value: New value for field
               - str: Set to string value
               - None: Explicitly set to NULL
               - UNSET: Don't change the field
    """
    if value is UNSET:
        return  # Don't update
    
    if value is None:
        database.set_null()  # Explicitly set to NULL
    else:
        database.set_value(value)  # Set to string value

# Usage:
update_field("hello")  # Set to "hello"
update_field(None)     # Set to NULL in database
update_field()         # Don't change the field (UNSET)
```

#### **Alternative 3: Use Result/Option Types (Functional Style)**

```python
from typing import Generic, TypeVar
from dataclasses import dataclass

T = TypeVar('T')

@dataclass
class Some(Generic[T]):
    value: T
    
    def is_some(self) -> bool:
        return True
    
    def is_none(self) -> bool:
        return False
    
    def unwrap(self) -> T:
        return self.value

@dataclass
class Nothing:
    def is_some(self) -> bool:
        return False
    
    def is_none(self) -> bool:
        return True
    
    def unwrap(self):
        raise ValueError("Called unwrap on Nothing")

Option = Some[T] | Nothing

def find_user(user_id: int) -> Option[dict]:
    """Returns Some(user) or Nothing."""
    user = database.get(user_id)
    return Some(user) if user else Nothing()

# Usage:
result = find_user(123)
if result.is_some():
    print(result.unwrap()['name'])
else:
    print("Not found")

# Or pattern matching (Python 3.10+)
match find_user(123):
    case Some(user):
        print(user['name'])
    case Nothing():
        print("Not found")
```

---

### **Practice 7: Use `typing.get_args()` and `typing.get_origin()` for Introspection**

```python
from typing import Optional, get_args, get_origin

# Introspect Optional types
optional_str = Optional[str]

print(get_origin(optional_str))  # <class 'typing.Union'>
print(get_args(optional_str))    # (<class 'str'>, <class 'NoneType'>)

# Check if a type is Optional
def is_optional(tp) -> bool:
    return get_origin(tp) is Union and type(None) in get_args(tp)

print(is_optional(Optional[str]))  # True
print(is_optional(str | None))     # True
print(is_optional(str))            # False
print(is_optional(int | str))      # False
```

---

### **Practice 8: Gradual Typing - Start with Optional, Refine Later**

```python
# Phase 1: Quick prototype - everything Optional
def process_data(
    data: dict | None = None,
    config: dict | None = None
) -> list | None:
    if data is None or config is None:
        return None
    # Process...
    return []

# Phase 2: Refine - make contracts clearer
from typing import TypedDict

class Config(TypedDict):
    timeout: int
    retries: int

def process_data_v2(
    data: dict,  # Required now
    config: Config | None = None  # Still optional but typed
) -> list:  # Never returns None
    if config is None:
        config = {'timeout': 30, 'retries': 3}
    # Process...
    return []

# Phase 3: Final - eliminate Optional where possible
def process_data_v3(
    data: dict,
    config: Config  # Required, caller provides or uses default
) -> list:
    # Process...
    return []

def get_default_config() -> Config:
    return {'timeout': 30, 'retries': 3}

# Caller handles defaults
result = process_data_v3(my_data, config or get_default_config())
```

---

### **Practice 9: Type Guards for Complex None Checks**

```python
from typing import TypeGuard

def is_not_none(value: str | None) -> TypeGuard[str]:
    """Type guard that narrows None away."""
    return value is not None

# Usage in filter
items: list[str | None] = ["a", None, "b", None, "c"]

# Without type guard - type checker sees list[str | None]
filtered = [x for x in items if x is not None]
# Type: list[str | None] - type checker doesn't know None is filtered

# With type guard - type checker sees list[str]
filtered_safe: list[str] = [x for x in items if is_not_none(x)]
# Type: list[str] - type checker knows None is filtered

# Also works with filter()
result: list[str] = list(filter(is_not_none, items))
```

---

### **Practice 10: Handling Optional in Data Classes**

```python
from dataclasses import dataclass, field
from typing import Optional

# ❌ Problematic - mutable default
@dataclass
class User:
    name: str
    tags: list[str] = []  # Shared across instances!

# ✅ Correct with field and default_factory
@dataclass
class User:
    name: str
    tags: list[str] = field(default_factory=list)
    metadata: dict[str, str] = field(default_factory=dict)

# ✅ Optional fields
@dataclass
class User:
    name: str
    email: str
    age: int | None = None  # Optional with default None
    phone: str | None = None

# ✅ Required Optional (must pass None explicitly)
from dataclasses import MISSING

@dataclass
class Config:
    api_key: str | None  # Required but can be None
    timeout: int = 30  # Optional with default

# Must provide api_key, even if None
config = Config(api_key=None)
# config = Config()  # Error: missing required argument api_key
```

---

## **Part 5: Common Patterns and Anti-Patterns**

### **Pattern: Optional Chaining (Manual)**

```python
from typing import Optional

@dataclass
class Address:
    street: str
    city: str

@dataclass
class Profile:
    address: Address | None

@dataclass
class User:
    profile: Profile | None

# ❌ Nested None checks - ugly
def get_city_bad(user: User | None) -> str | None:
    if user is not None:
        if user.profile is not None:
            if user.profile.address is not None:
                return user.profile.address.city
    return None

# ✅ Early returns - cleaner
def get_city_good(user: User | None) -> str | None:
    if user is None:
        return None
    if user.profile is None:
        return None
    if user.profile.address is None:
        return None
    return user.profile.address.city

# ✅ Using try-except (if performance is not critical)
def get_city_tryexcept(user: User | None) -> str | None:
    try:
        return user.profile.address.city
    except AttributeError:
        return None
```

### **Anti-Pattern: Optional in Collections**

```python
# ❌ Optional in collection type - usually a code smell
def process_items(items: Optional[list[str]]) -> int:
    """Why would items be None? Use empty list instead."""
    if items is None:
        return 0
    return len(items)

# ✅ Better - use empty collection as default
def process_items_good(items: list[str]) -> int:
    """Cleaner - caller provides empty list if needed."""
    return len(items)

# Caller handles empty case
result = process_items_good(my_items or [])

# Or with default argument
def process_items_default(items: list[str] | None = None) -> int:
    if items is None:
        items = []
    return len(items)
```

---

## **Part 6: Performance Considerations**

```python
import timeit

# None checks are extremely fast (identity check)
def check_is_none(x):
    return x is None

def check_equality(x):
    return x == None

# Benchmark
x = None
time_is = timeit.timeit(lambda: check_is_none(x), number=10_000_000)
time_eq = timeit.timeit(lambda: check_equality(x), number=10_000_000)

print(f"is None: {time_is:.4f}s")      # ~0.17s
print(f"== None: {time_eq:.4f}s")      # ~0.35s (2x slower)

# Type hints have ZERO runtime cost
def with_types(x: str | None) -> int | None:
    if x is None:
        return None
    return len(x)

def without_types(x):
    if x is None:
        return None
    return len(x)

# Identical performance
time_with = timeit.timeit(lambda: with_types("hello"), number=1_000_000)
time_without = timeit.timeit(lambda: without_types("hello"), number=1_000_000)

print(f"With types: {time_with:.4f}s")     # ~0.08s
print(f"Without types: {time_without:.4f}s")  # ~0.08s (same)
```

---

## **Summary Checklist**

**Understanding:**
✅ `Optional[T]` is just `Union[T, None]` or `T | None`  
✅ `None` is a singleton of type `NoneType`  
✅ Type hints have **zero runtime cost**  
✅ Use `is None`, not `== None` (identity vs equality)

**Best Practices:**
✅ Use `T | None` syntax in Python 3.10+  
✅ Always check None before using the value  
✅ Document why a value can be None  
✅ Consider exceptions for error cases  
✅ Don't overuse Optional - be explicit about requirements  
✅ Use type guards for complex filtering  
✅ Prefer empty collections over `None` collections  
✅ Use early returns to avoid nested None checks

**Type Checking:**
✅ Run `mypy` or `pyright` in your CI/CD  
✅ Enable strict mode: `mypy --strict`  
✅ Use type narrowing (if/assert) to help type checkers  
✅ Add type hints gradually (start with public APIs)

These practices will help you write type-safe, maintainable Python code!