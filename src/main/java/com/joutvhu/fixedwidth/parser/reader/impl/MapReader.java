package com.joutvhu.fixedwidth.parser.reader.impl;

import com.joutvhu.fixedwidth.parser.reader.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;

import java.util.Map;

public class MapReader extends FixedWidthReader<Map<?, ?>> {
    protected FixedTypeInfo keyInfo;
    protected FixedTypeInfo valueInfo;

    public MapReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if ((!Map.class.equals(info.getType()) && !Map.class.isAssignableFrom(info.getType())) ||
                info.getGenericInfo().size() != 2)
            this.skip();
        this.keyInfo = info.getGenericInfo().get(0);
        this.valueInfo = info.getGenericInfo().get(1);
    }

    @Override
    public Map<?, ?> read(StringAssembler assembler) {
        return null;
    }
}
