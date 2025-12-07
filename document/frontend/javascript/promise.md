# samples

# Yes! You can reject in the middle of a `.then()` chain

You have several ways to do this:

## Method 1: Throw an error (most common)

```javascript
fetch('/api/user')
  .then(response => {
    if (!response.ok) {
      throw new Error('HTTP error: ' + response.status);
    }
    return response.json();
  })
  .then(data => {
    if (!data.id) {
      throw new Error('Invalid data: missing id');
    }
    return data;
  })
  .then(data => {
    console.log('Valid data:', data);
  })
  .catch(error => {
    console.error('Caught error:', error.message);
  });
```

## Method 2: Return `Promise.reject()`

```javascript
fetch('/api/user')
  .then(response => {
    if (!response.ok) {
      return Promise.reject(new Error('HTTP error: ' + response.status));
    }
    return response.json();
  })
  .then(data => {
    if (!data.id) {
      return Promise.reject(new Error('Invalid data: missing id'));
    }
    return data;
  })
  .then(data => {
    console.log('Valid data:', data);
  })
  .catch(error => {
    console.error('Caught error:', error.message);
  });
```

## Method 3: Return a rejected promise directly

```javascript
fetch('/api/user')
  .then(response => {
    if (!response.ok) {
      return new Promise((resolve, reject) => {
        reject(new Error('HTTP error: ' + response.status));
      });
    }
    return response.json();
  })
  .catch(error => {
    console.error('Caught error:', error.message);
  });
```

## Complete Example

```javascript
function processUserData(userId) {
  return fetch(`/api/users/${userId}`)
    .then(response => {
      console.log('Step 1: Got response');
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      return response.json();
    })
    .then(user => {
      console.log('Step 2: Parsed JSON');
      if (!user.email) {
        throw new Error('User has no email');
      }
      return user;
    })
    .then(user => {
      console.log('Step 3: Validated user');
      if (user.age < 18) {
        return Promise.reject(new Error('User must be 18+'));
      }
      return user;
    })
    .then(user => {
      console.log('Step 4: Processing completed');
      return user;
    })
    .catch(error => {
      console.error('Error at some step:', error.message);
      // Handle or re-throw
      throw error; // Re-throw to propagate error further
    });
}

// Usage
processUserData(123)
  .then(user => console.log('Final user:', user))
  .catch(error => console.error('Final catch:', error.message));
```

## What happens when you reject mid-chain?

```javascript
Promise.resolve('start')
  .then(value => {
    console.log('Step 1:', value); // Executes: "Step 1: start"
    return 'step1 done';
  })
  .then(value => {
    console.log('Step 2:', value); // Executes: "Step 2: step1 done"
    throw new Error('Rejecting here!'); // Reject!
    return 'step2 done'; // This line never executes
  })
  .then(value => {
    console.log('Step 3:', value); // SKIPPED!
    return 'step3 done';
  })
  .then(value => {
    console.log('Step 4:', value); // SKIPPED!
    return 'step4 done';
  })
  .catch(error => {
    console.error('Caught:', error.message); // Executes: "Caught: Rejecting here!"
    return 'recovered'; // You can recover from error
  })
  .then(value => {
    console.log('Step 5:', value); // Executes: "Step 5: recovered"
    return 'completed';
  });
```

**Output:**
```
Step 1: start
Step 2: step1 done
Caught: Rejecting here!
Step 5: recovered
```

## Key Points

- When you reject (throw or return `Promise.reject()`), all subsequent `.then()` handlers are **skipped**
- Execution jumps directly to the next `.catch()` handler
- You can **recover** from an error by handling it in `.catch()` and returning a value
- After recovery, the chain continues with subsequent `.then()` handlers
- **Throwing an error** is the most common and cleanest approach