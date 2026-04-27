package com.joutvhu.fixedwidth.parser.convert.hook;

import com.joutvhu.fixedwidth.parser.convert.ModuleHook;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedStringAssembler;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Handles {@link Optional} fields.
 *
 * <p>READ at {@link Phase#READ_AFTER_TRANSFORM}: parses inner value and wraps in Optional.
 * WRITE at {@link Phase#WRITE_AFTER_GET}: unwraps Optional and delegates to inner type.
 *
 * <p>Requires the {@link FixedParseStrategy} to be stored in the context under the key
 * {@code "__strategy__"} (set by {@code FixedParseStrategy.createReadContext/createWriteContext}).
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public class OptionalHook implements ModuleHook {

    /**
     * Context key used to store the FixedParseStrategy reference.
     */
    public static final String STRATEGY_KEY = "__strategy__";

    @Override
    public boolean supports(FixedTypeInfo info) {
        return Optional.class.equals(info.getType());
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

    private FixedTypeInfo resolveValueInfo(FixedTypeInfo info) {
        ParameterizedType pt = info.getParameterizedType();
        if (pt != null && pt.getActualTypeArguments().length > 0) {
            Type innerType = pt.getActualTypeArguments()[0];
            if (innerType instanceof Class) {
                return info.ofType((Class<?>) innerType);
            } else if (innerType instanceof ParameterizedType) {
                return info.ofType((Class<?>) ((ParameterizedType) innerType).getRawType());
            }
        }
        return null;
    }

    private void handleRead(FixedTypeInfo info, ParseContext ctx) {
        FixedTypeInfo valueInfo = resolveValueInfo(info);
        if (valueInfo == null) {
            ctx.setCurrentValue(Optional.empty());
            return;
        }

        String processedString = ctx.getProcessedString();
        StringAssembler assembler = processedString != null
            ? FixedStringAssembler.of(processedString)
            : FixedStringAssembler.of(StringUtils.EMPTY);

        if (assembler.isBlank(info)) {
            ctx.setCurrentValue(Optional.empty());
            return;
        }

        FixedParseStrategy strategy = ctx.get(STRATEGY_KEY, FixedParseStrategy.class);
        if (strategy != null) {
            Object result = strategy.read(valueInfo, assembler);
            ctx.setCurrentValue(Optional.ofNullable(result));
        } else {
            ctx.setCurrentValue(Optional.empty());
        }
    }

    private void handleWrite(FixedTypeInfo info, ParseContext ctx) {
        Object rawValue = ctx.getCurrentValue();
        if (rawValue == null) {
            ctx.setCurrentValue(StringUtils.EMPTY);
            return;
        }
        Optional<?> optional = (Optional<?>) rawValue;
        if (!optional.isPresent()) {
            ctx.setCurrentValue(StringUtils.EMPTY);
            return;
        }

        FixedTypeInfo valueInfo = resolveValueInfo(info);
        if (valueInfo == null) {
            ctx.setCurrentValue(StringUtils.EMPTY);
            return;
        }

        FixedParseStrategy strategy = ctx.get(STRATEGY_KEY, FixedParseStrategy.class);
        if (strategy != null) {
            String result = strategy.write(valueInfo, optional.get());
            ctx.setCurrentValue(result);
        } else {
            ctx.setCurrentValue(optional.get().toString());
        }
    }
}
