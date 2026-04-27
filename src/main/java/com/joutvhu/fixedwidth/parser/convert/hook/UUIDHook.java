package com.joutvhu.fixedwidth.parser.convert.hook;

import com.joutvhu.fixedwidth.parser.convert.ModuleHook;
import com.joutvhu.fixedwidth.parser.support.FixedStringAssembler;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Handles {@link UUID} fields.
 *
 * <p>READ at {@link Phase#READ_AFTER_TRANSFORM}: parses string to UUID.
 * WRITE at {@link Phase#WRITE_AFTER_GET}: converts UUID to string.
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public class UUIDHook implements ModuleHook {

    @Override
    public boolean supports(FixedTypeInfo info) {
        return UUID.class.equals(info.getType());
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
        String processedString = ctx.getProcessedString();
        String value = processedString != null
            ? FixedStringAssembler.of(processedString).trim(info).getValue()
            : null;
        if (CommonUtil.isBlank(value)) {
            ctx.setCurrentValue(null);
        } else {
            ctx.setCurrentValue(UUID.fromString(value));
        }
    }

    private void handleWrite(FixedTypeInfo info, ParseContext ctx) {
        Object value = ctx.getCurrentValue();
        ctx.setCurrentValue(value != null ? value.toString() : StringUtils.EMPTY);
    }
}
