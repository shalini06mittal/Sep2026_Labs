# JavaScript Challenges: Single Thread & Event Loop, Callbacks, Promises, async/await

Tricky, concept-focused questions mixing **theory** and **code**. Try each one yourself before opening the solution.

**How to use this file**
- 🧠 = theory / explain it  |  💻 = predict output or write code
- Difficulty: ⭐ medium · ⭐⭐ hard · ⭐⭐⭐ very tricky
- Click **"Show solution"** under each question to reveal the answer.
- Run snippets in **Node 18+** (or a browser console). Where `await` is used at the top level, save as `.mjs`.
- Output notes assume Node.js. Exact error formatting may differ slightly in browsers.

**Helpers used throughout** (assume they're defined in every snippet that uses them):
```js
const delay = (ms, value) => new Promise((resolve) => setTimeout(resolve, ms, value));
const fail  = (ms, msg)   => new Promise((_, reject) => setTimeout(reject, ms, new Error(msg)));
```

## Table of Contents
1. [Part 1: Single Thread & the Event Loop](#part-1-single-thread--the-event-loop)
2. [Part 2: Callbacks & Callback Hell](#part-2-callbacks--callback-hell)
3. [Part 3: Promises](#part-3-promises-then-catch-promiseall)
4. [Part 4: async/await](#part-4-asyncawait)
5. [Capstone Challenge](#capstone-challenge)

---

# Part 1: Single Thread & the Event Loop

### Q1 🧠 ⭐ What does "single-threaded" really mean?
1. What does it mean that JavaScript is single-threaded?
2. If it's single-threaded, how can a page fetch data *while* the UI stays responsive?
3. Name the main pieces involved (call stack, queues, etc.) and what each does.

<details>
<summary>✅ Show solution</summary>

1. JS has **one call stack**, so it executes **one piece of JavaScript at a time**, run to completion. A long-running function blocks everything else on that thread (rendering, clicks, timers).
2. The **runtime around the engine** (browser Web APIs, or Node's libuv) does the waiting work (timers, network, disk) **outside** the JS thread. When the work finishes, a callback is queued; the **event loop** moves it onto the call stack once the stack is empty.
3. Pieces:
   - **Call stack:** where synchronous code runs, one frame at a time.
   - **Web APIs / libuv:** timers, `fetch`, DOM events, file I/O, handled outside the stack.
   - **Macrotask (task) queue:** callbacks from `setTimeout`, `setInterval`, I/O, DOM events, etc.
   - **Microtask queue:** Promise reactions (`.then`/`.catch`/`.finally`, `await` continuations) and `queueMicrotask`.
   - **Event loop:** when the stack is empty it runs **all** microtasks, then takes **one** macrotask, and repeats (browsers also render between tasks).

**Async ≠ parallel.** Async code *waits* concurrently, but the JavaScript itself still runs one piece at a time.

</details>

---

### Q2 💻 ⭐ `setTimeout(fn, 0)` ordering
```js
console.log("A");
setTimeout(() => console.log("B"), 0);
console.log("C");
```
What prints and why? Is `0` ms a guarantee?

<details>
<summary>✅ Show solution</summary>

**Output**
```
A
C
B
```

**Explanation**
`setTimeout` hands the callback to the timer system; it can only run when the **call stack is empty**, so the rest of the synchronous script (`C`) finishes first. The delay is a **minimum**, not a guarantee: the callback runs *no earlier than* the delay, and only once the stack is free (browsers also clamp nested timers to ≥4 ms).

</details>

---

### Q3 💻 ⭐⭐ Microtasks vs. macrotasks
```js
console.log(1);
setTimeout(() => console.log(2), 0);
Promise.resolve().then(() => console.log(3));
queueMicrotask(() => console.log(4));
console.log(5);
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
1
5
3
4
2
```

**Explanation**
1. Synchronous code runs first: `1`, `5`.
2. Stack is empty → the event loop drains the **entire microtask queue** (in FIFO order): `3`, then `4`.
3. Only then does it take the next **macrotask**: the timer callback → `2`.

In Node, `process.nextTick` callbacks run even *before* promise microtasks.

</details>

---

### Q4 💻 ⭐⭐ Blocking the thread
```js
const start = Date.now();

setTimeout(() => console.log("timer fired late?", Date.now() - start >= 500), 100);

while (Date.now() - start < 500) {
  // busy wait
}
console.log("done blocking");
```
What is the output and order? What does this say about timers?

<details>
<summary>✅ Show solution</summary>

**Output**
```
done blocking
timer fired late? true
```

**Explanation**
The timer was due at ~100 ms, but the busy loop occupies the only thread until ~500 ms. The callback can't run until the stack is empty. Timers are **not precise schedulers**; any long synchronous task delays them (and freezes the UI in browsers).

</details>

---

### Q5 💻 ⭐⭐⭐ Microtask starvation
```js
setTimeout(() => console.log("timeout"), 0);

let n = 0;
const loop = () => {
  if (++n <= 3) {
    console.log("micro", n);
    queueMicrotask(loop);
  }
};
queueMicrotask(loop);
```
What is printed? What would happen if the condition were always true?

<details>
<summary>✅ Show solution</summary>

**Output**
```
micro 1
micro 2
micro 3
timeout
```

**Explanation**
The event loop drains the microtask queue **completely**, including microtasks queued by other microtasks, before moving to the next macrotask. If `loop` re-queued itself forever, the timer (and rendering, clicks, I/O) would **never** get a chance to run, i.e. the page/process would hang even though there's no "blocking" loop in sight.

</details>

---

### Q6 💻 ⭐⭐ Why `try/catch` can't catch this
```js
try {
  setTimeout(() => {
    throw new Error("boom");
  }, 0);
} catch (e) {
  console.log("caught");
}
console.log("after");
```
What happens? How do you handle errors from async callbacks?

<details>
<summary>✅ Show solution</summary>

**Output**
```
after
```
followed by an **uncaught** `Error: boom` (Node crashes with it; browsers log it).

**Explanation**
`try/catch` only catches errors thrown while its block is **on the call stack**. By the time the timer callback runs, `try` has long finished and its stack frame is gone; the callback runs from a fresh stack.

**Handling options:** put `try/catch` *inside* the callback; use error-first callbacks; or use Promises/`async`-`await`, where failures become rejections you can `.catch()` or `try/catch` around an `await`.

</details>

---

### Q7 🧠 ⭐⭐ Is anything in JS actually parallel?
1. Does `Promise.all([fetch(a), fetch(b)])` run JavaScript in parallel?
2. In Node, `fs.readFile` and `crypto.pbkdf2` don't block the main thread. How is that possible?
3. How *can* you get real parallel JavaScript execution?

<details>
<summary>✅ Show solution</summary>

1. The **network requests** happen concurrently (done by the browser/OS), but the **JavaScript** that handles the results still runs on one thread, one callback at a time.
2. Node's **libuv** runs such work on a **thread pool** (default 4 threads) or via OS async I/O. Completed results are queued back to the event loop. Your JS stays single-threaded; the *runtime* is not.
3. **Web Workers** (browsers) or **`worker_threads`** (Node): separate threads with **their own** event loop and heap. They communicate via **message passing** (`postMessage`, structured clone) or `SharedArrayBuffer`+`Atomics`.

</details>

---

### Q8 💻 ⭐⭐⭐ Single-threaded ≠ no race conditions
```js
let balance = 100;

async function withdraw(amount) {
  if (balance >= amount) {
    await delay(10);          // e.g. a network/database call
    balance -= amount;
  }
}

async function main() {
  await Promise.all([withdraw(80), withdraw(80)]);
  console.log(balance);
}
main();
```
What prints and why? How would you fix it?

<details>
<summary>✅ Show solution</summary>

**Output:** `-60`

**Explanation**
Both calls run the `if (balance >= amount)` check synchronously *before* either reaches the `balance -= amount` after the `await`. At that point `balance` is still `100`, so both pass. Each `await` is a point where **other code can interleave**. Single-threaded means no two lines run at the exact same time, not that your logic is atomic across `await`s.

**Fixes**
- Re-check/modify **synchronously** (no `await` between check and update), or do the check **after** the async work.
- Serialize operations with a simple queue/mutex:
```js
let queue = Promise.resolve();
const withdrawSafe = (amount) =>
  (queue = queue.then(() => withdraw(amount)));
```
- Let the database enforce atomicity (transactions/conditional updates).

</details>

---

### Q9 💻 ⭐⭐ Don't freeze the thread
You must run `heavy(item)` on 1,000,000 items, but the UI/server must stay responsive. Write `processInChunks(items, fn, chunkSize)` that returns a Promise and yields to the event loop between chunks.

<details>
<summary>✅ Show solution</summary>

```js
function processInChunks(items, fn, chunkSize = 1000) {
  return new Promise((resolve) => {
    let i = 0;
    function nextChunk() {
      const end = Math.min(i + chunkSize, items.length);
      for (; i < end; i++) fn(items[i]);
      if (i < items.length) {
        setTimeout(nextChunk, 0); // macrotask: lets timers, I/O and rendering run
      } else {
        resolve();
      }
    }
    nextChunk();
  });
}
```

**Why `setTimeout` and not `Promise.resolve().then`?** Microtasks run *before* the loop moves on, so chaining them would still starve rendering/I/O (see Q5). A macrotask yields to everything else.

For truly CPU-heavy work, prefer a **Worker** (Q7). Newer browsers also offer `scheduler.yield()`.

</details>

---

# Part 2: Callbacks & Callback Hell

### Q10 💻 ⭐ Sync vs. async callbacks
```js
[1, 2].forEach((n) => console.log("forEach", n));
setTimeout(() => console.log("timeout"), 0);
console.log("end");
```
Both `forEach` and `setTimeout` take callbacks. What's the output, and what's the key difference between the two?

<details>
<summary>✅ Show solution</summary>

**Output**
```
forEach 1
forEach 2
end
timeout
```

**Explanation**
A **callback** is just a function passed to another function to be called later. It may be called **synchronously** (`forEach`, `map`, `sort`: finished before the outer call returns) or **asynchronously** (`setTimeout`, I/O, events: called after the current code finishes). Knowing which one you have determines whether `try/catch` works around it and what the execution order is.

</details>

---

### Q11 💻 ⭐⭐ Releasing Zalgo: sometimes sync, sometimes async
```js
const cache = { 1: { id: 1 } };

function getUser(id, cb) {
  if (cache[id]) return cb(null, cache[id]);
  setTimeout(() => {
    cache[id] = { id };
    cb(null, cache[id]);
  }, 10);
}

getUser(1, () => console.log("A"));
console.log("B");
getUser(2, () => console.log("C"));
console.log("D");
```
1. What's the output?
2. Why is this design dangerous?
3. Fix it.

<details>
<summary>✅ Show solution</summary>

1. **Output:** `A`, `B`, `D`, `C`. The cached call is synchronous (A before B), the uncached one is asynchronous.
2. A function that is **sometimes sync and sometimes async** makes callers unable to reason about ordering (code after the call may or may not have run yet), causing subtle bugs (state not initialised yet, re-entrancy, stack growth).
3. **Always be async** (or always sync):
```js
function getUser(id, cb) {
  if (cache[id]) {
    queueMicrotask(() => cb(null, cache[id])); // or process.nextTick in Node
    return;
  }
  setTimeout(() => {
    cache[id] = { id };
    cb(null, cache[id]);
  }, 10);
}
```
New output: `B`, `D`, `A`, `C`.

(Promises give you this guarantee for free: `.then` callbacks are *always* async.)

</details>

---

### Q12 💻 ⭐⭐ Callbacks and closures in loops
```js
for (var i = 0; i < 3; i++) setTimeout(() => console.log("var", i), 0);
for (let j = 0; j < 3; j++) setTimeout(() => console.log("let", j), 0);
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
var 3
var 3
var 3
let 0
let 1
let 2
```

**Explanation**
`var` is function-scoped: there's **one** `i`, and by the time the callbacks run (after the loop), it's `3`. `let` creates a **fresh binding per iteration**, so each closure captures its own `j`. (Pre-ES6 fix: wrap in an IIFE or use `setTimeout(fn, 0, i)`.)

</details>

---

### Q13 💻 ⭐⭐⭐ The callback that fires twice
```js
function readConfig(cb) {
  try {
    const data = JSON.parse('{"a":1}');
    cb(null, data);
  } catch (err) {
    cb(err);
  }
}

readConfig((err, data) => {
  if (err) {
    console.log("error:", err.message);
    return;
  }
  console.log("data", data);
  throw new Error("handler bug");
});
```
What's printed? What is the bug and the fix?

<details>
<summary>✅ Show solution</summary>

**Output**
```
data { a: 1 }
error: handler bug
```

**Explanation**
The call `cb(null, data)` is *inside* the `try`. When the user's handler throws, the `catch` block catches **the handler's own error** and calls `cb(err)` **a second time**. The callback was invoked twice (once with success, once with failure).

**Fix:** only wrap the risky operation, not the callback call.
```js
function readConfig(cb) {
  let data;
  try {
    data = JSON.parse('{"a":1}');
  } catch (err) {
    return cb(err);
  }
  cb(null, data);
}
```
Rule of thumb: **call a callback exactly once, and outside your own `try`.**

</details>

---

### Q14 💻 ⭐⭐ Flatten the pyramid (callback version)
Refactor this callback hell **without using Promises** so it's flat, readable, and handles errors in one place.

```js
getUser(1, (err, user) => {
  if (err) return handleError(err);
  getPosts(user.id, (err, posts) => {
    if (err) return handleError(err);
    getComments(posts[0].id, (err, comments) => {
      if (err) return handleError(err);
      console.log(comments);
    });
  });
});
```

<details>
<summary>✅ Show solution</summary>

Use **named functions** (one level of nesting) and the error-first convention:

```js
function onUser(err, user) {
  if (err) return handleError(err);
  getPosts(user.id, onPosts);
}
function onPosts(err, posts) {
  if (err) return handleError(err);
  getComments(posts[0].id, onComments);
}
function onComments(err, comments) {
  if (err) return handleError(err);
  console.log(comments);
}

getUser(1, onUser);
```

**Why was the original "hell"?**
- Rightward drift (pyramid of doom) hurts readability.
- Repeated error handling at each level.
- Variables from outer steps (`user`) get trapped in closures; hard to share state.
- Hard to add parallelism, retries, or early exits.

Named functions help, but sequencing is still **implicit and scattered**: the motivation for Promises and `async`/`await` (see Q29 and Q45).

</details>

---

### Q15 🧠 ⭐⭐ Inversion of control: the trust problem
When you pass a callback to a third-party function, you hand over control. List **at least five** things that could go wrong, and explain how Promises address them.

<details>
<summary>✅ Show solution</summary>

**What can go wrong with callbacks**
1. Called **too early** (synchronously; see Zalgo, Q11).
2. Called **too late**, or **never**.
3. Called **too many times** (e.g., Q13).
4. Called **too few times**.
5. Called with the **wrong `this`** or arguments.
6. Errors thrown inside get **swallowed** or crash the process.
7. Success and error paths aren't enforced; callers forget to check `err`.

**How Promises help**
- `.then` handlers are **always async** (never early).
- A promise **settles once** (resolve/reject only the first call counts), so no double-calling.
- The consumer **keeps control**: it gets a value (a promise) back and decides what to do with it, instead of handing a function away.
- Errors thrown in handlers become rejections that propagate down the chain to `.catch`.
- They're **composable** (`all`, `race`, chaining) and uniform, which removes the need for ad-hoc patterns.

(Promises don't solve *never settles*, so you still add timeouts. See Q47.)

</details>

---

### Q16 💻 ⭐⭐⭐ Coding: `parallel` with callbacks
Implement `parallel(tasks, done)`. Each task is `(cb) => void` using error-first callbacks. Run all tasks at once; call `done(err, results)` **exactly once** with results in the **original order**, or with the **first error**.

```js
parallel(
  [
    (cb) => setTimeout(cb, 200, null, "slow"),
    (cb) => setTimeout(cb, 50, null, "fast"),
  ],
  (err, results) => console.log(err, results)
);
// null [ 'slow', 'fast' ]
```

<details>
<summary>✅ Show solution</summary>

```js
function parallel(tasks, done) {
  const results = new Array(tasks.length);
  let remaining = tasks.length;
  let finished = false;

  if (remaining === 0) return queueMicrotask(() => done(null, results));

  tasks.forEach((task, i) => {
    task((err, value) => {
      if (finished) return;            // guard: never call done twice
      if (err) {
        finished = true;
        return done(err);
      }
      results[i] = value;              // keep original order, not completion order
      if (--remaining === 0) {
        finished = true;
        done(null, results);
      }
    });
  });
}
```

**Key points**
- Use the **index** to store results; completion order ≠ start order.
- Track a `finished` flag so a later error/success doesn't call `done` again.
- The empty-array case is handled asynchronously to avoid Zalgo.
- No locks needed for `remaining--`: single thread, and callbacks run one at a time.

</details>

---

### Q17 💻 ⭐⭐ Losing `this` in callbacks
```js
const timer = {
  label: "tick",
  log() {
    console.log(this.label);
  },
};

setTimeout(timer.log, 0);
setTimeout(() => timer.log(), 0);
setTimeout(timer.log.bind(timer), 0);
```
What prints?

<details>
<summary>✅ Show solution</summary>

**Output**
```
undefined
tick
tick
```

**Explanation**
Passing `timer.log` passes **just the function**, detached from `timer`. When the timer later calls it, `this` is not `timer` (a `Timeout` object in Node, `window` in browsers), so `this.label` is `undefined`. An arrow wrapper or `.bind(timer)` keeps the correct receiver.

</details>

---

# Part 3: Promises (.then, .catch, Promise.all)

### Q18 🧠 ⭐ Promise fundamentals
1. What are a promise's three states? Which transitions are possible?
2. What does `.then()` return?
3. Why is a settled promise's state/value "immutable"?

<details>
<summary>✅ Show solution</summary>

1. **pending**, **fulfilled**, **rejected**. A pending promise can move to fulfilled *or* rejected **once**; fulfilled/rejected ("settled") never change again.
2. Always a **new promise**. Its fate is determined by the handler:
   - handler returns a value → new promise **fulfills** with it,
   - handler returns a promise/thenable → new promise **follows** it,
   - handler **throws** → new promise **rejects** with the error,
   - no handler for that state → the result **passes through** unchanged.
3. Once settled, further `resolve`/`reject` calls are ignored. This guarantees consumers see one consistent outcome no matter how many `.then`s they attach (even late ones; handlers on an already-settled promise are still called, asynchronously).

</details>

---

### Q19 💻 ⭐⭐ The executor runs synchronously
```js
console.log(1);

new Promise((resolve) => {
  console.log(2);
  resolve(3);
  console.log(4);
}).then((v) => console.log(v));

console.log(5);
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
1
2
4
5
3
```

**Explanation**
The function passed to `new Promise` (the **executor**) runs **immediately and synchronously**. `resolve(3)` doesn't stop the executor, so `4` still prints. Only the `.then` callback is deferred to the **microtask queue**, after the current synchronous code is done.

</details>

---

### Q20 💻 ⭐ Only the first settle counts
```js
new Promise((resolve, reject) => {
  resolve("a");
  reject(new Error("b"));
  resolve("c");
})
  .then(console.log)
  .catch(() => console.log("err"));
```

<details>
<summary>✅ Show solution</summary>

**Output:** `a`

After the first `resolve("a")`, the promise is settled; the later `reject` and `resolve` are silently ignored.

</details>

---

### Q21 💻 ⭐⭐ Reading a `.then` chain
```js
Promise.resolve(1)
  .then((v) => v + 1)
  .then((v) => { console.log(v); })
  .then((v) => console.log(v))
  .then(() => { throw new Error("x"); })
  .then(() => console.log("skipped"))
  .catch((e) => { console.log("caught", e.message); return "recovered"; })
  .then((v) => console.log(v));
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
2
undefined
caught x
recovered
```

**Explanation**
- 1 → 2 and logs `2`. That handler has braces and **no `return`**, so the next value is `undefined`.
- `throw` rejects the chain; `.then(() => console.log("skipped"))` has no rejection handler, so the rejection **passes through**.
- `.catch` handles it and **returns a value**, so the chain is **fulfilled again** with `"recovered"`.

</details>

---

### Q22 💻 ⭐⭐ `.then(a, b)` vs `.then(a).catch(b)`
```js
Promise.resolve()
  .then(
    () => { throw new Error("a"); },
    (e) => console.log("handler1")
  )
  .catch((e) => console.log("handler2", e.message));
```
What prints? What if the first `.then` were rewritten as `.then(onOk).catch(onErr)`?

<details>
<summary>✅ Show solution</summary>

**Output:** `handler2 a`

**Explanation**
In `.then(onFulfilled, onRejected)`, `onRejected` only handles rejections of the **upstream** promise, **not** errors thrown by `onFulfilled` in the same call. The error from `onFulfilled` rejects the *returned* promise, which is caught downstream by `.catch`.

With `.then(onOk).catch(onErr)`, `onErr` would also handle errors thrown inside `onOk`. That's usually what you want, so prefer a trailing `.catch`.

</details>

---

### Q23 💻 ⭐⭐ Rethrowing inside `.catch`
```js
Promise.reject(new Error("1"))
  .catch((e) => { console.log("c1", e.message); throw new Error("2"); })
  .catch((e) => { console.log("c2", e.message); })
  .then(() => console.log("then"));
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
c1 1
c2 2
then
```

A `.catch` that returns normally **recovers** the chain; one that **throws** keeps it rejected. The second `.catch` swallows error "2" and returns `undefined`, so the final `.then` runs.

</details>

---

### Q24 💻 ⭐⭐ `.finally` semantics
Run each snippet **separately** and predict the output.

```js
// A
Promise.resolve("x")
  .finally(() => "ignored")
  .then(console.log);

// B
Promise.reject(new Error("e"))
  .finally(() => { throw new Error("f"); })
  .catch((err) => console.log(err.message));
```

<details>
<summary>✅ Show solution</summary>

**A → `x`**: `finally`'s callback receives no argument and its **return value is ignored**; the original value passes through.

**B → `f`**: if the `finally` callback **throws** (or returns a rejected promise), that error **replaces** the original outcome.

Use `.finally` for cleanup (hide spinners, close connections); it runs on both success and failure, and doesn't alter the result unless it fails.

</details>

---

### Q25 💻 ⭐⭐⭐ Errors in the executor: sync vs. async
```js
new Promise(() => {
  throw new Error("sync");
}).catch((e) => console.log("caught", e.message));

new Promise(() => {
  setTimeout(() => {
    throw new Error("async");
  }, 0);
}).catch((e) => console.log("never printed"));
```
What happens?

<details>
<summary>✅ Show solution</summary>

**Output**
```
caught sync
```
then an **uncaught exception** `Error: async` (the process crashes in Node).

**Explanation**
- A `throw` **synchronously inside the executor** is turned into a rejection automatically.
- A `throw` inside a later `setTimeout` callback happens on a **different stack**, after the executor already returned, so the promise machinery can't see it. It's a plain uncaught exception, **not** a rejection.

**Fix:** call `reject` explicitly (wrap the async work in try/catch → `reject(err)`), or better, use promise-returning APIs.

</details>

---

### Q26 💻 ⭐⭐ The missing `return`
```js
const fetchUser   = () => delay(10, { id: 1 });
const fetchPosts  = (id) => delay(10, ["p1", "p2"]);

fetchUser()
  .then((user) => {
    fetchPosts(user.id);
  })
  .then((posts) => console.log("posts:", posts));
```
What prints? Why? Fix it.

<details>
<summary>✅ Show solution</summary>

**Output:** `posts: undefined`

**Explanation**
The first `.then` callback doesn't `return` the promise. The chain therefore continues **immediately** with `undefined`; it doesn't wait for `fetchPosts`. (Also, any rejection from that un-returned promise isn't connected to the chain: an unhandled rejection.)

**Fix**
```js
fetchUser()
  .then((user) => fetchPosts(user.id))      // returned promise → chain waits for it
  .then((posts) => console.log("posts:", posts))
  .catch(console.error);
```

</details>

---

### Q27 💻 ⭐⭐ Coding: `promisify`
Write `promisify(fn)` that converts an error-first callback function `fn(...args, cb)` into one that returns a Promise. Then use it on the legacy API below.

```js
const legacyAdd = (a, b, cb) =>
  setTimeout(() => (typeof a === "number" ? cb(null, a + b) : cb(new Error("NaN"))), 10);
```

<details>
<summary>✅ Show solution</summary>

```js
const promisify = (fn) =>
  function (...args) {
    return new Promise((resolve, reject) => {
      fn.call(this, ...args, (err, value) => (err ? reject(err) : resolve(value)));
    });
  };

const add = promisify(legacyAdd);

add(1, 2).then(console.log);                       // 3
add("x", 2).catch((e) => console.log(e.message));  // NaN
```

**Notes**
- Using a regular `function` and `fn.call(this, ...)` preserves `this` for method-style calls.
- Node already ships this as `util.promisify` (and `fs/promises`, `timers/promises`).
- Callbacks that return **multiple values** need custom handling (e.g. resolve with an array/object).

</details>

---

### Q28 💻 ⭐⭐ Promise pyramid (the anti-pattern)
Promises don't magically fix nesting; this still is "callback hell with extra steps". Rewrite it flat:

```js
getUser(1).then((user) => {
  return getPosts(user.id).then((posts) => {
    return getComments(posts[0].id).then((comments) => {
      console.log(user.name, comments);
    });
  });
});
```

<details>
<summary>✅ Show solution</summary>

Return promises and **flatten**. When a later step needs earlier values, carry them forward in an object (or use `async/await`, see Part 4):

```js
getUser(1)
  .then((user) =>
    getPosts(user.id).then((posts) => ({ user, posts }))
  )
  .then(({ user, posts }) =>
    getComments(posts[0].id).then((comments) => ({ user, comments }))
  )
  .then(({ user, comments }) => console.log(user.name, comments))
  .catch(handleError);   // one place for every error in the chain
```

If a later step doesn't need `user`, it's simply:
```js
getUser(1)
  .then((user) => getPosts(user.id))
  .then((posts) => getComments(posts[0].id))
  .then(console.log)
  .catch(handleError);
```

**Also avoid:** the "explicit construction" anti-pattern: wrapping an existing promise in `new Promise(...)` just to resolve it.

</details>

---

### Q29 💻 ⭐⭐ `Promise.all`: fail-fast
```js
const p1 = delay(300, "a");
const p2 = fail(100, "fail");
const p3 = delay(50, "c");

Promise.all([p1, p2, p3])
  .then((r) => console.log("ok", r))
  .catch((e) => console.log("all failed:", e.message));
```
What prints and when? Does `p1` get cancelled?

<details>
<summary>✅ Show solution</summary>

**Output (at ~100 ms):** `all failed: fail`

**Explanation**
`Promise.all` **rejects as soon as any input rejects**, with that reason, and discards the other results. It doesn't wait for `p1`.

**Promises can't be cancelled.** `p1` still runs to completion in the background; its result is just ignored. To actually cancel work (e.g. HTTP requests), use `AbortController`.

</details>

---

### Q30 💻 ⭐⭐ `Promise.all`: order, plain values, empty input
```js
Promise.all([delay(200, "slow"), "plain", delay(50, "fast")]).then(console.log);
Promise.all([]).then((r) => console.log("empty:", r));
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
empty: []
[ 'slow', 'plain', 'fast' ]
```

**Explanation**
- Results are in **input order**, not completion order.
- Non-promise values are treated as already-resolved (wrapped via `Promise.resolve`).
- `Promise.all([])` resolves immediately (with `[]`), so it logs before the 200 ms promise finishes.

</details>

---

### Q31 💻 ⭐⭐⭐ `all` vs `allSettled` vs `race` vs `any`
```js
Promise.race([delay(200, "A"), fail(100, "B")])
  .then(console.log, (e) => console.log("race rejected:", e.message));

Promise.any([fail(100, "B"), delay(200, "A")]).then(console.log);

Promise.allSettled([delay(50, "A"), fail(60, "B")]).then(console.log);
```
In what order and with what values do the three lines print? Summarise when to use each combinator.

<details>
<summary>✅ Show solution</summary>

**Output (in time order)**
```
[ { status: 'fulfilled', value: 'A' },
  { status: 'rejected', reason: Error: B ... } ]      // at ~60 ms (allSettled)
race rejected: B                                       // at ~100 ms (race)
A                                                      // at ~200 ms (any)
```

| Combinator | Fulfills when | Rejects when |
|---|---|---|
| `Promise.all` | **all** fulfill → array of values | **any** rejects (first rejection) |
| `Promise.allSettled` | **all** settle → array of `{status, value/reason}` | never |
| `Promise.race` | the **first to settle** fulfills | the first to settle rejects |
| `Promise.any` | the **first to fulfill** | **all** reject → `AggregateError` |

**When to use:** `all` for "I need everything", `allSettled` for "best effort / report each outcome", `race` for timeouts, `any` for "first success wins" (e.g. mirrors/fallbacks).

</details>

---

### Q32 💻 ⭐⭐⭐ Microtask interleaving
```js
Promise.resolve()
  .then(() => console.log("a1"))
  .then(() => console.log("a2"))
  .then(() => console.log("a3"));

Promise.resolve()
  .then(() => console.log("b1"))
  .then(() => console.log("b2"));
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
a1
b1
a2
b2
a3
```

**Explanation**
Each `.then` handler runs as its own microtask, and the next link in a chain is only queued once the previous handler finishes. So the two chains **take turns**, one step each: `a1` (queues a2), `b1` (queues b2), `a2` (queues a3), `b2`, `a3`.

</details>

---

### Q33 💻 ⭐⭐⭐ Resolving with a promise costs extra ticks
```js
const p = new Promise((resolve) => resolve(Promise.resolve("x")));
p.then(() => console.log("p"));

Promise.resolve()
  .then(() => console.log(1))
  .then(() => console.log(2))
  .then(() => console.log(3))
  .then(() => console.log(4));
```
What's the output? Why does `"p"` come after `2`?

<details>
<summary>✅ Show solution</summary>

**Output**
```
1
2
p
3
4
```

**Explanation**
Resolving a promise **with another promise (thenable)** isn't instant. The spec queues a job that calls `inner.then(...)` (1 extra tick), the inner's reaction then resolves `p` (another tick), and only then `p`'s own `.then` handler is queued (third tick). So `p` is delayed by about **two extra microtask ticks** compared with a plain value. This is mostly trivia, but it explains surprising orderings. It's also why `return promise` inside `.then` is slightly slower than `return value`.

</details>

---

### Q34 💻 ⭐⭐⭐ Implement `Promise.all` from scratch
Write `myAll(iterable)` with the same behaviour as `Promise.all`: results in order, accepts non-promise values, resolves `[]` for empty input, rejects with the first rejection.

<details>
<summary>✅ Show solution</summary>

```js
function myAll(iterable) {
  return new Promise((resolve, reject) => {
    const items = [...iterable];
    const results = new Array(items.length);
    let remaining = items.length;

    if (remaining === 0) return resolve(results);

    items.forEach((item, i) => {
      Promise.resolve(item).then(
        (value) => {
          results[i] = value;              // index preserves order
          if (--remaining === 0) resolve(results);
        },
        reject                              // first rejection wins; later calls are ignored
      );
    });
  });
}

myAll([delay(100, "a"), 2, delay(10, "c")]).then(console.log); // [ 'a', 2, 'c' ]
```

**Why `Promise.resolve(item)`?** It wraps plain values and normalises thenables. **Why a counter, not `results.length`?** Results complete out of order, so `results.length` would be wrong (e.g. writing index 2 first makes `length` 3 immediately).

</details>

---

### Q35 🧠 ⭐⭐ Unhandled rejections
What happens when a promise rejects and **nobody** attaches a handler? How does this differ between browsers and Node? What if a handler is attached *later*?

```js
Promise.reject(new Error("nobody listens"));
```

<details>
<summary>✅ Show solution</summary>

- **Browsers:** an error is logged to the console and an **`unhandledrejection`** event is fired on `window` (you can listen to report/track it).
- **Node 15+:** the default mode **throws**: the process prints the error and **exits with code 1**. You can observe it with `process.on("unhandledRejection", ...)`, but you should fix the root cause.
- **Late handlers:** if a handler is attached after the runtime has already reported it (i.e. after the current microtask checkpoint), the promise was still considered unhandled, and a later `rejectionhandled` event can fire. So **attach handlers synchronously** after creating a promise.

**Rule:** every promise chain should end with a `.catch` (or be `await`ed inside a `try/catch`), and un-awaited promises should be handled with `.catch(...)` too.

</details>

---

# Part 4: async/await

### Q36 💻 ⭐ `async` functions always return promises
```js
async function f() { return 1; }
async function g() { throw new Error("oops"); }

console.log(f());
f().then(console.log);
g().catch((e) => console.log("g:", e.message));
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
Promise { 1 }
1
g: oops
```

An `async` function **always** returns a promise. `return x` fulfills it with `x`; `throw` rejects it. So `await`/`.then` is always needed to get the value.

</details>

---

### Q37 💻 ⭐⭐ What does `await` actually do?
```js
async function a() {
  console.log("a1");
  await b();
  console.log("a2");
}
async function b() {
  console.log("b");
}

console.log("start");
a();
console.log("end");
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
start
a1
b
end
a2
```

**Explanation**
An `async` function runs **synchronously until its first `await`** (so `a1` and `b` print immediately). `await` then **pauses `a`** and returns control to the caller (`end` prints); the rest of `a` is scheduled as a **microtask** and resumes once the stack is clear. Even `await` on an already-resolved value yields.

</details>

---

### Q38 💻 ⭐⭐⭐ The classic ordering puzzle
```js
console.log("script start");

setTimeout(() => console.log("setTimeout"), 0);

async function async1() {
  console.log("async1 start");
  await async2();
  console.log("async1 end");
}
async function async2() {
  console.log("async2");
}

async1();

new Promise((resolve) => {
  console.log("promise1");
  resolve();
}).then(() => console.log("promise2"));

console.log("script end");
```

<details>
<summary>✅ Show solution</summary>

**Output (modern engines)**
```
script start
async1 start
async2
promise1
script end
async1 end
promise2
setTimeout
```

**Explanation**
1. Synchronous phase: `script start`, `async1 start`, `async2` (called by `await async2()`), then `promise1` (executor is sync), `script end`.
2. `await async2()` queued the continuation of `async1` as a microtask **before** `promise2`'s `.then` was registered, so `async1 end` comes first.
3. Microtasks done → the macrotask `setTimeout` runs last.

(Older engines, pre-V8 7.2, took extra ticks for `await` and printed `promise2` before `async1 end`.)

</details>

---

### Q39 💻 ⭐⭐ Sequential vs. parallel
Estimate the run time of each version.

```js
async function seq() {
  const a = await delay(100, "a");
  const b = await delay(100, "b");
  return [a, b];
}

async function par() {
  const [a, b] = await Promise.all([delay(100, "a"), delay(100, "b")]);
  return [a, b];
}

async function sneaky() {
  const pa = delay(100, "a");
  const pb = delay(100, "b");
  const a = await pa;
  const b = await pb;
  return [a, b];
}
```

<details>
<summary>✅ Show solution</summary>

- `seq` ≈ **200 ms**: the second `delay` doesn't start until the first has finished.
- `par` ≈ **100 ms**: both start immediately.
- `sneaky` ≈ **100 ms**: both promises are *created* (started) before the first `await`; awaiting afterwards only collects results.

**Rule:** promises start working when **created**, not when awaited. To run independent work concurrently, start it first and await later (usually via `Promise.all`).

⚠️ `sneaky` has a hidden danger. See Q43.

</details>

---

### Q40 💻 ⭐⭐ `forEach` + `async` doesn't wait
```js
async function main() {
  [1, 2, 3].forEach(async (n) => {
    await delay(10);
    console.log(n);
  });
  console.log("done");
}
main();
```
What prints? How do you (a) run the items sequentially and (b) run them in parallel but wait for all?

<details>
<summary>✅ Show solution</summary>

**Output**
```
done
1
2
3
```

`forEach` ignores the return value of its callback, so the promises returned by the `async` callbacks are **thrown away**: nothing waits for them (and their errors become unhandled rejections).

**(a) Sequential**
```js
for (const n of [1, 2, 3]) {
  await delay(10);
  console.log(n);
}
```

**(b) Parallel, then wait**
```js
await Promise.all(
  [1, 2, 3].map(async (n) => {
    await delay(10);
    console.log(n);
  })
);
console.log("done");   // now truly after all items
```

Same trap with `filter`: `arr.filter(async x => ...)` keeps **every** item because a Promise object is always truthy.

</details>

---

### Q41 💻 ⭐⭐⭐ `return` vs. `return await` inside `try`
```js
async function f() {
  try {
    return Promise.reject(new Error("x"));
  } catch (e) {
    return "caught";
  }
}

f().then(console.log, (e) => console.log("rejected:", e.message));
```
What prints? What changes if you write `return await Promise.reject(...)`?

<details>
<summary>✅ Show solution</summary>

**Output:** `rejected: x`

**Explanation**
`return somePromise` hands the promise out of the function **without awaiting it inside the `try`**, so the rejection happens *after* control has left the `try` block: the `catch` never sees it.

With `return await Promise.reject(...)`, the rejection is thrown **inside** the `try`, the `catch` runs, and the output becomes `caught`.

**Rule:** inside `try/catch` (or `try/finally` where cleanup must wait), use `return await`.

</details>

---

### Q42 💻 ⭐⭐ Errors from `async` functions are not synchronous
```js
async function f() {
  throw new Error("boom");
}

try {
  f();
  console.log("no throw");
} catch (e) {
  console.log("caught");
}
```
What happens? How do you fix it?

<details>
<summary>✅ Show solution</summary>

**Output:** `no throw`, followed by an **unhandled promise rejection** (Node exits with the error).

**Explanation**
`f()` returns a **rejected promise** instead of throwing, so the synchronous `try/catch` never triggers. You must `await` it (inside an `async` function) or attach `.catch`:

```js
try {
  await f();       // now the rejection is rethrown at this line
} catch (e) {
  console.log("caught");
}
// or: f().catch((e) => console.log("caught"));
```

</details>

---

### Q43 💻 ⭐⭐⭐ The early-start unhandled rejection trap
```js
async function main() {
  const p1 = fail(100, "first");
  const p2 = fail(50, "second");   // rejects EARLIER than p1

  try {
    await p1;
    await p2;
  } catch (e) {
    console.log("caught:", e.message);
  }
}
main();
```
There's a `try/catch`, so is everything handled?

<details>
<summary>✅ Show solution</summary>

**No.** At 50 ms `p2` rejects, but nothing has attached a handler to it yet (execution is still suspended at `await p1`). The runtime reports an **unhandled rejection**; in Node 15+ that **crashes the process** before the `catch` can even print `caught: first`.

**Fix:** attach handlers to all promises up front, e.g. with `Promise.all` / `Promise.allSettled`:

```js
try {
  const [a, b] = await Promise.all([fail(100, "first"), fail(50, "second")]);
} catch (e) {
  console.log("caught:", e.message);   // "second": first rejection wins
}
```
This is the dark side of the `sneaky` pattern from Q39: start-then-await-later is only safe when you're sure errors will be handled, or when you use a combinator.

</details>

---

### Q44 💻 ⭐⭐ Refactor `.then` chain → `async/await`
Convert to `async/await` keeping identical behaviour (including error recovery and cleanup):

```js
function showProfile(id) {
  return getUser(id)
    .then((user) => getPosts(user.id).then((posts) => ({ user, posts })))
    .then(({ user, posts }) => {
      console.log(user.name, posts.length);
      return posts;
    })
    .catch((err) => {
      console.error("failed", err.message);
      return [];
    })
    .finally(() => console.log("done"));
}
```

<details>
<summary>✅ Show solution</summary>

```js
async function showProfile(id) {
  try {
    const user = await getUser(id);
    const posts = await getPosts(user.id);   // `user` is still in scope: no wrapper object needed
    console.log(user.name, posts.length);
    return posts;
  } catch (err) {
    console.error("failed", err.message);
    return [];
  } finally {
    console.log("done");
  }
}
```

**What improved:** linear top-to-bottom flow, shared variables in one scope, ordinary `try/catch/finally`. **What's the same:** it still returns a promise, and under the hood it's the same promise machinery.

⚠️ `try/catch` here also catches errors from the `console.log`/handler code, just like the trailing `.catch` did.

</details>

---

### Q45 💻 ⭐⭐ Coding: `retry` with exponential backoff
Write `retry(fn, retries, baseDelay)`:
- calls `fn(attempt)` (returns a promise),
- on failure waits `baseDelay * 2 ** attempt` ms and retries,
- after `retries` retries, rejects with the **last** error.

<details>
<summary>✅ Show solution</summary>

```js
async function retry(fn, retries = 3, baseDelay = 100) {
  let lastError;
  for (let attempt = 0; attempt <= retries; attempt++) {
    try {
      return await fn(attempt);        // `await` is essential: lets catch see the rejection
    } catch (err) {
      lastError = err;
      if (attempt < retries) await delay(baseDelay * 2 ** attempt);
    }
  }
  throw lastError;
}

// Demo: fails twice, then succeeds
retry(async (n) => {
  if (n < 2) throw new Error(`fail ${n}`);
  return "ok";
}).then(console.log); // ok (after ~100 + 200 ms)
```

**Details that matter:** `return await` inside `try` (Q41); no sleeping after the final failure; keep the *last* error. Production versions add **jitter**, a max delay, and only retry **retryable** errors.

</details>

---

### Q46 💻 ⭐⭐ Coding: `withTimeout`
Write `withTimeout(promise, ms)` that rejects with `Error("timeout")` if `promise` doesn't settle in time. Make sure the timer doesn't keep running (or keep the process alive) after the promise settles.

<details>
<summary>✅ Show solution</summary>

```js
function withTimeout(promise, ms) {
  let timer;
  const timeout = new Promise((_, reject) => {
    timer = setTimeout(() => reject(new Error("timeout")), ms);
  });
  return Promise.race([promise, timeout]).finally(() => clearTimeout(timer));
}

withTimeout(delay(500, "slow"), 100).catch((e) => console.log(e.message)); // timeout
withTimeout(delay(50, "fast"), 100).then(console.log);                     // fast
```

**Notes**
- `Promise.race` settles with whichever finishes first; `finally` clears the timer.
- The original promise is **not cancelled**; it keeps running. For HTTP use `AbortController` (e.g. `fetch(url, { signal })` or `AbortSignal.timeout(ms)`).

</details>

---

### Q47 💻 ⭐⭐⭐ Coding: concurrency-limited pool
You have an array of **task functions** (each returns a promise). Write `pool(tasks, limit)` that runs at most `limit` tasks at the same time and resolves with results **in input order**.

```js
const tasks = [100, 300, 200, 50].map((ms, i) => () => delay(ms, `t${i}`));
pool(tasks, 2).then(console.log); // [ 't0', 't1', 't2', 't3' ]
```

<details>
<summary>✅ Show solution</summary>

```js
async function pool(tasks, limit) {
  const results = new Array(tasks.length);
  let next = 0;

  async function worker() {
    while (next < tasks.length) {
      const i = next++;                   // safe: single-threaded, no two workers run this at once
      results[i] = await tasks[i]();
    }
  }

  const workers = Array.from({ length: Math.min(limit, tasks.length) }, worker);
  await Promise.all(workers);
  return results;
}
```

**How it works:** `limit` "workers" each loop, pulling the next unstarted task as soon as they finish the previous one. Tasks are **functions**, not already-started promises; otherwise they'd all be running before the pool could limit anything.

**Single-threading helps here:** `next++` can't be interleaved by another worker (contrast with Q8, where the race existed because of an `await` between check and update).

</details>

---

### Q48 🧠 ⭐⭐⭐ How does `async/await` work under the hood?
1. Is `async/await` a new capability or syntax over something existing?
2. Show how you could implement the behaviour using a **generator** and a small `run` helper.

```js
// Goal: make this work
run(function* () {
  const a = yield delay(100, 1);
  const b = yield delay(100, 2);
  return a + b;
}).then(console.log); // 3
```

<details>
<summary>✅ Show solution</summary>

1. It's **syntactic sugar over Promises** (and conceptually generators). `await x` ≈ "pause this function, subscribe to `Promise.resolve(x)`, and resume with the value (or throw the error) as a microtask".

2. A tiny `co`-style runner:
```js
function run(genFn) {
  return new Promise((resolve, reject) => {
    const gen = genFn();

    function step(method, arg) {
      let result;
      try {
        result = gen[method](arg);            // gen.next(value) or gen.throw(error)
      } catch (err) {
        return reject(err);                   // generator threw → reject the whole promise
      }
      if (result.done) return resolve(result.value);

      Promise.resolve(result.value).then(
        (value) => step("next", value),       // resume with the resolved value
        (error) => step("throw", error)       // resume by *throwing* inside the generator
      );
    }

    step("next");
  });
}
```
`yield` ↔ `await`, `function*` ↔ `async function`. Because errors are re-thrown **inside** the generator via `gen.throw`, ordinary `try/catch` around a `yield` works, just like around an `await`.

</details>

---

# Capstone Challenge

### Q49 💻 ⭐⭐⭐ Resilient dashboard loader
You're given a legacy callback API and two modern promise-based services:

```js
// legacy, error-first callback API
const getUserCb = (id, cb) =>
  setTimeout(() => (id > 0 ? cb(null, { id, name: "Ada" }) : cb(new Error("bad id"))), 50);

// modern services
const getPosts   = async (id) => { await delay(100); return ["p1", "p2"]; };
const getFriends = async (id) => { await delay(80);  throw new Error("friends service down"); };
```

Write `loadDashboard(id)` that:
1. wraps `getUserCb` into a Promise (**promisify**),
2. loads the user **first** (everything else depends on it),
3. then loads posts and friends **in parallel**, each with a **500 ms timeout**,
4. never fails because one service failed: returns `{ user, posts, friends, errors }`, using `[]` for failed parts and collecting error messages,
5. rejects only if the **user** can't be loaded.

Use `async/await`, `Promise.allSettled`, and `Promise.race`.

<details>
<summary>✅ Show solution</summary>

```js
const delay = (ms, value) => new Promise((resolve) => setTimeout(resolve, ms, value));

const promisify = (fn) => (...args) =>
  new Promise((resolve, reject) =>
    fn(...args, (err, value) => (err ? reject(err) : resolve(value)))
  );

const withTimeout = (promise, ms) => {
  let timer;
  const timeout = new Promise((_, reject) => {
    timer = setTimeout(() => reject(new Error("timeout")), ms);
  });
  return Promise.race([promise, timeout]).finally(() => clearTimeout(timer));
};

const getUser = promisify(getUserCb);

async function loadDashboard(id) {
  const user = await getUser(id);                       // step 2: sequential dependency; rejects → function rejects

  const [posts, friends] = await Promise.allSettled([   // step 3: parallel + individually guarded
    withTimeout(getPosts(user.id), 500),
    withTimeout(getFriends(user.id), 500),
  ]);

  const valueOrEmpty = (r) => (r.status === "fulfilled" ? r.value : []);

  return {
    user,
    posts: valueOrEmpty(posts),
    friends: valueOrEmpty(friends),
    errors: [posts, friends]
      .filter((r) => r.status === "rejected")
      .map((r) => r.reason.message),
  };
}

loadDashboard(1).then(console.log);
// {
//   user: { id: 1, name: 'Ada' },
//   posts: [ 'p1', 'p2' ],
//   friends: [],
//   errors: [ 'friends service down' ]
// }

loadDashboard(-1).catch((e) => console.log("fatal:", e.message)); // fatal: bad id
```

**Concepts exercised**
- Callback → Promise bridge (`promisify`) and the error-first convention.
- Sequential vs. parallel flow: `await` the dependency, then `allSettled` the independents (total ≈ 50 + 100 ms, not 50 + 100 + 80).
- `allSettled` for partial failure; `race` + `finally` for timeouts with clean-up.
- Errors as rejections, handled once, at the right level: only the user fetch is fatal.

</details>

---

## Score yourself
| Score | Level |
|---|---|
| 0–15 correct | Review the event loop and Promise basics, then retry |
| 16–30 correct | Solid foundation; revisit the ⭐⭐⭐ ordering and error-handling puzzles |
| 31–43 correct | Strong; you understand the subtle semantics |
| 44–49 correct | Excellent; ready to teach this |
