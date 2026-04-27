package com.joutvhu.fixedwidth.parser.convert.hook;

import com.joutvhu.fixedwidth.parser.convert.ModuleHook;
import com.joutvhu.fixedwidth.parser.exception.FixedParserException;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.Set;

/**
 * Handles {@code String}, {@code char}, and {@code Character} fields.
 *
 * <p>READ at {@link Phase#READ_AFTER_TRANSFORM}: trims and sets the value.
 * WRITE at {@link Phase#WRITE_AFTER_GET}: converts to string.
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public class StringHook implements ModuleHook {

    @Override
    public boolean supports(FixedTypeInfo info) {
        return TypeConstants.STRING_TYPES.contains(info.getType());
    }

    @Override
    public Set<Phase> phases() {
        return EnumSet.of(Phase.READ_AFTER_TRANSFORM, Phase.WRITE_AFTER_GET);
    }

    @Override
    public void handle(FixedTypeInfo info, ParseContext ctx) {
        if (ctx.getPhase().isRead()) {
            handleRead(info, ctx);
        } else {
            handleWrite(info, ctx);
        }
    }

    private void handleRead(FixedTypeInfo info, ParseContext ctx) {
        String value = ctx.getProcessedString();
        if (value == null) value = StringUtils.EMPTY;

        if (!String.class.equals(info.getType())) {
            // char / Character
            if (info.getLength() != 1)
                throw new FixedParserException(
                    info.buildMessage("Type of {label} is char then its length must be 1."));
            ctx.setCurrentValue(CommonUtil.isNotBlank(value) ? value.charAt(0) : null);
        } else {
            ctx.setCurrentValue(value);
        }
    }

    private void handleWrite(FixedTypeInfo info, ParseContext ctx) {
        Object value = ctx.getCurrentValue();
        String result = StringUtils.EMPTY;
        if (value != null) result += value;
        ctx.setCurrentValue(result);
    }
}
