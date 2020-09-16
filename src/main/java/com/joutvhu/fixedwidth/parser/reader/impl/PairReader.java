package com.joutvhu.fixedwidth.parser.reader.impl;

import com.joutvhu.fixedwidth.parser.reader.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import javafx.util.Pair;

public class PairReader extends FixedWidthReader<Pair<?, ?>> {
    protected FixedTypeInfo keyInfo;
    protected FixedTypeInfo valueInfo;

    protected Integer keyLength = 0;
    protected Integer valueLength = 0;

    public PairReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!Pair.class.equals(info.getType()) || info.getGenericInfo().size() != 2)
            this.skip();
        this.keyInfo = info.getGenericInfo().get(0);
        this.valueInfo = info.getGenericInfo().get(1);
        this.keyLength = keyInfo.getLength();
        this.valueLength = valueInfo.getLength();
    }

    @Override
    public Pair<?, ?> read(StringAssembler assembler) {
        if (keyLength > 0 && valueLength > 0) {
            Object key = strategy.read(keyInfo, assembler.child(0, keyLength));
            Object value = strategy.read(valueInfo, assembler.child(keyLength, valueLength));
            return new Pair<>(key, value);
        }
        return null;
    }
}
