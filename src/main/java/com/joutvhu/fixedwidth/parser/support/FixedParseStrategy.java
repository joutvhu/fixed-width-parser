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
 * @author Giao Ho
 * @since 1.0.0
 */
public class FixedParseStrategy {
    private FixedModule module;

    public FixedParseStrategy(FixedModule module) {
        this.module = module;
    }

    public void validate(FixedTypeInfo info, String value, ValidationType type) {
        List<FixedWidthValidator> validators = module.createValidatorsBy(info, this);
        for (FixedWidthValidator validator : validators) {
            validator.validate(value, type);
        }
    }

    public Object read(FixedTypeInfo info, StringAssembler assembler) {
        info.detectTypeWith(assembler);
        assembler.trim(info);
        if (assembler.isBlank(info)) {
            if (info.require)
                throw new NullPointerException(info.buildMessage("{label} can\'t be blank."));
            return null;
        }
        validate(info, assembler.getValue(), ValidationType.BEFORE_READ);

        FixedWidthReader<Object> reader = module.createReaderBy(info, this);
        if (reader != null) {
            Object result = reader.read(assembler);
            if (result == null && info.require)
                throw new NullPointerException(info.buildMessage("{title} can\'t be null."));
            return result;
        }
        throw new FixedException("Reader not found.");
    }

    public String write(FixedTypeInfo info, Object value) {
        if (value == null) {
            if (info.require)
                throw new NullPointerException(info.buildMessage("{label} can\'t be null."));
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
                    throw new NullPointerException(info.buildMessage("{label} can\'t be blank."));
            } else validate(info, assembler.getValue(), ValidationType.AFTER_WRITE);
            return assembler.getValue();
        }
        throw new FixedException("Writer not found.");
    }
}
