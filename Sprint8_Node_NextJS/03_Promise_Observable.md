# Asynchronous JavaScript: Callbacks, Promises, async/await and Observables

A hands-on guide that walks from callbacks to Promises, `Promise.all`, `async/await`, and finally Observables with RxJS.

## Table of Contents

- [Asynchronous JavaScript: Callbacks, Promises, async/await and Observables](#asynchronous-javascript-callbacks-promises-asyncawait-and-observables)
  - [Table of Contents](#table-of-contents)
  - [1. Setup](#1-setup)
  - [2. Asynchronous Calls Without Promises (Callbacks)](#2-asynchronous-calls-without-promises-callbacks)
    - [2.1 The problem: returning from async code](#21-the-problem-returning-from-async-code)
    - [2.2 Solution: callbacks](#22-solution-callbacks)
    - [2.3 Callback hell](#23-callback-hell)
  - [3. Handling Callbacks with Promises](#3-handling-callbacks-with-promises)
    - [3.1 Characteristics of Promises](#31-characteristics-of-promises)
    - [3.2 Promise chaining example](#32-promise-chaining-example)
    - [One caution](#one-caution)
  - [4. Running Promises in Parallel with `Promise.all`](#4-running-promises-in-parallel-with-promiseall)
    - [4.1 Sequential vs parallel](#41-sequential-vs-parallel)
    - [4.2 Basic example](#42-basic-example)
    - [4.3 Fail-fast error handling](#43-fail-fast-error-handling)
    - [4.4 Related: `Promise.allSettled`, `Promise.race`, `Promise.any`](#44-related-promiseallsettled-promiserace-promiseany)
  - [5. Handling Promises with async/await](#5-handling-promises-with-asyncawait)
    - [5.1 `Promise` with async/await](#51-promise-with-asyncawait)
    - [5.2 `Promise.all` with async/await](#52-promiseall-with-asyncawait)

---

## 1. Setup

1. Create a separate, independent folder named `javascript`.
2. From within the `javascript` folder, run:

   ```bash
   npm init -y
   ```

All examples below are run from this folder with `node <filename>.js`.

> **Note:** The RxJS examples that use `import` syntax need ES modules. Add `"type": "module"` to `package.json` (or use the `.mjs` extension).

---

## 2. Asynchronous Calls Without Promises (Callbacks)

### 2.1 The problem: returning from async code

Create a file `callback.js`. Assume the function below makes a REST API call to get the login status of a user:

```javascript
function loginUser(email) {
  // Simulate REST API call, returns result after 1.5 secs
  setTimeout(() => {
    console.log('Now we have the data');
    if (email === 'abc')
      return { email };
    return { 'status': 'email not found' };
  }, 1500);
}

console.log(loginUser('abc'));
```

Run it:

```bash
node callback.js
```

> **What will be the output?**
> The log prints `undefined`, because the `return` executes inside the timer callback 1.5 seconds later. `loginUser` itself has already finished by then and returned nothing.

### 2.2 Solution: callbacks

To handle this, JavaScript lets us pass a **callback** function that is invoked once the data is ready:

```javascript
function loginUser(email, callback) {
  console.log(callback);

  setTimeout(() => {
    console.log('Now we have the data');
    if (email === 'abc')
      callback({ email });
    else
      callback({ 'status': 'email not found' });
  }, 1500);
}

loginUser('abc', (msg) => console.log(msg));
```

### 2.3 Callback hell

When several dependent async calls are needed, callbacks nest deeper and deeper:

```javascript
console.log('Start');

// Three callback-based functions
function loginUser(email, callback) {
  setTimeout(() => {
    console.log('Now we have the data');
    callback({ email });
  }, 1500);
}

function getUserVidoes(email, callback) {
  setTimeout(() => {
    callback(['HTML', 'CSS', 'JAVASCRIPT']);
  }, 1200);
}

function videosDetails(video, callback) {
  setTimeout(() => {
    callback(`title of the video ${video}`);
  }, 1000);
}

// Callback execution.
// Here we get three nested callbacks, which makes the code hard to read.
// This problem is called "callback hell".
loginUser('shirshakkandel@gmail.com', function (user) {
  console.log(user);
  getUserVidoes(user.email, videos => {
    console.log(videos);
    videosDetails(videos[0], title => {
      console.log(title);
    });
  });
});

// The user is not available here, because it arrives after 1.5 seconds.
console.log('Finished');
```

To handle this situation, JavaScript provides **Promises**.

---

## 3. Handling Callbacks with Promises

Think of booking an Uber ride:

- The driver **promises** to give you a ride.
- If the driver comes to pick you up, the promise is **fulfilled** (resolved).
- If the driver cancels, the promise is **rejected**.

### 3.1 Characteristics of Promises

| Characteristic | Description |
|---|---|
| **Single value** | A Promise represents a single value that will be resolved or rejected. |
| **Immutable state** | Once a Promise is resolved or rejected, its state cannot change. |
| **Error handling** | Built-in error handling through `.catch()` or `try...catch` blocks (with `async/await`). |

A Promise is always in one of three states: **pending**, **fulfilled**, or **rejected**.

### 3.2 Promise chaining example

Create a file `promises.js` and run it with `node promises.js`:

```javascript
function loginUser(email) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      console.log('Now we have the data');
      if (email === 'abc')
        resolve({ email });
      reject({ status: 'failure' });
    }, 1500);
  });
}

function getUserVidoes(email) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      resolve(['HTML', 'CSS', 'JAVASCRIPT']);
    }, 1200);
  });
}

function videosDetails(video) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      resolve(`title of the video ${video}`);
    }, 1000);
  });
}

// Success case
loginUser('abc')
  .then(resp =>
    getUserVidoes(resp.email)
      .then(videos =>
        videosDetails(videos[0])
          .then(resp => console.log(resp))
      )
  );

// Failure case
loginUser('abc1')
  .then(resp =>
    getUserVidoes(resp.email)
      .then(videos =>
        videosDetails(videos[0])
          .then(resp => console.log(resp))
      )
  )
  // To handle a rejection, use catch
  .catch(err => console.log(err));

// OR BETTER:

loginUser('abc')
  .then(resp => getUserVidoes(resp.email))
  .then(videos => videosDetails(videos[0]))
  .then(detail => console.log(detail))
  .catch(err => console.log(err));

// OR
loginUser('abc')
  .then(user => getUserVidoes(user.email))
  .then(videos => videosDetails(videos[0]))
  .then(console.log)
  .catch(console.error);
```

### One caution

Flattening only works when you return the inner promise. This is a common bug:

```
// ❌ Missing return: the next .then() receives undefined and doesn't wait
.then(user => { getUserVidoes(user.email); })

// ✅ Arrow function without braces returns implicitly
.then(user => getUserVidoes(user.email))

// ✅ Or with braces, return explicitly
.then(user => { return getUserVidoes(user.email); })
```

> The code above can also get messy with all the chaining of promises.

---

## 4. Running Promises in Parallel with `Promise.all`

### 4.1 Sequential vs parallel

In the previous section, each call **depends on the result of the previous one** (login → videos → details), so they must run one after another.

But when calls are **independent**, awaiting them one by one wastes time. `Promise.all` starts them all at once and waits for all of them to finish.

| Approach | Total time for tasks taking 1500 ms, 1200 ms and 1000 ms |
|---|---|
| Sequential (one after another) | ≈ 3700 ms (the sum) |
| `Promise.all` (parallel) | ≈ 1500 ms (the slowest one) |

**Signature**

```javascript
Promise.all(iterable)  // iterable is usually an array of promises
```

- Returns a **single Promise** that resolves to an **array of results**, in the same order as the input (not the order in which they finish).
- **Fail-fast:** if *any* promise rejects, the combined promise rejects immediately with that first rejection reason.
- Non-promise values in the array are treated as already-resolved values.

### 4.2 Basic example

Create a file `promiseall.js` and run it with `node promiseall.js`:

```javascript
function getUserProfile(email) {
  return new Promise((resolve) => {
    setTimeout(() => resolve({ email, name: 'ABC' }), 1500);
  });
}

function getUserVideos(email) {
  return new Promise((resolve) => {
    setTimeout(() => resolve(['HTML', 'CSS', 'JAVASCRIPT']), 1200);
  });
}

function getUserNotifications(email) {
  return new Promise((resolve) => {
    setTimeout(() => resolve(['Welcome!', 'New video uploaded']), 1000);
  });
}

console.time('Promise.all');

Promise.all([
  getUserProfile('abc'),
  getUserVideos('abc'),
  getUserNotifications('abc')
])
  .then(([profile, videos, notifications]) => {
    console.log('Profile:', profile);
    console.log('Videos:', videos);
    console.log('Notifications:', notifications);
    console.timeEnd('Promise.all'); // ≈ 1500ms, not 3700ms
  });
```

**Expected output**

```text
Profile: { email: 'abc', name: 'ABC' }
Videos: [ 'HTML', 'CSS', 'JAVASCRIPT' ]
Notifications: [ 'Welcome!', 'New video uploaded' ]
Promise.all: 1503.2ms
```

Notice that the results come back in the **same order as the input array**, even though `getUserNotifications` finished first.

### 4.3 Fail-fast error handling

If one promise rejects, `Promise.all` rejects immediately, and the other results are discarded:

```javascript
function failingCall() {
  return new Promise((_, reject) => {
    setTimeout(() => reject(new Error('Videos service is down')), 500);
  });
}

Promise.all([
  getUserProfile('abc'),   // takes 1500ms
  failingCall(),           // rejects after 500ms
  getUserNotifications('abc')
])
  .then(results => console.log('Results:', results))
  .catch(err => console.log('Failed:', err.message));

// Output (after ~500ms): Failed: Videos service is down
```

> **Note:** The other promises are **not cancelled**. They keep running in the background; `Promise.all` just stops waiting for them.

### 4.4 Related: `Promise.allSettled`, `Promise.race`, `Promise.any`

| Method | Resolves when… | Rejects when… |
|---|---|---|
| `Promise.all` | **All** promises fulfil (array of values) | **Any** promise rejects (fail-fast) |
| `Promise.allSettled` | **All** promises settle, either way (array of `{status, value/reason}`) | Never |
| `Promise.race` | The **first** promise settles (fulfilled *or* rejected) | The first settled promise is a rejection |
| `Promise.any` | The **first** promise **fulfils** | **All** promises reject (`AggregateError`) |

Use `Promise.allSettled` when you want every result regardless of individual failures:

```javascript
Promise.allSettled([
  getUserProfile('abc'),
  failingCall(),
  getUserNotifications('abc')
]).then(results => console.log(results));

// [
//   { status: 'fulfilled', value: { email: 'abc', name: 'ABC' } },
//   { status: 'rejected',  reason: Error: Videos service is down },
//   { status: 'fulfilled', value: [ 'Welcome!', 'New video uploaded' ] }
// ]
```


## 5. Handling Promises with async/await

### 5.1 `Promise` with async/await

`async/await` lets promise-based code read like synchronous code, avoiding long `.then()` chains:

```javascript
const details = async () => {
  const user = await loginUser('abc');
  console.log(user);

  const videos = await getUserVidoes(user.email);
  console.log(videos);

  let detail = await videosDetails(videos[0]);
  return detail;
};

details()
  .then(data => console.log(data))
  .catch(err => console.log(err));
```

### 5.2 `Promise.all` with async/await

The same logic reads more naturally with `async/await`, and `try...catch` handles the failure case:

```javascript
const loadDashboard = async (email) => {
  try {
    const [profile, videos, notifications] = await Promise.all([
      getUserProfile(email),
      getUserVideos(email),
      getUserNotifications(email)
    ]);

    return { profile, videos, notifications };
  } catch (err) {
    console.log('Could not load dashboard:', err.message);
  }
};

loadDashboard('abc').then(data => console.log(data));
```

**Common mistake: accidentally running sequentially**

```javascript
// ❌ Sequential: ~3700ms. Each await blocks the next call from starting.
const profile = await getUserProfile('abc');
const videos = await getUserVideos('abc');
const notifications = await getUserNotifications('abc');

// ✅ Parallel: ~1500ms. All calls start immediately.
const [profile, videos, notifications] = await Promise.all([
  getUserProfile('abc'),
  getUserVideos('abc'),
  getUserNotifications('abc')
]);
```


---



