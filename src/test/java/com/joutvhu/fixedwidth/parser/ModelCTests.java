package com.joutvhu.fixedwidth.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ModelCTests {
    private FixedParser fixedParser;

    @BeforeAll
    public void beforeTest() {
        this.fixedParser = FixedParser.parser();
    }

    @Test
    public void read1Test() {
        ModelC model = fixedParser.parse("v2bdhWBxFBlYNQY88L0mGPQSmlHAww8GqHtD", ModelC.class);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(3, model.getListMap().size());
        Assertions.assertEquals("Bx", model.getListMap().get(0).get("hW"));
        Assertions.assertEquals("0m", model.getListMap().get(1).get("8L"));
        Assertions.assertEquals("tD", model.getListMap().get(2).get("qH"));
    }
}
