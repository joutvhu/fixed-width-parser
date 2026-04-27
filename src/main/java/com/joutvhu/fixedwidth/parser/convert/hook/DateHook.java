package com.joutvhu.fixedwidth.parser.convert.hook;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.ModuleHook;
import com.joutvhu.fixedwidth.parser.support.FixedStringAssembler;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.ObjectUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;

import java.util.EnumSet;
import java.util.Set;

/**
 * Handles date/time fields ({@code Date}, {@code LocalDate}, {@code LocalDateTime}, etc.).
 *
 * <p>READ at {@link Phase#READ_AFTER_TRANSFORM}: parses string to date.
 * WRITE at {@link Phase#WRITE_AFTER_GET}: formats date to string.
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public class DateHook implements ModuleHook {

    @Override
    public boolean supports(FixedTypeInfo info) {
        return TypeConstants.DATE_TYPES.contains(info.getType());
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
        Class<?> type = info.getType();
        String processedString = ctx.getProcessedString();
        String value = processedString != null
            ? FixedStringAssembler.of(processedString).trim(info).getValue()
            : null;
        String format = info.getAnnotationValue(FixedFormat.class, "format", String.class);

        if (CommonUtil.isNotBlank(value)) {
            ctx.setCurrentValue(ObjectUtil.parseDate(value, type, format));
        } else {
            ctx.setCurrentValue(null);
        }
    }

    private void handleWrite(FixedTypeInfo info, ParseContext ctx) {
        Object value = ctx.getCurrentValue();
        if (value == null) {
            ctx.setCurrentValue("");
            return;
        }
        String format = info.getAnnotationValue(FixedFormat.class, "format", String.class);
        ctx.setCurrentValue(ObjectUtil.formatDate(value, format));
    }
}
