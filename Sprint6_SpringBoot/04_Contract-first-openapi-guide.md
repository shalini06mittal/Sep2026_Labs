# Contract-First Design with OpenAPI — A Simple Guide

## Table of Contents
1. [What Is Contract-First Design?](#1-what-is-contract-first-design)
2. [Contract-First vs Code-First](#2-contract-first-vs-code-first)
3. [Why Bother? (The Simple Reasons)](#3-why-bother-the-simple-reasons)
4. [What Is OpenAPI?](#4-what-is-openapi)
5. [The Building Blocks of an OpenAPI YAML File](#5-the-building-blocks-of-an-openapi-yaml-file)
6. [Step 1: The Simplest Possible Spec](#6-step-1-the-simplest-possible-spec)
7. [Step 2: Add a Real Response Body](#7-step-2-add-a-real-response-body)
8. [Step 3: Add a Path Parameter](#8-step-3-add-a-path-parameter)
9. [Step 4: Add a POST Endpoint with a Request Body](#9-step-4-add-a-post-endpoint-with-a-request-body)
10. [Step 5: Reuse Schemas with `components`](#10-step-5-reuse-schemas-with-components)
11. [Step 6: Add Query Parameters, Error Responses, and Enums](#11-step-6-add-query-parameters-error-responses-and-enums)
12. [Step 7: Add Simple Authentication](#12-step-7-add-simple-authentication)
13. [The Full "Not Too Complex" Example](#13-the-full-not-too-complex-example)
14. [Tools to Check Your Work](#14-tools-to-check-your-work)
15. [Quick Recap Cheat Sheet](#15-quick-recap-cheat-sheet)

---

## 1. What Is Contract-First Design?

Imagine two teams building a house: one builds the plumbing, one builds the electrical wiring. If they don't agree on where the walls go *first*, the pipes and wires end up in the wrong place.

**Contract-first design** means you write down the "blueprint" of your API — what endpoints exist, what data goes in, what data comes out — **before** anyone writes actual code. That blueprint is called the **contract**, and with APIs, it's usually written in **OpenAPI** (YAML or JSON).

Everyone — frontend devs, backend devs, testers — builds against that one shared contract.

## 2. Contract-First vs Code-First

| | Code-First | Contract-First |
|---|---|---|
| Order | Write code → generate docs from it | Write spec (YAML) → generate code/docs from it |
| Who agrees first? | Whoever writes the code decides | Everyone agrees before coding starts |
| Risk | Docs can drift from real behavior | Spec and implementation forced to match |
| Best for | Small solo projects, quick prototypes | Teams, public APIs, parallel frontend/backend work |

## 3. Why Bother? (The Simple Reasons)

- **Parallel work**: Frontend can build against a fake/mock server made from the contract while backend is still coding.
- **Fewer surprises**: No "wait, I thought the field was called `user_id` not `userId`" arguments mid-project.
- **Free documentation**: The YAML file *is* the documentation.
- **Free tooling**: Client libraries, server stubs, mock servers, and validators can all be generated automatically from it.

## 4. What Is OpenAPI?

OpenAPI is a **standard format** (a set of rules) for describing a REST API in a YAML or JSON file. It says things like:

- What URLs (paths) exist — e.g. `/users`
- What HTTP methods work on them — `GET`, `POST`, etc.
- What data you send and receive, and its shape
- What errors can happen
- How to authenticate

Think of it as a very structured, machine-readable version of API documentation.

## 5. The Building Blocks of an OpenAPI YAML File

Every OpenAPI file has these main sections:

- `openapi` — which version of the spec you're using (e.g. `3.0.3`)
- `info` — title, description, version of *your* API
- `servers` — where the API actually lives (URL)
- `paths` — the actual endpoints and what they do
- `components` — reusable pieces (schemas, responses, security) so you don't repeat yourself

We'll build these up one at a time.

## 6. Step 1: The Simplest Possible Spec

Just enough to describe one endpoint that returns something, with no real detail yet.

```yaml
openapi: 3.0.3
info:
  title: Hello API
  version: 1.0.0
paths:
  /hello:
    get:
      summary: Say hello
      responses:
        '200':
          description: A greeting
```

That's a complete, valid OpenAPI file. It says: "there is a `GET /hello` endpoint that returns a 200 status." Nothing about the actual data shape yet.

## 7. Step 2: Add a Real Response Body

Now let's say what the response actually *looks like*, using a `schema`.

```yaml
openapi: 3.0.3
info:
  title: Hello API
  version: 1.0.0
paths:
  /hello:
    get:
      summary: Say hello
      responses:
        '200':
          description: A greeting
          content:
            application/json:
              schema:
                type: object
                properties:
                  message:
                    type: string
                    example: Hello, world!
```

Now a client knows: "I'll get back JSON with a `message` field that's a string."

## 8. Step 3: Add a Path Parameter

Let's make it personal: `GET /hello/{name}`.

```yaml
openapi: 3.0.3
info:
  title: Hello API
  version: 1.0.0
paths:
  /hello/{name}:
    get:
      summary: Say hello to someone specific
      parameters:
        - name: name
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: A personalized greeting
          content:
            application/json:
              schema:
                type: object
                properties:
                  message:
                    type: string
                    example: Hello, Alice!
```

Key idea: `in: path` means this value comes from the URL itself, and `required: true` is mandatory for path parameters.

## 9. Step 4: Add a POST Endpoint with a Request Body

Now let's add creating something — e.g. `POST /users`.

```yaml
openapi: 3.0.3
info:
  title: User API
  version: 1.0.0
paths:
  /users:
    post:
      summary: Create a new user
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required:
                - name
                - email
              properties:
                name:
                  type: string
                email:
                  type: string
      responses:
        '201':
          description: User created
          content:
            application/json:
              schema:
                type: object
                properties:
                  id:
                    type: string
                  name:
                    type: string
                  email:
                    type: string
```

Note: `required` at the top of a schema lists which *fields* must be present — different from `required: true` on a parameter.

## 10. Step 5: Reuse Schemas with `components`

Right now, the "user" shape is duplicated in request and response. That's messy. `components/schemas` lets you define it once and reference it everywhere with `$ref`.

```yaml
openapi: 3.0.3
info:
  title: User API
  version: 1.0.0
paths:
  /users:
    post:
      summary: Create a new user
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/NewUser'
      responses:
        '201':
          description: User created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'

components:
  schemas:
    NewUser:
      type: object
      required:
        - name
        - email
      properties:
        name:
          type: string
        email:
          type: string
    User:
      type: object
      properties:
        id:
          type: string
        name:
          type: string
        email:
          type: string
```

`$ref` just means "go look over there for the definition." This is the single biggest thing that keeps specs clean as they grow.

## 11. Step 6: Add Query Parameters, Error Responses, and Enums

Let's add `GET /users` with pagination, a status filter, and a proper error response.

```yaml
  /users:
    get:
      summary: List users
      parameters:
        - name: limit
          in: query
          required: false
          schema:
            type: integer
            default: 20
        - name: status
          in: query
          required: false
          schema:
            type: string
            enum: [active, inactive]
      responses:
        '200':
          description: A list of users
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/User'
        '400':
          description: Bad request
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
```

And add the reusable `Error` schema to `components`:

```yaml
    Error:
      type: object
      properties:
        code:
          type: string
        message:
          type: string
```

- `in: query` → comes from `?limit=20&status=active`
- `enum` → restricts the value to a fixed list of options
- Every real endpoint should describe at least one error response, not just the happy path.

## 12. Step 7: Add Simple Authentication

Most real APIs need auth. Here's the common "Bearer token" pattern.

```yaml
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

security:
  - bearerAuth: []
```

Putting `security` at the top level applies it to *all* endpoints by default. You can override it per-endpoint if one path should be public.

## 13. The Full "Not Too Complex" Example

Putting everything together — a small but realistic User API:

```yaml
openapi: 3.0.3
info:
  title: User API
  description: A simple API for managing users
  version: 1.0.0

servers:
  - url: https://api.example.com/v1

security:
  - bearerAuth: []

paths:
  /users:
    get:
      summary: List users
      parameters:
        - name: limit
          in: query
          required: false
          schema:
            type: integer
            default: 20
        - name: status
          in: query
          required: false
          schema:
            type: string
            enum: [active, inactive]
      responses:
        '200':
          description: A list of users
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/User'
        '400':
          description: Bad request
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

    post:
      summary: Create a new user
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/NewUser'
      responses:
        '201':
          description: User created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
        '400':
          description: Bad request
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

  /users/{userId}:
    get:
      summary: Get a single user
      parameters:
        - name: userId
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: The requested user
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
        '404':
          description: User not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:
    NewUser:
      type: object
      required:
        - name
        - email
      properties:
        name:
          type: string
        email:
          type: string

    User:
      type: object
      properties:
        id:
          type: string
        name:
          type: string
        email:
          type: string
        status:
          type: string
          enum: [active, inactive]

    Error:
      type: object
      properties:
        code:
          type: string
        message:
          type: string
```

This covers: listing, filtering, pagination, getting one item, creating an item, error handling, and auth — without going into deeper OpenAPI features like webhooks, `oneOf`/`allOf` polymorphism, or callbacks.

## 14. Tools to Check Your Work

- **Swagger Editor** (swagger.io/tools/swagger-editor) — paste your YAML, see live errors and a rendered doc UI.
- **Redocly / Redoc** — turns the YAML into a clean, browsable API doc page.
- **openapi-generator** — generates client SDKs or server stub code straight from the YAML.
- **Prism** (by Stoplight) — spins up a mock server from your spec so frontend devs can start coding immediately.

A good workflow: write the YAML → validate it in Swagger Editor → run Prism to mock it → build frontend and backend in parallel against that mock.

## 15. Quick Recap Cheat Sheet

| Concept | What it does |
|---|---|
| `paths` | Defines your endpoints (URLs + methods) |
| `parameters` (`in: path`/`query`) | Inputs baked into the URL |
| `requestBody` | Data sent by the client (POST/PUT/PATCH) |
| `responses` | What comes back, per status code |
| `schema` | Describes the shape/type of data |
| `components/schemas` | Reusable data shapes, referenced via `$ref` |
| `enum` | Restricts a value to a fixed set of options |
| `securitySchemes` + `security` | Describes how clients authenticate |

**The core habit to build:** start small (one endpoint, no auth, no reuse), get it valid, then add one concept at a time — parameters, then request bodies, then reusable schemas, then errors, then auth. You never need to write a "complex" spec in one go; it's just small steps stacked up.
