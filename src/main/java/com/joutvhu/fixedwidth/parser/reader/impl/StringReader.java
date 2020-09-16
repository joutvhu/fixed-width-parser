package com.joutvhu.fixedwidth.parser.reader.impl;

import com.joutvhu.fixedwidth.parser.reader.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import jdk.nashorn.internal.runtime.ParserException;

import java.util.List;

public class StringReader extends FixedWidthReader<Object> {
    private static final List<Class> TYPES = CommonUtil.listOf(String.class, Character.class, char.class);

    public StringReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!TYPES.contains(info.getType()))
            this.skip();
    }

    @Override
    public Object read(StringAssembler assembler) {
        String value = assembler.get(info);
        if (!String.class.equals(info.getType())) {
            if (info.getLength() != 1)
                throw new ParserException(String.format("Type of %s is char then it's length must be 1."));
            return CommonUtil.isNotBlank(value) ? value.charAt(0) : null;
        }
        return value;
    }
}
