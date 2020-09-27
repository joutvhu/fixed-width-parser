package com.joutvhu.fixedwidth.parser.convert.writer;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.support.FixedStringAssembler;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.support.WriteStrategy;

import java.util.Map;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public class MapWriter extends FixedWidthWriter<Map<?, ?>> {
    protected FixedTypeInfo keyInfo;
    protected FixedTypeInfo valueInfo;

    protected Integer start = 0;
    protected Integer keyLength = 0;
    protected Integer valueLength = 0;

    public MapWriter(FixedTypeInfo info, WriteStrategy strategy) {
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
        StringAssembler assembler = FixedStringAssembler.instance();
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            assembler.set(start, keyLength, write(keyInfo, entry.getKey()));
            start += keyLength;
            assembler.set(start, valueLength, write(valueInfo, entry.getValue()));
            start += valueLength;
        }
        return assembler.getValue();
    }
}
