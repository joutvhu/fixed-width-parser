# fixed-width-parser

Fixed Width Parser is a small library that purpose is:

- Parse fixed width string to java object.

- Export java object to fixed width string.

## Installation

- If you are using Gradle just add the following dependency to your `build.gradle`.

```groovy
compile "com.github.joutvhu:fixed-width-parser:1.0.4"
```

- Or add the following dependency to your `pom.xml` if you are using Maven.

```xml
<dependency>
    <groupId>com.github.joutvhu</groupId>
    <artifactId>fixed-width-parser</artifactId>
    <version>1.0.4</version>
</dependency>
```

## How to use?

The Fixed Width Parser makes it easy to switch back and forth between the string and the java object.
You only need to use the annotations provided by Fixed Width Parser to annotate your model class to use the parser.

### Annotate the model class

Here are some annotations that the parser offers you.

- `@FixedObject` is used to annotate an object that can be converted to a string, or it can be parsed from a string.

- `@FixedField` is used to annotate fields in the object.
The parser will ignore any fields that are not annotated by this annotation.

- `@FixedParam` will be used to annotate the generic params. Ex: `List<@FixedParam(length = 5) String>`.

### Convert fixed width string

- Parse fixed width string to java object.

```java
Model model = FixedParser.parser().parse("string value", Model.class);
```

- Export java object to fixed width string.

```java
String value = FixedParser.parser().export(modelObject);
```
