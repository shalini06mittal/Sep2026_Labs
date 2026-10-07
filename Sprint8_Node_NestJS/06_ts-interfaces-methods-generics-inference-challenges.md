# TypeScript Challenges: Extending Interfaces, Method Signatures, Generics & Inference

Tricky, concept-focused questions mixing **theory** and **code**. Try each one yourself before opening the solution.

**How to use this file**
- 🧠 = theory / explain it  |  💻 = predict the compiler/runtime result or write code
- Difficulty: ⭐ medium · ⭐⭐ hard · ⭐⭐⭐ very tricky
- Click **"Show solution"** under each question to reveal the answer.
- Unless stated otherwise, assume `"strict": true` (this includes `strictNullChecks`, `noImplicitAny` and `strictFunctionTypes`) and **TypeScript 5.4+** (a couple of questions use `const` type parameters from 5.0 and `NoInfer` from 5.4).
- Try snippets in the [TypeScript Playground](https://www.typescriptlang.org/play). Error codes and wording can vary slightly between versions.
- `declare function ...` / `declare const ...` in snippets just means "assume this exists with this signature".

## Table of Contents
1. [Part 1: Extending Interfaces, Optional & Readonly Fields](#part-1-extending-interfaces-optional--readonly-fields)
2. [Part 2: Method Signatures in Interfaces](#part-2-method-signatures-in-interfaces)
3. [Part 3: Generics: Functions, Interfaces, Constraints](#part-3-generics-functions-interfaces-constraints)
4. [Part 4: Two Inference Behaviors That Appear With Generics](#part-4-two-inference-behaviors-that-appear-with-generics)
5. [Capstone Challenge](#capstone-challenge)

---

# Part 1: Extending Interfaces, Optional & Readonly Fields

### Q1 🧠 ⭐ How does `extends` work for interfaces?
1. What does `interface B extends A` do?
2. Can an interface extend several interfaces?
3. What can a child interface change about an inherited property, and what can't it?
4. Is this the same as class inheritance at runtime?

<details>
<summary>✅ Show solution</summary>

1. `B` gets **all members of `A`** plus its own. Anything typed `B` can be used wherever an `A` is expected (substitutability).
2. Yes: `interface C extends A, B { ... }` (comma-separated). If the same property appears in several parents, the types must be **identical**.
3. A child may **narrow** a property to a subtype (`name: string` → `name: "Rex"`), make an optional property **required**, or drop `readonly`. It may **not** make the type incompatible, or turn a required property into an optional one.
4. **No.** Interfaces are erased at compile time. `extends` on an interface produces no JavaScript and no prototype chain. (Class `extends` *does* create a runtime prototype chain.)

</details>

---

### Q2 💻 ⭐⭐ Multiple parents with conflicting members
```ts
interface A { id: number; tag: string }
interface B { id: string; extra: boolean }

interface C extends A, B {}

type D = A & B;
```
Which declaration errors? What is the type of `id` in `D`? Can you create a value of type `D`?

<details>
<summary>✅ Show solution</summary>

- `interface C extends A, B {}` ❌ `Interface 'C' cannot simultaneously extend types 'A' and 'B'. Named property 'id' of types 'A' and 'B' are not identical. (TS2320)`
- `type D = A & B` ✅ **compiles**, and `D['id']` is `number & string`, which reduces to **`never`**.
- You **can't** create a valid `D` value because no value satisfies `never`.

**Lesson:** `extends` reports conflicts immediately; intersections hide them until you try to use the type. If both parents declare `id: number`, either approach works fine.

</details>

---

### Q3 💻 ⭐⭐ Optional ↔ required in a child interface
```ts
interface Base {
  a?: number;
  b: number;
}

interface Child1 extends Base { a: number }
interface Child2 extends Base { b?: number }
```
Which one errors, and why is the other allowed?

<details>
<summary>✅ Show solution</summary>

- `Child1` ✅ making an optional property **required** is a narrowing; every `Child1` still satisfies `Base`.
- `Child2` ❌ `Property 'b' is optional in type 'Child2' but required in type 'Base'. (TS2430)`. A `Child2` might lack `b`, so it can't stand in for a `Base`.

</details>

---

### Q4 💻 ⭐⭐ `x?: number` vs. `x: number | undefined`
```ts
interface A { x?: number }
interface B { x: number | undefined }

const a1: A = {};
const a2: A = { x: undefined };
const b1: B = {};
const b2: B = { x: undefined };
```
1. Which lines error?
2. What changes if `"exactOptionalPropertyTypes": true`?

<details>
<summary>✅ Show solution</summary>

1. Only `b1` ❌: `Property 'x' is missing in type '{}' but required in type 'B'.` A property typed `number | undefined` is still **required**; you must write it.
2. With `exactOptionalPropertyTypes`, `a2` also errors: an optional property means "**may be absent**", *not* "may be present with the value `undefined`". To allow both, write `x?: number | undefined`.

**Why it matters:** `"x" in obj`, `Object.keys`, and `JSON.stringify` all distinguish a missing key from a key holding `undefined`.

</details>

---

### Q5 💻 ⭐⭐ `readonly` is shallow
```ts
interface Config {
  readonly db: { host: string };
  readonly list: number[];
}

declare const c: Config;

c.db = { host: "x" };
c.db.host = "y";
c.list.push(1);
```
Which lines error? How do you make `list` truly read-only at the type level?

<details>
<summary>✅ Show solution</summary>

- `c.db = ...` ❌ `Cannot assign to 'db' because it is a read-only property. (TS2540)`
- `c.db.host = "y"` ✅ compiles. `readonly` protects the **property**, not the object it points to.
- `c.list.push(1)` ✅ compiles. The array is still mutable.

**Fix:**
```ts
interface Config {
  readonly db: Readonly<{ host: string }>;
  readonly list: readonly number[];     // or ReadonlyArray<number>
}
```
`Readonly<T>` is also only one level deep. Remember all of this is **compile-time only** (erased); use `Object.freeze` for runtime protection.

</details>

---

### Q6 💻 ⭐⭐⭐ `readonly` doesn't affect assignability
```ts
interface RO { readonly x: number }
interface RW { x: number }

const ro: RO = { x: 1 };
const rw: RW = ro;

rw.x = 2;
console.log(ro.x);
```
Does it compile? What prints?

<details>
<summary>✅ Show solution</summary>

**Compiles ✅ and prints `2`.**

**Explanation**
`readonly` is **not** part of type compatibility in TypeScript. A read-only property is assignable to a mutable one, so `rw` happily aliases the same object and mutates it. This is a known, deliberate unsoundness. Once a value is referenced through a mutable type, `readonly` can't protect it.

**Takeaway:** treat `readonly` as a **hint for API consumers** and linting, not a guarantee. Real immutability needs runtime enforcement (`Object.freeze`) or defensive copying.

</details>

---

### Q7 💻 ⭐⭐ What can an interface extend?
```ts
type Obj = { a: 1 };
type Union = { a: 1 } | { b: 2 };

interface I1 extends Obj {}
interface I2 extends Union {}
```
Which errors? How do you add a field to `Union` anyway?

<details>
<summary>✅ Show solution</summary>

- `I1` ✅ fine: an object type alias can be extended.
- `I2` ❌ `An interface can only extend an object type or intersection of object types with statically known members. (TS2312)`. A union isn't a single shape.

**Fix:** use an intersection in a type alias (it distributes over the union):
```ts
type Extended = Union & { c: 3 };   // ({a:1} & {c:3}) | ({b:2} & {c:3})
```

</details>

---

### Q8 💻 ⭐⭐ Coding: model a hierarchy and predict the errors
```ts
interface Entity { readonly id: string; createdAt: Date }
interface User extends Entity { name: string; email?: string }
interface Admin extends User { permissions: string[] }

const admin: Admin = { id: "a1", createdAt: new Date(), name: "Root", permissions: ["all"] };

admin.id = "a2";                                   // (1)
admin.email.length;                                // (2)
const u: User = admin;                             // (3)
const a2: Admin = u;                               // (4)
const copy: User = { ...admin, name: "Copy" };     // (5)

function rename(user: User, name: string): User {
  return { ...user, name };                        // (6)
}
```
Which of (1)–(6) error?

<details>
<summary>✅ Show solution</summary>

1. ❌ `id` is read-only (inherited from `Entity`).
2. ❌ `'admin.email' is possibly 'undefined'.` The property is optional in `User`, so it stays `string | undefined`.
3. ✅ An `Admin` is a `User` (upcast).
4. ❌ `Property 'permissions' is missing in type 'User'...` (downcast isn't automatic).
5. ✅ Spread members aren't excess-checked, so the extra `permissions` is fine. `copy` still **holds `permissions` at runtime** (types don't strip data).
6. ✅ The spread keeps `id`, `createdAt`, `email`; `rename` creates a **new object**, which is the idiomatic way to "change" read-only data.

</details>

---

# Part 2: Method Signatures in Interfaces

### Q9 💻 ⭐⭐⭐ Method syntax vs. property syntax
```ts
interface Animal { name: string }
interface Dog extends Animal { bark(): void }

interface Method { handle(x: Animal): void }
interface Prop   { handle: (x: Animal) => void }

const dogFn = (d: Dog) => d.bark();

const m: Method = { handle: dogFn };
const p: Prop   = { handle: dogFn };

m.handle({ name: "cat" });
```
1. Which assignments compile (with `strict`)?
2. What happens on the last line at runtime?
3. Why do the two syntaxes differ?

<details>
<summary>✅ Show solution</summary>

1. `m` ✅ compiles; `p` ❌ `Type '(d: Dog) => void' is not assignable to type '(x: Animal) => void'. Types of parameters 'd' and 'x' are incompatible.`
2. `m.handle({ name: "cat" })` compiles but crashes: **`TypeError: d.bark is not a function`**.
3. Under `strictFunctionTypes`, **function-type properties** (`handle: (x) => void`) are checked **contravariantly**: the safe rule. **Method-signature** declarations (`handle(x): void`) are deliberately exempt and stay **bivariant** (parameters may be assigned in either direction). That keeps common patterns like `Array<T>` (where `Array<Dog>` should be usable as `Array<Animal>`) working, at the cost of soundness.

**Guideline:** use **property syntax** (`handle: (x: T) => void`) when you want strict parameter checking; method syntax when you want the looser, ergonomic behavior.

</details>

---

### Q10 💻 ⭐⭐ Optional methods
```ts
interface Plugin {
  name: string;
  init?(): void;
}

function start(p: Plugin) {
  p.init();
  p.init?.();
}
```
Which line errors? What happens at runtime if you "fix" it with `p.init!()` for a plugin without `init`?

<details>
<summary>✅ Show solution</summary>

- `p.init()` ❌ `Cannot invoke an object which is possibly 'undefined'. (TS2722)`
- `p.init?.()` ✅ calls only if present.

With `p.init!()`, the non-null assertion silences the compiler (it's **erased**), so for a plugin without `init` you get `TypeError: p.init is not a function` at runtime. Prefer `?.()` or an explicit `if (p.init) p.init();`.

</details>

---

### Q11 💻 ⭐⭐⭐ The `this` parameter
```ts
interface Counter {
  count: number;
  inc(this: Counter): void;
}

const c: Counter = {
  count: 0,
  inc() { this.count++; },
};

c.inc();
const f = c.inc;
f();
```
1. What's the compile error and on which line?
2. What would happen at runtime if the compiler allowed it?
3. Does `this: Counter` appear in the emitted JavaScript?

<details>
<summary>✅ Show solution</summary>

1. `f()` ❌ `The 'this' context of type 'void' is not assignable to method's 'this' of type 'Counter'. (TS2684)`. Detaching a method loses its receiver; the `this` parameter lets TypeScript catch this.
2. In strict mode/ES modules, `this` would be `undefined` → `TypeError: Cannot read properties of undefined (reading 'count')`. (In sloppy mode, it would silently touch `globalThis.count`.)
3. **No**: a `this` parameter is a *fake* parameter, erased entirely. The emitted method is just `inc() { this.count++; }`.

**Fixes:** `c.inc()`, `const f = c.inc.bind(c)`, or `() => c.inc()`.

</details>

---

### Q12 💻 ⭐⭐⭐ Overloaded method signatures
```ts
interface Parser {
  parse(input: string): number;
  parse(input: string[]): number[];
}

const p: Parser = {
  parse(input: string | string[]) {
    return Array.isArray(input) ? input.map(Number) : Number(input);
  },
};
```
Does it compile? If not, fix it.

<details>
<summary>✅ Show solution</summary>

**It doesn't compile.** The implementation's return type is `number | number[]`, which isn't assignable to `number` (overload 1) or to `number[]` (overload 2). Each overload must be satisfied on its own.

**Fix: declare a real overloaded function, then assign it**
```ts
function parse(input: string): number;
function parse(input: string[]): number[];
function parse(input: string | string[]): number | number[] {
  return Array.isArray(input) ? input.map(Number) : Number(input);
}

const p: Parser = { parse };      // ✅ matches both overloads
```
(Alternatives: a generic/conditional-type signature, or a type assertion `as Parser`, trading safety for convenience.)

**Erasure note:** overloads are just multiple *signatures* over **one** implementation. The compiled JavaScript contains only the implementation function.

</details>

---

### Q13 💻 ⭐⭐ Callable ("hybrid") interfaces
```ts
interface Formatter {
  (value: number): string;
  locale: string;
}

const f: Formatter = (v) => v.toFixed(2);
```
1. What's the error?
2. Implement a valid `Formatter`.
3. How would you describe a *constructor* with an interface?

<details>
<summary>✅ Show solution</summary>

1. ❌ `Property 'locale' is missing in type '(v: number) => string' but required in type 'Formatter'. (TS2741)`. A **call signature** `(value: number): string` makes the type callable, but any extra members must exist too. (Functions are objects in JS, so they can carry properties.)
2. ```ts
   const f: Formatter = Object.assign(
     (value: number) => value.toFixed(2),
     { locale: "en-US" }
   );
   f(3.14159);   // "3.14"
   f.locale;     // "en-US"
   ```
3. A **construct signature**:
   ```ts
   interface PointCtor { new (x: number, y: number): { x: number; y: number } }
   ```

</details>

---

### Q14 💻 ⭐⭐ Returning `this` from interface methods
```ts
interface Builder {
  set(key: string, value: string): this;
  build(): string;
}
interface HtmlBuilder extends Builder {
  tag(name: string): this;
}

declare const b: HtmlBuilder;

const r = b.set("a", "1").tag("div").set("b", "2");
```
1. What's the type of `r`?
2. What breaks if `set` is declared as returning `Builder` instead of `this`?

<details>
<summary>✅ Show solution</summary>

1. **`HtmlBuilder`**. The special `this` type is **polymorphic**: it means "the type of whatever object the method was called on", so chaining preserves the most specific type.
2. With `set(...): Builder`, the result of `.set()` is just a `Builder`, so `.tag("div")` fails: `Property 'tag' does not exist on type 'Builder'`.

`this` return types are the standard way to type **fluent/chainable APIs** and work through `extends` chains and class `implements`.

</details>

---

# Part 3: Generics: Functions, Interfaces, Constraints

### Q15 🧠 ⭐ Why generics?
1. What problem do generics solve that `any` and function overloads don't?
2. Explain *type parameter* vs. *type argument*.
3. Do generics exist at runtime?

<details>
<summary>✅ Show solution</summary>

1. Generics **link types together** across parameters and return values without giving up type information.
   ```ts
   function idAny(x: any): any { return x; }      // loses the link: result is any
   function id<T>(x: T): T { return x; }          // result type = argument type
   ```
   Overloads would need one signature per type and can't cover "any type" cleanly.
2. In `function id<T>(x: T): T`, `T` is the **type parameter** (a placeholder). In `id<string>("a")`, `string` is the **type argument** (the value for the placeholder), explicit or inferred.
3. **No.** Type parameters and arguments are erased like all types. You can't write `new T()`, `T.name`, or `x instanceof T`; if you need a runtime value, pass it in (e.g. a constructor).

</details>

---

### Q16 💻 ⭐ Generic function basics
```ts
function first<T>(arr: T[]): T | undefined {
  return arr[0];
}

const a = first([1, 2, 3]);
const b = first(["x", 1]);
const c = first<string>([1]);
```
What are the types of `a` and `b`? What does `c` do?

<details>
<summary>✅ Show solution</summary>

- `a`: **`number | undefined`** (T inferred as `number`).
- `b`: **`string | number | undefined`**. The array literal's element type is `string | number`, so that's `T`.
- `c` ❌ `Type 'number' is not assignable to type 'string'`: you explicitly set `T = string`, so inference is skipped and the argument is checked against it.

</details>

---

### Q17 💻 ⭐⭐ Constraints with `extends`
```ts
function longest<T extends { length: number }>(a: T, b: T): T {
  return a.length >= b.length ? a : b;
}

longest("abc", "de");
longest([1, 2], [3]);
longest(10, 20);
longest("abc", [1, 2]);
```
Which calls compile? Why does the last one fail even though both arguments have `length`?

<details>
<summary>✅ Show solution</summary>

- `longest("abc", "de")` ✅ → `"abc"` at runtime.
- `longest([1, 2], [3])` ✅ → `[1, 2]`.
- `longest(10, 20)` ❌ `Argument of type 'number' is not assignable to parameter of type '{ length: number; }'`: the constraint isn't met.
- `longest("abc", [1, 2])` ❌ both satisfy the constraint, but **both parameters share the same `T`**. TypeScript infers `T = string` from the first argument, and `number[]` isn't a `string`. It doesn't invent a union (see Q28).

**Fixes if you want to mix:** use two type parameters (`<A extends {length:number}, B extends {length:number}>(a: A, b: B): A | B`) or pass `longest<string | number[]>(...)`.

</details>

---

### Q18 💻 ⭐⭐ What can you do with an unconstrained `T`?
```ts
function len<T>(x: T): number {
  return x.length;
}
```
Why does this fail, and what are two ways to make it work?

<details>
<summary>✅ Show solution</summary>

**Error:** `Property 'length' does not exist on type 'T'. (TS2339)`. An unconstrained `T` could be *anything* (`number`, `null`, ...), so the compiler only lets you do what's valid for **every** type.

**Fix 1: constrain it**
```ts
function len<T extends { length: number }>(x: T): number {
  return x.length;
}
```
**Fix 2: narrow at runtime**
```ts
function len<T>(x: T): number {
  return typeof x === "string" || Array.isArray(x) ? x.length : 0;
}
```
(Often you don't need generics here at all: `(x: { length: number }) => number` is simpler.)

</details>

---

### Q19 💻 ⭐⭐⭐ `K extends keyof T`
```ts
interface User { id: number; name: string }

function getProp<T, K extends keyof T>(obj: T, key: K): T[K] {
  return obj[key];
}

function getPropLoose<T>(obj: T, key: keyof T): T[keyof T] {
  return obj[key];
}

declare const u: User;

const a = getProp(u, "name");
const b = getProp(u, "id");
const c = getProp(u, "email");
const d = getPropLoose(u, "name");
```
What are the types of `a`, `b`, `d`, and what happens with `c`?

<details>
<summary>✅ Show solution</summary>

- `a`: **`string`**; `b`: **`number`**.
- `c` ❌ `Argument of type '"email"' is not assignable to parameter of type '"id" | "name"'`.
- `d`: **`string | number`**. With `key: keyof T`, TypeScript knows only that the key is *some* key, not *which* one, so the result is the union of all property types.

**Why `getProp` is precise:** `K` is inferred as the **literal type** `"name"` (because its constraint, `keyof T`, is a union of string literals), and `T[K]` then resolves to `User["name"]`, i.e. `string`. This "capture the exact key in a type parameter" pattern is the core of type-safe property access.

Note: `keyof` includes `readonly` properties, so `getProp` can read them but nothing here restricts writes.

</details>

---

### Q20 💻 ⭐⭐⭐ Type parameter on the interface vs. on the method
```ts
interface A<T> { f(x: T): T }
interface B     { f<T>(x: T): T }

const a: A<string> = { f: (x) => x.toUpperCase() };
const b1: B = { f: (x) => x.toUpperCase() };
const b2: B = { f: (x) => x };
const b3: B = { f: (x: string) => x };

a.f(1);
b2.f(1);
```
Which lines error? Explain the difference in meaning.

<details>
<summary>✅ Show solution</summary>

- `a` ✅ `T` is fixed to `string` when you write `A<string>`, so `x: string`.
- `b1` ❌ `Property 'toUpperCase' does not exist on type 'T'`. In `B`, the method itself is generic: the implementation must work for **every** `T`, so `x` is an unconstrained `T`.
- `b2` ✅ `x => x` works for any `T`.
- `b3` ❌ `Type 'T' is not assignable to type 'string'`. The implementation only handles `string`, but `B.f` promises *any* `T`.
- `a.f(1)` ❌ (`T` is `string`); `b2.f(1)` ✅ returns `number` (T chosen **per call**).

**Rule:** `interface A<T>`: **the user of the interface** picks `T` once. `f<T>(...)` inside: **each caller** picks `T` on every call, and the implementer must support all of them.

</details>

---

### Q21 💻 ⭐⭐ Generic interfaces with defaults
```ts
interface ApiResponse<T = unknown> {
  data: T;
  status: number;
}

const r1: ApiResponse = { data: 1, status: 200 };
r1.data.toFixed(2);

const r2: ApiResponse<string[]> = { data: ["a"], status: 200 };
const r3: ApiResponse<> = { data: 1, status: 200 };
let list: Array = [];
```
Which lines error? Why?

<details>
<summary>✅ Show solution</summary>

- `r1.data.toFixed(2)` ❌ `'r1.data' is of type 'unknown'`. With the default `T = unknown`, `ApiResponse` means `ApiResponse<unknown>`; the initializer doesn't narrow the declared type.
- `r2` ✅
- `r3` ❌ `Type argument list cannot be empty. (TS1099)`. Omit the angle brackets to use the default.
- `let list: Array` ❌ `Generic type 'Array<T>' requires 1 type argument(s). (TS2314)`: no default exists, so you must supply one.

**Design tip:** choose defaults deliberately. `unknown` forces callers to narrow; `any` would silently disable checks.

</details>

---

### Q22 💻 ⭐⭐⭐ Generic containers and variance
```ts
interface Animal { name: string }
interface Dog extends Animal { bark(): void }

const dogs: Dog[] = [{ name: "Rex", bark() {} }];
const animals: Animal[] = dogs;
animals.push({ name: "Tom" });
dogs[1].bark();
```
1. Which lines error at compile time?
2. What happens at runtime?
3. How do you make the aliasing safe?

<details>
<summary>✅ Show solution</summary>

1. **None.** `Dog[]` is assignable to `Animal[]` (arrays are treated **covariantly**), and `push` is a *method*, so its parameter is checked bivariantly (see Q9).
2. `dogs[1]` is `{ name: "Tom" }` (no `bark`) → **`TypeError: dogs[1].bark is not a function`**.
3. Alias through a read-only view:
   ```ts
   const animals: readonly Animal[] = dogs;   // push no longer exists on this type
   // animals.push(...)  ❌ Property 'push' does not exist on type 'readonly Animal[]'
   ```
   Read-only containers are safely covariant; mutable containers are not.

**General principle:** a generic type that only *produces* `T` (reads) is safely covariant; one that only *consumes* `T` (writes via function-type properties) is contravariant; both → invariant.

</details>

---

### Q23 💻 ⭐⭐⭐ Coding: `merge` with constraints
Write `merge(a, b)` that returns an object containing the properties of both, typed so the result has the properties of both inputs. Only objects should be accepted.

```ts
const m = merge({ a: 1 }, { b: "x" });
m.a;   // number
m.b;   // string
merge(1, 2);   // should be an error
```
What goes wrong with `merge({ a: 1 }, { a: "x" })`?

<details>
<summary>✅ Show solution</summary>

```ts
function merge<T extends object, U extends object>(a: T, b: U): T & U {
  return { ...a, ...b };
}
```
- Two type parameters allow different shapes; `extends object` rejects primitives (`merge(1, 2)` ❌ `Argument of type 'number' is not assignable to parameter of type 'object'`).
- Spreading two generic objects produces the intersection `T & U`, matching the return type.

**The catch:** `merge({ a: 1 }, { a: "x" })` compiles. The claimed type is `{ a: number } & { a: string }`, so `m.a` has type **`never`**, but at runtime the later spread wins and `m.a === "x"`. The intersection type doesn't model "later keys override earlier keys". For precise typing you'd use something like `Omit<T, keyof U> & U`.

</details>

---

### Q24 💻 ⭐⭐ Coding: a generic `Stack<T>`
Implement `createStack<T>()` for this interface and predict the types below.

```ts
interface Stack<T> {
  push(item: T): void;
  pop(): T | undefined;
  peek(): T | undefined;
  readonly size: number;
}

const s = createStack<number>();
s.push(1);
s.push("a");
s.size = 3;

const t = createStack();
t.push("anything");
const v = t.pop();
```

<details>
<summary>✅ Show solution</summary>

```ts
function createStack<T>(): Stack<T> {
  const items: T[] = [];
  return {
    push(item) { items.push(item); },
    pop() { return items.pop(); },
    peek() { return items[items.length - 1]; },
    get size() { return items.length; },     // getter satisfies a readonly property
  };
}
```
Predictions:
- `s.push("a")` ❌ `Argument of type 'string' is not assignable to parameter of type 'number'`.
- `s.size = 3` ❌ `Cannot assign to 'size' because it is a read-only property`. (A getter-only property also throws in strict mode at runtime.)
- `createStack()` with **no type argument and no context** has nothing to infer `T` from, so `T` falls back to **`unknown`**: `t` is `Stack<unknown>`; `t.push("anything")` ✅; `v` is **`unknown`**. (See Q33.)

</details>

---

### Q25 🧠 ⭐⭐ Generic arrow functions in `.tsx` files
```tsx
// Component.tsx
const identity = <T>(x: T) => x;
```
Why does this fail in a `.tsx` file, and how do you fix it?

<details>
<summary>✅ Show solution</summary>

In `.tsx`, `<T>` is parsed as the start of a **JSX element**, not a type-parameter list, giving errors like `JSX element 'T' has no corresponding closing tag`.

**Fixes**
```tsx
const identity = <T,>(x: T) => x;                  // trailing comma disambiguates
const identity2 = <T extends unknown>(x: T) => x;  // explicit constraint also works
function identity3<T>(x: T): T { return x; }       // function declarations are unaffected
```
(The same code is fine in plain `.ts` files.)

</details>

---

# Part 4: Two Inference Behaviors That Appear With Generics

> **Note on scope:** the topic says "two inference behaviors that only appear once generics are involved". This section covers the two core ones:
> **(A) Arguments → type parameters:** `T` is inferred from the arguments you pass (candidate selection, literal preservation vs. widening, constraints).
> **(B) Expected type → type parameters:** `T` is inferred from the *context* of the call (the return/assigned type), with defaults and `unknown` as fallbacks.
> If your course defines the two slightly differently, the underlying mechanics are the same.

### Q26 🧠 ⭐⭐ Where do type arguments come from?
For a call like `f(...)` to a generic function, list the places TypeScript can infer `T` from, and what happens when it finds **nothing** or **conflicting** information.

<details>
<summary>✅ Show solution</summary>

**Sources (roughly in priority order)**
1. **Explicit type arguments:** `f<string>(...)`; no inference happens for those.
2. **Arguments** (behavior A): each argument contributes *candidates* for the type parameters it mentions, including through callback parameter/return types.
3. **Contextual (expected) type** (behavior B): when the call result is assigned to something with a known type, e.g. `const x: string[] = f()`, the return type provides lower-priority candidates.
4. **Defaults:** `<T = string>` when there are no candidates.
5. **Constraint / `unknown`:** with no candidates and no default, `T` becomes its constraint (or `unknown`).

**Conflicts:** with several candidates for the same `T`, TypeScript picks the **best common supertype among the candidates**; if none of them is a supertype of the rest, it takes the first and reports an error on the others (it does **not** build a union for you; see Q27/Q28).

**Nothing found:** `unknown` (or default/constraint), never `any`.

</details>

---

### Q27 💻 ⭐⭐ Competing candidates
```ts
function pair<T>(a: T, b: T): [T, T] {
  return [a, b];
}

const p1 = pair(1, 2);
const p2 = pair(1, "a");
const p3 = pair<number | string>(1, "a");
```
What are the types of `p1` and `p3`? What happens with `p2` and why?

<details>
<summary>✅ Show solution</summary>

- `p1`: **`[number, number]`**: candidates `1` and `2` widen to `number`.
- `p2` ❌ `Argument of type 'string' is not assignable to parameter of type 'number'`. Candidates are `number` (from `1`) and `string` (from `"a"`); neither is a supertype of the other, so TS settles on the first and complains about the second.
- `p3`: **`[string | number, string | number]`**: explicit type argument bypasses inference.

</details>

---

### Q28 💻 ⭐⭐⭐ Best common supertype
```ts
interface Animal { name: string }
interface Dog extends Animal { bark(): void }
interface Cat extends Animal { meow(): void }

declare const animal: Animal;
declare const dog: Dog;
declare const cat: Cat;

const r1 = pair(dog, animal);
const r2 = pair(animal, dog);
const r3 = pair(dog, cat);
```
(`pair` is from Q27.) What are the types of `r1`, `r2`? What happens with `r3`, and how do you fix it?

<details>
<summary>✅ Show solution</summary>

- `r1` and `r2`: **`[Animal, Animal]`** regardless of order. `Dog` is assignable to `Animal`, so `Animal` is the best common supertype of the candidates.
- `r3` ❌ `Argument of type 'Cat' is not assignable to parameter of type 'Dog'`. `Dog` and `Cat` are siblings: neither is a supertype of the other, and TypeScript **won't compute their union or common parent**.

**Fixes**
```ts
const ok1 = pair<Animal>(dog, cat);          // explicit type argument
const ok2 = pair<Dog | Cat>(dog, cat);       // or a union
const ok3 = pair(dog as Animal, cat);        // or widen one argument
```

</details>

---

### Q29 💻 ⭐⭐⭐ Literal preserved or widened?
```ts
declare function id<T>(x: T): T;
declare function box<T>(x: T): { value: T };
declare function boxS<T extends string>(x: T): { value: T };

const a = id("hi");
let   b = id("hi");
const c = box("hi");
const d = boxS("hi");
```
What is the type of each of `a`, `b`, `c`, `d`?

<details>
<summary>✅ Show solution</summary>

| Variable | Type | Why |
|---|---|---|
| `a` | `"hi"` | `T` appears at the **top level** of the return type, so the literal candidate isn't widened; `const` keeps it |
| `b` | `string` | same inference (`"hi"`), but a mutable `let` declaration **widens** the literal |
| `c` | `{ value: string }` | `T` is only *nested* in the return type and has no constraint → the literal candidate is **widened** to `string` |
| `d` | `{ value: "hi" }` | `T extends string`: a constraint that includes a **primitive** tells TS "keep literal types" → **not widened** |

**Rule of thumb (simplified):** an inferred literal is widened to its base type unless (a) the type parameter has a **primitive/literal constraint**, or (b) `T` is returned at the **top level**. Practical consequence: add `extends string` (or `extends string | number`) when you want to *capture* the exact literal, e.g. event names or keys. It's also why `Promise.resolve("a")` gives `Promise<string>` rather than `Promise<"a">`.

</details>

---

### Q30 💻 ⭐⭐⭐ `const` type parameters (TS 5.0+)
```ts
declare function f1<T>(x: T): T;
declare function f2<const T>(x: T): T;
declare function f3<const T extends string[]>(x: T): T;
declare function f4<const T extends readonly string[]>(x: T): T;

const a = f1({ mode: "dark", sizes: [1, 2] });
const b = f2({ mode: "dark", sizes: [1, 2] });
const c = f3(["x", "y"]);
const d = f4(["x", "y"]);
```
What are the types of `a`–`d`? Why does `c` behave differently from `d`?

<details>
<summary>✅ Show solution</summary>

- `a`: `{ mode: string; sizes: number[] }`: normal widening of object literal properties.
- `b`: `{ readonly mode: "dark"; readonly sizes: readonly [1, 2] }`: `const T` infers as if the argument were written with `as const`.
- `c`: **`string[]`**: surprise! `const T` would infer `readonly ["x", "y"]`, but that isn't assignable to the **mutable** constraint `string[]`, so TS falls back to the constraint.
- `d`: **`readonly ["x", "y"]`**: the `readonly` constraint accepts the readonly tuple.

**Lesson:** when using `const T` with array constraints, make the constraint `readonly`.

</details>

---

### Q31 💻 ⭐⭐ Inference through callbacks
```ts
function map<T, U>(arr: T[], fn: (item: T) => U): U[] {
  return arr.map(fn);
}

const lens  = map(["a", "bb"], (s) => s.length);
const upper = map([1, 2], (n) => n.toString());
const bad   = map(["a", "bb"], (s: number) => s * 2);
```
Explain in order how `T` and `U` get inferred for `lens`. What are the types of `lens` and `upper`? Why does `bad` fail?

<details>
<summary>✅ Show solution</summary>

**How `lens` is inferred**
1. `T` is inferred from the first argument: `string[]` → `T = string`.
2. The arrow function `(s) => s.length` is *context-sensitive* (its parameter has no annotation), so TS postpones it until `T` is known, then **contextually types** `s: string`.
3. The arrow's return type `number` becomes the candidate for `U` → `U = number`.

So `lens: number[]` and `upper: string[]`.

`bad` ❌ `T` is already `string` (from the array), but the callback declares `s: number`: `Argument of type '(s: number) => number' is not assignable to parameter of type '(item: string) => number'`.

**Point:** inference **flows** from non-function arguments into callbacks' parameters, and from callbacks' return values into other type parameters.

</details>

---

### Q32 💻 ⭐⭐⭐ Explicit type arguments: all or nothing
```ts
declare function convert<From, To>(x: From): To;
declare function convert2<From, To = string>(x: From): To;

const r1 = convert<string>("a");
const r2 = convert2<number>(1);
const r3 = convert2(1);
const r4 = convert(1);
```
Which line errors? What are the types of `r2`, `r3`, `r4`?

<details>
<summary>✅ Show solution</summary>

- `r1` ❌ `Expected 2 type arguments, but got 1. (TS2558)`. TypeScript has **no partial inference**: if you write *any* explicit type arguments you must supply all that lack defaults, and **nothing** is inferred for the rest.
- `r2`: **`string`**. `To` isn't inferred; it takes its **default** `string`.
- `r3`: **`string`**. `From = number` is inferred from the argument, and `To` has no candidates so it uses the default.
- `r4`: **`unknown`**. `To` has no candidates, default, or constraint.

**Workarounds for "specify one, infer the rest":** currying (`convert<string>()(x)`), or restructuring so only the non-inferable parameter is explicit.

</details>

---

### Q33 💻 ⭐⭐ Inference from the expected type (behavior B)
```ts
function emptyList<T>(): T[] {
  return [];
}

const a = emptyList();
const b: string[] = emptyList();
const c = emptyList<number>();

const p1 = new Promise((resolve) => resolve(5));
const p2: Promise<number> = new Promise((resolve) => resolve(5));
const p3 = new Promise<number>((resolve) => resolve(5));
```
What are the types of `a`, `b`, `c`, `p1`, `p2`, `p3`? Why can `T` be inferred in `b` even though no argument mentions it?

<details>
<summary>✅ Show solution</summary>

| Variable | Type | Reason |
|---|---|---|
| `a` | `unknown[]` | no arguments, no context → `T` falls back to `unknown` |
| `b` | `string[]` | **return-type inference**: the declared type `string[]` is matched against `T[]`, so `T = string` |
| `c` | `number[]` | explicit type argument |
| `p1` | `Promise<unknown>` | TS **doesn't infer from `resolve(5)`** calls inside the executor; there's no other candidate |
| `p2` | `Promise<number>` | `T` comes from the annotation (expected type) |
| `p3` | `Promise<number>` | explicit |

**Behavior B** is "use the *expected* type of the call expression as a (lower-priority) source of candidates." It's why `const s: Set<string> = new Set()` works without writing `new Set<string>()`, and why you often see annotations on the *variable* rather than the call.

</details>

---

### Q34 💻 ⭐⭐⭐ Stopping an inference site: `NoInfer` (TS 5.4+)
```ts
function createLight<C extends string>(colors: C[], defaultColor?: C) {}

createLight(["red", "yellow", "green"], "red");     // (1)
createLight(["red", "yellow", "green"], "blue");    // (2)
```
1. Does (2) error? Why or why not?
2. How do you make it error?

<details>
<summary>✅ Show solution</summary>

1. **No error.** Both arguments are inference sites for `C`, so the candidates `"red" | "yellow" | "green"` and `"blue"` combine into `C = "red" | "yellow" | "green" | "blue"`. The "invalid" default silently *widens* the type parameter instead of being rejected (all thanks to the `extends string` literal-preserving behavior from Q29).
2. Tell TypeScript that `defaultColor` must **not contribute** candidates:
   ```ts
   function createLight<C extends string>(colors: C[], defaultColor?: NoInfer<C>) {}

   createLight(["red", "yellow", "green"], "blue");
   // ❌ Argument of type '"blue"' is not assignable to parameter of type '"red" | "yellow" | "green" | undefined'
   ```
   `C` is now inferred **only** from `colors`; `defaultColor` is merely *checked* against it.

(Before 5.4 people used tricks like `defaultColor?: C & {}` or a second type parameter `D extends C`.)

</details>

---

### Q35 💻 ⭐⭐⭐ Coding: type-safe `indexBy`
Write `indexBy(items, key)` that returns a `Map` from the value of `key` to the item, so that:

```ts
interface User { id: number; name: string }
declare const users: User[];

const byId   = indexBy(users, "id");     // Map<number, User>
const byName = indexBy(users, "name");   // Map<string, User>
indexBy(users, "age");                   // ❌ error
```
Explain which inference behaviors are at work, and what would change if the parameter were `key: keyof T`.

<details>
<summary>✅ Show solution</summary>

```ts
function indexBy<T, K extends keyof T>(items: T[], key: K): Map<T[K], T> {
  const map = new Map<T[K], T>();
  for (const item of items) {
    map.set(item[key], item);
  }
  return map;
}
```
**Inference at work**
1. `T = User` is inferred from `users` (behavior A, from the argument).
2. `K` is inferred from the string literal `"id"`; because `K extends keyof T` (a union of literals), TypeScript **keeps the literal** `"id"` rather than widening to `string` (Q29).
3. The return type `Map<T[K], T>` resolves to `Map<User["id"], User>` → `Map<number, User>`.
4. `"age"` isn't in `"id" | "name"` → compile error.

**With `key: keyof T`:** `K` disappears; the result would have to be `Map<T[keyof T], T>` = `Map<string | number, User>` for *every* call, losing precision.

**Runtime note:** types are erased, so duplicate keys still overwrite earlier items; the type doesn't prevent that.

</details>

---

# Capstone Challenge

### Q36 💻 ⭐⭐⭐ A typed in-memory repository
Using extending interfaces, `readonly`/optional fields, method signatures, generics with constraints, and inference, build:

```ts
interface Entity {
  readonly id: string;
  createdAt: Date;
}
interface User extends Entity { name: string; email?: string }
interface Product extends Entity { title: string; price: number }
```

1. A generic interface `Repository<T extends Entity>` with:
   - `readonly size: number`
   - `add(item: T): void`
   - `get(id: string): T | undefined`
   - `find(predicate: (item: T) => boolean): T[]`
   - `update<K extends keyof T>(id: string, key: K, value: T[K]): T`, a **generic method** that sets one field and returns the updated item (throw if the id doesn't exist)
2. `createRepository<T extends Entity>(): Repository<T>`.
3. Show which of these should (not) compile and why:

```ts
const users = createRepository<User>();
users.add({ id: "u1", createdAt: new Date(), name: "Ada" });
users.update("u1", "name", "Ada L.");     // (a)
users.update("u1", "name", 42);           // (b)
users.update("u1", "title", "x");         // (c)
users.size = 5;                           // (d)
createRepository<string>();               // (e)
const withEmail = users.find((u) => u.email !== undefined);   // (f)
```
4. Spot the **design weakness** related to `readonly`.

<details>
<summary>✅ Show solution</summary>

```ts
interface Entity {
  readonly id: string;
  createdAt: Date;
}
interface User extends Entity { name: string; email?: string }
interface Product extends Entity { title: string; price: number }

interface Repository<T extends Entity> {
  readonly size: number;
  add(item: T): void;
  get(id: string): T | undefined;
  find(predicate: (item: T) => boolean): T[];
  update<K extends keyof T>(id: string, key: K, value: T[K]): T;
}

function createRepository<T extends Entity>(): Repository<T> {
  const items = new Map<string, T>();

  return {
    get size() {
      return items.size;
    },
    add(item) {
      items.set(item.id, item);
    },
    get(id) {
      return items.get(id);
    },
    find(predicate) {
      return [...items.values()].filter(predicate);
    },
    update(id, key, value) {
      const item = items.get(id);
      if (!item) throw new Error(`Not found: ${id}`);
      item[key] = value;
      return item;
    },
  };
}
```

**Results**
- (a) ✅ `K` inferred as `"name"`; `value` must be `User["name"]` = `string`.
- (b) ❌ `Argument of type 'number' is not assignable to parameter of type 'string'`. The literal key *captures* the exact value type (Q19).
- (c) ❌ `"title"` isn't in `keyof User`.
- (d) ❌ `size` is read-only (implemented with a getter).
- (e) ❌ `Type 'string' does not satisfy the constraint 'Entity'`.
- (f) ✅ `withEmail: User[]`. `u` is contextually typed as `User`, and `email` is `string | undefined` thanks to the optional field.

**Design weakness: `readonly` isn't enforced by `keyof`.**
`K extends keyof T` includes `"id"`, so `users.update("u1", "id", "other")` **compiles** and, at runtime, changes an `id` the interface marked `readonly` (and desynchronises the `Map` key). `readonly` doesn't affect `keyof`, assignability (Q6), or generic indexed writes. Mitigation: restrict the keys explicitly, e.g. `K extends Exclude<keyof T, "id">`, and/or return a new object instead of mutating.

**Concepts exercised**
- **Extending interfaces** with optional and `readonly` members (`User`/`Product` from `Entity`).
- **Method signatures** (including a *generic method* whose type parameter belongs to each call, Q20) and a getter satisfying a `readonly` property.
- **Generics with constraints**: `T extends Entity`, `K extends keyof T`.
- **Inference** from arguments (`K` captured as a literal) and from contextual typing (the object literal's methods get their parameter types from `Repository<T>`).
- **Erasure:** every interface, constraint, and type argument vanishes; only the `Map`-based logic remains at runtime.

</details>

---

## Score yourself
| Score | Level |
|---|---|
| 0–10 correct | Review interfaces, generics basics and the compile-time/runtime split, then retry |
| 11–22 correct | Solid foundation; revisit the ⭐⭐⭐ questions on bivariance, literals, and inference |
| 23–31 correct | Strong; you understand the subtle semantics |
| 32–36 correct | Excellent; ready to teach this |
