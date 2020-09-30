package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.module.DefaultModule;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.support.*;
import com.joutvhu.fixedwidth.parser.util.Assert;

import java.io.InputStream;
import java.util.stream.Stream;

/**
 * Fixed width parser
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public class FixedParser {
    private FixedModule module = new DefaultModule();
    private FixedParseStrategy strategy = new FixedParseStrategy(module);

    public static FixedParser parser() {
        return new FixedParser();
    }

    /**
     * Replaces fixed module
     *
     * @param module replaces
     * @return {@link FixedParser}
     */
    public FixedParser use(FixedModule module) {
        this.module = module;
        this.strategy = new FixedParseStrategy(module);
        return this;
    }

    /**
     * Add a fixed module
     *
     * @param module added
     * @return {@link FixedParser}
     */
    public FixedParser with(FixedModule module) {
        this.module = module.merge(this.module);
        this.strategy = new FixedParseStrategy(this.module);
        return this;
    }

    /**
     * Parse fixed-width string with class type to object
     *
     * @param line fixed-width string
     * @param type class type of result
     * @param <T>  type of result
     * @return object
     * @deprecated use the {@link FixedParser#parse(Class, String)} method.
     */
    @Deprecated
    public <T> T parse(String line, Class<T> type) {
        return this.parse(type, line);
    }

    /**
     * Parse fixed-width string with class type to object
     *
     * @param type class type of result
     * @param line fixed-width string
     * @param <T>  type of result
     * @return object
     */
    public <T> T parse(Class<T> type, String line) {
        Assert.notNull(type, "The class type must not be null!");
        Assert.notNull(line, "The line must not be null!");

        StringAssembler stringAssembler = FixedStringAssembler.of(line);
        FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(type);
        return (T) this.strategy.read(fixedTypeInfo, stringAssembler);
    }

    /**
     * Parse fixed-width string from stream
     *
     * @param type   class type of result
     * @param stream string
     * @param <T>    type of result
     * @return {@link Stream} of T
     */
    public <T> Stream<T> parse(Class<T> type, Stream<String> stream) {
        Assert.notNull(type, "The class type must not be null!");
        Assert.notNull(stream, "The stream must not be null!");

        FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(type);
        return stream.map(s -> {
            StringAssembler stringAssembler = FixedStringAssembler.of(s);
            return (T) strategy.read(fixedTypeInfo, stringAssembler);
        });
    }

    /**
     * Parse fixed-width line by line from {@link InputStream}
     *
     * @param type  class type of result
     * @param input the {@link InputStream}
     * @param <T>   type of result
     * @return {@link ItemReader} of T
     */
    public <T> ItemReader<T> parse(Class<T> type, InputStream input) {
        return this.parse(type, input, null);
    }

    /**
     * Parse fixed-width line by line from {@link InputStream}
     *
     * @param type     class type of result
     * @param input    the {@link InputStream}
     * @param encoding of the {@link InputStream}
     * @param <T>      type of result
     * @return {@link ItemReader} of T
     */
    public <T> ItemReader<T> parse(Class<T> type, InputStream input, String encoding) {
        Assert.notNull(type, "The class type must not be null!");
        Assert.notNull(input, "The input stream must not be null!");

        FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(type);
        StringLineReader stringLineReader = new StringLineReader(input, encoding);
        return (ItemReader<T>) new FixedLineItemReader(stringLineReader, strategy, fixedTypeInfo);
    }

    /**
     * Export object to fixed-width string
     *
     * @param object value
     * @param <T>    object type
     * @return fixed-width string
     */
    public <T> String export(T object) {
        Assert.notNull(object, "The object must not be null!");

        FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(object);
        return this.strategy.write(fixedTypeInfo, object);
    }

    /**
     * Export stream object to stream fixed-width string
     *
     * @param objects stream
     * @param <T>     type of objects
     * @return stream string
     */
    public <T> Stream<String> export(Stream<? extends T> objects) {
        Assert.notNull(objects, "The stream must not be null!");

        return objects.map(t -> {
            if (t != null) {
                FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(t);
                return strategy.write(fixedTypeInfo, t);
            }
            return null;
        });
    }

    /**
     * Export stream object to stream fixed-width string
     *
     * @param type    class type of objects
     * @param objects stream
     * @param <T>     type of objects
     * @return stream string
     */
    public <T> Stream<String> export(Class<T> type, Stream<? extends T> objects) {
        Assert.notNull(type, "The class type must not be null!");
        Assert.notNull(objects, "The stream must not be null!");

        FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(type);
        return objects.map(t -> {
            if (t != null)
                return strategy.write(fixedTypeInfo, t);
            return null;
        });
    }
}
