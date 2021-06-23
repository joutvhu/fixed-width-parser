package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.domain.Alignment;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AlignmentTests {

    private FixedParser fixedParser;

    @BeforeAll
    public void beforeTest() {
        this.fixedParser = FixedParser.parser();
    }

    @ParameterizedTest
    @MethodSource("samples")
    void validateFormattedFieldWithPadding(Sample sample) {
        String output = fixedParser.export(sample.person);

        assertEquals(sample.expected, output);
    }

    @SuppressWarnings("unused")
    static List<Sample> samples() {
        return Arrays.asList(
                new Sample(new AutoPerson("Bob"), "Bob       "),
                new Sample(new LeftPerson("Bob"), "Bob       "),
                new Sample(new RightPerson("Bob"), "       Bob"),
                new Sample(new CenterPerson("Bob"), "   Bob    "),
                new Sample(new CenterPerson("Emma"), "   Emma   ")
        );
    }

    @Data
    @AllArgsConstructor
    static class Sample {
        private Object person;
        private String expected;
    }

    @FixedObject
    @Data
    @AllArgsConstructor
    public static class AutoPerson {
        @FixedField(length = 10, alignment = Alignment.AUTO)
        private String name;
    }

    @FixedObject
    @Data
    @AllArgsConstructor
    public static class LeftPerson {
        @FixedField(length = 10, alignment = Alignment.LEFT)
        private String name;
    }

    @FixedObject
    @Data
    @AllArgsConstructor
    public static class RightPerson {
        @FixedField(length = 10, alignment = Alignment.RIGHT)
        private String name;
    }

    @FixedObject
    @Data
    @AllArgsConstructor
    public static class CenterPerson {
        @FixedField(length = 10, alignment = Alignment.CENTER)
        private String name;
    }
}
