package com.joutvhu.fixedwidth.parser.convert.writer;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.WriteStrategy;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Writes an {@link java.util.Optional} into a fixed-width string.
 *
 * @author Giao Ho
 * @since 1.2.1
 */
public class OptionalWriter extends FixedWidthWriter<Optional<?>> {
    protected FixedTypeInfo valueInfo;

    public OptionalWriter(FixedTypeInfo info, WriteStrategy strategy) {
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
    public String write(Optional<?> value) {
        if (value == null || !value.isPresent()) return StringUtils.EMPTY;
        return write(valueInfo, value.get());
    }
}
