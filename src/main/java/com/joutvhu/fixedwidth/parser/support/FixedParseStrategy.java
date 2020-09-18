package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.exception.FixedException;
import com.joutvhu.fixedwidth.parser.handler.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.handler.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;

public class FixedParseStrategy {
    private FixedModule module;

    public FixedParseStrategy(FixedModule module) {
        this.module = module;
    }

    public Object read(FixedTypeInfo info, StringAssembler assembler) {
        info.detectTypeWith(assembler);
        FixedWidthReader reader = module.createReaderBy(info, this);
        if (reader != null) return reader.read(assembler);
        throw new FixedException("Reader not found.");
    }

    public String write(FixedTypeInfo info, Object value) {
        if (value == null)
            return StringAssembler.instance().black(info).getValue();

        FixedWidthWriter writer = module.createWriterBy(info, this);
        if (writer != null) {
            String result = writer.write(value);
            return StringAssembler
                    .of(CommonUtil.defaultIfNull(result, StringUtils.EMPTY))
                    .pad(info)
                    .getValue();
        }
        throw new FixedException("Writer not found.");
    }
}
