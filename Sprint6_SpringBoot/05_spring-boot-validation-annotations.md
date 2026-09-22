# Validation Annotations in `spring-boot-starter-validation`

`spring-boot-starter-validation` pulls in **Hibernate Validator** (the reference implementation of **Jakarta Bean Validation**). So the annotations you get come from two places:

1. `jakarta.validation.constraints.*` — the official Jakarta Bean Validation spec annotations (portable, work with any provider).
2. `org.hibernate.validator.constraints.*` — extra annotations provided specifically by Hibernate Validator (not part of the spec, but included automatically since Hibernate Validator is the default provider).

---

## Table of Contents
1. [Core Jakarta Validation Annotations](#1-core-jakarta-validation-annotations)
2. [Hibernate Validator Extra Annotations](#2-hibernate-validator-extra-annotations)
3. [Meta / Composition Annotations](#3-meta--composition-annotations)
4. [Usage Notes](#4-usage-notes)

---

## 1. Core Jakarta Validation Annotations
Package: `jakarta.validation.constraints`

| Annotation | Applies To | What It Checks |
|---|---|---|
| `@NotNull` | Any type | Value must not be `null` |
| `@Null` | Any type | Value must be `null` |
| `@NotEmpty` | `String`, `Collection`, `Map`, arrays | Must not be `null` and must have a length/size > 0 |
| `@NotBlank` | `CharSequence` (String) | Must not be `null` and must contain at least one non-whitespace character |
| `@Size(min=, max=)` | `String`, `Collection`, `Map`, arrays | Size/length must fall within the given range |
| `@Min(value)` | Numeric types | Value must be ≥ the given minimum |
| `@Max(value)` | Numeric types | Value must be ≤ the given maximum |
| `@DecimalMin(value)` | Numeric / `BigDecimal` | Value must be ≥ given decimal minimum |
| `@DecimalMax(value)` | Numeric / `BigDecimal` | Value must be ≤ given decimal maximum |
| `@Positive` | Numeric types | Value must be > 0 |
| `@PositiveOrZero` | Numeric types | Value must be ≥ 0 |
| `@Negative` | Numeric types | Value must be < 0 |
| `@NegativeOrZero` | Numeric types | Value must be ≤ 0 |
| `@Digits(integer=, fraction=)` | Numeric types | Restricts number of digits before/after decimal point |
| `@Pattern(regexp=)` | `CharSequence` | Must match the given regular expression |
| `@Email` | `CharSequence` | Must be a well-formed email address |
| `@Past` | Date/time types | Must be a date/time in the past |
| `@PastOrPresent` | Date/time types | Must be in the past or present |
| `@Future` | Date/time types | Must be a date/time in the future |
| `@FutureOrPresent` | Date/time types | Must be in the future or present |
| `@AssertTrue` | `boolean`/`Boolean` | Value must be `true` |
| `@AssertFalse` | `boolean`/`Boolean` | Value must be `false` |

---

## 2. Hibernate Validator Extra Annotations
Package: `org.hibernate.validator.constraints` (available automatically, not part of the Jakarta spec)

| Annotation | Applies To | What It Checks |
|---|---|---|
| `@NotEmpty` *(HV also re-exports its own historically)* | String/Collection | Same as above — kept here for awareness of overlap |
| `@Length(min=, max=)` | `CharSequence` | String length within range (older alternative to `@Size`) |
| `@Range(min=, max=)` | Numeric types | Value within numeric range (older alternative to `@Min`/`@Max` combined) |
| `@URL` | `CharSequence` | Must be a valid URL |
| `@CreditCardNumber` | `CharSequence` | Must pass Luhn checksum validation for credit card numbers |
| `@EAN` | `CharSequence` | Must be a valid EAN barcode (EAN-8/EAN-13) |
| `@ISBN` | `CharSequence` | Must be a valid ISBN |
| `@LuhnCheck` | `CharSequence` | Generic Luhn algorithm check digit validation |
| `@Mod10Check` | `CharSequence` | Modulo-10 checksum validation |
| `@Mod11Check` | `CharSequence` | Modulo-11 checksum validation |
| `@SafeHtml` | `CharSequence` | Value must not contain unsafe/malicious HTML markup |
| `@ScriptAssert` | Class-level | Validates using a scripting expression (e.g. JavaScript) evaluated against the object |
| `@ParameterScriptAssert` | Method/constructor | Similar to `@ScriptAssert` but for method parameters |
| `@UniqueElements` | Collections | All elements in the collection must be unique |
| `@CodePointLength` | `CharSequence` | Like `@Length` but counts Unicode code points instead of `char`s |
| `@DurationMin` / `@DurationMax` | `Duration` | Duration must be above/below given threshold |

> Note: exact set can vary slightly by Hibernate Validator version — check your Spring Boot version's managed dependency version if you need to confirm a specific annotation exists.

---

## 3. Meta / Composition Annotations

| Annotation | Purpose |
|---|---|
| `@Valid` | Triggers cascaded validation on a nested object, method parameter, or return value (this is `jakarta.validation.Valid`, not a constraint itself but essential for nested validation) |
| `@Validated` | Spring's own annotation (`org.springframework.validation.annotation.Validated`) — enables validation groups and method-level validation on Spring-managed beans |
| `@Constraint` | Used when building your **own custom constraint annotation** |
| `@GroupSequence` | Defines an ordered sequence of validation groups |
| `@ReportAsSingleViolation` | Used in composed constraints to collapse multiple violations into one |

---

## 4. Usage Notes

- **Add the dependency:**
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  ```
- **On a DTO:**
  ```java
  public class UserDto {
      @NotBlank
      private String name;

      @Email
      @NotNull
      private String email;

      @Min(18)
      private int age;
  }
  ```
- **On a controller:** add `@Valid` to trigger validation of the request body.
  ```java
  @PostMapping("/users")
  public ResponseEntity<?> createUser(@Valid @RequestBody UserDto dto) { ... }
  ```
- **Custom message:** every constraint annotation accepts a `message` attribute, e.g. `@NotNull(message = "Name is required")`.
- **Validation groups:** most annotations accept a `groups` attribute to apply conditionally (used with `@Validated(SomeGroup.class)`).
- **Method-level validation:** annotate a Spring bean's class with `@Validated` to validate individual method parameters/return values outside of web request bodies.
