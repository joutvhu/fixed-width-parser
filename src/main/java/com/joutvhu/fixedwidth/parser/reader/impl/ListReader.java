package com.joutvhu.fixedwidth.parser.reader.impl;

import com.joutvhu.fixedwidth.parser.reader.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;

import java.util.List;

public class ListReader extends FixedWidthReader<List<?>> {
    public ListReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!List.class.isAssignableFrom(info.getType()))
            this.skip();
        List<Class<?>> genericTypes = info.getGenericTypes();
        if (genericTypes.size() != 1)
            this.skip();
    }

    @Override
    public List<?> read(StringAssembler assembler) {
        return null;
    }
}
