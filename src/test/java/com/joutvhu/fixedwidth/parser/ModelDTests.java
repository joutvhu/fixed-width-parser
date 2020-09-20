package com.joutvhu.fixedwidth.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ModelDTests {
    private FixedParser fixedParser;

    @BeforeAll
    public void beforeTest() {
        this.fixedParser = FixedParser.parser();
    }

    @Test
    public void read1Test() {
        ModelD model = fixedParser.parse("0111,741,404", ModelD.class);
        Assertions.assertNotNull(model);
        Assertions.assertTrue(model instanceof ModelE);
        Assertions.assertEquals(1741404, ((ModelE) model).getFieldB());
    }

    @Test
    public void read2Test() {
        ModelD model = fixedParser.parse("9241,741.404", ModelD.class);
        Assertions.assertNotNull(model);
        Assertions.assertTrue(model instanceof ModelF);
        Assertions.assertEquals(1741.404, ((ModelF) model).getFieldC());
    }

    @Test
    public void read3Test() {
        ModelD model = fixedParser.parse("099A", ModelD.class);
        Assertions.assertNotNull(model);
        Assertions.assertTrue(model instanceof ModelG);
        Assertions.assertEquals('A', ((ModelG) model).getFieldD());
    }
}
