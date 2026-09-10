# Pandas Lab: GroupBy, Pivot, Merge & Resample

A hands-on progression from **simple → intermediate → advanced**, using only in-memory Python dictionaries (no CSV files needed). Each section has a short concept intro, exercises, and hidden solutions you can reveal after attempting them yourself.

---

## Setup

Run this once at the top of your notebook/script. It creates all the sample datasets used throughout the lab.

```python
import pandas as pd
import numpy as np

# ---------------------------------------------------------
# Dataset 1: Retail Sales (used for groupby & pivot)
# ---------------------------------------------------------
sales_data = {
    "order_id":  [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12],
    "date":      pd.to_datetime([
        "2024-01-05", "2024-01-08", "2024-01-15", "2024-01-20",
        "2024-02-02", "2024-02-10", "2024-02-18", "2024-02-25",
        "2024-03-01", "2024-03-09", "2024-03-15", "2024-03-28"
    ]),
    "region":    ["East", "West", "East", "South", "West", "East",
                  "South", "West", "East", "South", "West", "East"],
    "category":  ["Electronics", "Furniture", "Furniture", "Electronics",
                  "Electronics", "Furniture", "Furniture", "Electronics",
                  "Furniture", "Electronics", "Furniture", "Electronics"],
    "sales_rep": ["Alice", "Bob", "Alice", "Carol", "Bob", "Alice",
                  "Carol", "Bob", "Alice", "Carol", "Bob", "Alice"],
    "units":     [3, 5, 2, 7, 4, 6, 3, 8, 5, 2, 9, 4],
    "revenue":   [450, 620, 300, 980, 560, 720, 410, 1150, 640, 275, 990, 530],
}

df_sales = pd.DataFrame(sales_data)

# ---------------------------------------------------------
# Dataset 2: Sales Reps info (used for merge)
# ---------------------------------------------------------
reps_data = {
    "sales_rep": ["Alice", "Bob", "Carol", "Dave"],
    "hire_year": [2019, 2021, 2020, 2023],
    "team":      ["Team A", "Team B", "Team A", "Team B"],
}
df_reps = pd.DataFrame(reps_data)

# ---------------------------------------------------------
# Dataset 3: Regional Targets (used for merge - mismatched keys on purpose)
# ---------------------------------------------------------
targets_data = {
    "region": ["East", "West", "North"],   # note: "South" missing, "North" extra
    "quarterly_target": [2000, 2500, 1800],
}
df_targets = pd.DataFrame(targets_data)

# ---------------------------------------------------------
# Dataset 4: Hourly Sensor Readings (used for resample)
# ---------------------------------------------------------
rng = pd.date_range("2024-01-01", periods=72, freq="H")
np.random.seed(42)
sensor_data = {
    "timestamp": rng,
    "temperature": np.round(20 + 5 * np.sin(np.linspace(0, 6*np.pi, 72)) + np.random.randn(72), 2),
    "humidity": np.round(50 + 10 * np.cos(np.linspace(0, 6*np.pi, 72)) + np.random.randn(72)*2, 2),
}
df_sensor = pd.DataFrame(sensor_data)
df_sensor = df_sensor.set_index("timestamp")

print(df_sales.head())
print(df_reps)
print(df_targets)
print(df_sensor.head())
```

---

## Level 1 — Simple

### 1.1 GroupBy Basics

**Concept:** `.groupby("col")` splits a DataFrame into groups; chain an aggregation (`.sum()`, `.mean()`, `.count()`, etc.) to combine results.

**Exercises (use `df_sales`):**

1. Find total `revenue` by `region`.
2. Find the average `units` sold per `category`.
3. Count how many orders each `sales_rep` has.
4. Find total revenue by `region`, sorted from highest to lowest.

<details>
<summary>💡 Solutions</summary>

```python
# 1
df_sales.groupby("region")["revenue"].sum()

# 2
df_sales.groupby("category")["units"].mean()

# 3
df_sales.groupby("sales_rep")["order_id"].count()

# 4
df_sales.groupby("region")["revenue"].sum().sort_values(ascending=False)
```
</details>

---

### 1.2 Simple Pivot Table

**Concept:** `pd.pivot_table(df, values=, index=, columns=, aggfunc=)` reshapes long data into a summary grid — like an Excel PivotTable.

**Exercises:**

1. Build a pivot table with `region` as rows, `category` as columns, and the **sum of revenue** as values.
2. Build a pivot table showing the **average units** sold, with `sales_rep` as rows and `category` as columns.

<details>
<summary>💡 Solutions</summary>

```python
# 1
pd.pivot_table(df_sales, values="revenue", index="region",
                columns="category", aggfunc="sum")

# 2
pd.pivot_table(df_sales, values="units", index="sales_rep",
                columns="category", aggfunc="mean")
```
</details>

---

### 1.3 Simple Merge

**Concept:** `pd.merge(left, right, on=, how=)` joins two DataFrames on a shared key, similar to a SQL join.

**Exercises:**

1. Merge `df_sales` with `df_reps` on `sales_rep` to attach each order's `team` and `hire_year`.
2. From the merged result, find total revenue by `team`.

<details>
<summary>💡 Solutions</summary>

```python
# 1
df_merged = pd.merge(df_sales, df_reps, on="sales_rep", how="left")

# 2
df_merged.groupby("team")["revenue"].sum()
```
</details>

---

### 1.4 Simple Resample

**Concept:** `.resample(rule)` groups a **DatetimeIndex** into time buckets (e.g., `"D"` = daily, `"6H"` = 6-hourly) so you can aggregate over time.

**Exercises (use `df_sensor`):**

1. Resample to daily frequency and compute the mean `temperature` per day.
2. Resample to 6-hour buckets and find the max `humidity` in each bucket.

<details>
<summary>💡 Solutions</summary>

```python
# 1
df_sensor["temperature"].resample("D").mean()

# 2
df_sensor["humidity"].resample("6H").max()
```
</details>

---

## Level 2 — Intermediate

### 2.1 GroupBy with Multiple Keys & Aggregations

**Concept:** You can group by multiple columns, and apply multiple aggregation functions at once using `.agg()`.

**Exercises:**

1. Group `df_sales` by `["region", "category"]` and get the sum of `revenue` and `units` together.
2. Using `.agg()`, compute the **sum**, **mean**, and **max** of `revenue` per `region` in a single call, with clean column names (e.g., `total_revenue`, `avg_revenue`, `max_revenue`).
3. Find, for each `sales_rep`, the number of orders and the total revenue — then filter to reps with more than 3 orders.

<details>
<summary>💡 Solutions</summary>

```python
# 1
df_sales.groupby(["region", "category"])[["revenue", "units"]].sum()

# 2
df_sales.groupby("region").agg(
    total_revenue=("revenue", "sum"),
    avg_revenue=("revenue", "mean"),
    max_revenue=("revenue", "max"),
)

# 3
rep_summary = df_sales.groupby("sales_rep").agg(
    num_orders=("order_id", "count"),
    total_revenue=("revenue", "sum"),
)
rep_summary[rep_summary["num_orders"] > 3]
```
</details>

---

### 2.2 Pivot Table with Multiple Aggregations & Margins

**Concept:** `pivot_table` supports multiple `values`/`aggfunc`, and a `margins=True` flag to add row/column totals.

**Exercises:**

1. Build a pivot table with `region` as rows, `category` as columns, values = `revenue`, aggregated by **both** `sum` and `mean`.
2. Build a pivot table of total `revenue` by `region` (rows) and `category` (columns), including grand totals (`margins=True`, `margins_name="Total"`).
3. Replace missing combinations with `0` instead of `NaN` (hint: `fill_value`).

<details>
<summary>💡 Solutions</summary>

```python
# 1
pd.pivot_table(df_sales, values="revenue", index="region",
                columns="category", aggfunc=["sum", "mean"])

# 2
pd.pivot_table(df_sales, values="revenue", index="region",
                columns="category", aggfunc="sum",
                margins=True, margins_name="Total")

# 3
pd.pivot_table(df_sales, values="revenue", index="region",
                columns="category", aggfunc="sum", fill_value=0)
```
</details>

---

### 2.3 Merge Types (inner/left/right/outer) & Indicator

**Concept:** The `how` parameter controls which rows survive a merge. `indicator=True` shows where each row came from — great for debugging mismatched keys.

**Exercises (use `df_sales` and `df_targets`, joining on `region`):**

1. Do an **inner** merge — which regions survive?
2. Do a **left** merge (keep all sales rows) — what happens to `South`'s target?
3. Do an **outer** merge with `indicator=True` and inspect the `_merge` column to see which rows matched, and which came only from `df_sales` or only from `df_targets`.

<details>
<summary>💡 Solutions</summary>

```python
# 1 - only East and West survive (South & North drop out)
pd.merge(df_sales, df_targets, on="region", how="inner")

# 2 - South rows are kept, quarterly_target is NaN for them
pd.merge(df_sales, df_targets, on="region", how="left")

# 3
merged_outer = pd.merge(df_sales, df_targets, on="region",
                         how="outer", indicator=True)
merged_outer["_merge"].value_counts()
```
</details>

---

### 2.4 Resample with Multiple Aggregations & Upsampling

**Concept:** `.resample().agg([...])` runs multiple functions per bucket. Resampling to a **finer** frequency than the data (upsampling) creates gaps you can fill with `.ffill()`/`.interpolate()`.

**Exercises (use `df_sensor`):**

1. Resample daily and get `min`, `max`, and `mean` of `temperature` in one call.
2. Resample to 2-day buckets, computing `mean` for `temperature` and `sum` for... well, humidity doesn't have a natural "sum," so instead compute `mean` for both columns using `.agg({"temperature": "mean", "humidity": "mean"})`.
3. Downsample to daily mean, then upsample back to hourly using `.resample("H").ffill()` to forward-fill the daily value across each hour.

<details>
<summary>💡 Solutions</summary>

```python
# 1
df_sensor["temperature"].resample("D").agg(["min", "max", "mean"])

# 2
df_sensor.resample("2D").agg({"temperature": "mean", "humidity": "mean"})

# 3
daily_mean = df_sensor.resample("D").mean()
daily_mean.resample("H").ffill()
```
</details>

---

## Level 3 — Advanced

### 3.1 GroupBy: Custom Functions, `transform`, and `apply`

**Concept:** `.transform()` returns an output the **same shape** as the input (great for adding group-level stats back onto each row). `.apply()` lets you run arbitrary custom logic per group.

**Exercises:**

1. Add a new column `region_avg_revenue` to `df_sales` that holds the average revenue of that row's region (using `.transform`).
2. Add a column `pct_of_region_revenue` showing what percent each order's revenue is of its region's total revenue.
3. Using `.apply()`, write a function that returns the **top 2 highest-revenue orders** within each `category`.

<details>
<summary>💡 Solutions</summary>

```python
# 1
df_sales["region_avg_revenue"] = df_sales.groupby("region")["revenue"].transform("mean")

# 2
region_totals = df_sales.groupby("region")["revenue"].transform("sum")
df_sales["pct_of_region_revenue"] = (df_sales["revenue"] / region_totals * 100).round(1)

# 3
def top_n(group, n=2):
    return group.nlargest(n, "revenue")

df_sales.groupby("category", group_keys=False).apply(top_n)
```
</details>

---

### 3.2 Pivot + GroupBy Combined: Multi-level Analysis

**Concept:** Real analysis often chains groupby/pivot with reshaping (`.unstack()`, `.stack()`) and multi-index columns.

**Exercises:**

1. Group `df_sales` by `["region", "category"]`, sum `revenue`, then use `.unstack("category")` to reshape `category` into columns — confirm it matches a `pivot_table` with the same spec.
2. Build a pivot table showing **both revenue and units**, summed, with `region` as rows and `category` as columns (multi-level columns). Then flatten the resulting column MultiIndex into single strings like `revenue_Electronics`.
3. From the pivot in (2), compute a new column `Electronics_revenue_share` = each region's Electronics revenue divided by that region's total revenue across all categories.

<details>
<summary>💡 Solutions</summary>

```python
# 1
grouped = df_sales.groupby(["region", "category"])["revenue"].sum().unstack("category")
pivot_check = pd.pivot_table(df_sales, values="revenue", index="region", columns="category", aggfunc="sum")
grouped.equals(pivot_check)  # True (modulo column ordering)

# 2
pv = pd.pivot_table(df_sales, values=["revenue", "units"], index="region",
                     columns="category", aggfunc="sum")
pv.columns = [f"{val}_{cat}" for val, cat in pv.columns]
pv = pv.reset_index()

# 3
revenue_cols = [c for c in pv.columns if c.startswith("revenue_")]
pv["row_total_revenue"] = pv[revenue_cols].sum(axis=1)
pv["Electronics_revenue_share"] = (pv["revenue_Electronics"] / pv["row_total_revenue"] * 100).round(1)
```
</details>

---

### 3.3 Advanced Merge: Multi-key Joins, Suffixes & Validation

**Concept:** Merges can use multiple key columns, need `suffixes` when both frames share non-key column names, and can be `validate`d to catch unexpected duplicate keys.

**Exercises:**

1. Create a second "promotions" dictionary keyed by `region` **and** `category`, then merge it onto `df_sales` using both keys together.
2. Merge `df_sales` with `df_reps`, but first rename `df_reps`'s columns so there's a name collision on `region` (add a fake `region` column to `df_reps` for practice) — perform the merge and use `suffixes=("_sale", "_rep")` to disambiguate.
3. Use `validate="many_to_one"` when merging `df_sales` (many rows per rep) with `df_reps` (one row per rep) to confirm the relationship is what you expect. Then deliberately duplicate a row in `df_reps` and show that `validate` raises an error.

<details>
<summary>💡 Solutions</summary>

```python
# 1
promo_data = {
    "region":   ["East", "East", "West", "South"],
    "category": ["Electronics", "Furniture", "Electronics", "Furniture"],
    "promo_active": [True, False, True, True],
}
df_promo = pd.DataFrame(promo_data)
pd.merge(df_sales, df_promo, on=["region", "category"], how="left")

# 2
df_reps_fake = df_reps.copy()
df_reps_fake["region"] = ["East", "West", "South", "North"]
pd.merge(df_sales, df_reps_fake, on="sales_rep", suffixes=("_sale", "_rep"))

# 3
pd.merge(df_sales, df_reps, on="sales_rep", how="left", validate="many_to_one")  # passes

df_reps_dup = pd.concat([df_reps, df_reps.iloc[[0]]])  # duplicate Alice's row
try:
    pd.merge(df_sales, df_reps_dup, on="sales_rep", how="left", validate="many_to_one")
except Exception as e:
    print("Validation caught it:", e)
```
</details>

---

### 3.4 Advanced Resample: Custom Offsets, `apply`, and Groupby + Resample Together

**Concept:** `.resample()` can take a custom function via `.apply()`, and can be **combined with `.groupby()`** to resample within categories (e.g., per-sensor-region time series).

**Exercises:**

1. Add a `zone` column to a copy of `df_sensor` that splits readings into `"Zone A"` (first 36 rows) and `"Zone B"` (last 36 rows). Then compute the **daily mean temperature per zone** using `.groupby("zone").resample("D")`.
2. Write a custom function that computes the **temperature range** (max - min) in each resample bucket, and apply it with `.resample("D").apply(custom_func)`.
3. Resample hourly temperature to daily using an **anchored custom offset**: compute the mean using a business-day frequency (`"B"`) instead of calendar day, and explain (in a comment) why the bucket counts differ from `"D"`.

<details>
<summary>💡 Solutions</summary>

```python
# 1
df_zone = df_sensor.copy()
df_zone["zone"] = ["Zone A"] * 36 + ["Zone B"] * 36
df_zone.groupby("zone")["temperature"].resample("D").mean()

# 2
def temp_range(series):
    return series.max() - series.min()

df_sensor["temperature"].resample("D").apply(temp_range)

# 3
df_sensor["temperature"].resample("B").mean()
# "B" buckets only land on weekdays, so weekend hours get folded into
# the nearest preceding/following business-day bucket rather than getting
# their own calendar-day bucket like "D" would produce.
```
</details>

---

## Capstone Challenge (All Four Topics Together)

Using everything above, starting from `df_sales`, `df_reps`, and `df_targets`:

1. Merge `df_sales` with `df_reps` to attach `team`.
2. Group the merged data by `["team", "region"]` and compute total revenue and total units.
3. Pivot that result so `team` is rows and `region` is columns, values = total revenue.
4. Separately, take `df_sensor`, resample to daily, and merge the daily average temperature onto `df_sales` by matching each order's `date` to the sensor's daily date (hint: normalize both to date-only before merging) — then check whether higher temperatures correlate with higher revenue that day.

<details>
<summary>💡 Solution</summary>

```python
# 1
df_full = pd.merge(df_sales, df_reps, on="sales_rep", how="left")

# 2
team_region_summary = df_full.groupby(["team", "region"]).agg(
    total_revenue=("revenue", "sum"),
    total_units=("units", "sum"),
).reset_index()

# 3
pivot_final = pd.pivot_table(team_region_summary, values="total_revenue",
                              index="team", columns="region", aggfunc="sum")

# 4
daily_temp = df_sensor["temperature"].resample("D").mean().reset_index()
daily_temp["date_only"] = daily_temp["timestamp"].dt.date

df_sales_copy = df_sales.copy()
df_sales_copy["date_only"] = df_sales_copy["date"].dt.date

merged_weather = pd.merge(df_sales_copy, daily_temp, on="date_only", how="left")
merged_weather[["revenue", "temperature"]].corr()
```
</details>

---

### Tips for Practicing Further
- Try re-running each exercise with `as_index=False` in `groupby` to see how the output shape changes.
- Experiment with `pivot_table(..., aggfunc="count")` to build frequency tables.
- Try `merge(..., how="cross")` (pandas ≥ 1.2) to see a full cross-join.
- Try `.resample("W")` (weekly) and `.resample("M")` (month-end) on `df_sensor` to compare bucket sizes.
