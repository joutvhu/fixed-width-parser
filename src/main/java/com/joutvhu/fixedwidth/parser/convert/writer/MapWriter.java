package com.joutvhu.fixedwidth.parser.convert.writer;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;

import java.util.Map;

public class MapWriter extends FixedWidthWriter<Map<?, ?>> {
    protected FixedTypeInfo keyInfo;
    protected FixedTypeInfo valueInfo;

    protected Integer start = 0;
    protected Integer keyLength = 0;
    protected Integer valueLength = 0;

    public MapWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!Map.class.isAssignableFrom(info.getType()) || info.getGenericTypeInfo().size() != 2)
            this.reject();
        this.keyInfo = info.getGenericTypeInfo().get(0);
        this.valueInfo = info.getGenericTypeInfo().get(1);
        this.keyLength = keyInfo.getLength();
        this.valueLength = valueInfo.getLength();
    }

    @Override
    public String write(Map<?, ?> value) {
        StringAssembler assembler = StringAssembler.instance();
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            assembler.set(start, keyLength, strategy.write(keyInfo, entry.getKey()));
            start += keyLength;
            assembler.set(start, valueLength, strategy.write(valueInfo, entry.getValue()));
            start += valueLength;
        }
        return assembler.getValue();
    }
}
