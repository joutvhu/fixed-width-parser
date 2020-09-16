package com.joutvhu.fixedwidth.parser.reader.impl;

import com.joutvhu.fixedwidth.parser.reader.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;

public class StringReader extends FixedWidthReader<String> {
    public StringReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!String.class.equals(info.getType()))
            this.skip();
    }

    @Override
    public String read(StringAssembler assembler) {
        return assembler.get(info);
    }
}
