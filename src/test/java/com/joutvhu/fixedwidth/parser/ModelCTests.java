package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.util.CommonUtil;
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

    @Test
    public void write1Test() {
        ModelC model = new ModelC(CommonUtil.listOf(
                CommonUtil.mapOfEntries(
                        CommonUtil.mapEntryOf("d0", "R1"),
                        CommonUtil.mapEntryOf("7i", "mj"),
                        CommonUtil.mapEntryOf("qD", "o0")
                )
        ));
        String line = fixedParser.export(model);
        Assertions.assertNotNull(line);
        Assertions.assertTrue(line.contains("d0R1"));
        Assertions.assertTrue(line.contains("7imj"));
        Assertions.assertTrue(line.contains("qDo0"));
    }

    @Test
    public void read2Test() {
        ModelC model = fixedParser.parse("BfhgtnoT", ModelC.class);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(1, model.getListMap().size());
        Assertions.assertEquals(2, model.getListMap().get(0).size());
        Assertions.assertEquals("hg", model.getListMap().get(0).get("Bf"));
        Assertions.assertEquals("oT", model.getListMap().get(0).get("tn"));
    }
}
