package com.joutvhu.fixedwidth.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ModelFTests {
    private FixedParser fixedParser;

    @BeforeAll
    public void beforeTest() {
        this.fixedParser = FixedParser.parser();
    }

    @Test
    public void write1Test() {
        ModelF model = new ModelF(1741.404);
        model.setFieldA(699L);
        String line = fixedParser.export(model);
        Assertions.assertNotNull(line);
        Assertions.assertEquals("6991,741.404", line);
    }
}
