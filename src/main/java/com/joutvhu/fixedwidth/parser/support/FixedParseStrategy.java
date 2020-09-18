package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.exception.FixedException;
import com.joutvhu.fixedwidth.parser.handle.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.handle.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.handle.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.handle.ValidationType;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

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
        assembler.pad(info);
        validate(info, assembler.getValue(), ValidationType.BEFORE_READ);

        FixedWidthReader<Object> reader = module.createReaderBy(info, this);
        if (reader != null)
            return reader.read(assembler);
        throw new FixedException("Reader not found.");
    }

    public String write(FixedTypeInfo info, Object value) {
        if (value == null)
            return StringAssembler.instance().black(info).getValue();

        FixedWidthWriter<Object> writer = module.createWriterBy(info, this);
        if (writer != null) {
            String result = writer.write(value);
            result = StringAssembler
                    .of(CommonUtil.defaultIfNull(result, StringUtils.EMPTY))
                    .pad(info)
                    .getValue();

            validate(info, result, ValidationType.AFTER_WRITE);
            return result;
        }
        throw new FixedException("Writer not found.");
    }
}
