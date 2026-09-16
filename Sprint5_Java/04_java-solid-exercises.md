# SOLID Principle Exercises (Java)

Each exercise below contains a working piece of Java code. It compiles and runs, but its design violates one of the SOLID principles.

For each exercise:
1. Read the code and figure out what will hurt when the requirements change — not just whether it works today.
2. Decide which principle is being broken and why.
3. Rewrite the design yourself.
4. Only then expand the **Solution** section to compare your answer.

The principle being tested is never named up front — figuring that out is part of the exercise.

---

## Exercise 1

```java
public class Invoice {

    private String customerName;
    private List<InvoiceItem> items;

    public double calculateTotal() {
        double total = 0;
        for (InvoiceItem item : items) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    public void printInvoice() {
        System.out.println("Invoice for: " + customerName);
        for (InvoiceItem item : items) {
            System.out.println(item.getName() + " x" + item.getQuantity());
        }
        System.out.println("Total: " + calculateTotal());
    }

    public void saveToDatabase() {
        String sql = "INSERT INTO invoices (customer, total) VALUES ('"
                + customerName + "', " + calculateTotal() + ")";
        Database.execute(sql);
    }
}
```

<details>
<summary>Solution</summary>

**Principle violated: Single Responsibility Principle (SRP).**

`Invoice` has three unrelated reasons to change:
- The *business rule* for computing a total changes (e.g., tax, discounts).
- The *presentation format* changes (e.g., printing becomes HTML or a PDF instead of console text).
- The *persistence mechanism* changes (e.g., switching from raw SQL to an ORM, or from SQL to a document store).

Bundling all three into one class means a change to how invoices are stored can require touching — and re-testing — the same class that computes totals and prints receipts. Each responsibility should have exactly one reason to change, and right now this class has three.

**Fix:** split by responsibility, and let `Invoice` just hold data and the calculation that's intrinsically its own:

```java
public class Invoice {
    private String customerName;
    private List<InvoiceItem> items;

    public double calculateTotal() {
        double total = 0;
        for (InvoiceItem item : items) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    // getters for customerName, items
}

public class InvoicePrinter {
    public void print(Invoice invoice) {
        System.out.println("Invoice for: " + invoice.getCustomerName());
        for (InvoiceItem item : invoice.getItems()) {
            System.out.println(item.getName() + " x" + item.getQuantity());
        }
        System.out.println("Total: " + invoice.calculateTotal());
    }
}

public class InvoiceRepository {
    public void save(Invoice invoice) {
        String sql = "INSERT INTO invoices (customer, total) VALUES (?, ?)";
        Database.executeWithParams(sql, invoice.getCustomerName(), invoice.calculateTotal());
    }
}
```

Now printing logic, storage logic, and the invoice's own data/behavior can each change independently.
</details>

---

## Exercise 2

```java
public class AreaCalculator {

    public double calculateArea(Object shape) {
        if (shape instanceof Circle) {
            Circle circle = (Circle) shape;
            return Math.PI * circle.getRadius() * circle.getRadius();
        } else if (shape instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) shape;
            return rectangle.getWidth() * rectangle.getHeight();
        }
        throw new IllegalArgumentException("Unknown shape");
    }
}
```

<details>
<summary>Solution</summary>

**Principle violated: Open/Closed Principle (OCP).**

Every time a new shape is added — `Triangle`, `Hexagon`, whatever comes next — someone has to reopen `AreaCalculator` and add another `else if` branch. The class is never "done"; it has to be modified for every new requirement of the same kind, and every modification risks breaking the shapes that already work. A well-designed abstraction should let you *add* new shapes without touching existing, already-tested code.

**Fix:** push the area calculation into each shape via a common abstraction, so `AreaCalculator` never needs to change again:

```java
public interface Shape {
    double calculateArea();
}

public class Circle implements Shape {
    private final double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }
}

public class Rectangle implements Shape {
    private final double width;
    private final double height;

    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double calculateArea() {
        return width * height;
    }
}

public class AreaCalculator {
    public double calculateArea(Shape shape) {
        return shape.calculateArea();
    }
}
```

Adding `Triangle` now means writing a new class that implements `Shape` — `AreaCalculator` is closed for modification but open for that extension.
</details>

---

## Exercise 3

```java
public class Bird {
    public void fly() {
        System.out.println("Flying high");
    }
}

public class Sparrow extends Bird {
}

public class Penguin extends Bird {
    @Override
    public void fly() {
        throw new UnsupportedOperationException("Penguins can't fly");
    }
}
```

```java
public void releaseAllBirds(List<Bird> birds) {
    for (Bird bird : birds) {
        bird.fly();
    }
}
```

<details>
<summary>Solution</summary>

**Principle violated: Liskov Substitution Principle (LSP).**

`releaseAllBirds` is written against the `Bird` type, and it should be able to treat any `Bird` — including a `Penguin` — interchangeably, because `Penguin` *is a* `Bird`. But substituting a `Penguin` in place of a `Sparrow` breaks the program with a runtime exception. A subclass that overrides a method to throw where the parent guaranteed behavior is a textbook LSP violation: the subtype makes a promise (`Bird` can `fly()`) that it then breaks, so it isn't truly substitutable for its parent.

**Fix:** don't model "can fly" as something every `Bird` has. Separate the capability from the general concept:

```java
public abstract class Bird {
    public abstract void eat();
}

public interface FlyingBird {
    void fly();
}

public class Sparrow extends Bird implements FlyingBird {
    @Override
    public void eat() { System.out.println("Pecking seeds"); }

    @Override
    public void fly() { System.out.println("Flying high"); }
}

public class Penguin extends Bird {
    @Override
    public void eat() { System.out.println("Catching fish"); }
    // no fly() — Penguin never claims a capability it doesn't have
}
```

```java
public void releaseFlyingBirds(List<FlyingBird> birds) {
    for (FlyingBird bird : birds) {
        bird.fly();
    }
}
```

Now the type system itself prevents anyone from calling `fly()` on a `Penguin` — there's no runtime surprise because the contract was never falsely promised.
</details>

---

## Exercise 4

```java
public interface Worker {
    void work();
    void eat();
    void sleep();
}

public class HumanWorker implements Worker {
    @Override
    public void work() { System.out.println("Working"); }

    @Override
    public void eat() { System.out.println("Eating lunch"); }

    @Override
    public void sleep() { System.out.println("Sleeping"); }
}

public class RobotWorker implements Worker {
    @Override
    public void work() { System.out.println("Working"); }

    @Override
    public void eat() {
        throw new UnsupportedOperationException("Robots don't eat");
    }

    @Override
    public void sleep() {
        throw new UnsupportedOperationException("Robots don't sleep");
    }
}
```

<details>
<summary>Solution</summary>

**Principle violated: Interface Segregation Principle (ISP).**

`Worker` bundles three unrelated capabilities into one interface, so any implementer is forced to provide all three — even a `RobotWorker` for whom `eat()` and `sleep()` make no sense. `RobotWorker` ends up implementing methods it fundamentally cannot support, and the only thing it can do with them is throw. Callers that just want to call `work()` are still exposed to `eat()` and `sleep()` on the interface, with no way to know at compile time that calling them on a `RobotWorker` will blow up. Clients shouldn't be forced to depend on methods they don't use — and implementers shouldn't be forced to implement methods that don't apply to them.

**Fix:** split the fat interface into smaller, focused ones, and let each class implement only what actually applies to it:

```java
public interface Workable {
    void work();
}

public interface Eatable {
    void eat();
}

public interface Sleepable {
    void sleep();
}

public class HumanWorker implements Workable, Eatable, Sleepable {
    @Override
    public void work() { System.out.println("Working"); }

    @Override
    public void eat() { System.out.println("Eating lunch"); }

    @Override
    public void sleep() { System.out.println("Sleeping"); }
}

public class RobotWorker implements Workable {
    @Override
    public void work() { System.out.println("Working"); }
}
```

Code that only cares about work can depend on `Workable`, and `RobotWorker` never has to fake support for `eat()` or `sleep()`.
</details>

---

## Exercise 5

```java
public class OrderService {

    private MySqlOrderRepository repository = new MySqlOrderRepository();

    public void placeOrder(Order order) {
        repository.save(order);
    }
}

public class MySqlOrderRepository {
    public void save(Order order) {
        System.out.println("Saving order to MySQL: " + order.getId());
    }
}
```

<details>
<summary>Solution</summary>

**Principle violated: Dependency Inversion Principle (DIP).**

`OrderService` — a high-level policy class about placing orders — directly instantiates and depends on `MySqlOrderRepository`, a low-level, concrete implementation detail. This means:
- `OrderService` can never be tested without a real (or heavily mocked) MySQL dependency.
- Switching storage — to Postgres, to a document store, to an in-memory store for tests — means editing `OrderService` itself, even though "how orders are placed" hasn't actually changed.

High-level modules should depend on abstractions, not on concrete low-level details; the concrete detail should depend on the abstraction instead.

**Fix:** introduce an interface that `OrderService` depends on, and inject the concrete implementation from outside:

```java
public interface OrderRepository {
    void save(Order order);
}

public class MySqlOrderRepository implements OrderRepository {
    @Override
    public void save(Order order) {
        System.out.println("Saving order to MySQL: " + order.getId());
    }
}

public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public void placeOrder(Order order) {
        repository.save(order);
    }
}
```

Now `OrderService` depends only on the `OrderRepository` abstraction. A test can pass in an in-memory fake, and swapping databases means writing a new `OrderRepository` implementation — `OrderService` never has to change.
</details>

---

## Exercise 6

```java
public class ReportGenerator {

    public void generateReport(String type, List<Sale> sales) {
        double total = 0;
        for (Sale sale : sales) {
            total += sale.getAmount();
        }

        if (type.equals("summary")) {
            System.out.println("Total sales: " + total);
        } else if (type.equals("detailed")) {
            for (Sale sale : sales) {
                System.out.println(sale.getProduct() + ": " + sale.getAmount());
            }
            System.out.println("Total: " + total);
        } else if (type.equals("tax")) {
            System.out.println("Tax owed: " + (total * 0.08));
        }
    }
}
```

<details>
<summary>Solution</summary>

**Principle violated: Open/Closed Principle (OCP).**

Every new report format — a CSV export, a PDF summary, a quarterly tax report with a different rate — means opening `generateReport` and adding another `else if` branch, growing an already-tangled method and risking the existing report types every time. The class isn't closed against this very predictable kind of change: "we need another report format" is guaranteed to happen again, and the current design makes each occurrence riskier than the last.

**Fix:** define a `ReportFormatter` abstraction and let each format be its own class:

```java
public interface ReportFormatter {
    void format(List<Sale> sales, double total);
}

public class SummaryReportFormatter implements ReportFormatter {
    @Override
    public void format(List<Sale> sales, double total) {
        System.out.println("Total sales: " + total);
    }
}

public class DetailedReportFormatter implements ReportFormatter {
    @Override
    public void format(List<Sale> sales, double total) {
        for (Sale sale : sales) {
            System.out.println(sale.getProduct() + ": " + sale.getAmount());
        }
        System.out.println("Total: " + total);
    }
}

public class TaxReportFormatter implements ReportFormatter {
    private static final double TAX_RATE = 0.08;

    @Override
    public void format(List<Sale> sales, double total) {
        System.out.println("Tax owed: " + (total * TAX_RATE));
    }
}

public class ReportGenerator {
    public void generateReport(ReportFormatter formatter, List<Sale> sales) {
        double total = sales.stream().mapToDouble(Sale::getAmount).sum();
        formatter.format(sales, total);
    }
}
```

Adding a new report type now means adding a new `ReportFormatter` implementation — `ReportGenerator` itself never changes again.
</details>

---

## Exercise 7

```java
public class ReadOnlyList<T> extends ArrayList<T> {

    @Override
    public boolean add(T item) {
        throw new UnsupportedOperationException("This list is read-only");
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException("This list is read-only");
    }
}
```

```java
public void addSampleData(List<String> list) {
    list.add("sample");
}
```

<details>
<summary>Solution</summary>

**Principle violated: Liskov Substitution Principle (LSP).**

`ReadOnlyList` extends `ArrayList`, so anywhere a `List<T>` is expected, a `ReadOnlyList<T>` should work as a drop-in replacement — that's the whole point of subtyping. But `addSampleData` above, written against the general `List` contract, blows up with an exception the instant it's handed a `ReadOnlyList`. The subclass changes the behavioral contract of its parent (silently forbidding operations the parent guarantees) rather than extending it, so it can't actually be substituted wherever an `ArrayList` or `List` is expected without risk.

**Fix:** don't model "read-only" as inheriting from a mutable collection and disabling parts of it. Either wrap an existing list in an unmodifiable view, or model immutability as its own type rather than a mutable one in disguise:

```java
public List<String> createReadOnlyList(List<String> source) {
    return Collections.unmodifiableList(new ArrayList<>(source));
}
```

`Collections.unmodifiableList` still honors the `List` contract faithfully (it throws `UnsupportedOperationException` on mutation *as part of the documented interface contract*, not as a surprise override of a class that promised otherwise) — the key difference is that nothing pretends to be a fully mutable `ArrayList` while secretly breaking that promise.
</details>

---

## Exercise 8

```java
public interface Machine {
    void print(Document doc);
    void scan(Document doc);
    void fax(Document doc);
}

public class BasicPrinter implements Machine {
    @Override
    public void print(Document doc) {
        System.out.println("Printing: " + doc.getName());
    }

    @Override
    public void scan(Document doc) {
        throw new UnsupportedOperationException("This printer cannot scan");
    }

    @Override
    public void fax(Document doc) {
        throw new UnsupportedOperationException("This printer cannot fax");
    }
}
```

<details>
<summary>Solution</summary>

**Principle violated: Interface Segregation Principle (ISP).**

`Machine` forces every implementer to support printing, scanning, *and* faxing, even though plenty of real machines only do one of those things. `BasicPrinter` is stuck implementing `scan()` and `fax()` purely to satisfy the interface, and both are landmines that throw at runtime if anyone calls them — which the type system does nothing to prevent, since `Machine` advertises all three as valid operations on any implementer.

**Fix:** break the interface apart by capability, so a class only signs up for what it can actually do:

```java
public interface Printer {
    void print(Document doc);
}

public interface Scanner {
    void scan(Document doc);
}

public interface Fax {
    void fax(Document doc);
}

public class BasicPrinter implements Printer {
    @Override
    public void print(Document doc) {
        System.out.println("Printing: " + doc.getName());
    }
}

public class AllInOnePrinter implements Printer, Scanner, Fax {
    @Override
    public void print(Document doc) { System.out.println("Printing: " + doc.getName()); }

    @Override
    public void scan(Document doc) { System.out.println("Scanning: " + doc.getName()); }

    @Override
    public void fax(Document doc) { System.out.println("Faxing: " + doc.getName()); }
}
```

Code that only needs printing can depend on `Printer` alone, and `BasicPrinter` never has to fake capabilities it doesn't have.
</details>

---

## Exercise 9

```java
public class NotificationService {

    private EmailSender emailSender = new EmailSender();

    public void notifyUser(String message, String userContact) {
        emailSender.send(userContact, message);
    }
}

public class EmailSender {
    public void send(String email, String message) {
        System.out.println("Emailing " + email + ": " + message);
    }
}
```

<details>
<summary>Solution</summary>

**Principle violated: Dependency Inversion Principle (DIP).**

`NotificationService` is meant to represent a general policy — "notify the user" — but it's wired directly to one concrete, low-level mechanism: email. Adding SMS or push notifications later means editing `NotificationService` itself, and testing it means actually sending emails (or heavily mocking a concrete class) rather than substituting a simple fake. The high-level notification policy is dictating its own implementation detail instead of depending on an abstraction that any delivery mechanism could satisfy.

**Fix:** introduce a `MessageSender` abstraction and inject the concrete channel from outside:

```java
public interface MessageSender {
    void send(String contact, String message);
}

public class EmailSender implements MessageSender {
    @Override
    public void send(String contact, String message) {
        System.out.println("Emailing " + contact + ": " + message);
    }
}

public class SmsSender implements MessageSender {
    @Override
    public void send(String contact, String message) {
        System.out.println("Texting " + contact + ": " + message);
    }
}

public class NotificationService {

    private final MessageSender messageSender;

    public NotificationService(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void notifyUser(String message, String userContact) {
        messageSender.send(userContact, message);
    }
}
```

`NotificationService` now works with any `MessageSender` — email, SMS, or a test fake — without ever needing to change itself.
</details>

---

## Exercise 10

```java
public class Employee {

    private String name;
    private double baseSalary;
    private String role;

    public double calculateSalary() {
        if (role.equals("MANAGER")) {
            return baseSalary * 1.5;
        } else if (role.equals("DEVELOPER")) {
            return baseSalary * 1.2;
        } else if (role.equals("INTERN")) {
            return baseSalary * 0.8;
        }
        return baseSalary;
    }

    public String generateMonthlyReport() {
        return name + " (" + role + "): $" + calculateSalary();
    }

    public void sendPayslipEmail(String emailAddress) {
        System.out.println("Emailing payslip to " + emailAddress + ": " + generateMonthlyReport());
    }
}
```

<details>
<summary>Solution</summary>

**Principle violated: this one combines two — Single Responsibility Principle and Open/Closed Principle.**

- **SRP:** `Employee` mixes salary calculation, report formatting, and email delivery — three unrelated reasons to change (a pay-rate policy change, a report format change, an email provider change) all living in one class.
- **OCP:** `calculateSalary()` needs a new `else if` branch every time a new role is introduced, exactly like the shape and report examples above — the class is never closed against the very predictable growth of "we hired a new kind of role."

**Fix:** separate the responsibilities, and replace the role `if/else` chain with a per-role abstraction so new roles don't require modifying existing code:

```java
public interface SalaryPolicy {
    double calculate(double baseSalary);
}

public class ManagerSalaryPolicy implements SalaryPolicy {
    @Override
    public double calculate(double baseSalary) { return baseSalary * 1.5; }
}

public class DeveloperSalaryPolicy implements SalaryPolicy {
    @Override
    public double calculate(double baseSalary) { return baseSalary * 1.2; }
}

public class InternSalaryPolicy implements SalaryPolicy {
    @Override
    public double calculate(double baseSalary) { return baseSalary * 0.8; }
}

public class Employee {
    private final String name;
    private final double baseSalary;
    private final SalaryPolicy salaryPolicy;

    public Employee(String name, double baseSalary, SalaryPolicy salaryPolicy) {
        this.name = name;
        this.baseSalary = baseSalary;
        this.salaryPolicy = salaryPolicy;
    }

    public double calculateSalary() {
        return salaryPolicy.calculate(baseSalary);
    }

    public String getName() { return name; }
}

public class PayslipReportGenerator {
    public String generateMonthlyReport(Employee employee) {
        return employee.getName() + ": $" + employee.calculateSalary();
    }
}

public class PayslipEmailer {
    public void send(Employee employee, String emailAddress, PayslipReportGenerator reportGenerator) {
        System.out.println("Emailing payslip to " + emailAddress + ": "
                + reportGenerator.generateMonthlyReport(employee));
    }
}
```

A new role is now a new `SalaryPolicy` implementation, and reporting/emailing can each evolve — new formats, new delivery channels — without touching `Employee` or each other.
</details>

---

## Wrap-up

Once you've been through all ten, try to map each exercise to its principle without looking back:

- **S — Single Responsibility Principle:** a class should have only one reason to change (1, 10)
- **O — Open/Closed Principle:** open for extension, closed for modification — new cases should mean new code, not edited code (2, 6, 10)
- **L — Liskov Substitution Principle:** a subtype must be usable anywhere its parent type is expected, without surprises (3, 7)
- **I — Interface Segregation Principle:** don't force a class to implement methods it has no use for (4, 8)
- **D — Dependency Inversion Principle:** depend on abstractions, not concrete low-level details (5, 9)

If you correctly identified the principle before reading the solution each time, you're not just spotting bad code — you're recognizing *why* it's fragile, which is the harder and more useful skill.
