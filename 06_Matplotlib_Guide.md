# Matplotlib: From Basics to Intermediate
### Plotting, Subplots, and Backends (`matplotlib.use`)

**Level:** Beginner → Intermediate
**Estimated time:** 90–120 minutes

---

## Table of Contents

- [Matplotlib: From Basics to Intermediate](#matplotlib-from-basics-to-intermediate)
    - [Plotting, Subplots, and Backends (`matplotlib.use`)](#plotting-subplots-and-backends-matplotlibuse)
  - [Table of Contents](#table-of-contents)
  - [1. Introduction](#1-introduction)
  - [2. Installation \& Import](#2-installation--import)
  - [3. Anatomy of a Matplotlib Plot](#3-anatomy-of-a-matplotlib-plot)
    - [3.1 Figure, Axes, and Axis](#31-figure-axes-and-axis)
    - [3.2 The Two Interfaces: pyplot vs. Object-Oriented](#32-the-two-interfaces-pyplot-vs-object-oriented)
  - [4. Basic Plot Types](#4-basic-plot-types)
    - [4.1 Line Plot](#41-line-plot)
    - [4.2 Bar Chart](#42-bar-chart)
    - [4.3 Scatter Plot](#43-scatter-plot)
    - [4.4 Histogram](#44-histogram)
  - [5. Customizing a Plot](#5-customizing-a-plot)
    - [5.1 Titles and Axis Labels](#51-titles-and-axis-labels)
    - [5.2 Colors, Line Styles, and Markers](#52-colors-line-styles-and-markers)
    - [5.3 Legends](#53-legends)
    - [5.4 Grid Lines and Limits](#54-grid-lines-and-limits)
  - [6. Saving a Figure](#6-saving-a-figure)
  - [7. Subplots — Multiple Plots in One Figure](#7-subplots--multiple-plots-in-one-figure)
    - [7.1 Why Use Subplots?](#71-why-use-subplots)
    - [7.2 `plt.subplots()` Basics](#72-pltsubplots-basics)
    - [7.3 Grids of Subplots](#73-grids-of-subplots)
    - [7.4 Sharing Axes: `sharex` / `sharey`](#74-sharing-axes-sharex--sharey)
    - [7.5 Fixing Overlap: `tight_layout()`](#75-fixing-overlap-tight_layout)
    - [7.6 Uneven Layouts with `GridSpec`](#76-uneven-layouts-with-gridspec)
  - [8. Plotting Directly from a Pandas DataFrame](#8-plotting-directly-from-a-pandas-dataframe)
    - [8.1 The Sample DataFrame](#81-the-sample-dataframe)
    - [8.2 `df.plot()` — the Quick Way](#82-dfplot--the-quick-way)
    - [8.3 Bar Chart from an Aggregated DataFrame](#83-bar-chart-from-an-aggregated-dataframe)
    - [8.4 Automatic Subplots: `subplots=True`](#84-automatic-subplots-subplotstrue)
    - [8.5 Manual Subplots with DataFrame Columns](#85-manual-subplots-with-dataframe-columns)
    - [8.6 Scatter and Histogram from a DataFrame](#86-scatter-and-histogram-from-a-dataframe)
  - [9. Backends and `matplotlib.use()`](#9-backends-and-matplotlibuse)
    - [9.1 What Is a Backend?](#91-what-is-a-backend)
    - [9.2 Interactive vs. Non-Interactive Backends](#92-interactive-vs-non-interactive-backends)
    - [9.3 How and When to Call `matplotlib.use()`](#93-how-and-when-to-call-matplotlibuse)
    - [9.4 Common Backend Errors and Fixes](#94-common-backend-errors-and-fixes)
  - [10. Common Pitfalls](#10-common-pitfalls)
  - [11. Summary Cheat Sheet](#11-summary-cheat-sheet)
  - [12. Practice Exercises](#12-practice-exercises)

---

## 1. Introduction

Matplotlib is the foundational plotting library in Python — most other visualization libraries (pandas plotting, seaborn) are built directly on top of it. It can produce almost any type of static, publication-quality chart: line plots, bar charts, scatter plots, histograms, heatmaps, and more.

This guide takes you from the very basics (drawing a single line plot) through intermediate topics that are essential for real projects: arranging multiple plots with **subplots**, and understanding **backends** via `matplotlib.use()`, which controls *how and where* your plots are actually rendered.

---

## 2. Installation & Import

```bash
pip install matplotlib
```

The standard convention is to import the `pyplot` module under the alias `plt`:

```python
import matplotlib.pyplot as plt
import numpy as np
```

`numpy` isn't required by matplotlib itself, but nearly every example — including in this guide — uses it to generate sample data.

---

## 3. Anatomy of a Matplotlib Plot

### 3.1 Figure, Axes, and Axis

Three terms are used constantly in matplotlib and are frequently confused:

| Term | What it means |
|---|---|
| **Figure** | The entire window or page — the outermost container. A figure can hold one or many plots. |
| **Axes** | A single plot area inside the figure (confusingly, *not* the plural of "axis"). A figure with 4 subplots has 4 Axes objects. |
| **Axis** | One of the actual x or y number lines *within* an Axes (its ticks, labels, limits). |

```text
Figure
 └── Axes (a single subplot / plot area)
      ├── x-axis
      ├── y-axis
      ├── title
      ├── lines / bars / points (the data)
      └── legend
```

Every plot you build is really: **create a Figure → create one or more Axes on it → draw data on the Axes.**

### 3.2 The Two Interfaces: pyplot vs. Object-Oriented

Matplotlib can be used in two different styles. You will see both in documentation and other people's code, so it's important to recognize them.

**Style 1 — the `pyplot` (state-based) interface.** Each `plt.` call implicitly acts on "whatever figure and axes were last created":

```python
plt.plot([1, 2, 3], [4, 5, 6])
plt.title('Quick Plot')
plt.show()
```

**Style 2 — the object-oriented (OO) interface.** You explicitly create Figure and Axes objects and call methods on them:

```python
fig, ax = plt.subplots()
ax.plot([1, 2, 3], [4, 5, 6])
ax.set_title('Quick Plot')
plt.show()
```

Both produce the same plot. The OO style is recommended for anything beyond a one-off quick plot, because it scales cleanly to multiple subplots and avoids ambiguity about which axes you're drawing on. **This guide uses the OO style (`fig, ax = plt.subplots()`) from here on.**

---

## 4. Basic Plot Types

### 4.1 Line Plot

The most common chart — good for showing a trend, typically over a continuous variable like time.

```python
import matplotlib.pyplot as plt
import numpy as np

x = np.linspace(0, 10, 100)
y = np.sin(x)

fig, ax = plt.subplots()
ax.plot(x, y)
plt.show()
```

**Output:**

![Basic line plot](mpl_images/01_basic_line.png)

### 4.2 Bar Chart

Good for comparing values across discrete categories.

```python
categories = ['A', 'B', 'C', 'D']
values = [23, 45, 12, 38]

fig, ax = plt.subplots()
ax.bar(categories, values, color='steelblue')
plt.show()
```

### 4.3 Scatter Plot

Good for showing the relationship between two numeric variables.

```python
x = np.random.rand(50)
y = np.random.rand(50)

fig, ax = plt.subplots()
ax.scatter(x, y, alpha=0.6, color='darkorange')
plt.show()
```

### 4.4 Histogram

Good for showing the distribution of a single numeric variable.

```python
data = np.random.randn(1000)

fig, ax = plt.subplots()
ax.hist(data, bins=30, color='seagreen')
plt.show()
```

**Bar, scatter, and histogram together for comparison:**

![Bar, scatter, and histogram](mpl_images/03_plot_types.png)

---

## 5. Customizing a Plot

### 5.1 Titles and Axis Labels

```python
ax.set_title('Sine Wave')
ax.set_xlabel('x values')
ax.set_ylabel('sin(x)')
```

### 5.2 Colors, Line Styles, and Markers

`plot()` accepts several keyword arguments to control appearance:

| Argument | Purpose | Example values |
|---|---|---|
| `color` | Line/marker color | `'red'`, `'#d62728'`, `'steelblue'` |
| `linewidth` (or `lw`) | Thickness of the line | `1`, `2`, `2.5` |
| `linestyle` (or `ls`) | Style of the line | `'-'`, `'--'`, `'-.'`, `':'` |
| `marker` | Symbol at each data point | `'o'`, `'s'`, `'^'`, `'x'` |

```python
fig, ax = plt.subplots()
ax.plot(x, y, color='#d62728', linewidth=2, linestyle='--', label='sin(x)')
ax.set_title('Sine Wave')
ax.set_xlabel('x values')
ax.set_ylabel('sin(x)')
ax.legend()
ax.grid(True, alpha=0.3)
plt.show()
```

**Output:**

![Customized line plot](mpl_images/02_customized_line.png)

### 5.3 Legends

A legend labels each series so a viewer can tell them apart. Two steps are required:

1. Pass a `label=` argument to each plotting call.
2. Call `ax.legend()` once, after all series are plotted.

```python
ax.plot(x, np.sin(x), label='sin(x)')
ax.plot(x, np.cos(x), label='cos(x)')
ax.legend()
```

### 5.4 Grid Lines and Limits

```python
ax.grid(True, alpha=0.3)      # turn on a light grid
ax.set_xlim(0, 10)            # control the visible x range
ax.set_ylim(-1.5, 1.5)        # control the visible y range
```

---

## 6. Saving a Figure

To save a plot to a file instead of (or in addition to) displaying it, use `savefig()`:

```python
fig, ax = plt.subplots()
ax.plot(x, y)
fig.savefig('my_plot.png', dpi=150, bbox_inches='tight')
```

- `dpi` controls resolution (higher = sharper, larger file).
- `bbox_inches='tight'` trims excess whitespace around the plot.
- The file format is inferred from the extension — `.png`, `.pdf`, `.svg`, and `.jpg` are all supported.

> **Important ordering rule:** call `savefig()` *before* `plt.show()`. Calling `show()` first can clear the figure on some backends, resulting in a blank saved image.

---

## 7. Subplots — Multiple Plots in One Figure

### 7.1 Why Use Subplots?

Subplots let you place multiple related charts inside a single figure — for example, comparing four different metrics side by side, or showing "before" and "after" views together. This is far more useful for reports and dashboards than saving several separate images.

### 7.2 `plt.subplots()` Basics

`plt.subplots(nrows, ncols)` is the standard way to create a grid of Axes at once. It returns **two things**: the Figure, and either a single Axes or an array of Axes.

```python
# A single plot (still returns a Figure and one Axes)
fig, ax = plt.subplots()

# A 1-row, 3-column grid of plots
fig, axes = plt.subplots(1, 3, figsize=(12, 3.5))

axes[0].bar(categories, values, color='steelblue')
axes[0].set_title('Bar Chart')

axes[1].scatter(x, y, alpha=0.6, color='darkorange')
axes[1].set_title('Scatter Plot')

axes[2].hist(data, bins=30, color='seagreen')
axes[2].set_title('Histogram')

plt.tight_layout()
plt.show()
```

When you request more than one row **and** more than one column, `axes` becomes a **2D array**, and you index it with `axes[row, col]`.

### 7.3 Grids of Subplots

```python
fig, axes = plt.subplots(2, 2, figsize=(8, 6))

x = np.linspace(0, 10, 100)
axes[0, 0].plot(x, np.sin(x))
axes[0, 0].set_title('sin(x)')

axes[0, 1].plot(x, np.cos(x), color='orange')
axes[0, 1].set_title('cos(x)')

axes[1, 0].plot(x, np.tan(x), color='green')
axes[1, 0].set_ylim(-5, 5)
axes[1, 0].set_title('tan(x)')

axes[1, 1].plot(x, -x, color='red')
axes[1, 1].set_title('-x')

fig.suptitle('2x2 Grid of Subplots')
plt.tight_layout()
plt.show()
```

**Output:**

![2x2 grid of subplots](mpl_images/04_subplots_2x2.png)

> **Tip:** `fig.suptitle()` sets a title for the *whole figure*, while `ax.set_title()` sets a title for one individual Axes.

### 7.4 Sharing Axes: `sharex` / `sharey`

When subplots represent comparable data, it's often clearer if they share the same x-axis and/or y-axis scale, so viewers can compare across panels directly.

axes[0, 0]   # top-left

axes[0, 1]   # top-right

axes[1, 0]   # bottom-left

axes[1, 1]   # bottom-right

```python
fig, axes = plt.subplots(2, 2, figsize=(8, 6), sharex=True, sharey=True)

axes[0, 0].plot(x, np.sin(x))
axes[0, 0].set_title('sin(x)')

axes[0, 1].plot(x, np.sin(x + 1), color='orange')
axes[0, 1].set_title('sin(x+1)')

axes[1, 0].plot(x, np.sin(x + 2), color='green')
axes[1, 0].set_title('sin(x+2)')

axes[1, 1].plot(x, np.sin(x + 3), color='red')
axes[1, 1].set_title('sin(x+3)')

fig.suptitle('Shared X and Y Axes')
plt.tight_layout()
plt.show()
```

**Output:**

![Subplots with shared axes](mpl_images/05_subplots_share.png)

With `sharex=True` / `sharey=True`, matplotlib also automatically hides the duplicate tick labels on the inner edges, which reduces visual clutter.

### 7.5 Fixing Overlap: `tight_layout()`

By default, titles, labels, and subplots can overlap each other, especially in dense grids. `plt.tight_layout()` (or the equivalent `fig.tight_layout()`) automatically adjusts spacing to prevent this. As a rule of thumb: **call it right before `show()` or `savefig()`, after all subplots have been fully configured.**

An alternative, more manual tool is:

```python
fig.subplots_adjust(hspace=0.4, wspace=0.3)
```

which lets you directly control the horizontal (`wspace`) and vertical (`hspace`) spacing between subplots.

### 7.6 Uneven Layouts with `GridSpec`

`plt.subplots()` only creates *uniform* grids, where every subplot is the same size. When you need an uneven layout — for example, one wide plot on top and two smaller plots below — use `GridSpec`.

```python
fig = plt.figure(figsize=(8, 6))
gs = fig.add_gridspec(2, 2)

ax1 = fig.add_subplot(gs[0, :])   # top row, spans both columns
ax2 = fig.add_subplot(gs[1, 0])   # bottom-left cell
ax3 = fig.add_subplot(gs[1, 1])   # bottom-right cell

ax1.plot(x, np.sin(x))
ax1.set_title('Wide Top Plot (spans both columns)')

ax2.scatter(x, y, alpha=0.6)
ax2.set_title('Bottom Left')

ax3.bar(categories, values, color='purple')
ax3.set_title('Bottom Right')

plt.tight_layout()
plt.show()
```

**Output:**

![GridSpec uneven layout](mpl_images/06_gridspec.png)

`gs[0, :]` means "row 0, all columns" — this is standard NumPy-style slicing applied to the grid.

---

## 8. Plotting Directly from a Pandas DataFrame

So far every example has plotted plain NumPy arrays or lists. In practice, most real data arrives in a pandas DataFrame — and pandas ships a `.plot()` method built directly on top of matplotlib, so you rarely need to unpack columns into arrays by hand.

### 8.1 The Sample DataFrame

```python
import pandas as pd
import numpy as np

df = pd.DataFrame({
    'month': pd.date_range('2026-01-01', periods=6, freq='MS'),
    'North': [120, 135, 128, 150, 165, 172],
    'South': [95, 100, 110, 108, 115, 120],
    'East':  [80, 85, 90, 95, 92, 98],
})
df = df.set_index('month')
df
```

**Output:**

```text
            North  South  East
month
2026-01-01    120     95    80
2026-02-01    135    100    85
2026-03-01    128    110    90
2026-04-01    150    108    95
2026-05-01    165    115    92
2026-06-01    172    120    98
```

```python
df.describe()
```

**Output:**

```text
            North       South      East
count    6.000000    6.000000   6.00000
mean   145.000000  108.000000  90.00000
std     20.823064    9.273618   6.60303
min    120.000000   95.000000  80.00000
25%    129.750000  102.000000  86.25000
50%    142.500000  109.000000  91.00000
75%    161.250000  113.750000  94.25000
max    172.000000  120.000000  98.00000
```

Setting `month` as the index matters: when the index is date-like, `df.plot()` automatically uses it as the x-axis.

### 8.2 `df.plot()` — the Quick Way

Calling `.plot()` on a DataFrame plots **every numeric column** as its own line, using the index as the x-axis and the column names as the legend labels — no manual `ax.plot()` calls needed.

```python
fig, ax = plt.subplots(figsize=(7, 4))
df.plot(ax=ax)
ax.set_title('Monthly Revenue by Region (df.plot())')
ax.set_ylabel('Revenue ($k)')
plt.tight_layout()
plt.show()
```

**Output:**

![Line plot directly from a DataFrame](mpl_images/07_dataframe_line.png)

Passing `ax=ax` tells pandas to draw onto an Axes you already created with the OO interface, rather than creating its own new figure — this is what lets you combine `df.plot()` with everything else in this guide, including subplots.

### 8.3 Bar Chart from an Aggregated DataFrame

A very common pattern is to aggregate a DataFrame first (e.g. with `sum()`, or `groupby()` as covered in a pandas-focused lab), then plot the resulting Series as a bar chart.

```python
totals = df.sum().sort_values(ascending=False)
totals
```

**Output:**

```text
North    870
South    648
East     540
dtype: int64
```

```python
fig, ax = plt.subplots(figsize=(6, 4))
totals.plot(kind='bar', ax=ax, color=['#4C72B0', '#DD8452', '#55A868'])
ax.set_title('Total Revenue by Region')
ax.set_ylabel('Total Revenue ($k)')
ax.set_xlabel('Region')
plt.tight_layout()
plt.show()
```

**Output:**

![Bar chart from an aggregated DataFrame](mpl_images/08_dataframe_bar.png)

### 8.4 Automatic Subplots: `subplots=True`

Pandas can generate a full grid of subplots for you directly — one subplot per column — without you ever calling `plt.subplots()` yourself:

```python
axes = df.plot(subplots=True, figsize=(7, 6), title='Each Region on Its Own Subplot')
plt.tight_layout()
plt.show()
```

**Output:**

![Automatic subplots, one per DataFrame column](mpl_images/09_dataframe_subplots.png)

This is a convenient shortcut, but it gives you less control over the layout than building the subplot grid yourself (Section 7) — use it for quick exploration, and switch to manual subplots when you need a specific arrangement.

### 8.5 Manual Subplots with DataFrame Columns

For full control, combine `plt.subplots()` (Section 7) with `df.plot(ax=...)` or `df['column'].plot(ax=...)`, plotting different slices of the same DataFrame onto different Axes:

```python
fig, axes = plt.subplots(1, 2, figsize=(10, 4))

df['North'].plot(ax=axes[0], color='#4C72B0', marker='o')
axes[0].set_title('North Region Over Time')
axes[0].set_ylabel('Revenue ($k)')

df[['South', 'East']].plot(ax=axes[1])
axes[1].set_title('South vs East')
axes[1].set_ylabel('Revenue ($k)')

plt.tight_layout()
plt.show()
```

**Output:**

![Manual subplots pulling different DataFrame columns](mpl_images/10_dataframe_manual_subplots.png)

This pattern — one DataFrame, sliced differently into each Axes — is the most common way real projects mix pandas and matplotlib.

### 8.6 Scatter and Histogram from a DataFrame

`df.plot(kind=...)` also supports `'scatter'` and `'hist'`, so the same `.plot()` method covers every chart type from Section 4, just called on a DataFrame instead of raw arrays.

```python
obs = pd.DataFrame({
    'study_hours': np.random.uniform(0, 10, 80),
})
obs['exam_score'] = 50 + obs['study_hours'] * 4 + np.random.normal(0, 8, 80)
obs.head()
```

**Output:**

```text
   study_hours  exam_score
0     0.763083   50.650782
1     7.799188   71.756895
2     4.384092   79.517482
3     7.234652   76.677525
4     9.779895   89.988767
```

```python
fig, axes = plt.subplots(1, 2, figsize=(10, 4))

obs.plot(kind='scatter', x='study_hours', y='exam_score', ax=axes[0], alpha=0.6, color='darkorange')
axes[0].set_title('Exam Score vs Study Hours')

obs['exam_score'].plot(kind='hist', ax=axes[1], bins=15, color='seagreen')
axes[1].set_title('Distribution of Exam Scores')
axes[1].set_xlabel('Exam Score')

plt.tight_layout()
plt.show()
```

**Output:**

![Scatter and histogram from a DataFrame](mpl_images/11_dataframe_scatter_hist.png)

For `kind='scatter'`, `x=` and `y=` must be given as **column name strings** — this is different from `ax.scatter()`, which takes raw arrays directly.

---

## 9. Backends and `matplotlib.use()`

### 9.1 What Is a Backend?

A **backend** is the piece of matplotlib responsible for actually *rendering* a figure — turning your plotting commands into either pixels on a screen (an interactive window) or bytes in a file (a PNG, PDF, SVG, etc.). Matplotlib separates "what to draw" (your `plot()`, `bar()`, `scatter()` calls) from "how and where to draw it" (the backend), which is why the same code can produce an interactive window on your laptop and a saved file on a server with no display at all.

### 9.2 Interactive vs. Non-Interactive Backends

| Type | Description | Examples |
|---|---|---|
| **Interactive** | Opens a window you can pan, zoom, and interact with. Requires a display/GUI toolkit. | `TkAgg`, `Qt5Agg`, `MacOSX`, `notebook` (in Jupyter) |
| **Non-interactive** | Renders directly to a file. No window, no display required. | `Agg`, `pdf`, `svg`, `cairo` |

If you run matplotlib on a server, in a Docker container, or in a CI/CD pipeline — anywhere without a graphical display — an interactive backend will fail or hang. This is the single most common reason `matplotlib.use('Agg')` shows up in real-world code.

### 9.3 How and When to Call `matplotlib.use()`

```python
import matplotlib
matplotlib.use('Agg')      # must come before importing pyplot
import matplotlib.pyplot as plt
```

**The critical rule: `matplotlib.use()` must be called *before* `matplotlib.pyplot` is imported.** Once `pyplot` has been imported, it has already selected and initialized a backend, and switching afterward may not work reliably (and will raise a warning in newer matplotlib versions).

Typical situations where you'd explicitly set the backend:

- **Headless servers / Docker containers** with no display — use `matplotlib.use('Agg')`.
- **Automated scripts or cron jobs** that only need to save charts to disk, never show them.
- **Web backends** (e.g., a Flask or Django app generating charts for a webpage) — these should never try to open an interactive window.
- **CI/CD test pipelines** where code that plots something must not hang waiting for a window that can never open.

If you never call `matplotlib.use()` yourself, matplotlib tries to auto-detect a suitable backend based on what's installed and whether a display is available — which is fine for everyday interactive use (e.g., in Jupyter or on your own desktop) but can silently fail in unusual environments.

### 9.4 Common Backend Errors and Fixes

| Symptom | Likely cause | Fix |
|---|---|---|
| `plt.show()` does nothing / script hangs | No display available, or an interactive backend was selected in a headless environment | `matplotlib.use('Agg')` and use `savefig()` instead of `show()` |
| `ImportError` mentioning Tkinter, Qt, or GTK | The interactive backend's underlying GUI library isn't installed | Install the missing GUI library, or switch to a non-interactive backend if you don't actually need a window |
| Backend "already selected" warning after calling `matplotlib.use()` | `pyplot` was imported (directly or indirectly, e.g. via another library) before `matplotlib.use()` ran | Move the `matplotlib.use()` call to the very first lines of the script, before any other matplotlib-related imports |

---

## 10. Common Pitfalls

- **Confusing Axes with axis.** "Axes" is a plot area; "axis" is one number line on it. This trips up almost everyone at first.
- **Forgetting `label=` before calling `legend()`.** If no series were given a `label`, `ax.legend()` produces an empty or missing legend.
- **Calling `savefig()` after `show()`.** On some backends this can save a blank image — always save first.
- **Reusing the same figure across a loop without closing it.** Repeated `plt.plot()` calls in a loop without `plt.figure()` or `plt.close()` will keep adding to the *same* figure, or leak memory over many iterations. Use `plt.close(fig)` when a figure is no longer needed.
- **Calling `matplotlib.use()` too late.** As covered in Section 8.3, it must run before `pyplot` is imported anywhere in the program.

---

## 11. Summary Cheat Sheet

| Task | Code |
|---|---|
| Create a figure and one plot | `fig, ax = plt.subplots()` |
| Create a grid of subplots | `fig, axes = plt.subplots(nrows, ncols)` |
| Line / bar / scatter / histogram | `ax.plot()` / `ax.bar()` / `ax.scatter()` / `ax.hist()` |
| Titles and labels | `ax.set_title()`, `ax.set_xlabel()`, `ax.set_ylabel()` |
| Legend | `ax.legend()` (after setting `label=` on each series) |
| Fix subplot spacing | `plt.tight_layout()` |
| Uneven subplot layout | `fig.add_gridspec(...)`, `fig.add_subplot(gs[...])` |
| Plot a DataFrame's columns as lines | `df.plot(ax=ax)` |
| Plot a DataFrame on an existing Axes | `df.plot(ax=ax)` or `df['col'].plot(ax=ax)` |
| One subplot per DataFrame column | `df.plot(subplots=True)` |
| Bar/scatter/hist from a DataFrame | `df.plot(kind='bar'/'scatter'/'hist', ...)` |
| Save to file | `fig.savefig('name.png', dpi=150, bbox_inches='tight')` |
| Set a non-interactive backend | `matplotlib.use('Agg')` — **before** importing `pyplot` |

---

## 12. Practice Exercises

1. Create a single figure with a line plot of `y = x**2` for `x` from -10 to 10. Add a title, axis labels, and a grid.
2. Create a 1×2 grid of subplots: a bar chart on the left and a histogram on the right, using data of your choice.
3. Create a 2×2 grid of subplots that all share the same y-axis, each showing a different transformation of the same dataset (e.g., original, squared, square root, log).
4. Use `GridSpec` to build a layout with one large plot on the left and two stacked smaller plots on the right.
5. Write a short script that: sets the backend to `'Agg'` at the very top, generates a plot, and saves it to a file — without ever calling `plt.show()`. Explain in a comment why this script would work unmodified on a server with no display.
