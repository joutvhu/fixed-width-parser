package com.joutvhu.fixedwidth.parser.convert.reader;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.exception.FixedException;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
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

    protected Integer start = 0;
    protected Integer keyLength = 0;
    protected Integer valueLength = 0;

    public MapReader(FixedTypeInfo info, FixedParseStrategy strategy) {
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
            throw new FixedException(String.format("Not found subclass for %s", info.getLabel()));

        Map<Object, Object> objects = FixedHelper.newInstanceOf(selectedType);
        if (keyLength > 0 && valueLength > 0) {
            int len = assembler.length();
            while (start < len) {
                Object key = strategy.read(keyInfo, assembler.child(start, keyLength));
                start += keyLength;
                Object value = strategy.read(valueInfo, assembler.child(start, valueLength));
                start += valueLength;
                objects.put(key, value);
            }
        }
        return objects;
    }
}
