# Pandas Practice: Lab, Exercise & Solutions

This document combines the lab code, the exercise questions, and the worked solutions in one place.

---

## 1. Lab (`lab.py`)

```python
import pandas as pd

data = {
    'name': ['Alice', 'Bob', 'Charlie', 'David'],
    'age': [25, 30, 22, 35],
    'salary': [50000, 60000, 45000, 70000]
}

df = pd.DataFrame(data, index=(1,2,3,4))

print(df)
print(df.head())
print(df.shape)
print(df.columns)
print(df.dtypes)
print(df.info())

# Selecting columns
print(df['name'])
print(df[['name', 'salary']])

# Selecting rows
# iloc works using the position of the row
print(df.iloc[0])
print()
print(df.iloc[0:2])
print()
print(df.iloc[1:2])

print()
print(df.loc[1])
print()
print(df.loc[1:3, ['name', 'salary']])

#filtering
print(df[df['salary'] > 50000])
print(df[(df['age'] > 25) & (df['salary'] > 50000)])

# add a column
df['bonus'] = df['salary'] * 0.10
print(df)

#update values
df['salary'] = df['salary'] * 1.05

#Sorting
print(df.sort_values('salary'))
print(df.sort_values(
    ['age', 'salary'],
    ascending=[True, False]
))

# Basic Statistics
print(df['salary'].mean())
print(df['salary'].max())
print(df['salary'].min())
print(df['salary'].sum())
print(df['salary'].median())


# More data
data = {
    'employee': ['Alice', 'Bob', 'Alice', 'Charlie', 'Bob', 'Alice'],
    'department': ['IT', 'Sales', 'IT', 'HR', 'Sales', 'IT'],
    'product': ['Laptop', 'Phone', 'Monitor', 'Laptop', 'Tablet', 'Phone'],
    'sales': [1200, 800, 600, 1500, 900, 700]
}

df = pd.DataFrame(data)

# Group By

print(df.groupby('employee')['sales'].sum()) # Total sales by employee

print(df.groupby('department')['sales'].mean()) #Average sales by department

print(df.groupby('department')['sales'].agg(
    ['sum', 'mean', 'max', 'min']
)) # multiple aggregations

# group by multiple columns

#How much did each department sell for each product?
df.groupby(
    ['department', 'product']
)['sales'].sum()

# Rename column
df.rename(
    columns={'sales': 'sales_amount'},
    inplace=True
)

# Missing Values

data = {
    'name': ['Alice', 'Bob', 'Charlie', 'David'],
    'age': [25, None, 30, 28],
    'salary': [50000, 60000, None, 70000]
}

df = pd.DataFrame(data)

print(df.isnull()) # check mmissing values

print(df.isnull().sum())

# remove rows
df.dropna()

# apply allows to apply function to values

df['salary_category'] = df['salary'].apply(
    lambda x: 'High' if x >= 60000 else 'Low'
)

def categorize_salary(salary):
    if salary >= 60000:
        return 'High'
    else:
        return 'Low'

df['category'] = df['salary'].apply(categorize_salary)

# String operations

df['name'] = df['name'].str.upper()
df['name'].str.lower()
df['name'].str.len()
df['name'].str.contains('A')

# working with dates

data = {
    'order_id': [101, 102, 103, 104],
    'order_date': [
        '2026-01-10',
        '2026-02-15',
        '2026-02-20',
        '2026-03-05'
    ],
    'amount': [500, 700, 300, 900]
}

df = pd.DataFrame(data)

# Convert
df['order_date'] = pd.to_datetime(df['order_date'])
# then

df['month'] = df['order_date'].dt.month
df['year'] = df['order_date'].dt.year
df['day'] = df['order_date'].dt.day

# Now
df.groupby('month')['amount'].sum()
```

---

## 2. Exercise (`exercise.py`)

```python
import pandas as pd

sales = pd.DataFrame({
    'employee': [
        'Alice', 'Bob', 'Alice', 'Charlie',
        'Bob', 'Alice', 'Charlie', 'Bob'
    ],
    'department': [
        'IT', 'Sales', 'IT', 'HR',
        'Sales', 'IT', 'HR', 'Sales'
    ],
    'product': [
        'Laptop', 'Phone', 'Monitor', 'Laptop',
        'Tablet', 'Phone', 'Monitor', 'Laptop'
    ],
    'amount': [
        1200, 800, 600, 1500,
        900, 700, 500, 1100
    ]
})
```

### Beginner
1. Display the first 5 rows.
2. Display only employee and amount.
3. Find all sales greater than 800.
4. Sort sales from highest to lowest.
5. Find the average sales amount.

### Intermediate
6. Find total sales by employee.
7. Find total sales by department.
8. Find average sales by product.
9. Find the highest sale made by each employee.
10. Find departments whose total sales are greater than 2,000.
11. Add a column called commission equal to 5% of sales.
12. Find the employee with the highest total sales.
13. Create a pivot table showing department vs product sales.
14. Find the top 2 sales transactions for each employee.

---

## 3. Solutions (`solutions.txt`)

### 1. Display the first 5 rows

```python
sales.head()
```

Output:

```
  employee department  product  amount
0    Alice         IT   Laptop    1200
1      Bob      Sales    Phone     800
2    Alice         IT  Monitor     600
3  Charlie         HR   Laptop    1500
4      Bob      Sales   Tablet     900
```

### 2. Display only employee and amount

```python
sales[['employee', 'amount']]
```

Remember:

```python
sales['employee']              # one column
sales[['employee', 'amount']]  # multiple columns
```

### 3. Find all sales greater than 800

```python
sales[sales['amount'] > 800]
```

Output:

```
  employee department product  amount
0    Alice         IT  Laptop    1200
3  Charlie         HR  Laptop    1500
4      Bob      Sales  Tablet     900
7      Bob      Sales  Laptop    1100
```

### 4. Sort sales from highest to lowest

```python
sales.sort_values('amount', ascending=False)
```

Output:

```
  employee department product  amount
3  Charlie         HR  Laptop    1500
0    Alice         IT  Laptop    1200
7      Bob      Sales  Laptop    1100
4      Bob      Sales  Tablet     900
1      Bob      Sales   Phone     800
5    Alice         IT   Phone     700
2    Alice         IT  Monitor     600
6  Charlie         HR  Monitor     500
```

### 5. Find the average sales amount

```python
sales['amount'].mean()
```

Result: `912.5`

---

### Intermediate

### 6. Find total sales by employee

```python
sales.groupby('employee')['amount'].sum()
```

Result:

```
Alice      2500
Bob        2800
Charlie    2000
```

**Teaching point**

This:

```python
groupby('employee')
```

means: put all rows belonging to the same employee together.

Then:

```python
['amount'].sum()
```

means: add their amounts.

### 7. Find total sales by department

```python
sales.groupby('department')['amount'].sum()
```

Result:

```
HR       2000
IT       2500
Sales    3600
```

### 8. Find average sales by product

```python
sales.groupby('product')['amount'].mean()
```

Result:

```
Laptop     1266.67
Monitor     550.00
Phone       750.00
Tablet      900.00
```

### 9. Find the highest sale made by each employee

```python
sales.groupby('employee')['amount'].max()
```

Result:

```
Alice      1200
Bob        1100
Charlie    1500
```

### 10. Find departments whose total sales are greater than 2,000

This is a very good intermediate question because students need to understand filtering after aggregation.

```python
department_sales = sales.groupby('department')['amount'].sum()

department_sales[department_sales > 2000]
```

Result:

```
IT       2500
Sales    3600
```

You can also write:

```python
sales.groupby('department')['amount'].sum().loc[
    lambda x: x > 2000
]
```

But I would teach the first version to beginners because it is easier to understand.

### 11. Add a commission column equal to 5% of sales

```python
sales['commission'] = sales['amount'] * 0.05
```

Now:

```python
print(sales)
```

For example:

```
employee  amount  commission
Alice      1200       60.0
Bob         800       40.0
Alice       600       30.0
```

This is a nice example of vectorized operations. You don't need a loop:

```python
# NOT required
for ...
```

Pandas performs the calculation for the entire column.

### 12. Find the employee with the highest total sales

First calculate total sales:

```python
employee_sales = sales.groupby('employee')['amount'].sum()
```

Then find the maximum:

```python
employee_sales.idxmax()
```

Result: `Bob`

And if you want the amount:

```python
employee_sales.max()
```

Result: `2800`

So: Bob has the highest total sales of $2,800.

### 13. Create a pivot table showing department vs product sales

```python
sales.pivot_table(
    values='amount',
    index='department',
    columns='product',
    aggfunc='sum',
    fill_value=0
)
```

Result:

```
product     Laptop  Monitor  Phone  Tablet
department
HR            1500      500      0       0
IT            1200      600    700       0
Sales         1100        0    800     900
```

This is a great opportunity to explain that a pivot table is essentially creating a summary report.

### 14. Find the top 2 sales transactions for each employee ⭐

This is the most advanced question in the exercise.

A simple solution is:

```python
sales.groupby('employee', group_keys=False).apply(
    lambda x: x.nlargest(2, 'amount')
)
```

A more Pandas-oriented solution:

```python
sales.sort_values(
    ['employee', 'amount'],
    ascending=[True, False]
).groupby('employee').head(2)
```

I recommend teaching the second version first.

**Step 1 — Sort**

```python
sales.sort_values(
    ['employee', 'amount'],
    ascending=[True, False]
)
```

Now each employee's transactions are ordered from highest to lowest.

**Step 2 — Group by employee**

```python
.groupby('employee')
```

**Step 3 — Take first 2 rows from each group**

```python
.head(2)
```

Result:

```
  employee department product  amount
0    Alice         IT  Laptop    1200
5    Alice         IT   Phone     700
7      Bob      Sales  Laptop    1100
4      Bob      Sales  Tablet     900
3  Charlie         HR  Laptop    1500
6  Charlie         HR  Monitor    500
```
