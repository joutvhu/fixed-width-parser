package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.domain.KeepPadding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DecimalTests {
    @Test
    public void numberTest1() {
        DecimalTests.Numbers numbers = FixedParser
                .parser()
                .parse("00001     0.02 ", DecimalTests.Numbers.class);

        assertEquals(1L, numbers.first);
        assertEquals(0.02, numbers.second);
    }

    @Test
    public void numberTest2() {
        DecimalTests.Numbers numbers = FixedParser
                .parser()
                .parse("00001000000.027", DecimalTests.Numbers.class);

        assertEquals(1L, numbers.first);
        assertEquals(0.027, numbers.second);
    }

    @Test
    public void numberTest3() {
        DecimalTests.Numbers numbers = FixedParser
                .parser()
                .parse("80001320000.102", DecimalTests.Numbers.class);

        assertEquals(80001L, numbers.first);
        assertEquals(320000.102, numbers.second);
    }

    @FixedObject
    public static class Numbers {
        @FixedField(length = 5)
        private Long first;

        @FixedField(start = 5, length = 10, keepPadding = KeepPadding.DROP)
        private Double second;
    }
}
