package com.joutvhu.fixedwidth.parser.convert.reader;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.exception.FixedParserException;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ReadStrategy;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.FixedHelper;

import java.util.Collection;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public class CollectionReader extends FixedWidthReader<Collection<?>> {
    protected FixedTypeInfo valueInfo;
    protected Integer length = 0;

    public CollectionReader(FixedTypeInfo info, ReadStrategy strategy) {
        super(info, strategy);
        if (!Collection.class.isAssignableFrom(info.getType()) || info.getGenericTypeInfo().size() != 1)
            this.reject();
        this.valueInfo = info.getGenericTypeInfo().get(0);
        this.length = valueInfo.getLength();
    }

    @Override
    public Collection<?> read(StringAssembler assembler) {
        Class<?> type = info.getType();
        Class<? extends Collection> selectedType = (Class<? extends Collection>) FixedHelper.selectSubTypeOf(type);
        if (selectedType == null)
            throw new FixedParserException(String.format("Not found subclass for %s", info.getLabel()));

        Collection<Object> objects = FixedHelper.newInstanceOf(selectedType);
        if (length > 0) {
            int len = assembler.length();
            int cursor = 0;
            while (cursor < len) {
                String rawItem = assembler.get(cursor, length);
                if (rawItem == null || rawItem.isEmpty()) break;

                StringAssembler itemAssembler = assembler.child(cursor, length);
                if (itemAssembler.isBlank(valueInfo)) break;
                Object item = read(valueInfo, itemAssembler);
                objects.add(item);
                cursor += length;
            }
        }
        return objects;
    }
}
