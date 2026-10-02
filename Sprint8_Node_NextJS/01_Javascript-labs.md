# JavaScript Labs for Graduates

## Table of Contents

| # | Lab |
|---|-----|
| 0 | [Setup](#lab-0-setup) |
| 1 | [Why JavaScript?](#lab-1-why-javascript) | 
| 2 | [Browser vs Node.js](#lab-2-browser-vs-nodejs) | 
| 3 | [Syntax, Variables and Data Types](#lab-3-syntax-variables-and-data-types) | 
| 4 | [Functions](#lab-4-functions) | 
| 5 | [Scope, and why `var` is avoided](#lab-5-scope-and-why-var-is-avoided) | 
| 6 | [Control Flow](#lab-6-control-flow) | 
| 7 | [Mini Project: Grade Book](#lab-7-mini-project-grade-book) | 
| 8 | [The Challenge: Bug Hunt](#lab-8-the-challenge-bug-hunt) | 
| | [Solutions](#solutions) | |

**Jump to solutions:** [Lab 1](#lab-1-solutions) · [Lab 2](#lab-2-solutions) · [Lab 3](#lab-3-solutions) · [Lab 4](#lab-4-solutions) · [Lab 5](#lab-5-solutions) · [Lab 6](#lab-6-solutions) · [Lab 7](#lab-7-solutions) · [Lab 8: Bug Hunt](#lab-8-solutions-bug-hunt) · [Wrap-up self-check](#wrap-up-self-check)

---


**Format of every lab:** short concept notes, then a **Try it** demo, then **Exercises** (marked with difficulty: ⭐ easy, ⭐⭐ medium, ⭐⭐⭐ hard). Solutions are all at the **end** of this document. Resist the urge to scroll down early; struggling for ten minutes teaches more than reading an answer.

---

## Lab 0: Setup

You need two places to run JavaScript. That's the point of Lab 2.

**Node.js**
1. Install the current LTS from nodejs.org.
2. Check it works:
   ```bash
   node --version
   ```
3. Create a folder `js-labs` and put each exercise in its own file, e.g. `lab3.js`. Run with:
   ```bash
   node lab3.js
   ```
4. Or type `node` alone for an interactive REPL (exit with `Ctrl+D`).

**Browser**
1. Open any browser, press `F12` (or `Cmd+Option+I` on Mac), choose the **Console** tab.

**Ground rules**
- Always **predict the output before running** the code. Write the prediction down. The gap between prediction and reality is where learning happens.
- Use `console.log()` as your print statement.
- Put `"use strict";` at the top of your files for these labs. It turns some silent mistakes into real errors.

---

## Lab 1: Why JavaScript?

### The story

- **1995:** Brendan Eich built the first version at Netscape in about ten days. It was called Mocha, then LiveScript, then JavaScript (a marketing decision; it has nothing to do with Java).
- It was meant for small tasks in the browser, like validating forms.
- The language is standardised as **ECMAScript** (ES). **ES2015 (ES6)** was the big modernisation: `let`, `const`, arrow functions, classes, modules. A new version ships every year.

### Why learn it?

| Reason | What it means |
|--------|---------------|
| **It's the browser's language** | Every browser runs JavaScript natively. No other language can manipulate a web page without being compiled to it (WebAssembly) or transpiled to it. |
| **It runs everywhere** | Browser, server (Node.js, Deno, Bun), mobile (React Native), desktop (Electron), scripts, serverless functions. |
| **One language across the stack** | Same language, same validation logic, same data format (JSON) on client and server. |
| **Huge ecosystem** | npm is one of the largest package registries in the world. |
| **Non-blocking by design** | JavaScript is single-threaded with an **event loop**. It handles thousands of waiting operations (network, disk) without threads, which is why it works well for I/O-heavy servers. |

### Traits that will feel different from your first language

- **Dynamically typed:** variables don't have types, *values* do.
- **First-class functions:** functions are values; you can store them, pass them, return them.
- **Prototype-based objects** (classes exist but are a layer on top).
- **Forgiving, sometimes too forgiving:** JS tries hard to keep running and converts types behind your back. This causes most of the "WTF JavaScript" memes. These labs teach you to see through them.

### Try it

Run in **both** Node and the browser console:

```js
console.log("Hello from", typeof window === "undefined" ? "Node" : "the browser");
```

---

## Lab 2: Browser vs Node.js

Same language, **different environment**. The language (ECMAScript) gives you `if`, `Array`, `Promise`, `Math`. The *environment* gives you everything else.

| | **Browser** | **Node.js** |
|---|---|---|
| Purpose | Make web pages interactive | Run JS on servers, CLIs, tooling |
| Global object | `window` (also `globalThis`) | `global` (also `globalThis`) |
| Web page access (DOM) | ✅ `document`, `window`, `localStorage` | ❌ none |
| File system | ❌ (sandboxed) | ✅ `fs` module |
| Dialogs | ✅ `alert`, `prompt`, `confirm` | ❌ |
| Command-line args, env vars | ❌ | ✅ `process.argv`, `process.env` |
| Modules | ES modules (`<script type="module">`) | CommonJS (`require`) **and** ES modules (`import`) |
| Security model | Heavily sandboxed | Full access to the machine as the user |
| `fetch` | ✅ | ✅ (modern versions) |
| `setTimeout` | ✅ | ✅ (but returns a different kind of value) |

**Key idea:** if code touches `document`, it's browser code. If it touches `fs` or `process`, it's Node code. Everything else is "just JavaScript" and runs in both.

### Try it

In the browser console:
```js
document.title = "I changed the tab title!";
```
In Node:
```js
console.log(document.title);   // What happens?
```

#
## Lab 3: Syntax, Variables and Data Types

### Syntax in 60 seconds

```js
"use strict";

// single-line comment
/* multi-line comment */

const language = "JavaScript";   // semicolons are optional... but use them
let year = 1995;
year = year + 1;

console.log(`${language} is ${year - 1995} year(s) old`);   // template literal
```

- Statements end with `;` (it's technically optional because of **Automatic Semicolon Insertion**; you'll see this bite in the Challenge).
- Blocks use `{ }`. Identifiers are case-sensitive. Convention: `camelCase`.

### `let`, `const`, `var`

| Keyword | Reassignable? | Scope | Use it? |
|---------|--------------|-------|---------|
| `const` | ❌ (the *binding* can't change) | block | **Default choice** |
| `let` | ✅ | block | When you must reassign |
| `var` | ✅ | function | Avoid (Lab 5 shows why) |

> ⚠️ `const` does **not** make a value immutable. It makes the *variable* unchangeable. A `const` array or object can still be modified.

### Data types

**Primitives** (immutable values): `number`, `string`, `boolean`, `undefined`, `null`, `bigint`, `symbol`.
**Everything else is an object:** arrays, functions, dates, plain `{}` objects.

Surprises for programmers from other languages:
- There is **one** number type (`number`, a 64-bit float). No `int`.
- `undefined` means "no value assigned"; `null` means "deliberately empty". Two different "nothings".
- `typeof null` is `"object"`. A 30-year-old bug that can never be fixed without breaking the web.
- `==` converts types before comparing; `===` doesn't. **Use `===`.**

### Try it

```js
console.log(0.1 + 0.2);            // ?
console.log(typeof 42);            // ?
console.log("5" + 3, "5" - 3);     // ?
```

### Exercises

**3.1 ⭐ `typeof` quiz.** Predict the result of each:
```js
typeof 42
typeof "42"
typeof true
typeof undefined
typeof null
typeof []
typeof {}
typeof Math.max
typeof NaN
typeof 10n
typeof Symbol("id")
```

**3.2 ⭐ What does `const` really do?** For each line, say whether it works or throws, and why:
```js
const user = { name: "Ada" };
user.name = "Grace";          // line 1
user = { name: "Linus" };     // line 2
const nums = [1, 2, 3];
nums.push(4);                 // line 3
nums = [];                    // line 4
```

**3.3 ⭐ Coercion predictions.**
```js
console.log("5" + 3);
console.log("5" - 3);
console.log(true + 1);
console.log("b" + "a" + +"a" + "a");
```
Explain the rule that `+` follows versus the one `-` follows.

**3.4 ⭐ `==` vs `===`.** Fill in `true`/`false` for both operators:

| Expression | `==` | `===` |
|---|---|---|
| `0` vs `false` | ? | ? |
| `null` vs `undefined` | ? | ? |
| `NaN` vs `NaN` | ? | ? |
| `""` vs `0` | ? | ? |

How would you correctly check whether a value is `NaN`?

**3.5 ⭐ Template literals.** Given:
```js
const item = "keyboard";
const price = 49.5;
const qty = 3;
```
Print exactly: `3 x keyboard @ $49.50 = $148.50` using a single template literal and `toFixed(2)`.

**3.6 ⭐ Temperature converter.** Given `const celsius = 36.6;`, print `36.6°C is 97.88°F`. (Formula: `F = C × 9/5 + 32`.)

**3.7 ⭐⭐ Truthy or falsy?** JavaScript has exactly six falsy values (plus `-0` and `0n` which are numeric zeros). Classify each:
`0`, `"0"`, `""`, `" "`, `[]`, `{}`, `null`, `undefined`, `NaN`, `-1`, `"false"`, `0n`

**3.8 ⭐⭐ The edge of numbers.**
```js
console.log(Number.MAX_SAFE_INTEGER + 1 === Number.MAX_SAFE_INTEGER + 2);
console.log(BigInt(Number.MAX_SAFE_INTEGER) + 2n);
```
Predict both. Why would a banking application avoid floating-point `number` for money? Propose one safe alternative.

---

## Lab 4: Functions

Functions are **first-class values**: you can put them in variables, pass them as arguments and return them from other functions.

### Three ways to write one

```js
// 1. Declaration: hoisted, can be called before it appears
function add(a, b) {
  return a + b;
}

// 2. Expression: a function value stored in a variable (NOT hoisted as callable)
const subtract = function (a, b) {
  return a - b;
};

// 3. Arrow function: a shorter way to write a function expression (explained below)
const multiply = (a, b) => {
  return a * b;
};
```

### Arrow functions, step by step

An **arrow function** is a shorter syntax for a function *expression*. Start from a function expression you already know and shrink it:

```js
// Step 0: the function expression from above
const add0 = function (a, b) {
  return a + b;
};

// Step 1: remove the word `function` and put `=>` (the "arrow") after the parameters
const add1 = (a, b) => {
  return a + b;
};

// Step 2: if the body is ONE expression, drop the braces AND `return`.
//         The value is returned automatically ("implicit return")
const add2 = (a, b) => a + b;

// Step 3: exactly one parameter? The parentheses become optional
const square = n => n * n;

// Zero parameters? You must write empty parentheses
const greetWorld = () => "Hello, world";

console.log(add2(2, 3), square(4), greetWorld());   // 5 16 Hello, world
```

Rules and gotchas:
- **With braces `{ }` you must write `return` yourself.** Without braces, the expression's value is returned automatically. Forgetting `return` inside braces gives you `undefined`.
- To implicitly return an **object literal**, wrap it in parentheses: `const makeUser = name => ({ name: name });`. Otherwise the `{` is read as the start of a function body.
- An arrow function is always an **expression**. It isn't hoisted like a declaration, and `const` rules (TDZ) apply to the variable holding it.
- Arrow functions treat the keyword `this` differently from `function` functions. That's a later topic; everything in these labs works with either style.
- Rule of thumb: arrows are great for short inline functions (callbacks, one-liners); `function` declarations are great for named, top-level functions.

### Parameters: JavaScript doesn't check them

```js
function show(a, b) { console.log(a, b); }
show(1);          // b is undefined. No error!
show(1, 2, 3);    // third argument silently ignored
```

Modern tools to manage this:
```js
function greet(name = "stranger") { return `Hello, ${name}`; }   // default parameter
function sum(...numbers) {                                       // rest parameter
  let total = 0;
  for (const n of numbers) total += n;
  return total;
}
```

### Return values

- A function with no `return` (or a bare `return;`) returns `undefined`.
- You can return only one value, but it can be an array or object, and you can **destructure** it:
  ```js
  // `...arr` spreads the array's elements out as separate arguments: Math.min(4, 9, 2)
  const minMax = arr => ({ min: Math.min(...arr), max: Math.max(...arr) });
  const { min, max } = minMax([4, 9, 2]);   // destructuring: pull out the properties `min` and `max`
  ```

### Functions as values

```js
function applyTwice(fn, x) { return fn(fn(x)); }

function addThree(n) { return n + 3; }

console.log(applyTwice(addThree, 10));     // 16  (a named function passed as a value: no parentheses!)
console.log(applyTwice(n => n + 3, 10));   // 16  (an arrow function written inline)
```

A function that you hand to another function to be called later is called a **callback**. (The `sayLater` function you gave to `setTimeout` in Lab 2 was a callback.)

JavaScript arrays have many methods that take callbacks. The most common is **`map`**: it calls your function once for each element and returns a **new array** with the results. The original array is not changed.

```js
const numbers = [1, 2, 3];
const doubled = numbers.map(n => n * 2);
console.log(doubled);   // [ 2, 4, 6 ]
console.log(numbers);   // [ 1, 2, 3 ]   (unchanged)
```

### Exercises

**4.1 ⭐ Three styles.** Write `square(n)` as (a) a declaration, (b) a function expression, (c) an arrow function. Which of these can you call **before** the line where it's defined? Prove it by experiment, then explain.

**4.2 ⭐ `clamp`.** Write `clamp(value, min, max)` that restricts `value` to the range. `clamp(15, 0, 10)` gives `10`; `clamp(-3, 0, 10)` gives `0`; `clamp(5, 0, 10)` gives `5`.

**4.3 ⭐ Defaults and rest.**
- (a) Write `introduce(name, role = "student")` that returns `"<name> - <role>"`. `introduce("Ada")` gives `"Ada - student"` and `introduce("Grace", "admiral")` gives `"Grace - admiral"`.
- (b) Write `average(...nums)` that returns the mean of any number of arguments, or `0` if none are given. `average(2, 4, 6)` gives `4`.
- (c) Predict: what do `introduce("Ada", undefined)` and `introduce("Ada", null)` return? What does this tell you about *when* a default value is used?

**4.4 ⭐ Predict.**
```js
function show(a, b) { console.log(a, b); }
show(1);
show(1, 2, 3);

function noReturn() { const x = 5; }
console.log(noReturn());
```

**4.5 ⭐⭐ Higher-order.** Write `applyN(fn, n, x)` which applies `fn` to `x` a total of `n` times. `applyN(x => x * 2, 3, 1)` should give `8`.

**4.6 ⭐⭐ Function factory.** Write `makeMultiplier(n)` that **returns a function**. `const triple = makeMultiplier(3); triple(5)` gives `15`. (Think about where `n` lives after `makeMultiplier` has finished. This is a **closure**, which Lab 5 builds on.)

**4.7 ⭐⭐ Palindrome.** Write `isPalindrome(text)` that ignores case, spaces and punctuation. `isPalindrome("A man, a plan, a canal: Panama")` is `true`.

**4.8 ⭐⭐ Callbacks.** Using only the array method `map` and an arrow function, turn `[1, 2, 3, 4]` into `["#1", "#2", "#3", "#4"]`.

---

## Lab 5: Scope, and why `var` is avoided

### What is scope?

**Scope** is the region of code where a name is visible. JavaScript uses **lexical scope**: visibility is decided by *where you wrote the code*, not where it's called from.

| Scope | Created by | Seen by `var`? | Seen by `let`/`const`? |
|-------|-----------|:---:|:---:|
| Global / module | The file itself | ✅ | ✅ |
| Function | `function () { }` | ✅ | ✅ |
| **Block** | `{ }` in `if`, `for`, `while`, bare blocks | ❌ **ignores blocks** | ✅ |

When you use a name, JS looks in the current scope, then the enclosing scope, then the next one out... up to global. This is the **scope chain**. An inner variable with the same name **shadows** the outer one.

### The goal: *explain*, don't just *assert*

"Avoid `var`" is advice you'll hear everywhere. The labs below let you **see the evidence yourself** so you can explain it to a colleague. Run each experiment, compare your prediction with the result, and note which language rule caused it.

### Experiment A: `var` leaks out of blocks

```js
if (true) {
  var a = 1;
  let b = 2;
}
console.log(a);
console.log(b);
```
**Why:** `var` only knows about function scope, so a block doesn't contain it. `let` is block-scoped, so `b` is gone when the block ends.
**Consequence:** a variable you thought was local to an `if` or loop is alive (and can be accidentally reused) for the whole function.

### Experiment B: `var` is hoisted as `undefined`

```js
console.log(x);   // no crash!
var x = 5;

console.log(y);   // crash
let y = 5;
```
**Why:** before running, JS registers declarations. `var x` is registered **and initialised to `undefined`**, so reading it early gives `undefined` instead of an error. `let` is registered too, but stays *uninitialised* in the **temporal dead zone (TDZ)** until its line runs, so early access throws a `ReferenceError`.
**Consequence:** with `var`, a typo or ordering mistake produces a silent `undefined` that surfaces far from the real bug. With `let`/`const` the bug is reported at the exact line.

### Experiment C: `var` allows silent redeclaration

```js
var count = 1;
// ...200 lines later, a teammate adds:
var count = 99;
console.log(count);

let total = 1;
let total = 99;      // try it
```
**Why:** `var` lets you declare the same name again in the same scope with no complaint, and the second silently overwrites the first. `let` and `const` raise a `SyntaxError`.

### Experiment D: the famous loop bug

> setTimeout(fn, ms)` schedules `fn` to run later, **after the current code (here, the whole loop) has finished**, even with a delay of `0`.

```js
for (var i = 0; i < 3; i++) {
  setTimeout(() => console.log("var:", i), 0);
}
for (let j = 0; j < 3; j++) {
  setTimeout(() => console.log("let:", j), 0);
}
```
**Why:** `var i` is **one** variable for the whole function. The callbacks run *after* the loop has finished, when `i` is `3`, and they all read that same single variable. With `let`, the loop creates a **fresh `j` for every iteration**, so each callback closes over its own copy.
This is a *closure* (a function that remembers the variables from where it was created) interacting with *scope*.

### Experiment E: `var` pollutes the global object (browser)

In a **classic browser `<script>`** or the console:
```js
var g = "var";
let h = "let";
console.log(window.g);
console.log(window.h);
```
**Why:** top-level `var` (and function declarations) in a classic script become properties of `window`, so they can collide with browser built-ins or other scripts. Top-level `let`/`const` don't. (In Node, files are modules with their own scope, so top-level `var` is not global there. Notice how this differs from the browser!)

### Summary to be able to say out loud

> `var` is function-scoped, so it leaks out of blocks. It's hoisted and initialised to `undefined`, so mistakes become silent bugs. It permits redeclaration. It shares a single binding across loop iterations, which breaks closures. And in browser scripts it attaches itself to `window`. `let` and `const` fix every one of these by being block-scoped, TDZ-protected and non-redeclarable.

### Exercises

**5.1 ⭐ Predict the output** (and name the rule involved):

```js
// (a)
var x = "global";
function test() {
  console.log(x);
  var x = "local";
  console.log(x);
}
test();
```
```js
// (b)
let y = 1;
{
  let y = 2;
  console.log(y);
}
console.log(y);
```
```js
// (c)
function outer() {
  const msg = "hi";
  function inner() { return msg + "!"; }
  return inner;
}
console.log(outer()());
```
```js
// (d)
let v = "outer";
{
  console.log(v);
  let v = "inner";
}
```

**5.2 ⭐⭐ Fix without `let`.** Rewrite Experiment D's `var` loop so it prints `0 1 2` but **still uses `var`** (no `let`, no `const`). This proves you understand *why* it breaks, not just that `let` fixes it. (Hint: you need a new function scope per iteration.)

**5.3 ⭐⭐ Counter with private state.** Write `createCounter(start = 0)` returning an object with `inc()`, `dec()` and `value()`. The count must **not** be directly accessible from outside. Two counters must not interfere with each other.

**5.4 ⭐⭐ Draw the scope chain.** For the code below, list every variable visible at the line marked `// HERE`, and for each say which scope it lives in.
```js
const a = 1;
function f(b) {
  const c = 3;
  if (b > 0) {
    let d = 4;
    // HERE
  }
}
f(2);
```

**5.5 ⭐⭐⭐ Write the case against `var`.** In 120–150 words, write an explanation for a colleague who says *"var works fine, why bother?"* You must reference **at least three** of Experiments A–E as *evidence* and explain the language rule behind each. No unsupported assertions allowed.

---

## Lab 6: Control Flow

### The basics (they look familiar)

```js
// if / else if / else
if (score >= 90) {
  grade = "A";
} else if (score >= 80) {
  grade = "B";
} else {
  grade = "C";
}

// ternary
const label = age >= 18 ? "adult" : "minor";

// switch (uses ===; remember break!)
switch (day) {
  case "Sat":
  case "Sun":
    console.log("weekend");
    break;
  default:
    console.log("weekday");
}

// classic for
for (let i = 0; i < 5; i++) { /* ... */ }

// while / do...while
while (condition) { /* ... */ }
do { /* runs at least once */ } while (condition);
```

### The JavaScript-specific parts

- **`for...of`** iterates over **values** of an iterable (arrays, strings, Maps, Sets).
- **`for...in`** iterates over **property names (keys, as strings)** of an object. It's for objects, not arrays.
- Conditions use **truthiness**, not just booleans: `if ("hello")` runs, `if (0)` doesn't.
- `break` exits a loop; `continue` skips to the next iteration.

### Try it

```js
const fruits = ["apple", "banana", "cherry"];
for (const f of fruits) console.log(f);
for (const i in fruits) console.log(i, typeof i);
```

### Exercises

**6.1 ⭐ FizzBuzz.** Print numbers 1 to 30. For multiples of 3 print `Fizz`, multiples of 5 print `Buzz`, multiples of both print `FizzBuzz`.

**6.2 ⭐ Digit sum.** Using a `while` loop (no strings, no arrays), compute the sum of digits of `98765`. Result: `35`.

**6.3 ⭐⭐ Primes.** Write `isPrime(n)` and print all primes up to 50.

**6.4 ⭐ `for...in` vs `for...of`.** Predict the output of both loops in the *Try it* above. What type are the values `i` in the `for...in` loop? Why does that matter if you write `i + 1`?

**6.5 ⭐ Break and continue.** Using one `for` loop with `continue` and `break`, print the first five even numbers greater than 10.

**6.6 ⭐⭐ Guessing game (simulated).** The secret is `7`. You are given `const guesses = [3, 9, 7, 5];`. Use a `do...while` loop to process guesses one by one, printing `"<n>: too low"`, `"<n>: too high"` or `"<n>: correct!"`, and **stop immediately when correct** (the `5` must never be processed).

**6.7 ⭐⭐ Triangle.** Print this pattern using nested loops:
```
*
**
***
****
*****
```

**6.8 ⭐⭐⭐ Collatz.** Starting from `n = 27`: if `n` is even, halve it; if odd, do `3n + 1`. Repeat until `n` reaches `1`. Count the steps. (Take a guess at the answer before you run it. It will surprise you.)

---

## Lab 7: Mini Project: Grade Book

Combine everything: variables, functions, scope and control flow.

```js
"use strict";

const students = [
  { name: "Asha", scores: [92, 88, 95] },
  { name: "Ben",  scores: [70, 65, 80] },
  { name: "Chen", scores: [55, 60, 58] },
  { name: "Dara", scores: [] },
];
```

**Tools you may need**
- `text.padEnd(n)` pads a string with spaces on the right up to length `n`: `"Ben".padEnd(6)` gives `"Ben   "`.
- `number.toFixed(2)` formats a number with 2 decimals (it returns a string).
- Destructuring in a loop: `for (const { name, scores } of students)` pulls `name` and `scores` out of each student object.

**Requirements**
1. `average(scores)` returns the mean, or `null` for an empty array.
2. `letterGrade(avg)` returns `A` (≥90), `B` (≥80), `C` (≥70), `D` (≥60), otherwise `F`. For `null` return `"-"`.
3. Loop over the students and print an aligned report (two decimals; `N/A` when there's no average).
4. After the report, print the name of the top student.

**Expected output**
```
Asha   91.67  A
Ben    71.67  C
Chen   57.67  F
Dara   N/A    -
Top: Asha
```

**7.1 ⭐⭐ Build it.** Use `const` by default, `let` only where needed. Use `padEnd` for alignment.
**7.2 ⭐⭐⭐ Extend it.** Add a student with a score of `"85"` (a string!) in the array. What happens to your average? Fix your `average` function so it works regardless.

---

## Lab 8: The Challenge: Bug Hunt

Each snippet below **doesn't do what its author wanted**: it either gives the wrong output, throws, or relies on a misunderstanding of JavaScript's syntax or concepts. For every one:

1. **Predict** exactly what happens when it runs. (Write it down!)
2. **Run it** and compare.
3. **Explain** the JavaScript rule responsible.
4. **Fix** it.

Topics covered: syntax, variables, data types, functions, scope, control flow.

### C1 ⭐⭐ The vanishing user
```js
function getUser() {
  return
  {
    name: "Ada"
  };
}
console.log(getUser());
```
**Wanted:** `{ name: 'Ada' }`

### C2 ⭐ The unchangeable total
```js
const total = 0;
for (let i = 1; i <= 5; i++) {
  total += i;
}
console.log(total);
```
**Wanted:** `15`

### C3 ⭐⭐ The echo loop
```js
for (var i = 0; i < 3; i++) {
  setTimeout(function () {
    console.log("Item", i);
  }, 100);
}
```
**Wanted:** `Item 0`, `Item 1`, `Item 2`

### C4 ⭐ The unequal sum
```js
const result = 0.1 + 0.2;
if (result === 0.3) {
  console.log("equal");
} else {
  console.log("not equal");
}
```
**Wanted:** `equal`. Give **two** different valid fixes.

### C5 ⭐⭐ The bad sort
```js
const numbers = [10, 9, 1];
numbers.sort();
console.log(numbers);
```
**Wanted:** `[1, 9, 10]`

### C6 ⭐⭐ Too early to call
```js
console.log(add(2, 3));
var add = function (a, b) {
  return a + b;
};
```
**Wanted:** `5`. Then change `var` to `const`: does the error change? Why?

### C7 ⭐⭐ The leaky switch
```js
function describe(day) {
  switch (day) {
    case "Sat":
    case "Sun":
      console.log("weekend");
    default:
      console.log("weekday");
  }
}
describe("Sat");
```
**Wanted:** only `weekend`

### C8 ⭐⭐ The tiny semicolon
```js
for (let i = 0; i < 5; i++);
{
  console.log(i);
}
```
**Wanted:** `0 1 2 3 4`, each on its own line.

### C9 ⭐ The full-looking empty list
```js
const items = [];
if (items) {
  console.log("has items");
} else {
  console.log("empty");
}
console.log(items == false);
```
**Wanted:** `empty`. Also explain why the last line *seems to contradict* the `if`.

### C10 ⭐⭐⭐ The map that wasn't
```js
const result = ["1", "2", "3"].map(parseInt);
console.log(result);
```
**Wanted:** `[1, 2, 3]`. (Hint: look up the signature of `parseInt` and what `map` passes to its callback.)

### C11 ⭐⭐ The grade that returned nothing
```js
function letterGrade(score) {
  if (score >= 90) return "A";
  else if (score >= 80) return "B";
  else if (score >= 70) "C";
  else return "F";
}
console.log(letterGrade(75));
```
**Wanted:** `C`

### Bonus: write your own trap ⭐⭐⭐
Write a short snippet (≤10 lines) that has a hidden JavaScript gotcha **not** covered above. Swap it with a partner and let them find the bug and explain it. Bonus points for a bug that crashes on only one of Node or the browser.

---
---

# Solutions

> Only read these after you've attempted the exercise. Compare your *reasoning*, not just your answer.

## Lab 3 Solutions

**3.1**
```
typeof 42            // "number"
typeof "42"          // "string"
typeof true          // "boolean"
typeof undefined     // "undefined"
typeof null          // "object"   (historic bug)
typeof []            // "object"   (use Array.isArray to tell)
typeof {}            // "object"
typeof Math.max      // "function" (functions are objects with their own typeof result)
typeof NaN           // "number"   (Not-a-Number is a number!)
typeof 10n           // "bigint"
typeof Symbol("id")  // "symbol"
```

**3.2**
- Line 1: **works.** `const` protects the binding, not the object's contents.
- Line 2: **throws** `TypeError: Assignment to constant variable.`
- Line 3: **works.** Mutating the array is allowed.
- Line 4: **throws** `TypeError`. Same reason as line 2.

If you want a truly unmodifiable object, use `Object.freeze(obj)` (shallow).

**3.3**
```
"53"
2
2
"baNaNa"
```
`+` with a string on either side means **concatenation** (the number is converted to a string). `-` has no string meaning, so both sides are converted to **numbers**. `true` becomes `1`. In the last one, `+"a"` is unary plus, which converts `"a"` to `NaN`, so the pieces are `"b" + "a" + NaN + "a"` giving `"baNaNa"`.

**3.4**

| Expression | `==` | `===` |
|---|---|---|
| `0` vs `false` | `true` | `false` |
| `null` vs `undefined` | `true` | `false` |
| `NaN` vs `NaN` | `false` | `false` |
| `""` vs `0` | `true` | `false` |

`NaN` is never equal to anything, including itself. Use `Number.isNaN(value)` (or `Object.is(value, NaN)`). Prefer `===` always; `==` applies a complicated coercion algorithm.

**3.5**
```js
console.log(`${qty} x ${item} @ $${price.toFixed(2)} = $${(price * qty).toFixed(2)}`);
```
Output: `3 x keyboard @ $49.50 = $148.50`. (`$$` works because `$` only starts interpolation when followed by `{`.)

**3.6**
```js
const celsius = 36.6;
const fahrenheit = celsius * 9 / 5 + 32;
console.log(`${celsius}°C is ${fahrenheit.toFixed(2)}°F`);   // 36.6°C is 97.88°F
```

**3.7**
- **Falsy:** `0`, `""`, `null`, `undefined`, `NaN`, `0n`
- **Truthy:** `"0"`, `" "`, `[]`, `{}`, `-1`, `"false"`

Non-empty strings are truthy even if they *say* "0" or "false". Empty arrays and objects are truthy.

**3.8**
- First line: `true`. Beyond 2^53 the 64-bit float can't represent every integer, so both sides round to the same value.
- Second line: `9007199254740993n`. `BigInt` has arbitrary precision.

Money in binary floating point can't represent values like `0.10` exactly, so errors accumulate (`0.1 + 0.2 !== 0.3`). Safe alternatives: store **integer cents** (`4950` instead of `49.50`), use `BigInt` for cents, or use a decimal library.

## Lab 4 Solutions

**4.1**
```js
function squareA(n) { return n * n; }          // declaration
const squareB = function (n) { return n * n; };  // expression
const squareC = n => n * n;                    // arrow
```
Only the **declaration** can be called before its definition. Declarations are hoisted completely (name and body). With `const`, calling early throws a `ReferenceError` (TDZ). With `var`, the name exists but is `undefined`, so you get `TypeError: squareB is not a function`.

**4.2**
```js
const clamp = (value, min, max) => Math.min(Math.max(value, min), max);
```

**4.3**
```js
// (a)
function introduce(name, role = "student") {
  return `${name} - ${role}`;
}
console.log(introduce("Ada"));              // Ada - student
console.log(introduce("Grace", "admiral")); // Grace - admiral

// (b)
function average(...nums) {       // nums is an array of all the arguments
  if (nums.length === 0) return 0;
  let total = 0;
  for (const n of nums) total += n;
  return total / nums.length;
}
console.log(average(2, 4, 6));    // 4
console.log(average());           // 0

// (c)
console.log(introduce("Ada", undefined));   // Ada - student
console.log(introduce("Ada", null));        // Ada - null
```
(c): a default is used **only when the argument is `undefined`** (missing or explicitly `undefined`). `null` is a real value ("deliberately empty"), so it's passed through as-is.

**4.4**
```
1 undefined
1 2
undefined
```
Missing arguments become `undefined`; extra arguments are ignored; a function without `return` returns `undefined`.

**4.5**
```js
function applyN(fn, n, x) {
  let result = x;
  for (let i = 0; i < n; i++) result = fn(result);
  return result;
}
console.log(applyN(x => x * 2, 3, 1));   // 8
```

**4.6**
```js
function makeMultiplier(n) {
  return x => x * n;
}
const triple = makeMultiplier(3);
console.log(triple(5));   // 15
```
`n` survives after `makeMultiplier` returns because the inner function **closes over** it: it keeps a reference to the scope where it was created.

**4.7**
```js
function isPalindrome(text) {
  const cleaned = text.toLowerCase().replace(/[^a-z0-9]/g, "");
  return cleaned === [...cleaned].reverse().join("");
}
console.log(isPalindrome("A man, a plan, a canal: Panama"));   // true
```

**4.8**
```js
const labels = [1, 2, 3, 4].map(n => `#${n}`);
console.log(labels);   // [ '#1', '#2', '#3', '#4' ]
```

## Lab 5 Solutions

**5.1**
- **(a)** prints `undefined`, then `local`. `var x` inside `test` is hoisted to the top of the function and initialised to `undefined`; it **shadows** the global `x` for the whole function body, even before its assignment line.
- **(b)** prints `2`, then `1`. The inner `let y` lives only inside the block and shadows the outer `y`.
- **(c)** prints `hi!`. `inner` closes over `msg` and keeps it alive after `outer` returns.
- **(d)** throws `ReferenceError: Cannot access 'v' before initialization`. The inner `let v` is hoisted to the top of its block, so the name `v` already refers to the inner binding, but it's in the TDZ until its declaration line. It does **not** fall back to the outer `v`.

**5.2** Create a new function scope per iteration with an IIFE:
```js
for (var i = 0; i < 3; i++) {
  (function (n) {
    setTimeout(() => console.log(n), 0);
  })(i);
}
// 0 1 2
```
Each call gets its own parameter `n`, so each callback closes over a different variable. (Another option: `setTimeout(console.log, 0, i)`, which passes the current value as an argument.)

**5.3**
```js
function createCounter(start = 0) {
  let count = start;            // private: only the returned functions can see it
  return {
    inc: () => ++count,
    dec: () => --count,
    value: () => count,
  };
}
const a = createCounter();
const b = createCounter(10);
a.inc(); a.inc();
console.log(a.value(), b.value());   // 2 10
console.log(a.count);                // undefined (not accessible)
```
Every call to `createCounter` creates a new scope, hence an independent `count`.

**5.4** At `// HERE` the visible variables are:
- `d` (block scope of the `if`)
- `c` and `b` (function scope of `f`; `b` is the parameter)
- `a` (module/global scope)
- `f` itself (module/global scope)

Lookup goes innermost to outermost: block, function, module/global.

**5.5** *(model answer, yours will differ)*

> `var` is function-scoped, not block-scoped. In Experiment A, a `var` declared inside an `if` is still readable after it, while a `let` is not, so `var` lets variables outlive the block you meant them to live in. Experiment B shows `var` is hoisted and initialised to `undefined`, so using it early silently gives `undefined`, whereas `let` throws a clear `ReferenceError` at the exact line, because it sits in the temporal dead zone. In Experiment D, a `var` loop counter is one shared variable, so asynchronous callbacks all see its final value `3`; `let` creates a new binding per iteration, so each callback sees its own. Experiment C adds that `var` lets the same name be redeclared without complaint, hiding overwrites. These aren't style preferences. They are scope rules that make bugs silent. `let` and `const` turn those silent bugs into immediate errors.

## Lab 6 Solutions

**6.1**
```js
for (let i = 1; i <= 30; i++) {
  if (i % 15 === 0) console.log("FizzBuzz");
  else if (i % 3 === 0) console.log("Fizz");
  else if (i % 5 === 0) console.log("Buzz");
  else console.log(i);
}
```
(Check the "both" case **first**; otherwise `15` would print `Fizz`.)

**6.2**
```js
let n = 98765;
let sum = 0;
while (n > 0) {
  sum += n % 10;
  n = Math.floor(n / 10);   // no integer division in JS!
}
console.log(sum);   // 35
```

**6.3**
```js
function isPrime(n) {
  if (n < 2) return false;
  for (let i = 2; i * i <= n; i++) {
    if (n % i === 0) return false;
  }
  return true;
}
const primes = [];
for (let n = 2; n <= 50; n++) {
  if (isPrime(n)) primes.push(n);
}
console.log(primes.join(" "));   // 2 3 5 7 11 13 17 19 23 29 31 37 41 43 47
```

**6.4**
```
apple
banana
cherry
0 string
1 string
2 string
```
`for...of` gives **values**; `for...in` gives **keys as strings**. So `i + 1` with `i = "0"` gives `"01"` (string concatenation), not `1`. Use `for...of` (or `entries()`) for arrays.

**6.5**
```js
let count = 0;
for (let n = 11; ; n++) {
  if (n % 2 !== 0) continue;   // skip odd numbers
  console.log(n);
  count++;
  if (count === 5) break;      // stop after five
}
// 12 14 16 18 20
```

**6.6**
```js
const secret = 7;
const guesses = [3, 9, 7, 5];
let i = 0;
let guess;
do {
  guess = guesses[i++];
  if (guess < secret) console.log(`${guess}: too low`);
  else if (guess > secret) console.log(`${guess}: too high`);
  else console.log(`${guess}: correct!`);
} while (guess !== secret);
// 3: too low
// 9: too high
// 7: correct!
```

**6.7**
```js
for (let row = 1; row <= 5; row++) {
  let line = "";
  for (let col = 0; col < row; col++) line += "*";
  console.log(line);
}
// (shorter alternative: console.log("*".repeat(row)))
```

**6.8**
```js
let n = 27;
let steps = 0;
while (n !== 1) {
  n = n % 2 === 0 ? n / 2 : 3 * n + 1;
  steps++;
}
console.log(steps);   // 111
```
27 takes 111 steps and climbs as high as 9232 before falling to 1.

## Lab 7 Solutions

**7.1**
```js
"use strict";

const students = [
  { name: "Asha", scores: [92, 88, 95] },
  { name: "Ben",  scores: [70, 65, 80] },
  { name: "Chen", scores: [55, 60, 58] },
  { name: "Dara", scores: [] },
];

function average(scores) {
  if (scores.length === 0) return null;
  let total = 0;
  for (const s of scores) total += s;
  return total / scores.length;
}

function letterGrade(avg) {
  if (avg === null) return "-";
  if (avg >= 90) return "A";
  if (avg >= 80) return "B";
  if (avg >= 70) return "C";
  if (avg >= 60) return "D";
  return "F";
}

let top = null;
let topAvg = -Infinity;

for (const { name, scores } of students) {
  const avg = average(scores);
  const shown = avg === null ? "N/A" : avg.toFixed(2);
  console.log(`${name.padEnd(6)} ${shown.padEnd(6)} ${letterGrade(avg)}`);
  if (avg !== null && avg > topAvg) {
    top = name;
    topAvg = avg;
  }
}
console.log(`Top: ${top}`);
```

**7.2** With a string `"85"` in the scores, `total += s` does **string concatenation** once a string enters the sum (e.g. `0 + "85"` is `"085"`), giving a nonsense average. Fix by converting:
```js
for (const s of scores) total += Number(s);
```
Better: also validate with `Number.isNaN(Number(s))` and decide whether to skip or reject bad values.

## Lab 8 Solutions: Bug Hunt

**C1: Output `undefined`.**
*Rule:* **Automatic Semicolon Insertion.** A `return` followed by a line break is treated as `return;`. The `{ name: "Ada" }` below is then parsed as an unreachable *block* containing a *label* `name:` and the expression `"Ada"`.
*Fix:* put the opening brace on the same line: `return {`.

**C2: `TypeError: Assignment to constant variable.`**
*Rule:* `const` bindings can't be reassigned, and `+=` is reassignment.
*Fix:* `let total = 0;` (or use `reduce`).

**C3: prints `Item 3` three times.**
*Rule:* `var i` is function-scoped: one shared variable. The timeouts fire after the loop ends, when `i === 3`.
*Fix:* `for (let i = ...)` (new binding per iteration), or the IIFE pattern from 5.2.

**C4: prints `not equal`** (`0.1 + 0.2` is `0.30000000000000004`).
*Rule:* binary floating point can't represent 0.1 or 0.2 exactly.
*Fixes (two of many):*
```js
// 1. Compare with a tolerance
Math.abs(result - 0.3) < Number.EPSILON
// 2. Use integers (e.g. work in tenths, or cents)
(1 + 2) === 3
```

**C5: prints `[ 1, 10, 9 ]`.**
*Rule:* `Array.prototype.sort()` with no comparator converts items to **strings** and sorts alphabetically (`"10"` < `"9"`).
*Fix:* `numbers.sort((a, b) => a - b);`

**C6: `TypeError: add is not a function`.**
*Rule:* `var add` is hoisted and initialised to `undefined`. The *assignment* of the function hasn't run yet when `add(2, 3)` is called. With `const`, the error becomes `ReferenceError: Cannot access 'add' before initialization` (the TDZ).
*Fix:* define before use, or use a function **declaration** (`function add(a, b) {...}`), which is fully hoisted.

**C7: prints `weekend` and then `weekday`.**
*Rule:* `switch` **falls through** to the next case unless you `break` or `return`. `default` isn't special; it also runs if reached by fall-through.
*Fix:*
```js
case "Sun":
  console.log("weekend");
  break;
default:
  console.log("weekday");
```

**C8: `ReferenceError: i is not defined`.**
*Rule:* the stray `;` after the `for(...)` is an **empty statement**, which becomes the loop body. The `{ ... }` afterwards is just an ordinary block that runs once, *after* the loop, where `let i` no longer exists (block scope!).
*Fix:* remove the semicolon: `for (let i = 0; i < 5; i++) { console.log(i); }`

**C9: prints `has items`, then `true`.**
*Rule:* an empty array is an object, and **all objects are truthy**, so `if (items)` passes. But `items == false` uses a different mechanism: *loose equality* converts the array to a primitive (`""`), then to a number (`0`), and compares it with `false` (`0`). Truthiness and `==` are separate algorithms, so they can disagree.
*Fix:* `if (items.length > 0)` (check what you actually mean). Avoid `==`.

**C10: prints `[ 1, NaN, NaN ]`.**
*Rule:* `map` calls its callback with **three** arguments: `(value, index, array)`. `parseInt(string, radix)` takes the second argument as the radix (number base). So JS runs `parseInt("1", 0)` (radix 0 means default base 10, giving 1), `parseInt("2", 1)` (radix 1 is invalid, giving `NaN`) and `parseInt("3", 2)` (the digit 3 doesn't exist in base 2, giving `NaN`).
*Fix:* `["1","2","3"].map(Number)` or `.map(s => parseInt(s, 10))`.

**C11: prints `undefined`.**
*Rule:* the line `else if (score >= 70) "C";` just evaluates the string `"C"` as an expression statement and throws it away. Without `return`, control falls out of the `if` chain and the function ends with `undefined`.
*Fix:* `else if (score >= 70) return "C";`

---

## Wrap-up: Self-Check

Without looking back, can you explain:

- [ ] Why JavaScript runs in the browser, and what Node.js adds?
- [ ] Which APIs belong to the *language* and which to the *environment*?
- [ ] The difference between `let`, `const` and `var`, with **evidence** for each claim?
- [ ] What hoisting and the temporal dead zone are?
- [ ] Why `typeof null === "object"` and why `0.1 + 0.2 !== 0.3`?
- [ ] How a closure lets a function remember its scope?
- [ ] What `return` followed by a newline does, and why?
- [ ] The difference between `for...in` and `for...of`?

If any box is unchecked, revisit that lab's experiments. Next steps in your journey: arrays and objects in depth, `this`, promises and `async/await`, modules, and TypeScript.
