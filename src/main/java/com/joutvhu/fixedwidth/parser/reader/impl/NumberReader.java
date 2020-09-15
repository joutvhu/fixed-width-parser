package com.joutvhu.fixedwidth.parser.reader.impl;

import com.joutvhu.fixedwidth.parser.reader.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.ObjectUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class NumberReader extends FixedWidthReader<Object> {
    private static final List<Class> TYPES = CommonUtil.listOf(
            Short.class, Integer.class, Long.class, BigInteger.class, short.class, int.class, long.class,
            Float.class, Double.class, BigDecimal.class, float.class, double.class);

    public NumberReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!TYPES.contains(info.getType()))
            this.skip();
    }

    @Override
    public Object read(StringAssembler assembler) {
        Class<?> type = info.getType();
        String value = info.trimValue(assembler);
        return ObjectUtil.readValue(value, type);
    }
}
