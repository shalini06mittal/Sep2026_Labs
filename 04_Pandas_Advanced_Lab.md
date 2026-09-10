# Pandas Advanced Operations Lab
### GroupBy · Pivot Tables · Merge · Resample

**Course:** Python for Data Analysis
**Level:** Intermediate / Advanced
**Estimated time:** 90–120 minutes

---

## Table of Contents

- [Pandas Advanced Operations Lab](#pandas-advanced-operations-lab)
    - [GroupBy · Pivot Tables · Merge · Resample](#groupby--pivot-tables--merge--resample)
  - [Table of Contents](#table-of-contents)
  - [1. Introduction \& Scenario](#1-introduction--scenario)
  - [2. Setup: Building the Dataset](#2-setup-building-the-dataset)
    - [Understand the data before you touch it](#understand-the-data-before-you-touch-it)
  - [3. Part 1 — GroupBy: Aggregating Data](#3-part-1--groupby-aggregating-data)
    - [3.1 The Business Question](#31-the-business-question)
    - [3.2 The Split → Apply → Combine Model](#32-the-split--apply--combine-model)
    - [3.3 Level 1: One Aggregation](#33-level-1-one-aggregation)
    - [3.4 Level 2: Multiple Aggregations](#34-level-2-multiple-aggregations)
    - [3.5 Level 3: Grouping by Two Columns](#35-level-3-grouping-by-two-columns)
    - [3.6 Level 4: Controlling the Index with `as_index`](#36-level-4-controlling-the-index-with-as_index)
    - [3.7 Challenge: Find the Top Performer](#37-challenge-find-the-top-performer)
  - [4. Part 2 — Pivot Tables: Reshaping Data](#4-part-2--pivot-tables-reshaping-data)
    - [4.1 From Vertical Report to Excel-Style Table](#41-from-vertical-report-to-excel-style-table)
    - [4.2 GroupBy vs. Pivot: What's the Difference?](#42-groupby-vs-pivot-whats-the-difference)
    - [4.3 The Excel Analogy](#43-the-excel-analogy)
  - [5. Part 3 — Merge: Combining DataFrames](#5-part-3--merge-combining-dataframes)
    - [5.1 Why We Need a Second Table](#51-why-we-need-a-second-table)
    - [5.2 Visualizing a Merge](#52-visualizing-a-merge)
  - [6. Part 4 — Join Types](#6-part-4--join-types)
    - [6.1 Inner Join](#61-inner-join)
    - [6.2 Left Join](#62-left-join)
    - [6.3 Mental Model for All Join Types](#63-mental-model-for-all-join-types)
  - [7. Part 5 — Resample: Time-Based Grouping](#7-part-5--resample-time-based-grouping)
    - [7.1 The Problem with Millions of Rows](#71-the-problem-with-millions-of-rows)
    - [7.2 Preparing the Date Column](#72-preparing-the-date-column)
    - [7.3 Setting the Date as Index](#73-setting-the-date-as-index)
    - [7.4 Resampling at Different Frequencies](#74-resampling-at-different-frequencies)
    - [7.5 Combining Resample with Aggregation](#75-combining-resample-with-aggregation)
  - [8. Summary \& Key Takeaways](#8-summary--key-takeaways)
  - [9. Practice Exercises](#9-practice-exercises)

---

## 1. Introduction & Scenario

Imagine you are a data analyst for an e-commerce company. You have sales transactions, employee information, and customer information, and management wants several different reports built from that data.

In this lab, you will use four of the most important tools in the pandas library to answer real business questions:

| Tool | Purpose |
|---|---|
| `groupby()` | Summarize data by category (e.g., total sales per employee) |
| `pivot_table()` | Reshape a summary into an Excel-style row/column layout |
| `merge()` | Combine two related tables into one |
| `resample()` | Summarize data across time periods (day, week, month) |

By the end of this lab you should be able to look at a business question and know which of these four tools to reach for.

---

## 2. Setup: Building the Dataset

Before working with any of the four operations, you need a dataset. Run the cell below to create a small sales table.

```python
import pandas as pd

sales = pd.DataFrame({
    'date': [
        '2026-01-01',
        '2026-01-01',
        '2026-01-02',
        '2026-01-02',
        '2026-01-03',
        '2026-01-03'
    ],
    'employee': ['Alice', 'Bob', 'Alice', 'Bob', 'Carol', 'Alice'],
    'department': ['Sales', 'Sales', 'Sales', 'Sales', 'Support', 'Sales'],
    'product': ['Laptop', 'Mouse', 'Laptop', 'Keyboard', 'Mouse', 'Keyboard'],
    'amount': [1200, 50, 1200, 80, 60, 80]
})

sales
```

**Output:**

```text
         date employee department   product  amount
0  2026-01-01    Alice      Sales    Laptop    1200
1  2026-01-01      Bob      Sales     Mouse      50
2  2026-01-02    Alice      Sales    Laptop    1200
3  2026-01-02      Bob      Sales  Keyboard      80
4  2026-01-03    Carol    Support     Mouse      60
5  2026-01-03    Alice      Sales  Keyboard      80
```

### Understand the data before you touch it

Before running any operation on this table, answer the following questions for yourself:

- What does **one row** represent?
- What is a **column**, and what does the `amount` column represent?
- Which columns are **categories** (things you might group by) and which are **measurements** (things you might sum or average)?

Understanding this now matters because every operation in this lab — GroupBy, Pivot, Merge, and Resample — manipulates this same table in a different way.

---

## 3. Part 1 — GroupBy: Aggregating Data

### 3.1 The Business Question

Rather than starting from syntax, start from the question a manager would actually ask:

> **"Who sold the most?"**

With six rows, you could answer this by eye. But real transaction tables can have millions of rows — you need code that scales. This is exactly the problem `groupby()` solves.

```python
sales.groupby('employee')['amount'].sum()
```

**Output:**

```text
employee
Alice    2480
Bob       130
Carol      60
Name: amount, dtype: int64
```

### 3.2 The Split → Apply → Combine Model

The single most important concept in this section is the mental model behind `groupby()`:

> **GROUPBY = Split → Apply → Combine**

```text
                    sales
                      |
        ┌─────────────┼─────────────┐
        ↓             ↓             ↓
      Alice          Bob          Carol
        |             |             |
       SUM           SUM           SUM
        |             |             |
        └─────────────┼─────────────┘
                       ↓
                     result
```

1. **Split** the table into groups (one group per employee).
2. **Apply** a calculation to each group independently (e.g., `sum`).
3. **Combine** the results back into a single table or series.

Keep this diagram in mind — every `groupby()` call you write, no matter how complex, is doing exactly this.

### 3.3 Level 1: One Aggregation

**Question:** What is the total sales for each department?

```python
sales.groupby('department')['amount'].sum()
```

**Output:**

```text
department
Sales      2610
Support      60
Name: amount, dtype: int64
```

### 3.4 Level 2: Multiple Aggregations

**Question:** Why would a business want more than just the total?

A manager rarely wants only a sum — they usually also want the average transaction size, how many transactions occurred, and the largest single transaction. Pass a list of functions to `agg()` to get all of them at once:

```python
sales.groupby('department')['amount'].agg(['sum', 'mean', 'count', 'max'])
```

**Output:**

```text
             sum   mean  count   max
department
Sales       2610  522.0      5  1200
Support       60   60.0      1    60
```

Think in terms of what each column tells a manager:

- `sum` → total revenue
- `mean` → average transaction size
- `count` → number of transactions
- `max` → largest single transaction

### 3.5 Level 3: Grouping by Two Columns

**Question:** What if management wants sales broken down by department **and** product?

```python
sales.groupby(['department', 'product'])['amount'].sum()
```

**Output:**

```text
department  product
Sales       Keyboard     160
            Laptop      2400
            Mouse         50
Support     Mouse         60
Name: amount, dtype: int64
```

This produces a hierarchical (multi-level) index. Conceptually, it looks like this:

```text
Sales
├── Keyboard   160
├── Laptop    2400
└── Mouse       50

Support
└── Mouse       60
```

### 3.6 Level 4: Controlling the Index with `as_index`

```python
result = sales.groupby('employee', as_index=False)['amount'].sum()
result
```

**Output:**

```text
  employee  amount
0    Alice    2480
1      Bob     130
2    Carol      60
```

- `as_index=True` (the default) makes the grouping column the **index** of the result.
- `as_index=False` keeps the grouping column as a **normal column** instead.

This distinction matters a lot once you start sorting results or feeding them into a pivot table — remember it for the next section.

### 3.7 Challenge: Find the Top Performer

**Task:** Find the employee with the highest total sales.

```python
emp_sales = (
    sales.groupby('employee', as_index=False)['amount']
    .sum()
)

emp_sales.sort_values('amount', ascending=False)
```

**Output:**

```text
  employee  amount
0    Alice    2480
1      Bob     130
2    Carol      60
```

An alternative approach uses `idxmax()` to find the row directly:

```python
emp_sales.loc[emp_sales['amount'].idxmax()]
```

**Output:**

```text
employee    Alice
amount       2480
Name: 0, dtype: object
```

> **Key distinction:** `groupby()` calculates the numbers. `idxmax()` identifies which row corresponds to the maximum value.

---

## 4. Part 2 — Pivot Tables: Reshaping Data

### 4.1 From Vertical Report to Excel-Style Table

Suppose a manager says: *"I don't want this report vertically — I want it laid out like Excel, with rows and columns."*

The target layout looks like this:

```text
Department   Laptop   Mouse   Keyboard
Sales          2400      50        160
Support           0      60          0
```

`pivot_table()` produces exactly this shape:

```python
pd.pivot_table(
    sales,
    values='amount',
    index='department',
    columns='product',
    aggfunc='sum',
    fill_value=0
)
```

**Output:**

```text
product     Keyboard  Laptop  Mouse
department
Sales            160    2400     50
Support             0       0     60
```

### 4.2 GroupBy vs. Pivot: What's the Difference?

These two operations are the most commonly confused tools in pandas. Compare them side by side using the **same** data.

**GroupBy:**

```python
sales.groupby(['department', 'product'])['amount'].sum()
```

```text
department  product
Sales       Keyboard     160
            Laptop      2400
            Mouse         50
Support     Mouse         60
Name: amount, dtype: int64
```

**Pivot:**

```python
pd.pivot_table(
    sales,
    values='amount',
    index='department',
    columns='product',
    aggfunc='sum',
    fill_value=0
)
```

```text
product     Keyboard  Laptop  Mouse
department
Sales            160    2400     50
Support             0       0     60
```

Both calculate the exact same numbers. The difference is entirely about **shape**:

> **GroupBy is primarily for aggregation.**
> **Pivot is primarily for reshaping an already-aggregated view.**

### 4.3 The Excel Analogy

If you have used Excel PivotTables before, `pivot_table()` will feel very familiar:

| Excel PivotTable | pandas `pivot_table()` |
|---|---|
| Rows → `department` | `index='department'` |
| Columns → `product` | `columns='product'` |
| Values → `amount` | `values='amount'` |
| Calculation → Sum | `aggfunc='sum'` |

```python
pd.pivot_table(
    sales,
    index='department',
    columns='product',
    values='amount',
    aggfunc='sum'
)
```

---

## 5. Part 3 — Merge: Combining DataFrames

### 5.1 Why We Need a Second Table

The `sales` table tells you *what happened*, but it doesn't tell you anything about the employees themselves. To answer questions like *"which office location generated the most revenue?"* you need a second table.

```python
employees = pd.DataFrame({
    'employee': ['Alice', 'Bob', 'Carol'],
    'designation': ['Senior Sales', 'Sales Executive', 'Support Executive'],
    'location': ['NY', 'Boston', 'Chicago']
})

employees
```

**Output:**

```text
  employee        designation location
0    Alice       Senior Sales       NY
1      Bob    Sales Executive   Boston
2    Carol  Support Executive  Chicago
```

**Question:** How do we attach `designation` and `location` to the sales data?

```python
sales.merge(employees, on='employee')
```

**Output:**

```text
         date employee department   product  amount        designation location
0  2026-01-01    Alice      Sales    Laptop    1200       Senior Sales       NY
1  2026-01-01      Bob      Sales     Mouse      50    Sales Executive   Boston
2  2026-01-02    Alice      Sales    Laptop    1200       Senior Sales       NY
3  2026-01-02      Bob      Sales  Keyboard      80    Sales Executive   Boston
4  2026-01-03    Carol    Support     Mouse      60  Support Executive  Chicago
5  2026-01-03    Alice      Sales  Keyboard      80       Senior Sales       NY
```

### 5.2 Visualizing a Merge

```text
   SALES TABLE                    EMPLOYEES TABLE
   employee                       employee
   --------                       --------
   Alice     ─────────────────>   Alice
   Bob       ─────────────────>   Bob
   Carol     ─────────────────>   Carol
```

`employee` is the **key** — the column both tables have in common.

```python
sales.merge(employees, on='employee')
```

In plain language, this line means: *"Find the same employee in both tables and bring the matching columns together."*

---

## 6. Part 4 — Join Types

Create a modified employees table where one employee, David, has no sales records at all:

```python
employees = pd.DataFrame({
    'employee': ['Alice', 'Bob', 'Carol', 'David'],
    'designation': [
        'Senior Sales',
        'Sales Executive',
        'Support Executive',
        'Manager'
    ],
    'location': ['NY', 'Boston', 'Chicago', 'SFO']
})

employees
```

**Output:**

```text
  employee        designation   location
0    Alice       Senior Sales   NY
1      Bob    Sales Executive   Boston
2    Carol  Support Executive   Chicago
3    David            Manager   SFO
```

**Question:** Should David appear in a merged sales report if he has no sales?

The answer depends entirely on which **join type** you choose.

### 6.1 Inner Join

Keeps only employees that exist in **both** tables.

```python
sales.merge(employees, on='employee', how='inner')
```

**Output:**

```text
         date employee department   product  amount        designation location
0  2026-01-01    Alice      Sales    Laptop    1200       Senior Sales       NY
1  2026-01-01      Bob      Sales     Mouse      50    Sales Executive   Boston
2  2026-01-02    Alice      Sales    Laptop    1200       Senior Sales       NY
3  2026-01-02      Bob      Sales  Keyboard      80    Sales Executive   Boston
4  2026-01-03    Carol    Support     Mouse      60  Support Executive  Chicago
5  2026-01-03    Alice      Sales  Keyboard      80       Senior Sales       NY
```

Notice David does **not** appear — he has no matching sales record.

### 6.2 Left Join

Keeps **every** sales record, even if the matching employee information is missing.

```python
sales.merge(employees, on='employee', how='left')
```

**Output:**

```text
         date employee department   product  amount        designation location
0  2026-01-01    Alice      Sales    Laptop    1200       Senior Sales       NY
1  2026-01-01      Bob      Sales     Mouse      50    Sales Executive   Boston
2  2026-01-02    Alice      Sales    Laptop    1200       Senior Sales       NY
3  2026-01-02      Bob      Sales  Keyboard      80    Sales Executive   Boston
4  2026-01-03    Carol    Support     Mouse      60  Support Executive  Chicago
5  2026-01-03    Alice      Sales  Keyboard      80       Senior Sales       NY
```

In this particular dataset the left join looks identical to the inner join, because every employee who appears in `sales` also appears in `employees`. The difference would only show up if a sales record referenced an employee who was **missing** from the employees table — that row would still be kept, with `NaN` in the employee-info columns.

### 6.3 Mental Model for All Join Types

```text
inner  → keep only rows where the key exists in BOTH tables
left   → keep everything from the LEFT table, fill in matches where possible
right  → keep everything from the RIGHT table, fill in matches where possible
outer  → keep everything from BOTH tables
```

---

## 7. Part 5 — Resample: Time-Based Grouping

### 7.1 The Problem with Millions of Rows

**Question:** Suppose you have 5 million transactions, one per day, and management asks: *"What were our total sales for each month?"*

```text
2026-01-01    100
2026-01-02    250
2026-01-03    300
...
2026-02-01    500
...
```

You could technically extract the month from each date and use `groupby()`. But pandas has a tool built specifically for **time-based grouping**: `resample()`.

### 7.2 Preparing the Date Column

Before resampling, pandas needs to know that the `date` column actually contains dates, not just text.

```python
sales['date'].dtype
```

**Output (before conversion):**

```text
object
```

Convert the column using `pd.to_datetime()`:

```python
sales['date'] = pd.to_datetime(sales['date'])
sales['date'].dtype
```

**Output (after conversion):**

```text
datetime64[ns]
```

> **Note:** Depending on your installed pandas version you may see `datetime64[ns]` or `datetime64[us]`. Both are proper datetime types — the important change is that the column is no longer plain text.

### 7.3 Setting the Date as Index

`resample()` requires a `DatetimeIndex`, so make `date` the index of the DataFrame:

```python
sales = sales.set_index('date')
sales
```

**Output:**

```text
           employee department   product  amount
date
2026-01-01    Alice      Sales    Laptop    1200
2026-01-01      Bob      Sales     Mouse      50
2026-01-02    Alice      Sales    Laptop    1200
2026-01-02      Bob      Sales  Keyboard      80
2026-01-03    Carol    Support     Mouse      60
2026-01-03    Alice      Sales  Keyboard      80
```

### 7.4 Resampling at Different Frequencies

```python
sales['amount'].resample('D').sum()
```

**Output — `'D'` means Day:**

```text
date
2026-01-01    1250
2026-01-02    1280
2026-01-03     140
Freq: D, Name: amount, dtype: int64
```

```python
sales['amount'].resample('W').sum()
```

**Output — `'W'` means Week:**

```text
date
2026-01-04    2670
Freq: W-SUN, Name: amount, dtype: int64
```

```python
sales['amount'].resample('ME').sum()
```

**Output — `'ME'` means Month-End frequency:**

```text
date
2026-01-31    2670
Freq: ME, Name: amount, dtype: int64
```

Notice the progression in frequency:

```text
resample('D')  →  resample('W')  →  resample('ME')
   (daily)          (weekly)         (monthly)
```

### 7.5 Combining Resample with Aggregation

Just like `groupby()`, `resample()` accepts multiple aggregation functions:

```python
sales['amount'].resample('ME').agg(['sum', 'mean', 'count'])
```

**Output:**

```text
             sum   mean  count
date
2026-01-31  2670  445.0      6
```

**Question:** Can we find the month with the highest sales?

```python
monthly = sales['amount'].resample('ME').sum()
monthly.idxmax()
```

**Output:**

```text
Timestamp('2026-01-31 00:00:00')
```

```python
monthly.max()
```

**Output:**

```text
2670
```

With more months of data, this same pattern (`resample()` → `idxmax()` / `max()`) is exactly how you would identify the best- or worst-performing month.

---

## 8. Summary & Key Takeaways

| Operation | Use it when you need to... | Core syntax |
|---|---|---|
| `groupby()` | Aggregate rows into per-category summaries | `df.groupby(col)[value].agg(func)` |
| `pivot_table()` | Reshape a summary into a row/column (Excel-style) layout | `pd.pivot_table(df, index=, columns=, values=, aggfunc=)` |
| `merge()` | Combine two tables using a shared key column | `df1.merge(df2, on=key, how=join_type)` |
| `resample()` | Aggregate time-series data into fixed time periods | `df[value].resample(freq).agg(func)` |

Remember the four mental models from this lab:

1. **GroupBy = Split → Apply → Combine**
2. **GroupBy aggregates; Pivot reshapes.**
3. **Merge finds matching keys across two tables.**
4. **Inner keeps the intersection; Left keeps everything from the left table; Outer keeps everything from both.**

---

## 9. Practice Exercises

Try these on your own using the `sales` and `employees` DataFrames built in this lab, or with a larger dataset of your own.

1. Use `groupby()` to find the total `amount` per **product** (not per department or employee).
2. Use `pivot_table()` to build a table with `employee` as rows, `product` as columns, and total `amount` as values.
3. Create a third DataFrame called `customers` with at least two columns, then `merge()` it into an existing report using an appropriate join type. Explain why you chose that join type.
4. Using `resample()`, calculate the **average** daily sales amount instead of the sum.
5. **Challenge:** Combine everything — group the data by `department`, then merge in employee `location`, then find which `location` had the highest total sales.
