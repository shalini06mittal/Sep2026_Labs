# Batch vs. Real-Time: 10 Decision Scenarios

Each scenario below state the decision and justify it against one concrete consequence: **deadline risk**, **failure visibility**, **infrastructure cost**, or **boundedness of the data**.

---

SCENARIOS:

### 1. Nightly retail sales reporting

### 2. Credit card fraud scoring

### 3. Company payroll processing

### 4. Live sports score updates

### 5. Monthly SaaS invoice generation

### 6. Factory equipment anomaly/safety monitoring

### 7. Customer churn model retraining

### 8. Ride-share driver–rider matching

### 9. Weekly executive BI dashboard refresh

### 10. Chat/messaging app message delivery

### SOLTIONS:

### 1. Nightly retail sales reporting
**Decision: Batch.**
The data is fully bounded the moment the store closes — there is no benefit to processing transaction #4,000 before transaction #4,001 exists. Deadline risk is low (report is due "by morning," not "within seconds"), so a single nightly job is cheaper to run and operate than a standing streaming pipeline.

### 2. Credit card fraud scoring
**Decision: Real-time.**
Deadline risk is the deciding factor: the scoring decision has to land before the authorization completes, or the fraudulent charge simply goes through. A batch job that catches the fraud six hours later has already failed at its actual job, regardless of accuracy.

### 3. Company payroll processing
**Decision: Batch.**
The data is bounded by definition — a fixed pay period with a fixed employee list — so there's nothing to "stream." Deadline risk is manageable (a known pay date, not a per-second SLA), which makes a scheduled batch run far cheaper than keeping compute running continuously for an event that happens twice a month.

### 4. Live sports score updates
**Decision: Real-time.**
The data is inherently unbounded (the game keeps generating events until it ends), so there's no natural batch boundary to wait for. Failure visibility is also immediate and public: a scoreboard stuck 5 minutes behind is a visible, embarrassing failure users notice instantly, unlike a quietly-late internal report.

### 5. Monthly SaaS invoice generation
**Decision: Batch.**
Billing data is bounded to a closed cycle (the month has ended, usage is final), so real-time processing would just mean repeatedly recomputing a number that isn't final yet — wasted infrastructure cost for no benefit. Deadline risk is loose (invoices due within days, not seconds), so a scheduled batch job is the cost-efficient choice.

### 6. Factory equipment anomaly/safety monitoring
**Decision: Real-time.**
Deadline risk is severe and physical: a delayed alert on an overheating machine can mean equipment damage or injury, not just a late dashboard. Failure visibility also has to be immediate — a batch summary that surfaces the anomaly after the fact provides forensic information, not prevention.

### 7. Customer churn model retraining
**Decision: Batch.**
Although the underlying event stream (logins, cancellations, usage) is technically unbounded, churn signals only become meaningful when aggregated over days or weeks — so artificially bounding the data into daily/weekly batches loses nothing. Running retraining continuously would multiply infrastructure cost for a model that doesn't meaningfully change minute to minute.

### 8. Ride-share driver–rider matching
**Decision: Real-time.**
The data is unbounded and location is only valid for seconds (drivers and riders keep moving), so batching would match people to locations that no longer exist. Deadline risk directly threatens the product: a multi-second delay in matching is a failed user experience, and the infrastructure cost of real-time processing is justified by the revenue each successful match represents.

### 9. Weekly executive BI dashboard refresh
**Decision: Batch.**
The data is bounded to "everything through last night," and the only real deadline is the weekly leadership meeting — hours of slack, not seconds. Failure visibility is low-stakes here too: if the batch job fails, there's time to detect it and rerun before anyone notices, which wouldn't be true of a real-time pipeline with no buffer.

### 10. Chat/messaging app message delivery
**Decision: Real-time.**
The message stream is unbounded by design (conversations don't have a natural "batch cutoff"), and failure visibility is immediate and per-message: a delayed text is a failure the sender and recipient both see directly, right when it happens. There's no deadline slack to exploit — "deliver eventually" isn't an acceptable version of a messaging product.
