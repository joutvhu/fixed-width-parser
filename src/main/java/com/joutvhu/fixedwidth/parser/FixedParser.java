package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.module.DefaultModule;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedStringAssembler;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;

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
     */
    public <T> T parse(String line, Class<T> type) {
        StringAssembler stringAssembler = FixedStringAssembler.of(line);
        FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(type);
        return (T) this.strategy.read(fixedTypeInfo, stringAssembler);
    }

    /**
     * Export object to fixed-width string
     *
     * @param object value
     * @param <T>    object type
     * @return fixed-width string
     */
    public <T> String export(T object) {
        FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(object);
        return this.strategy.write(fixedTypeInfo, object);
    }
}
