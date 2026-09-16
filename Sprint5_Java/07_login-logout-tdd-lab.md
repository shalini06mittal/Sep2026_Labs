# Lab: From TDD to JUnit 5 to Mockito — Built on a Login/Logout Console App

**Duration:** ~3 hours · **Level:** Beginner → Intermediate · **Language:** Java 17+

---

## What you'll build

A tiny console application where a user can **log in** and **log out**. That's it. No database, no web framework, no Spring.

The whole point is that this small app hides a trap that teaches you the single most important lesson in unit testing:

> **The session cache survives between tests. If you don't reset it, your tests lie to you.**

You will deliberately write a test suite that passes, then reorder two tests and watch it break. Then you'll fix it with `@BeforeEach` and understand *why* that annotation exists — not because a tutorial told you to use it.

### Path through the lab

| Part | Topic | What you walk away with |
|---|---|---|
| 0 | Setup | Working Maven project |
| 1 | TDD | Red → Green → Refactor, done for real |
| 2 | JUnit 5 lifecycle | `@BeforeEach`, `@AfterEach`, `@BeforeAll`, `@AfterAll` |
| 3 | **The Session Trap** | Why shared state ruins test suites |
| 4 | JUnit 5 in depth | Nested, parameterized, repeated, tags, assertions |
| 5 | Mockito | Mocks, stubs, verification, captors, spies |
| 6 | Hamcrest | Readable assertions that fail with useful messages |
| 7 | The console app | Tying it together |
| 8 | Exercises | Your turn |

---

## Part 0 — Setup

### 0.1 Project structure

```
login-lab/
├── pom.xml
└── src
    ├── main/java/com/lab/auth/
    │   ├── User.java
    │   ├── Session.java
    │   ├── SessionManager.java
    │   ├── UserRepository.java
    │   ├── InMemoryUserRepository.java
    │   ├── TokenGenerator.java
    │   ├── UuidTokenGenerator.java
    │   ├── AuditLogger.java
    │   ├── ConsoleAuditLogger.java
    │   ├── AuthService.java
    │   ├── InvalidCredentialsException.java
    │   ├── NotLoggedInException.java
    │   └── ConsoleApp.java
    └── test/java/com/lab/auth/
        └── (we build these up through the lab)
```

### 0.2 `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.lab</groupId>
  <artifactId>login-lab</artifactId>
  <version>1.0-SNAPSHOT</version>

  <properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <!-- JUnit 5 -->
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.10.2</version>
      <scope>test</scope>
    </dependency>

    <!-- Parameterized test sources -->
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-params</artifactId>
      <version>5.10.2</version>
      <scope>test</scope>
    </dependency>

    <!-- Mockito -->
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

    <!-- Hamcrest -->
    <dependency>
      <groupId>org.hamcrest</groupId>
      <artifactId>hamcrest</artifactId>
      <version>2.2</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.2.5</version>
      </plugin>
    </plugins>
  </build>
</project>
```

Verify it works:

```bash
mvn -q test
```

You should see `No tests to run` and a `BUILD SUCCESS`. Good — you're ready.

---

## Part 1 — TDD: Red, Green, Refactor

### 1.1 The rules

TDD has exactly three steps, repeated in a tight loop:

1. **🔴 RED** — Write a test for behaviour that does not exist yet. Run it. It must **fail**. If it passes, your test is wrong.
2. **🟢 GREEN** — Write the *dumbest possible* code that makes it pass. Cheating is allowed and encouraged.
3. **🔵 REFACTOR** — Clean up. Tests must stay green throughout.

The rule that trips people up: **never write production code without a failing test demanding it.**

### 1.2 Cycle 1 — "A valid user can log in"

#### 🔴 RED

Create `src/test/java/com/lab/auth/AuthServiceTddTest.java`:

```java
package com.lab.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTddTest {

    @Test
    void loginWithValidCredentialsReturnsAToken() {
        AuthService auth = new AuthService();

        String token = auth.login("alice", "secret123");

        assertNotNull(token, "login should return a session token");
    }
}
```

Run it:

```bash
mvn -q test
```

**It doesn't even compile.** `AuthService` doesn't exist. That counts as RED — compilation failure is the reddest red there is.

#### 🟢 GREEN

Create the minimum. `src/main/java/com/lab/auth/AuthService.java`:

```java
package com.lab.auth;

public class AuthService {
    public String login(String username, String password) {
        return "fake-token";   // shameless. deliberately shameless.
    }
}
```

```bash
mvn -q test    # ✅ BUILD SUCCESS
```

> **Why hardcode?** Because it proves the *test harness itself* works before you trust it to verify real logic. This technique has a name: **Fake It Till You Make It**. The next test will force you to delete the fake.

#### 🔵 REFACTOR

Nothing to clean yet. Move on.

---

### 1.3 Cycle 2 — "A wrong password is rejected"

#### 🔴 RED

```java
    @Test
    void loginWithWrongPasswordIsRejected() {
        AuthService auth = new AuthService();

        assertThrows(InvalidCredentialsException.class,
                     () -> auth.login("alice", "wrong-password"));
    }
```

Fails — the fake returns a token for *everything*. **This test kills the fake.**

#### 🟢 GREEN

`InvalidCredentialsException.java`:

```java
package com.lab.auth;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
```

`User.java` — a Java record, immutable and free of boilerplate:

```java
package com.lab.auth;

public record User(String username, String password) { }
```

Now `AuthService` needs somewhere to look users up:

```java
package com.lab.auth;

import java.util.Map;

public class AuthService {

    private final Map<String, User> users = Map.of(
        "alice", new User("alice", "secret123")
    );

    public String login(String username, String password) {
        User user = users.get(username);
        if (user == null || !user.password().equals(password)) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return "fake-token";
    }
}
```

✅ Both tests green.

#### 🔵 REFACTOR

That hardcoded `Map` is a problem. Real users live somewhere else, and a test shouldn't depend on Alice existing. Extract an interface — this is the seam Mockito will use later.

`UserRepository.java`:

```java
package com.lab.auth;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
}
```

`InMemoryUserRepository.java`:

```java
package com.lab.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> users = new HashMap<>();

    public InMemoryUserRepository() {
        save(new User("alice", "secret123"));
        save(new User("bob",   "hunter2"));
    }

    public void save(User user) {
        users.put(user.username(), user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }
}
```

`AuthService` now takes the repository via the constructor — **constructor injection**, the thing that makes code testable:

```java
package com.lab.auth;

public class AuthService {

    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public String login(String username, String password) {
        User user = users.findByUsername(username)
                         .orElseThrow(() -> new InvalidCredentialsException(
                                 "Invalid username or password"));
        if (!user.password().equals(password)) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return "fake-token";
    }
}
```

Update both tests to `new AuthService(new InMemoryUserRepository())`. ✅ Still green. That's a successful refactor: **behaviour unchanged, design improved, tests never went red.**

---

### 1.4 Cycle 3 — "Logging in creates a session"

Now we introduce the thing this whole lab revolves around.

#### 🔴 RED

```java
    @Test
    void loginStoresAnActiveSession() {
        AuthService auth = new AuthService(new InMemoryUserRepository());

        String token = auth.login("alice", "secret123");

        assertTrue(auth.isLoggedIn(token));
    }
```

#### 🟢 GREEN

`Session.java`:

```java
package com.lab.auth;

import java.time.Instant;

public class Session {

    private final String token;
    private final String username;
    private final Instant createdAt;

    public Session(String token, String username, Instant createdAt) {
        this.token = token;
        this.username = username;
        this.createdAt = createdAt;
    }

    public String getToken()      { return token; }
    public String getUsername()   { return username; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Session{" + username + ", token=" + token + "}";
    }
}
```

`SessionManager.java` — **a singleton, on purpose.** This is the landmine.

```java
package com.lab.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Holds every active session for the whole application.
 *
 * NOTE: this is a singleton — one instance, shared by everything,
 * living for the entire JVM lifetime. That includes your test run.
 */
public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private final Map<String, Session> sessions = new HashMap<>();

    private SessionManager() { }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void save(Session session) {
        sessions.put(session.getToken(), session);
    }

    public Session find(String token) {
        return sessions.get(token);
    }

    /** @return true if a session was actually removed */
    public boolean remove(String token) {
        return sessions.remove(token) != null;
    }

    public int activeCount() {
        return sessions.size();
    }

    public Set<String> activeUsernames() {
        return sessions.values().stream()
                       .map(Session::getUsername)
                       .collect(java.util.stream.Collectors.toSet());
    }

    /** Wipes every session. Your tests are going to need this. */
    public void clear() {
        sessions.clear();
    }
}
```

Wire it into `AuthService`:

```java
    private final SessionManager sessions = SessionManager.getInstance();

    public String login(String username, String password) {
        User user = users.findByUsername(username)
                         .orElseThrow(() -> new InvalidCredentialsException(
                                 "Invalid username or password"));
        if (!user.password().equals(password)) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        String token = java.util.UUID.randomUUID().toString();
        sessions.save(new Session(token, username, java.time.Instant.now()));
        return token;
    }

    public boolean isLoggedIn(String token) {
        return token != null && sessions.find(token) != null;
    }
```

✅ Green.

---

### 1.5 Cycle 4 — "Logout destroys the session"

#### 🔴 RED

```java
    @Test
    void logoutRemovesTheSession() {
        AuthService auth = new AuthService(new InMemoryUserRepository());
        String token = auth.login("alice", "secret123");

        auth.logout(token);

        assertFalse(auth.isLoggedIn(token));
    }

    @Test
    void logoutWithoutAnActiveSessionFails() {
        AuthService auth = new AuthService(new InMemoryUserRepository());

        assertThrows(NotLoggedInException.class,
                     () -> auth.logout("token-that-was-never-issued"));
    }
```

#### 🟢 GREEN

`NotLoggedInException.java`:

```java
package com.lab.auth;

public class NotLoggedInException extends RuntimeException {
    public NotLoggedInException(String message) {
        super(message);
    }
}
```

```java
    public void logout(String token) {
        boolean removed = sessions.remove(token);
        if (!removed) {
            throw new NotLoggedInException("No active session for token: " + token);
        }
    }
```

✅ Green. You now have a working login/logout built entirely test-first.

---

## Part 2 — JUnit 5 Lifecycle Annotations

Your test class currently repeats `new AuthService(new InMemoryUserRepository())` in every method. JUnit 5 gives you hooks to handle setup and teardown.

### 2.1 The four lifecycle hooks

| Annotation | Runs | Method must be | Typical use |
|---|---|---|---|
| `@BeforeAll` | **Once**, before any test in the class | `static` | Open a DB connection, start a container |
| `@BeforeEach` | Before **every** test | instance | **Reset state**, build fresh objects |
| `@AfterEach` | After **every** test | instance | Clean up, verify no leaks |
| `@AfterAll` | **Once**, after all tests | `static` | Close the connection |

### 2.2 Watch them fire

Create `LifecycleDemoTest.java` and actually run it — reading about the order is not the same as seeing it:

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

```bash
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

### 2.3 Why `@BeforeAll` must be `static`

By default JUnit 5 creates a **brand-new instance of your test class for every test method**. That's a deliberate design choice: it means instance fields can't leak between tests. Since there is no single instance that spans all tests, `@BeforeAll` has nowhere to live except a static context.

You can change this with `@TestInstance(Lifecycle.PER_CLASS)`, which reuses one instance and lets `@BeforeAll` be non-static — but then **instance fields leak between tests**, which is usually not what you want:

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PerClassTest {

    @BeforeAll
    void setUp() { /* no 'static' needed now */ }
}
```

---

## Part 3 — 🚨 The Session Trap

This is the centrepiece of the lab. Do not skip it.

### 3.1 Set the trap

Create `SessionTrapTest.java`. Note what's **missing**: there is no `@BeforeEach`.

```java
package com.lab.auth;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("🚨 The Session Trap — no cleanup between tests")
class SessionTrapTest {

    private final AuthService auth = new AuthService(new InMemoryUserRepository());

    // Deliberately forced to run FIRST.
    @Test
    @Order(1)
    @DisplayName("logout ends alice's session")
    void logoutEndsSession() {
        // We assume alice is logged in... but who logged her in?
        String token = SessionManager.getInstance()
                                     .activeUsernames()
                                     .isEmpty() ? "???" : "???";

        auth.logout(token);          // 💥
        assertEquals(0, SessionManager.getInstance().activeCount());
    }

    @Test
    @Order(2)
    @DisplayName("login starts alice's session")
    void loginStartsSession() {
        String token = auth.login("alice", "secret123");
        assertTrue(auth.isLoggedIn(token));
    }
}
```

Run it:

```bash
mvn -q test -Dtest=SessionTrapTest
```

```
[ERROR] logout ends alice's session
  com.lab.auth.NotLoggedInException: No active session for token: ???
```

**The logout test cannot stand on its own.** It only ever passed because, by luck of ordering, a login test happened to run first and left a session lying around in the singleton.

### 3.2 The two separate bugs hiding here

This one failure is actually teaching two distinct lessons:

**Bug A — Test order dependency.** `logout` needs a session to exist. It was silently relying on a *different test method* to create one. JUnit makes no promise about method order by default (it uses a deterministic but intentionally non-obvious algorithm). Change the JVM, add a test, upgrade JUnit — the order can shift and your suite breaks for no apparent reason.

**Bug B — State leaking through the singleton.** `SessionManager` is static. It lives as long as the JVM. Every test in your entire suite shares it. Prove it:

```java
package com.lab.auth;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("🚨 Leak proof — state escapes from one test into the next")
class SessionLeakTest {

    private final AuthService auth = new AuthService(new InMemoryUserRepository());

    @Test
    @Order(1)
    void testOne_logsAliceIn() {
        auth.login("alice", "secret123");
        assertEquals(1, SessionManager.getInstance().activeCount());
    }

    @Test
    @Order(2)
    void testTwo_expectsACleanSlate() {
        // This test logged nobody in. It should see zero sessions.
        assertEquals(0, SessionManager.getInstance().activeCount(),
                     "a fresh test should never see another test's sessions");
    }
}
```

```
[ERROR] testTwo_expectsACleanSlate
  a fresh test should never see another test's sessions ==> expected: <0> but was: <1>
```

Alice walked straight out of test one and into test two.

> **The failure mode that costs real teams real days:** tests pass locally, fail on CI. Or pass when you run the whole class, fail when you run one method. Or pass in the morning and fail after a colleague adds an unrelated test. The cause is almost always leaked shared state.

### 3.3 Spring the trap — `@BeforeEach`

Two fixes, both required:

1. **Reset the shared cache before every test** → `@BeforeEach`
2. **Make each test set up its own preconditions** → no test may depend on another

`SessionFixedTest.java`:

```java
package com.lab.auth;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("✅ Fixed — every test owns its own state")
class SessionFixedTest {

    private AuthService auth;

    @BeforeEach
    void setUp() {
        // FIX 1: wipe the shared singleton so every test starts from zero
        SessionManager.getInstance().clear();

        // FIX 2: fresh service, fresh repository, every single time
        auth = new AuthService(new InMemoryUserRepository());

        assertEquals(0, SessionManager.getInstance().activeCount(),
                     "precondition: no sessions at test start");
    }

    @AfterEach
    void tearDown() {
        // Belt and braces: leave the world as clean as you found it,
        // so a test that forgets @BeforeEach elsewhere still gets a clean slate.
        SessionManager.getInstance().clear();
    }

    @Test
    @Order(1)
    @DisplayName("logout ends the session — and logs in first, itself")
    void logoutEndsSession() {
        String token = auth.login("alice", "secret123");   // ← own your setup

        auth.logout(token);

        assertFalse(auth.isLoggedIn(token));
        assertEquals(0, SessionManager.getInstance().activeCount());
    }

    @Test
    @Order(2)
    @DisplayName("login starts a session")
    void loginStartsSession() {
        String token = auth.login("alice", "secret123");

        assertTrue(auth.isLoggedIn(token));
        assertEquals(1, SessionManager.getInstance().activeCount());
    }
}
```

✅ Green. Now **delete the `@Order` annotations** and run again. Still green. Run each method individually. Still green. That is what an independent test suite looks like.

### 3.4 The FIRST principles

Every good unit test is:

- **F**ast — milliseconds, so you run them constantly
- **I**ndependent — no test needs any other test to have run
- **R**epeatable — same result on your laptop, your colleague's, and CI
- **S**elf-validating — passes or fails, no human reading console output
- **T**imely — written with (or before) the code

Part 3 is entirely about **I**. `@BeforeEach` is how you buy it.

---

## Part 4 — JUnit 5 in Depth

Now that state is under control, let's cover the rest of the toolkit.

### 4.1 The assertions you'll actually use

```java
package com.lab.auth;

import org.junit.jupiter.api.*;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JUnit 5 assertion toolkit")
class AssertionsTest {

    private AuthService auth;

    @BeforeEach
    void setUp() {
        SessionManager.getInstance().clear();
        auth = new AuthService(new InMemoryUserRepository());
    }

    @Test
    @DisplayName("assertEquals / assertNotNull / assertTrue")
    void basics() {
        String token = auth.login("alice", "secret123");

        assertNotNull(token);
        assertTrue(auth.isLoggedIn(token));
        assertEquals(1, SessionManager.getInstance().activeCount());
        assertNotEquals("", token);
    }

    @Test
    @DisplayName("assertAll — report every failure, not just the first")
    void assertAllReportsEverything() {
        String token = auth.login("alice", "secret123");
        Session session = SessionManager.getInstance().find(token);

        // Without assertAll, the first failure hides the rest.
        assertAll("session should be fully populated",
            () -> assertEquals("alice", session.getUsername()),
            () -> assertEquals(token, session.getToken()),
            () -> assertNotNull(session.getCreatedAt())
        );
    }

    @Test
    @DisplayName("assertThrows — capture the exception and inspect it")
    void assertThrowsInspectsTheException() {
        NotLoggedInException ex = assertThrows(
            NotLoggedInException.class,
            () -> auth.logout("bogus-token")
        );

        assertTrue(ex.getMessage().contains("bogus-token"));
    }

    @Test
    @DisplayName("assertDoesNotThrow — the happy path shouldn't blow up")
    void assertDoesNotThrowOnValidLogout() {
        String token = auth.login("bob", "hunter2");

        assertDoesNotThrow(() -> auth.logout(token));
    }

    @Test
    @DisplayName("assertTimeout — login must be fast")
    void loginIsFast() {
        assertTimeout(Duration.ofMillis(500),
                      () -> auth.login("alice", "secret123"));
    }

    @Test
    @Timeout(1)   // seconds — annotation form, fails the test if exceeded
    @DisplayName("@Timeout as an annotation")
    void loginIsFastAnnotated() {
        auth.login("alice", "secret123");
    }
}
```

**Key distinction:** `assertAll` groups independent assertions so a single run tells you *everything* that's wrong. Without it, you fix one line, rerun, find the next failure, rerun again. Use it whenever you're checking several properties of one object.

### 4.2 `@Nested` — group related tests

Nested classes give your report a readable tree structure, and each nested class gets its own `@BeforeEach` in addition to the outer one.

```java
package com.lab.auth;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthService")
class AuthServiceNestedTest {

    private AuthService auth;

    @BeforeEach
    void resetWorld() {
        SessionManager.getInstance().clear();
        auth = new AuthService(new InMemoryUserRepository());
    }

    @Nested
    @DisplayName("when logging in")
    class Login {

        @Test
        @DisplayName("with valid credentials → issues a token")
        void validCredentials() {
            assertNotNull(auth.login("alice", "secret123"));
        }

        @Test
        @DisplayName("with a wrong password → rejected")
        void wrongPassword() {
            assertThrows(InvalidCredentialsException.class,
                         () -> auth.login("alice", "nope"));
        }

        @Test
        @DisplayName("with an unknown user → rejected")
        void unknownUser() {
            assertThrows(InvalidCredentialsException.class,
                         () -> auth.login("mallory", "whatever"));
        }

        @Test
        @DisplayName("twice → two independent sessions")
        void twiceCreatesTwoSessions() {
            String t1 = auth.login("alice", "secret123");
            String t2 = auth.login("bob", "hunter2");

            assertNotEquals(t1, t2);
            assertEquals(2, SessionManager.getInstance().activeCount());
        }
    }

    @Nested
    @DisplayName("when logging out")
    class Logout {

        private String token;

        // Runs AFTER the outer @BeforeEach — so the world is already clean.
        @BeforeEach
        void logAliceIn() {
            token = auth.login("alice", "secret123");
        }

        @Test
        @DisplayName("with a live session → session destroyed")
        void liveSession() {
            auth.logout(token);

            assertFalse(auth.isLoggedIn(token));
            assertEquals(0, SessionManager.getInstance().activeCount());
        }

        @Test
        @DisplayName("twice → second attempt rejected")
        void twiceFails() {
            auth.logout(token);

            assertThrows(NotLoggedInException.class, () -> auth.logout(token));
        }

        @Test
        @DisplayName("with an unknown token → rejected, live session untouched")
        void unknownTokenDoesNotAffectOthers() {
            assertThrows(NotLoggedInException.class, () -> auth.logout("ghost"));
            assertTrue(auth.isLoggedIn(token));
        }
    }
}
```

Report output:

```
AuthService
├─ when logging in
│  ├─ with valid credentials → issues a token          ✔
│  ├─ with a wrong password → rejected                 ✔
│  ├─ with an unknown user → rejected                  ✔
│  └─ twice → two independent sessions                 ✔
└─ when logging out
   ├─ with a live session → session destroyed          ✔
   ├─ twice → second attempt rejected                  ✔
   └─ with an unknown token → rejected, ...            ✔
```

> **Order of `@BeforeEach` in nested classes:** outermost first, then inward. Outer `resetWorld()` runs, *then* inner `logAliceIn()`. That's exactly the layering you want — global reset, then test-group-specific setup.

### 4.3 `@ParameterizedTest` — one test, many inputs

Stop copy-pasting near-identical test methods.

```java
package com.lab.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Parameterized login validation")
class LoginParameterizedTest {

    private AuthService auth;

    @BeforeEach
    void setUp() {
        SessionManager.getInstance().clear();
        auth = new AuthService(new InMemoryUserRepository());
    }

    // --- @ValueSource: a single argument per run ---
    @ParameterizedTest(name = "[{index}] password \"{0}\" is rejected")
    @ValueSource(strings = {"wrong", "SECRET123", "secret12", "secret1234", " secret123"})
    void wrongPasswordsAreRejected(String password) {
        assertThrows(InvalidCredentialsException.class,
                     () -> auth.login("alice", password));
    }

    // --- @NullAndEmptySource: the two inputs everyone forgets ---
    @ParameterizedTest(name = "[{index}] username \"{0}\" is rejected")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void blankUsernamesAreRejected(String username) {
        assertThrows(IllegalArgumentException.class,
                     () -> auth.login(username, "secret123"));
    }

    // --- @CsvSource: multiple arguments per run ---
    @ParameterizedTest(name = "[{index}] {0}/{1} → login should succeed = {2}")
    @CsvSource({
        "alice,   secret123, true",
        "bob,     hunter2,   true",
        "alice,   hunter2,   false",
        "bob,     secret123, false",
        "mallory, secret123, false"
    })
    void credentialMatrix(String username, String password, boolean shouldSucceed) {
        if (shouldSucceed) {
            assertNotNull(auth.login(username, password));
        } else {
            assertThrows(InvalidCredentialsException.class,
                         () -> auth.login(username, password));
        }
    }

    // --- @MethodSource: full control, complex objects ---
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("validUsers")
    void everyValidUserCanLogIn(User user) {
        String token = auth.login(user.username(), user.password());

        assertTrue(auth.isLoggedIn(token));
    }

    static Stream<User> validUsers() {
        return Stream.of(
            new User("alice", "secret123"),
            new User("bob",   "hunter2")
        );
    }

    // --- @EnumSource: every value of an enum ---
    enum BadInput {
        EMPTY(""), SPACES("   "), TAB("\t");

        final String value;
        BadInput(String value) { this.value = value; }
    }

    @ParameterizedTest
    @EnumSource(BadInput.class)
    void enumDrivenBadInput(BadInput input) {
        assertThrows(IllegalArgumentException.class,
                     () -> auth.login(input.value, "secret123"));
    }
}
```

These tests demand new validation. **Red → Green:** add guards at the top of `login`:

```java
    public String login(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password must not be blank");
        }
        // ... rest unchanged
    }
```

| Source | Use it for |
|---|---|
| `@ValueSource` | One simple argument (String, int, long, double, boolean, Class) |
| `@NullSource` / `@EmptySource` / `@NullAndEmptySource` | The classic edge cases |
| `@CsvSource` | Several arguments, written inline |
| `@CsvFileSource` | Several arguments, loaded from a `.csv` on the classpath |
| `@MethodSource` | Anything — objects, streams, computed data |
| `@EnumSource` | Every constant of an enum |

### 4.4 `@RepeatedTest`, `@Disabled`, `@Tag`, assumptions

```java
package com.lab.auth;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class MiscAnnotationsTest {

    private AuthService auth;

    @BeforeEach
    void setUp() {
        SessionManager.getInstance().clear();
        auth = new AuthService(new InMemoryUserRepository());
    }

    // --- @RepeatedTest: catch flakiness and randomness bugs ---
    @RepeatedTest(value = 10, name = "token uniqueness run {currentRepetition}/{totalRepetitions}")
    @DisplayName("every login produces a unique token")
    void tokensAreUnique(RepetitionInfo info) {
        String token = auth.login("alice", "secret123");

        assertNotNull(token);
        assertEquals(1, SessionManager.getInstance().activeCount(),
                     "repetition " + info.getCurrentRepetition()
                     + " saw leftover state — @BeforeEach isn't doing its job");
    }

    // --- @Disabled: skip, with a reason. Never leave one un-explained. ---
    @Test
    @Disabled("Password hashing not implemented yet — see ticket AUTH-42")
    void passwordsAreStoredHashed() {
        fail("implement me");
    }

    // --- @Tag: split fast tests from slow ones ---
    @Test
    @Tag("fast")
    void fastLogin() {
        assertNotNull(auth.login("alice", "secret123"));
    }

    @Test
    @Tag("slow")
    @Tag("integration")
    void slowishScenario() {
        for (int i = 0; i < 1_000; i++) {
            String t = auth.login("alice", "secret123");
            auth.logout(t);
        }
        assertEquals(0, SessionManager.getInstance().activeCount());
    }

    // --- Assumptions: skip (not fail) when preconditions aren't met ---
    @Test
    void onlyRunsOnCiServer() {
        assumeTrue("true".equals(System.getenv("CI")),
                   "skipped: not running on CI");

        assertNotNull(auth.login("alice", "secret123"));
    }

    @Test
    void partOfTheTestIsConditional() {
        String token = auth.login("alice", "secret123");
        assertTrue(auth.isLoggedIn(token));

        assumingThat("true".equals(System.getenv("CI")), () -> {
            // extra checks that only make sense on CI
            assertEquals(1, SessionManager.getInstance().activeCount());
        });
    }
}
```

Run tags selectively:

```bash
mvn test -Dgroups=fast              # only @Tag("fast")
mvn test -DexcludedGroups=slow      # everything except @Tag("slow")
```

> **`assertFalse` vs `assumeFalse`:** an *assertion* that fails means **the code is broken**. An *assumption* that fails means **this test doesn't apply here** — it's reported as skipped, not failed.

### 4.5 `@TestMethodOrder` — and why you usually shouldn't

```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)   // honour @Order(n)
@TestMethodOrder(MethodOrderer.DisplayName.class)       // alphabetical by display name
@TestMethodOrder(MethodOrderer.MethodName.class)        // alphabetical by method name
@TestMethodOrder(MethodOrderer.Random.class)            // 🔥 shuffle — great for finding leaks
```

In Part 3 we used `@Order` as a **teaching device** to force the bug into the open. In production code, needing `@Order` is a smell: it means your tests are coupled.

**The exception — use `Random` deliberately.** Add this to a suspicious test class:

```java
@TestMethodOrder(MethodOrderer.Random.class)
class MySuite { ... }
```

If it fails intermittently, you have leaked state. Fix it with `@BeforeEach`.

---

## Part 5 — Mockito

### 5.1 Why we need it

`AuthService` currently depends on `InMemoryUserRepository`, which is real code. That means:

- You can't test "what if the database throws?" — the in-memory map never throws.
- Tokens are random UUIDs, so you can't assert on an exact value.
- You have no way to check that a security event was *logged*.

Mockito lets you replace a collaborator with a **test double** you fully control.

### 5.2 Vocabulary

| Term | Meaning |
|---|---|
| **Mock** | A fake object. Every method returns a default (`null`, `0`, `false`) until you stub it. |
| **Stub** | Teaching a mock what to return: `when(x.y()).thenReturn(z)` |
| **Verify** | Asserting a method *was called*: `verify(x).y()` |
| **Spy** | A wrapper around a **real** object; real methods run unless stubbed |
| **Argument captor** | Grabs the actual argument passed to a mock so you can assert on it |

### 5.3 Add two more collaborators

`TokenGenerator.java`:

```java
package com.lab.auth;

public interface TokenGenerator {
    String newToken();
}
```

`UuidTokenGenerator.java`:

```java
package com.lab.auth;

import java.util.UUID;

public class UuidTokenGenerator implements TokenGenerator {
    @Override
    public String newToken() {
        return UUID.randomUUID().toString();
    }
}
```

`AuditLogger.java`:

```java
package com.lab.auth;

public interface AuditLogger {
    void log(String event);
}
```

`ConsoleAuditLogger.java`:

```java
package com.lab.auth;

import java.time.LocalTime;

public class ConsoleAuditLogger implements AuditLogger {
    @Override
    public void log(String event) {
        System.out.println("[AUDIT " + LocalTime.now().withNano(0) + "] " + event);
    }
}
```

### 5.4 Final `AuthService`

```java
package com.lab.auth;

import java.time.Instant;

public class AuthService {

    private final UserRepository users;
    private final SessionManager sessions;
    private final TokenGenerator tokens;
    private final AuditLogger audit;

    public AuthService(UserRepository users,
                       SessionManager sessions,
                       TokenGenerator tokens,
                       AuditLogger audit) {
        this.users    = users;
        this.sessions = sessions;
        this.tokens   = tokens;
        this.audit    = audit;
    }

    /** Convenience constructor for the real application. */
    public AuthService(UserRepository users) {
        this(users,
             SessionManager.getInstance(),
             new UuidTokenGenerator(),
             new ConsoleAuditLogger());
    }

    public String login(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password must not be blank");
        }

        User user = users.findByUsername(username).orElse(null);

        if (user == null || !user.password().equals(password)) {
            audit.log("LOGIN_FAILED:" + username);
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = tokens.newToken();
        sessions.save(new Session(token, username, Instant.now()));
        audit.log("LOGIN_SUCCESS:" + username);
        return token;
    }

    public void logout(String token) {
        Session session = sessions.find(token);
        if (session == null) {
            audit.log("LOGOUT_FAILED:" + token);
            throw new NotLoggedInException("No active session for token: " + token);
        }
        sessions.remove(token);
        audit.log("LOGOUT_SUCCESS:" + session.getUsername());
    }

    public boolean isLoggedIn(String token) {
        return token != null && sessions.find(token) != null;
    }

    public String currentUser(String token) {
        Session session = sessions.find(token);
        if (session == null) {
            throw new NotLoggedInException("No active session for token: " + token);
        }
        return session.getUsername();
    }
}
```

> **Notice:** `SessionManager` is now *injected*, not fetched from the singleton inside the class. This is the real structural fix for Part 3's trap — but the singleton still exists for the console app, so `@BeforeEach` discipline still matters.

### 5.5 Your first mocks — manual style

```java
package com.lab.auth;

import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Mockito — the manual way")
class MockitoManualTest {

    private UserRepository users;
    private TokenGenerator tokens;
    private AuditLogger audit;
    private SessionManager sessions;
    private AuthService auth;

    @BeforeEach
    void setUp() {
        users    = mock(UserRepository.class);
        tokens   = mock(TokenGenerator.class);
        audit    = mock(AuditLogger.class);

        sessions = SessionManager.getInstance();
        sessions.clear();                       // ← still essential

        auth = new AuthService(users, sessions, tokens, audit);
    }

    @Test
    @DisplayName("stubbing: teach the mock what to return")
    void stubbing() {
        when(users.findByUsername("alice"))
            .thenReturn(Optional.of(new User("alice", "secret123")));
        when(tokens.newToken()).thenReturn("TOKEN-123");

        String token = auth.login("alice", "secret123");

        assertEquals("TOKEN-123", token);   // deterministic! no random UUID
    }

    @Test
    @DisplayName("un-stubbed mocks return sensible defaults")
    void defaults() {
        // We never stubbed findByUsername, so Mockito returns Optional.empty()
        // for Optional returns, null for objects, 0 for ints, false for booleans.
        assertThrows(InvalidCredentialsException.class,
                     () -> auth.login("ghost", "whatever"));
    }

    @Test
    @DisplayName("verify: assert the call happened")
    void verifyCalls() {
        when(users.findByUsername("alice"))
            .thenReturn(Optional.of(new User("alice", "secret123")));
        when(tokens.newToken()).thenReturn("T1");

        auth.login("alice", "secret123");

        verify(users).findByUsername("alice");
        verify(tokens).newToken();
        verify(audit).log("LOGIN_SUCCESS:alice");
    }

    @Test
    @DisplayName("verify times / never / atLeast")
    void verificationModes() {
        when(users.findByUsername("alice"))
            .thenReturn(Optional.of(new User("alice", "secret123")));
        when(tokens.newToken()).thenReturn("T1", "T2", "T3");   // consecutive returns

        auth.login("alice", "secret123");
        auth.login("alice", "secret123");

        verify(users, times(2)).findByUsername("alice");
        verify(users, never()).findByUsername("bob");
        verify(tokens, atLeast(1)).newToken();
        verify(tokens, atMost(5)).newToken();
        verify(audit, times(2)).log("LOGIN_SUCCESS:alice");
        verify(audit, never()).log(startsWithFailed());
    }

    private static String startsWithFailed() {
        return "LOGIN_FAILED:alice";
    }

    @Test
    @DisplayName("failed login is audited and issues no token")
    void failedLoginIsAudited() {
        when(users.findByUsername("alice"))
            .thenReturn(Optional.of(new User("alice", "secret123")));

        assertThrows(InvalidCredentialsException.class,
                     () -> auth.login("alice", "WRONG"));

        verify(audit).log("LOGIN_FAILED:alice");
        verify(tokens, never()).newToken();          // no token for a failed login
        assertEquals(0, sessions.activeCount());
    }
}
```

### 5.6 `@Mock`, `@InjectMocks` and `MockitoExtension`

Same tests, far less boilerplate.

```java
package com.lab.auth;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Mockito — annotation style")
class MockitoAnnotationTest {

    @Mock  private UserRepository users;
    @Mock  private TokenGenerator tokens;
    @Mock  private AuditLogger    audit;
    @Mock  private SessionManager sessions;     // even the singleton can be mocked

    @InjectMocks private AuthService auth;      // constructor-injected automatically

    private final User alice = new User("alice", "secret123");

    @Test
    @DisplayName("login saves a session")
    void loginSavesSession() {
        when(users.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(tokens.newToken()).thenReturn("T-1");

        String token = auth.login("alice", "secret123");

        assertEquals("T-1", token);
        verify(sessions).save(any(Session.class));
    }

    @Test
    @DisplayName("logout removes the session it found")
    void logoutRemovesSession() {
        Session live = new Session("T-1", "alice", java.time.Instant.now());
        when(sessions.find("T-1")).thenReturn(live);

        auth.logout("T-1");

        verify(sessions).remove("T-1");
        verify(audit).log("LOGOUT_SUCCESS:alice");
    }

    @Test
    @DisplayName("logout with no session never touches remove()")
    void logoutWithoutSession() {
        when(sessions.find("ghost")).thenReturn(null);

        assertThrows(NotLoggedInException.class, () -> auth.logout("ghost"));

        verify(sessions, never()).remove(anyString());
        verify(audit).log("LOGOUT_FAILED:ghost");
    }
}
```

> **`@ExtendWith(MockitoExtension.class)` runs strict stubs by default.** If you stub something and never use it, the test **fails** with `UnnecessaryStubbingException`. That's a feature — it stops dead stubs rotting in your suite. If a stub is only used by some tests in the class, move it out of `@BeforeEach` into the tests that need it, or use `@MockitoSettings(strictness = Strictness.LENIENT)`.

### 5.7 Argument matchers

```java
    @Test
    @DisplayName("argument matchers")
    void matchers() {
        when(users.findByUsername(anyString())).thenReturn(Optional.of(alice));
        when(tokens.newToken()).thenReturn("T-1");

        auth.login("alice", "secret123");

        verify(audit).log(contains("alice"));
        verify(audit).log(startsWith("LOGIN_SUCCESS"));
        verify(audit).log(matches("LOGIN_SUCCESS:\\w+"));
        verify(sessions).save(argThat(s -> s.getUsername().equals("alice")));
    }
```

⚠️ **The rule that catches everyone:** if you use a matcher for *one* argument, you must use matchers for *all* of them. Wrap literals in `eq()`:

```java
// ❌ InvalidUseOfMatchersException
verify(someMock).method(anyString(), "literal");

// ✅
verify(someMock).method(anyString(), eq("literal"));
```

### 5.8 `ArgumentCaptor` — inspect what was actually passed

`verify(...)` tells you a call happened. A captor hands you the **actual object** so you can assert on its contents.

```java
package com.lab.auth;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArgumentCaptor")
class CaptorTest {

    @Mock private UserRepository users;
    @Mock private TokenGenerator tokens;
    @Mock private AuditLogger    audit;
    @Mock private SessionManager sessions;

    @InjectMocks private AuthService auth;

    @Captor private ArgumentCaptor<Session> sessionCaptor;
    @Captor private ArgumentCaptor<String>  auditCaptor;

    @Test
    @DisplayName("capture the Session object handed to save()")
    void captureSession() {
        when(users.findByUsername("alice"))
            .thenReturn(Optional.of(new User("alice", "secret123")));
        when(tokens.newToken()).thenReturn("T-42");

        Instant before = Instant.now();
        auth.login("alice", "secret123");

        verify(sessions).save(sessionCaptor.capture());

        Session captured = sessionCaptor.getValue();
        assertAll(
            () -> assertEquals("T-42",  captured.getToken()),
            () -> assertEquals("alice", captured.getUsername()),
            () -> assertNotNull(captured.getCreatedAt()),
            () -> assertFalse(captured.getCreatedAt().isBefore(before))
        );
    }

    @Test
    @DisplayName("capture every audit message across a full login→logout flow")
    void captureAllAuditEvents() {
        when(users.findByUsername("alice"))
            .thenReturn(Optional.of(new User("alice", "secret123")));
        when(tokens.newToken()).thenReturn("T-42");
        when(sessions.find("T-42"))
            .thenReturn(new Session("T-42", "alice", Instant.now()));

        String token = auth.login("alice", "secret123");
        auth.logout(token);

        verify(audit, times(2)).log(auditCaptor.capture());

        List<String> events = auditCaptor.getAllValues();
        assertEquals(List.of("LOGIN_SUCCESS:alice", "LOGOUT_SUCCESS:alice"), events);
    }
}
```

`getValue()` returns the last captured value; `getAllValues()` returns them all in order.

### 5.9 Stubbing exceptions and behaviours

```java
    @Test
    @DisplayName("thenThrow: simulate a database outage")
    void repositoryFailure() {
        when(users.findByUsername("alice"))
            .thenThrow(new RuntimeException("DB connection refused"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                                           () -> auth.login("alice", "secret123"));

        assertEquals("DB connection refused", ex.getMessage());
        verify(sessions, never()).save(any());
    }

    @Test
    @DisplayName("doThrow: the only way to stub a void method")
    void voidMethodThrows() {
        doThrow(new IllegalStateException("audit backend down"))
            .when(audit).log(anyString());

        when(users.findByUsername("alice"))
            .thenReturn(Optional.of(new User("alice", "secret123")));
        when(tokens.newToken()).thenReturn("T-1");

        assertThrows(IllegalStateException.class,
                     () -> auth.login("alice", "secret123"));
    }

    @Test
    @DisplayName("consecutive returns: different answer each call")
    void consecutiveReturns() {
        when(tokens.newToken()).thenReturn("T-1", "T-2", "T-3");

        assertEquals("T-1", tokens.newToken());
        assertEquals("T-2", tokens.newToken());
        assertEquals("T-3", tokens.newToken());
        assertEquals("T-3", tokens.newToken());   // last value repeats forever
    }

    @Test
    @DisplayName("thenAnswer: compute the return value from the arguments")
    void dynamicAnswer() {
        when(users.findByUsername(anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);
            return name.startsWith("valid")
                 ? Optional.of(new User(name, "pw"))
                 : Optional.empty();
        });

        assertTrue(users.findByUsername("valid-user").isPresent());
        assertTrue(users.findByUsername("nope").isEmpty());
    }
```

### 5.10 `InOrder` — verify the sequence

Order matters for security auditing: the session must be destroyed **before** the success is logged.

```java
    @Test
    @DisplayName("logout: session removed before the success is logged")
    void orderOfOperations() {
        when(sessions.find("T-1"))
            .thenReturn(new Session("T-1", "alice", Instant.now()));

        auth.logout("T-1");

        InOrder inOrder = inOrder(sessions, audit);
        inOrder.verify(sessions).find("T-1");
        inOrder.verify(sessions).remove("T-1");
        inOrder.verify(audit).log("LOGOUT_SUCCESS:alice");
        inOrder.verifyNoMoreInteractions();
    }
```

### 5.11 Spies — a real object you can partially fake

A mock is hollow. A spy is a **real object** whose methods actually run, unless you stub them.

```java
package com.lab.auth;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Spies")
class SpyTest {

    @Test
    @DisplayName("a spy keeps real behaviour and records calls")
    void spyKeepsRealBehaviour() {
        SessionManager real  = SessionManager.getInstance();
        real.clear();
        SessionManager spy = spy(real);

        spy.save(new Session("T-1", "alice", java.time.Instant.now()));

        assertEquals(1, spy.activeCount());          // ← real logic ran
        verify(spy).save(any(Session.class));        // ← and it was recorded
    }

    @Test
    @DisplayName("partial stubbing: override one method, keep the rest")
    void partialStub() {
        InMemoryUserRepository real = new InMemoryUserRepository();
        InMemoryUserRepository spy  = spy(real);

        // Pretend alice was deleted, while bob still works normally.
        doReturn(java.util.Optional.empty()).when(spy).findByUsername("alice");

        assertTrue(spy.findByUsername("alice").isEmpty());   // stubbed
        assertTrue(spy.findByUsername("bob").isPresent());   // real
    }
}
```

> **`doReturn().when(spy)` vs `when(spy...).thenReturn()`:** with a spy, `when(spy.findByUsername("alice"))` **actually calls the real method** while setting up the stub. If that method throws or has side effects, you're in trouble. Use the `doReturn(...).when(spy).method(...)` form for spies.

### 5.12 Mockito quick reference

| Goal | Code |
|---|---|
| Create a mock | `mock(Foo.class)` or `@Mock` |
| Auto-inject mocks | `@InjectMocks` + `@ExtendWith(MockitoExtension.class)` |
| Stub a return | `when(m.f()).thenReturn(v)` |
| Stub consecutive returns | `when(m.f()).thenReturn(a, b, c)` |
| Stub an exception | `when(m.f()).thenThrow(ex)` |
| Stub a **void** method | `doThrow(ex).when(m).f()` / `doNothing().when(m).f()` |
| Compute the answer | `when(m.f(any())).thenAnswer(inv -> ...)` |
| Verify a call | `verify(m).f()` |
| Verify a count | `verify(m, times(2)).f()` |
| Verify absence | `verify(m, never()).f()` |
| Verify nothing else happened | `verifyNoMoreInteractions(m)` |
| Verify order | `InOrder o = inOrder(a, b); o.verify(a).x(); o.verify(b).y();` |
| Capture an argument | `@Captor ArgumentCaptor<T> c;` then `verify(m).f(c.capture())` |
| Real object, recorded | `spy(realObject)` |
| Reset a mock | `reset(m)` — usually a smell; prefer a fresh mock in `@BeforeEach` |

---

## Part 6 — Hamcrest

JUnit's `assertEquals` gives you pass/fail. Hamcrest gives you **readable intent** and **failure messages that explain themselves**.

```java
package com.lab.auth;

import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Hamcrest matchers")
class HamcrestTest {

    private AuthService auth;

    @BeforeEach
    void setUp() {
        SessionManager.getInstance().clear();
        auth = new AuthService(new InMemoryUserRepository(),
                               SessionManager.getInstance(),
                               new UuidTokenGenerator(),
                               event -> { /* no-op audit logger */ });
    }

    @Test
    @DisplayName("core matchers")
    void coreMatchers() {
        String token = auth.login("alice", "secret123");

        assertThat(token, is(notNullValue()));
        assertThat(token, not(emptyString()));
        assertThat(token.length(), is(greaterThan(10)));
        assertThat(auth.isLoggedIn(token), is(true));
        assertThat(auth.currentUser(token), is(equalTo("alice")));
    }

    @Test
    @DisplayName("string matchers")
    void stringMatchers() {
        String token = auth.login("alice", "secret123");
        Session session = SessionManager.getInstance().find(token);

        assertThat(session.toString(), containsString("alice"));
        assertThat(session.toString(), startsWith("Session{"));
        assertThat(session.toString(), endsWith("}"));
        assertThat(token, matchesPattern("[0-9a-f-]{36}"));
    }

    @Test
    @DisplayName("collection matchers")
    void collectionMatchers() {
        auth.login("alice", "secret123");
        auth.login("bob", "hunter2");

        var names = SessionManager.getInstance().activeUsernames();

        assertThat(names, hasSize(2));
        assertThat(names, hasItem("alice"));
        assertThat(names, hasItems("alice", "bob"));
        assertThat(names, containsInAnyOrder("bob", "alice"));
        assertThat(names, not(hasItem("mallory")));
        assertThat(names, everyItem(not(emptyString())));
    }

    @Test
    @DisplayName("combining matchers: allOf / anyOf")
    void combining() {
        String token = auth.login("alice", "secret123");

        assertThat(token, allOf(
            notNullValue(),
            not(emptyString()),
            hasLength(36)
        ));

        assertThat(auth.currentUser(token), anyOf(is("alice"), is("bob")));
    }

    @Test
    @DisplayName("numeric and comparable matchers")
    void numbers() {
        auth.login("alice", "secret123");

        int count = SessionManager.getInstance().activeCount();

        assertThat(count, is(1));
        assertThat(count, greaterThanOrEqualTo(1));
        assertThat(count, lessThan(10));
        assertThat(count, both(greaterThan(0)).and(lessThan(5)));
    }

    @Test
    @DisplayName("after logout the session store is empty")
    void afterLogout() {
        String token = auth.login("alice", "secret123");
        auth.logout(token);

        assertThat(SessionManager.getInstance().activeCount(), is(0));
        assertThat(SessionManager.getInstance().activeUsernames(), is(empty()));
        assertThat(auth.isLoggedIn(token), is(false));
    }
}
```

### 6.1 Why the failure message matters

```java
// JUnit
assertEquals(2, names.size());
// → expected: <2> but was: <1>          ...which element is missing?

// Hamcrest
assertThat(names, containsInAnyOrder("alice", "bob"));
// → Expected: iterable with items ["alice", "bob"] in any order
//        but: no item matches: "bob" in ["alice"]
```

The Hamcrest message tells you *what* is wrong, not just *that* something is.

### 6.2 Writing a custom matcher

```java
package com.lab.auth;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class SessionMatchers {

    public static TypeSafeMatcher<Session> belongsTo(String username) {
        return new TypeSafeMatcher<>() {

            @Override
            protected boolean matchesSafely(Session session) {
                return username.equals(session.getUsername());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a session belonging to ").appendValue(username);
            }

            @Override
            protected void describeMismatchSafely(Session session, Description mismatch) {
                mismatch.appendText("was a session belonging to ")
                        .appendValue(session.getUsername());
            }
        };
    }
}
```

Usage:

```java
assertThat(session, belongsTo("alice"));

// Failure output:
// Expected: a session belonging to "alice"
//      but: was a session belonging to "bob"
```

### 6.3 Hamcrest cheat sheet

| Category | Matchers |
|---|---|
| Core | `is`, `not`, `equalTo`, `nullValue`, `notNullValue`, `sameInstance`, `instanceOf` |
| Logical | `allOf`, `anyOf`, `both().and()`, `either().or()` |
| String | `containsString`, `startsWith`, `endsWith`, `emptyString`, `equalToIgnoringCase`, `matchesPattern`, `hasLength` |
| Number | `greaterThan`, `lessThan`, `greaterThanOrEqualTo`, `closeTo` |
| Collection | `hasSize`, `hasItem`, `hasItems`, `contains`, `containsInAnyOrder`, `empty`, `everyItem` |
| Map | `hasKey`, `hasValue`, `hasEntry`, `anEmptyMap` |
| Object | `hasProperty`, `hasToString` |

---

## Part 7 — The Console Application

`ConsoleApp.java`:

```java
package com.lab.auth;

import java.util.Scanner;

public class ConsoleApp {

    private final AuthService auth;
    private final Scanner in = new Scanner(System.in);

    private String currentToken = null;

    public ConsoleApp(AuthService auth) {
        this.auth = auth;
    }

    public static void main(String[] args) {
        AuthService auth = new AuthService(new InMemoryUserRepository());
        new ConsoleApp(auth).run();
    }

    public void run() {
        System.out.println("=== Login Lab ===");
        System.out.println("Try: alice/secret123  or  bob/hunter2\n");

        boolean running = true;
        while (running) {
            printMenu();
            switch (in.nextLine().trim()) {
                case "1" -> doLogin();
                case "2" -> doLogout();
                case "3" -> doStatus();
                case "4" -> running = false;
                default  -> System.out.println("Unknown option.\n");
            }
        }
        System.out.println("Bye.");
    }

    private void printMenu() {
        String who = (currentToken == null) ? "guest" : auth.currentUser(currentToken);
        System.out.println("--- logged in as: " + who + " ---");
        System.out.println("1) Login   2) Logout   3) Status   4) Exit");
        System.out.print("> ");
    }

    private void doLogin() {
        if (currentToken != null) {
            System.out.println("Already logged in. Log out first.\n");
            return;
        }
        System.out.print("username: ");
        String username = in.nextLine();
        System.out.print("password: ");
        String password = in.nextLine();

        try {
            currentToken = auth.login(username, password);
            System.out.println("✅ Logged in. Token: " + currentToken + "\n");
        } catch (IllegalArgumentException | InvalidCredentialsException e) {
            System.out.println("❌ " + e.getMessage() + "\n");
        }
    }

    private void doLogout() {
        try {
            auth.logout(currentToken);
            System.out.println("✅ Logged out. Session destroyed.\n");
            currentToken = null;
        } catch (NotLoggedInException e) {
            System.out.println("❌ " + e.getMessage() + "\n");
        }
    }

    private void doStatus() {
        System.out.println("Active sessions in cache: "
                           + SessionManager.getInstance().activeCount());
        System.out.println("Logged-in users: "
                           + SessionManager.getInstance().activeUsernames() + "\n");
    }
}
```

Run it:

```bash
mvn -q compile exec:java -Dexec.mainClass=com.lab.auth.ConsoleApp
```

Try this sequence to feel the bug from the user's side: **Logout (option 2) before logging in.** You get `No active session for token: null`. That is exactly the failure your out-of-order test reproduced. The test wasn't being pedantic — it found a real behaviour.

---

## Part 8 — Exercises

### Exercise 1 — TDD a session limit 🟢

Using strict Red-Green-Refactor, add: **a user may have at most 3 active sessions.** A fourth login throws `TooManySessionsException`.

1. Write the failing test first.
2. Make it pass with the simplest code.
3. Refactor.
4. Add a `@ParameterizedTest` covering 1, 2, 3 and 4 concurrent logins.

### Exercise 2 — Session expiry with a mocked clock 🟡

Add an injectable `java.time.Clock` to `AuthService`. Sessions expire after 30 minutes.

- `isLoggedIn` returns `false` for an expired session.
- Test it by mocking `Clock` so you don't have to wait half an hour.

```java
Clock clock = mock(Clock.class);
when(clock.instant()).thenReturn(
    Instant.parse("2026-01-01T10:00:00Z"),   // at login
    Instant.parse("2026-01-01T10:31:00Z")    // 31 minutes later
);
```

Use `ArgumentCaptor` to confirm the session was stored with the first timestamp.

### Exercise 3 — Account lockout 🟡

After 3 consecutive failed logins, lock the account for 15 minutes.

- Verify with `InOrder` that each failure is audited before the lockout event.
- Use `verify(audit, times(3)).log(startsWith("LOGIN_FAILED"))`.
- Use Hamcrest to assert the lockout message contains both the username and the duration.

### Exercise 4 — Break it on purpose 🔴

1. Delete the `SessionManager.getInstance().clear()` line from a `@BeforeEach`.
2. Add `@TestMethodOrder(MethodOrderer.Random.class)` to the class.
3. Run `mvn test` ten times.
4. Record how many runs pass and how many fail.
5. Restore the line. Run ten times again.

Write two sentences explaining what you observed. This is the single most valuable exercise in the lab.

### Exercise 5 — Custom Hamcrest matcher 🟡

Write `isActiveSessionFor(String username)` that checks the session exists, belongs to that user, and is not expired. Give it a mismatch description that says *which* condition failed.

---

## Appendix A — The takeaway table

| Symptom | Cause | Fix |
|---|---|---|
| Passes alone, fails in the suite | Another test left state behind | `@BeforeEach` reset |
| Passes in the suite, fails alone | This test depends on another test's setup | Each test builds its own fixture |
| Passes locally, fails on CI | Order differs between JVMs | Remove order dependency |
| Fails intermittently | Shared mutable state / randomness | Reset state; `@RepeatedTest` to expose it |
| `UnnecessaryStubbingException` | Stub defined but never exercised | Move the stub into the test that needs it |
| `InvalidUseOfMatchersException` | Mixed raw values with matchers | Wrap literals in `eq(...)` |
| Spy runs real code during stubbing | Used `when(spy.x())` | Use `doReturn(v).when(spy).x()` |

## Appendix B — Command reference

```bash
mvn test                                  # everything
mvn test -Dtest=AuthServiceNestedTest     # one class
mvn test -Dtest=AuthServiceNestedTest#loginStartsSession   # one method
mvn test -Dtest='*Mockito*'               # pattern
mvn test -Dgroups=fast                    # by @Tag
mvn test -DexcludedGroups=slow
mvn -q compile exec:java -Dexec.mainClass=com.lab.auth.ConsoleApp
```

## Appendix C — The one-sentence summary

> Login/logout is trivial code, but the moment two tests share one session cache, the order they run in decides whether they pass — and `@BeforeEach` is the annotation that takes that decision away from luck and gives it back to you.
