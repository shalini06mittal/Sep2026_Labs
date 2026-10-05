# JavaScript Challenges: Objects & Arrays, Destructuring/Spread/Rest/Arrows, ES Modules

Tricky, concept-focused questions mixing **theory** and **code**. Try each one yourself before opening the solution.

**How to use this file**
- 🧠 = theory / explain it  |  💻 = predict output or write code
- Difficulty: ⭐ medium · ⭐⭐ hard · ⭐⭐⭐ very tricky
- Click **"Show solution"** under each question to reveal the answer.
- To run module examples in Node.js, save files with the `.mjs` extension (or set `"type": "module"` in `package.json`).

## Table of Contents
1. [Part 1: Objects and Arrays](#part-1-objects-and-arrays)
2. [Part 2: Destructuring, Spread/Rest, Arrow Functions](#part-2-destructuring-spreadrest-and-arrow-functions)
3. [Part 3: ES Modules](#part-3-es-modules-importexport)
4. [Capstone Challenge](#capstone-challenge)

---

# Part 1: Objects and Arrays

### Q1 🧠 ⭐ Reference vs. value
What does this print, and *why*? What is the difference between `b` and `c`?

```js
const a = { x: 1, inner: { y: 1 } };
const b = a;
const c = { ...a };

b.x = 2;
c.inner.y = 99;

console.log(a.x, c.x);
console.log(a.inner.y);
console.log(a === b, a === c);
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
2 1
99
true false
```

**Explanation**
- Objects are stored by **reference**. `b = a` copies the *reference*, so `b` and `a` point to the same object → `b.x = 2` changes `a.x`.
- `{ ...a }` creates a **new top-level object** (a *shallow copy*), so `c.x` stays `1`.
- But `c.inner` still points to the **same nested object** as `a.inner`, so `c.inner.y = 99` is visible through `a`.
- `===` on objects compares references, not contents.

</details>

---

### Q2 💻 ⭐⭐ Object keys are strings (or symbols)
What is the output?

```js
const obj = {};
const k1 = { id: 1 };
const k2 = { id: 2 };

obj[k1] = "first";
obj[k2] = "second";

console.log(obj[k1]);
console.log(Object.keys(obj));
```

How would you fix this if you really need objects as keys?

<details>
<summary>✅ Show solution</summary>

**Output**
```
second
[ '[object Object]' ]
```

**Explanation**
Property keys are coerced to **strings** (unless they are Symbols). Both `k1` and `k2` become `"[object Object]"`, so the second assignment overwrites the first.

**Fix:** use a `Map` (keys can be any value) or a `WeakMap`:
```js
const map = new Map();
map.set(k1, "first");
map.set(k2, "second");
console.log(map.get(k1)); // "first"
```

</details>

---

### Q3 💻 ⭐⭐ Property ordering
What is the output?

```js
const o = {
  b: 1,
  2: "two",
  a: 2,
  1: "one",
  [Symbol("s")]: 3,
};

console.log(Object.keys(o));
console.log(JSON.stringify(o));
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
[ '1', '2', 'b', 'a' ]
{"1":"one","2":"two","b":1,"a":2}
```

**Explanation**
Own property order is:
1. **Integer-like keys** in ascending numeric order,
2. then **string keys** in insertion order,
3. then **symbols** in insertion order.

`Object.keys` and `JSON.stringify` skip symbol keys entirely.

</details>

---

### Q4 💻 ⭐⭐ Sparse arrays and `length`
Predict the output of each `console.log`.

```js
const arr = [1, 2, 3];
arr[5] = 6;

console.log(arr.length);
console.log(arr);

let calls = 0;
arr.forEach(() => calls++);
console.log(calls);

console.log(arr.map((x) => x * 2));

arr.length = 2;
console.log(arr);
```

<details>
<summary>✅ Show solution</summary>

**Output** (Node-style display)
```
6
[ 1, 2, 3, <2 empty items>, 6 ]
4
[ 2, 4, 6, <2 empty items>, 12 ]
[ 1, 2 ]
```

**Explanation**
- Assigning past the end creates **holes**; `length` becomes `highestIndex + 1`.
- `forEach`/`map`/`filter` **skip holes** (they're not "present" properties). `map` preserves the holes in the result.
- Setting `length` to a smaller value **truncates** the array (destructively).

</details>

---

### Q5 💻 ⭐⭐ The default `sort()` trap
```js
const nums = [10, 9, 1, 100];
const sorted = nums.sort();

console.log(sorted);
console.log(sorted === nums);
console.log(["10", "9", "1"].map(parseInt));
```

What is printed? Then, how do you sort numbers correctly without mutating the original?

<details>
<summary>✅ Show solution</summary>

**Output**
```
[ 1, 10, 100, 9 ]
true
[ 10, NaN, 1 ]
```

**Explanation**
- Default `sort()` converts elements to **strings** and compares by UTF-16 order: `"1" < "10" < "100" < "9"`.
- `sort()` **mutates** and returns the **same array reference**.
- `map(parseInt)` is the classic pitfall: `map` passes `(value, index, array)`, so `parseInt` receives the index as its radix:
  - `parseInt("10", 0)` → radix `0` is treated as 10 → `10`
  - `parseInt("9", 1)` → radix `1` is invalid → `NaN`
  - `parseInt("1", 2)` → `"1"` in binary → `1`

**Correct numeric, non-mutating sort**
```js
const sortedNums = [...nums].sort((a, b) => a - b);
// or in modern runtimes:
const sortedNums2 = nums.toSorted((a, b) => a - b);
```

</details>

---

### Q6 💻 ⭐⭐ `Array(3).fill([])`
```js
const grid = Array(3).fill([]);
grid[0].push("X");
console.log(grid);
```
What prints? Give **two** ways to create 3 independent empty arrays.

<details>
<summary>✅ Show solution</summary>

**Output**
```
[ [ 'X' ], [ 'X' ], [ 'X' ] ]
```

**Explanation**
`fill` puts the **same reference** in every slot. All three slots point to one array.

**Fixes**
```js
const g1 = Array.from({ length: 3 }, () => []);
const g2 = [...Array(3)].map(() => []);
```

</details>

---

### Q7 💻 ⭐⭐ `in` vs `hasOwn` vs `undefined` checks
```js
const o = { a: undefined };

console.log("a" in o);
console.log(o.a !== undefined);
console.log(Object.hasOwn(o, "a"));
console.log("toString" in o);
console.log(Object.hasOwn(o, "toString"));
console.log(o.b?.c?.d);
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
true
false
true
true
false
undefined
```

**Explanation**
- `in` checks the **whole prototype chain** (hence `"toString" in o` is `true`).
- `Object.hasOwn` checks **own** properties only.
- A property can exist with the value `undefined`, so `!== undefined` isn't a reliable "exists" check.
- Optional chaining `?.` short-circuits to `undefined` when it hits `null`/`undefined`.

</details>

---

### Q8 💻 ⭐⭐ `Object.freeze` is shallow
```js
const cfg = Object.freeze({ port: 3000, db: { host: "localhost" } });

cfg.port = 4000;
cfg.db.host = "remote";

console.log(cfg.port, cfg.db.host);
```
1. What prints in a *non-strict script*?
2. What happens in an **ES module** (or with `"use strict"`)?
3. Write a `deepFreeze` function.

<details>
<summary>✅ Show solution</summary>

1. **Non-strict:** `3000 remote`. The write to `port` is silently ignored; `db` isn't frozen, so `host` changes.
2. **Strict mode / ES modules** (modules are always strict): `cfg.port = 4000` throws `TypeError: Cannot assign to read only property 'port'...` and the script stops there.
3. **deepFreeze**
```js
const deepFreeze = (obj) => {
  Object.values(obj).forEach((v) => {
    if (typeof v === "object" && v !== null && !Object.isFrozen(v)) deepFreeze(v);
  });
  return Object.freeze(obj);
};
```

</details>

---

### Q9 💻 ⭐⭐ Deep copy options
```js
const original = { name: "A", tags: ["x"], created: new Date(0), fn() {} };

const viaSpread = { ...original };
const viaJSON = JSON.parse(JSON.stringify(original));

viaSpread.tags.push("y");

console.log(original.tags);
console.log(viaJSON.created instanceof Date);
console.log("fn" in viaJSON);
```
What prints? What would `structuredClone(original)` do?

<details>
<summary>✅ Show solution</summary>

**Output**
```
[ 'x', 'y' ]
false
false
```

**Explanation**
- Spread is shallow: `tags` array is shared.
- JSON round-trip turns `Date` into an **ISO string** and **drops functions** (and `undefined` values, Symbols; it also throws on circular references and BigInt).
- `structuredClone(original)` **throws a `DataCloneError`** because the object contains a function. If you remove `fn`, `structuredClone` correctly deep-copies the `Date`, `Map`, `Set`, nested arrays, and circular references (but not prototypes/class methods).

</details>

---

### Q10 💻 ⭐⭐ Coding: `groupBy`
Write `groupBy(array, keyFn)` that returns an object mapping each key to an array of items.

```js
groupBy([6.1, 4.2, 6.3], Math.floor);
// { '4': [4.2], '6': [6.1, 6.3] }

groupBy(["one", "two", "three"], (s) => s.length);
// { '3': ['one', 'two'], '5': ['three'] }
```

<details>
<summary>✅ Show solution</summary>

```js
const groupBy = (arr, keyFn) =>
  arr.reduce((acc, item) => {
    const key = keyFn(item);
    (acc[key] ??= []).push(item);
    return acc;
  }, {});
```

**Notes**
- `??=` initializes the bucket only if missing.
- Use `Object.create(null)` as the initial accumulator if keys like `"__proto__"` or `"constructor"` could appear.
- Modern runtimes have built-ins: `Object.groupBy(arr, fn)` and `Map.groupBy(arr, fn)`.

</details>

---

### Q11 💻 ⭐⭐⭐ Coding: de-duplicate by `id`, keep the *last* occurrence
```js
const items = [
  { id: 1, v: "a" },
  { id: 2, v: "b" },
  { id: 1, v: "c" },
];
// expected: [ { id: 1, v: 'c' }, { id: 2, v: 'b' } ]
```
Write it in one expression. What is the **order** of the result and why?

<details>
<summary>✅ Show solution</summary>

```js
const dedupe = (arr) => [...new Map(arr.map((o) => [o.id, o])).values()];

console.log(dedupe(items));
// [ { id: 1, v: 'c' }, { id: 2, v: 'b' } ]
```

**Why this order?** A `Map` keeps the position of the key from its **first insertion**, but a later `set` with the same key **replaces the value** without moving it. So id `1` stays first (position of first appearance) while holding the last value.

</details>

---

### Q12 💻 ⭐⭐ `splice` vs `slice` vs `delete`
```js
const a = [1, 2, 3, 4, 5];

const removed = a.splice(1, 2);
console.log(a, removed);

const tail = a.slice(-2);
console.log(a, tail);

delete a[0];
console.log(a, a.length);
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
[ 1, 4, 5 ] [ 2, 3 ]
[ 1, 4, 5 ] [ 4, 5 ]
[ <1 empty item>, 4, 5 ] 3
```

**Explanation**
- `splice(start, deleteCount)` **mutates** and returns the removed items.
- `slice` **never mutates**; it returns a shallow copy of a range (negative indexes count from the end).
- `delete arr[i]` removes the *property*, leaving a **hole** and not changing `length`. Use `splice` (or `filter`) to really remove elements.

</details>

---

### Q13 🧠 ⭐⭐ Theory: Which of these are true?
Pick all that apply.

1. `typeof []` is `"array"`.
2. `typeof null` is `"object"`.
3. `Array.isArray(new Array(3))` is `true`.
4. `[] instanceof Object` is `true`.
5. `[1, 2, 3] == "1,2,3"` is `true`.
6. `[10, 1, 3].toString()` is `"10,1,3"`.

<details>
<summary>✅ Show solution</summary>

- 1 ❌: `typeof []` is `"object"`. Use `Array.isArray`.
- 2 ✅: a historical quirk of JS.
- 3 ✅
- 4 ✅: arrays are objects; `Array.prototype` inherits from `Object.prototype`.
- 5 ✅: loose equality converts the array to a primitive via `toString()` → `"1,2,3"`.
- 6 ✅

**True: 2, 3, 4, 5, 6**

</details>

---

# Part 2: Destructuring, Spread/Rest, and Arrow Functions

### Q14 💻 ⭐⭐ Defaults only apply to `undefined`
```js
const { a = 10, b = 20, c = 30, d = 40 } = { a: null, b: undefined, c: 0 };
console.log(a, b, c, d);
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
null 20 0 40
```

**Explanation**
Destructuring defaults trigger **only when the value is `undefined`** (missing or explicitly `undefined`). `null` and `0` are real values, so they are kept.

</details>

---

### Q15 💻 ⭐⭐ Nested destructuring, renaming, and what actually gets declared
```js
const user = {
  id: 1,
  profile: { name: "Sam", address: { city: "Paris" } },
};

const {
  profile: {
    name: userName,
    address: { city, zip = "N/A" },
  },
} = user;

console.log(userName, city, zip);
console.log(typeof profile, typeof name, typeof address);
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
Sam Paris N/A
undefined undefined undefined
```

**Explanation**
In `{ profile: { ... } }` the left side of the colon is the **property to read**, and the right side is the **binding to create**. Here, `profile` and `address` are only paths, so they are **not** declared as variables. `name: userName` creates `userName` only (not `name`).

Caveat: in a browser, `typeof name` could return a string because `window.name` exists globally. In Node or inside a module it's `"undefined"`.

</details>

---

### Q16 💻 ⭐⭐⭐ Array destructuring from any iterable
```js
const [first, , third = "x", ...rest] = "hello";
console.log(first, third, rest);

const [p, q] = new Set([1, 2, 2, 3]);
console.log(p, q);

const [m] = {};
```
What prints? What happens on the last line?

<details>
<summary>✅ Show solution</summary>

**Output**
```
h l [ 'l', 'o' ]
1 2
```
and the last line throws `TypeError: {} is not iterable`.

**Explanation**
- Array destructuring works on **any iterable** (strings, Sets, Maps, generators), not just arrays.
- The empty slot (`,,`) skips `"e"`; `third` gets `"l"`; the rest collects `["l", "o"]`.
- The Set de-duplicates to `{1, 2, 3}` → `p = 1`, `q = 2`.
- Plain objects are **not iterable**, so array destructuring on `{}` throws.

</details>

---

### Q17 💻 ⭐⭐⭐ The semicolon trap with destructuring swap
```js
let a = 1;
let b = 2
[a, b] = [b, a];
console.log(a, b);
```
What happens, and why? How do you fix it?

<details>
<summary>✅ Show solution</summary>

**Result:** `ReferenceError: Cannot access 'b' before initialization`.

**Why?** There's no semicolon after `2`, and a line beginning with `[` does **not** trigger automatic semicolon insertion. The parser reads:
```js
let b = 2[a, b] = [b, a];
```
i.e. property access on the number `2` with the comma-expression `a, b` as the key, and `b` is read while still in its temporal dead zone.

**Fix:** always terminate statements, e.g. `let b = 2;` (or start such lines with a defensive `;[a, b] = [b, a];`).

```js
let a = 1;
let b = 2;
[a, b] = [b, a];
console.log(a, b); // 2 1
```

</details>

---

### Q18 💻 ⭐⭐ Function parameter destructuring
```js
function f({ a = 1, b = 2 } = {}) {
  return a + b;
}
function g({ a = 1, b = 2 }) {
  return a + b;
}

console.log(f());
console.log(f({ a: 5 }));
console.log(g({}));
// Which of the next two throw?
console.log(g());
console.log(f(null));
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
3
7
3
```
Then **both** `g()` and `f(null)` throw `TypeError`.

**Explanation**
- `f()` works because of the `= {}` default for the whole parameter, which applies when the argument is `undefined`.
- `g()` tries to destructure `undefined` → TypeError.
- `f(null)`: default parameters only kick in for `undefined`, **not** `null`. Destructuring `null` throws.

</details>

---

### Q19 💻 ⭐⭐⭐ Default parameter scope (TDZ)
```js
let x = "outer";

function f(a = x, x = "inner") {
  return a;
}

console.log(f());
```
What happens?

<details>
<summary>✅ Show solution</summary>

**Result:** `ReferenceError: Cannot access 'x' before initialization`.

**Explanation**
Parameters are evaluated left to right in their **own scope**, and each acts like a `let` binding. When `a = x` is evaluated, `x` refers to the **parameter** `x` (not the outer variable), which is still in its temporal dead zone. If you swap the order (`x = "inner", a = x`) it works and returns `"inner"`.

</details>

---

### Q20 💻 ⭐⭐ Rest vs. spread, and what's spreadable
Predict the result (or error) of each line.

```js
console.log([..."hi"]);
console.log({ ..."hi" });
console.log({ ...[10, 20] });
console.log({ ...null, ...undefined, ...42 });
console.log([...null]);
const { a, ...others } = { a: 1, b: 2, c: 3 };
console.log(a, others);
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
[ 'h', 'i' ]
{ '0': 'h', '1': 'i' }
{ '0': 10, '1': 20 }
{}
```
then `[...null]` throws `TypeError: null is not iterable` and the remaining lines never run (without it, they would print `1 { b: 2, c: 3 }`).

**Explanation**
- **Array spread** requires an **iterable** → `null` throws.
- **Object spread** copies **own enumerable** properties and silently ignores `null`/`undefined`; primitives like `42` have no own enumerable props, so they contribute nothing; strings/arrays contribute their indices.
- **Rest** (`...others`) in a destructuring pattern collects the *remaining* properties and must be the **last** element.

</details>

---

### Q21 💻 ⭐⭐ Spread is shallow, and order matters
```js
const defaults = { theme: "light", lang: "en", nested: { size: 1 } };
const user = { lang: "fr", theme: undefined };

const merged = { ...defaults, ...user };

console.log(merged.theme, merged.lang);
merged.nested.size = 99;
console.log(defaults.nested.size);
```
What prints? How would you prevent `undefined` from overwriting defaults?

<details>
<summary>✅ Show solution</summary>

**Output**
```
undefined fr
99
```

**Explanation**
- Later spreads **overwrite** earlier keys, even with `undefined`.
- `nested` is shared (shallow copy).

**Ignore `undefined` overrides**
```js
const clean = Object.fromEntries(
  Object.entries(user).filter(([, v]) => v !== undefined)
);
const merged2 = { ...defaults, ...clean };
```

</details>

---

### Q22 🧠 ⭐⭐ Theory: Arrow functions vs regular functions
List **at least five** differences. Then say which of these will work: `new (() => {})()`, an arrow function as an object method that uses `this.name`, and an arrow function using `arguments`.

<details>
<summary>✅ Show solution</summary>

| Feature | Regular function | Arrow function |
|---|---|---|
| `this` | Dynamic (depends on call site) | **Lexical** (inherited from enclosing scope) |
| `arguments` | Available | **Not available** (uses the outer function's, if any) |
| `new` | Can be a constructor | **Cannot** (`TypeError: ... is not a constructor`) |
| `prototype` | Has one | None |
| `call/apply/bind` | Can change `this` | `this` can't be changed (args still passed) |
| Generators (`function*`) | Possible | **Not possible** |
| Implicit return | No | Yes, with expression body |
| Hoisting | Declarations are hoisted | Behaves like a variable (`const`), so not callable before definition |

**The three cases**
- `new (() => {})()` → ❌ TypeError.
- Arrow as an object method using `this.name` → ❌ doesn't do what you want: `this` is the enclosing scope's `this`, **not** the object.
- Arrow using `arguments` → ❌ in a top-level arrow it's a ReferenceError (or the CommonJS wrapper's `arguments` in Node!); inside another function it silently refers to that function's `arguments`. Use rest params `(...args) =>` instead.

</details>

---

### Q23 💻 ⭐⭐⭐ Lexical `this` inside timers
```js
const counter = {
  count: 0,
  start() {
    setTimeout(function () { this.count++; }, 0); // A
    setTimeout(() => { this.count++; }, 0);       // B
  },
};

counter.start();
setTimeout(() => console.log(counter.count), 10);
```
What prints and why?

<details>
<summary>✅ Show solution</summary>

**Output:** `1`

**Explanation**
- **A** is a regular function invoked by the timer, so its `this` is **not** `counter` (it's `window` in browsers, a `Timeout` object in Node). It increments some other object's `count`.
- **B** is an arrow function that captures `this` from `start()`, where `this === counter`. It increments `counter.count` once.

</details>

---

### Q24 💻 ⭐⭐⭐ `arguments` inside an arrow
```js
function outer() {
  const arrow = () => arguments[0];
  return arrow(99);
}

console.log(outer(1));
```

<details>
<summary>✅ Show solution</summary>

**Output:** `1`

**Explanation**
Arrow functions have **no own `arguments`**. `arguments` is resolved lexically to `outer`'s arguments object, so `arguments[0]` is `1`, not `99`.

</details>

---

### Q25 💻 ⭐⭐ Returning an object literal from an arrow
```js
const a = () => { id: 1 };
const b = () => ({ id: 1 });

console.log(a());
console.log(b());
```

<details>
<summary>✅ Show solution</summary>

**Output**
```
undefined
{ id: 1 }
```

**Explanation**
In `a`, the `{ ... }` is parsed as a **function body block**, and `id: 1` is a *labeled statement* (label `id`, expression `1`). Nothing is returned. Wrapping the literal in parentheses makes it an expression.

</details>

---

### Q26 💻 ⭐⭐ Coding: `pipe` with rest/spread and arrows
Implement `pipe(...fns)` that returns a function applying `fns` left-to-right.

```js
const inc = (n) => n + 1;
const dbl = (n) => n * 2;

pipe(inc, dbl)(3); // 8
pipe()(5);         // 5
```

<details>
<summary>✅ Show solution</summary>

```js
const pipe = (...fns) => (x) => fns.reduce((acc, fn) => fn(acc), x);

console.log(pipe(inc, dbl)(3)); // (3+1)*2 = 8
console.log(pipe()(5));         // 5 (empty reduce returns the initial value)
```

**Bonus:** `compose` is the same with `reduceRight`.

</details>

---

### Q27 💻 ⭐⭐⭐ Coding: `omit` using rest destructuring
1. Remove a **single dynamic key** using destructuring only (no `delete`).
2. Write `omit(obj, ...keys)` for any number of keys, immutably.

```js
omit({ a: 1, b: 2, c: 3 }, "a", "c"); // { b: 2 }
```

<details>
<summary>✅ Show solution</summary>

**1. One dynamic key (computed key + rest)**
```js
const key = "a";
const { [key]: _removed, ...rest } = { a: 1, b: 2, c: 3 };
console.log(rest); // { b: 2, c: 3 }
```

**2. Many keys**
```js
const omit = (obj, ...keys) =>
  Object.fromEntries(Object.entries(obj).filter(([k]) => !keys.includes(k)));

console.log(omit({ a: 1, b: 2, c: 3 }, "a", "c")); // { b: 2 }
```
Destructuring needs the removed names to be known when writing the pattern, so a variable number of keys requires `Object.entries`/`fromEntries` (or a loop).

</details>

---

### Q28 💻 ⭐⭐ Coding: reshape data with destructuring in parameters
Given:
```js
const users = [
  { id: 1, name: { first: "Ada", last: "Lovelace" }, role: "admin" },
  { id: 2, name: { first: "Alan", last: "Turing" } },
];
```
Produce:
```js
[
  { id: 1, full: "Ada Lovelace", role: "admin" },
  { id: 2, full: "Alan Turing", role: "guest" },
]
```
using a single `map` call with a destructured parameter.

<details>
<summary>✅ Show solution</summary>

```js
const result = users.map(({ id, name: { first, last }, role = "guest" }) => ({
  id,
  full: `${first} ${last}`,
  role,
}));
```
Notes: destructure nested objects in the parameter list, give `role` a default, and wrap the returned object literal in parentheses.

</details>

---

### Q29 💻 ⭐⭐ Iterating with destructuring
```js
const scores = new Map([["al", 90], ["bo", 75]]);

for (const [name, score] of scores) console.log(name, score);
for (const [name, score] of Object.entries({ al: 90, bo: 75 })) console.log(name, score);
for (const [name, score] of { al: 90, bo: 75 }) console.log(name, score);
```
Which loop fails and why?

<details>
<summary>✅ Show solution</summary>

The first two print `al 90` and `bo 75` each.

The **third** throws `TypeError: {(intermediate value)} is not iterable`. A plain object isn't iterable, so `for...of` can't consume it. A `Map` iterates `[key, value]` pairs; `Object.entries()` converts an object into an array of such pairs.

</details>

---

# Part 3: ES Modules (import/export)

### Q30 💻 ⭐⭐ Imports are live, read-only bindings
```js
// counter.js
export let count = 0;
export function inc() { count++; }

// main.js
import { count, inc } from "./counter.js";

console.log(count);
inc();
console.log(count);
count = 5;
```
What happens at each step?

<details>
<summary>✅ Show solution</summary>

**Output**
```
0
1
```
then `TypeError: Assignment to constant variable.`

**Explanation**
- Imports are **live bindings** to the exporting module's variable (not copies), so after `inc()` the imported `count` reflects `1`.
- But the importing module **cannot reassign** an imported binding; only the exporting module can change it.
- (Contrast with CommonJS, where `const { count } = require(...)` copies the value at that moment.)

</details>

---

### Q31 💻 ⭐⭐ Import hoisting and evaluation order
```js
// hi.js
console.log("hi.js loaded");
export const hi = "hi";

// main.js
console.log("main start");
import { hi } from "./hi.js";
console.log("main end", hi);
```
What's the output order?

<details>
<summary>✅ Show solution</summary>

**Output**
```
hi.js loaded
main start
main end hi
```

**Explanation**
`import` declarations are **hoisted**: the module graph is parsed, linked, and the dependencies are **evaluated first** (depth-first), then the importing module's body runs. Where you place the `import` in the file doesn't change that.

</details>

---

### Q32 💻 ⭐⭐ Modules run only once
```js
// log.js
console.log("log.js evaluated");
export const x = 1;

// main.js
import "./log.js";
import { x } from "./log.js";
const m = await import("./log.js");
console.log(x, m.x);
```
How many times is `"log.js evaluated"` printed?

<details>
<summary>✅ Show solution</summary>

**Output**
```
log.js evaluated
1 1
```

**Explanation**
A module is fetched, instantiated and evaluated **once** per module graph (cached by its resolved URL). Every `import`, static or dynamic, gets the **same module instance**. This makes modules natural singletons.

</details>

---

### Q33 💻 ⭐⭐⭐ Circular dependency and the TDZ
```js
// a.js
import { b } from "./b.js";
export const a = "A";
console.log("a sees", b);

// b.js
import { a } from "./a.js";
export const b = "B";
console.log("b sees", a);

// main.js
import "./a.js";
```
What happens? How do you fix it?

<details>
<summary>✅ Show solution</summary>

**Result:** `ReferenceError: Cannot access 'a' before initialization`

**Explanation**
1. `main` → starts `a.js`, which depends on `b.js`, so `b.js` is evaluated **first**.
2. `b.js` imports `a`, but `a.js` is already "in progress" (cycle), so it's not evaluated again; the binding `a` exists but is still in the **temporal dead zone**.
3. `console.log("b sees", a)` reads `a` before `a.js` ran `export const a = "A"`, so it throws.

**Fixes**
- Extract the shared code into a third module that neither depends on circularly.
- Don't touch imported values at module top level; only inside functions called later:
```js
// b.js
import { a } from "./a.js";
export const b = "B";
export const showA = () => console.log("b sees", a); // safe: runs later
```
- Note that `function` declarations are hoisted and initialized early, so circular access to exported *functions* may work where `const` doesn't.

</details>

---

### Q34 💻 ⭐⭐ Which of these are valid syntax?
```js
// 1
export const a = 1, b = 2;
// 2
export { a as default };
// 3
export default const x = 1;
// 4
export default class {}
// 5
if (true) { import fs from "fs"; }
// 6
import * as ns from "./m.js"; ns.foo = 1;
// 7
export { a as "my-name" };
```

<details>
<summary>✅ Show solution</summary>

1. ✅ Valid. Exports two named bindings.
2. ✅ Valid. Re-labels `a` as the default export.
3. ❌ **SyntaxError.** `export default` takes an *expression* or function/class declaration, not `const`/`let`/`var`. Write `const x = 1; export default x;`.
4. ✅ Valid. Anonymous default class.
5. ❌ **SyntaxError.** Static `import` must be at the **top level** of a module. Use `await import("fs")` for conditional loading.
6. ✅ The line is syntactically valid, but at **runtime** it throws `TypeError` because a module namespace object is immutable (you can't add or assign properties).
7. ✅ Valid in modern engines (ES2022 string-literal export names), though rarely used.

</details>

---

### Q35 💻 ⭐⭐ Imported `const` vs. mutating the object
```js
// config.js
export const config = { debug: false };

// main.js
import { config } from "./config.js";

config.debug = true;   // (1)
config = { debug: true }; // (2)
```
Which line throws, and what's the underlying rule?

<details>
<summary>✅ Show solution</summary>

- (1) ✅ **Works.** The *binding* is read-only, but the object it points to is still mutable, and every module that imports `config` sees the change (shared singleton).
- (2) ❌ **TypeError: Assignment to constant variable.** You can't reassign an imported binding.

To prevent (1), export a frozen object (`Object.freeze`) or expose getter/setter functions.

</details>

---

### Q36 💻 ⭐⭐ Default vs. named exports
```js
// math.js
export default function add(a, b) { return a + b; }
export const PI = 3.14159;

// main.js: which imports work, and what does each bind?
import add from "./math.js";             // (1)
import sum from "./math.js";             // (2)
import { add } from "./math.js";         // (3)
import add2, { PI } from "./math.js";    // (4)
import * as math from "./math.js";       // (5)
console.log(math.default === add, math.PI);
```

<details>
<summary>✅ Show solution</summary>

1. ✅ Imports the default export under the name `add`.
2. ✅ Also valid. A default import can be given **any name** (that's the key difference from named imports).
3. ❌ Fails at link time: `SyntaxError: The requested module './math.js' does not provide an export named 'add'`. The function's own name (`add`) isn't an export name; the export is called `default`.
4. ✅ Default plus a named import in one statement (default must come first).
5. ✅ A namespace object with keys `default` and `PI`.

(Also note: declaring both `add` in (1) and (3) in the *same* file would be a duplicate-declaration error. These are shown as separate alternatives.)

The last line prints `true 3.14159`. A trade-off to know: named exports give better tooling (auto-imports, refactoring, tree-shaking clarity); default exports allow inconsistent naming across files.

</details>

---

### Q37 💻 ⭐⭐⭐ Dynamic `import()` with destructuring
```js
// math.js (same as before)
export default function add(a, b) { return a + b; }
export const PI = 3.14159;
```
1. What does `import("./math.js")` return?
2. Write a line that loads the module lazily and destructures **both** the default export (as `add`) and `PI`.
3. Can it be used inside `if` or functions? Does it work in non-module scripts?

<details>
<summary>✅ Show solution</summary>

1. A **Promise** that resolves to the module **namespace object** (`{ default: [Function: add], PI: 3.14159 }`).
2. Rename `default` during destructuring:
```js
const { default: add, PI } = await import("./math.js");
console.log(add(1, 2), PI);
```
3. Yes. It is a runtime expression, so it can be used in conditions, functions, and event handlers (great for code-splitting and lazy loading). It also works in classic (non-module) scripts and CommonJS files, making it the bridge for loading ESM from CJS.

Top-level `await` (used above) is only allowed in ES modules; elsewhere use `.then()` or an `async` function.

</details>

---

### Q38 💻 ⭐⭐⭐ Re-exports and "barrel" files
```js
// shapes/circle.js
export const circle = "circle";
export default function drawCircle() {}

// shapes/square.js
export const square = "square";

// shapes/index.js  <-- barrel file
???
```
Write `shapes/index.js` so that consumers can do:
```js
import { circle, square, drawCircle, squareNs } from "./shapes/index.js";
```
Does `export * from "./circle.js"` re-export the default export?

<details>
<summary>✅ Show solution</summary>

```js
// shapes/index.js
export * from "./circle.js";                // re-exports circle (NOT default)
export { default as drawCircle } from "./circle.js"; // re-export the default under a name
export * from "./square.js";                // re-exports square
export * as squareNs from "./square.js";    // namespace re-export (ES2020)
```

**Key point:** `export * from` **does not re-export `default`**. You must do it explicitly with `export { default } from "..."` or `export { default as name } from "..."`.

Also: if two `export *` statements export the same name, that name becomes **ambiguous** and is excluded (importing it explicitly produces a SyntaxError).

</details>

---

### Q39 🧠 ⭐⭐ Theory: ES Modules vs. CommonJS
Give **at least six** differences. Then answer: why can't you write `import x from "./a.js"` and use `__dirname` in a native Node ES module? What is the replacement?

<details>
<summary>✅ Show solution</summary>

| Aspect | CommonJS | ES Modules |
|---|---|---|
| Syntax | `require` / `module.exports` | `import` / `export` |
| Loading | Synchronous, at runtime | Static analysis, asynchronous loading |
| Bindings | **Copies** of values | **Live** read-only bindings |
| Position | `require` can be anywhere | Static `import` only at top level (use `import()` for dynamic) |
| Strict mode | Opt-in | **Always strict** |
| Top-level `this` | `module.exports` | `undefined` |
| Top-level `await` | Not supported | Supported |
| Tree-shaking | Hard | Easy (static structure) |
| File extension | Optional in `require` | Must be explicit in browsers/Node (`./a.js`) |
| Enabling in Node | default for `.js`/`.cjs` | `.mjs` or `"type": "module"` in `package.json` |

**`__dirname`:** `__dirname`, `__filename`, `require`, and `module` are variables injected by CommonJS' module **wrapper function**. ES modules aren't wrapped that way. Replacement:
```js
import { fileURLToPath } from "node:url";
import { dirname } from "node:path";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
// Newer Node versions also provide import.meta.dirname and import.meta.filename
```

</details>

---

### Q40 🧠 ⭐ Theory: Using modules in the browser
1. How do you load a module in HTML?
2. How does it differ from a classic `<script>` regarding execution timing and scope?
3. Why does opening the HTML via `file://` often fail?

<details>
<summary>✅ Show solution</summary>

1. `<script type="module" src="./main.js"></script>`
2. Module scripts are **deferred by default** (run after the document is parsed, in order), are in **strict mode**, have **module scope** (top-level variables are *not* added to `window`), and are fetched with CORS.
3. Browsers enforce **CORS** for module scripts, and `file://` origins are treated as opaque, so loads are blocked. Serve the folder via a local server (e.g. `npx serve`, `python -m http.server`).

</details>

---

# Capstone Challenge

### Q41 💻 ⭐⭐⭐ Build an immutable shopping cart module
Create **`cart.js`** with:
- a named export `createItem` taking `{ id, name, price, qty = 1, ...meta }` and returning `{ id, name, price, qty, meta }`,
- a named export `addItem(cart, item)` that returns a **new** cart; if an item with the same `id` exists, **increase its `qty`** instead of adding a duplicate,
- a named export `total(cart)` computing the sum of `price * qty` using parameter destructuring,
- a **default export** object containing all three functions.

Then in **`main.js`**, import the default **and** the named functions, add three items (one repeated), print the total, and prove the original cart wasn't mutated. Use arrow functions, spread, rest, and destructuring.

<details>
<summary>✅ Show solution</summary>

```js
// cart.js
export const createItem = ({ id, name, price, qty = 1, ...meta }) => ({
  id,
  name,
  price,
  qty,
  meta,
});

export const addItem = (cart, item) =>
  cart.some(({ id }) => id === item.id)
    ? cart.map((i) => (i.id === item.id ? { ...i, qty: i.qty + item.qty } : i))
    : [...cart, item];

export const total = (cart) =>
  cart.reduce((sum, { price, qty }) => sum + price * qty, 0);

export default { createItem, addItem, total };
```

```js
// main.js
import cartApi, { createItem, addItem, total } from "./cart.js";

const empty = [];
const apple  = createItem({ id: 1, name: "Apple",  price: 0.5, color: "red" });
const bread  = createItem({ id: 2, name: "Bread",  price: 2.25, qty: 2 });
const apple2 = createItem({ id: 1, name: "Apple",  price: 0.5, qty: 3 });

const cart = [apple, bread, apple2].reduce(addItem, empty);

console.log(cart);
// [
//   { id: 1, name: 'Apple', price: 0.5, qty: 4, meta: { color: 'red' } },
//   { id: 2, name: 'Bread', price: 2.25, qty: 2, meta: {} }
// ]
console.log(total(cart));              // 6.5  (4*0.5 + 2*2.25)
console.log(empty.length);             // 0  -> original cart untouched
console.log(apple.qty);                // 1  -> original item untouched
console.log(cartApi.total === total);  // true -> same function reference
```

**Concepts exercised**
- Object & array handling without mutation (`map`, spread instead of `push`).
- Destructuring with defaults and **rest** (`...meta`) in parameters.
- Arrow functions with implicit returns (object literal wrapped in parentheses).
- Named + default exports, and how a default import and named imports from the same module share the **same module instance**.

Interesting follow-up: `reduce(addItem, empty)` works because `addItem(accumulator, currentItem)` matches the callback signature `(acc, item, index, array)`. The extra index/array arguments are ignored here since `addItem` declares only two parameters.

</details>

---

## Score yourself
| Score | Level |
|---|---|
| 0–15 correct | Review the basics, then retry |
| 16–28 correct | Solid foundation, revisit the ⭐⭐⭐ ones |
| 29–36 correct | Strong. You understand the subtle semantics |
| 37–41 correct | Excellent. Ready to teach this |
