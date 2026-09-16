# JUnit, Step by Step — `11-junit` Test Suite

This guide walks through **every JUnit 5, Mockito, and Hamcrest feature** used in the
`src/test/java/com/neueda/leap/sprint5/` folder of this project, in the order you'd
actually encounter them while reading the two test classes:

- `JUnitFeaturesDemoTest.java` — core JUnit 5 features
- `MockitoAndHamcrestDemoTest.java` — mocking and readable assertions

Each section explains **what the feature is**, **why it exists**, and **exactly where
it's used in this project**, with the real code alongside.

---

## Table of Contents

- [JUnit, Step by Step — `11-junit` Test Suite](#junit-step-by-step--11-junit-test-suite)
  - [Table of Contents](#table-of-contents)
  - [1. Project Setup: Dependencies and Test Runner](#1-project-setup-dependencies-and-test-runner)
  - [2. The Classes Under Test](#2-the-classes-under-test)
  - [3. `@Test` and `@DisplayName` — the Basics](#3-test-and-displayname--the-basics)
  - [4. `@BeforeEach` — Fresh State for Every Test](#4-beforeeach--fresh-state-for-every-test)
  - [5. JUnit 5 Lifecycle Annotations](#5-junit-5-lifecycle-annotations)
  - [6. Basic Assertions: `assertEquals`, `assertTrue`](#6-basic-assertions-assertequals-asserttrue)
  - [6. `assertThrows` — Testing for Exceptions the Formal Way](#6-assertthrows--testing-for-exceptions-the-formal-way)
  - [7. `assertAll` — Grouped Assertions](#7-assertall--grouped-assertions)
  - [8. `@ParameterizedTest` + `@ValueSource` — One Test, Many Inputs](#8-parameterizedtest--valuesource--one-test-many-inputs)
  - [9. `@ParameterizedTest` + `@CsvSource` — Input/Output Pairs](#9-parameterizedtest--csvsource--inputoutput-pairs)
  - [10. `@Nested` — Structuring the Test Report](#10-nested--structuring-the-test-report)
  - [11. `@Disabled` — Skipping a Test With Intent](#11-disabled--skipping-a-test-with-intent)
  - [12. Why Mocking? The Problem With Real Collaborators](#12-why-mocking-the-problem-with-real-collaborators)
  - [13. `@Mock` and `@ExtendWith(MockitoExtension.class)`](#13-mock-and-extendwithmockitoextensionclass)
  - [14. Stubbing: `when(...).thenReturn(...)`](#14-stubbing-whenthenreturn)
  - [15. `verify()` — Proving an Interaction Happened](#15-verify--proving-an-interaction-happened)
  - [16. `ArgumentCaptor` — Inspecting What Was Actually Passed](#16-argumentcaptor--inspecting-what-was-actually-passed)
  - [17. Mocks vs. Hand-Rolled Fakes](#17-mocks-vs-hand-rolled-fakes)
  - [18. Hamcrest Matchers — Readable, Composable Assertions](#18-hamcrest-matchers--readable-composable-assertions)
  - [19. Running the Tests](#19-running-the-tests)
  - [20. Quick Reference Cheat Sheet](#20-quick-reference-cheat-sheet)

---

## 1. Project Setup: Dependencies and Test Runner

The testing stack is declared in `pom.xml`, all scoped to `test` (they're never shipped
in the production jar):

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.11.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.11.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.hamcrest</groupId>
    <artifactId>hamcrest</artifactId>
    <version>2.2</version>
    <scope>test</scope>
</dependency>
```

- **`junit-jupiter`** — the JUnit 5 API (`@Test`, `@BeforeEach`, assertions, etc.).
- **`mockito-core`** + **`mockito-junit-jupiter`** — creates mock objects and wires
  Mockito into the JUnit 5 lifecycle.
- **`hamcrest`** — an alternative, composable assertion library (`assertThat(value, matcher)`).

The `maven-surefire-plugin` is what actually discovers and runs the tests when you run:

```bash
mvn test
```

---

## 2. The Classes Under Test

Both test classes exercise real production classes from
`src/main/java/com/neueda/leap/sprint5/`, so it helps to know them up front:

| Class | Role |
|---|---|
| `Holding` | Tracks a quantity; `adjust()` enforces "quantity can never go negative." |
| `Instrument` (abstract) | Has a `ticker` and an abstract `calculateFee(tradeValue)`. |
| `BondInstrument` | A flat $5 fee, regardless of trade value. |
| `EquityInstrument` | A 0.1% fee of trade value. |
| `Order` | Pairs a `clientId`, an `Instrument`, and a `tradeValue`. |
| `OrderExecutor` | Asks the `Order` for its fee, writes a line via a `ReportWriter`, returns the fee. |
| `ReportWriter` (interface) | Abstraction for "write a line somewhere." |
| `InMemoryReportWriter` | A hand-written test double that stores lines in a `List`. |

`OrderExecutor` is the star of the Mockito section, because its whole job is
**coordinating two collaborators** (`Instrument` and `ReportWriter`) rather than doing
real work itself — which is exactly the situation mocking is built for.

---

## 3. `@Test` and `@DisplayName` — the Basics

```java
@Test
@DisplayName("a fresh holding starts with the quantity it was constructed with")
void freshHoldingHasInitialQuantity() {
    assertEquals(100.0, holding.getQuantity(), 0.0001);
}
```

- **`@Test`** marks a method as a test case JUnit should run.
- **`@DisplayName`** gives it a human-readable name for the test report. A report full
  of `bondFeeIsAlwaysFive` is fine; one full of *"a bond's fee is always exactly $5,
  regardless of trade value"* is something a non-technical stakeholder could read.
  `@DisplayName` can be applied to a class, a `@Nested` class, or an individual test method.

---

## 4. `@BeforeEach` — Fresh State for Every Test

```java
private Holding holding;

@BeforeEach
void setUp() {
    holding = new Holding(100);
}
```

`setUp()` runs **before every single test method** in the class, giving each test its
own brand-new `Holding`. Without this, a `Holding` created once and shared across tests
would let one test's leftover state leak into another — producing flaky results that
depend on execution order. `@BeforeEach` guarantees isolation instead.

A `@Nested` class can declare its **own** `@BeforeEach`, which runs *in addition to*
the outer class's (see [Section 10](#10-nested--structuring-the-test-report)).

---

---

## 5. JUnit 5 Lifecycle Annotations

Your test class currently repeats new AuthService(new InMemoryUserRepository()) in every method. JUnit 5 gives you hooks to handle setup and teardown.

5.1 The four lifecycle hooks

| **Annotation** | **Runs** | **Method Must be** | **Typical Use** |
|---|---|---|---|
| `@BeforeAll` | **Once**, before any test in the class | `static` | Open a DB connection, start a container |
| `@BeforeEach` | Before **every** test | instance | **Reset state**, build fresh objects |
| `@AfterEach` | After **every** test | instance | Clean up, verify no leaks |
| `@AfterAll` | **Once**, after all tests | `static` | Close the connection |

5.2 Watch them fire
Create LifecycleDemoTest.java and actually run it — reading about the order is not the same as seeing it:

```java
package com.lab.auth;

import org.junit.jupiter.api.*;

class LifecycleDemoTest {

    @BeforeAll
    static void beforeAll() {
        System.out.println("  @BeforeAll   — once, before everything");
    }

    @BeforeEach
    void beforeEach(TestInfo info) {
        System.out.println("    @BeforeEach  — before " + info.getDisplayName());
    }

    @Test
    void firstTest() {
        System.out.println("      >> firstTest body");
    }

    @Test
    void secondTest() {
        System.out.println("      >> secondTest body");
    }

    @AfterEach
    void afterEach(TestInfo info) {
        System.out.println("    @AfterEach   — after " + info.getDisplayName());
    }

    @AfterAll
    static void afterAll() {
        System.out.println("  @AfterAll    — once, after everything");
    }
}
```

```
mvn -q test -Dtest=LifecycleDemoTest
```

Output:
```
  @BeforeAll   — once, before everything
    @BeforeEach  — before firstTest()
      >> firstTest body
    @AfterEach   — after firstTest()
    @BeforeEach  — before secondTest()
      >> secondTest body
    @AfterEach   — after secondTest()
  @AfterAll    — once, after everything
  ```
By default JUnit 5 creates a brand-new instance of your test class for every test method. That's a deliberate design choice: it means instance fields can't leak between tests. Since there is no single instance that spans all tests, @BeforeAll has nowhere to live except a static context.

You can change this with @TestInstance(Lifecycle.PER_CLASS), which reuses one instance and lets @BeforeAll be non-static — but then instance fields leak between tests, which is usually not what you want:

## 6. Basic Assertions: `assertEquals`, `assertTrue`

```java
@Test
@DisplayName("adjusting is reflected immediately")
void adjustChangesQuantity() {
    holding.adjust(50);
    assertEquals(150.0, holding.getQuantity(), 0.0001);
}
```

`assertEquals(expected, actual, delta)` is the workhorse assertion. The third argument
(`0.0001`) is a **tolerance** — required for `double` comparisons, since floating-point
arithmetic can produce tiny rounding errors that would otherwise cause a false failure.

---

## 6. `assertThrows` — Testing for Exceptions the Formal Way

```java
@Test
@DisplayName("adjusting below zero throws, and does not change state")
void adjustBelowZeroThrows() {
    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> holding.adjust(-1000)
    );
    assertTrue(exception.getMessage().contains("negative"));
}
```

`assertThrows(ExceptionType.class, lambda)`:

- Runs the lambda.
- **Fails the test if the expected exception is never thrown** — a manual
  `try { ...; fail(); } catch (X e) { ... }` can silently miss this if the `catch`
  block is empty.
- Returns the caught exception, so you can assert on it further (here, its message).

---

## 7. `assertAll` — Grouped Assertions

```java
@Test
@DisplayName("a bond's ticker and fee are both correct, reported together")
void bondReportsTickerAndFeeTogether() {
    BondInstrument bond = new BondInstrument("VOD.L");

    assertAll("bond properties",
            () -> assertEquals("VOD.L", bond.getTicker()),
            () -> assertEquals(5.0, bond.calculateFee(1000), 0.0001),
            () -> assertEquals(5.0, bond.calculateFee(1_000_000), 0.0001)
    );
}
```

A plain chain of three separate `assertEquals` calls **stops at the first failure**,
hiding any others until that one is fixed and the test re-run. `assertAll` runs every
lambda and reports **all** failures together in a single test run.

---

## 8. `@ParameterizedTest` + `@ValueSource` — One Test, Many Inputs

```java
@ParameterizedTest
@ValueSource(doubles = {1000, 5000, 1_000_000, 0.01})
@DisplayName("a bond's fee is always exactly $5, regardless of trade value")
void bondFeeIsAlwaysFive(double tradeValue) {
    assertEquals(5.0, new BondInstrument("VOD.L").calculateFee(tradeValue), 0.0001);
}
```

`@ParameterizedTest` replaces what would otherwise be four or five near-identical,
copy-pasted `@Test` methods. `@ValueSource` supplies **one input per run** — use it
when the assertion logic is the same for every input (here: the fee is always `5.0`).

---

## 9. `@ParameterizedTest` + `@CsvSource` — Input/Output Pairs

```java
@ParameterizedTest
@CsvSource({
        "1000, 1.0",
        "10000, 10.0",
        "100000, 100.0"
})
@DisplayName("an equity's fee is 0.1% of trade value")
void equityFeeIsOnePercentOfTradeValue(double tradeValue, double expectedFee) {
    assertEquals(expectedFee, new EquityInstrument("AAPL").calculateFee(tradeValue), 0.0001);
}
```

`@CsvSource` supplies **an input and its expected output, paired**, one row per run.
Use this over `@ValueSource` whenever each input has a *different* expected result —
here, the expected fee scales with the trade value.

---

## 10. `@Nested` — Structuring the Test Report

```java
@Nested
@DisplayName("when the holding is at exactly zero")
class WhenHoldingIsAtZero {

    private Holding zeroHolding;

    @BeforeEach
    void setUp() {
        zeroHolding = new Holding(0);
    }

    @Test
    @DisplayName("a positive adjustment succeeds")
    void positiveAdjustmentSucceeds() { ... }

    @Test
    @DisplayName("any negative adjustment throws")
    void anyNegativeAdjustmentThrows() { ... }
}
```

`@Nested` groups related tests inside their own inner class, so the test report itself
reads like documentation:

> `JUnitFeaturesDemoTest > when the holding is at exactly zero > any negative adjustment throws`

The nested class's `@BeforeEach` (`zeroHolding = new Holding(0)`) runs **in addition
to** the outer class's `setUp()` — both fire before each test inside `WhenHoldingIsAtZero`.

This pattern is used extensively in `MockitoAndHamcrestDemoTest`, which organizes its
tests into five `@Nested` classes: `Stubbing`, `Verifying`, `Capturing`,
`MocksVersusHandRolledFakes`, and `HamcrestMatchers`.

---

## 11. `@Disabled` — Skipping a Test With Intent

```java
@Test
@Disabled("Derivative instruments aren't introduced until Module 7's OCP kata")
void placeholderForFutureInstrumentType() {
    fail("not yet implemented");
}
```

`@Disabled` skips a test **while keeping a record of why**. This is different from
deleting the test or commenting it out — both of those lose the fact that the test was
*intended* to exist. `@Disabled` shows up in the test report as a skip, not a silence.

---

## 12. Why Mocking? The Problem With Real Collaborators

`MockitoAndHamcrestDemoTest` opens with a comment worth internalizing before any
Mockito syntax:

`OrderExecutor`'s actual job is small — ask its `Instrument` for a fee, ask its
`ReportWriter` to write a line, return the fee. Testing it with a **real**
`BondInstrument` and a **real** `InMemoryReportWriter` means the test is really
exercising three things at once: `OrderExecutor`'s coordination logic, *and*
`BondInstrument`'s fee formula, *and* `InMemoryReportWriter`'s storage. If any one of
the three has a bug, you get the same symptom — a red test with `OrderExecutor`'s name
on it — with no way to tell which class is actually broken without investigating.

**A mock replaces a real collaborator with a fake stand-in whose behavior you control
completely**, and which records every call made to it. Testing `OrderExecutor` against
a mock `Instrument` and a mock `ReportWriter` means: if the test goes red, it is
unambiguously `OrderExecutor`'s fault — nothing else is even real enough to have a bug.

This is the core principle: **a unit test should test one unit.**

---

## 13. `@Mock` and `@ExtendWith(MockitoExtension.class)`

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderExecutor, tested in true isolation with Mockito")
class MockitoAndHamcrestDemoTest {

    @Mock
    private Instrument mockInstrument;

    @Mock
    private ReportWriter mockWriter;
    ...
}
```

- **`@Mock`** creates a fake `Instrument` and a fake `ReportWriter` — objects of the
  right type that do nothing at all by default.
- **`@ExtendWith(MockitoExtension.class)`** is what actually processes those `@Mock`
  fields and creates fresh mocks before each test — functionally similar to
  `@BeforeEach`, but specifically for mock creation.

---

## 14. Stubbing: `when(...).thenReturn(...)`

```java
@Test
@DisplayName("when(...).thenReturn(...) makes the mock respond however the test needs")
void executorUsesWhateverFeeTheInstrumentReturns() {
    when(mockInstrument.calculateFee(1000.0)).thenReturn(42.0);

    Order order = new Order("C001", mockInstrument, 1000.0);
    OrderExecutor executor = new OrderExecutor(mockWriter);

    double fee = executor.execute(order);

    assertEquals(42.0, fee);
}
```

Read `when(mockInstrument.calculateFee(1000.0)).thenReturn(42.0)` as a sentence:
*"when `calculateFee` is called with `1000.0`, then return `42.0`."* `42.0` is not a
real fee any instrument would compute — that's deliberate. The test doesn't care what
a real fee formula looks like; it only checks whether `OrderExecutor` correctly uses
whatever its collaborator gives back.

```java
when(mockInstrument.calculateFee(anyDouble())).thenReturn(10.0);
```

`anyDouble()` (a **Mockito argument matcher**) stubs a response for *any* argument,
for tests where the exact input value doesn't matter to what's being checked.

---

## 15. `verify()` — Proving an Interaction Happened

`verify()` is a different *shape* of assertion — not "does this value equal that
value," but "did this interaction with a collaborator actually happen."

```java
verify(mockWriter).write("C001: $42.0");
```
Proves `OrderExecutor` talked to its writer with exactly the right line.

```java
verify(mockInstrument, times(1)).calculateFee(500.0);
verify(mockWriter, times(1)).write(anyString());
```
`times(1)` proves the call happened **exactly once** — not zero times, not twice.

```java
verifyNoInteractions(mockWriter);
```
Proves a mock was **never touched at all** — useful for proving a code path (e.g. a
validation failure) genuinely never reaches a particular collaborator.

---

## 16. `ArgumentCaptor` — Inspecting What Was Actually Passed

```java
ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
verify(mockWriter).write(captor.capture());

assertThat(captor.getValue(), allOf(containsString("C004"), containsString("7.5")));
```

`verify(mockWriter).write("C001: $42.0")` requires an **exact** string match.
`ArgumentCaptor` instead captures the real argument that was passed, so you can make a
looser or more detailed assertion on it afterward. The shape is always two steps:
**capture first**, via `captor.capture()` inside a `verify()` call, then **assert
separately** on `captor.getValue()`.

---

## 17. Mocks vs. Hand-Rolled Fakes

```java
@Test
@DisplayName("a hand-rolled fake still uses REAL collaborator logic elsewhere")
void theHandRolledFakeOnlyReplacesTheWriterNotTheInstrument() {
    InMemoryReportWriter fakeWriter = new InMemoryReportWriter();
    Instrument realBond = new BondInstrument("VOD.L");
    Order order = new Order("C003", realBond, 8000);

    new OrderExecutor(fakeWriter).execute(order);

    assertEquals(List.of("C003: $5.0"), fakeWriter.getLines());
}

@Test
@DisplayName("a mock isolates OrderExecutor from BOTH collaborators, not just one")
void theMockIsolatesFromBothCollaborators() {
    when(mockInstrument.calculateFee(anyDouble())).thenReturn(999.0);
    Order order = new Order("C003", mockInstrument, 8000);

    double fee = new OrderExecutor(mockWriter).execute(order);

    assertEquals(999.0, fee); // no real BondInstrument would ever produce 999.0
}
```

`InMemoryReportWriter` is a genuine, hand-written test double — but it only replaces
**one** of `OrderExecutor`'s two collaborators. The first test above still depends on
`BondInstrument`'s real fee formula being correct. The mock-based version replaces
**both** collaborators and returns `999.0` — a number no real `BondInstrument` would
ever produce — specifically to prove the result can't possibly depend on real
instrument logic at all.

**Neither approach is "wrong."** A hand-rolled fake is a fine choice when a
collaborator is simple and stable. Mockito earns its complexity when there are many
collaborators, or when controlling their exact behavior per-test matters.

---

## 18. Hamcrest Matchers — Readable, Composable Assertions

```java
assertThat(fee, is(42.0));
assertThat(fee, greaterThan(0.0));
```

`assertThat(value, matcher)` checks the same thing as `assertEquals`/`assertTrue`, but
reads closer to the requirement it's checking.

```java
assertThat("Settlement Report\nC001: $42.0", containsString("C001"));
assertThat("Settlement Report\nC001: $42.0", startsWith("Settlement"));

List<String> lines = List.of("first", "second", "third");
assertThat(lines, hasSize(3));
assertThat(lines, hasItem("second"));
```

`containsString`, `startsWith`, `hasSize`, and `hasItem` express intent on strings and
collections almost like plain English.

```java
assertThat(fee, allOf(greaterThan(0.0), lessThan(100.0)));
```

Hamcrest matchers **compose**: `allOf(...)` combines several matchers into one
readable assertion that reports exactly which part failed, where the plain-JUnit
equivalent would need two separate assertions or a hand-written boolean expression
with no useful failure message.

`assertEquals` and `assertThat` aren't competitors — use whichever reads more clearly
for a given check.

---

## 19. Running the Tests

From the project root (`11-junit/`):

```bash
mvn test
```

Maven Surefire discovers every class ending in `*Test` under `src/test/java`,
runs each `@Test` method (respecting `@BeforeEach`, `@Nested`, `@ParameterizedTest`,
and `@Disabled`), and prints a summary of passed/failed/skipped counts.

To run a single test class:

```bash
mvn test -Dtest=JUnitFeaturesDemoTest
mvn test -Dtest=MockitoAndHamcrestDemoTest
```

---

## 20. Quick Reference Cheat Sheet

| Feature | Purpose | Used in |
|---|---|---|
| `@Test` | Marks a method as a test case | Both files |
| `@DisplayName` | Human-readable name in test reports | Both files |
| `@BeforeEach` | Fresh setup before every test | `JUnitFeaturesDemoTest` |
| `assertEquals(expected, actual, delta)` | Numeric equality with tolerance | `JUnitFeaturesDemoTest` |
| `assertTrue(condition)` | Boolean check | `JUnitFeaturesDemoTest` |
| `assertThrows(Type.class, lambda)` | Assert an exception is thrown | `JUnitFeaturesDemoTest` |
| `assertAll(label, lambdas...)` | Run and report multiple assertions together | `JUnitFeaturesDemoTest` |
| `@ParameterizedTest` + `@ValueSource` | One test body, many single inputs | `JUnitFeaturesDemoTest` |
| `@ParameterizedTest` + `@CsvSource` | One test body, paired input/expected-output | `JUnitFeaturesDemoTest` |
| `@Nested` | Group related tests, document structure | Both files |
| `@Disabled(reason)` | Skip a test, keep the reason on record | `JUnitFeaturesDemoTest` |
| `@ExtendWith(MockitoExtension.class)` | Activates Mockito's `@Mock` processing | `MockitoAndHamcrestDemoTest` |
| `@Mock` | Creates a fake collaborator | `MockitoAndHamcrestDemoTest` |
| `when(...).thenReturn(...)` | Stub a mock's return value | `MockitoAndHamcrestDemoTest` |
| `anyDouble()` / `anyString()` | Argument matchers for stubs/verifies | `MockitoAndHamcrestDemoTest` |
| `verify(mock).method(args)` | Prove an interaction happened | `MockitoAndHamcrestDemoTest` |
| `verify(mock, times(n))` | Prove an interaction happened exactly `n` times | `MockitoAndHamcrestDemoTest` |
| `verifyNoInteractions(mock)` | Prove a mock was never touched | `MockitoAndHamcrestDemoTest` |
| `ArgumentCaptor` | Capture and inspect a real argument passed to a mock | `MockitoAndHamcrestDemoTest` |
| `assertThat(value, matcher)` | Hamcrest-style readable assertion | `MockitoAndHamcrestDemoTest` |
| `is`, `greaterThan`, `lessThan` | Basic Hamcrest matchers | `MockitoAndHamcrestDemoTest` |
| `containsString`, `startsWith`, `hasSize`, `hasItem` | String/collection Hamcrest matchers | `MockitoAndHamcrestDemoTest` |
| `allOf`, `anyOf` | Compose multiple matchers into one assertion | `MockitoAndHamcrestDemoTest` |
