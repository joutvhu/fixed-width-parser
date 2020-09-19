package com.joutvhu.fixedwidth.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ModelBTests {
    private FixedParser fixedParser;

    @BeforeAll
    public void beforeTest() {
        this.fixedParser = FixedParser.parser();
    }

    @Test
    public void read1Test() {
        ModelB model = fixedParser.parse("vGdtMjUCgB43TMqsp21LvGdtMjktR2", ModelB.class);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(15, model.getCollectionValues().size());
        Assertions.assertEquals(15, model.getListValue().size());
        Assertions.assertEquals(6, model.getMapValues().size());
        Assertions.assertEquals(15, model.getQueueValues().size());
        Assertions.assertEquals(12, model.getSetValues().size());
        Assertions.assertEquals(15, model.getStackValues().size());
    }
}
