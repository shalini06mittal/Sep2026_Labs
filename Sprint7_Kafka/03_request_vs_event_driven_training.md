# Request-Driven vs. Event-Driven Communication

*A training guide with real-world examples and practice scenarios*

---

## 1. The Core Idea

| | Request-Driven | Event-Driven |
|---|---|---|
| **Pattern** | "Give me X" → wait → "here's X" | "X happened" → move on, whoever cares can react later |
| **Coupling** | Caller knows exactly who it's talking to | Producer doesn't know (or care) who's listening |
| **Timing** | Synchronous in effect — the caller is logically blocked on a reply | Asynchronous — producer and consumer act independently, at different times |
| **Typical tech** | REST, gRPC, GraphQL | Kafka, RabbitMQ, SNS/SQS, EventBridge |
| **Failure visibility** | Immediate — a timeout/error surfaces right at the call site | Delayed — the producer has already moved on; failures show up via monitoring, dead-letter queues, or a downstream symptom |
| **Adding a new consumer** | Requires changing the caller to add a new call | Requires nothing from the producer — new subscriber, no code change upstream |

---

## 2. Request-Driven: Real-World Examples

**Checkout: card validation**
A checkout page calls a payment service to validate a card. The UI cannot render a confirmation or an error until it gets an answer — there is nothing useful to show in the meantime. This *has* to be request/response.

**Mobile app: fetching a user profile**
The app calls `GET /users/42` and renders the screen once the response arrives. There's no "profile fetched" event to broadcast to anyone else — exactly one caller wants exactly one answer, right now.

**Internal service-to-service lookup**
An order service calls an inventory service: "how many units of SKU-123 are in stock?" It needs the number immediately to decide whether to accept the order.

---

## 3. Event-Driven: Real-World Examples

**E-commerce: `OrderPlaced`**
When an order is placed, four unrelated things need to happen: decrement inventory, charge the card, schedule shipping, log analytics. If the order service called all four directly, it would need to know about (and stay up in sync with) every one of them. Instead it publishes one event, and each interested service subscribes independently. Services can be added or removed without ever touching the order service.

**IoT / clickstream ingestion**
Thousands of sensor readings or user clicks arrive in bursts. A queue/topic absorbs the burst so producers never block, and consumers process at their own pace instead of the producer needing to wait on a slow consumer.

**Long-running background work: video transcoding**
A user uploads a video. The upload request shouldn't hang for minutes waiting for transcoding to finish. The upload service publishes `VideoUploaded`, returns immediately, and a worker picks up the event whenever it's ready.

---

## 4. Decision Checklist

Ask these questions when choosing:

1. **Does the caller need the answer *right now* to proceed?** → Request-driven.
2. **Do multiple, independent things need to happen as a result of one action?** → Event-driven.
3. **Can the producer tolerate not knowing whether/when the work downstream completes?** → Event-driven. If no, request-driven (or request/reply on top of a broker).
4. **Is the volume bursty or unpredictable?** → Event-driven, so a queue can absorb the burst.
5. **How costly is a silent failure?** → Request-driven surfaces failures immediately at the call site; event-driven requires you to build monitoring, retries, and dead-letter handling to catch problems at all.

Real systems commonly mix both: request-driven at client-facing edges (something needs an immediate answer), event-driven internally for fan-out and background work.

---

## 5. Practice Scenarios

For each scenario, decide: **request-driven or event-driven?** Justify your answer using at least one of: *deadline risk, failure visibility, coupling/extensibility, or burst/volume handling.*

**Scenario A**
A ride-share app needs to show the rider whether a driver has accepted their ride request within the next few seconds, so it can update the screen.

**Scenario B**
A bank needs to notify the fraud team, the customer's mobile app, and a compliance audit log whenever a transaction over $10,000 is made — three separate systems, none of which need to give feedback that changes the transaction itself.

**Scenario C**
A user clicks "check username availability" while signing up, and the form needs to show a green check or red X immediately.

**Scenario D**
A food delivery platform wants to update "estimated delivery time" for a customer's order as the driver's GPS location changes every few seconds, without the driver's app waiting on any downstream system.

**Scenario E**
A hotel booking system needs to confirm room availability and lock the room before showing the customer a "Payment" screen.

**Scenario F**
A company's HR system, when an employee is marked "terminated," needs to trigger: revoking building access, disabling their email account, and removing them from payroll — three unrelated internal teams' systems, added over time as the company grew.

**Scenario G**
A weather sensor network publishes temperature readings every 10 seconds from 50,000 devices, and various dashboards, alerting systems, and archival services want to consume this data at their own pace.

**Scenario H**
An internal admin tool needs to fetch a specific customer's account balance to display on a support agent's screen when the agent opens a ticket.

---

## 6. Solutions

**Scenario A — Request-driven** (with polling or a lightweight status check).
The rider's screen needs a direct answer within seconds to know whether to proceed. Deadline risk is the driver here: the UI has nothing useful to show without a response tied to *this specific request*. (In production this is often implemented as request/response for the initial acceptance, with events used internally for driver-matching logic — but from the rider app's point of view, it's request-driven.)

**Scenario B — Event-driven.**
None of the three recipients need to give feedback that affects the original transaction — the transaction already happened. Coupling is the key issue: if the bank called each system directly, adding a fourth recipient later (e.g. a new reporting tool) would require changing the transaction-processing code. Publishing a `LargeTransactionOccurred` event lets new consumers subscribe with zero change upstream.

**Scenario C — Request-driven.**
The form is blocked on the answer — there's nothing to render until the check comes back. This is a classic synchronous, low-latency lookup with a single caller and a single, immediately-needed answer.

**Scenario D — Event-driven.**
Location updates are high-frequency and bursty (every few seconds, per driver), and the driver's app must not block waiting for downstream consumers (ETA calculators, customer notifications) to finish processing. An event stream absorbs the volume and lets consumers process independently.

**Scenario E — Request-driven.**
This is a hard deadline-risk case: the customer cannot see a payment screen until the room lock actually succeeds, because two customers could otherwise be shown the same available room. The booking flow needs a direct, immediate answer to proceed correctly.

**Scenario F — Event-driven.**
This is structurally identical to Scenario B: one trigger, multiple independent systems added by different teams over time, none of which need to send anything back to HR to "approve" the termination. An `EmployeeTerminated` event lets each system react on its own, and new systems can subscribe later without touching the HR system.

**Scenario G — Event-driven.**
This is the textbook burst/volume case: 50,000 devices reporting continuously would overwhelm any system forced to handle each reading as a blocking request, and consumers (dashboards, alerting, archival) each want to process at a different pace. A topic/queue is required to absorb the volume and decouple producers from consumers.

**Scenario H — Request-driven.**
The support agent is staring at a screen waiting for a specific answer about a specific customer, right now. There's no fan-out, no multiple consumers, and no tolerance for "the balance will show up eventually" — it's a direct, synchronous lookup.
