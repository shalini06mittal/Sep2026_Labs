# Lab: Test-Driven Development — Red, Green, Refactor

## Learning Objectives

By the end of this lab you will be able to:

1. Explain the red-green-refactor cycle and why the steps happen in that specific order.
2. Write the *smallest possible* implementation that makes a failing test pass — resisting the urge to build more than the test demands.
3. Recognize TDD as a **design tool** that shapes your code's interface and structure, not just a safety net that checks correctness after the fact.

Estimated time: 60–90 minutes. Language used: Java with **JUnit 5** (the ideas transfer directly to any language/test framework).

---

## Part 0 — Setup

If you're using Maven, add JUnit 5 to `pom.xml`:

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>
```

Project layout:

```
src/main/java/Calculator.java
src/test/java/CalculatorTest.java
```

Run tests with:

```bash
mvn test
```

(If you're using an IDE like IntelliJ or Eclipse, you can just right-click the test class and choose "Run".)

---

## Part 1 — The Cycle, and Why Order Matters

The cycle has three steps, always in this order:

| Step | What you do | What it proves |
|------|-------------|-----------------|
| 🔴 **Red** | Write a test for behavior that doesn't exist yet. Run it. Watch it fail. | The test can actually fail — it's testing something real, not a tautology. |
| 🟢 **Green** | Write the *minimum* code to make that one test pass. | The behavior now exists, and only the behavior you specified. |
| 🔵 **Refactor** | Clean up the code (and/or the test) with the safety net of a passing test suite. | You can improve structure without changing behavior, because the tests would catch a break. |

**Why the order is not arbitrary:**

- **Red must come before Green.** If you write the implementation first and the test second, you're not testing your code — you're testing your memory of what you just wrote. A test you've never seen fail is a test you can't trust. It might pass even if it's broken (e.g., it asserts `assertTrue(true)` by accident). Seeing red first is proof the test is *capable* of catching a bug. In Java specifically, watch for the difference between a real failing assertion and a test that doesn't even compile yet (a missing class) — both count as "red," but make sure you know which one you're looking at.
- **Green must come before Refactor.** You need working code before you improve its shape. Refactoring broken code is just rewriting, and it's easy to get lost polishing something that doesn't work yet.
- **Refactor must not be skipped.** If you go Red → Green → Red → Green → Red → Green forever, you accumulate duplication and awkward code with no chance to clean it up while it's cheap. Refactoring right after green — while the logic is fresh and a safety net exists — is the moment cleanup is *cheapest and safest*. Skip it repeatedly and the codebase rots even though every individual test passes.

Think of it like a tightrope with a net: Red confirms the net has a hole where you need it, Green gets you across, Refactor lets you rearrange your balance without falling, because the net (tests) is still there.

---

## Part 2 — Exercise 1: The Smallest Possible Step

**Goal:** practice writing the *minimum* code that makes a test pass — nothing more.

We'll build a `Calculator.add` method one test at a time. Resist every urge to "just handle" cases the test doesn't ask about yet.

### Round 1

Write this test in `CalculatorTest.java`:

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class CalculatorTest {

    @Test
    void addReturnsSumOfTwoPositiveNumbers() {
        Calculator calc = new Calculator();
        assertEquals(5, calc.add(2, 3));
    }
}
```

Run `mvn test`. It fails — there's no `Calculator` class yet, so this won't even compile. That's 🔴 **Red**, and it's the correct kind of failure (a missing symbol, not a typo in your assertion).

Now write the **smallest** thing that makes it pass. Not this:

```java
// Resist this — it's more than the test requires
public class Calculator {
    public int add(int... numbers) {
        int sum = 0;
        for (int n : numbers) sum += n;
        return sum;
    }
}
```

Instead, literally the least code that satisfies the one test you have:

```java
public class Calculator {
    public int add(int a, int b) {
        return 5;
    }
}
```

Yes — `return 5;` passes the test. That feels wrong, and that discomfort is the point of this exercise: **a single test can never prove a general implementation is correct.** It only proves the code satisfies *the examples you gave it*. The looseness in `return 5` is really a gap in your test suite, not a flaw in the rule "write the smallest thing that passes."

### Round 2

Add a second test that exposes the gap:

```java
@Test
void addReturnsSumOfDifferentNumbers() {
    Calculator calc = new Calculator();
    assertEquals(11, calc.add(10, 1));
}
```

Run it — 🔴 Red, because `return 5;` fails this new case. Now the smallest change that makes *both* tests pass forces real logic:

```java
public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
}
```

🟢 Green. Both tests pass.

### Refactor

Look at the two tests — is there duplication? Both create `new Calculator()`. Refactor using a `@BeforeEach` setup method:

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalculatorTest {

    private Calculator calc;

    @BeforeEach
    void setUp() {
        calc = new Calculator();
    }

    @Test
    void addReturnsSumOfTwoPositiveNumbers() {
        assertEquals(5, calc.add(2, 3));
    }

    @Test
    void addReturnsSumOfDifferentNumbers() {
        assertEquals(11, calc.add(10, 1));
    }
}
```

Run `mvn test` again — still green. You changed structure with zero risk, because the tests would have screamed if you'd broken something.

**Takeaway:** "smallest implementation" isn't cheating — it's a discipline that makes *the tests* responsible for specifying the behavior. If `return 5;` is embarrassing, the fix is to write a better test, not to guess ahead at logic the test doesn't require yet.

---

## Part 3 — Exercise 2: Letting Tests Drive the Design

**Goal:** see TDD shape an interface, not just verify one you already decided on.

Task: build a `Stack` with `push`, `pop`, and `isEmpty`. Don't write a class first. Let each test pull the next piece of the design out of you.

### Step 1 — a stack starts empty

```java
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class StackTest {

    @Test
    void newStackIsEmpty() {
        Stack stack = new Stack();
        assertTrue(stack.isEmpty());
    }
}
```

🔴 Red (no `Stack` class). Minimal green:

```java
public class Stack {
    public boolean isEmpty() {
        return true;
    }
}
```

Notice: this test *decided* the class needs an `isEmpty()` method returning a boolean, before you wrote any storage logic at all. The test came first, and it already fixed part of the interface.

### Step 2 — pushing makes it non-empty

```java
import static org.junit.jupiter.api.Assertions.assertFalse;

@Test
void stackWithPushedItemIsNotEmpty() {
    Stack stack = new Stack();
    stack.push(1);
    assertFalse(stack.isEmpty());
}
```

🔴 Red — `push` doesn't exist, and even once added, `isEmpty()` still hardcodes `true`. Minimal green forces real state:

```java
import java.util.ArrayList;
import java.util.List;

public class Stack {
    private final List<Integer> items = new ArrayList<>();

    public void push(int item) {
        items.add(item);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
```

### Step 3 — pop returns the last pushed item (LIFO)

```java
@Test
void popReturnsMostRecentlyPushedItem() {
    Stack stack = new Stack();
    stack.push(1);
    stack.push(2);
    assertEquals(2, stack.pop());
}

@Test
void popRemovesTheItem() {
    Stack stack = new Stack();
    stack.push(1);
    stack.push(2);
    stack.pop();
    assertEquals(1, stack.pop());
}
```

🔴 Red — no `pop`. Minimal green:

```java
public int pop() {
    return items.remove(items.size() - 1);
}
```

🟢 Both pass — removing from the end of the list gives LIFO order, so no extra logic was needed beyond calling the right `List` method.

### Reflect before continuing

At no point did you sit down and design "a `Stack` class with three methods" up front. Each test asked one question about behavior, and the interface (`push`, `pop`, `isEmpty`, parameter and return types, exception semantics) emerged answer by answer. This is the difference between:

- **TDD as a checking tool:** design the class first, then write tests afterward to confirm it works.
- **TDD as a design tool:** let the next test decide what the class must be able to do next, so the interface is pulled into existence by real usage examples instead of imagined ahead of time.

### One more design decision, made visible by a test

What should `pop()` do on an empty stack? Don't guess — write the test that forces the decision:

```java
import static org.junit.jupiter.api.Assertions.assertThrows;

@Test
void popOnEmptyStackThrowsException() {
    Stack stack = new Stack();
    assertThrows(IndexOutOfBoundsException.class, stack::pop);
}
```

Run it — it likely already passes, since `List.remove(int index)` throws `IndexOutOfBoundsException` when the list is empty. That's still valuable: the test now **documents** a design decision (empty pop is an error, not returning `0` or `null`) that was previously just an accident of implementation. If you later refactor the backing storage away from an `ArrayList`, this test protects that decision. (You might also consider defining your own `EmptyStackException` to make the contract clearer — try it, and notice the test tells you exactly where you need to change the code.)

---

## Part 4 — Exercise 3: When Skipping "Red" Bites You

**Goal:** feel directly why writing the test *after* the code is a weaker practice, even when it looks like the same test.

Below is an already-written method. Someone wrote the implementation first, then wrote a test to match it:

```java
public class PasswordValidator {
    public boolean isValid(String password) {
        return password.length() >= 8;
    }
}
```

```java
@Test
void passwordValidation() {
    PasswordValidator validator = new PasswordValidator();
    assertTrue(validator.isValid("password"));
}
```

This test passes immediately — it was written by looking at the code and copying its behavior. Now:

1. Temporarily replace the method body with `return true;` (a deliberately broken version).
2. Run the test.

It still passes! A test written *after* the code, by consulting the code, tends to only exercise the path the author already had in mind, and can't distinguish "correct" from "coincidentally similar." This is exactly what Red-before-Green prevents: if you'd written `passwordValidation` first against no implementation, then made it pass with real logic, you would have had a moment where you *watched it fail* — proof the test was discriminating between right and wrong behavior.

**Now do it properly, TDD-style, adding one requirement at a time:**

```java
@Test
void passwordShorterThan8CharsIsInvalid() {
    assertFalse(validator.isValid("short1"));
}

@Test
void passwordWithoutDigitIsInvalid() {
    assertFalse(validator.isValid("noDigitsHere"));
}

@Test
void passwordWithDigitAndLengthIsValid() {
    assertTrue(validator.isValid("goodpass1"));
}
```

Add these one at a time, watching each go 🔴 before writing the minimal 🟢 code (e.g., checking length, then scanning for a digit with something like `password.chars().anyMatch(Character::isDigit)`). By the end, `isValid` was **specified entirely by its tests** — anyone reading the test class can tell you the exact rules without reading the implementation. That's the design-tool payoff: the tests *are* a readable specification, because they were written as decisions, not as an afterthought that mirrors whatever code already existed.

---

## Part 5 — Common Pitfalls Checklist

Go back through your own code from this lab and check for these:

- [ ] Did I ever write implementation code before a failing test existed for it? (If so, that code is unverified by TDD's own logic — a test that never went red can't be trusted.)
- [ ] Did I write more than the current test required, "because I knew I'd need it later"? (This is over-engineering ahead of evidence — let the *next* test ask for it.)
- [ ] Did I skip refactoring after any green step because "it works, move on"? (Small skips compound into a codebase that's technically tested but structurally messy.)
- [ ] Could someone read only my test names and know what the code is supposed to do? (If not, the tests aren't functioning as a specification yet.)

---

## Part 6 — Reflection Questions

Answer these in a few sentences each:

1. In Exercise 1, `return 5;` technically passed the first test. Why is that a sign of a weak *test*, not a flaw in the "smallest implementation" rule?
2. In Exercise 2, list the order in which `Stack`'s methods came into existence. Would you have designed the class differently if you'd written the whole interface up front, before any tests? Which approach do you trust more, and why?
3. In Exercise 3, the "code-first" test passed even against a broken implementation. Describe, in your own words, what the "Red" step is actually proving when it succeeds — i.e., what does a test have to be able to do before it's trustworthy?
4. Where in your own past projects have you written tests after the code (or skipped them entirely)? Knowing what you now know, what's one thing you might have designed differently if you'd written a failing test first?

---

## Summary

- **Red** proves your test can fail — without this, you don't actually know the test tests anything.
- **Green** proves the *minimum* behavior exists — resisting extra code keeps the tests, not your assumptions, in charge of what the software does.
- **Refactor** is where you cash in the safety net to improve structure risk-free — skipping it repeatedly lets small messes become permanent.
- Across the exercises, the interface of `Stack` and the rules of `PasswordValidator.isValid` were never designed in advance on paper — they were **discovered** one test at a time. That's what it means to say TDD is a design tool: the tests don't just check a design you already had, they actively shape what the design becomes.
