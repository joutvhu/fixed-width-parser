package com.joutvhu.fixedwidth.parser.writer.impl;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.writer.FixedWidthWriter;

import java.util.Map;

public class MapWriter extends FixedWidthWriter<Map<?, ?>> {
    public MapWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if ((!Map.class.equals(info.getType()) && !Map.class.isAssignableFrom(info.getType())) ||
                info.getGenericInfo().size() != 2)
            this.skip();
    }

    @Override
    public String write(Map<?, ?> value) {
        return null;
    }
}
