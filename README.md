# fixed-width-parser

Fixed Width Parser is a small library that purpose is:

- Parse fixed width string to java object.

- Export java object to fixed width string.

## Installation

- If you are using Gradle just add the following dependency to your `build.gradle`.

```groovy
compile "com.github.joutvhu:fixed-width-parser:1.1.2"
```

- Or add the following dependency to your `pom.xml` if you are using Maven.

```xml
<dependency>
    <groupId>com.github.joutvhu</groupId>
    <artifactId>fixed-width-parser</artifactId>
    <version>1.1.2</version>
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

In addition to the annotations to describe the fixed width fields,
the _fixed-width-parser_ provides the following constraint annotations to validate fixed width value.

- `@FixedFormat` is used to check for formatting and help parse with certain types of fields like `date`, `number`, `boolean`.

- `@FixedOption` is used to ensure that the fixed width string is one of the specified options.

- `@FixedRegex` is used to check that the fixed width string must match a regular expression.

See the following example:

```java
@FixedObject(subTypes = {
        @FixedObject.Type(value = Food.class, prop = "id", matchWith = "^[0-5].+$")
}, defaultSubType = Medicine.class)
public class Product {
    @FixedField(label = "Product Id", start = 0, length = 5)
    private Long id;

    @FixedField(label = "Product Name", start = 5, length = 20)
    private String name;
}

@FixedObject
public class Food extends Product {
    @FixedFormat(format = "MM/dd/yyyy")
    @FixedField(label = "Expiry Date", start = 25, length = 10)
    private LocalDate expiryDate;

    @FixedOption(options = {"rice  ", "breads", "fruit "})
    @FixedField(label = "Type", start = 35, length = 6)
    private String type;
}

@FixedObject
public class Medicine extends Product {
    @FixedFormat(format = "Y|N")
    @FixedField(label = "Topical", start = 25, length = 1)
    private Boolean topical;

    @FixedField(label = "Ingredients", start = 26, length = 60)
    private List<@FixedParam(length = 15) String> ingredients;
}
```

### Convert fixed width string

- Parse fixed width string to java object.

```java
Food food = (Food) FixedParser
        .parser()
        .parse(Product.class, "00001Dragon Fruit        09/30/2020fruit ");

Medicine medicine = (Medicine) FixedParser
        .parser()
        .parse(Product.class, "60002Golden Star Balm    YCamphor        Peppermint oil Menthol        Tea Tree Oil   ");
```

- Export java object to fixed width string.

```java
String value = FixedParser
    .parser()
    .export(food);
```
