package com.joutvhu.fixedwidth.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ModelETests {
    private FixedParser fixedParser;

    @BeforeAll
    public void beforeTest() {
        this.fixedParser = FixedParser.parser();
    }

    @Test
    public void write1Test() {
        ModelE model = new ModelE(1741404);
        model.setFieldA(0L);
        String line = fixedParser.export(model);
        Assertions.assertNotNull(line);
        Assertions.assertEquals("0001,741,404", line);
    }
}
