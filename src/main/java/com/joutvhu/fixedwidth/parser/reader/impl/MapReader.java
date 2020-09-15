package com.joutvhu.fixedwidth.parser.reader.impl;

import com.joutvhu.fixedwidth.parser.reader.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;

import java.util.Map;

public class MapReader extends FixedWidthReader<Map<?, ?>> {
    public MapReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!Map.class.equals(info.getType()) && !Map.class.isAssignableFrom(info.getType()))
            this.skip();
    }

    @Override
    public Map<?, ?> read(StringAssembler assembler) {
        return null;
    }
}
