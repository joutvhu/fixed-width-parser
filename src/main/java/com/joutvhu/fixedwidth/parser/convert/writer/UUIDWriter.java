package com.joutvhu.fixedwidth.parser.convert.writer;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.WriteStrategy;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * Writes a {@link java.util.UUID} into a fixed-width string.
 *
 * @author Giao Ho
 * @since 1.2.1
 */
public class UUIDWriter extends FixedWidthWriter<UUID> {
    public UUIDWriter(FixedTypeInfo info, WriteStrategy strategy) {
        super(info, strategy);
        if (!UUID.class.equals(info.getType()))
            this.reject();
    }

    @Override
    public String write(UUID value) {
        return value != null ? value.toString() : StringUtils.EMPTY;
    }
}
