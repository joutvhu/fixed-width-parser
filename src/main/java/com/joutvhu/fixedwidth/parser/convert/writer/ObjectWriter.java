package com.joutvhu.fixedwidth.parser.convert.writer;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;

import java.util.ArrayList;
import java.util.List;

public class ObjectWriter extends FixedWidthWriter<Object> {
    public ObjectWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (info.getFixedObject() == null) this.reject();
    }

    @Override
    public String write(Object value) {
        StringAssembler assembler = StringAssembler.instance();
        List<FixedTypeInfo> children = CommonUtil.defaultIfNull(info.getElementTypeInfo(), new ArrayList<>());
        for (FixedTypeInfo info : children) {
            ReflectionUtil.makeAccessible(info.getField());
            Object item = ReflectionUtil.getField(info.getField(), value);
            assembler.set(info.getStart(), info.getLength(), strategy.write(info, item));
        }
        return assembler.getValue();
    }
}
