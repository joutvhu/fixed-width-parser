package com.joutvhu.fixedwidth.parser.reader.impl;

import com.joutvhu.fixedwidth.parser.reader.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;

import java.util.Collection;

public class CollectionReader extends FixedWidthReader<Collection<?>> {
    public CollectionReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        this.skip();
    }

    @Override
    public Collection<?> read(StringAssembler assembler) {
        return null;
    }
}
