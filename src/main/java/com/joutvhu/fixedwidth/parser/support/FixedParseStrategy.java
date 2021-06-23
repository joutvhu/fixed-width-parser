package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.convert.ValidationType;
import com.joutvhu.fixedwidth.parser.exception.FixedException;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Fixed width string serialization and deserialization
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public class FixedParseStrategy implements ReadStrategy, WriteStrategy {
    private FixedModule module;

    public FixedParseStrategy(FixedModule module) {
        this.module = module;
    }

    /**
     * Validate string value with type information form {@link FixedTypeInfo}
     *
     * @param info  {@link FixedTypeInfo}
     * @param value string
     * @param type  is {@link ValidationType}, before read or after write.
     */
    private void validate(FixedTypeInfo info, String value, ValidationType type) {
        List<FixedWidthValidator> validators = module.createValidatorsBy(info, this);
        for (FixedWidthValidator validator : validators) {
            validator.validate(value, type);
        }
    }

    /**
     * Read value from {@link StringAssembler} with type information form {@link FixedTypeInfo}
     *
     * @param info      {@link FixedTypeInfo}
     * @param assembler {@link StringAssembler}
     * @return object value
     */
    @Override
    public Object read(FixedTypeInfo info, StringAssembler assembler) {
        info.detectTypeWith(assembler);
        String value = assembler.getValue();
        assembler.trim(info);
        if (assembler.isBlank(info)) {
            if (info.require)
                throw new NullPointerException(info.buildMessage("{title} cannot be blank."));
            return null;
        }
        validate(info, value, ValidationType.BEFORE_READ);

        FixedWidthReader<Object> reader = module.createReaderBy(info, this);
        if (reader != null) {
            Object result = reader.read(assembler);
            if (result == null && info.require)
                throw new NullPointerException(info.buildMessage("{label} cannot be null."));
            return result;
        }
        throw new FixedException("Reader not found.");
    }

    /**
     * Write object value to string with type information form {@link FixedTypeInfo}
     *
     * @param info  {@link FixedTypeInfo}
     * @param value object
     * @return string value
     */
    @Override
    public String write(FixedTypeInfo info, Object value) {
        if (value == null) {
            if (info.require)
                throw new NullPointerException(info.buildMessage("{label} cannot be null."));
            return FixedStringAssembler.black(info).getValue();
        }
        info.detectTypeWith(value);

        FixedWidthWriter<Object> writer = module.createWriterBy(info, this);
        if (writer != null) {
            String result = writer.write(value);
            StringAssembler assembler = FixedStringAssembler
                    .of(CommonUtil.defaultIfNull(result, StringUtils.EMPTY))
                    .pad(info);

            if (assembler.isBlank(info)) {
                if (info.require)
                    throw new NullPointerException(info.buildMessage("{title} cannot be blank."));
            } else validate(info, assembler.getValue(), ValidationType.AFTER_WRITE);
            return assembler.getValue();
        }
        throw new FixedException("Writer not found.");
    }
}
