# DATA SCIENCE LABORATORY — DS-LAB-02
## NumPy & Pandas: Foundations of Data Engineering
*Retail Sales Analytics │ From Raw Data to Business Insight*

---

| Field | Details |
|---|---|
| **Lab Code** | DS-LAB-02 |
| Topic | NumPy — Numerical Computing │ Pandas — Data Handling |
| Industry Use Case | Retail Sales Analytics & Business Intelligence |
| Difficulty Progression | Beginner → Intermediate → Advanced |
| Estimated Duration | 4 – 5 Hours |
| Prerequisites | Basic Python (variables, loops, functions) |
| Version | NumPy 1.26 │ Pandas 2.1 │ Python 3.11 |

---

## Learning Objectives

1. Understand NumPy arrays and why they outperform plain Python lists for numerical work
2. Master array creation, indexing, slicing, reshaping, and broadcasting
3. Apply NumPy statistical and mathematical operations to real retail data
4. Load, inspect, and clean a real-world business dataset using Pandas
5. Filter, sort, group, and aggregate data to answer business questions
6. Handle missing values, duplicates, and data type issues professionally
7. Merge datasets, apply custom transformations, and produce analytic summaries
8. Build an end-to-end analytics pipeline from raw CSV data to executive report

## Business Context

**Company:** RetailEdge Pvt. Ltd. (Mid-size retail chain — 12 stores across India)

**Challenge:** The analytics team has received raw transactional data from multiple stores. They need to clean, transform, and analyze it to answer executive-level business questions.

**Dataset:** `RetailEdge_Sales.csv` — 10,000 rows covering sales from Jan–Dec 2023

**Columns:** OrderID, Date, Store, Region, Category, Product, Units, Unit_Price, Discount, Revenue, Profit, SalesRep

**Goal:** Use NumPy and Pandas to uncover insights, fix data quality issues, and generate reports.

---

## Table of Contents

- [DATA SCIENCE LABORATORY — DS-LAB-02](#data-science-laboratory--ds-lab-02)
  - [NumPy \& Pandas: Foundations of Data Engineering](#numpy--pandas-foundations-of-data-engineering)
  - [Learning Objectives](#learning-objectives)
  - [Business Context](#business-context)
  - [Table of Contents](#table-of-contents)
  - [Part 1 — NumPy: Numerical Computing](#part-1--numpy-numerical-computing)
  - [Chapter 1: Introduction to NumPy](#chapter-1-introduction-to-numpy)
    - [1.1 Why NumPy?](#11-why-numpy)
    - [1.2 Installation and Import](#12-installation-and-import)
    - [1.3 Creating Arrays — The Basics](#13-creating-arrays--the-basics)
      - [1.3.1 From Python Lists](#131-from-python-lists)
      - [1.3.2 Built-in Array Creation Functions](#132-built-in-array-creation-functions)
      - [1.3.3 Random Arrays](#133-random-arrays)
    - [1.4 Indexing and Slicing](#14-indexing-and-slicing)
      - [1.4.1 1-D Indexing](#141-1-d-indexing)
      - [1.4.2 2-D Indexing](#142-2-d-indexing)
      - [1.4.3 Boolean Indexing (Conditional Selection)](#143-boolean-indexing-conditional-selection)
    - [1.5 Array Operations and Vectorization](#15-array-operations-and-vectorization)
      - [1.5.1 Element-wise Arithmetic](#151-element-wise-arithmetic)
      - [1.5.2 Broadcasting](#152-broadcasting)
      - [1.5.3 Aggregate Functions](#153-aggregate-functions)
      - [1.5.4 Mathematical and Financial Functions](#154-mathematical-and-financial-functions)
  - [Part 2 — Pandas: Data Handling \& Analysis](#part-2--pandas-data-handling--analysis)
  - [Chapter 2: Introduction to Pandas](#chapter-2-introduction-to-pandas)
    - [2.1 Pandas Data Structures](#21-pandas-data-structures)
      - [2.1.1 Series — 1-D Labeled Array](#211-series--1-d-labeled-array)
      - [2.1.2 DataFrame — 2-D Labeled Table](#212-dataframe--2-d-labeled-table)
    - [2.2 Loading Real Data — RetailEdge\_Sales.csv](#22-loading-real-data--retailedge_salescsv)
      - [2.2.1 DataFrame.describe() — Statistical Summary](#221-dataframedescribe--statistical-summary)
    - [2.3 Data Selection and Indexing](#23-data-selection-and-indexing)
      - [2.3.1 Column Selection](#231-column-selection)
      - [2.3.2 Row Selection — loc and iloc](#232-row-selection--loc-and-iloc)
      - [2.3.3 Boolean Filtering](#233-boolean-filtering)
      - [2.3.4 The .query() Method](#234-the-query-method)
    - [2.4 Data Cleaning — Professional Practices](#24-data-cleaning--professional-practices)
      - [2.4.1 Detecting and Handling Missing Values](#241-detecting-and-handling-missing-values)
      - [2.4.2 Handling Duplicates](#242-handling-duplicates)
      - [2.4.3 Data Type Corrections](#243-data-type-corrections)
      - [2.4.4 Detecting and Handling Outliers](#244-detecting-and-handling-outliers)
    - [2.5 Data Transformation](#25-data-transformation)
      - [2.5.1 Creating Derived Columns](#251-creating-derived-columns)
      - [2.5.2 The apply() and map() Methods](#252-the-apply-and-map-methods)
      - [2.5.3 Sorting and Ranking](#253-sorting-and-ranking)
    - [2.6 GroupBy and Aggregation — The Analytical Core](#26-groupby-and-aggregation--the-analytical-core)
      - [2.6.1 Basic GroupBy](#261-basic-groupby)
      - [2.6.2 Multi-level GroupBy](#262-multi-level-groupby)
      - [2.6.3 GroupBy with Transform (Group-Level Enrichment)](#263-groupby-with-transform-group-level-enrichment)
    - [2.7 Pivot Tables and Cross-tabulation](#27-pivot-tables-and-cross-tabulation)
    - [2.8 Merging and Joining DataFrames](#28-merging-and-joining-dataframes)
    - [2.9 Time Series Analysis with Pandas](#29-time-series-analysis-with-pandas)
  - [Appendix A: NumPy Quick Reference](#appendix-a-numpy-quick-reference)
  - [Appendix B: Pandas Quick Reference](#appendix-b-pandas-quick-reference)
  - [Appendix C: Glossary](#appendix-c-glossary)

---

## Part 1 — NumPy: Numerical Computing

## Chapter 1: Introduction to NumPy

NumPy (Numerical Python) is the cornerstone library for numerical computation in Python. It provides a powerful N-dimensional array object, mathematical functions, random number generation, linear algebra routines, and much more — all implemented in optimized C/Fortran code, making it orders of magnitude faster than pure Python.

> **RetailEdge Context:** Before loading data into Pandas, raw numerical arrays from sensors, point-of-sale systems, and inventory tracking arrive as flat number sequences. NumPy allows the analytics team to pre-process these efficiently — computing revenue totals, applying discount matrices, and flagging outliers — before formal data analysis begins.

### 1.1 Why NumPy?

Python lists are flexible but slow for numerical work. NumPy arrays store data in contiguous memory blocks and support vectorized operations — applying the same operation to every element without a Python loop.

| Python List | NumPy ndarray |
|---|---|
| Stores any type (int, str, dict) | Stores one fixed type (dtype) |
| Each element is a Python object with overhead | Elements are raw C values — minimal overhead |
| Loops are needed for element-wise operations | Vectorized ops work element-wise automatically |
| Memory: scattered; cache-unfriendly | Memory: contiguous block; cache-friendly |
| Speed: slow for large numerical arrays | Speed: typically 10x–100x faster than lists |

### 1.2 Installation and Import

```python
# Install (run once in terminal)
pip install numpy

# Standard import convention — always use 'np' alias
import numpy as np

# Verify version
print(np.__version__)    # Expected: 1.26.x

# Alternatively can use Jupyter Lab installed within windows VM.
You do. not have to install anything as its readily available. Just type in the cell
import numpy as np 
It will work
```

### 1.3 Creating Arrays — The Basics

#### 1.3.1 From Python Lists

The most common way to create a NumPy array is from an existing Python list using `np.array()`.

```python
# 1-D array — one week of daily units sold at Store Mumbai
daily_units = np.array([120, 95, 142, 88, 210, 175, 133])
print(daily_units)
print('Shape:', daily_units.shape)     # (7,)
print('Dtype:', daily_units.dtype)     # int64
print('Dimensions:', daily_units.ndim) # 1

# 2-D array — 4 stores x 7 days of units sold
weekly_units = np.array([
    [120, 95, 142, 88, 210, 175, 133],   # Mumbai
    [98,  112, 89, 104, 188, 160, 120],  # Delhi
    [75,  80,  95, 70,  155, 130, 110],  # Pune
    [50,  60,  55, 65,  120, 95,  80],   # Jaipur
])
print(weekly_units.shape)   # (4, 7)  => 4 rows, 7 columns
print(weekly_units.ndim)    # 2
```

**Output:**
```
[120  95 142  88 210 175 133]
Shape: (7,)
Dtype: int64
Dimensions: 1
(4, 7)
2
```

#### 1.3.2 Built-in Array Creation Functions

NumPy provides factory functions to create arrays without manually listing values — essential for initializing matrices in data pipelines.

```python
# np.zeros — initialize a discount matrix (4 stores x 12 months) with 0
discount_matrix = np.zeros((4, 12))
print(discount_matrix.shape)    # (4, 12)

# np.ones — initialize all unit prices to 1 (to be filled later)
price_vector = np.ones(10) * 499.0   # 10 products at Rs. 499
print(price_vector)

# np.arange — months 1 through 12
months = np.arange(1, 13)            # [1 2 3 4 ... 12]

# np.linspace — 5 evenly spaced discount rates between 0% and 30%
discount_tiers = np.linspace(0, 0.30, 5)
print(discount_tiers)   # [0.   0.075 0.15 0.225 0.30]

# np.eye — identity matrix (useful in linear algebra)
identity = np.eye(3)

# np.full — create a 3x3 array filled with 100 (e.g., base target units)
target_units = np.full((3, 3), 100)
```

#### 1.3.3 Random Arrays

Random arrays are used for simulations, sampling, and initializing model weights in machine learning.

```python
# Set seed for reproducibility
np.random.seed(42)

# Simulate 1000 random order values (Rs. 100 to Rs. 5000)
simulated_orders = np.random.uniform(100, 5000, size=1000)
print('Min:', simulated_orders.min())   # ~100
print('Max:', simulated_orders.max())   # ~5000

# Simulate daily foot traffic using a normal distribution
# Mean = 300 customers/day, Std = 50
# np.random.normal() is a NumPy function used to generate random numbers according to a normal distribution.
# loc means the mean (average).
# scale represents the standard deviation
# size=365 Generate 365 random numbers.

foot_traffic = np.random.normal(loc=300, scale=50, size=365)
print('Mean traffic:', foot_traffic.mean().round(2))

# Random integers — simulate units sold (1 to 20 per transaction)
txn_units = np.random.randint(1, 21, size=500)
```

---

### 1.4 Indexing and Slicing

Accessing elements of an array is a core skill. NumPy extends Python list slicing to multiple dimensions, enabling powerful data selection patterns.

#### 1.4.1 1-D Indexing

```python
daily_revenue = np.array([45200, 38900, 52100, 41300, 67800, 59400, 48700])
# Days: Mon    Tue    Wed    Thu    Fri    Sat    Sun

# Single element (0-indexed)
print(daily_revenue[0])     # 45200  (Monday)
print(daily_revenue[-1])    # 48700  (Sunday)

# Slicing [start:stop:step]
print(daily_revenue[1:4])   # [38900 52100 41300]  (Tue to Thu)
print(daily_revenue[::2])   # [45200 52100 67800 48700]  (every other day)
print(daily_revenue[::-1])  # Reverse the array
```

#### 1.4.2 2-D Indexing

For 2-D arrays (matrices), use `[row, column]` syntax. Think of rows as stores and columns as days/months.

```python
# weekly_units shape: (4 stores x 7 days)
# Rows: Mumbai, Delhi, Pune, Jaipur

# Single element — Delhi's (row 1) Thursday (col 3) sales
print(weekly_units[1, 3])        # 104

# Entire row — all of Pune's weekly sales
print(weekly_units[2, :])        # [75 80 95 70 155 130 110]

# Entire column — all stores' Friday (col 4) sales
print(weekly_units[:, 4])        # [210 188 155 120]

# Sub-matrix — first 2 stores, first 3 days
print(weekly_units[:2, :3])
# [[120  95 142]
#  [ 98 112  89]]

# Last 2 stores, weekend (cols 5 and 6)
print(weekly_units[2:, 5:])
# [[130 110]
#  [ 95  80]]
```

#### 1.4.3 Boolean Indexing (Conditional Selection)

Select elements that satisfy a condition — one of the most powerful NumPy features for data filtering.

```python
revenue = np.array([45200, 38900, 52100, 41300, 67800, 59400, 48700])

# Days where revenue exceeded Rs. 50,000
high_rev_mask = revenue > 50000
print(high_rev_mask)         # [False False  True False  True  True False]
print(revenue[high_rev_mask])# [52100 67800 59400]

# Shorthand (inline condition)
print(revenue[revenue > 50000])

# Multiple conditions — revenue between 40k and 60k
mid_rev = revenue[(revenue >= 40000) & (revenue <= 60000)]
print(mid_rev)    # [45200 52100 41300 59400 48700]

# Which days had below-average revenue?
avg = revenue.mean()
below_avg_days = np.where(revenue < avg, 'Below Avg', 'Above Avg')
print(below_avg_days)
# ['Above' 'Below' 'Above' 'Below' 'Above' 'Above' 'Below']
```

---

### 1.5 Array Operations and Vectorization

NumPy operations are vectorized — they apply to every element automatically, without Python for-loops. This is both faster and more readable.

#### 1.5.1 Element-wise Arithmetic

```python
units  = np.array([120, 95, 142, 88, 210, 175, 133])
prices = np.array([499, 799, 299, 999, 199, 599, 449])

# Revenue = units * price  (element-wise multiplication)
revenue = units * prices
print(revenue)
# [59880 75905 42458 87912 41790 104825 59717]

# Apply a 10% discount to all products
discounted_prices = prices * (1 - 0.10)
print(discounted_prices.round(2))

# Gross Profit = Revenue - Cost (assume 40% of price is cost)
cost = units * prices * 0.40
gross_profit = revenue - cost
print(gross_profit.sum())   # Total week's gross profit

# Add a Rs. 50 shipping fee to all prices
prices_with_shipping = prices + 50

# Operations between same-shaped arrays are element-wise
last_week = np.array([100, 80, 130, 90, 200, 160, 120])
growth = ((units - last_week) / last_week) * 100
print('WoW Growth %:', growth.round(1))
```

#### 1.5.2 Broadcasting

Broadcasting allows NumPy to perform operations on arrays with different shapes by automatically expanding dimensions — a crucial concept for applying store-level adjustments across all days.

```python
# weekly_units shape: (4 stores x 7 days)
# Suppose each store has a different price per unit
store_prices = np.array([499, 799, 299, 999])  # shape (4,)

# We want revenue for each store-day cell
# store_prices must become shape (4,1) to broadcast across 7 columns
revenue_matrix = weekly_units * store_prices.reshape(4, 1)
print(revenue_matrix.shape)   # (4, 7)
print(revenue_matrix)

# Apply a different monthly discount per store (4 discounts x 1 column)
discounts = np.array([0.10, 0.15, 0.05, 0.20]).reshape(4, 1)
net_revenue = revenue_matrix * (1 - discounts)
print('Net Revenue Matrix:', net_revenue.round(0))
```

**Output:**
```
revenue_matrix shape: (4, 7)
[[59880 47405 70858 43912 104790 87325 66367]
 [78302 89488 71111 83096 150212 127840 95880]
 [22425 23920 28405 20930  46345  38870  32890]
 [49950 59940 54945 64935 119880  94905  79920]]
```

#### 1.5.3 Aggregate Functions

NumPy provides powerful aggregation functions. Most accept an `axis` parameter to aggregate across rows (`axis=1`) or columns (`axis=0`).

```python
# Total units sold per store (sum across columns = axis=1)
store_totals = weekly_units.sum(axis=1)
print('Store Totals:', store_totals)    # [963, 871, 715, 525]

# Total units sold per day (sum across rows = axis=0)
daily_totals = weekly_units.sum(axis=0)
print('Daily Totals:', daily_totals)    # [343 347 381 327 673 560 443]

# Best performing store (which row has highest total)?
best_store_idx = store_totals.argmax()
stores = ['Mumbai', 'Delhi', 'Pune', 'Jaipur']
print('Best store:', stores[best_store_idx])  # Mumbai

# Statistical summary
print('Mean daily units:', weekly_units.mean(axis=1).round(1))
print('Std deviation:', weekly_units.std(axis=1).round(1))
print('25th percentile:', np.percentile(weekly_units, 25))
print('Median:', np.median(weekly_units))
print('75th percentile:', np.percentile(weekly_units, 75))
```

#### 1.5.4 Mathematical and Financial Functions

```python
revenue_monthly = np.array([
    450000, 420000, 510000, 480000, 550000, 620000,
    590000, 540000, 610000, 680000, 720000, 800000,
])

# Month-over-month growth rate
mom_growth = np.diff(revenue_monthly) / revenue_monthly[:-1] * 100
print('MoM Growth %:', mom_growth.round(2))

# Cumulative revenue (running total for the year)
cumulative_rev = np.cumsum(revenue_monthly)
print('Cumulative Revenue:', cumulative_rev)

# Log transformation (used in skewed data analysis)
log_rev = np.log(revenue_monthly)

# Clip values — ensure no store reports < 0 or > 1M in a buggy dataset
clipped = np.clip(revenue_monthly, 400000, 750000)

# Round to nearest thousand
print(np.round(revenue_monthly, -3))
```
---

## Part 2 — Pandas: Data Handling & Analysis

## Chapter 2: Introduction to Pandas

Pandas (Panel Data) is Python's premier data analysis library. Built on top of NumPy, it introduces two core data structures — Series and DataFrame — designed for labeled, tabular data. Pandas provides Excel-like operations (filter, sort, group, pivot) combined with the power of a programming language, making it indispensable for business analytics.

> **RetailEdge Context:** The raw sales data arrives as a CSV file — `RetailEdge_Sales.csv`. The file has 10,000 rows, mixed data types, missing values, and duplicate entries. The analytics team will use Pandas to load, clean, transform, and analyze this data, ultimately answering questions like: Which store is most profitable? Which product category drives the most revenue? How does performance vary across regions and quarters?

### 2.1 Pandas Data Structures

#### 2.1.1 Series — 1-D Labeled Array

A Series is a one-dimensional array with labels (an index). Think of it as a single column from a spreadsheet.

```python
import pandas as pd
import numpy as np

# Series from a list — monthly revenue for Mumbai store
monthly_revenue = pd.Series(
    [450000, 420000, 510000, 480000, 550000, 620000,
     590000, 540000, 610000, 680000, 720000, 800000],
    index=['Jan','Feb','Mar','Apr','May','Jun',
           'Jul','Aug','Sep','Oct','Nov','Dec'],
    name='Mumbai Revenue (Rs.)'
)
print(monthly_revenue)
print('\nTotal:', monthly_revenue.sum())
print('Best Month:', monthly_revenue.idxmax())
print('Mean:', monthly_revenue.mean())
```

**Output:**
```
Jan     450000
Feb     420000
Mar     510000
...     ...
Dec     800000
Name: Mumbai Revenue (Rs.), dtype: int64

Total: 6970000
Best Month: Dec
Mean: 580833.33
```

#### 2.1.2 DataFrame — 2-D Labeled Table

A DataFrame is Pandas' primary data structure — a table with labeled rows and columns. Each column is a Series. This maps directly to a business spreadsheet or database table.

```python
# Creating a DataFrame manually (small sample of RetailEdge data)
data = {
    'OrderID'   : ['ORD001', 'ORD002', 'ORD003', 'ORD004', 'ORD005'],
    'Date'      : ['2023-01-05', '2023-01-07', '2023-01-08', '2023-01-10', '2023-01-12'],
    'Store'     : ['Mumbai', 'Delhi', 'Mumbai', 'Pune', 'Delhi'],
    'Category'  : ['Electronics', 'Clothing', 'Electronics', 'Home & Kitchen', 'Clothing'],
    'Product'   : ['Laptop', 'T-Shirt', 'Phone', 'Mixer', 'Jeans'],
    'Units'     : [2, 5, 3, 1, 4],
    'Unit_Price': [49999, 799, 24999, 2499, 1499],
    'Discount'  : [0.10, 0.05, 0.15, 0.00, 0.10],
    'Revenue'   : [89998.2, 3795.25, 63747.45, 2499, 5396.4],
    'Profit'    : [17999.64, 759.05, 12749.49, 374.85, 1079.28],
}
df = pd.DataFrame(data)
print(df.head())
print('\nShape:', df.shape)        # (5, 10)
print('Columns:', df.columns.tolist())
```

### 2.2 Loading Real Data — RetailEdge_Sales.csv

In practice, data is loaded from files, databases, or APIs — not typed manually. Pandas provides `read_csv()` to load CSV files, with dozens of parameters for handling messy real-world files.

```python
# Load the RetailEdge dataset
df = pd.read_csv('RetailEdge_Sales.csv')

# Common parameters for messy CSV files:
df = pd.read_csv(
    'RetailEdge_Sales.csv',
    parse_dates=['Date'],          # Auto-parse date column
    dtype={'OrderID': str},        # Force OrderID as string
    na_values=['N/A', 'NA', ''],   # Treat these as NaN
    encoding='utf-8',              # Handle special characters
)

# Always start with these 5 inspection commands
print(df.shape)          # (10000, 12) — rows x columns
print(df.head(3))        # First 3 rows
print(df.tail(3))        # Last 3 rows
print(df.dtypes)         # Data types of each column
print(df.info())         # Full info: non-null counts, dtypes, memory
```

#### 2.2.1 DataFrame.describe() — Statistical Summary

```python
# Numerical summary — count, mean, std, min, quartiles, max
print(df.describe())

# Include non-numeric columns too
print(df.describe(include='all'))

# Business interpretation:
# - If Units.min() < 0  => data quality issue (negative units)
# - If Discount.max() > 1  => likely percentage entered as 100 instead of 0-1
# - If Revenue std is very high vs mean => large outliers exist
```
---

### 2.3 Data Selection and Indexing

#### 2.3.1 Column Selection

```python
# Single column returns a Series
revenue = df['Revenue']
print(type(revenue))    # <class 'pandas.core.series.Series'>

# Multiple columns returns a DataFrame
subset = df[['Store', 'Category', 'Revenue', 'Profit']]
print(subset.head())

# Dot notation (only for simple column names without spaces)
print(df.Revenue.sum())   # Same as df['Revenue'].sum()
```

#### 2.3.2 Row Selection — loc and iloc

Pandas provides two primary indexers: `.loc[]` for label-based access and `.iloc[]` for integer position-based access.

```python
# .loc — label-based (use actual index labels and column names)
print(df.loc[0])                      # First row (all columns)
print(df.loc[0, 'Revenue'])           # Specific cell: row 0, Revenue col
print(df.loc[0:4, 'Store':'Revenue']) # Rows 0-4, cols Store to Revenue

# .iloc — position-based (use integer positions)
print(df.iloc[0])          # First row
print(df.iloc[-1])         # Last row
print(df.iloc[0:5, 0:4])   # First 5 rows, first 4 columns
print(df.iloc[:, -3:])     # All rows, last 3 columns

# KEY DIFFERENCE: loc includes the stop index, iloc does NOT
# df.loc[0:4] → rows 0,1,2,3,4  (5 rows)
# df.iloc[0:4] → rows 0,1,2,3   (4 rows)
```

#### 2.3.3 Boolean Filtering

The most powerful selection tool — filter rows based on conditions, just like SQL WHERE clauses.

```python
# Single condition — orders from Mumbai store
mumbai = df[df['Store'] == 'Mumbai']
print(f'Mumbai orders: {len(mumbai)}')         # e.g., 2450

# Multiple conditions (& = AND, | = OR, ~ = NOT)
# High-value electronics orders
high_value_elec = df[
    (df['Category'] == 'Electronics') &
    (df['Revenue'] > 50000)
]

# Orders that are either from Mumbai OR have Profit > 10000
priority = df[(df['Store'] == 'Mumbai') | (df['Profit'] > 10000)]

# NOT — exclude Clothing category
non_clothing = df[~(df['Category'] == 'Clothing')]

# .isin() — multiple values at once (like SQL IN clause)
metro_stores = df[df['Store'].isin(['Mumbai', 'Delhi', 'Pune'])]

# String methods with .str accessor
laptop_orders = df[df['Product'].str.contains('Laptop', case=False)]
high_discount  = df[df['Discount'] >= 0.20]   # 20% or more discount
```

#### 2.3.4 The .query() Method

For complex filtering, `.query()` accepts SQL-like string expressions and is often more readable.

```python
# Equivalent to: df[(df['Store']=='Mumbai') & (df['Revenue']>50000)]
result = df.query("Store == 'Mumbai' and Revenue > 50000")

# Using variables in query (prefix with @)
min_rev = 30000
filtered = df.query('Revenue > @min_rev and Category == "Electronics"')

# Date filtering
q2_orders = df.query('Date >= "2023-04-01" and Date <= "2023-06-30"')
print(f'Q2 orders: {len(q2_orders)}')
```
---

### 2.4 Data Cleaning — Professional Practices

Real business data is almost always messy. Data cleaning is consistently reported as consuming 60-80% of a data analyst's time. Pandas provides a complete toolkit for identifying and resolving data quality issues.

#### 2.4.1 Detecting and Handling Missing Values

```python
# Step 1: Assess missing values
missing = df.isnull().sum()
missing_pct = (df.isnull().sum() / len(df) * 100).round(2)
missing_report = pd.DataFrame({'Missing Count': missing, 'Missing %': missing_pct})
print(missing_report[missing_report['Missing Count'] > 0])

# Step 2: Visualize missing pattern
print(df.isnull().any(axis=1).sum(), 'rows have at least one missing value')

# Step 3a: Drop rows where Revenue is missing (critical column)
df_clean = df.dropna(subset=['Revenue'])

# Step 3b: Fill numerical missing values with median (robust to outliers)
df_clean['Profit'] = df_clean['Profit'].fillna(df_clean['Profit'].median())

# Step 3c: Fill categorical missing values with mode (most frequent)
df_clean['SalesRep'] = df_clean['SalesRep'].fillna(df_clean['SalesRep'].mode()[0])

# Step 3d: Forward fill — carry last known value forward (time-series)
df_clean['Category'] = df_clean['Category'].fillna(method='ffill')

# Step 3e: Fill with a specific business-logic value
df_clean['Discount'] = df_clean['Discount'].fillna(0.0)   # No discount = 0

print('Missing values after cleaning:', df_clean.isnull().sum().sum())
```

#### 2.4.2 Handling Duplicates

```python
# Detect duplicates
total_dupes = df.duplicated().sum()
print(f'Duplicate rows: {total_dupes}')

# Check duplicates based on specific columns (OrderID should be unique)
order_dupes = df.duplicated(subset=['OrderID']).sum()
print(f'Duplicate OrderIDs: {order_dupes}')

# View the duplicate rows
print(df[df.duplicated(subset=['OrderID'], keep=False)].head())

# Remove duplicates — keep first occurrence
df_clean = df.drop_duplicates(subset=['OrderID'], keep='first')
print(f'Rows after deduplication: {len(df_clean)}')
```

#### 2.4.3 Data Type Corrections

```python
# Check dtypes
print(df.dtypes)

# Convert Date column (often loaded as object/string)
df['Date'] = pd.to_datetime(df['Date'], format='%Y-%m-%d')

# Extract time components
df['Year']    = df['Date'].dt.year
df['Month']   = df['Date'].dt.month
df['Quarter'] = df['Date'].dt.quarter
df['DayName'] = df['Date'].dt.day_name()    # 'Monday', 'Tuesday', ...

# Convert Discount to % if stored as 10 instead of 0.10
if df['Discount'].max() > 1:
    df['Discount'] = df['Discount'] / 100

# Convert categorical columns to 'category' dtype (saves memory)
for col in ['Store', 'Region', 'Category']:
    df[col] = df[col].astype('category')

# Strip whitespace from string columns (common in CSV files)
str_cols = df.select_dtypes(include='object').columns
for col in str_cols:
    df[col] = df[col].str.strip()

print('\nCleaned dtypes:')
print(df.dtypes)
```

#### 2.4.4 Detecting and Handling Outliers

```python
# IQR Method — flag statistical outliers in Revenue
Q1 = df['Revenue'].quantile(0.25)
Q3 = df['Revenue'].quantile(0.75)
IQR = Q3 - Q1

lower_bound = Q1 - 1.5 * IQR
upper_bound = Q3 + 1.5 * IQR

outliers = df[(df['Revenue'] < lower_bound) | (df['Revenue'] > upper_bound)]
print(f'Revenue outliers: {len(outliers)} ({len(outliers)/len(df)*100:.1f}%)')
print(outliers[['OrderID','Store','Product','Revenue']].head())

# Strategy 1: Remove outliers
df_no_outliers = df[(df['Revenue'] >= lower_bound) & (df['Revenue'] <= upper_bound)]

# Strategy 2: Cap outliers (Winsorization)
df['Revenue_Capped'] = df['Revenue'].clip(lower=lower_bound, upper=upper_bound)

# Strategy 3: Flag for investigation
df['is_outlier'] = ((df['Revenue'] < lower_bound) |
                    (df['Revenue'] > upper_bound)).astype(int)
```

---

### 2.5 Data Transformation

#### 2.5.1 Creating Derived Columns

Derived columns turn raw data into business metrics — profit margins, revenue bands, performance tiers.

```python
# Profit Margin (%)
df['Profit_Margin_Pct'] = (df['Profit'] / df['Revenue'] * 100).round(2)

# Revenue Band — business segmentation
def revenue_band(rev):
    if rev < 1000:    return 'Small'
    elif rev < 10000: return 'Medium'
    elif rev < 50000: return 'Large'
    else:             return 'Enterprise'

df['Revenue_Band'] = df['Revenue'].apply(revenue_band)

# Or using pd.cut for numerical binning
df['Revenue_Tier'] = pd.cut(
    df['Revenue'],
    bins=[0, 1000, 10000, 50000, float('inf')],
    labels=['Small', 'Medium', 'Large', 'Enterprise'],
    right=False
)

# Discount impact — revenue without discount
df['Gross_Revenue'] = df['Units'] * df['Unit_Price']
df['Discount_Amount'] = df['Gross_Revenue'] - df['Revenue']

# Performance flag
df['Above_Avg_Profit'] = df['Profit'] > df['Profit'].mean()
```

#### 2.5.2 The apply() and map() Methods

```python
# apply() on a column — custom function per row value
def classify_store_tier(store):
    tier1 = ['Mumbai', 'Delhi', 'Pune']
    tier2 = ['Jaipur', 'Nagpur', 'Ahmedabad']
    return 'Tier 1' if store in tier1 else 'Tier 2'

df['Store_Tier'] = df['Store'].apply(classify_store_tier)

# apply() with axis=1 — row-wise (uses multiple columns)
def effective_price(row):
    return row['Unit_Price'] * (1 - row['Discount'])

df['Effective_Price'] = df.apply(effective_price, axis=1)

# map() — simple value replacement using dictionary
region_map = {
    'Mumbai': 'West', 'Pune': 'West', 'Nagpur': 'Central',
    'Delhi':  'North', 'Jaipur': 'North',
    'Chennai': 'South', 'Hyderabad': 'South',
}
df['Region_Derived'] = df['Store'].map(region_map)

# Lambda functions — concise one-liners
df['Profit_Rs_Thousands'] = df['Profit'].apply(lambda x: round(x/1000, 1))
```

#### 2.5.3 Sorting and Ranking

```python
# Sort by Revenue descending
top_orders = df.sort_values('Revenue', ascending=False).head(10)
print(top_orders[['OrderID','Store','Product','Revenue','Profit']])

# Multi-column sort — by Store then Revenue
df_sorted = df.sort_values(['Store', 'Revenue'], ascending=[True, False])

# Rank within dataset
df['Revenue_Rank'] = df['Revenue'].rank(ascending=False, method='dense').astype(int)

# Rank within groups (store-level percentile rank)
df['Store_Revenue_Rank'] = df.groupby('Store')['Revenue'].rank(
    ascending=False, method='dense'
).astype(int)
```

---

### 2.6 GroupBy and Aggregation — The Analytical Core

GroupBy is Pandas' most powerful analytical feature — equivalent to SQL GROUP BY. It allows you to split data into groups, apply aggregate functions, and combine results into a summary table.

#### 2.6.1 Basic GroupBy

```python
# Total Revenue and Profit by Store
store_summary = df.groupby('Store')[['Revenue', 'Profit']].sum()
print(store_summary.sort_values('Revenue', ascending=False))

# Multiple aggregations — mean, sum, count
store_stats = df.groupby('Store')['Revenue'].agg(['sum', 'mean', 'count', 'std'])
store_stats.columns = ['Total_Revenue', 'Avg_Order', 'Order_Count', 'Revenue_Std']
print(store_stats)

# Named aggregations (Pandas 0.25+) — most readable approach
store_report = df.groupby('Store').agg(
    Total_Revenue  = ('Revenue', 'sum'),
    Total_Profit   = ('Profit',  'sum'),
    Order_Count    = ('OrderID', 'count'),
    Avg_Discount   = ('Discount','mean'),
    Max_Order      = ('Revenue', 'max'),
).round(2).reset_index()
store_report['Profit_Margin'] = (store_report['Total_Profit'] / store_report['Total_Revenue'] * 100).round(1)
print(store_report.sort_values('Total_Revenue', ascending=False))
```

#### 2.6.2 Multi-level GroupBy

```python
# Revenue by Store AND Category
store_cat = df.groupby(['Store', 'Category'])['Revenue'].sum().reset_index()
store_cat.columns = ['Store', 'Category', 'Revenue']
print(store_cat.sort_values(['Store', 'Revenue'], ascending=[True, False]))

# Quarterly performance by Region
quarterly = df.groupby(['Region', 'Quarter']).agg(
    Revenue = ('Revenue', 'sum'),
    Profit  = ('Profit', 'sum'),
    Orders  = ('OrderID', 'count')
).reset_index()
quarterly['Margin'] = (quarterly['Profit']/quarterly['Revenue']*100).round(1)
print(quarterly)

# Top product per store
top_product = (df.groupby(['Store', 'Product'])['Revenue']
               .sum()
               .reset_index()
               .sort_values('Revenue', ascending=False)
               .groupby('Store')
               .first()
               .reset_index())
print(top_product[['Store', 'Product', 'Revenue']])
```

#### 2.6.3 GroupBy with Transform (Group-Level Enrichment)

Unlike `agg()` which reduces rows, `transform()` returns a result with the same length as the original DataFrame — enabling group-level enrichments.

```python
# Add each order's store's total annual revenue as a column
df['Store_Annual_Revenue'] = df.groupby('Store')['Revenue'].transform('sum')

# Each order's % contribution to its store's revenue
df['Revenue_Share_Pct'] = (df['Revenue'] / df['Store_Annual_Revenue'] * 100).round(4)

# Flag if order is above its store's average
df['Store_Avg_Revenue'] = df.groupby('Store')['Revenue'].transform('mean')
df['Above_Store_Avg'] = df['Revenue'] > df['Store_Avg_Revenue']

# Normalize revenue within each category (z-score)
def zscore(series):
    return (series - series.mean()) / series.std()

df['Revenue_ZScore'] = df.groupby('Category')['Revenue'].transform(zscore)
print(df[['Store', 'Category', 'Revenue', 'Revenue_ZScore']].head(10))
```

---

### 2.7 Pivot Tables and Cross-tabulation

Pivot tables are multi-dimensional summaries — the analytical backbone of business reports. They allow slicing the same data across two or more dimensions simultaneously.

```python
# pivot_table — Revenue by Store (rows) and Quarter (columns)
pivot = pd.pivot_table(
    df,
    values='Revenue',
    index='Store',
    columns='Quarter',
    aggfunc='sum',
    fill_value=0,
    margins=True,          # Add row/column totals
    margins_name='Total'
)
pivot.columns = ['Q1', 'Q2', 'Q3', 'Q4', 'Total']
print(pivot.round(0))

# Multi-value pivot — both Revenue and Profit
multi_pivot = pd.pivot_table(
    df,
    values=['Revenue', 'Profit'],
    index='Region',
    columns='Category',
    aggfunc='sum'
)

# Cross-tabulation — frequency count (like a contingency table)
# How many orders per Store per Category?
ct = pd.crosstab(df['Store'], df['Category'], margins=True)
print(ct)

# Normalize — proportion instead of count
ct_pct = pd.crosstab(df['Store'], df['Category'], normalize='index').round(3)
print(ct_pct)
```

### 2.8 Merging and Joining DataFrames

Business data rarely exists in a single table. Merging is the Pandas equivalent of SQL JOINs — combining data from multiple sources using a common key.

```python
# Sample supplementary tables
store_info = pd.DataFrame({
    'Store'   : ['Mumbai', 'Delhi', 'Pune', 'Jaipur', 'Nagpur'],
    'Manager' : ['Raj Patel', 'Priya Sharma', 'Amit Singh', 'Neha Gupta', 'Rohit Jain'],
    'Sq_Ft'   : [8000, 6500, 5500, 4000, 3500],
    'Tier'    : ['Metro', 'Metro', 'Metro', 'Tier2', 'Tier2'],
})

product_info = pd.DataFrame({
    'Product'   : ['Laptop', 'Phone', 'T-Shirt', 'Mixer', 'Jeans'],
    'Brand'     : ['HP', 'Samsung', 'Arrow', 'Bajaj', 'Levis'],
    'Warranty_Yr': [1, 1, 0, 2, 0],
})

# INNER JOIN — only matching rows (default)
df_enriched = df.merge(store_info, on='Store', how='inner')
print(f'Rows after inner join: {len(df_enriched)}')

# LEFT JOIN — keep all sales, fill missing store info with NaN
df_left = df.merge(store_info, on='Store', how='left')

# Chain multiple merges
df_full = (df
    .merge(store_info,   on='Store',   how='left')
    .merge(product_info, on='Product', how='left'))
print(df_full[['Store','Tier','Product','Brand','Revenue']].head())
```

### 2.9 Time Series Analysis with Pandas

Pandas was built with time series in mind. With a datetime index, you unlock powerful resampling, rolling statistics, and lag features critical for trend analysis.

```python
# Set Date as the index for time-series operations
ts = df.set_index('Date').sort_index()

# Resample to weekly revenue total
weekly_rev = ts['Revenue'].resample('W').sum()
print(weekly_rev.head())

# Resample to monthly — multiple aggregations
monthly = ts.resample('M').agg(
    Revenue = ('Revenue', 'sum'),
    Orders  = ('Revenue', 'count'),
    Profit  = ('Profit',  'sum')
)
monthly['Margin'] = (monthly['Profit']/monthly['Revenue']*100).round(1)
print(monthly)

# 7-day rolling average (smooth out weekly cycles)
ts['Rolling_7d_Rev'] = ts['Revenue'].rolling(window=7).mean()

# Month-over-month growth
monthly['MoM_Growth_Pct'] = monthly['Revenue'].pct_change() * 100

# Lag feature — compare today's sales to 7 days ago
ts['Revenue_7d_lag'] = ts['Revenue'].shift(7)
ts['WoW_Change'] = ts['Revenue'] - ts['Revenue_7d_lag']
```

---

## Appendix A: NumPy Quick Reference

| Function | Description | Example |
|---|---|---|
| `np.array()` | Create array from list | `np.array([1, 2, 3])` |
| `np.zeros(shape)` | Array of zeros | `np.zeros((3,4))` |
| `np.ones(shape)` | Array of ones | `np.ones(5)` |
| `np.arange(start,stop,step)` | Evenly spaced values | `np.arange(0,10,2)` |
| `np.linspace(start,stop,n)` | n points between start–stop | `np.linspace(0,1,5)` |
| `np.random.normal(mu,sigma,n)` | Normal distribution | `np.random.normal(0,1,100)` |
| `arr.reshape(shape)` | Change shape | `arr.reshape(3,4)` |
| `arr.sum(axis=)` | Sum along axis | `arr.sum(axis=1)` |
| `np.dot(A, B)` | Dot/matrix product | `A @ B` |
| `np.corrcoef(a,b)` | Pearson correlation matrix | `np.corrcoef(x,y)` |
| `np.percentile(arr,q)` | Compute percentile | `np.percentile(arr, 25)` |
| `np.where(cond,a,b)` | Conditional element choice | `np.where(arr>0,'Pos','Neg')` |
| `np.argsort(arr)` | Indices that would sort | `arr[np.argsort(arr)]` |
| `np.cumsum(arr)` | Cumulative sum | `np.cumsum([1,2,3])` |
| `np.diff(arr)` | Consecutive differences | `np.diff([10,20,25])` |

---

## Appendix B: Pandas Quick Reference

| Operation | Code | Description |
|---|---|---|
| Load CSV | `pd.read_csv('file.csv', parse_dates=['col'])` | Load with auto date parsing |
| Shape | `df.shape` | Rows x columns |
| Info | `df.info()` | Dtypes and non-null counts |
| Missing | `df.isnull().sum()` | Missing value count per column |
| Describe | `df.describe()` | Statistical summary of numerics |
| Select cols | `df[['A','B']]` | Select multiple columns |
| Filter rows | `df[df['col'] > x]` | Boolean filtering |
| loc | `df.loc[row, col]` | Label-based indexing |
| iloc | `df.iloc[i, j]` | Position-based indexing |
| Sort | `df.sort_values('col', ascending=False)` | Sort by column |
| GroupBy | `df.groupby('col').agg(x=('col2','sum'))` | Named aggregation |
| Pivot Table | `pd.pivot_table(df, values, index, columns)` | Multi-dim summary |
| Merge | `df.merge(df2, on='key', how='left')` | SQL-style join |
| Apply | `df['col'].apply(func)` | Custom function per element |
| Drop NA | `df.dropna(subset=['col'])` | Remove rows with NaN |
| Fill NA | `df['col'].fillna(value)` | Replace NaN with value |
| Resample | `ts.resample('M').sum()` | Time-based aggregation |
| Rolling | `ts['col'].rolling(7).mean()` | Moving window statistics |
| to_numpy() | `df['col'].to_numpy()` | Convert Series to NumPy array |

---

## Appendix C: Glossary

**ndarray:** NumPy's core N-dimensional array object — the backbone of all numerical computation.

**dtype:** The data type of array elements — int64, float64, bool, object, etc.

**Broadcasting:** NumPy's mechanism to perform operations on arrays of different (compatible) shapes.

**Vectorization:** Applying an operation to an entire array without explicit Python loops.

**Series:** A 1-D Pandas array with a labeled index — analogous to a single spreadsheet column.

**DataFrame:** A 2-D labeled table with rows and named columns — Pandas' primary data structure.

**Boolean Indexing:** Selecting elements by applying a True/False condition array as a mask.

**GroupBy:** Splitting a DataFrame by a key, applying a function, and combining results.

**Pivot Table:** A multi-dimensional summary aggregating values across two or more categorical axes.

**Merge/Join:** Combining two DataFrames on a common key column (SQL-equivalent JOIN).

**Missing Value (NaN):** Not a Number — Pandas' representation of absent or undefined data.

**Resampling:** Aggregating time series data to a different frequency (daily → weekly → monthly).

**Rolling Window:** Applying a function over a sliding window of consecutive rows.

**IQR:** Interquartile Range (Q3 − Q1) — a robust measure of data spread used for outlier detection.

---

*End of Lab Document — NumPy & Pandas: Foundations of Data Engineering*
