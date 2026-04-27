package com.joutvhu.fixedwidth.parser.convert.reader;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ReadStrategy;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;

import java.util.UUID;

/**
 * Reads a fixed-width string into a {@link java.util.UUID}.
 *
 * @author Giao Ho
 * @since 1.2.1
 */
public class UUIDReader extends FixedWidthReader<UUID> {
    public UUIDReader(FixedTypeInfo info, ReadStrategy strategy) {
        super(info, strategy);
        if (!UUID.class.equals(info.getType()))
            this.reject();
    }

    @Override
    public UUID read(StringAssembler assembler) {
        String value = assembler.trim(info).getValue();
        if (CommonUtil.isBlank(value)) return null;
        return UUID.fromString(value);
    }
}
