package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedParam;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.constraint.FixedOption;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDate;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DemoTests {
    @Test
    public void demoTest() {
        Food food = (Food) FixedParser
                .parser()
                .parse("00001Dragon Fruit        09/30/2020fruit ", Product.class);

        Medicine medicine = (Medicine) FixedParser
                .parser()
                .parse("60002Golden Star Balm    YCamphor        Peppermint oil Menthol        Tea Tree Oil", Product.class);

        String value = FixedParser
                .parser()
                .export(food);

        Assertions.assertNotNull(medicine);
        Assertions.assertNotNull(value);
    }

    @FixedObject(subTypes = {
            @FixedObject.Type(value = Food.class, prop = "id", matchWith = "^[0-5].+$")
    }, defaultSubType = Medicine.class)
    public static class Product {
        @FixedField(label = "Product Id", start = 0, length = 5)
        private Long id;

        @FixedField(label = "Product Name", start = 5, length = 20)
        private String name;
    }

    @FixedObject
    public static class Food extends Product {
        @FixedFormat(format = "MM/dd/yyyy")
        @FixedField(label = "Expiry Date", start = 25, length = 10)
        private LocalDate expiryDate;

        @FixedOption(options = {"rice  ", "breads", "fruit "})
        @FixedField(label = "Type", start = 35, length = 6)
        private String type;
    }

    @FixedObject
    public static class Medicine extends Product {
        @FixedFormat(format = "Y|N")
        @FixedField(label = "Topical", start = 25, length = 1)
        private Boolean topical;

        @FixedField(label = "Ingredients", start = 26, length = 60)
        private List<@FixedParam(length = 15) String> ingredients;
    }
}
