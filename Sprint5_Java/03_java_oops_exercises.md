# Java OOP — Incremental Practice Exercises

Each exercise focuses on **one concept only**. Read the buggy code, figure out
why it fails or behaves incorrectly, *then* check the solution and explanation.

Difficulty increases gradually — do them in order.

---

## Exercise 1: Classes & Objects (basic instantiation)

**Concept:** Creating and using objects.

```java
public class Student {
    String name;
    int age;
}

public class Main {
    public static void main(String[] args) {
        Student s1;
        s1.name = "Alex";
        s1.age = 20;
        System.out.println(s1.name);
    }
}
```

**Task:** This won't even compile. Why?

<details>
<summary>Solution</summary>

```java
public class Main {
    public static void main(String[] args) {
        Student s1 = new Student();   // object must be created with 'new'
        s1.name = "Alex";
        s1.age = 20;
        System.out.println(s1.name);
    }
}
```

**Explanation:** `Student s1;` only *declares a reference variable* — it doesn't
create an object. `s1` is `null` until you call `new Student()`, which
allocates memory and gives you an actual object to point to. Accessing
`s1.name` before that throws a `NullPointerException` (or won't compile in
some cases, since the compiler flags the uninitialized variable).

</details>

---

## Exercise 2: Constructors

**Concept:** Default vs parameterized constructors.

```java
public class Book {
    String title;

    public Book(String title) {
        this.title = title;
    }
}

public class Main {
    public static void main(String[] args) {
        Book b = new Book();   // ???
        System.out.println(b.title);
    }
}
```

**Task:** Why does this fail to compile?

<details>
<summary>Solution</summary>

```java
public class Book {
    String title;

    public Book() {                 // add a no-arg constructor
        this.title = "Untitled";
    }

    public Book(String title) {
        this.title = title;
    }
}
```

**Explanation:** Java only gives you a free "default" (no-arg) constructor if
you **define no constructors at all**. The moment you write *any* constructor
(here, `Book(String title)`), the implicit default constructor disappears. If
you still want `new Book()` to work, you must write that constructor
yourself.

</details>

---

## Exercise 3: The `this` Keyword

**Concept:** Disambiguating fields from parameters.

```java
public class Rectangle {
    int width;
    int height;

    public Rectangle(int width, int height) {
        width = width;
        height = height;
    }

    int area() {
        return width * height;
    }
}
```

**Task:** `new Rectangle(5, 10).area()` returns `0`. Why?

<details>
<summary>Solution</summary>

```java
public Rectangle(int width, int height) {
    this.width = width;
    this.height = height;
}
```

**Explanation:** Inside the constructor, the parameter `width` **shadows**
the field `width`. The line `width = width;` just assigns the parameter to
itself — the object's field is never touched and stays at its default value
`0`. `this.width` explicitly refers to the instance field, resolving the
ambiguity.

</details>

---

## Exercise 4: Array of Objects

**Concept:** Each array slot needs its own object.

```java
public class Student {
    String name;
    Student(String name) { this.name = name; }
}

public class Main {
    public static void main(String[] args) {
        Student[] students = new Student[3];
        students[0].name = "Amy";
        students[1].name = "Ben";
        students[2].name = "Cid";

        for (Student s : students) {
            System.out.println(s.name);
        }
    }
}
```

**Task:** This throws a `NullPointerException`. Why, and how do you fix it?

<details>
<summary>Solution</summary>

```java
public class Main {
    public static void main(String[] args) {
        Student[] students = new Student[3];
        students[0] = new Student("Amy");
        students[1] = new Student("Ben");
        students[2] = new Student("Cid");

        for (Student s : students) {
            System.out.println(s.name);
        }
    }
}
```

**Explanation:** `new Student[3]` only creates an array of **3 reference
slots**, each initialized to `null` — it does NOT create 3 `Student` objects.
You must explicitly call `new Student(...)` for every slot before using it.
This is one of the most common beginner mistakes with arrays of objects
(as opposed to arrays of primitives like `int[]`, which auto-initialize to
`0`).

</details>

---

## Exercise 5: Encapsulation (private fields + getters/setters)

**Concept:** Why public fields are dangerous.

```java
public class BankAccount {
    public double balance;

    public BankAccount(double balance) {
        this.balance = balance;
    }
}

public class Main {
    public static void main(String[] args) {
        BankAccount acc = new BankAccount(1000);
        acc.balance = -500;   // uh oh
        System.out.println(acc.balance);
    }
}
```

**Task:** This compiles and runs fine — that's exactly the problem. What
OOP principle is being violated, and how would you fix the class?

<details>
<summary>Solution</summary>

```java
public class BankAccount {
    private double balance;               // 1. hide the field

    public BankAccount(double balance) {
        this.balance = balance;
    }

    public double getBalance() {          // 2. controlled read access
        return balance;
    }

    public void deposit(double amount) {  // 3. controlled write access
        if (amount > 0) balance += amount;
    }

    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) balance -= amount;
    }
}
```

**Explanation:** This is **encapsulation**: hide internal state (`private`)
and expose only the operations that make sense (`deposit`, `withdraw`)
instead of letting anyone set `balance` to any arbitrary value, including
invalid ones like `-500`. The class itself is now responsible for protecting
its own invariants.

</details>

---

## Exercise 6: Static vs Instance Members

**Concept:** Shared state vs per-object state.

```java
public class Counter {
    int count = 0;

    void increment() {
        count++;
    }
}

public class Main {
    public static void main(String[] args) {
        Counter c1 = new Counter();
        Counter c2 = new Counter();
        c1.increment();
        c1.increment();
        c2.increment();
        // Requirement: track TOTAL increments across ALL Counter objects
        System.out.println("Total: " + c1.count); // wrong, only shows c1's count
    }
}
```

**Task:** How do you track a total shared across every `Counter` instance?

<details>
<summary>Solution</summary>

```java
public class Counter {
    static int totalCount = 0;   // shared across all instances
    int count = 0;               // unique to each instance

    void increment() {
        count++;
        totalCount++;
    }
}

public class Main {
    public static void main(String[] args) {
        Counter c1 = new Counter();
        Counter c2 = new Counter();
        c1.increment();
        c1.increment();
        c2.increment();
        System.out.println("Total: " + Counter.totalCount); // 3
    }
}
```

**Explanation:** Instance fields (`count`) belong to each object separately —
`c1` and `c2` each get their own copy. `static` fields belong to the **class
itself**, so there's exactly one copy shared by every object. Use `static`
when data needs to be common/shared (counters, configuration, constants);
use instance fields when each object needs its own independent value.

</details>

---

## Exercise 7: Inheritance — Field/Method Access

**Concept:** What subclasses inherit and what they don't.

**Scenario:** An HR system has a generic `Employee` and a specialized
`Manager` who also needs to show the team size in their sound-bite... er,
their profile summary.

```java
public class Employee {
    private String name;

    public Employee(String name) {
        this.name = name;
    }

    void printProfile() {
        System.out.println("Employee: " + name);
    }
}

public class Manager extends Employee {
    int teamSize;

    public Manager(String name, int teamSize) {
        super(name);
        this.teamSize = teamSize;
    }

    void printProfile() {
        System.out.println("Manager: " + name + ", team size: " + teamSize); // compile error
    }
}
```

**Task:** `Manager` can't compile. Why can't it access `name`?

<details>
<summary>Solution</summary>

```java
public class Employee {
    protected String name;    // change from private to protected

    public Employee(String name) {
        this.name = name;
    }

    void printProfile() {
        System.out.println("Employee: " + name);
    }
}
```

**Explanation:** `private` members are visible **only inside the class that
declares them** — not even subclasses can see them directly. `Manager`
inherits the *concept* of having a name, but it can't reach into
`Employee`'s private field directly. Raising the visibility to `protected`
makes it visible to subclasses (and classes in the same package) while
still hiding it from unrelated outside code — the usual middle ground used
in inheritance hierarchies.

</details>

---

## Exercise 8: Constructor Chaining with `super()`

**Concept:** Superclass constructors must run first.

**Scenario:** Building on the HR system — every `Manager` is still an
`Employee`, so their construction has to start with the `Employee` part
being initialized first.

```java
public class Employee {
    String name;
    String department;

    public Employee(String name, String department) {
        this.name = name;
        this.department = department;
        System.out.println("Employee record created: " + name);
    }
}

public class Manager extends Employee {
    int teamSize;

    public Manager(String name, String department, int teamSize) {
        this.teamSize = teamSize;   // missing something before this line
    }
}
```

**Task:** This fails to compile. What's missing?

<details>
<summary>Solution</summary>

```java
public class Manager extends Employee {
    int teamSize;

    public Manager(String name, String department, int teamSize) {
        super(name, department);   // must call the superclass constructor first
        this.teamSize = teamSize;
    }
}
```

**Explanation:** Every constructor's **first statement** must either call
another constructor in the same class (`this(...)`) or a constructor in the
superclass (`super(...)`). If you don't write one, Java silently inserts a
no-arg `super()` call. But `Employee` has no no-arg constructor (only
`Employee(String, String)`), so the implicit call fails to compile, and you
must call `super(name, department)` explicitly. The superclass portion of
the object (`Employee`'s fields) must be fully initialized before the
subclass (`Manager`) adds its own state on top.

</details>

---

## Exercise 9: Method Overriding vs Overloading

**Concept:** Recognizing when you've overloaded instead of overridden.

**Scenario:** A payroll module has a base `SalaryCalculator` and a
`BonusSalaryCalculator` that was *supposed* to change how the final salary is
computed, but a signature mismatch quietly broke that intention.

```java
public class SalaryCalculator {
    double calculateSalary() {
        return 50000;
    }
}

public class BonusSalaryCalculator extends SalaryCalculator {
    double bonus = 5000;

    double calculateSalary(double base) {      // intended to override
        return base + bonus;
    }
}

public class Main {
    public static void main(String[] args) {
        SalaryCalculator calc = new BonusSalaryCalculator();
        System.out.println(calc.calculateSalary()); // prints 50000, no bonus applied
    }
}
```

**Task:** Why does `calc.calculateSalary()` ignore the bonus?

<details>
<summary>Solution</summary>

```java
public class BonusSalaryCalculator extends SalaryCalculator {
    double bonus = 5000;

    @Override
    double calculateSalary() {                 // same signature as parent -> true override
        return 50000 + bonus;
    }
}
```

**Explanation:** `calculateSalary(double base)` in `BonusSalaryCalculator`
has a **different signature** (it takes a parameter) than
`SalaryCalculator`'s `calculateSalary()`. That makes it a brand-new,
*overloaded* method — not an override. So `calc.calculateSalary()` (no
arguments) still calls the parent's version and the bonus logic never runs.
Always use `@Override` — the compiler immediately flags an error if the
signature doesn't actually match a parent method, catching this exact
mistake before it reaches payroll.

</details>

---

## Exercise 10: Polymorphism (Dynamic Method Dispatch)

**Concept:** Which method actually runs at runtime.

**Scenario:** A notification system sends alerts through different
channels. The code is written against the general `NotificationService`
type so new channels can be added later without touching calling code.

```java
public class NotificationService {
    void send(String message) {
        System.out.println("Sending generic notification: " + message);
    }
}

public class EmailNotificationService extends NotificationService {
    void send(String message) {
        System.out.println("Sending EMAIL: " + message);
    }
}

public class Main {
    public static void main(String[] args) {
        NotificationService service = new EmailNotificationService();
        service.send("Server down!");  // what gets printed, and why does it surprise beginners?
    }
}
```

**Task:** Predict the output. Then explain *why* it isn't `"Sending generic
notification..."` even though the variable's declared type is
`NotificationService`.

<details>
<summary>Solution</summary>

Output: `Sending EMAIL: Server down!`

**Explanation:** This is **runtime polymorphism**. Java decides which
overridden method to call based on the object's **actual runtime type**
(`EmailNotificationService`), not the reference's **compile-time/declared
type** (`NotificationService`). This is exactly why this pattern is used in
real systems: application code can call `service.send(...)` without caring
whether it's actually an email, SMS, or Slack notifier under the hood — the
correct behavior is picked automatically at runtime based on which concrete
class was instantiated.

</details>

---

## Exercise 11: Abstraction (Abstract Classes)

**Concept:** Why you can't instantiate an abstract class, and what
subclasses are forced to do.

**Scenario:** A reporting module needs to support multiple report formats,
but every report shares the same overall generation flow.

```java
public abstract class Report {
    abstract void generateContent();

    void export() {
        System.out.println("Starting export...");
        generateContent();
        System.out.println("Export complete.");
    }
}

public class Main {
    public static void main(String[] args) {
        Report r = new Report();   // line 1
        r.export();
    }
}

class PdfReport extends Report {
    // no generateContent() implementation   // line 2
}
```

**Task:** Two separate problems exist here (marked line 1 and line 2). Find
both.

<details>
<summary>Solution</summary>

```java
public abstract class Report {
    abstract void generateContent();

    void export() {
        System.out.println("Starting export...");
        generateContent();
        System.out.println("Export complete.");
    }
}

class PdfReport extends Report {
    @Override
    void generateContent() {
        System.out.println("Writing content into PDF format...");
    }
}

public class Main {
    public static void main(String[] args) {
        Report r = new PdfReport();  // instantiate a concrete subclass instead
        r.export();
    }
}
```

**Explanation:**
- **Line 1:** You can never do `new Report()` when `Report` is `abstract` —
  abstract classes are incomplete by design (they may have methods with no
  body) and Java forbids creating objects from them directly.
- **Line 2:** Any *concrete* (non-abstract) subclass of an abstract class
  **must** implement every abstract method it inherits, or it must also be
  declared `abstract` itself. `PdfReport` was concrete but skipped
  `generateContent()`, which is not allowed.

Abstraction lets you define a fixed process (`export()`: start, generate,
finish) while letting each report type fill in only the part that differs —
a common real-world pattern called the **Template Method** pattern.

</details>

---

## Exercise 12: Interfaces vs Abstract Classes

**Concept:** Implementing multiple interfaces; interface method rules.

**Scenario:** In a payroll system, some workers are `Payable` (get a
salary) and separately `Taxable` (have tax deducted) — two independent
capabilities a single `Employee` class needs to support at once.

```java
public interface Payable {
    void pay();
}

public interface Taxable {
    double calculateTax();
}

public class Employee implements Payable, Taxable {
    public void pay() {
        System.out.println("Salary paid");
    }
    // calculateTax() not implemented
}
```

**Task:** Why won't `Employee` compile, and what are the two ways to fix it?

<details>
<summary>Solution</summary>

**Option A — implement the missing method:**
```java
public class Employee implements Payable, Taxable {
    public void pay() {
        System.out.println("Salary paid");
    }
    public double calculateTax() {
        return 5000.0;
    }
}
```

**Option B — make Employee abstract:**
```java
public abstract class Employee implements Payable, Taxable {
    public void pay() { System.out.println("Salary paid"); }
    // calculateTax() left abstract, deferred to a further subclass like Contractor
}
```

**Explanation:** A class that `implements` an interface signs a contract to
provide a real method body for **every** abstract method in that interface
(unless it's a `default` method). `Employee` implemented `Payable` fully but
ignored `Taxable.calculateTax()`, breaking the contract. Unlike single
inheritance with `extends`, a class **can implement multiple interfaces** —
this is exactly how real payroll/HR systems model workers who need several
independent capabilities (payable, taxable, insurable, etc.) at once.

</details>

---

## Exercise 13: Compile-Time Polymorphism — Overload Resolution

**Concept:** How Java picks *which* overloaded method to call, based on
argument types known at compile time.

```java
public class InvoiceService {
    void process(int amount) {
        System.out.println("Processing int amount: " + amount);
    }

    void process(double amount) {
        System.out.println("Processing double amount: " + amount);
    }

    void process(String amount) {
        System.out.println("Processing string amount: " + amount);
    }
}

public class Main {
    public static void main(String[] args) {
        InvoiceService service = new InvoiceService();
        long invoiceAmount = 5000L;
        service.process(invoiceAmount);   // which overload runs, and why?
    }
}
```

**Task:** There's no `process(long)` overload. Which method actually gets
called, and why doesn't this throw a compile error?

<details>
<summary>Solution</summary>

Output: `Processing double amount: 5000.0`

**Explanation:** This is **compile-time (static) polymorphism** —
overload resolution happens entirely at compile time, based on the
argument's **declared type**. Java looks for an exact match (`process(long)`)
first; since none exists, it looks for the closest **widening primitive
conversion**. `long` can widen to `float` or `double` (but not narrow to
`int`), so the compiler picks `process(double)`. If you added a
`process(long)` overload, that exact match would win instead. The key
takeaway: overload resolution is decided by the compiler using the static
type of the arguments, not any runtime behavior.

</details>

---

## Exercise 14: Compile-Time Polymorphism — Ambiguous Overloads

**Concept:** Autoboxing and varargs can make an overload call ambiguous.

```java
public class ReportPrinter {
    void print(Integer id) {
        System.out.println("Printing by Integer id: " + id);
    }

    void print(long id) {
        System.out.println("Printing by long id: " + id);
    }
}

public class Main {
    public static void main(String[] args) {
        ReportPrinter printer = new ReportPrinter();
        printer.print(10);   // which one runs?
    }
}
```

**Task:** Predict which `print` overload is called for `printer.print(10)`,
where `10` is a plain `int` literal.

<details>
<summary>Solution</summary>

Output: `Printing by long id: 10`

**Explanation:** Java's overload resolution happens in phases:
1. First, try an exact match or a **widening primitive conversion**
   (`int -> long` here) with **no boxing**.
2. Only if phase 1 fails, try **autoboxing/unboxing** (`int -> Integer`).
3. Only if that also fails, try varargs.

Since `int` can widen to `long` without any boxing, `print(long id)` wins
over `print(Integer id)`, even though boxing to `Integer` might feel like
the "closer" match. This is a classic gotcha: **widening beats boxing** in
Java's overload resolution rules. Being explicit — e.g. calling
`printer.print(Integer.valueOf(10))` — is the only way to force the boxed
overload.

</details>

---

## Exercise 15: Runtime Polymorphism — Fields Are NOT Polymorphic

**Concept:** Overriding applies to methods, not fields.

```java
public class Employee {
    String role = "Employee";
}

public class Manager extends Employee {
    String role = "Manager";
}

public class Main {
    public static void main(String[] args) {
        Employee e = new Manager();
        System.out.println(e.role);   // expected "Manager", prints "Employee"
    }
}
```

**Task:** Why does `e.role` print `"Employee"` even though the actual
object is a `Manager`?

<details>
<summary>Solution</summary>

```java
public class Employee {
    private String role = "Employee";

    String getRole() {
        return role;
    }
}

public class Manager extends Employee {
    // no separate 'role' field — override behavior via a method instead
    @Override
    String getRole() {
        return "Manager";
    }
}

// Main:
Employee e = new Manager();
System.out.println(e.getRole());  // "Manager", as expected
```

**Explanation:** Fields in Java are resolved by the **declared/reference
type** at compile time — this is called **field hiding**, not overriding.
Since `e` is declared as `Employee`, `e.role` always reads `Employee`'s
`role` field, no matter what the actual runtime object is. Only
**methods** participate in true runtime polymorphism (dynamic dispatch).
This is exactly why encapsulation matters: expose state through methods
(`getRole()`), not raw public/inherited fields, so runtime polymorphism
actually works as intended.

</details>

---

## Exercise 16: Runtime Polymorphism — Calling the Parent's Version with `super`

**Concept:** Extending, not replacing, inherited behavior.

```java
public class Employee {
    void printDetails() {
        System.out.println("Name: John");
        System.out.println("Role: Employee");
    }
}

public class Manager extends Employee {
    int teamSize = 6;

    @Override
    void printDetails() {
        // Requirement: keep printing Name/Role from Employee, then ADD team size
        System.out.println("Team size: " + teamSize);
    }
}
```

**Task:** `Manager`'s `printDetails()` now only prints the team size and
loses the `Name`/`Role` lines that `Employee` used to print. How do you
keep both, without duplicating the `Employee` printing logic?

<details>
<summary>Solution</summary>

```java
public class Manager extends Employee {
    int teamSize = 6;

    @Override
    void printDetails() {
        super.printDetails();   // run the parent's version first
        System.out.println("Team size: " + teamSize);
    }
}
```

**Explanation:** Overriding a method completely **replaces** the parent's
version when called through a `Manager` object — the parent code doesn't
run automatically just because it exists. `super.printDetails()` explicitly
invokes the superclass's implementation from within the override, letting
you **extend** behavior (reuse + add) instead of duplicating the original
print statements. This pattern is extremely common in real codebases:
override a method to add extra behavior while still calling `super` for the
shared base logic.

</details>

---

## Exercise 17: Exception Handling — Unchecked vs Checked Exceptions

**Concept:** Why some exceptions force you to handle them and others don't.

```java
import java.io.FileReader;
import java.io.IOException;

public class ConfigLoader {
    void loadConfig(String path) {
        FileReader reader = new FileReader(path);   // compile error
        System.out.println("Config loaded");
    }
}
```

**Task:** This fails to compile with "unreported exception IOException;
must be caught or declared to be thrown." Why does this happen here but
not, say, for an `ArithmeticException` from `10 / 0`?

<details>
<summary>Solution</summary>

**Option A — catch it:**
```java
void loadConfig(String path) {
    try {
        FileReader reader = new FileReader(path);
        System.out.println("Config loaded");
    } catch (IOException e) {
        System.out.println("Failed to load config: " + e.getMessage());
    }
}
```

**Option B — declare it and let the caller handle it:**
```java
void loadConfig(String path) throws IOException {
    FileReader reader = new FileReader(path);
    System.out.println("Config loaded");
}
```

**Explanation:** `IOException` is a **checked exception** — a subclass of
`Exception` (but not `RuntimeException`) that the compiler forces you to
either `catch` or declare with `throws`, because it represents a
recoverable, expected failure condition (like a missing file) that callers
should consciously plan for. `ArithmeticException`, by contrast, is a
**runtime (unchecked) exception** — a subclass of `RuntimeException` — which
the compiler does *not* force you to handle, since these usually represent
programming bugs (like dividing by zero) rather than expected external
failures.

</details>

---

## Exercise 18: Exception Handling — Catch Block Ordering

**Concept:** More specific exceptions must be caught before more general ones.

```java
public class PayrollProcessor {
    void process(int[] salaries, int index) {
        try {
            int salary = salaries[index];
            int bonus = salary / 0;
        } catch (Exception e) {                        // line A
            System.out.println("General error: " + e);
        } catch (ArithmeticException e) {               // line B, compile error
            System.out.println("Math error: " + e);
        }
    }
}
```

**Task:** This fails to compile. Why, and what's the fix?

<details>
<summary>Solution</summary>

```java
void process(int[] salaries, int index) {
    try {
        int salary = salaries[index];
        int bonus = salary / 0;
    } catch (ArrayIndexOutOfBoundsException e) {
        System.out.println("Invalid salary index: " + e);
    } catch (ArithmeticException e) {
        System.out.println("Math error: " + e);
    } catch (Exception e) {
        System.out.println("General error: " + e);
    }
}
```

**Explanation:** `catch` blocks are checked **top to bottom**, and the first
matching one wins. `ArithmeticException` **is-a** `Exception` (it's a
subclass), so a `catch (Exception e)` block placed first would already match
every exception type below it — the compiler flags the unreachable
`ArithmeticException` catch block as an error, since it could never be
reached. The rule: **order catch blocks from most specific to least
specific**, ending with the most general type if you have one.

</details>

---

## Exercise 19: Exception Handling — `finally` and Resource Cleanup

**Concept:** `finally` always runs, even when something goes wrong.

```java
public class DatabaseConnection {
    boolean isOpen = false;

    void connect() {
        isOpen = true;
        System.out.println("Connection opened");
    }

    void close() {
        isOpen = false;
        System.out.println("Connection closed");
    }
}

public class Main {
    public static void main(String[] args) {
        DatabaseConnection db = new DatabaseConnection();
        db.connect();

        int[] data = {1, 2, 3};
        System.out.println(data[5]);   // throws — close() below never runs

        db.close();
    }
}
```

**Task:** The connection never gets closed if an exception is thrown before
`db.close()`. How do you guarantee cleanup runs regardless of success or
failure?

<details>
<summary>Solution</summary>

```java
public class Main {
    public static void main(String[] args) {
        DatabaseConnection db = new DatabaseConnection();
        db.connect();

        try {
            int[] data = {1, 2, 3};
            System.out.println(data[5]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Bad index: " + e.getMessage());
        } finally {
            db.close();   // always runs, exception or not
        }
    }
}
```

**Explanation:** A `finally` block runs **no matter what** happens in the
`try` — whether it completes normally, throws a caught exception, or even
throws an *uncaught* exception. This makes it the right place for cleanup
code (closing connections, releasing locks, closing files) that must happen
regardless of outcome. In modern Java, try-with-resources
(`try (Resource r = ...) { }`) is preferred for closeable resources since it
calls `close()` automatically, but understanding `finally` is essential
since it's the mechanism try-with-resources is built on.

</details>

---

## Exercise 20: Exception Handling — Custom Exceptions

**Concept:** Creating and throwing domain-specific exceptions.

```java
public class InsufficientBalanceException {   // missing something important
    public InsufficientBalanceException(String message) {
        System.out.println(message);
    }
}

public class BankAccount {
    double balance = 1000;

    void withdraw(double amount) {
        if (amount > balance) {
            throw new InsufficientBalanceException("Not enough funds");  // compile error
        }
        balance -= amount;
    }
}
```

**Task:** `throw` won't accept this class. What's missing from
`InsufficientBalanceException`?

<details>
<summary>Solution</summary>

```java
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}

public class BankAccount {
    double balance = 1000;

    void withdraw(double amount) {
        if (amount > balance) {
            throw new InsufficientBalanceException("Not enough funds to withdraw " + amount);
        }
        balance -= amount;
    }
}
```

**Explanation:** Only objects whose class **extends `Throwable`** (directly
or indirectly, via `Exception` or `RuntimeException`) can be used with
`throw`. The original class was a plain, unrelated class, so the compiler
rejected it. Extending `RuntimeException` creates an **unchecked** custom
exception (no `throws` declaration required at call sites); extending
`Exception` instead would make it a **checked** exception that callers must
handle or declare. Custom exceptions like this are common in real
applications to represent domain-specific failures (e.g.
`InsufficientBalanceException`, `InvalidOrderException`) instead of generic
ones.

</details>

---

## Exercise 21: `ArrayList` — `ConcurrentModificationException`

**Concept:** You can't structurally modify a list while iterating over it
with a for-each loop.

```java
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> employees = new ArrayList<>();
        employees.add("Amit");
        employees.add("Neha");
        employees.add("Contractor-Raj");

        for (String emp : employees) {
            if (emp.startsWith("Contractor")) {
                employees.remove(emp);   // throws ConcurrentModificationException
            }
        }
        System.out.println(employees);
    }
}
```

**Task:** Why does removing an item during a for-each loop blow up at
runtime?

<details>
<summary>Solution</summary>

```java
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> employees = new ArrayList<>();
        employees.add("Amit");
        employees.add("Neha");
        employees.add("Contractor-Raj");

        Iterator<String> it = employees.iterator();
        while (it.hasNext()) {
            String emp = it.next();
            if (emp.startsWith("Contractor")) {
                it.remove();   // safe: removes via the iterator itself
            }
        }
        System.out.println(employees);
    }
}
```

**Alternative (Java 8+):**
```java
employees.removeIf(emp -> emp.startsWith("Contractor"));
```

**Explanation:** A for-each loop uses an internal `Iterator` behind the
scenes. `ArrayList` keeps a `modCount` (modification counter); if you call
`list.remove(...)` directly while the iterator is mid-traversal, the
iterator notices the list changed outside of its own control and throws
`ConcurrentModificationException` on the next `next()` call — a
fail-fast safety check, not a bug. The fix is to remove through the
**iterator's own `remove()` method** (which keeps the count in sync), or to
use `removeIf(...)`, which handles this safely internally.

</details>

---

## Exercise 22: `ArrayList` — `remove(int)` vs `remove(Object)`

**Concept:** Overload ambiguity between primitive `int` and boxed `Integer`.

```java
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Integer> employeeIds = new ArrayList<>();
        employeeIds.add(101);
        employeeIds.add(102);
        employeeIds.add(103);

        employeeIds.remove(102);   // intended: remove the id "102"
        System.out.println(employeeIds);  // prints [101, 103]?? Actually removes wrong element
    }
}
```

**Task:** The team expected `102` to be removed from the list. What
actually gets removed, and why?

<details>
<summary>Solution</summary>

```java
employeeIds.remove(Integer.valueOf(102));   // removes the OBJECT 102
// or
employeeIds.remove((Integer) 102);
```

**Explanation:** `List<Integer>` has **two** overloaded `remove` methods:
`remove(int index)` and `remove(Object o)`. A plain `int` literal like
`102` always resolves to the **exact match** `remove(int index)` first
(exact matches win over boxing, same rule as Exercise 14) — so
`employeeIds.remove(102)` removes **the element at index 102** (which would
actually throw `IndexOutOfBoundsException` here since the list only has 3
elements, or silently remove the wrong element in a longer list), not the
value `102`. To remove *by value*, you must force autoboxing so the
`remove(Object)` overload is selected — e.g. `Integer.valueOf(102)` or an
explicit `(Integer)` cast. This is one of the most notorious real-world
`ArrayList<Integer>` traps.

</details>

---

## Exercise 23: `HashMap` — Duplicate Keys and Null Handling

**Concept:** `put()` overwrites existing keys silently.

```java
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<String, Double> salaries = new HashMap<>();
        salaries.put("Amit", 60000.0);
        salaries.put("Neha", 75000.0);
        salaries.put("Amit", 65000.0);   // was this an update or an error?

        System.out.println(salaries.size());          // expected 3, prints 2
        System.out.println(salaries.get("Amit"));      // prints 65000.0
    }
}
```

**Task:** The team expected `size()` to be `3` (three `put` calls). Why is
it `2`, and is this a bug?

<details>
<summary>Solution</summary>

**This is not a bug — it's expected `Map` behavior.** If you actually need
to keep a history of salary changes, you need a different data structure,
e.g.:

```java
Map<String, List<Double>> salaryHistory = new HashMap<>();
salaryHistory.computeIfAbsent("Amit", k -> new ArrayList<>()).add(60000.0);
salaryHistory.computeIfAbsent("Amit", k -> new ArrayList<>()).add(65000.0);
```

**Explanation:** A `Map` enforces **unique keys** by design. Calling
`put(key, value)` with a key that already exists **overwrites** the
previous value and returns the old value (which was ignored here) — it does
not add a second entry. So after the third `put("Amit", ...)`, there are
still only 2 distinct keys (`"Amit"`, `"Neha"`), and `"Amit"` now maps to
the newest value, `65000.0`. This "last write wins" behavior is intentional
and is exactly how you'd implement something like "current salary lookup by
employee name" — but it's the wrong tool if you need to preserve history.

</details>

---

## Exercise 24: `HashMap` — Safe Lookups with `getOrDefault` / `computeIfAbsent`

**Concept:** Avoiding `NullPointerException` on missing keys.

```java
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<String, Integer> bonusPoints = new HashMap<>();
        bonusPoints.put("Amit", 10);
        bonusPoints.put("Neha", 5);

        int rajPoints = bonusPoints.get("Raj");     // Raj was never added
        int updated = rajPoints + 5;                 // NullPointerException
        bonusPoints.put("Raj", updated);
    }
}
```

**Task:** Why does this throw a `NullPointerException`, and what's the
idiomatic fix?

<details>
<summary>Solution</summary>

```java
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<String, Integer> bonusPoints = new HashMap<>();
        bonusPoints.put("Amit", 10);
        bonusPoints.put("Neha", 5);

        // Option A: getOrDefault
        int rajPoints = bonusPoints.getOrDefault("Raj", 0);
        bonusPoints.put("Raj", rajPoints + 5);

        // Option B: merge (adds if absent, combines if present) — great for accumulating scores
        bonusPoints.merge("Raj", 5, Integer::sum);

        System.out.println(bonusPoints);
    }
}
```

**Explanation:** `map.get(key)` returns `null` when the key doesn't exist —
it does **not** throw by itself. The crash happens one line later, when
Java tries to **auto-unbox** that `null` `Integer` into a primitive `int`
(`rajPoints + 5`), which throws `NullPointerException`. `getOrDefault(key,
default)` sidesteps this by returning a fallback value instead of `null`
when the key is missing. For "read-or-initialize-then-update" patterns
(very common with counters/aggregations), `merge(...)` or
`computeIfAbsent(...)` are the idiomatic, one-line solutions instead of
manual null-checking.

</details>

---

## Exercise 25: `equals()` and Object Comparison

**Concept:** `==` vs `.equals()` for objects.

```java
public class Point {
    int x, y;
    Point(int x, int y) { this.x = x; this.y = y; }
}

public class Main {
    public static void main(String[] args) {
        Point p1 = new Point(1, 2);
        Point p2 = new Point(1, 2);

        if (p1 == p2) {
            System.out.println("Same point");
        } else {
            System.out.println("Different points");  // this prints, unexpectedly
        }
    }
}
```

**Task:** `p1` and `p2` have identical coordinates. Why does the program
say they're different, and how do you fix the comparison?

<details>
<summary>Solution</summary>

```java
import java.util.Objects;

public class Point {
    int x, y;
    Point(int x, int y) { this.x = x; this.y = y; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Point)) return false;
        Point p = (Point) obj;
        return this.x == p.x && this.y == p.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}

// Main:
if (p1.equals(p2)) {
    System.out.println("Same point");
}
```

**Explanation:** `==` on objects compares **references** (memory addresses),
not content — `p1` and `p2` are two distinct objects in memory even though
their field values match, so `==` is `false`. The default `.equals()`
(inherited from `Object`) also just does `==` under the hood unless you
override it. To compare objects by *value*, you must override `equals()`
(and, by convention/contract, `hashCode()` too, so the object behaves
correctly in collections like `HashMap`/`HashSet`).

</details>

---

## Exercise 26: `toString()` Override

**Concept:** Meaningful object printing.

```java
public class Employee {
    String name;
    double salary;

    Employee(String name, double salary) {
        this.name = name;
        this.salary = salary;
    }
}

public class Main {
    public static void main(String[] args) {
        Employee e = new Employee("Sara", 50000);
        System.out.println(e);   // prints Employee@1b6d3586 — not useful
    }
}
```

**Task:** Why does printing `e` produce gibberish, and how do you make it
print something readable like `Employee{name='Sara', salary=50000.0}`?

<details>
<summary>Solution</summary>

```java
public class Employee {
    String name;
    double salary;

    Employee(String name, double salary) {
        this.name = name;
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "Employee{name='" + name + "', salary=" + salary + "}";
    }
}
```

**Explanation:** `System.out.println(e)` implicitly calls `e.toString()`.
The default `toString()` from `Object` just returns
`ClassName@hashCodeInHex`, which is rarely useful. Overriding `toString()`
lets every object describe itself in a human-readable way, and it
automatically improves debugging, logging, and string concatenation
(`"Info: " + e`) everywhere in your code.

</details>

---

## Suggested order to practice

1. Classes & Objects → Constructors → `this` → Array of Objects
2. Encapsulation → Static vs Instance
3. Inheritance → `super()` chaining → Overriding vs Overloading
4. Polymorphism (basic) → Compile-Time (Overload Resolution) → Runtime (Field Hiding & `super`)
5. Abstraction (abstract classes) → Interfaces
6. Exception Handling: checked vs unchecked → catch ordering → `finally` → custom exceptions
7. `ArrayList`: iteration pitfalls → overload traps
8. `HashMap`: duplicate keys → safe lookups
9. `equals()`/`hashCode()` → `toString()`

Each stage builds on the last — don't skip ahead if a concept still feels shaky.
