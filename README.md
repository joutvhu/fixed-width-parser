# Fixed Width Parser

A Java library for parsing fixed-width strings into objects and exporting objects back to fixed-width strings.

In a fixed-width format, each field occupies a fixed number of characters at a known position — no delimiters needed:

```
"040hello     Y2024-01-15"
 ^^^                        → id     (Long,    3 chars, pos 0)
    ^^^^^^^^^               → name   (String, 10 chars, pos 3)
             ^              → active (Boolean,  1 char,  pos 13)
              ^^^^^^^^^^    → date   (Date,    10 chars, pos 14)
```

## Installation

Gradle:

```groovy
implementation "com.github.joutvhu:fixed-width-parser:2.0.0"
```

Maven:

```xml
<dependency>
    <groupId>com.github.joutvhu</groupId>
    <artifactId>fixed-width-parser</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Quick start

### 1. Annotate your model

```java
@FixedObject
public class Product {
    @FixedField(start = 0, length = 5)
    private Long id;

    @FixedField(start = 5, length = 20)
    private String name;

    @FixedFormat(format = "yyyy-MM-dd")
    @FixedField(start = 25, length = 10)
    private LocalDate expiryDate;
}
```

### 2. Parse and export

```java
FixedParser parser = FixedParser.parser();

// Parse a string into an object
Product product = parser.parse(Product.class, "00001Dragon Fruit        2025-09-30");

// Export an object back to a string
String line = parser.export(product);
```

---

## Annotations

### `@FixedObject`

Marks a class as a fixed-width object. Required on every class you want to parse or export.

```java
@FixedObject(
    length = 0,                  // total length; 0 = unlimited
    subTypes = {                 // polymorphic subtypes
        @FixedObject.Type(value = Food.class, prop = "id", matchWith = "^[0-5].+$")
    },
    defaultSubType = Medicine.class
)
public class Product { ... }
```

Use `@FixedPadding` on the class to set a default padding for all fields:

```java
@FixedObject
@FixedPadding(value = '*', alignment = Alignment.LEFT)
public class Record { ... }
```

### `@FixedField`

Marks a field for parsing. Fields without this annotation are ignored.

```java
@FixedField(
    label  = "Product Id",  // used in error messages
    start  = 0,             // byte offset (default 0)
    length = 5              // number of characters (required)
)
private Long id;
```

### `@FixedParam`

Annotates generic type parameters for collections and maps.

```java
@FixedField(start = 10, length = 0)
private List<@FixedParam(length = 5) String> items;

// With padding on the param
@FixedField(start = 0, length = 0)
private List<@FixedParam(length = 5) @FixedPadding(value = '0', alignment = Alignment.RIGHT) String> ids;

@FixedField(start = 0, length = 0)
private Map<@FixedParam(length = 2) String, @FixedParam(length = 8) String> codes;
```

### `@FixedPadding`

Controls padding character, null padding, keep/drop behaviour, and alignment. Can be placed on a field or on a class (applies to all fields as a default).

```java
// Field-level: right-align with zero padding
@FixedPadding(value = '0', alignment = Alignment.RIGHT, keep = KeepPadding.DROP)
@FixedField(start = 0, length = 8)
private Integer amount;

// Class-level default: all fields use '*' padding unless overridden
@FixedObject
@FixedPadding(value = '*', alignment = Alignment.LEFT)
public class Record {
    @FixedField(length = 10)
    private String code;          // uses '*' from class-level

    @FixedPadding(value = '-')    // overrides class-level
    @FixedField(start = 10, length = 10)
    private String name;
}
```

| Attribute | Default | Description |
|-----------|---------|-------------|
| `value` | `AUTO` | Padding character |
| `nullValue` | `AUTO` | Padding character when value is null |
| `keep` | `AUTO` | `KEEP` or `DROP` padding on parse |
| `alignment` | `AUTO` | `LEFT`, `RIGHT`, or `CENTER` |

Default behaviour by type when no `@FixedPadding` is set:

| Type | Padding | Alignment | Keep on parse |
|------|---------|-----------|---------------|
| Integer, Long, … | `'0'` | RIGHT | DROP |
| Float, Double, … | `'0'` | RIGHT | DROP |
| String, Character | `' '` | LEFT | KEEP |
| Boolean, Date, … | `' '` | LEFT | KEEP |

### `@FixedRequired`

Throws `MandatoryValueException` when exporting a `null` value.

```java
@FixedRequired
@FixedField(start = 0, length = 5)
private String code;
```

### `@FixedFormat`

Specifies the format for date, number, and boolean fields.

```java
// Date
@FixedFormat(format = "yyyy-MM-dd")
@FixedField(start = 0, length = 10)
private LocalDate date;

// Boolean — "Y" for true, "N" for false
@FixedFormat(format = "Y|N")
@FixedField(start = 10, length = 1)
private Boolean active;

// Number with decimal format
@FixedFormat(format = "#,###,###")
@FixedField(start = 11, length = 9)
private Integer amount;
```

### `@FixedOption`

Validates that the field value is (or is not) one of the allowed options.

```java
@FixedOption(options = {"rice  ", "bread ", "fruit "})
@FixedField(start = 0, length = 6)
private String category;

// Exclude mode: value must NOT be one of these
@FixedOption(options = {"ERROR", "NULL "}, contains = false)
@FixedField(start = 6, length = 5)
private String status;
```

### `@FixedRegex`

Validates that the field value matches a regular expression.

```java
@FixedRegex(regex = "^[A-Z]{3}$", message = "{title} must be 3 uppercase letters.")
@FixedField(start = 0, length = 3)
private String code;
```

---

## Collection fields

### Fixed count

```java
// Always read exactly 3 items
@FixedCount(3)
@FixedField(start = 0, length = 0)
private List<@FixedParam(length = 5) String> items;

// Read count from another field
@FixedField(start = 0, length = 3)
private Integer itemCount;

@FixedCount(field = "itemCount")
@FixedField(start = 3, length = 0)
private List<@FixedParam(length = 5) String> items;
```

### Delimiter-separated

```java
// "AA,BBB,CC,DDDD      " → ["AA", "BBB", "CC", "DDDD"]
@FixedDelimiter(",")
@FixedField(start = 0, length = 20)
private List<String> tags;
```

### Terminator

```java
// "AAABBBCCC|          " → ["AAA", "BBB", "CCC"]
@FixedTerminator("|")
@FixedField(start = 0, length = 20)
private List<@FixedParam(length = 3) String> codes;
```

---

## Polymorphic types

Use `@FixedObject.subTypes` to select the concrete class at parse time based on field content.

```java
@FixedObject(
    subTypes = {
        @FixedObject.Type(value = Food.class,     prop = "id", oneOf = {"001", "002"}),
        @FixedObject.Type(value = Medicine.class, prop = "id", matchWith = "^[6-9].+$")
    },
    defaultSubType = GenericProduct.class
)
public class Product {
    @FixedField(start = 0, length = 5)
    private Long id;
    // ...
}
```

```java
Product p = FixedParser.parser().parse(Product.class, line);
// p is an instance of Food, Medicine, or GenericProduct
```

---

## Conditional fields

Parse a field only when another field has a specific value. The dependency resolver ensures the referenced field is always parsed first.

```java
@FixedObject
public class Record {
    @FixedField(start = 0, length = 1)
    private String type;

    @FixedConditional(dependsOnField = "type", whenValue = "A")
    @FixedField(start = 1, length = 10)
    private String dataA;   // only parsed when type == "A"

    @FixedConditional(dependsOnField = "type", whenValue = "B")
    @FixedField(start = 1, length = 10)
    private String dataB;   // only parsed when type == "B"
}
```

---

## Parser API

### Basic parse and export

```java
FixedParser parser = FixedParser.parser();

// Single line
Product p = parser.parse(Product.class, line);

// Stream of lines
Stream<Product> products = parser.parse(Product.class, lineStream);

// InputStream (line by line)
ItemReader<Product> reader = parser.parse(Product.class, inputStream);
while (reader.hasNext()) {
    Product item = reader.next();
}
reader.close();

// Export
String line   = parser.export(product);
Stream<String> lines = parser.export(productStream);
```

### Parser-level properties

Properties set on the parser instance apply to every call.

```java
FixedParser parser = FixedParser.parser()
    .withProperty("locale", Locale.US)
    .withProperty("timezone", ZoneId.of("UTC"));
```

### Per-call session properties

Override parser properties for a single call without affecting the instance.

```java
ParseProperties props = ParseProperties.builder()
    .set("locale", Locale.JAPAN)
    .build();

Product p = parser.parse(Product.class, line, props);
```

### Collect-all-errors mode

Instead of throwing on the first error, collect all field errors and return a partial result.

```java
ParseResult<Product> result = FixedParser.parser()
    .collectErrors()
    .parseResult(Product.class, line);

if (result.hasErrors()) {
    for (ParseError error : result.getErrors()) {
        System.out.printf("[%s] %s — raw: '%s'%n",
            error.getPhase(), error.getFieldPath(), error.getRawValue());
    }
}
Product partial = result.getValue(); // may be partially populated
```

### Custom module

Add custom readers or writers that take priority over the built-in ones.

```java
public class MyModule extends FixedModule {
    public MyModule() {
        super(MyCustomReader.class, MyCustomWriter.class);
    }
}

FixedParser parser = FixedParser.parser().with(new MyModule());
```

---

## Custom annotation handlers

The `@FixedHandler` meta-annotation links any annotation to a handler class. The handler is invoked automatically at the declared pipeline phases — no module registration needed.

```java
// 1. Define the annotation
@FixedHandler(UpperCaseHandler.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UpperCase {}

// 2. Implement the handler
public class UpperCaseHandler implements AnnotationHandler<UpperCase> {
    @Override
    public Set<Phase> getPhases(UpperCase annotation) {
        return Set.of(Phase.READ_AFTER_TRANSFORM);
    }

    @Override
    public void handle(UpperCase annotation, FixedTypeInfo info, ParseContext ctx) {
        String value = ctx.getProcessedString();
        if (value != null) ctx.setProcessedString(value.toUpperCase());
    }
}

// 3. Use it
@UpperCase
@FixedField(start = 0, length = 5)
private String code;
```

### Built-in handlers

| Annotation | Handler | Phases |
|-----------|---------|--------|
| `@FixedRegex` | `RegexHandler` | `READ_AFTER_TRANSFORM`, `WRITE_AFTER_TRANSFORM` |
| `@FixedOption` | `OptionHandler` | `READ_AFTER_TRANSFORM`, `WRITE_AFTER_TRANSFORM` |
| `@FixedFormat` (date/bool/number) | `FormatDispatchHandler` | `READ_AFTER_TRANSFORM` |
| `@FixedConditional` | `ConditionalHandler` | `READ_PRE_CUT` |
| `@FixedEncoding` | `EncodingHandler` | `READ_AFTER_CUT`, `WRITE_AFTER_CONVERT` |

---

## Per-field encoding

Use `@FixedEncoding` when a file mixes character encodings across fields.

```java
@FixedEncoding("ISO-8859-1")
@FixedField(start = 0, length = 20)
private String legacyName;
```

---

## Schema validation

Validate a class schema at startup to catch configuration errors early.

```java
// Throws FixedParserException if any errors are found
FixedParser.parser().validate(Product.class).throwIfInvalid();

// Inspect errors without throwing
SchemaValidator v = FixedParser.parser().validate(Product.class);
if (!v.isValid()) {
    v.getErrors().forEach(System.out::println);
}
```

Detects:
- **Field overlap** — two fields whose `start/length` ranges overlap
- **Missing dependency** — `@FixedConditional` or `@FixedCount(field)` references a non-existent field
- **Circular subtype** — a subtype that directly or transitively references itself

---

## Schema documentation

Generate a schema reference for a class in Markdown, JSON, or CSV.

```java
SchemaDocument doc = FixedParser.parser().document(Product.class);

String markdown = doc.toMarkdown();
String json     = doc.toJson();
String csv      = doc.toCsv();
```

Markdown output example:

```
## Product
| Field       | Start | Length | Type      | Required | Padding | Alignment |
|-------------|-------|--------|-----------|----------|---------|-----------|
| id          | 0     | 5      | Long      | false    |         |           |
| name        | 5     | 20     | String    | false    |         |           |
| expiryDate  | 25    | 10     | LocalDate | false    |         |           |
```

---

## Debug mode

Log every pipeline phase transition to trace parse/export issues.

```java
import java.util.logging.Logger;

Logger log = Logger.getLogger(MyClass.class.getName());

Product p = FixedParser.parser()
    .debug(log)
    .parse(Product.class, line);
```

Each log line (at `FINE` level) shows: phase, field name, raw value, processed value, and converted value.

---

## Pipeline phases

The parse and export pipelines expose the following phases for handlers:

**READ pipeline:**

| Phase | When |
|-------|------|
| `READ_PRE_CUT` | Before slicing the input string |
| `READ_AFTER_CUT` | After slicing; raw string available |
| `READ_AFTER_TRANSFORM` | After padding/trim handlers run |
| `READ_AFTER_CONVERT` | After conversion to Java type |
| `READ_AFTER_SET` | After value is set on the object |
| `READ_AFTER_OBJECT` | After all fields of an object are read |

**WRITE pipeline:**

| Phase | When |
|-------|------|
| `WRITE_PRE_GET` | Before reading the field value from the object |
| `WRITE_AFTER_GET` | After reading the field value |
| `WRITE_AFTER_CONVERT` | After converting the value to string |
| `WRITE_AFTER_TRANSFORM` | After padding/alignment applied |
| `WRITE_AFTER_PUT` | After the string is placed into the output |
| `WRITE_AFTER_OBJECT` | After all fields of an object are written |

---

## License

[MIT](LICENSE)
