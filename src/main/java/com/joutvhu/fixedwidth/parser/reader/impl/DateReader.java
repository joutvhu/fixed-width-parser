package com.joutvhu.fixedwidth.parser.reader.impl;

import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.validator.FixedFormat;
import com.joutvhu.fixedwidth.parser.reader.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.ObjectUtil;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

public class DateReader extends FixedWidthReader<Object> {
    private static final List<Class> TYPES = CommonUtil
            .listOf(Date.class, java.sql.Date.class, Timestamp.class, LocalDate.class, LocalTime.class, LocalDateTime.class, Instant.class);

    public DateReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!TYPES.contains(info.getType()))
            this.skip();
    }

    @Override
    public Object read(StringAssembler assembler) {
        Class<?> type = info.getType();
        String value = info.trimValue(assembler);
        FixedFormat fixedFormat = info.getAnnotation(FixedFormat.class);
        String format = fixedFormat != null ? fixedFormat.format() : null;

        if (CommonUtil.isNotBlank(value))
            return ObjectUtil.parseDate(value, type, format);
        return null;
    }
}
