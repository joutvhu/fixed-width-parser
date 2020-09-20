package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.exception.InvalidException;
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
    public void read1Test() {
        try {
            fixedParser.parse("0111,741,123", ModelD.class);
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof InvalidException);
        }
    }

    @Test
    public void write1Test() {
        ModelE model = new ModelE(1741111, 111);
        model.setFieldA(0L);
        String line = fixedParser.export(model);
        Assertions.assertNotNull(line);
        Assertions.assertEquals("0001,741,111", line);
    }
}
