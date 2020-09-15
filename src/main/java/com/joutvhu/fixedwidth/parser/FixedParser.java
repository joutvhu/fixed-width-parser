package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.module.DefaultModule;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;

public class FixedParser {
    private FixedModule module = new DefaultModule();
    private FixedParseStrategy strategy = new FixedParseStrategy(module);

    public static FixedParser parser() {
        return new FixedParser();
    }

    public FixedParser module(FixedModule module) {
        this.module = module;
        this.strategy = new FixedParseStrategy(module);
        return this;
    }

    public FixedParser with(FixedModule module) {
        this.module = module.merge(this.module);
        this.strategy = new FixedParseStrategy(this.module);
        return this;
    }

    public <T> T parse(String line, Class<T> type) {
        StringAssembler stringAssembler = StringAssembler.of(line);
        FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(type);
        return (T) this.strategy.read(fixedTypeInfo, stringAssembler);
    }

    public <T> String write(T object) {
        FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(object.getClass());
        return this.strategy.write(fixedTypeInfo, object);
    }
}
