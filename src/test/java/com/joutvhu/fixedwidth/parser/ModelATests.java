package com.joutvhu.fixedwidth.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDateTime;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ModelATests {
    private FixedParser fixedParser;

    @BeforeAll
    public void beforeTest() {
        this.fixedParser = FixedParser.parser();
    }

    @Test
    public void read1Test() {
        ModelA model = fixedParser.parse("040myjY2020-09-19 12:14:2785.36", ModelA.class);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(40L, model.getFieldA());
        Assertions.assertEquals("myj", model.getFieldB());
        Assertions.assertEquals(true, model.getFieldC());
        Assertions.assertEquals(LocalDateTime.parse("2020-09-19T12:14:27"), model.getFieldD());
        Assertions.assertEquals(85.36, model.getFieldE());
    }

    @Test
    public void write1Test() {
        ModelA model = new ModelA(40L, "myj", true, LocalDateTime.parse("2020-09-19T12:14:27"), 85.36);
        String line = fixedParser.export(model);
        Assertions.assertNotNull(line);
        Assertions.assertEquals("040myjY2020-09-19 12:14:2785.36", line);
    }

    @Test
    public void write2Test() {
        ModelA model = new ModelA();
        String line = fixedParser.export(model);
        Assertions.assertNotNull(line);
        Assertions.assertEquals("                               ", line);
    }
}
