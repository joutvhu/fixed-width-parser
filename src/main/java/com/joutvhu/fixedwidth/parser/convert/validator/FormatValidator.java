package com.joutvhu.fixedwidth.parser.convert.validator;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Validator for {@link FixedFormat} annotation
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public abstract class FormatValidator extends FixedWidthValidator {
    protected FixedFormat fixedFormat;

    public FormatValidator(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        this.fixedFormat = info.getAnnotation(FixedFormat.class);
        if (fixedFormat == null) this.reject();
    }

    @Override
    protected Map<String, Supplier<String>> getArguments(String value) {
        return CommonUtil.putToMap(super.getArguments(value),
                "{format}", () -> fixedFormat.format());
    }
}
