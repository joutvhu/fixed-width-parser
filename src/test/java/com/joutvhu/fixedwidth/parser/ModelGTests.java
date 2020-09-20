package com.joutvhu.fixedwidth.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ModelGTests {
    private FixedParser fixedParser;

    @BeforeAll
    public void beforeTest() {
        this.fixedParser = FixedParser.parser();
    }

    @Test
    public void write1Test() {
        ModelG model = new ModelG('A');
        model.setFieldA(99L);
        String line = fixedParser.export(model);
        Assertions.assertNotNull(line);
        Assertions.assertEquals("099A", line);
    }
}
