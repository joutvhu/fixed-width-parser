package com.joutvhu.fixedwidth.parser.convert.reader;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.exception.FixedParserException;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ReadStrategy;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.FixedHelper;

import java.util.Map;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public class MapReader extends FixedWidthReader<Map<?, ?>> {
    protected FixedTypeInfo keyInfo;
    protected FixedTypeInfo valueInfo;
    protected Integer keyLength = 0;
    protected Integer valueLength = 0;

    public MapReader(FixedTypeInfo info, ReadStrategy strategy) {
        super(info, strategy);
        if (!Map.class.isAssignableFrom(info.getType()) || info.getGenericTypeInfo().size() != 2)
            this.reject();
        this.keyInfo = info.getGenericTypeInfo().get(0);
        this.valueInfo = info.getGenericTypeInfo().get(1);
        this.keyLength = keyInfo.getLength();
        this.valueLength = valueInfo.getLength();
    }

    @Override
    public Map<?, ?> read(StringAssembler assembler) {
        Class<?> type = info.getType();
        Class<? extends Map> selectedType = (Class<? extends Map>) FixedHelper.selectSubTypeOf(type);
        if (selectedType == null)
            throw new FixedParserException(String.format("Not found subclass for %s", info.getLabel()));

        Map<Object, Object> objects = FixedHelper.newInstanceOf(selectedType);
        if (keyLength > 0 && valueLength > 0) {
            int len = assembler.length();
            int cursor = 0;
            while (cursor < len) {
                String rawKey = assembler.get(cursor, keyLength);
                if (rawKey == null || rawKey.isEmpty()) break;

                StringAssembler keyAssembler = assembler.child(cursor, keyLength);
                if (keyAssembler.isBlank(keyInfo)) break;
                Object key = read(keyInfo, keyAssembler);
                cursor += keyLength;

                StringAssembler valueAssembler = assembler.child(cursor, valueLength);
                Object value = read(valueInfo, valueAssembler);
                cursor += valueLength;
                objects.put(key, value);
            }
        }
        return objects;
    }
}
