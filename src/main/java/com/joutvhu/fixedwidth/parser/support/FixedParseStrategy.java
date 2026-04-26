package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.convert.ValidationType;
import com.joutvhu.fixedwidth.parser.exception.FixedException;
import com.joutvhu.fixedwidth.parser.exception.MandatoryValueException;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Fixed width string serialization and deserialization
 * Updated for thread-safety and immutable metadata.
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public class FixedParseStrategy implements ReadStrategy, WriteStrategy {
    private FixedModule module;

    public FixedParseStrategy(FixedModule module) {
        this.module = module;
    }

    private boolean isNumber(FixedTypeInfo info) {
        Class<?> type = info.getType();
        return TypeConstants.INTEGER_NUMBER_TYPES.contains(type) ||
                TypeConstants.DECIMAL_NUMBER_TYPES.contains(type);
    }

    private void validate(FixedTypeInfo info, String value, ValidationType type) {
        List<FixedWidthValidator> validators = module.createValidatorsBy(info, this);
        for (FixedWidthValidator validator : validators) {
            validator.validate(value, type);
        }
    }

    @Override
    public Object read(FixedTypeInfo info, StringAssembler assembler) {
        FixedTypeInfo actualInfo = info.detectTypeWith(assembler);
        
        if (assembler.isBlank(actualInfo) && !isNumber(actualInfo)) {
            if (actualInfo.require)
                throw new MandatoryValueException(actualInfo.buildMessage("{title} cannot be blank."));
            return null;
        }
        
        String validationValue = assembler.getValue();
        // If children list is empty, it's likely a leaf node where trimming matters for validation
        if (actualInfo.getElementTypeInfo().isEmpty() && !actualInfo.getDefaultKeepPadding()) {
            validationValue = FixedStringAssembler.of(validationValue).trim(actualInfo).getValue();
        }
        validate(actualInfo, validationValue, ValidationType.BEFORE_READ);

        FixedWidthReader<Object> reader = module.createReaderBy(actualInfo, this);
        if (reader != null) {
            Object result = reader.read(assembler);
            if (result == null && actualInfo.require)
                throw new MandatoryValueException(actualInfo.buildMessage("{label} cannot be null."));
            return result;
        }
        throw new FixedException("Reader not found.");
    }

    @Override
    public String write(FixedTypeInfo info, Object value) {
        if (value == null) {
            if (info.require)
                throw new MandatoryValueException(info.buildMessage("{label} cannot be null."));
            return FixedStringAssembler.black(info).getValue();
        }
        
        FixedTypeInfo actualInfo = info.detectTypeWith(value);
        FixedWidthWriter<Object> writer = module.createWriterBy(actualInfo, this);
        
        if (writer != null) {
            String result = writer.write(value);
            StringAssembler assembler = FixedStringAssembler
                    .of(CommonUtil.defaultIfNull(result, StringUtils.EMPTY))
                    .pad(actualInfo);

            if (assembler.isBlank(actualInfo)) {
                if (actualInfo.require)
                    throw new MandatoryValueException(actualInfo.buildMessage("{title} cannot be blank."));
            } else validate(actualInfo, assembler.getValue(), ValidationType.AFTER_WRITE);
            return assembler.getValue();
        }
        throw new FixedException("Writer not found.");
    }
}
