package com.joutvhu.fixedwidth.parser.convert.reader;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ReadStrategy;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Reads a fixed-width string into a {@link java.util.Optional}.
 *
 * @author Giao Ho
 * @since 1.2.1
 */
public class OptionalReader extends FixedWidthReader<Optional<?>> {
    protected FixedTypeInfo valueInfo;

    public OptionalReader(FixedTypeInfo info, ReadStrategy strategy) {
        super(info, strategy);
        if (!Optional.class.equals(info.getType()))
            this.reject();

        ParameterizedType pt = info.getParameterizedType();
        if (pt != null && pt.getActualTypeArguments().length > 0) {
            Type innerType = pt.getActualTypeArguments()[0];
            if (innerType instanceof Class) {
                this.valueInfo = info.ofType((Class<?>) innerType);
            } else if (innerType instanceof ParameterizedType) {
                this.valueInfo = info.ofType((Class<?>) ((ParameterizedType) innerType).getRawType());
            }
        }

        if (this.valueInfo == null)
            this.reject();
    }

    @Override
    public Optional<?> read(StringAssembler assembler) {
        if (assembler.isBlank(info)) return Optional.empty();
        Object result = read(valueInfo, assembler);
        return Optional.ofNullable(result);
    }
}
