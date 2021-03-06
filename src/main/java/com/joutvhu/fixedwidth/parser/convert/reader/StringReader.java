package com.joutvhu.fixedwidth.parser.convert.reader;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.exception.ParserException;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ReadStrategy;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public class StringReader extends FixedWidthReader<Object> {
    public StringReader(FixedTypeInfo info, ReadStrategy strategy) {
        super(info, strategy);
        if (!TypeConstants.STRING_TYPES.contains(info.getType()))
            this.reject();
    }

    @Override
    public Object read(StringAssembler assembler) {
        String value = assembler.getValue();
        if (!String.class.equals(info.getType())) {
            if (info.getLength() != 1)
                throw new ParserException(info.buildMessage("Type of {label} is char then its length must be 1."));
            return CommonUtil.isNotBlank(value) ? value.charAt(0) : null;
        }
        return value;
    }
}
