# Code Review: `TradeReportGenerator`

## 1. SOLID / Design Principles

**Single Responsibility Principle (SRP) — violated hard.**
`doIt()` does everything: file I/O, CSV parsing, fee calculation, aggregation, console logging, and report writing, all in one method. Any change to one concern (e.g. switching to JSON output) risks breaking the others.

**Open/Closed Principle — violated.**
Fee logic is a hardcoded `if/else` on string literals (`"EQUITY"`, `"BOND"`). Adding a new instrument type means editing this method rather than extending a strategy/config. Worse, the `BOND` branch and the `else` branch compute the *same* fee (0.0005) — either dead code or a bug where a third type was intended but never distinguished.

**Encapsulation — violated.**
`tot`, `f`, and `c` are static, package-visible, mutable fields with no accessors, no reset, and no ownership boundary. Any class in the package can read or mutate them mid-processing.

**Dependency Inversion — absent.**
Everything depends directly on concrete `FileReader`/`FileWriter`. There's no `TradeSource` or `ReportSink` abstraction, so nothing can be swapped (e.g. reading from a DB or Kafka topic instead of a CSV) without rewriting the class.

## 2. Reusability

- **Static state is not reset between calls.** `tot`, `f`, and `c` persist across invocations. Call `doIt()` twice in the same JVM (e.g. in a loop, a service, or a test suite) and totals from the first file silently bleed into the second — a real correctness bug, not just a smell.
- **No Trade domain object.** Ticker, type, qty, price are passed around as raw `String[]` indices (`x[0]`, `x[1]`...). Any reordering of CSV columns breaks everything silently.
- **Output format is baked in.** The report-writing logic can't be reused to produce anything other than exactly this CSV shape.

## 3. Extensibility

- Adding a new fee tier, instrument type, output format, or validation rule all require editing the same monolithic method — no seams to extend behavior without modifying existing, working code.
- Fee rates (`0.001`, `0.0005`) are magic numbers baked into logic rather than externalized, so a business rule change requires a code change and redeploy.
- No enum for trade type — `typ.equals("EQUITY")` is stringly-typed. A typo like `"equity"` (lowercase) silently falls into the default fee bucket instead of failing loudly.

## 4. Scalability / Performance

- **`out = out + tkr + ...` inside a `while` loop is O(n²).** Strings are immutable in Java, so every concatenation allocates a new, larger string. For a large trade file this becomes a serious bottleneck and GC pressure source.
- **Entire report held in memory as one string, plus two full-size hash maps.** No streaming — memory usage grows unnecessarily with file size.
- **No buffering on write** (`FileWriter` used raw instead of wrapped in `BufferedWriter`).

## 5. Reliability / Error Handling

- **`catch (Exception e) { // skip bad row }`** swallows *everything* with zero logging. In a trade-reporting context, silently dropping rows with no audit trail is dangerous.
- **No resource management via try-with-resources.** If anything after the read loop throws, `fw` never gets closed (leak).
- **`throws Exception` on `main`/`doIt`** is a blanket, non-specific contract.

## 6. Domain Correctness (financial-specific)

- **Using `double` for money.** Binary floating-point rounding errors compound across trades — a classic bug class in financial software. Should be `BigDecimal` with an explicit rounding mode/scale.
- **No validation of row shape** — a row with a missing/extra field throws and is silently discarded rather than flagged for review.
- **Naive CSV parsing (`split(",")`)** — breaks on any quoted field containing a comma.

## 7. Testability

- Static mutable state + direct file I/O inside the same method means the fee calculation and aggregation logic cannot be unit tested without touching the filesystem and manually resetting shared state before every test.
- No return value — results are only observable via `System.out` or the output file.

## 8. Readability / Maintainability

- Cryptic single/double-letter names (`p`, `x`, `f`, `c`, `q`, `pr`, `val`, `tkr`, `typ`) obscure intent.
- Hardcoded output path `"report.csv"` written to the current working directory — reruns silently clobber prior output.

---

# Refactored Solution

The design splits responsibilities into: a `Trade` record, a `TradeType` enum, a `TradeParser`, a `FeeCalculator` strategy, a `TradeAggregator`, and a `ReportWriter` interface with a CSV implementation. All money math uses `BigDecimal`. Errors are collected and reported rather than silently dropped.

```java
package com.neueda.leap.sprint7.legacy;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.util.*;

// ---------------------------------------------------------------------
// Domain
// ---------------------------------------------------------------------

enum TradeType {
    EQUITY(new BigDecimal("0.0010")),
    BOND(new BigDecimal("0.0005")),
    FX(new BigDecimal("0.0005"));

    final BigDecimal feeRate;

    TradeType(BigDecimal feeRate) {
        this.feeRate = feeRate;
    }

    static TradeType fromString(String raw) {
        return TradeType.valueOf(raw.trim().toUpperCase());
    }
}

record Trade(String ticker, TradeType type, BigDecimal quantity, BigDecimal price) {
    BigDecimal value() {
        return quantity.multiply(price);
    }
}

record TradeResult(Trade trade, BigDecimal value, BigDecimal fee) {}

// ---------------------------------------------------------------------
// Parsing
// ---------------------------------------------------------------------

class TradeParsingException extends Exception {
    TradeParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}

class TradeParser {

    Trade parse(String csvLine) throws TradeParsingException {
        try {
            String[] fields = csvLine.split(",");
            if (fields.length != 4) {
                throw new IllegalArgumentException(
                        "Expected 4 fields, found " + fields.length);
            }
            String ticker = fields[0].trim();
            TradeType type = TradeType.fromString(fields[1]);
            BigDecimal quantity = new BigDecimal(fields[2].trim());
            BigDecimal price = new BigDecimal(fields[3].trim());
            return new Trade(ticker, type, quantity, price);
        } catch (Exception e) {
            throw new TradeParsingException("Failed to parse line: " + csvLine, e);
        }
    }
}

// ---------------------------------------------------------------------
// Fee calculation (Strategy)
// ---------------------------------------------------------------------

class FeeCalculator {

    private static final int SCALE = 2;

    TradeResult calculate(Trade trade) {
        BigDecimal value = trade.value().setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal fee = trade.value()
                .multiply(trade.type().feeRate)
                .setScale(SCALE, RoundingMode.HALF_UP);
        return new TradeResult(trade, value, fee);
    }
}

// ---------------------------------------------------------------------
// Aggregation
// ---------------------------------------------------------------------

class TradeAggregator {

    private final Map<String, BigDecimal> totalsByTicker = new LinkedHashMap<>();
    private final Map<String, BigDecimal> feesByTicker = new LinkedHashMap<>();
    private int processedCount = 0;

    void accumulate(TradeResult result) {
        String ticker = result.trade().ticker();
        totalsByTicker.merge(ticker, result.value(), BigDecimal::add);
        feesByTicker.merge(ticker, result.fee(), BigDecimal::add);
        processedCount++;
    }

    Map<String, BigDecimal> totals() {
        return Collections.unmodifiableMap(totalsByTicker);
    }

    Map<String, BigDecimal> fees() {
        return Collections.unmodifiableMap(feesByTicker);
    }

    int processedCount() {
        return processedCount;
    }
}

// ---------------------------------------------------------------------
// Output (Dependency Inversion via interface)
// ---------------------------------------------------------------------

interface ReportWriter extends Closeable {
    void writeHeader() throws IOException;
    void writeRow(TradeResult result) throws IOException;
}

class CsvReportWriter implements ReportWriter {

    private final BufferedWriter writer;

    CsvReportWriter(Path outputPath) throws IOException {
        this.writer = Files.newBufferedWriter(outputPath);
    }

    @Override
    public void writeHeader() throws IOException {
        writer.write("TICKER,QTY,VALUE,FEE");
        writer.newLine();
    }

    @Override
    public void writeRow(TradeResult r) throws IOException {
        writer.write(String.join(",",
                r.trade().ticker(),
                r.trade().quantity().toPlainString(),
                r.value().toPlainString(),
                r.fee().toPlainString()));
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}

// ---------------------------------------------------------------------
// Orchestration
// ---------------------------------------------------------------------

class TradeReportGenerator {

    private final TradeParser parser;
    private final FeeCalculator feeCalculator;

    TradeReportGenerator(TradeParser parser, FeeCalculator feeCalculator) {
        this.parser = parser;
        this.feeCalculator = feeCalculator;
    }

    TradeAggregator generate(Path inputPath, Path outputPath) throws IOException {
        TradeAggregator aggregator = new TradeAggregator();
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputPath);
             ReportWriter reportWriter = new CsvReportWriter(outputPath)) {

            reportWriter.writeHeader();

            String line = reader.readLine(); // header, discarded
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    Trade trade = parser.parse(line);
                    TradeResult result = feeCalculator.calculate(trade);
                    aggregator.accumulate(result);
                    reportWriter.writeRow(result);
                } catch (TradeParsingException e) {
                    errors.add(e.getMessage());
                }
            }
        }

        if (!errors.isEmpty()) {
            System.err.println("Skipped " + errors.size() + " invalid row(s):");
            errors.forEach(msg -> System.err.println("  - " + msg));
        }

        return aggregator;
    }

    public static void main(String[] args) throws IOException {
        Path input = Paths.get(args.length > 0 ? args[0] : "src/main/resources/trades.csv");
        Path output = Paths.get("report.csv");

        TradeReportGenerator generator = new TradeReportGenerator(
                new TradeParser(), new FeeCalculator());

        TradeAggregator result = generator.generate(input, output);

        System.out.println("Processed " + result.processedCount() + " trades");
        result.totals().forEach((ticker, total) ->
                System.out.println(ticker
                        + " total=" + total
                        + " fee=" + result.fees().get(ticker)));
    }
}
```

## What each fix addresses

| Problem | Fix |
|---|---|
| SRP violation | Split into `TradeParser`, `FeeCalculator`, `TradeAggregator`, `ReportWriter`, and a thin orchestrator |
| Open/Closed violation | `TradeType` enum carries its own fee rate — adding a type needs no `if/else` edit |
| Static mutable state | All state is instance-scoped inside `TradeAggregator`, created fresh per run |
| O(n²) string concatenation | Rows streamed directly to a `BufferedWriter` line by line |
| `double` for money | Replaced with `BigDecimal` and explicit rounding |
| Silent row-skipping | Parse failures are collected with the offending line and reported, not swallowed |
| No resource safety | `try-with-resources` on reader and writer |
| Untestable design | Each class can be unit tested in isolation with no filesystem or static state involved |
| Stringly-typed instrument type | `TradeType.fromString` fails loudly (`IllegalArgumentException`) on unknown/misspelled types |

## Still worth considering for production

- Externalize fee rates to a config file rather than the enum, if they change independently of code deploys.
- Use a proper CSV library (e.g. Apache Commons CSV or OpenCSV) instead of `split(",")` to handle quoted fields correctly.
- Add unit tests for `TradeParser`, `FeeCalculator`, and `TradeAggregator` covering edge cases (zero quantity, unknown type, malformed numbers).
- Consider making the output path/format configurable rather than hardcoded to `report.csv`.
