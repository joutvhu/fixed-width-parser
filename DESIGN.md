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
7. [Readers and writers](#7-readers-and-writers)
8. [Handler system](#8-handler-system)
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
│   └── FixedHandler.java         ← meta-annotation linking annotation → handler
│
├── constraint/                   ← validation annotations
│   ├── FixedFormat.java          ← date / number / boolean format
│   ├── FixedOption.java          ← allowed-values list
│   └── FixedRegex.java           ← regex constraint
│
├── convert/                      ← readers, writers, handlers
│   ├── FixedWidthReader.java     ← base class for readers
│   ├── FixedWidthWriter.java     ← base class for writers
│   ├── AnnotationHandler.java    ← interface for annotation-driven handlers
│   ├── ParsingApprover.java      ← base class with reject() mechanism
│   ├── reader/                   ← 7 built-in readers
│   ├── writer/                   ← 7 built-in writers
│   ├── handler/                  ← 8 built-in annotation handlers
│   └── general/                  ← shared helpers (NumberHelper, BooleanHelper)
│
├── domain/                       ← enums (Alignment, KeepPadding, Padding, OnError)
│
├── module/
│   ├── FixedModule.java          ← abstract registry for readers/writers
│   └── DefaultModule.java        ← default registration
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
│   ├── FixedMetadataRegistry.java← metadata cache
│   ├── FieldDependencyResolver.java ← topological sort for field ordering
│   ├── FixedStringBuilder.java   ← interface: write-side output accumulator
│   ├── DefaultFixedStringBuilder.java
│   ├── StringAssembler.java      ← interface: raw string slice/pad/trim
│   ├── FixedStringAssembler.java ← implementation
│   ├── ReadStrategy.java         ← interface passed to readers
│   └── WriteStrategy.java        ← interface passed to writers
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

- **`FixedModule`** — registry of reader and writer classes
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
| `with(module)` | Merges — the new module's readers/writers take priority |

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

`FixedParseStrategy` implements both `ReadStrategy` and `WriteStrategy`. It is the central orchestrator.

### READ pipeline (per field)

```
ObjectHook iterates fields in dependency order
│
├─ READ_PRE_CUT      ← hooks may call ctx.skipCurrentField()
│   invokeHooks()
│
├─ [skip? → write blank, continue]
│
├─ child assembler = assembler.child(start, length)
│
├─ READ_AFTER_CUT    ← raw string available in ctx.getRawString()
│   invokeHooks()   ← e.g. EncodingHook re-decodes bytes
│
├─ [blank check → null or throw if required]
│
├─ trim/pad according to FixedTypeInfo defaults
│
├─ READ_AFTER_TRANSFORM  ← processed string in ctx.getProcessedString()
│   invokeHooks()   ← e.g. RegexHook, OptionHook validate here
│
├─ module-hook.handle()  ← StringHook / NumberHook / DateHook / … perform parsing here
│
├─ READ_AFTER_CONVERT  ← Java value in ctx.getCurrentValue()
│   invokeHooks()
│
├─ READ_AFTER_SET
│   invokeHooks()
│
└─ set value on parent object via reflection
```

### WRITE pipeline (per field)

```
ObjectHook iterates fields in dependency order
│
├─ WRITE_PRE_GET     ← hooks may call ctx.skipCurrentField()
│   invokeHooks()   ← e.g. ConditionalHook skips field here
│
├─ [skip? → write blank, continue]
│
├─ read field value from object via reflection
│
├─ WRITE_AFTER_GET   ← value in ctx.getCurrentValue()
│   invokeHooks()   ← hooks may transform value (e.g. encrypt)
│
├─ module-hook.handle()  ← StringHook / NumberHook / DateHook / … perform serialization here
│
├─ WRITE_AFTER_CONVERT  ← string result in ctx.getCurrentValue()
│   invokeHooks()   ← e.g. EncodingHook re-encodes bytes
│
├─ pad(assembler, info)          ← apply alignment and padding
│
├─ WRITE_AFTER_TRANSFORM  ← final padded string in ctx.getCurrentValue()
│   invokeHooks()   ← e.g. RegexHook, OptionHook validate here
│
├─ WRITE_AFTER_PUT
│   invokeHooks()
│
├─ builder.addPart(fieldName, info, paddedString)
│
└─ [blank + required? → throw]
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

`FixedModule` holds two collections: `hooks` (a `LinkedHashSet` of module-hook classes) and `hookInstances` (a cache of singletons). **Order matters**: the first hook that returns `true` for `supports()` is used.

### Hook selection

```java
for (Class<? extends ModuleHook> hookClass : hooks) {
    ModuleHook candidate = hookInstances.get(hookClass);
    if (candidate != null && candidate.supports(info)) {
        return candidate;  // first match wins
    }
}
throw new NoHookFoundException(info);
```

A hook signals "I can't handle this type" by returning `false` from `supports(info)`.

### DefaultModule registration order

```
Hooks: StringHook → BooleanHook → NumberHook → DateHook → EnumHook → UUIDHook → OptionalHook → CollectionHook → MapHook → ObjectHook
```

`ObjectHook` must be last because it accepts any class annotated with `@FixedObject`.

### Module composition

```java
// Add custom hooks with higher priority
FixedParser.parser().with(new MyModule());

// Replace everything
FixedParser.parser().use(new MyFullModule());
```

`with()` calls `newModule.merge(existingModule)`, which appends the existing hooks after the new ones — so new entries have priority.

---

## 7. Hooks

### Built-in module-hooks (`convert/hook/`)

| Class | Handles | Notes |
|-------|---------|-------|
| `StringHook` | `String`, `char` | Trims padding; char requires `length=1` |
| `NumberHook` | `Byte`, `Short`, `Integer`, `Long`, `Float`, `Double`, `BigInteger`, `BigDecimal`, atomic types | Supports `DecimalFormat` via `@FixedFormat` |
| `DateHook` | `LocalDate`, `LocalTime`, `LocalDateTime`, `ZonedDateTime`, `Instant`, `Date`, `sql.Date/Time/Timestamp` | Format from `@FixedFormat` |
| `BooleanHook` | `Boolean`, `boolean` | Tokens: `Y/N`, `T/F`, `YES/NO`, `TRUE/FALSE`, `ON/OFF`, `1/0`; custom via `@FixedFormat` |
| `CollectionHook` | `Collection` and subtypes | Supports `@FixedCount`, `@FixedDelimiter`, `@FixedTerminator` |
| `MapHook` | `Map` and subtypes | Key+value pairs via `@FixedParam` |
| `ObjectHook` | Any `@FixedObject` class | Recursive; pushes OBJECT frame; sorts fields by dependency |
| `EnumHook` | `Enum` types | Parses/exports Enums |
| `UUIDHook` | `UUID` | Parses/exports UUIDs |
| `OptionalHook` | `Optional` | Wraps other types |

After a hook writes a string, `FixedParseStrategy` calls `pad(info)` to apply alignment and padding to the correct `length`.

---

## 8. Handler system

The handler system is the primary extension mechanism. It decouples annotation semantics from the pipeline engine.

### How it works

1. Any annotation can be linked to a hook by annotating it with `@FixedHandler(MyHook.class)`.
2. At each pipeline phase, `FixedModule.invokeHooks()` scans all annotations on the current field/class.
3. For each annotation that carries `@FixedHandler`, the declared hook is instantiated (no-arg constructor) and invoked if the current phase is in `hook.getSupportedPhases()`.
4. Composed annotations (meta-annotations) are unwrapped up to depth 3, so `@StandardDate` → `@FixedFormat` works automatically.

```
field annotations
  └─ for each annotation A:
       └─ A.annotationType().getAnnotation(FixedHandler.class)?
            └─ yes → instantiate hook → hook.getSupportedPhases() contains currentPhase?
                 └─ yes → hook.handle(info, ctx)
```

### Hook interface

```java
public interface Hook {
    // Which phases to be called at (default: all phases)
    default Set<Phase> getSupportedPhases() { ... }

    // Field names that must be parsed before this field
    default Set<String> getDependencies(FixedTypeInfo info) { ... }

    // The actual logic
    void handle(FixedTypeInfo info, ParseContext ctx);
}
```

### Built-in annotation hooks (`convert/hook/`)

| Hook | Annotation | Phases |
|---------|-----------|--------|
| `RegexHook` | `@FixedRegex` | `READ_AFTER_TRANSFORM`, `WRITE_AFTER_TRANSFORM` |
| `OptionHook` | `@FixedOption` | `READ_AFTER_TRANSFORM`, `WRITE_AFTER_TRANSFORM` |
| `FormatDispatchHook` | `@FixedFormat` | `READ_AFTER_TRANSFORM` — dispatches to Date/Boolean/Number hook |
| `DateHook` | (via FormatDispatch) | date format validation |
| `BooleanHook` | (via FormatDispatch) | boolean token validation |
| `NumberHook` | (via FormatDispatch) | number format validation |
| `ConditionalHook` | `@FixedConditional` | `READ_PRE_CUT`, `WRITE_PRE_GET` — calls `ctx.skipCurrentField()` |
| `EncodingHook` | `@FixedEncoding` | `READ_AFTER_CUT`, `WRITE_AFTER_CONVERT` — re-encodes bytes |

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

When `ctx.isCollectErrors()` is true, `FixedParseStrategy` wraps hook calls in try/catch. Errors are recorded as `DefaultParseError` objects and stored in `ctx.collectedErrors`. The parse continues with the next field.

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
@FixedHandler(UpperCaseHook.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UpperCase {}

// 2. Implement the hook
public class UpperCaseHook implements Hook {

    @Override
    public Set<Phase> getSupportedPhases() {
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
@FixedFormat(format = "yyyy-MM-dd")   // ← carries @FixedHandler(FormatDispatchHook.class)
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

### Immutable metadata

`FixedTypeInfo` is immutable after construction. This allows safe concurrent access from multiple threads without synchronization on reads. The cache in `FixedMetadataRegistry` uses `ConcurrentHashMap`.

### Hook selection by supports()

A hook signals "I can't handle this type" by returning `false` from `supports(info)`. This keeps the selection logic in the module generic and allows adding new hooks without modifying the module. `NoHookFoundException` is thrown when no hook can handle the field.

### Annotation-driven hooks vs. module-registered validators

The old validator system required registering classes in a module. The new `@FixedHandler` system is self-contained: the annotation carries a reference to its hook, so user-defined annotations work without any module changes. This is the preferred extension point for new validation or transformation logic.

### Frame stack for nested objects

The `ParseContext` frame stack mirrors the object tree. This gives handlers access to parent objects and sibling fields that have already been parsed — enabling patterns like checksum fields, conditional fields, and dynamic collection sizes.

### Dependency-ordered field processing

Fields are sorted by `FieldDependencyResolver` before processing. This ensures that `@FixedConditional` and `@FixedCount(field=...)` always see the value of the referenced field, regardless of declaration order in the class.

### FixedStringBuilder for write-side inspection

During export, `ObjectWriter` accumulates field outputs in a `FixedStringBuilder` before assembling the final string. Handlers running at `WRITE_PRE_GET` can read already-written fields from the builder — enabling computed fields like checksums that depend on other fields' output.

### ThreadLocal context

`FixedParseStrategy` stores the active `ParseContext` in a `ThreadLocal`. This avoids threading issues when the same `FixedParser` instance is used concurrently, while keeping the API clean (no context parameter on every method).
