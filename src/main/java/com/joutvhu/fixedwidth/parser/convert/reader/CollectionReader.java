package com.joutvhu.fixedwidth.parser.convert.reader;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.exception.FixedException;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.FixedHelper;

import java.util.Collection;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public class CollectionReader extends FixedWidthReader<Collection<?>> {
    protected FixedTypeInfo valueInfo;
    protected Integer start = 0;
    protected Integer length = 0;

    public CollectionReader(FixedTypeInfo info, FixedParseStrategy strategy) {
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
            throw new FixedException(String.format("Not found subclass for %s", info.getLabel()));

        Collection<Object> objects = FixedHelper.newInstanceOf(selectedType);
        if (length > 0) {
            int len = assembler.length();
            while (start < len) {
                Object item = strategy.read(valueInfo, assembler.child(start, length));
                objects.add(item);
                start += length;
            }
        }
        return objects;
    }
}
