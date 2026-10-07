# TypeScript Challenges: What TS Adds, Primitive & Function Types, Interfaces, Type Erasure

Tricky, concept-focused questions mixing **theory** and **code**. Try each one yourself before opening the solution.

**How to use this file**
- 🧠 = theory / explain it  |  💻 = predict the compiler/runtime result or write code
- Difficulty: ⭐ medium · ⭐⭐ hard · ⭐⭐⭐ very tricky
- Click **"Show solution"** under each question to reveal the answer.
- Unless stated otherwise, assume `"strict": true` in `tsconfig.json` (this includes `strictNullChecks` and `noImplicitAny`).
- Try snippets in the [TypeScript Playground](https://www.typescriptlang.org/play) or locally with `npx tsc`. Error codes (e.g. `TS2322`) and wording can vary slightly between TypeScript versions.

## Table of Contents
1. [Part 1: What TypeScript Adds (and Why)](#part-1-what-typescript-adds-and-why)
2. [Part 2: Primitive Types & Typing Functions](#part-2-primitive-types--typing-functions)
3. [Part 3: Interfaces](#part-3-interfaces)
4. [Part 4: Type Erasure](#part-4-type-erasure)
5. [Capstone Challenge](#capstone-challenge)

---

# Part 1: What TypeScript Adds (and Why)

### Q1 🧠 ⭐ TypeScript vs. JavaScript
1. What exactly is TypeScript's relationship to JavaScript?
2. List what TypeScript **adds**, and what it deliberately does **not** add.
3. Why do teams adopt it?

<details>
<summary>✅ Show solution</summary>

1. TypeScript is a **superset of JavaScript**: every valid JS program is (syntactically) valid TS. It adds a **static type system** and a **compiler** (`tsc`) that checks types and then emits plain JavaScript.
2. **Adds:** type annotations and inference, `interface`/`type` declarations, generics, union/literal types, enums, access modifiers (`private`, `readonly`), better editor tooling (autocomplete, refactoring, go-to-definition), and compile-time checks (null-safety, wrong arguments, typos).
   **Does not add:** a new runtime, new runtime type checks, or different execution semantics. The output is ordinary JavaScript running on the same engine.
3. **Why:**
   - Catch whole classes of bugs *before* running (wrong types, missing properties, `undefined` access).
   - Types act as **living documentation** of function contracts and data shapes.
   - Safer **refactoring** in large codebases (rename a property → every usage is flagged).
   - Much better **IDE support**.

</details>

---

### Q2 💻 ⭐ Inference and assignment
```ts
let age = 25;
age = "twenty-five";
```
Does this compile? Why, even though no type was written?

<details>
<summary>✅ Show solution</summary>

**It does not compile:** `Type 'string' is not assignable to type 'number'. (TS2322)`

**Explanation**
TypeScript **infers** `age: number` from the initializer `25`. A variable's type is fixed at declaration and checked on every assignment afterwards. You get type safety "for free" without annotating everything. Annotate when inference can't know your intent (function parameters, empty arrays, public APIs).

</details>

---

### Q3 💻 ⭐⭐ Do type errors stop the build?
```ts
// app.ts
const n: number = "hello";
console.log("still runs?");
```
You run `tsc app.ts` and then `node app.js`. What happens?

<details>
<summary>✅ Show solution</summary>

`tsc` **reports the error** (and exits with a non-zero code) but, **by default, still emits `app.js`**. Running `node app.js` prints `still runs?`.

**Explanation**
Type checking and JS emission are **independent** steps. To stop emitting on errors, enable **`"noEmitOnError": true`** (or fail your CI on `tsc --noEmit` errors). The type checker is a *guard rail for developers*, not part of the runtime.

</details>

---

### Q4 💻 ⭐⭐ Inferred types: literal vs. widened
What type does TypeScript infer for each declaration?

```ts
let a = 1;
const b = 1;
let c = [1, "a"];
const d = { x: 1 };
const e = [1, 2] as const;
let f = "hi" as const;
```

<details>
<summary>✅ Show solution</summary>

| Declaration | Inferred type | Why |
|---|---|---|
| `let a = 1` | `number` | `let` can be reassigned → literal **widened** to `number` |
| `const b = 1` | `1` | `const` can't change → keeps the **literal type** |
| `let c = [1, "a"]` | `(string \| number)[]` | array element type is the union of element types |
| `const d = { x: 1 }` | `{ x: number }` | properties are mutable, so `x` is widened (const only freezes the *binding*) |
| `const e = [1, 2] as const` | `readonly [1, 2]` | `as const` → readonly tuple of literal types |
| `let f = "hi" as const` | `"hi"` | `as const` keeps the literal even for `let` |

</details>

---

### Q5 💻 ⭐⭐ The operator that still "works"
Which lines produce compile errors?

```ts
const a = "5" * 2;
const b = "5" + 2;
const c = true + 1;
const d = "5" - "2";
```

<details>
<summary>✅ Show solution</summary>

- `"5" * 2` ❌ **error** (TS2362): arithmetic operands must be `number`, `bigint` (or `any`/enum).
- `"5" + 2` ✅ **compiles**: `+` is also string concatenation, so the result type is `string` (`"52"` at runtime).
- `true + 1` ❌ **error** (TS2365): `+` isn't defined for `boolean` and `number`.
- `"5" - "2"` ❌ **error**: `-` only accepts numeric operands.

**Takeaway:** JS silently coerces (`"5" * 2 === 10`); TypeScript refuses most implicit coercion, but `+` with a string operand is always legal, so it's still a classic source of bugs.

</details>

---

### Q6 🧠 ⭐⭐ What TypeScript can and can't catch
For each bug, say whether TypeScript catches it **at compile time**:

1. Calling `user.nmae` when the property is `name`.
2. Passing `"42"` to a function declared `(n: number)`.
3. A REST API returns `{ "age": "42" }` instead of a number, and your code assumes `age: number`.
4. Division by zero.
5. Using `any` and calling `value.foo.bar()` on a string.
6. Forgetting to handle `null` from `document.getElementById("x")`.

<details>
<summary>✅ Show solution</summary>

1. ✅ **Caught.** `Property 'nmae' does not exist`.
2. ✅ **Caught.** Argument of type `string` is not assignable to `number`.
3. ❌ **Not caught.** Types describe what you *claim* data looks like; incoming JSON is untrusted. The wrong type only shows up at runtime (see Q30).
4. ❌ **Not caught.** TS has no value-level reasoning about `0`.
5. ❌ **Not caught.** `any` switches off checking.
6. ✅ **Caught** (with `strictNullChecks`): `getElementById` returns `HTMLElement | null`.

**Rule:** TypeScript verifies that **your code is consistent with its declared types**. It can't verify that the **outside world** matches them.

</details>

---

# Part 2: Primitive Types & Typing Functions

### Q7 💻 ⭐⭐ `string` vs. `String`
```ts
let a: string = "x";
let b: String = "x";
let c: string = new String("x");
let d: String = new String("x");
```
Which lines error? Why? What should you use?

<details>
<summary>✅ Show solution</summary>

- `a` ✅ fine.
- `b` ✅ fine: a primitive is assignable to its wrapper interface type.
- `c` ❌ `Type 'String' is not assignable to type 'string'. 'string' is a primitive, but 'String' is a wrapper object.`
- `d` ✅ fine.

**Explanation**
`string`, `number`, `boolean`, `bigint`, `symbol` are the **primitive types**; `String`, `Number`, `Boolean` are **wrapper object** types. Use the lowercase primitives for annotations. (Avoid `Object`, `Function`, and `{}` too, since they're very loose.)

</details>

---

### Q8 💻 ⭐⭐ Literal types and widening
```ts
function move(dir: "left" | "right") {}

const d1 = "left";
let d2 = "left";
const obj = { dir: "left" };

move(d1);
move(d2);
move(obj.dir);
```
Which calls error? How do you fix each?

<details>
<summary>✅ Show solution</summary>

- `move(d1)` ✅ `const d1` has the literal type `"left"`.
- `move(d2)` ❌ `let d2` is widened to `string`.
- `move(obj.dir)` ❌ object properties are mutable, so `dir` is widened to `string`.

**Fixes**
```ts
let d2: "left" | "right" = "left";          // annotate
const obj = { dir: "left" } as const;       // freeze literal types (dir is readonly "left")
// or: const obj: { dir: "left" | "right" } = { dir: "left" };
```
Literal unions are TypeScript's lightweight alternative to enums for a fixed set of values.

</details>

---

### Q9 💻 ⭐ `strictNullChecks`
```ts
function len(s: string | null): number {
  return s.length;
}
```
What's the error and how do you fix it (give two ways)?

<details>
<summary>✅ Show solution</summary>

**Error:** `'s' is possibly 'null'. (TS18047)`. Without `strictNullChecks`, `null`/`undefined` are assignable to every type and this slips through, which is a major source of "Cannot read properties of null" crashes.

**Fixes**
```ts
// 1) Narrow with a check
function len(s: string | null): number {
  if (s === null) return 0;
  return s.length;           // s is string here
}

// 2) Optional chaining + nullish coalescing
const len2 = (s: string | null): number => s?.length ?? 0;
```
(Avoid the non-null assertion `s!.length`; it silences the compiler without adding any runtime safety.)

</details>

---

### Q10 💻 ⭐ Arity is checked
```ts
function add(a: number, b: number): number {
  return a + b;
}

add(1);
add(1, 2, 3);
add("1", 2);
```
What does the compiler say about each call? What would plain JavaScript do at runtime for the first two?

<details>
<summary>✅ Show solution</summary>

- `add(1)`: ❌ `Expected 2 arguments, but got 1. (TS2554)`
- `add(1, 2, 3)`: ❌ `Expected 2 arguments, but got 3.`
- `add("1", 2)`: ❌ `Argument of type 'string' is not assignable to parameter of type 'number'.`

In plain JS, `add(1)` returns **`NaN`** (`1 + undefined`) and `add(1, 2, 3)` returns `3` (extra arg ignored). TypeScript turns these silent mistakes into compile errors.

</details>

---

### Q11 💻 ⭐⭐ Optional and default parameters
```ts
function f1(a?: number, b: number) {}
function f2(a: number = 1, b: number) {}
function f3(name: string, greeting?: string) {
  return greeting.toUpperCase();
}
function f4(name: string, greeting = "Hello") {
  return greeting.toUpperCase();
}
```
Which declarations or bodies error? What type does `greeting` have in `f3` and `f4`? What's the type of `f4`'s parameter *from the caller's point of view*?

<details>
<summary>✅ Show solution</summary>

- `f1` ❌ `A required parameter cannot follow an optional parameter. (TS1016)`
- `f2` ✅ allowed: a parameter with a default may precede required ones, but callers must pass `undefined` explicitly to get the default: `f2(undefined, 5)`.
- `f3` ❌ body: `greeting` is `string | undefined` → `'greeting' is possibly 'undefined'`.
- `f4` ✅ body: `greeting` is `string` (default fills in `undefined`).

For callers, `f4`'s signature is `(name: string, greeting?: string) => string`: it's **optional** for the caller but always a `string` inside.

</details>

---

### Q12 💻 ⭐⭐ `void` isn't what you think
```ts
function a(): void { return 42; }
const b: () => void = () => 42;

const result = b();
```
Which lines compile? What's the type of `result`? Why is this designed this way?

<details>
<summary>✅ Show solution</summary>

- `a` ❌ `Type 'number' is not assignable to type 'void'`. A function *declared* `: void` can't return a value.
- `b` ✅ compiles. When a **function type** has a `void` return, any function can be assigned to it; its return value is just ignored.
- `result` has type `void`.

**Why?** So that this works:
```ts
const list: number[] = [];
[1, 2, 3].forEach((n) => list.push(n)); // push returns number, but forEach expects (…) => void
```
Related: `undefined` is a real value type; `void` means "don't use the result"; `never` means "never returns" (always throws / infinite loop).

</details>

---

### Q13 💻 ⭐⭐ `any` vs. `unknown` vs. `never`
```ts
let a: any = "text";
a.foo.bar();

let u: unknown = "text";
u.toUpperCase();

if (typeof u === "string") {
  u.toUpperCase();
}

function fail(msg: string): never {
  throw new Error(msg);
}
```
Which lines error? What happens at runtime for `a.foo.bar()`? When would you use each type?

<details>
<summary>✅ Show solution</summary>

- `a.foo.bar()` ✅ **compiles** (`any` turns checking off) but **throws at runtime**: `TypeError: Cannot read properties of undefined (reading 'bar')`.
- `u.toUpperCase()` ❌ `'u' is of type 'unknown'. (TS18046)`: you must narrow first.
- inside `typeof u === "string"` ✅ `u` is narrowed to `string`.
- `fail` ✅ `never` = the function never returns normally.

**Usage**
- `any`: opt-out escape hatch; avoid (it spreads and silently disables checks).
- `unknown`: the **type-safe `any`**: use for values whose shape you don't know yet (JSON, `catch` variables, user input) and narrow before use.
- `never`: functions that never return, impossible states, and **exhaustiveness checks** (see Q35).

</details>

---

### Q14 💻 ⭐⭐ Contextual typing and `noImplicitAny`
```ts
const nums = [1, 2, 3];
const labels = nums.map((n) => n.toFixed(1));

function double(x) {
  return x * 2;
}
```
Why doesn't `n` need an annotation while `x` does?

<details>
<summary>✅ Show solution</summary>

- In `nums.map((n) => ...)`, `map` expects a callback `(value: number, ...) => U`, so TypeScript **contextually types** `n` as `number`.
- `double(x)` has **no context** to infer from. With `noImplicitAny`, this errors: `Parameter 'x' implicitly has an 'any' type. (TS7006)`.

**Rule of thumb:** annotate **function parameters** you declare yourself; let inference handle local variables and callback parameters. Return types can be inferred too, but annotating them on exported functions documents intent and catches accidental changes (see Q17).

</details>

---

### Q15 💻 ⭐⭐ Function assignability and arity
```ts
const cb1: (a: number, b: number) => void = (a) => {};
const cb2: (a: number) => void = (a, b) => {};
```
Which one errors? Why does this matter in practice?

<details>
<summary>✅ Show solution</summary>

- `cb1` ✅ OK. A function that accepts **fewer** parameters can stand in for one that supplies more; extra arguments are simply ignored.
- `cb2` ❌ error: the callback demands a second parameter `b` that callers of `(a: number) => void` will never provide.

**In practice:** this is why `arr.forEach((item) => ...)` works even though `forEach` calls the callback with `(item, index, array)`. It's also why `["1","2"].map(parseInt)` compiles but misbehaves: `parseInt(string, radix?)` happily accepts the index as `radix`.

</details>

---

### Q16 💻 ⭐⭐ Narrowing a union parameter
```ts
function format(v: string | number | boolean): string {
  return v.toFixed(2);
}
```
1. What's the error?
2. Rewrite it so each type is handled. What's the type of `v` in the final statement?

<details>
<summary>✅ Show solution</summary>

1. ❌ `Property 'toFixed' does not exist on type 'string | number | boolean'.` You can only use operations valid for **every** member of the union.

2. Narrow with `typeof` checks:
```ts
function format(v: string | number | boolean): string {
  if (typeof v === "string") return v.trim();     // v: string
  if (typeof v === "number") return v.toFixed(2); // v: number
  return v ? "yes" : "no";                        // v: boolean (what's left)
}
```
TypeScript performs **control-flow analysis**, so the type of `v` shrinks as each branch rules out possibilities. In the last line it's `boolean`.

Note that `typeof` narrowing is possible **because `typeof` is a runtime operation**, which is relevant for Part 4.

</details>

---

### Q17 💻 ⭐⭐⭐ The return type that wasn't satisfied
```ts
function sign(n: number): "pos" | "neg" | "zero" {
  if (n > 0) return "pos";
  if (n < 0) return "neg";
  if (n === 0) return "zero";
}
```
Does this compile? Why or why not? What does the `NaN` input do at runtime?

<details>
<summary>✅ Show solution</summary>

**It doesn't compile:** `Function lacks ending return statement and return type does not include 'undefined'. (TS2366)`.

**Explanation**
TypeScript doesn't know that `n > 0`, `n < 0`, and `n === 0` cover all numbers. It sees a path where nothing is returned. And it's right: `sign(NaN)` falls through and returns **`undefined`** at runtime, violating the declared contract. The annotation caught a real bug.

**Fix**
```ts
function sign(n: number): "pos" | "neg" | "zero" {
  if (n > 0) return "pos";
  if (n < 0) return "neg";
  return "zero";   // (NaN also lands here; handle it explicitly if it matters)
}
```
**Lesson:** explicit return type annotations make the **function body** answer to its **contract**.

</details>

---

# Part 3: Interfaces

### Q18 💻 ⭐⭐ Structural typing and excess property checks
```ts
interface Point {
  x: number;
  y: number;
}

const p3 = { x: 1, y: 2, z: 3 };

const a: Point = p3;
const b: Point = { x: 1, y: 2, z: 3 };
```
Which line errors, and why do they differ even though the data is the same?

<details>
<summary>✅ Show solution</summary>

- `a` ✅ compiles.
- `b` ❌ `Object literal may only specify known properties, and 'z' does not exist in type 'Point'. (TS2353)`

**Explanation**
TypeScript uses **structural typing**: a value is assignable to an interface if it **has at least** the required properties with the right types. `p3` has `x` and `y`, so it fits `Point` (extra `z` is fine).

But for **fresh object literals** assigned directly, TS applies an additional **excess property check**, because a typo like `{ x: 1, yy: 2 }` is much more likely to be a mistake than intentional. Assigning via a variable bypasses this check.

</details>

---

### Q19 💻 ⭐⭐ `readonly` and optional properties
```ts
interface User {
  readonly id: number;
  name: string;
  email?: string;
}

const u: User = { id: 1, name: "Ada" };

u.id = 2;
u.email.toLowerCase();
(u as any).id = 2;
console.log(u.id);
```
Which lines error? What prints? Does `readonly` protect the value at runtime?

<details>
<summary>✅ Show solution</summary>

- `u.id = 2` ❌ `Cannot assign to 'id' because it is a read-only property. (TS2540)`
- `u.email.toLowerCase()` ❌ `'u.email' is possibly 'undefined'.` (Optional means the type is `string | undefined`.)
- `(u as any).id = 2` ✅ compiles. It prints **`2`**.

`readonly` is **compile-time only**. At runtime it's a normal writable property. If you need real immutability use `Object.freeze` (shallow) or `#private` fields/closures.

</details>

---

### Q20 💻 ⭐⭐⭐ Index signatures
```ts
interface Dict {
  [key: string]: number;
}

const d: Dict = { a: 1, b: "x" };
const e: Dict = { a: 1 };

console.log(e.missing.toFixed(2));
```
1. Which line errors at compile time?
2. Does `e.missing.toFixed(2)` compile? What happens at runtime?
3. How would you make this safer?

<details>
<summary>✅ Show solution</summary>

1. `d` ❌: `b: "x"` is a string but every property must be `number`.
2. `e.missing.toFixed(2)` ✅ **compiles**, because the index signature claims **every** string key yields a `number`. At runtime `e.missing` is `undefined`, so you get `TypeError: Cannot read properties of undefined (reading 'toFixed')`.
3. Declare the value type as possibly missing, or enable `"noUncheckedIndexedAccess": true` (index access then yields `number | undefined`):
```ts
interface Dict { [key: string]: number | undefined }
```
Bonus gotcha: adding a named property whose type isn't assignable to the index signature (e.g. `name: string` next to `[k: string]: number`) is an error.

</details>

---

### Q21 💻 ⭐⭐ Declaration merging
```ts
interface A { x: number }
interface A { y: number }

const a: A = { x: 1 };

type B = { x: number };
type B = { y: number };
```
What errors do you get and why? What does this tell you about `interface` vs. `type`?

<details>
<summary>✅ Show solution</summary>

- `const a: A = { x: 1 }` ❌ `Property 'y' is missing in type '{ x: number; }' but required in type 'A'. (TS2741)`: the two `interface A` declarations **merge** into `{ x: number; y: number }`.
- `type B = ...` twice ❌ `Duplicate identifier 'B'. (TS2300)`: type aliases can't be re-opened.

Declaration merging is how libraries let you **augment** their types (e.g. adding properties to `Window` or Express's `Request`). It can also accidentally merge types you didn't mean to.

</details>

---

### Q22 💻 ⭐⭐ `extends` and conflicting members
```ts
interface Animal { name: string }

interface Dog1 extends Animal { name: number }
interface Dog2 extends Animal { name: "Rex" }
```
Which interface errors? Why is the other fine?

<details>
<summary>✅ Show solution</summary>

- `Dog1` ❌ `Interface 'Dog1' incorrectly extends interface 'Animal'. Types of property 'name' are incompatible. (TS2430)`
- `Dog2` ✅ `"Rex"` is a subtype of `string`: an extending interface may **narrow** a property's type but never make it incompatible.

This guarantees that anything typed as `Dog2` can safely be used wherever an `Animal` is expected (**substitutability**).

</details>

---

### Q23 💻 ⭐⭐⭐ Weak type detection
```ts
interface Options {
  verbose?: boolean;
  depth?: number;
}

const o = { vrbose: true };       // typo!
const opts: Options = o;
```
All of `Options`' properties are optional, and `o` has *no* conflicting properties, so is it assignable? Explain.

<details>
<summary>✅ Show solution</summary>

**It errors:** `Type '{ vrbose: boolean; }' has no properties in common with type 'Options'. (TS2559)`

**Explanation**
Without a special rule, *any* object would be assignable to an all-optional ("weak") type, so typos would pass silently. **Weak type detection** requires at least **one matching property** when the target type has only optional properties. (`const opts: Options = {}` is still fine.)

</details>

---

### Q24 🧠 ⭐⭐ `interface` vs. `type`
Give at least five differences or guidelines between interfaces and type aliases.

<details>
<summary>✅ Show solution</summary>

| Aspect | `interface` | `type` alias |
|---|---|---|
| Describes | Object shapes (incl. callable/constructable) | **Anything**: primitives, unions, tuples, mapped/conditional types |
| Union types | ❌ can't *be* a union | ✅ `type Id = string \| number` |
| Extending | `extends` (errors on conflicting members) | intersection `&` (conflicts produce `never`-ish types silently) |
| Re-opening / merging | ✅ declaration merging | ❌ duplicate identifier |
| Used with `implements` | ✅ | ✅ (if it's an object type) |
| Error messages / perf | Generally cleaner and cached | Can be more verbose on big intersections |
| Runtime presence | none | none |

**Practical guideline:** use `interface` for **object shapes/public contracts** (especially ones others may extend), and `type` for **unions, tuples, aliases, and computed types**. Consistency within a codebase matters more than the choice.

</details>

---

### Q25 💻 ⭐⭐ `implements` and the runtime
```ts
interface Shape {
  area(): number;
}

class Circle implements Shape {
  constructor(public r: number) {}
  area() {
    return Math.PI * this.r ** 2;
  }
}

const c = new Circle(2);
if (c instanceof Shape) {
  console.log("is a shape");
}
```
1. What's the compile error?
2. What does `implements Shape` do at runtime?
3. What JavaScript does the `constructor(public r: number)` compile to?

<details>
<summary>✅ Show solution</summary>

1. ❌ `'Shape' only refers to a type, but is being used as a value here. (TS2693)`: interfaces don't exist at runtime, so `instanceof Shape` is meaningless.
2. **Nothing.** `implements` is purely a compile-time check that the class has the required members. It's erased.
3. A **parameter property** is shorthand that *does* emit code:
```js
class Circle {
    constructor(r) {
        this.r = r;
    }
    area() { return Math.PI * this.r ** 2; }
}
```
(Parameter properties are one of the few TS features that aren't pure type annotations. See Q36.)

To check "is this a Shape?" at runtime, check the **structure** yourself (see Q34) or use a discriminant property.

</details>

---

### Q26 💻 ⭐⭐⭐ Assignability puzzle
Which lines compile?

```ts
interface Named { name: string }
interface Aged { age: number }

const both = { name: "A", age: 1 };
class Person { name = "P"; }

function greet(x: Named) {}

const n: Named = both;                  // 1
const m: Named & Aged = n;              // 2
greet({ name: "A", age: 3 });           // 3
greet(both);                            // 4
greet(new Person());                    // 5
const list: Named[] = [both, new Person()]; // 6
```

<details>
<summary>✅ Show solution</summary>

1. ✅ `both` has `name: string`; the extra `age` is fine (not a fresh literal).
2. ❌ `n` is only `Named`; it lacks `age` (`Property 'age' is missing`).
3. ❌ **Excess property check** on the fresh object literal (`age`).
4. ✅ Passing a variable bypasses the excess property check.
5. ✅ **Structural typing:** `Person` has a `name: string`, so it's a `Named`, with no declared relationship needed.
6. ✅ Both elements are structurally `Named`.

TypeScript is **structurally typed** ("duck typing", checked at compile time), unlike Java/C#, which are nominally typed.

</details>

---

### Q27 💻 ⭐⭐ Coding: model an API response
Write interfaces for this JSON, then a function `getZip(user: UserDto): string` that returns the zip code or `"N/A"` when it's `null`. Also: `nickname` may be absent, and `role` is only ever `"admin"` or `"user"`.

```json
{
  "id": 7,
  "name": "Ada",
  "address": { "city": "London", "zip": null },
  "tags": ["math", "engines"],
  "role": "admin"
}
```
After that: does `const user: UserDto = JSON.parse(text)` guarantee that the shape is right?

<details>
<summary>✅ Show solution</summary>

```ts
interface Address {
  city: string;
  zip: string | null;
}

interface UserDto {
  id: number;
  name: string;
  nickname?: string;              // may be absent
  address: Address;
  tags: string[];
  role: "admin" | "user";         // literal union, not just string
}

function getZip(user: UserDto): string {
  return user.address.zip ?? "N/A";
}
```
Use `??` (not `||`): `||` would also replace an empty string `""`.

**Does `JSON.parse` guarantee the shape?** **No.** `JSON.parse` returns `any`, so the assignment compiles no matter what the text contains. The interface is a **claim**, not a check. Validate untrusted data at the boundary with a type guard or a schema library (see Q34).

</details>

---

### Q28 💻 ⭐⭐ Coding: type this JavaScript
Convert to TypeScript using an interface. Then say which of the four calls are compile errors, and what each returns in the *original* JS.

```js
function calculateTotal(items, taxRate) {
  let total = 0;
  for (const item of items) total += item.price * item.qty;
  return total * (1 + taxRate);
}

calculateTotal([{ price: "10", qty: 2 }], 0.1);   // (1)
calculateTotal([{ price: 5 }], 0.1);              // (2)
calculateTotal([{ price: 5, qty: 1 }]);           // (3)
calculateTotal([{ price: 5, qty: 1 }], "10%");    // (4)
```

<details>
<summary>✅ Show solution</summary>

```ts
interface LineItem {
  price: number;
  qty: number;
}

function calculateTotal(items: LineItem[], taxRate: number): number {
  let total = 0;
  for (const item of items) total += item.price * item.qty;
  return total * (1 + taxRate);
}
```

| Call | TypeScript | Original JS result |
|---|---|---|
| (1) `price: "10"` | ❌ `string` not assignable to `number` | `"10" * 2 = 20` → `22` (works *by accident* via coercion) |
| (2) missing `qty` | ❌ property `qty` is missing | `5 * undefined = NaN` → `NaN` |
| (3) missing `taxRate` | ❌ expected 2 arguments | `1 + undefined = NaN` → `NaN` |
| (4) `"10%"` | ❌ `string` not assignable to `number` | `1 + "10%" = "110%"`, then `5 * "110%"` → `NaN` |

All four are caught **before running**. In JS, three silently return `NaN` and one "works" for the wrong reasons.

</details>

---

# Part 4: Type Erasure

### Q29 💻 ⭐⭐ What JavaScript is emitted?
Write the JavaScript that `tsc` (target ES2020) emits for this file:

```ts
interface User {
  id: number;
  name: string;
}

type Id = string | number;

function greet(user: User, greeting: string = "Hi"): string {
  return `${greeting}, ${user.name}`;
}

const id: Id = 5;
const user = { id: id as number, name: "Ada" } satisfies User;

console.log(greet(user));
```

<details>
<summary>✅ Show solution</summary>

```js
function greet(user, greeting = "Hi") {
    return `${greeting}, ${user.name}`;
}
const id = 5;
const user = { id: id, name: "Ada" };
console.log(greet(user));
```
(Whitespace may differ.)

**What disappeared:** `interface User`, `type Id`, all `: type` annotations, `as number`, and `satisfies User`. The **default parameter value** stays because it's real JavaScript. **Type erasure:** types are checked at compile time, then removed; nothing about them exists when the program runs.

</details>

---

### Q30 💻 ⭐⭐ Compiles fine, crashes at runtime
```ts
interface User {
  id: number;
  name: string;
}

const data = JSON.parse('{"id":"oops","name":"Ada"}') as User;
console.log(data.id.toFixed(2));
```
Does it compile? What happens at runtime? Why didn't the type save us?

<details>
<summary>✅ Show solution</summary>

**Compiles ✅.** `JSON.parse` returns `any`, and `as User` is an **assertion** ("trust me"), not a check.

**Runtime:** `TypeError: data.id.toFixed is not a function`, because `data.id` is the string `"oops"`.

**Why:** after compilation the code is just `const data = JSON.parse(...); console.log(data.id.toFixed(2));`. No `User` information exists to validate against. Static types can't verify **external data**; you need **runtime validation** (type guards, or libraries such as Zod/Valibot/io-ts) at the boundary: network responses, files, `localStorage`, user input.

</details>

---

### Q31 💻 ⭐⭐ Assertions don't convert
```ts
const a = "42" as number;
const n = "42" as unknown as number;

console.log(typeof n, n + 1);
```
1. Which line errors?
2. What does the second line print?

<details>
<summary>✅ Show solution</summary>

1. `"42" as number` ❌ `Conversion of type 'string' to type 'number' may be a mistake. (TS2352)`: the types don't sufficiently overlap. Going through `unknown` (or `any`) is the "I know better" escape hatch.
2. **Output:** `string 421`. The assertion only changes what the *compiler believes*; the value is still the string `"42"`, so `+ 1` concatenates.

**Assertions (`as`) are erased and perform no runtime conversion.** To actually convert, use `Number("42")`, `parseInt`, etc.

</details>

---

### Q32 💻 ⭐⭐ Types don't strip data
```ts
interface Point { x: number; y: number }

const p3 = { x: 1, y: 2, z: 3 };
const p: Point = p3;

console.log(Object.keys(p));
console.log(JSON.stringify(p));
```
What's printed?

<details>
<summary>✅ Show solution</summary>

**Output**
```
[ 'x', 'y', 'z' ]
{"x":1,"y":2,"z":3}
```

**Explanation**
Typing `p` as `Point` doesn't remove `z`. It's the same object at runtime. The type just *limits what you may access* in the code. This matters when you serialize/log/send typed objects: **extra properties (e.g. passwords, internal IDs) still go along.** To drop fields you must build a new object explicitly (`{ x: p.x, y: p.y }`).

</details>

---

### Q33 💻 ⭐⭐⭐ `private` vs. `#private`
```ts
class Account {
  private balance = 100;
  #pin = 1234;
}

const a = new Account();

console.log((a as any).balance);
console.log((a as any).pin);
console.log((a as any)["#pin"]);
```
1. What do `a.balance` and `a.#pin` (outside the class) do at compile time?
2. What do the three `console.log`s print?
3. Which one is "real" privacy?

<details>
<summary>✅ Show solution</summary>

1. `a.balance` ❌ `Property 'balance' is private and only accessible within class 'Account'. (TS2341)`. `a.#pin` ❌ `Property '#pin' is not accessible outside class 'Account'...`

2. **Output**
```
100
undefined
undefined
```
`private` is erased, so `balance` is an ordinary public property at runtime. `#pin` is stored in a **hidden slot** that isn't reachable via any property name.

3. **`#pin`** is enforced by the JavaScript engine. TypeScript's `private` is only a compile-time courtesy.

</details>

---

### Q34 💻 ⭐⭐⭐ Coding: a runtime type guard
Since `interface User` doesn't exist at runtime, write `isUser(x: unknown): x is User` that checks the structure for real, and use it to safely handle `JSON.parse` output.

```ts
interface User { id: number; name: string }
```

<details>
<summary>✅ Show solution</summary>

```ts
interface User {
  id: number;
  name: string;
}

function isUser(x: unknown): x is User {
  if (typeof x !== "object" || x === null) return false;
  const obj = x as Record<string, unknown>;
  return typeof obj.id === "number" && typeof obj.name === "string";
}

function parseUser(json: string): User {
  const data: unknown = JSON.parse(json);   // annotate as unknown, not any
  if (!isUser(data)) {
    throw new Error("Invalid user payload");
  }
  return data;                              // narrowed to User
}

console.log(parseUser('{"id":1,"name":"Ada"}'));   // { id: 1, name: 'Ada' }
// parseUser('{"id":"oops","name":"Ada"}');         // throws Error: Invalid user payload
```

**Key ideas**
- The `x is User` return type is a **type predicate**: it tells the compiler "if this returns `true`, treat `x` as `User`". TypeScript **trusts** you, so the check must be correct.
- The *runtime* part (`typeof`, `in`, `Array.isArray`, ...) is real JavaScript; the *type* part is erased.
- For bigger shapes, a schema validation library infers the TS type **from** the runtime schema, so the two can't drift apart.

</details>

---

### Q35 💻 ⭐⭐⭐ Coding: discriminated union with exhaustiveness
Types are erased, so to tell shapes apart at runtime you need **real data** (a tag). Model `Circle` (`radius`) and `Square` (`side`) with a discriminant property, write `area(shape)`, and make the compiler **fail** if someone later adds a `Triangle` but forgets to update `area`.

<details>
<summary>✅ Show solution</summary>

```ts
interface Circle { kind: "circle"; radius: number }
interface Square { kind: "square"; side: number }
type Shape = Circle | Square;

function area(s: Shape): number {
  switch (s.kind) {
    case "circle":
      return Math.PI * s.radius ** 2;   // s: Circle
    case "square":
      return s.side ** 2;               // s: Square
    default: {
      const _exhaustive: never = s;     // compile error if a case is missing
      throw new Error(`Unhandled shape: ${JSON.stringify(_exhaustive)}`);
    }
  }
}

console.log(area({ kind: "square", side: 3 })); // 9
```

**Adding `interface Triangle { kind: "triangle"; ... }` to `Shape`** makes `s` in `default` of type `Triangle`, so `const _exhaustive: never = s` errors: `Type 'Triangle' is not assignable to type 'never'`. You're forced to handle it.

**Why this works with erasure:** the `kind` string is an ordinary runtime property, so `switch` can inspect it, and TypeScript uses it to narrow the types at compile time.

</details>

---

### Q36 🧠 ⭐⭐⭐ Not everything in TypeScript is erased
Which of these **emit JavaScript**, and which vanish entirely? Show the emitted code for the enum.

```ts
enum Color { Red, Green }                       // 1
interface Box { w: number }                     // 2
class P { constructor(public x: number) {} }    // 3
namespace Util { export const v = 1; }          // 4
type T = string;                               // 5
const n = <number>(5 as any);                   // 6 (the assertion)
declare const VERSION: string;                  // 7
```

<details>
<summary>✅ Show solution</summary>

| # | Emits JS? |
|---|---|
| 1 `enum` | ✅ **Yes**: an object |
| 2 `interface` | ❌ erased |
| 3 parameter property | ✅ **Yes**: `this.x = x` assignment |
| 4 `namespace` (with values) | ✅ **Yes**: an IIFE |
| 5 `type` alias | ❌ erased |
| 6 type assertion | ❌ erased (`const n = 5;`) |
| 7 `declare` | ❌ erased (it only tells TS a global exists) |

Emitted code for the enum:
```js
var Color;
(function (Color) {
    Color[Color["Red"] = 0] = "Red";
    Color[Color["Green"] = 1] = "Green";
})(Color || (Color = {}));
```
(Numeric enums create a **reverse mapping**, so `Color[0] === "Red"`.)

**Why it matters:** tools that only *strip* types (esbuild/swc in transpile-only mode, and Node's built-in type stripping) can handle **erasable syntax** but not features that need code generation (enums, namespaces with values, parameter properties). TypeScript's `"erasableSyntaxOnly": true` flag flags these. Alternatives: a union of string literals, or `const Color = { Red: 0, Green: 1 } as const`.

</details>

---

### Q37 💻 ⭐⭐⭐ Imports and erasure
```ts
// types.ts
export interface User { id: number }
export const DEFAULT_ID = 0;

// main.ts
import { User, DEFAULT_ID } from "./types";

const u: User = { id: DEFAULT_ID };
console.log(u);
```
1. What does the emitted `main.js` import (assume default settings)?
2. What if `main.ts` only imported `{ User }`? What risk does that create?
3. What is `import type` for?

<details>
<summary>✅ Show solution</summary>

1. `User` is only used as a type, so it's **elided**:
```js
import { DEFAULT_ID } from "./types";
const u = { id: DEFAULT_ID };
console.log(u);
```
2. If *only* types are imported, the **entire import statement disappears**: `./types` is never loaded, so any **side effects** in that module won't run. (Use `import "./types";` to force it.)
3. `import type { User } from "./types"` makes the intent **explicit and guaranteed erased**. It's required when per-file transpilers can't tell whether a name is a type or a value (see `isolatedModules` / `verbatimModuleSyntax`) and for re-exporting types: `export type { User } from "./types"`.

</details>

---

### Q38 💻 ⭐⭐ Generics are erased too
```ts
function create<T>(): T {
  return new T();
}
```
Why does this error? How can you fix it?

<details>
<summary>✅ Show solution</summary>

**Error:** `'T' only refers to a type, but is being used as a value here. (TS2693)`

**Explanation**
`T` is a type parameter: it **doesn't exist at runtime**, so there's nothing to call `new` on. There is no reflection over types in JS.

**Fix: pass the runtime value (the constructor) explicitly**
```ts
function create<T>(ctor: new () => T): T {
  return new ctor();
}

class Foo { hello = "hi"; }
const foo = create(Foo);   // foo: Foo
```
The same limitation explains why you can't do `x instanceof SomeInterface`, `typeof T`, or overload a function at **runtime** by parameter type. TS overloads are just multiple *signatures* over one implementation.

</details>

---

### Q39 🧠 ⭐⭐ Type-checking vs. transpiling
Many tools run TypeScript without checking it: Babel, esbuild, swc, `tsx`, `ts-node --transpile-only`, Vite, and newer Node versions that can strip types directly.
1. How can they run TS **without a type checker**?
2. What's the consequence for your workflow?

<details>
<summary>✅ Show solution</summary>

1. Because of **type erasure**: converting TS to JS is mostly a *syntactic* operation (delete annotations, interfaces, aliases, assertions). It needs no type information, so it's fast and can be done per-file.
2. **Type errors won't stop your app from running** under such tools. You must run the checker separately:
```bash
tsc --noEmit        # type-check only, emit nothing
```
Typical setup: a fast transpiler for dev/build **+** `tsc --noEmit` in your editor, pre-commit hook, and CI.

(Runtime type-stripping in Node only supports erasable syntax, so enums, namespaces with values, and parameter properties are a problem. See Q36.)

</details>

---

### Q40 💻 ⭐⭐ `any` punches a hole through the types
```ts
function double(x: number): number {
  return x * 2;
}

console.log(double("21" as any));
console.log(double("abc" as any));
console.log(double(null as any));
```
What prints, and what does it say about type safety?

<details>
<summary>✅ Show solution</summary>

**Output**
```
42
NaN
0
```

**Explanation**
After erasure this is plain JS: `double("21")` → `"21" * 2 = 42` (coercion), `"abc" * 2` → `NaN`, `null * 2` → `0`. The `: number` annotation is **not enforced at runtime**, so wrongly-typed values entering through `any`, `as`, untyped libraries, or external data behave like regular JavaScript.

**Takeaway:** TypeScript's guarantees hold only **inside the typed, checked parts** of your program. Keep `any` out, treat boundaries as untrusted (`unknown` + validation), and don't rely on annotations as runtime guards.

</details>

---

# Capstone Challenge

### Q41 💻 ⭐⭐⭐ Typed discounts that survive the real world
Convert this JavaScript to TypeScript:

```js
function applyDiscount(order, code) {
  const rates = { SAVE10: 0.1, SAVE20: 0.2 };
  const rate = rates[code];
  return order.items.reduce((s, i) => s + i.price * i.qty, 0) * (1 - rate);
}

console.log(applyDiscount({ items: [{ price: 10, qty: 2 }] }, "SAVE10")); // 18
console.log(applyDiscount({ items: [{ price: "10", qty: 2 }] }, "SAVE15")); // NaN
```

Requirements:
1. Interfaces for the order and its items; a **literal union** for valid codes.
2. Both bad calls at the bottom must become **compile errors**.
3. A discount code arrives from a **text input** (`string`) at runtime. Add a function that safely accepts that input, and explain **why the literal union alone isn't enough** (type erasure!).
4. Show roughly what the emitted JavaScript looks like.

<details>
<summary>✅ Show solution</summary>

```ts
interface OrderItem {
  price: number;
  qty: number;
}

interface Order {
  items: OrderItem[];
}

const RATES = { SAVE10: 0.1, SAVE20: 0.2 } as const;
type DiscountCode = keyof typeof RATES;          // "SAVE10" | "SAVE20"

// Runtime guard: real JS check + type predicate
function isDiscountCode(code: string): code is DiscountCode {
  return Object.hasOwn(RATES, code);             // not `code in RATES` (see note)
}

function applyDiscount(order: Order, code: DiscountCode): number {
  const subtotal = order.items.reduce((sum, { price, qty }) => sum + price * qty, 0);
  return subtotal * (1 - RATES[code]);
}

// Boundary function for untrusted input
function checkout(order: Order, userInput: string): number {
  if (!isDiscountCode(userInput)) {
    throw new Error(`Invalid code: ${userInput}`);
  }
  return applyDiscount(order, userInput);        // narrowed to DiscountCode
}

console.log(applyDiscount({ items: [{ price: 10, qty: 2 }] }, "SAVE10")); // 18
// applyDiscount({ items: [{ price: "10", qty: 2 }] }, "SAVE15");
//   ❌ price: string is not assignable to number
//   ❌ "SAVE15" is not assignable to DiscountCode
```

**Why the literal union isn't enough**
`DiscountCode` only exists at **compile time**. A string typed by a user or returned from a server is just a `string` at runtime, and `applyDiscount(order, userInput as DiscountCode)` would compile and then compute `1 - undefined = NaN`. The **type guard** (`isDiscountCode`) is real JavaScript that checks membership at runtime; the predicate is what tells the compiler about it.

**Note on `in`:** `"toString" in RATES` is `true` (inherited from `Object.prototype`), so use `Object.hasOwn` (ES2022; or `Object.prototype.hasOwnProperty.call`).

**Emitted JavaScript (approximately)**
```js
const RATES = { SAVE10: 0.1, SAVE20: 0.2 };

function isDiscountCode(code) {
    return Object.hasOwn(RATES, code);
}
function applyDiscount(order, code) {
    const subtotal = order.items.reduce((sum, { price, qty }) => sum + price * qty, 0);
    return subtotal * (1 - RATES[code]);
}
function checkout(order, userInput) {
    if (!isDiscountCode(userInput)) {
        throw new Error(`Invalid code: ${userInput}`);
    }
    return applyDiscount(order, userInput);
}
console.log(applyDiscount({ items: [{ price: 10, qty: 2 }] }, "SAVE10"));
```
`interface OrderItem`, `interface Order`, `type DiscountCode`, `as const`, every annotation, and the type predicate vanish. The `RATES` object and `isDiscountCode` logic **remain** because they're real values.

**Concepts exercised**
- Inference vs. annotation, literal types, and `as const`.
- Function parameter/return annotations and interfaces for object shapes.
- Structural checks at compile time vs. **runtime** validation at the boundary.
- Type erasure: what's checked once and gone, and what must exist as real code.

</details>

---

## Score yourself
| Score | Level |
|---|---|
| 0–12 correct | Review TypeScript basics and the compile-time/runtime split, then retry |
| 13–26 correct | Solid foundation; revisit the ⭐⭐⭐ questions on erasure and assignability |
| 27–36 correct | Strong; you understand the subtle semantics |
| 37–41 correct | Excellent; ready to teach this |
