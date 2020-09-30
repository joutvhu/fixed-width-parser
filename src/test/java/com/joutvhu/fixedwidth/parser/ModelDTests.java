package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.support.ItemReader;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ModelDTests {
    private FixedParser fixedParser;

    @BeforeAll
    public void beforeTest() {
        this.fixedParser = FixedParser.parser();
    }

    @Test
    public void read1Test() {
        ModelD model = fixedParser.parse("0111,741,000", ModelD.class);
        Assertions.assertNotNull(model);
        Assertions.assertTrue(model instanceof ModelE);
        Assertions.assertEquals(1741000, ((ModelE) model).getFieldB());
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

    @Test
    public void read4Test() throws IOException {
        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("input/model-d.txt");
        ModelD model;
        ItemReader<ModelD> itemReader = fixedParser.parse(ModelD.class, inputStream);

        Assertions.assertTrue(itemReader.hasNext());
        model = itemReader.next();
        Assertions.assertTrue(model instanceof ModelE);

        Assertions.assertTrue(itemReader.hasNext());
        model = itemReader.next();
        Assertions.assertTrue(model instanceof ModelF);

        Assertions.assertTrue(itemReader.hasNext());
        model = itemReader.next();
        Assertions.assertTrue(model instanceof ModelG);

        Assertions.assertFalse(itemReader.hasNext());

        itemReader.close();
    }

    @Test
    public void read5Test() throws IOException {
        Stream<String> input = CommonUtil
                .listOf("0222,741,000", "9245,741.404", "059A")
                .stream();

        Stream<ModelD> stream = fixedParser.parse(ModelD.class, input);
        Assertions.assertNotNull(stream);
        List<ModelD> models = stream.collect(Collectors.toList());
        Assertions.assertEquals(3, models.size());
    }

    @Test
    public void write1Test() throws IOException {
        Stream<? extends ModelD> input = CommonUtil
                .listOf(
                        new ModelE(22L, 4154812, 111),
                        new ModelF(86L, 8434.501),
                        new ModelG(10L, 'V')
                )
                .stream();

        Stream<String> stream = fixedParser.export(input);
        Assertions.assertNotNull(stream);
        List<String> models = stream.collect(Collectors.toList());
        Assertions.assertEquals(3, models.size());
    }

    @Test
    public void write2Test() throws IOException {
        Stream<? extends ModelD> input = CommonUtil
                .listOf(
                        new ModelE(22L, 4154812, 111),
                        new ModelF(86L, 8434.501),
                        new ModelG(10L, 'V')
                )
                .stream();

        Stream<String> stream = fixedParser.export(ModelD.class, input);
        Assertions.assertNotNull(stream);
        List<String> models = stream.collect(Collectors.toList());
        Assertions.assertEquals(3, models.size());
    }
}
