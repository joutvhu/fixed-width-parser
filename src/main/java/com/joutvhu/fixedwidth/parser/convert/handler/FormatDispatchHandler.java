package com.joutvhu.fixedwidth.parser.convert.handler;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.Hook;
import com.joutvhu.fixedwidth.parser.convert.general.NumberHelper;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Dispatches {@link FixedFormat} validation to the appropriate type-specific handler:
 * {@link DateHandler} for date types, {@link BooleanHandler} for boolean types,
 * and {@link NumberHandler} for numeric types.
 *
 * <p>This is the single handler declared on {@code @FixedFormat} via
 * {@code @FixedHandler(FormatDispatchHandler.class)}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class FormatDispatchHandler implements Hook, NumberHelper {

    private boolean isDecimal;

    @Override
    public Set<Phase> getSupportedPhases() {
        return new HashSet<>(Arrays.asList(
            Phase.READ_AFTER_TRANSFORM,
            Phase.WRITE_AFTER_TRANSFORM));
    }

    @Override
    public void handle(FixedTypeInfo info, ParseContext ctx) {
        if (TypeConstants.DATE_TYPES.contains(info.getType())) {
            new DateHandler().handle(info, ctx);
        } else if (TypeConstants.BOOLEAN_TYPES.contains(info.getType())) {
            // BooleanHandler only runs on READ
            if (ctx.getPhase().isRead()) {
                new BooleanHandler().handle(info, ctx);
            }
        } else if (isNumeric(info)) {
            // NumberHandler only runs on READ
            if (ctx.getPhase().isRead()) {
                new NumberHandler().handle(info, ctx);
            }
        }
    }

    @Override
    public void setIsDecimal(boolean isDecimal) {
        this.isDecimal = isDecimal;
    }
}
