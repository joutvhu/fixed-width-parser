package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.constraint.FixedOption;
import com.joutvhu.fixedwidth.parser.domain.KeepPadding;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OptionTests {
    private FixedParser fixedParser;

    @BeforeAll
    public void beforeTest() {
        this.fixedParser = FixedParser.parser();
    }

    @Test
    void optionsWithDropPadding() {
        DropPaddingStone stone = new DropPaddingStone("blue");

        assertEquals("blue ", fixedParser.export(stone));
        assertEquals("blue", fixedParser.parse(DropPaddingStone.class, "blue ").color);
    }

    @Test
    void optionsWithKeepPadding() {
        KeepPaddingStone stone = new KeepPaddingStone("blue");

        assertEquals("blue ", fixedParser.export(stone));
        assertEquals("blue ", fixedParser.parse(KeepPaddingStone.class, "blue ").color);
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DropPaddingStone {
        @FixedField(length = 5, keepPadding = KeepPadding.DROP)
        @FixedOption(options = {"blue ", "white"})
        private String color;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeepPaddingStone {
        @FixedField(length = 5, keepPadding = KeepPadding.KEEP)
        @FixedOption(options = {"blue ", "white"})
        private String color;
    }
}
