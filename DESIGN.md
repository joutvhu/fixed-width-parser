# Architecture & Design Guide

This document describes the internal architecture of `fixed-width-parser` v2. It is intended for contributors who want to understand how the library works before adding features or fixing bugs.

---

## Table of contents

1. [Package layout](#1-package-layout)
2. [Entry point — FixedParser](#2-entry-point--fixedparser)
3. [Annotation model](#3-annotation-model)
4. [Metadata layer — FixedTypeInfo](#4-metadata-layer--fixedtypeinfo)
5. [Pipeline engine — FixedParseStrategy](#5-pipeline-engine--fixedparsestrategy)
6. [Module system — FixedModule](#6-module-system--fixedmodule)
7. [Module hooks](#7-module-hooks)
8. [Annotation handler system](#8-annotation-handler-system)
9. [ParseContext and frame stack](#9-parsecontext-and-frame-stack)
10. [Dependency resolution](#10-dependency-resolution)
11. [Write builder — FixedStringBuilder](#11-write-builder--fixedstringbuilder)
12. [String manipulation — StringAssembler](#12-string-manipulation--stringassembler)
13. [Error handling](#13-error-handling)
14. [Extension points](#14-extension-points)
15. [Key design decisions](#15-key-design-decisions)

---

## 1. Package layout

```
com.joutvhu.fixedwidth.parser/
│
├── FixedParser.java              ← public API entry point
├── ParseResult.java              ← result type for collect-all mode
├── ParseError.java               ← single error descriptor
├── DefaultParseError.java        ← default ParseError implementation
├── ParseProperties.java          ← per-call property overrides
│
├── annotation/                   ← all annotations
│   ├── FixedObject.java          ← marks a class as a fixed-width object
│   ├── FixedField.java           ← marks a field (start, length)
│   ├── FixedParam.java           ← annotates generic type parameters
│   ├── FixedPadding.java         ← padding / alignment config
│   ├── FixedRequired.java        ← non-null enforcement
│   ├── FixedConditional.java     ← conditional field (depends on another field)
│   ├── FixedCount.java           ← collection element count
│   ├── FixedDelimiter.java       ← delimiter-separated collection
│   ├── FixedTerminator.java      ← terminator-stopped collection
│   ├── FixedEncoding.java        ← per-field charset
│   └── FixedHandler.java         ← meta-annotation linking annotation → Hook class
│
├── constraint/                   ← validation annotations
│   ├── FixedFormat.java          ← date / number / boolean format
│   ├── FixedFormatSymbols.java   ← custom format symbols
│   ├── FixedOption.java          ← allowed-values list
│   ├── FixedBoolean.java         ← boolean token config
│   └── FixedRegex.java           ← regex constraint
│
├── convert/                      ← hook interfaces and implementations
│   ├── Hook.java                 ← unified interface for all hooks
│   ├── ModuleHook.java           ← Hook subtype registered in FixedModule (singleton)
│   ├── handler/                  ← annotation-driven hooks (per-invocation)
│   │   ├── ConditionalHandler.java
│   │   ├── EncodingHandler.java
│   │   ├── FormatDispatchHandler.java
│   │   ├── BooleanHandler.java
│   │   ├── DateHandler.java
│   │   ├── NumberHandler.java
│   │   ├── OptionHandler.java
│   │   └── RegexHandler.java
│   ├── hook/                     ← module-registered hooks (singletons)
│   │   ├── StringHook.java
│   │   ├── BooleanHook.java
│   │   ├── NumberHook.java
│   │   ├── DateHook.java
│   │   ├── EnumHook.java
│   │   ├── UUIDHook.java
│   │   ├── OptionalHook.java
│   │   ├── CollectionHook.java
│   │   ├── MapHook.java
│   │   └── ObjectHook.java
│   └── general/                  ← shared helpers
│       ├── NumberHelper.java
│       └── BooleanHelper.java
│
├── domain/                       ← enums (Alignment, KeepPadding, Padding, OnError)
│
├── module/
│   ├── FixedModule.java          ← abstract registry for module-hooks; dispatches all hooks
│   └── DefaultModule.java        ← default registration of 10 built-in module-hooks
│
├── support/                      ← core engine
│   ├── FixedParseStrategy.java   ← READ/WRITE pipeline orchestrator
│   ├── ParseContext.java         ← interface: per-call state
│   ├── DefaultParseContext.java  ← implementation
│   ├── ContextFrame.java         ← interface: one node in the parse tree
│   ├── DefaultContextFrame.java  ← implementation
│   ├── FrameType.java            ← OBJECT | FIELD | COLLECTION_ITEM | MAP_ENTRY
│   ├── Phase.java                ← pipeline phase enum
│   ├── FixedTypeInfo.java        ← immutable field/type metadata
│   ├── FixedMetadataRegistry.java← metadata cache (ConcurrentHashMap)
│   ├── FieldDependencyResolver.java ← topological sort for field ordering
│   ├── FixedStringBuilder.java   ← interface: write-side output accumulator
│   ├── DefaultFixedStringBuilder.java
│   ├── StringAssembler.java      ← interface: raw string slice/pad/trim
│   ├── FixedStringAssembler.java ← implementation
│   ├── BuiltPart.java            ← one assembled field part (write side)
│   ├── FinalTypeFinder.java      ← interface: subtype detection logic
│   ├── TypeDetector.java         ← abstract: reads annotations, determines SourceType
│   ├── TypeInfoSetter.java       ← abstract: extracts start/length/padding/…
│   ├── FixedLineItemReader.java  ← ItemReader backed by InputStream
│   ├── ItemReader.java           ← interface: streaming parse result
│   ├── StringLineReader.java     ← line-by-line InputStream reader
│   └── TypeConstants.java        ← type classification sets
│
├── validation/
│   └── SchemaValidator.java      ← startup schema checks
│
├── doc/
│   └── SchemaDocument.java       ← Markdown / JSON / CSV schema output
│
├── debug/
│   └── DebugLogger.java          ← per-phase debug logging
│
├── exception/                    ← typed exceptions
└── util/                         ← internal utilities
```

---

## 2. Entry point — FixedParser

`FixedParser` is the only public-facing class users interact with directly. It holds two internal objects:

- **`FixedModule`** — registry of module-hooks and dispatcher for all hooks
- **`FixedParseStrategy`** — the pipeline engine

```
FixedParser
  ├── FixedModule (DefaultModule by default)
  └── FixedParseStrategy
        ├── ThreadLocal<DefaultParseContext>  ← one context per call
        └── Map<Phase, Consumer<ParseContext>> ← phase hooks (debug, testing)
```

Every `parse()` or `export()` call:
1. Creates a fresh `DefaultParseContext` and stores it in a `ThreadLocal`.
2. Builds a `FixedTypeInfo` for the target class (from cache).
3. Delegates to `strategy.read()` or `strategy.write()`.

Module composition:

| Method | Effect |
|--------|--------|
| `use(module)` | Replaces the current module entirely |
| `with(module)` | Merges — the new module's hooks take priority |

---

## 3. Annotation model

Annotations are split into three groups by responsibility:

### Structure annotations (where and what to slice)

| Annotation | Target | Purpose |
|-----------|--------|---------|
| `@FixedObject` | class | Marks a class as parseable; declares subtypes |
| `@FixedField` | field | Declares `start` and `length` |
| `@FixedParam` | type-use | Annotates generic parameters (`List<@FixedParam(length=5) String>`); use `@FixedPadding` alongside for padding config |

### Format / control annotations (how to process the slice)

| Annotation | Purpose |
|-----------|---------|
| `@FixedPadding` | Padding char, null padding, keep/drop, alignment |
| `@FixedRequired` | Throw on null export |
| `@FixedConditional` | Skip field unless another field equals a value |
| `@FixedCount` | Fixed or dynamic collection element count |
| `@FixedDelimiter` | Delimiter-separated collection |
| `@FixedTerminator` | Terminator-stopped collection |
| `@FixedEncoding` | Per-field charset re-encoding |

### Constraint annotations (validate the value)

| Annotation | Purpose |
|-----------|---------|
| `@FixedFormat` | Date pattern, number format, boolean token pair |
| `@FixedOption` | Allowed (or excluded) value list |
| `@FixedRegex` | Regex match |

All constraint and control annotations are linked to their handler via `@FixedHandler` — no module registration is needed.

---

## 4. Metadata layer — FixedTypeInfo

`FixedTypeInfo` is an **immutable** object that holds all metadata for one type, field, or generic parameter. It is built once via reflection and cached in `FixedMetadataRegistry`.

### Inheritance chain

```
FinalTypeFinder  (interface — subtype detection logic)
    └── TypeDetector  (abstract — reads annotations, determines SourceType)
            └── TypeInfoSetter  (abstract — extracts start/length/padding/…, builds child lists)
                    └── FixedTypeInfo  (concrete — adds helper methods)
```

### Four factory methods

| Method | Used when |
|--------|-----------|
| `FixedTypeInfo.of(Class<?>)` | Parsing from a class |
| `FixedTypeInfo.of(Field)` | A field annotated with `@FixedField` |
| `FixedTypeInfo.of(AnnotatedType)` | A generic parameter with `@FixedParam` |
| `FixedTypeInfo.of(Object)` | Exporting from an object instance |

### Padding precedence (field constructor)

```
1. @FixedPadding on the field
2. @FixedPadding on the declaring class (class-level default)
3. Built-in defaults by type (numbers → '0'/RIGHT/DROP, strings → ' '/LEFT/KEEP)
```

### Metadata cache

`FixedMetadataRegistry` uses a `ConcurrentHashMap` keyed by `Class<?>`, `Field`, or `AnnotatedType`. Circular references (class A has a field of type A) are broken by a `ThreadLocal<Set>` that tracks types currently being built — a second encounter returns `null`, which `detectFields()` filters out.

---

## 5. Pipeline engine — FixedParseStrategy

`FixedParseStrategy` is the central orchestrator. It manages the `ThreadLocal<DefaultParseContext>` and drives the READ/WRITE pipelines by calling `FixedModule.invokeHooks()` at each phase.

### READ pipeline (per field)

```
ObjectHook iterates fields in dependency order
│
├─ READ_AFTER_CUT    ← raw string available in ctx.getRawString()
│   invokeHooks()   ← e.g. EncodingHandler re-decodes bytes
│
├─ [blank check → null or throw if required]
│
├─ trim/pad according to FixedTypeInfo defaults
│
├─ READ_AFTER_TRANSFORM  ← processed string in ctx.getProcessedString()
│   invokeHooks()   ← e.g. RegexHandler, OptionHandler validate here
│                   ← FormatDispatchHandler → DateHandler / BooleanHandler / NumberHandler
│
├─ module-hook.handle()  ← StringHook / NumberHook / DateHook / … perform parsing here
│
├─ READ_AFTER_CONVERT  ← Java value in ctx.getCurrentValue()
│   invokeHooks()
│
└─ set value on parent object via reflection
```

> Note: `READ_PRE_CUT` is not fired per-field in the current implementation. `ConditionalHandler` runs at `READ_AFTER_CUT` by checking `ctx.skipCurrentField()` before the blank check.

### WRITE pipeline (per field)

```
ObjectHook iterates fields in dependency order
│
├─ WRITE_AFTER_GET   ← value in ctx.getCurrentValue()
│   invokeHooks()   ← module-hook performs serialization (StringHook, NumberHook, …)
│
├─ [skip? → write blank, continue]
│
├─ WRITE_AFTER_CONVERT  ← string result in ctx.getCurrentValue()
│   invokeHooks()   ← e.g. EncodingHandler re-encodes bytes
│
├─ pad(assembler, info)          ← apply alignment and padding
│
├─ WRITE_AFTER_TRANSFORM  ← final padded string in ctx.getCurrentValue()
│   invokeHooks()   ← e.g. RegexHandler, OptionHandler validate here
│
└─ builder.addPart(fieldName, info, paddedString)
```

### Phase enum

```
READ_PRE_CUT → READ_AFTER_CUT → READ_AFTER_TRANSFORM
  → READ_AFTER_CONVERT → READ_AFTER_SET → READ_AFTER_OBJECT

WRITE_PRE_GET → WRITE_AFTER_GET → WRITE_AFTER_CONVERT
  → WRITE_AFTER_TRANSFORM → WRITE_AFTER_PUT → WRITE_AFTER_OBJECT
```

`Phase.isRead()` and `Phase.isWrite()` are convenience helpers for handlers registered at multiple phases.

---

## 6. Module system — FixedModule

`FixedModule` holds two collections: `hooks` (a `LinkedHashSet` of module-hook classes) and `hookInstances` (a `LinkedHashMap` of singletons). **Order matters**: the first hook that returns `true` for `supports()` is used.

### Hook dispatch — `invokeHooks()`

`FixedModule.invokeHooks()` is the single dispatch point for every phase. It runs in two steps:

```
Step 1 — Annotation-hooks (per-invocation):
  for each annotation on the field/class:
    if annotation carries @FixedHandler:
      instantiate hook (no-arg constructor)
      if currentPhase ∈ hook.phases() → hook.handle(info, ctx)
      if ctx.isSkipField() → stop

Step 2 — Module-hook (first-match singleton):
  for each registered ModuleHook:
    if candidate.supports(info):
      selected = candidate; break
  if selected == null → throw NoHookFoundException
  if currentPhase ∈ selected.phases() → selected.handle(info, ctx)
```

Composed annotations (meta-annotations) are unwrapped up to depth 3, so `@StandardDate` → `@FixedFormat` works automatically.

### DefaultModule registration order

```
StringHook → BooleanHook → NumberHook → DateHook → EnumHook
  → UUIDHook → OptionalHook → CollectionHook → MapHook → ObjectHook
```

`ObjectHook` must be last because it accepts any class annotated with `@FixedObject`.

### Module composition

```java
// Add custom hooks with higher priority
FixedParser.parser().with(new MyModule());

// Replace everything
FixedParser.parser().use(new MyFullModule());
```

`with()` calls `newModule.merge(existingModule)`, which prepends the new hooks before the existing ones — so new entries have priority.

---

## 7. Module hooks

Module-hooks (`convert/hook/`) are **singletons** registered in `FixedModule`. They must be thread-safe.

| Class | Handles | Notes |
|-------|---------|-------|
| `StringHook` | `String`, `char` | Trims padding; char requires `length=1` |
| `NumberHook` | `Byte`, `Short`, `Integer`, `Long`, `Float`, `Double`, `BigInteger`, `BigDecimal`, atomic types | Supports `DecimalFormat` via `@FixedFormat` |
| `DateHook` | `LocalDate`, `LocalTime`, `LocalDateTime`, `ZonedDateTime`, `Instant`, `Date`, `sql.Date/Time/Timestamp` | Format from `@FixedFormat` |
| `BooleanHook` | `Boolean`, `boolean` | Tokens: `Y/N`, `T/F`, `YES/NO`, `TRUE/FALSE`, `ON/OFF`, `1/0`; custom via `@FixedFormat` |
| `CollectionHook` | `Collection` and subtypes | Supports `@FixedCount`, `@FixedDelimiter`, `@FixedTerminator` |
| `MapHook` | `Map` and subtypes | Key+value pairs via `@FixedParam` |
| `ObjectHook` | Any `@FixedObject` class | Recursive; pushes OBJECT frame; sorts fields by dependency |
| `EnumHook` | `Enum` types | Parses/exports by name |
| `UUIDHook` | `UUID` | Parses/exports UUIDs |
| `OptionalHook` | `Optional` | Wraps other types; stores strategy reference via `STRATEGY_KEY` |

After a module-hook writes a string, `FixedParseStrategy` calls `pad(info)` to apply alignment and padding to the correct `length`.

---

## 8. Annotation handler system

Annotation-handlers (`convert/handler/`) are **per-invocation** hooks linked to annotations via `@FixedHandler`. They are instantiated fresh for each call to `invokeHooks()`.

### Built-in annotation handlers

| Class | Annotation | Phases |
|-------|-----------|--------|
| `RegexHandler` | `@FixedRegex` | `READ_AFTER_TRANSFORM`, `WRITE_AFTER_TRANSFORM` |
| `OptionHandler` | `@FixedOption` | `READ_AFTER_TRANSFORM`, `WRITE_AFTER_TRANSFORM` |
| `FormatDispatchHandler` | `@FixedFormat` | `READ_AFTER_TRANSFORM`, `WRITE_AFTER_TRANSFORM` — dispatches to `DateHandler` / `BooleanHandler` / `NumberHandler` |
| `ConditionalHandler` | `@FixedConditional` | `READ_PRE_CUT`, `WRITE_PRE_GET` — calls `ctx.skipCurrentField()` |
| `EncodingHandler` | `@FixedEncoding` | `READ_AFTER_CUT`, `WRITE_AFTER_CONVERT` — re-encodes bytes |

### Hook interface

```java
public interface Hook {
    // The actual logic
    void handle(FixedTypeInfo info, ParseContext ctx);

    // Which phases to be called at (default: all phases)
    default Set<Phase> phases() { ... }

    // Field names that must be parsed before this field
    default Set<String> dependencies(FixedTypeInfo info) { ... }
}
```

`ModuleHook` extends `Hook` and adds:

```java
public interface ModuleHook extends Hook {
    // Whether this hook can handle the given field/type
    boolean supports(FixedTypeInfo info);
}
```

---

## 9. ParseContext and frame stack

A new `DefaultParseContext` is created for every `parse()` or `export()` call and stored in a `ThreadLocal`. It is never shared between calls.

### Frame stack

The context maintains a `Deque<DefaultContextFrame>` that mirrors the object tree being traversed:

```
parse(Product.class, line)
  → push OBJECT frame (Product)
      → push FIELD frame (id)
      → pop FIELD frame
      → push FIELD frame (items)
          → push COLLECTION_ITEM frame [0]
          → pop
          → push COLLECTION_ITEM frame [1]
          → pop
      → pop FIELD frame
  → pop OBJECT frame
```

### ContextFrame fields

| Field | Type | Description |
|-------|------|-------------|
| `typeInfo` | `FixedTypeInfo` | Metadata for this node |
| `frameType` | `FrameType` | `OBJECT`, `FIELD`, `COLLECTION_ITEM`, `MAP_ENTRY` |
| `depth` | `int` | Depth in tree (root = 0) |
| `index` | `int` | Position in collection/map; `-1` otherwise |
| `rawString` | `String` | Immutable slice from input; `null` during WRITE |
| `processedString` | `String` | Mutable; starts equal to `rawString`; handlers may modify |
| `partialResult` | `Object` | Object being assembled (READ) or source object (WRITE) |
| `assembler` | `StringAssembler` | Raw string operations |
| `builder` | `FixedStringBuilder` | Non-null only on WRITE OBJECT frames |

### Property scopes

```java
ctx.put("key", value);        // global — lives for the entire session
ctx.putGlobal("key", value);  // same as put()
ctx.putScoped("key", value);  // removed automatically when current frame is popped
```

### Property priority chain

```
ctx.getProperty("locale", Locale.class, Locale.US)
  → session properties (per-call ParseProperties)
  → parser config (withProperty() on FixedParser)
  → defaultValue
```

---

## 10. Dependency resolution

`FieldDependencyResolver.sort()` reorders a list of `FixedTypeInfo` objects so that fields are processed after their dependencies.

### Dependency sources

- `@FixedConditional(dependsOnField = "x")` — field must be parsed after `x`
- `@FixedCount(field = "x")` — collection size comes from field `x`, so `x` must be parsed first
- `Hook.dependencies(info)` — any hook can declare additional field dependencies

### Algorithm — Kahn's topological sort

```
1. Build dependency graph:
   dependencies[field] = set of fields it depends on
   dependents[field]   = set of fields that depend on it

2. Seed queue with fields that have no dependencies

3. While queue not empty:
   - Take field F from queue
   - Add F to sorted list
   - For each field D that depends on F:
       - Remove F from D's dependency set
       - If D's dependency set is now empty → add D to queue

4. If sorted.size() != fields.size() → CircularDependencyException
```

Fields with no dependency declarations keep their original relative order.

---

## 11. Write builder — FixedStringBuilder

During export, `ObjectHook` creates a `DefaultFixedStringBuilder` and attaches it to the OBJECT frame. After each field is written, the padded string is added to the builder:

```java
builder.addPart("price", priceInfo, "00042");
```

Hooks running at `WRITE_PRE_GET` can inspect already-written fields:

```java
String alreadyWritten = ctx.parentFrame().getBuilder().getPart("price");
```

`builder.build()` assembles the final string by placing each part at its `start` position using a `StringAssembler`.

### FixedStringBuilder API

| Method | Description |
|--------|-------------|
| `addPart(name, info, value)` | Add a field's padded string |
| `replacePart(name, newValue)` | Replace an already-added part |
| `getPart(name)` | Retrieve a part's value |
| `hasPart(name)` | Check if a part exists |
| `getAllParts()` | All parts in insertion order |
| `build()` | Assemble the final output string |

---

## 12. String manipulation — StringAssembler

`FixedStringAssembler` wraps a `String` and provides positional operations:

| Method | Description |
|--------|-------------|
| `get(start, length)` | Extract substring; auto-pads if string is shorter |
| `set(start, length, value)` | Write substring at position; truncates if longer |
| `child(start, length)` | Create a new assembler from a substring |
| `pad(info)` | Apply alignment and padding to reach `length` |
| `trim(info)` | Strip padding characters according to alignment |
| `isBlank(info)` | True if the entire value is the padding character |

`pad()` and `trim()` are inverses: `trim(pad(x)) == x` when `keepPadding = DROP`.

---

## 13. Error handling

### Fail-fast mode (default)

The first exception thrown by a hook propagates immediately.

### Collect-all mode

```java
ParseResult<Product> result = FixedParser.parser()
    .collectErrors()
    .parseResult(Product.class, line);
```

When `ctx.isCollectErrors()` is true, `FixedParseStrategy.invokeHooksSafe()` wraps hook calls in try/catch. Errors are recorded as `DefaultParseError` objects and stored in `ctx.getCollectedErrors()`. The parse continues with the next field.

### ParseError fields

| Field | Description |
|-------|-------------|
| `message` | Human-readable description |
| `phase` | Pipeline phase where the error occurred |
| `fieldPath` | Dot-separated path, e.g. `"Product.items[2].price"` |
| `rawValue` | Raw string at the time of failure |
| `cause` | Underlying exception |

---

## 14. Extension points

### Custom module hook

```java
public class MyTypeHook implements ModuleHook {

    @Override
    public boolean supports(FixedTypeInfo info) {
        return MyType.class.equals(info.getType());
    }

    @Override
    public void handle(FixedTypeInfo info, ParseContext ctx) {
        if (ctx.getPhase().isRead()) {
            String raw = ctx.getProcessedString();
            ctx.setCurrentValue(MyType.parse(raw));
        } else if (ctx.getPhase().isWrite()) {
            MyType value = (MyType) ctx.getCurrentValue();
            ctx.setCurrentValue(value.serialize());
        }
    }
}
```

### Custom module

```java
public class MyModule extends FixedModule {
    public MyModule() {
        super(MyTypeHook.class);
    }
}

FixedParser parser = FixedParser.parser().with(new MyModule());
```

### Custom annotation hook

No module registration needed — just annotate your annotation with `@FixedHandler`.

```java
// 1. Define the annotation
@FixedHandler(UpperCaseHandler.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UpperCase {}

// 2. Implement the hook
public class UpperCaseHandler implements Hook {

    @Override
    public Set<Phase> phases() {
        return EnumSet.of(Phase.READ_AFTER_TRANSFORM);
    }

    @Override
    public void handle(FixedTypeInfo info, ParseContext ctx) {
        String value = ctx.getProcessedString();
        if (value != null)
            ctx.setProcessedString(value.toUpperCase());
    }
}

// 3. Use it
@UpperCase
@FixedField(start = 0, length = 5)
private String code;
```

### Composed annotations

An annotation can itself be annotated with `@FixedFormat` or any other `@FixedHandler` annotation. The engine unwraps up to 3 levels of meta-annotations automatically:

```java
@FixedFormat(format = "yyyy-MM-dd")   // ← carries @FixedHandler(FormatDispatchHandler.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IsoDate {}

// Usage — no need to also add @FixedFormat
@IsoDate
@FixedField(start = 0, length = 10)
private LocalDate date;
```

### Schema validation at startup

```java
FixedParser.parser().validate(Product.class).throwIfInvalid();
```

`SchemaValidator` checks for field overlap, missing dependency fields, and circular subtype references. Run this in application startup to catch configuration errors early.

---

## 15. Key design decisions

### Unified Hook interface

The old `FixedWidthReader` + `FixedWidthWriter` + `AnnotationHandler` trio has been replaced by a single `Hook` interface. A hook handles both parse and export by checking `ctx.getPhase()` inside `handle()`. This eliminates the need to maintain parallel reader/writer hierarchies.

### Two hook lifecycles

- **Module-hooks** (`ModuleHook`) are singletons registered in `FixedModule`. They must be thread-safe because the same instance is reused across concurrent calls.
- **Annotation-hooks** (`Hook` via `@FixedHandler`) are instantiated fresh per `invokeHooks()` call. They do not need to be thread-safe.

### Immutable metadata

`FixedTypeInfo` is immutable after construction. This allows safe concurrent access from multiple threads without synchronization on reads. The cache in `FixedMetadataRegistry` uses `ConcurrentHashMap`.

### Hook selection by supports()

A module-hook signals "I can't handle this type" by returning `false` from `supports(info)`. This keeps the selection logic in the module generic and allows adding new hooks without modifying the module. `NoHookFoundException` is thrown when no hook can handle the field.

### Annotation-driven hooks vs. module-registered hooks

Module-registered hooks require explicit registration and are singletons. Annotation-driven hooks (`@FixedHandler`) are self-contained: the annotation carries a reference to its hook class, so user-defined annotations work without any module changes. This is the preferred extension point for new validation or transformation logic.

### Frame stack for nested objects

The `ParseContext` frame stack mirrors the object tree. This gives handlers access to parent objects and sibling fields that have already been parsed — enabling patterns like checksum fields, conditional fields, and dynamic collection sizes.

### Dependency-ordered field processing

Fields are sorted by `FieldDependencyResolver` before processing. This ensures that `@FixedConditional` and `@FixedCount(field=...)` always see the value of the referenced field, regardless of declaration order in the class.

### FixedStringBuilder for write-side inspection

During export, `ObjectHook` accumulates field outputs in a `FixedStringBuilder` before assembling the final string. Handlers running at `WRITE_PRE_GET` can read already-written fields from the builder — enabling computed fields like checksums that depend on other fields' output.

### ThreadLocal context

`FixedParseStrategy` stores the active `ParseContext` in a `ThreadLocal`. This avoids threading issues when the same `FixedParser` instance is used concurrently, while keeping the API clean (no context parameter on every method).
