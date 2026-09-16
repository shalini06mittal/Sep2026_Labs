# Clean Code Exercises (Java)

Each exercise below contains a working piece of Java code. It compiles and runs, but it has at least one clean-code problem hiding in it.

For each exercise:
1. Read the code.
2. Write down (on paper, in a comment, wherever) what's wrong with it and why it matters.
3. Rewrite it yourself.
4. Only then expand the **Solution** section to compare your answer.

Don't peek early — the point is to train your eye, not to read someone else's fix.

---

## Exercise 1

```java
public class Processor {

    public double calc(double a, double b, int t) {
        if (t == 1) {
            return a + b;
        } else if (t == 2) {
            return a - b;
        } else if (t == 3) {
            return a * b;
        } else if (t == 4) {
            return a / b;
        } else {
            return 0;
        }
    }
}
```

<details>
<summary>Solution</summary>

**Problems:**
- `Processor`, `calc`, `a`, `b`, `t` tell you nothing. A reader has to trace every branch just to learn that `1` means "add." Good names should let someone understand the call site (`calculator.add(3, 4)`) without opening the method body.
- The magic numbers `1`, `2`, `3`, `4` encode a hidden enum. Every caller has to remember what each number means, and there's nothing stopping someone from passing `t = 99`.

**Fix:** replace the type code with an enum (or, even better, separate methods) and give everything an intention-revealing name.

```java
public class Calculator {

    public double add(double left, double right) {
        return left + right;
    }

    public double subtract(double left, double right) {
        return left - right;
    }

    public double multiply(double left, double right) {
        return left * right;
    }

    public double divide(double left, double right) {
        return left / right;
    }
}
```

Now `calculator.add(3, 4)` is self-explanatory, and there's no invalid state to guard against.
</details>

---

## Exercise 2

```java
public void registerUser(String name, String email, String password, String address,
                          String city, String zip, boolean sendWelcomeEmail) {

    // check name
    if (name == null || name.trim().isEmpty()) {
        throw new IllegalArgumentException("bad name");
    }

    // check email
    if (email == null || !email.contains("@")) {
        throw new IllegalArgumentException("bad email");
    }

    // hash password
    String hashed = "";
    for (int i = 0; i < password.length(); i++) {
        hashed += (char) (password.charAt(i) + 1);
    }

    User user = new User(name, email, hashed, address, city, zip);
    database.save(user);

    if (sendWelcomeEmail) {
        EmailClient client = new EmailClient("smtp.company.com", 587);
        client.connect();
        client.send(email, "Welcome!", "Hi " + name + ", thanks for joining.");
        client.disconnect();
    }

    logger.log("User registered: " + email);
}
```

<details>
<summary>Solution</summary>

**Problems:**
- **One method, five jobs**: validating the name, validating the email, hashing the password, persisting the user, and sending an email are all mixed together at different levels of abstraction. Reading this method means constantly switching mental context between "input validation," "crypto," and "SMTP."
- The comments (`// check name`, `// check email`, `// hash password`) are compensating for the fact that the code isn't broken into named methods. A method call (`validateName(name)`) says the same thing as the comment, but it's also executable and can't drift out of sync with the code.
- The "hashing" isn't real hashing (it's a Caesar-cipher-style shift) — a naming lie that would mislead the next engineer into thinking passwords are safely hashed.

**Fix:** split by responsibility, one level of abstraction per method, and let the top-level method read like a summary of the steps:

```java
public void registerUser(RegistrationRequest request) {
    validateName(request.getName());
    validateEmail(request.getEmail());

    User user = buildUser(request);
    database.save(user);

    if (request.shouldSendWelcomeEmail()) {
        sendWelcomeEmail(user);
    }

    logger.log("User registered: " + user.getEmail());
}

private void validateName(String name) {
    if (name == null || name.trim().isEmpty()) {
        throw new IllegalArgumentException("Name must not be empty");
    }
}

private void validateEmail(String email) {
    if (email == null || !email.contains("@")) {
        throw new IllegalArgumentException("Email must contain '@'");
    }
}

private User buildUser(RegistrationRequest request) {
    String hashedPassword = passwordHasher.hash(request.getPassword());
    return new User(request.getName(), request.getEmail(), hashedPassword,
                     request.getAddress(), request.getCity(), request.getZip());
}

private void sendWelcomeEmail(User user) {
    emailClient.send(user.getEmail(), "Welcome!",
                      "Hi " + user.getName() + ", thanks for joining.");
}
```

Two extra wins: swapping the flat parameter list for a `RegistrationRequest` object, and delegating to a real `passwordHasher` instead of hand-rolled crypto.
</details>

---

## Exercise 3

```java
public class OrderService {

    public double getFinalPrice(Order order) {
        double price = order.getBasePrice();

        if (order.getCustomer().getYearsAsMember() > 2) {
            price = price - (price * 0.1);
        }

        if (order.getItemCount() > 10) {
            price = price - (price * 0.05);
        }

        if (order.getTotalWeight() > 50) {
            price = price + 15.99;
        }

        return price;
    }
}
```

<details>
<summary>Solution</summary>

**Problems:**
- `0.1`, `0.05`, `2`, `10`, `50`, and `15.99` are all magic numbers. Nothing tells you that `0.1` is a "loyalty discount" and `15.99` is a "shipping surcharge." If the business changes the loyalty discount from 10% to 12%, someone has to hunt through the method to find the right unlabeled literal.
- The three discount/surcharge rules are also duplicating the same shape of logic (`if (condition) adjust price`), which will get worse as more pricing rules are added.

**Fix:** name every constant, and consider extracting each rule into its own method so the top-level method reads as a policy summary:

```java
public class OrderService {

    private static final double LOYALTY_DISCOUNT_RATE = 0.10;
    private static final int LOYALTY_THRESHOLD_YEARS = 2;

    private static final double BULK_DISCOUNT_RATE = 0.05;
    private static final int BULK_ORDER_ITEM_THRESHOLD = 10;

    private static final double HEAVY_ORDER_SURCHARGE = 15.99;
    private static final double HEAVY_ORDER_WEIGHT_THRESHOLD_KG = 50;

    public double getFinalPrice(Order order) {
        double price = order.getBasePrice();
        price = applyLoyaltyDiscount(price, order);
        price = applyBulkDiscount(price, order);
        price = applyHeavyOrderSurcharge(price, order);
        return price;
    }

    private double applyLoyaltyDiscount(double price, Order order) {
        boolean isLoyalCustomer = order.getCustomer().getYearsAsMember() > LOYALTY_THRESHOLD_YEARS;
        return isLoyalCustomer ? price * (1 - LOYALTY_DISCOUNT_RATE) : price;
    }

    private double applyBulkDiscount(double price, Order order) {
        boolean isBulkOrder = order.getItemCount() > BULK_ORDER_ITEM_THRESHOLD;
        return isBulkOrder ? price * (1 - BULK_DISCOUNT_RATE) : price;
    }

    private double applyHeavyOrderSurcharge(double price, Order order) {
        boolean isHeavyOrder = order.getTotalWeight() > HEAVY_ORDER_WEIGHT_THRESHOLD_KG;
        return isHeavyOrder ? price + HEAVY_ORDER_SURCHARGE : price;
    }
}
```
</details>

---

## Exercise 4

```java
public String getShippingLabel(Customer customer) {
    String label;
    if (customer != null) {
        if (customer.getAddress() != null) {
            if (customer.getAddress().getCountry() != null) {
                if (customer.getAddress().getCountry().equals("US")) {
                    label = customer.getName() + "\n" + customer.getAddress().getStreet()
                            + "\n" + customer.getAddress().getCity() + ", "
                            + customer.getAddress().getState() + " " + customer.getAddress().getZip();
                } else {
                    label = customer.getName() + "\n" + customer.getAddress().getStreet()
                            + "\n" + customer.getAddress().getCity() + "\n"
                            + customer.getAddress().getCountry();
                }
            } else {
                label = "NO ADDRESS";
            }
        } else {
            label = "NO ADDRESS";
        }
    } else {
        label = "UNKNOWN CUSTOMER";
    }
    return label;
}
```

<details>
<summary>Solution</summary>

**Problems:**
- Four levels of nested `if`s just to reach the actual formatting logic. The reader has to hold the whole nesting stack in their head to figure out which branch does what.
- The "no address" case is duplicated in two different branches.
- The happy path (the US/non-US formatting) is buried at the deepest level of indentation, when it's actually the most important part of the method.

**Fix:** use early returns (guard clauses) to handle the invalid/edge cases up front, so the method body reads top-to-bottom as "reject the invalid stuff first, then do the real work":

```java
public String getShippingLabel(Customer customer) {
    if (customer == null) {
        return "UNKNOWN CUSTOMER";
    }

    Address address = customer.getAddress();
    if (address == null || address.getCountry() == null) {
        return "NO ADDRESS";
    }

    if (address.getCountry().equals("US")) {
        return formatDomesticLabel(customer, address);
    }
    return formatInternationalLabel(customer, address);
}

private String formatDomesticLabel(Customer customer, Address address) {
    return customer.getName() + "\n" + address.getStreet() + "\n"
            + address.getCity() + ", " + address.getState() + " " + address.getZip();
}

private String formatInternationalLabel(Customer customer, Address address) {
    return customer.getName() + "\n" + address.getStreet() + "\n"
            + address.getCity() + "\n" + address.getCountry();
}
```

No nesting deeper than one level, and each formatting method has a single, obvious job.
</details>

---

## Exercise 5

```java
public class ReportGenerator {

    // this method builds the report by looping through all transactions
    // and adding up the totals per category, then it formats everything
    // as a string and returns it - watch out, category names must be
    // uppercase or the grouping will silently break
    public String gen(List<Transaction> tx) {
        Map<String, Double> m = new HashMap<>();
        for (Transaction t : tx) {
            String c = t.getCategory().toUpperCase();
            // add to running total
            if (m.containsKey(c)) {
                m.put(c, m.get(c) + t.getAmount());
            } else {
                m.put(c, t.getAmount());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String k : m.keySet()) {
            sb.append(k).append(": ").append(m.get(k)).append("\n");
        }
        return sb.toString();
    }
}
```

<details>
<summary>Solution</summary>

**Problems:**
- The big comment block is doing the job the code should be doing: explaining what the method does step by step. A comment that describes *what* the code does (rather than *why*) is a sign the code itself isn't readable yet — and comments like this rot the moment someone changes the code without updating the prose above it.
- The buried warning ("category names must be uppercase or grouping silently breaks") is a landmine disguised as a comment. That behavior should be made impossible, not documented.
- `gen`, `tx`, `m`, `c`, `k`, `t` — none of these names say what they hold.
- Manual "does the map contain this key" bookkeeping is exactly what `Map.merge` exists for.

**Fix:** make the code say what the comment was saying, and remove the sharp edge instead of warning about it:

```java
public class ReportGenerator {

    public String generateCategoryTotalsReport(List<Transaction> transactions) {
        Map<String, Double> totalsByCategory = sumAmountsByCategory(transactions);
        return formatAsReport(totalsByCategory);
    }

    private Map<String, Double> sumAmountsByCategory(List<Transaction> transactions) {
        Map<String, Double> totals = new HashMap<>();
        for (Transaction transaction : transactions) {
            String normalizedCategory = transaction.getCategory().toUpperCase();
            totals.merge(normalizedCategory, transaction.getAmount(), Double::sum);
        }
        return totals;
    }

    private String formatAsReport(Map<String, Double> totalsByCategory) {
        StringBuilder report = new StringBuilder();
        for (Map.Entry<String, Double> entry : totalsByCategory.entrySet()) {
            report.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return report.toString();
    }
}
```

No comments needed — `sumAmountsByCategory` and `formatAsReport` say exactly what the old comment used to say, except they can't go stale.
</details>

---

## Exercise 6

```java
public class DiscountCalculator {

    public double d(Customer c) {
        double result = 0;
        if (c.getType() == 1) {
            result = 100;
        }
        if (c.getType() == 2) {
            result = 50;
        }
        if (c.getType() == 1) {
            if (c.getOrders() > 20) {
                result = result + 25;
            }
        }
        return result;
    }
}
```

<details>
<summary>Solution</summary>

**Problems:**
- `d`, `c`, `result` — meaningless names again, this time hiding the fact that this method computes a discount *amount*, not a rate, boolean, or anything else.
- `c.getType() == 1` is checked twice in two separate `if` blocks instead of once — a sign the logic wasn't planned as a whole, just accreted over time. It also means a future edit to "type 1" behavior has to remember to touch two spots.
- Type codes `1` and `2` are unexplained magic numbers, same problem as Exercise 1 — this should be an enum.

**Fix:**

```java
public enum CustomerType {
    PREMIUM,
    STANDARD
}

public class DiscountCalculator {

    private static final double PREMIUM_BASE_DISCOUNT = 100;
    private static final double STANDARD_BASE_DISCOUNT = 50;
    private static final double LOYALTY_BONUS = 25;
    private static final int LOYALTY_ORDER_THRESHOLD = 20;

    public double calculateDiscount(Customer customer) {
        if (customer.getType() == CustomerType.PREMIUM) {
            return calculatePremiumDiscount(customer);
        }
        if (customer.getType() == CustomerType.STANDARD) {
            return STANDARD_BASE_DISCOUNT;
        }
        return 0;
    }

    private double calculatePremiumDiscount(Customer customer) {
        double discount = PREMIUM_BASE_DISCOUNT;
        if (customer.getOrders() > LOYALTY_ORDER_THRESHOLD) {
            discount += LOYALTY_BONUS;
        }
        return discount;
    }
}
```

All the "type 1" logic now lives in one place (`calculatePremiumDiscount`), so there's nothing to keep in sync.
</details>

---

## Exercise 7

```java
public class InventoryManager {

    private List<Item> items = new ArrayList<>();

    public List<Item> getItems() {
        return items;
    }

    public void removeExpired() {
        for (Item i : items) {
            if (i.isExpired()) {
                items.remove(i);
            }
        }
    }
}
```

<details>
<summary>Solution</summary>

**Problems:**
- `getItems()` returns the live internal list, not a copy. Any caller can do `manager.getItems().clear()` and silently corrupt the manager's state from the outside — the class has no real control over its own data.
- `removeExpired()` modifies `items` while iterating over it with a for-each loop, which throws a `ConcurrentModificationException` at runtime. This isn't a style nitpick, it's a bug — but it's the kind of bug that hides in "clean-looking" code until someone runs it with real data.

**Fix:** return a defensive copy, and use `Iterator.remove()` (or `removeIf`) instead of mutating during a for-each:

```java
public class InventoryManager {

    private final List<Item> items = new ArrayList<>();

    public List<Item> getItems() {
        return List.copyOf(items);
    }

    public void removeExpired() {
        items.removeIf(Item::isExpired);
    }
}
```

`removeIf` also reads better than the original loop — it says exactly what it does.
</details>

---

## Exercise 8

```java
public void processPayment(Payment payment) {
    try {
        paymentGateway.charge(payment);
    } catch (Exception e) {
    }
}
```

<details>
<summary>Solution</summary>

**Problems:**
- An empty `catch` block silently swallows every possible failure — network errors, declined cards, invalid input, even `NullPointerException` from a bug elsewhere. The caller has no idea the payment failed; it just silently didn't happen.
- Catching `Exception` (rather than the specific exception the API can throw) hides genuine bugs alongside expected failure cases, and makes debugging production issues far harder than it needs to be.

**Fix:** catch the specific exception, and either handle it meaningfully or let it propagate with context:

```java
public void processPayment(Payment payment) throws PaymentFailedException {
    try {
        paymentGateway.charge(payment);
    } catch (PaymentGatewayException e) {
        throw new PaymentFailedException("Payment failed for order " + payment.getOrderId(), e);
    }
}
```

Now a failure is visible to the caller, includes the original cause for debugging, and can't be confused with success.
</details>

---

## Exercise 9

```java
public class TemperatureConverter {

    public double convert(double temp, boolean toF) {
        if (toF) {
            return temp * 9 / 5 + 32;
        } else {
            return (temp - 32) * 5 / 9;
        }
    }
}
```

<details>
<summary>Solution</summary>

**Problems:**
- `convert(temp, true)` at the call site tells you nothing — is `true` "convert to Fahrenheit," "is already Fahrenheit," or something else? A boolean flag parameter forces the reader to go look up the method signature every time they see a call.
- This is a common pattern worth naming: a boolean parameter that changes *which behavior* a method performs (rather than just data) usually means the method is really two methods wearing one name.

**Fix:** split into two clearly named methods:

```java
public class TemperatureConverter {

    public double celsiusToFahrenheit(double celsius) {
        return celsius * 9 / 5 + 32;
    }

    public double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }
}
```

`celsiusToFahrenheit(100)` reads correctly with no need to check the method definition.
</details>

---

## Exercise 10

```java
public class FileReaderUtil {

    public String readFile(String path) {
        String content = "";
        try {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                content += line + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }
}
```

<details>
<summary>Solution</summary>

**Problems:**
- The `FileReader`/`BufferedReader` are never closed if an exception happens (or even on the happy path here) — a resource leak. Every file handle stays open until the JVM garbage-collects it, which is not guaranteed to happen promptly.
- `content += line + "\n"` inside a loop rebuilds a new `String` on every iteration, which is O(n²) for large files — a `StringBuilder` should be used instead.
- `catch (Exception e) { e.printStackTrace(); }` hides the failure from the caller: if the file doesn't exist, `readFile` silently returns `""` instead of signaling that something went wrong.

**Fix:** use try-with-resources to guarantee the streams close, a `StringBuilder` for efficient concatenation, and propagate the failure:

```java
public class FileReaderUtil {

    public String readFile(String path) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}
```

The `throws IOException` is honest about what can go wrong, and try-with-resources closes the reader automatically even if `readLine()` throws.
</details>

---

## Exercise 11

```java
public int sumArray(int[] numbers) {
    int sum = 0;
    for (int i = 0; i <= numbers.length; i++) {
        sum += numbers[i];
    }
    return sum;
}
```

<details>
<summary>Solution</summary>

**Problems:**
- `i <= numbers.length` is an off-by-one error. Array indices run from `0` to `length - 1`, so on the last iteration `numbers[numbers.length]` throws `ArrayIndexOutOfBoundsException`. This is one of the most common loop bugs there is.
- Even once fixed, this loop only needs the values, not the index — that's a sign an enhanced for-loop is a better (and safer) fit.

**Fix:**

```java
public int sumArray(int[] numbers) {
    int sum = 0;
    for (int number : numbers) {
        sum += number;
    }
    return sum;
}
```

The enhanced for-loop removes the possibility of an off-by-one error entirely, because there's no index to get wrong.
</details>

---

## Exercise 12

```java
public void printEveryOther(List<String> items) {
    for (int i = 0; i < items.size(); i++) {
        System.out.println(items.get(i));
        i++;
    }
}
```

<details>
<summary>Solution</summary>

**Problems:**
- The loop mutates its own control variable (`i++`) inside the body *in addition to* the `for` statement's own increment. Now there are two places responsible for advancing `i`, and a reader has to mentally add them together to figure out the real step size. It's easy to misread this as printing every element.
- This pattern is a common source of off-by-one bugs when someone later edits the loop body without noticing the extra `i++`.

**Fix:** express the step size once, in the loop header, where it's expected:

```java
public void printEveryOther(List<String> items) {
    for (int i = 0; i < items.size(); i += 2) {
        System.out.println(items.get(i));
    }
}
```

Now there's exactly one place that controls how `i` advances.
</details>

---

## Exercise 13

```java
public void notifyActiveUsers() {
    for (int i = 0; i < getActiveUsers().size(); i++) {
        User user = getActiveUsers().get(i);
        sendNotification(user);
    }
}

private List<User> getActiveUsers() {
    return database.findAll().stream()
            .filter(User::isActive)
            .collect(Collectors.toList());
}
```

<details>
<summary>Solution</summary>

**Problems:**
- `getActiveUsers()` isn't a cheap field access — it hits `database.findAll()` and filters the result. Calling it in the loop condition means it re-runs on *every single iteration* (`n` times), and it's called again in the body to fetch the element, doubling the cost. For `n` active users, that's roughly `2n` full database scans instead of one.
- Because the condition is re-evaluated each pass, this also silently breaks if the underlying data changes mid-loop (a user becomes active/inactive while you're iterating), which is very hard to reproduce and debug.

**Fix:** call the expensive method once, store the result, and iterate over the stable local copy:

```java
public void notifyActiveUsers() {
    List<User> activeUsers = getActiveUsers();
    for (User user : activeUsers) {
        sendNotification(user);
    }
}
```

Same effect, one database call, and no risk of the collection changing underneath you mid-loop.
</details>

---

## Exercise 14

```java
public class ReportBuilder {

    private String lastCustomerName;

    public String buildSummary(List<Order> orders) {
        double total = 0;
        for (Order order : orders) {
            lastCustomerName = order.getCustomerName();
            total += order.getAmount();
        }
        return lastCustomerName + ": " + total;
    }
}
```

<details>
<summary>Solution</summary>

**Problems:**
- `lastCustomerName` is declared as a field, but it's only ever read and written inside `buildSummary`. Giving it field scope means every other method on this class — and every thread calling this class concurrently — can see and be affected by a value that's really just loop-local bookkeeping. The scope is far wider than the data's actual lifetime requires.
- Because it's overwritten on every loop iteration, by the time the loop ends it just holds whichever customer happened to be processed *last* — which is probably not what "build a summary of these orders" was supposed to mean, but the bug is easy to miss because the code "runs fine."

**Fix:** give the variable the narrowest scope that does the job, and make the method actually do what its name promises:

```java
public class ReportBuilder {

    public String buildSummary(List<Order> orders) {
        double total = 0;
        for (Order order : orders) {
            total += order.getAmount();
        }
        return orders.size() + " orders, total: " + total;
    }
}
```

If the "last customer" was genuinely needed, it should be a local variable inside the method — never a field just to smuggle a value out of a loop.
</details>

---

## Exercise 15

```java
public boolean isSameScore(Integer scoreA, Integer scoreB) {
    return scoreA == scoreB;
}
```

<details>
<summary>Solution</summary>

**Problems:**
- `Integer` is a boxed (object) type, so `==` compares object references, not numeric value. Java caches boxed `Integer`s from `-128` to `127`, so `isSameScore(100, 100)` happens to return `true`, but `isSameScore(200, 200)` returns `false` — the exact same logic gives different answers depending on the magnitude of the number, which is a nasty, cache-dependent bug.
- More generally, mixing primitives and boxed types invites this class of mistake: the compiler happily lets you write `==` on two objects without warning you that you probably meant `.equals()`.

**Fix:** compare boxed types with `.equals()` (guarding against `null`), or use primitive `int` parameters if `null` is never a valid input:

```java
public boolean isSameScore(Integer scoreA, Integer scoreB) {
    if (scoreA == null || scoreB == null) {
        return scoreA == scoreB;
    }
    return scoreA.equals(scoreB);
}
```

Or, if a score should never legitimately be absent:

```java
public boolean isSameScore(int scoreA, int scoreB) {
    return scoreA == scoreB;
}
```
</details>

---

## Exercise 16

```java
public class ShoppingCart {

    private double total = 0.0;

    public void addItem(double price) {
        total += price;
    }

    public double getTotal() {
        return total;
    }
}
```

<details>
<summary>Solution</summary>

**Problems:**
- `double` (and `float`) use binary floating-point representation, which cannot represent most decimal fractions exactly. Adding `0.10` three times doesn't reliably equal `0.30` — for money, these tiny representation errors accumulate and eventually show up as real discrepancies on an invoice or a balance sheet.
- This isn't a style preference — it's a correctness issue specific to representing currency, and it can go unnoticed for a long time because most test totals happen to round correctly by chance.

**Fix:** use `BigDecimal` with an explicit scale and rounding mode for anything representing money:

```java
public class ShoppingCart {

    private BigDecimal total = BigDecimal.ZERO;

    public void addItem(BigDecimal price) {
        total = total.add(price).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotal() {
        return total;
    }
}
```

(An alternative used in some systems is to store money as an integer number of cents, avoiding decimals entirely — but `BigDecimal` is the standard choice when you need to keep working in dollars-and-cents form.)
</details>

---

## Exercise 17

```java
public String buildCsv(List<String[]> rows) {
    String csv = "";
    for (String[] row : rows) {
        csv += String.join(",", row) + "\n";
    }
    return csv;
}
```

<details>
<summary>Solution</summary>

**Problems:**
- `String` is immutable in Java, so `csv += ...` doesn't append to the existing string — it creates a brand-new `String` on every iteration and copies the old contents into it. For `n` rows, that's roughly `n` copies of an ever-growing string, making this method's cost grow quadratically (`O(n²)`) instead of linearly. On a small list you won't notice; on a large export, this becomes the slowest part of the whole program.

**Fix:** use `StringBuilder`, which is built for exactly this — appending in a loop without recopying the whole buffer each time:

```java
public String buildCsv(List<String[]> rows) {
    StringBuilder csv = new StringBuilder();
    for (String[] row : rows) {
        csv.append(String.join(",", row)).append("\n");
    }
    return csv.toString();
}
```
</details>

---

## Exercise 18

```java
public List<Order> getOrdersForCustomer(String customerId) {
    List<Order> orders = database.findOrders(customerId);
    if (orders == null || orders.isEmpty()) {
        return null;
    }
    return orders;
}
```

```java
// at a call site elsewhere in the codebase
List<Order> orders = orderService.getOrdersForCustomer(customerId);
double total = 0;
for (Order order : orders) {
    total += order.getAmount();
}
```

<details>
<summary>Solution</summary>

**Problems:**
- Returning `null` to mean "no orders" forces *every single caller, forever* to remember to null-check before using the result. The call site shown above doesn't, and it throws a `NullPointerException` the first time a customer has no orders — a bug that won't show up in testing until someone happens to test an empty case.
- "No orders" and "orders exist" are both perfectly normal, expected outcomes — there's no reason to represent one of them with a special sentinel value that the type system can't force anyone to check.

**Fix:** return an empty collection instead of `null`. An empty list is just as easy to check (`isEmpty()`) but is also safe to iterate over directly with no crash risk:

```java
public List<Order> getOrdersForCustomer(String customerId) {
    List<Order> orders = database.findOrders(customerId);
    return orders != null ? orders : Collections.emptyList();
}
```

The call site now works correctly with zero changes — the loop simply doesn't execute when the list is empty. (For values that are genuinely optional rather than "empty is valid," `Optional<T>` is the equivalent tool.)
</details>

---

## Exercise 19

```java
public class NotificationManager {

    private final List<Listener> listeners = new ArrayList<>();

    public void registerListener() {
        listeners.add(new Listener() {
            @Override
            public void onNotify(String message) {
                handleMessage(message);
            }
        });
    }

    private void handleMessage(String message) {
        System.out.println(message);
    }
}
```

<details>
<summary>Solution</summary>

**Problems:**
- `Listener` here is a single-method (functional) interface, so the anonymous inner class is pure ceremony — four lines and a level of nesting just to say "call `handleMessage` when notified." The extra boilerplate makes the actual intent harder to see at a glance.
- An anonymous class also can't be referenced, reused, or tested in isolation — if the same listener logic is needed in two places, it has to be copy-pasted rather than shared.

**Fix:** use a lambda, which expresses the same behavior with none of the ceremony:

```java
public class NotificationManager {

    private final List<Listener> listeners = new ArrayList<>();

    public void registerListener() {
        listeners.add(message -> handleMessage(message));
        // or, since it's a direct pass-through: listeners.add(this::handleMessage);
    }

    private void handleMessage(String message) {
        System.out.println(message);
    }
}
```

One line, no nesting, and it reads as "when notified, handle the message."
</details>

---

## Exercise 20

```java
public class Point {

    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Point)) {
            return false;
        }
        Point other = (Point) obj;
        return this.x == other.x && this.y == other.y;
    }
}
```

```java
Set<Point> visited = new HashSet<>();
visited.add(new Point(1, 2));

System.out.println(visited.contains(new Point(1, 2))); // prints false!
```

<details>
<summary>Solution</summary>

**Problems:**
- `equals()` was overridden, but `hashCode()` wasn't. `HashSet`, `HashMap`, and `HashMap`-backed structures use `hashCode()` first to decide which "bucket" to look in, and only use `equals()` to compare objects *within* that bucket. Two `Point`s that are `equals()`-equal but have different (default, identity-based) hash codes will usually land in different buckets, so `contains()` never even calls `equals()` on them — it reports "not found" even though an equal object is sitting right there in the set.
- This is a well-known Java pitfall precisely because the code compiles fine, looks correct, and often "works" in ad-hoc testing (e.g., printing objects) — it only breaks once the object is used as a key or set member.

**Fix:** the contract is explicit: if you override `equals()`, you must override `hashCode()` so that equal objects always produce equal hash codes.

```java
@Override
public int hashCode() {
    return Objects.hash(x, y);
}
```

With both methods overridden consistently, `visited.contains(new Point(1, 2))` correctly returns `true`.
</details>

---

## Exercise 21

```java
public class RequestCounter {

    private static int count = 0;

    public void increment() {
        count++;
    }

    public int getCount() {
        return count;
    }
}
```

<details>
<summary>Solution</summary>

**Problems:**
- `count` is `static`, so it's shared across *every* instance of `RequestCounter` in the JVM — two unrelated parts of the codebase creating their own `RequestCounter` will silently affect each other's counts. If the intent was "each `RequestCounter` tracks its own requests," `static` is the wrong tool entirely.
- If the intent actually *was* a single shared, application-wide counter, `count++` is still not thread-safe: it's really three operations (read, increment, write), and two threads incrementing at the same time can race and lose an update. Nothing about this code prevents that.

**Fix:** decide which one is actually intended, and make it explicit. For a genuinely shared, thread-safe counter:

```java
public class RequestCounter {

    private static final AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.incrementAndGet();
    }

    public int getCount() {
        return count.get();
    }
}
```

If each instance should track its own count instead, simply drop `static` from the field.
</details>

---

## Exercise 22

```java
public void printAll(List list) {
    for (Object o : list) {
        String s = (String) o;
        System.out.println(s);
    }
}

public static void main(String[] args) {
    List names = new ArrayList();
    names.add("Alice");
    names.add(42);
    printAll(names);
}
```

<details>
<summary>Solution</summary>

**Problems:**
- `List` (a raw type, with no `<Type>`) turns off the compiler's type checking for that list entirely. `names.add(42)` compiles without complaint even though this is meant to be a list of names, and the mistake isn't caught until `printAll` tries to cast the `Integer` to a `String` at runtime, throwing a `ClassCastException` far away from where the actual mistake was made.
- Raw types exist in Java only for backward compatibility with pre-generics code — in new code they should essentially never appear, since generics exist specifically to catch this class of bug at compile time instead of at runtime.

**Fix:** use the generic type parameter so the compiler rejects the bad input immediately, at the point of the mistake:

```java
public void printAll(List<String> list) {
    for (String s : list) {
        System.out.println(s);
    }
}

public static void main(String[] args) {
    List<String> names = new ArrayList<>();
    names.add("Alice");
    names.add(42); // now a compile error, exactly where the mistake happens
    printAll(names);
}
```
</details>

---

## Exercise 23

```java
public void processOrder(Order order) {
    System.out.println("Processing order: " + order.getId());
    try {
        orderProcessor.process(order);
    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}
```

<details>
<summary>Solution</summary>

**Problems:**
- `System.out.println` isn't logging — it can't be turned on or off by severity, can't be redirected to a file or log aggregator, has no timestamp or thread/class context, and can't be controlled differently per-environment (verbose in development, quiet in production). Anything worth printing for diagnostics belongs in a real logging framework, not raw console output.
- `e.getMessage()` alone discards the stack trace. Once this happens, there is no way to tell *where* in `orderProcessor.process()` the failure occurred — the single most useful piece of debugging information is thrown away.
- There's no distinction between routine information ("processing started") and a genuine failure ("processing failed") — both go through the same undifferentiated `println`, so a log reader (human or tool) can't filter for what matters.

**Fix:** use a logging abstraction with proper levels, and always pass the exception object itself (not just its message) so the stack trace is preserved:

```java
public void processOrder(Order order) {
    logger.info("Processing order: {}", order.getId());
    try {
        orderProcessor.process(order);
    } catch (Exception e) {
        logger.error("Failed to process order: {}", order.getId(), e);
    }
}
```

This also uses parameterized placeholders (`{}`) instead of string concatenation, so the message is only built if that log level is actually enabled — avoiding wasted work for logging that will be discarded anyway.
</details>

---

## Wrap-up

Once you've been through all of them, go back and see if you can name the *category* of problem each exercise belonged to, without looking at the solutions again:

- Names that require a comment or a lookup to understand (1, 6, 9)
- Methods doing more than one job / mixing levels of abstraction (2, 5)
- Unexplained magic numbers and duplicated logic (1, 3, 6)
- Deep nesting instead of early returns (4)
- Comments compensating for unclear code, or hiding a landmine (5)
- Broken encapsulation and unsafe iteration (7)
- Silently swallowed exceptions (8)
- Boolean flag parameters hiding two behaviors in one method (9)
- Resource leaks and inefficient string handling (10, 17)
- Off-by-one errors and confusing loop-index mutation (11, 12)
- Expensive calls repeated unnecessarily inside a loop (13)
- Variable scope wider than the data's actual lifetime (14)
- Autoboxing pitfalls — `==` vs `.equals()` on boxed types (15)
- Floating-point types used where exact decimal arithmetic is required (16)
- `null` used as a sentinel instead of an empty collection / `Optional` (18)
- Anonymous inner classes where a lambda is clearer (19)
- `equals()` overridden without a matching `hashCode()` (20)
- Static mutable state and thread-safety (21)
- Raw types instead of generics (22)
- Console output instead of real logging, and lost stack traces (23)

If you found most of these on your own read-through, you're already reading code the way a reviewer should.
