# Choosing a Kafka Partition Key: 10 Case Studies

## Quick Primer

For a record with a non-null key, the default producer partitioner picks the partition as:

```
partition = murmur2(keyBytes) % numPartitions
```

That single line drives three properties you must reason about for every scenario:

| Property | What it means |
|---|---|
| **Ordering** | Kafka guarantees order only *within a partition*. Same key → same partition → ordered. |
| **Load distribution** | Too few distinct keys, or a few very "hot" keys, means uneven partitions (skew) and a bottleneck. |
| **Co-location / statefulness** | Consumers and stream processors that keep per-entity state need all of an entity's events on the same partition. |

**Questions to ask for every case:**
1. What entity must have its events processed **in order**?
2. What entity needs **all its events in one place** (state, aggregation, joins)?
3. Is the key's **cardinality high** and its **distribution even**?
4. Will the key **change over time** (e.g. a user changes city)? Keys should be immutable.

> Read each case first and decide on your own key. The solutions start in the **Solutions** section below.

---

# Part 1: Case Studies

## Case Study 1: E-commerce Order Lifecycle

An online retailer publishes events to a topic `order-events` with 24 partitions:
`OrderCreated → PaymentAuthorized → InventoryReserved → Shipped → Delivered` (or `Cancelled`).

A downstream order-state-machine consumer rejects any event that arrives out of sequence (e.g. `Shipped` before `PaymentAuthorized`). Peak traffic is around 5,000 orders per second, and each order produces 5 to 8 events over several days.

**Available fields:** `order_id`, `customer_id`, `product_id`, `warehouse_id`, `event_type`

**Question:** Which field should be the partition key?

---

## Case Study 2: Ride-Hailing Driver Location Tracking

A ride-hailing platform receives GPS pings from 500,000 active drivers every 4 seconds into topic `driver-locations` (60 partitions). A consumer maintains the "latest known position" of each driver to power matching. Stale pings arriving after newer ones would place a driver at the wrong location.

The platform operates in 300 cities, but 3 megacities account for roughly 40% of all drivers.

**Available fields:** `driver_id`, `city_id`, `trip_id`, `vehicle_type`, `timestamp`

**Question:** Which field should be the partition key?

---

## Case Study 3: Banking Account Ledger

A bank streams debit and credit transactions into topic `account-transactions` (48 partitions). A consumer computes running balances and enforces "no overdraft" rules. If a withdrawal is processed before the deposit that funded it, a valid transaction is wrongly rejected. If two withdrawals are processed by different consumers at the same time, the account may be overdrawn.

**Available fields:** `transaction_id` (unique UUID), `account_id`, `customer_id` (a customer may own several accounts), `branch_id`, `amount`

**Question:** Which field should be the partition key?

---

## Case Study 4: Smart Factory IoT Telemetry

A factory has 200 IoT gateways, each connected to about 50 sensors (10,000 sensors total). Each sensor emits a reading every second into topic `sensor-readings` (30 partitions). An anomaly detector computes a **5-minute rolling average per sensor** and raises an alert when it crosses a threshold.

One legacy gateway (`gw-17`) aggregates 2,000 sensors, forty times more than any other.

**Available fields:** `sensor_id`, `gateway_id`, `factory_line`, `sensor_type`

**Question:** Which field should be the partition key?

---

## Case Study 5: Website Clickstream and Sessions

A media site publishes every click event into topic `clickstream` (40 partitions). Analysts want to compute **per-visit funnels** (landing → article → subscribe) in a stream processor. A small share of "power users" and a few crawler bots generate hundreds of thousands of events a day. The homepage `/` receives about 30% of all page views.

**Available fields:** `session_id`, `user_id` (null for anonymous visitors), `page_url`, `ip_address`, `country`

**Question:** Which field should be the partition key?

---

## Case Study 6: Multi-Tenant SaaS Audit Logs

A B2B SaaS product writes audit logs from all customers into a shared topic `audit-logs` (36 partitions). There are 4,000 tenants. One enterprise tenant, "MegaCorp", produces 55% of all traffic. Each tenant's logs must be **ordered per user** (to reconstruct what a specific user did), but there is no ordering requirement across users, even within one tenant.

**Available fields:** `tenant_id`, `user_id`, `action_type`, `resource_id`

**Question:** Which key would you choose, and would `tenant_id` alone be safe?

---

## Case Study 7: Chat Application Messages

A messaging app stores every message through topic `chat-messages` (64 partitions). Users must see messages in a conversation in the order they were sent. Conversations include 1:1 chats and group chats of up to 500 members. A consumer writes to a read-optimized store and pushes to connected clients.

**Available fields:** `message_id`, `sender_id`, `conversation_id`, `recipient_id`, `sent_at`

**Question:** Which field should be the partition key?

---

## Case Study 8: Joining Orders with Payments (Kafka Streams)

Two topics feed a Kafka Streams application that joins them to detect "orders paid but not shipped":

* `orders` (keyed by `order_id`, 12 partitions)
* `payments` (currently keyed by `payment_id`, 12 partitions)

Each payment record contains an `order_id`. The join between the two streams silently produces missing matches in production, even though both topics contain all the data.

**Available fields on payments:** `payment_id`, `order_id`, `customer_id`, `method`

**Question:** Why is the join failing, and what should the payments topic key be?

---

## Case Study 9: Change Data Capture (CDC) from a Relational Database

Debezium streams row changes from a `customers` table (primary key `customer_id`) and an `orders` table (primary key `order_id`) into Kafka. Each table gets its own topic. Consumers rebuild a materialized view of the latest row state, and the topics use **log compaction** to retain only the newest record per key.

**Available fields:** `table_name`, primary key column(s), `updated_at`, `operation` (insert/update/delete)

**Question:** What should the key be for each topic, and why does it matter for compaction?

---

## Case Study 10: Application Log Aggregation

Hundreds of microservices ship structured logs to topic `app-logs` (50 partitions), which are indexed in Elasticsearch. There is **no** ordering requirement between log lines; search relies on timestamps. Traffic is uneven: a chatty `checkout-service` produces 35% of all logs.

**Available fields:** `service_name`, `host`, `log_level`, `timestamp`, `trace_id`

**Question:** Do you need a key at all? If so, which?

---

<br>

# Part 2: Solutions

Each solution follows the same format: **Recommended key → Why → What happens with other choices.**

---

## Solution 1: E-commerce Order Lifecycle

**Recommended key: `order_id`**

**Why**
* The ordering requirement is *per order*. Keying by `order_id` sends every event of one order to one partition, in the order produced.
* Cardinality is very high (millions of orders), so load spreads evenly across the 24 partitions.
* Orders are independent of each other, so no cross-order ordering is needed.

**What happens with other keys**

| Key | Consequence |
|---|---|
| `customer_id` | Ordering is preserved (all of a customer's orders are in one partition), but a heavy buyer (B2B account, reseller) creates a hot partition. It also imposes unneeded ordering across unrelated orders, which slows the consumer. |
| `product_id` | Events of one order are scattered across partitions, so `Shipped` can be consumed before `PaymentAuthorized`, breaking the state machine. Popular products also cause skew. |
| `warehouse_id` | Very low cardinality (a handful of values), so most partitions sit idle. It also doesn't group events for the same order. |
| `event_type` | Only ~6 distinct values. Catastrophic skew and no per-order ordering. |
| `null` (round robin) | Perfect balance but no ordering at all. State-machine consumer sees random sequences. |

---

## Solution 2: Ride-Hailing Driver Locations

**Recommended key: `driver_id`**

**Why**
* "Latest position per driver" needs pings for one driver in order, so the entity is the driver.
* 500,000 drivers gives high cardinality and near-uniform distribution. Each driver emits at the same steady rate, so no key is hot.
* The consumer's state store (latest location per driver) is naturally partitioned by the same key.

**What happens with other keys**

| Key | Consequence |
|---|---|
| `city_id` | The 3 megacities land on 3 partitions carrying ~40% of traffic. Those partitions' consumers lag and matching quality drops in exactly the busiest markets. Only ~300 keys for 60 partitions also gives uneven hashing. |
| `trip_id` | Only exists while a driver is on a trip. Idle drivers, the ones you most need to match, have no trip, so their pings can't be keyed. Also breaks "latest position per driver" as one driver moves across many keys. |
| `vehicle_type` | A few values (car, bike, van), so extreme skew. |
| `null` | Pings for one driver spread across partitions and consumers, so an old ping can overwrite a newer one. |

*Note:* If you later need geo-based queries, do that in a downstream stream job (re-key by geo-cell) instead of changing the ingest key.

---

## Solution 3: Banking Account Ledger

**Recommended key: `account_id`**

**Why**
* Balance and overdraft checks are *per account*. All transactions for an account must be processed serially and in order, which a single partition (and a single consumer at a time) guarantees.
* This also prevents concurrent processing of two withdrawals on the same account, which is the double-spend race.
* Many accounts means good distribution.

**What happens with other keys**

| Key | Consequence |
|---|---|
| `transaction_id` | Unique per record, so it behaves like random. A withdrawal can be processed before its funding deposit (false decline), and two consumers can approve concurrent withdrawals (overdraft). Functionally incorrect for a ledger. |
| `customer_id` | Ordering is preserved for each account (a customer's accounts share a partition), but it needlessly couples unrelated accounts. Corporate customers with thousands of accounts create hot partitions. It would be acceptable only if rules span all of a customer's accounts. |
| `branch_id` | Low cardinality and skewed, since big branches dominate. Also no per-account ordering guarantee if a customer transacts via different branches (the key varies), so the balance state breaks. |

---

## Solution 4: Smart Factory IoT Telemetry

**Recommended key: `sensor_id`**

**Why**
* The rolling average is computed *per sensor*, so all readings from one sensor must be on the same partition and in time order.
* 10,000 sensors emitting at the same rate is a high-cardinality, uniform distribution.
* Sensors from the big gateway `gw-17` are spread across all partitions instead of concentrating on one.

**What happens with other keys**

| Key | Consequence |
|---|---|
| `gateway_id` | `gw-17` carries 2,000 sensors, forty times more than typical, so its partition is overwhelmed while others are near idle. Only 200 keys across 30 partitions also hashes unevenly. The lagging partition delays alerts for that gateway, likely the one you most want to watch. |
| `factory_line` | Few values, so heavy skew and poor parallelism. |
| `sensor_type` | A handful of types, so terrible skew (e.g. all temperature sensors in one partition) and the per-sensor window state is split across consumers. |
| `null` | Readings for one sensor scatter, so the rolling average is computed on incomplete data and produces wrong alerts. |

---

## Solution 5: Website Clickstream and Sessions

**Recommended key: `session_id`** (fall back to a generated anonymous ID if it can be null)

**Why**
* The funnel is per visit, so events of one session must be together and ordered.
* `session_id` is never null (generate it client-side), has enormous cardinality, and each session is short-lived, so no key stays hot for long.
* Bots or power users get many sessions, spreading their load.

**What happens with other keys**

| Key | Consequence |
|---|---|
| `user_id` | Null for anonymous visitors. Kafka would then treat null keys as unkeyed and spread them randomly, so the funnel for those visitors breaks (or all anonymous traffic must be given one fake key, creating a giant hot partition). Power users and bots also create hot partitions. Works only if you want lifetime user analytics rather than per-visit funnels. |
| `page_url` | The homepage `/` alone gets 30% of traffic, so one partition takes 30% of the load. And a session's events are scattered across pages, so funnels can't be computed. |
| `ip_address` | Corporate NAT or mobile carrier IPs cause massive skew. Also unstable, since one user's IP changes mid-session. |
| `country` | Very low cardinality and skewed toward a few big countries. |

---

## Solution 6: Multi-Tenant SaaS Audit Logs

**Recommended key: composite `tenant_id:user_id`** (or just `user_id` if user IDs are globally unique)

**Why**
* The requirement is ordering *per user*, not per tenant. Using the composite key preserves it (a given user always hashes to the same partition).
* Because MegaCorp has many users, its 55% share is spread across all partitions instead of one.
* The tenant prefix guarantees uniqueness if `user_id` values are only unique within a tenant.

**What happens with other keys**

| Key | Consequence |
|---|---|
| `tenant_id` alone | **Not safe.** MegaCorp's 55% of traffic lands on one partition, which becomes a bottleneck: one consumer at 55% load while the other 35 sit mostly idle, growing consumer lag and slower processing for everyone hashed to that partition. Small tenants sharing that partition ("noisy neighbor") suffer too. |
| `resource_id` | Splits one user's actions across partitions, so their audit trail can't be reconstructed in order. |
| `action_type` | Few values, so heavy skew and no per-user ordering. |
| `null` | Balanced but no per-user order. |

*If a tenant needs full isolation instead:* give MegaCorp its own dedicated topic, or salt the key (`tenant_id:hash(user_id) % N`), accepting that ordering is only per salt bucket.

---

## Solution 7: Chat Application Messages

**Recommended key: `conversation_id`**

**Why**
* Users expect messages to be ordered *within a conversation*. All messages for a conversation go to one partition, in send order, no matter who sent them.
* Many conversations, most of them small, gives good distribution.

**What happens with other keys**

| Key | Consequence |
|---|---|
| `sender_id` | Ordering is per sender only. In a group chat, Alice's and Bob's messages land on different partitions and can be consumed in a different order than sent, so replies appear before questions. |
| `recipient_id` | Doesn't work for group chats (multiple recipients), and a 1:1 chat is split into two "directions" on different partitions. |
| `message_id` | Effectively random, so no ordering at all within a conversation. |
| `null` | Same problem, with round-robin. |

*Watch out:* A very large or viral group chat can become a hot key. If that's realistic, monitor per-partition throughput, and consider sharding those giant rooms (with a sequence number per shard, then merging on the client).

---

## Solution 8: Joining Orders with Payments

**Recommended key for `payments`: `order_id`**

**Why the join failed**
* Kafka Streams joins require the two topics to be **co-partitioned**: same key, same number of partitions, and the same partitioning strategy, so that records with the same key meet in the same task.
* `orders` is keyed by `order_id` but `payments` by `payment_id`. The hash of `payment_id` sends a payment to a different partition than its order, so the two records are processed by different tasks and never meet. The join silently drops matches.

**Fix**
* Re-key `payments` by `order_id` at the producer, or in the stream with `selectKey(...)` followed by a repartition (this creates an internal repartition topic). Prefer fixing at the producer to avoid the extra topic and network hop.
* Ensure both topics have the same partition count (12) and use the same partitioner.

**What happens with other choices**

| Choice | Consequence |
|---|---|
| Key `payments` by `payment_id` (status quo) | Missing join results, hard to notice since no error is thrown. |
| Key `payments` by `customer_id` | Still not co-partitioned with `orders`, so the join still fails. Every key must be the *join key*. |
| Different partition counts (e.g. 12 vs 24) | Same key hashes to different partition numbers, so the Streams app throws a `TopologyException` at startup or the join breaks. |
| Use a `GlobalKTable` for one side | Works without co-partitioning, but only if the table is small enough to replicate to every instance. Not suitable for a large payments stream. |

---

## Solution 9: Change Data Capture (CDC)

**Recommended keys:** `customer_id` for the `customers` topic and `order_id` for the `orders` topic. That is, **the row's primary key** (Debezium does this by default).

**Why**
* All changes to one row go to one partition, so they arrive in commit order and the consumer's final state is right.
* With log compaction, Kafka keeps the latest record *per key*. Keying by primary key means "latest record per key" equals "latest state of each row". A delete is represented as a tombstone (null value) for that key, letting compaction remove the row.

**What happens with other keys**

| Key | Consequence |
|---|---|
| `table_name` | The same key for every record in the topic, so one partition gets everything (zero parallelism), and compaction would retain only **one record for the whole table**, losing all other rows. Data loss. |
| `null` | Compaction cannot work (records with null keys are not compacted, and a compacted topic will reject them). Updates to one row spread across partitions and can be applied out of order. |
| `updated_at` | Every update has a new key, so compaction never removes old versions and the topic grows without bound. It also splits one row's changes across partitions. |
| Non-primary attribute (e.g. `email`) | It's mutable: when it changes, the row's history splits across partitions and the old key is left as a stale record in a compacted topic. |

---

## Solution 10: Application Log Aggregation

**Recommended key: `null`** (no key), and let the producer's built-in sticky partitioner spread the load.

**Why**
* Nothing requires ordering or co-location, since search works on timestamps in Elasticsearch. Without a key, Kafka can spread records evenly across all 50 partitions, so `checkout-service` (35% of traffic) does not create a hot spot.
* The sticky partitioner (Kafka 2.4+) fills a batch for one partition before switching, which gives better batching, higher throughput, and lower latency than random per-record assignment.

**What happens with other keys**

| Key | Consequence |
|---|---|
| `service_name` | `checkout-service` maps to one partition carrying 35% of all logs. Also, a few hundred keys across 50 partitions hashes unevenly. You pay for ordering nobody needs. |
| `host` | Better cardinality, but chatty hosts are still hot, and again there is no consumer that needs per-host ordering. |
| `log_level` | 4 or 5 values, so at most 5 partitions used. `INFO` dominates. |
| `trace_id` | Good *if* you later want to reconstruct a request's path inside a stream processor (all logs of a request together). Distribution is fine. Choose it only when that requirement exists. |

*Rule of thumb:* If no consumer relies on ordering or grouping, don't invent a key.

---

<br>

# Summary Table

| # | Scenario | Best Key | Main Reason | Classic Wrong Choice |
|---|---|---|---|---|
| 1 | E-commerce orders | `order_id` | Per-order ordering, high cardinality | `event_type` / `warehouse_id` (skew) |
| 2 | Driver locations | `driver_id` | Per-driver latest state | `city_id` (hot megacities) |
| 3 | Bank ledger | `account_id` | Serial balance checks | `transaction_id` (no ordering) |
| 4 | IoT telemetry | `sensor_id` | Per-sensor windows, avoids big gateway skew | `gateway_id` (hot gateway) |
| 5 | Clickstream | `session_id` | Per-visit funnels, never null | `page_url` (hot homepage) |
| 6 | Multi-tenant logs | `tenant_id:user_id` | Per-user order, spreads huge tenant | `tenant_id` alone (hot tenant) |
| 7 | Chat messages | `conversation_id` | Order within a conversation | `sender_id` (order lost in groups) |
| 8 | Stream join | `order_id` on both topics | Co-partitioning | `payment_id` (silent missed joins) |
| 9 | CDC + compaction | Row primary key | Latest-state-per-key semantics | `table_name` (compaction data loss) |
| 10 | Log aggregation | `null` | No ordering need, even spread | `service_name` (skew) |

# Key Takeaways

1. **Start from the ordering/state requirement**, not the data you happen to have. The key is the entity whose events must stay in order.
2. **Cardinality and distribution matter as much as correctness.** A correct but skewed key still causes consumer lag. Composite or salted keys can fix hot keys, at the cost of coarser ordering.
3. **Keys should be immutable and non-null** when ordering matters. Mutable keys split an entity's history, and null keys are distributed without ordering.
4. **Co-partition for joins.** Same key, same partition count, same partitioner.
5. **Plan partition count up front.** Increasing partitions changes `hash(key) % N`, so existing keys map to new partitions and per-key ordering across the change is lost. Over-provision a little, or migrate to a new topic.
6. **Don't use a key you don't need.** With no ordering requirement, `null` keys give the best balance and batching.
