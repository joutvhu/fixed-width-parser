package com.joutvhu.fixedwidth.parser.convert.hook;

import com.joutvhu.fixedwidth.parser.annotation.FixedCount;
import com.joutvhu.fixedwidth.parser.convert.ModuleHook;
import com.joutvhu.fixedwidth.parser.support.DefaultContextFrame;
import com.joutvhu.fixedwidth.parser.support.DefaultFixedStringBuilder;
import com.joutvhu.fixedwidth.parser.support.DefaultParseContext;
import com.joutvhu.fixedwidth.parser.support.FieldDependencyResolver;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedStringAssembler;
import com.joutvhu.fixedwidth.parser.support.FixedStringBuilder;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.FrameType;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.FixedHelper;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Handles {@code @FixedObject}-annotated types.
 *
 * <p>Must be registered last in {@code DefaultModule} because it matches any type
 * with {@code @FixedObject}, which would otherwise shadow more specific hooks.
 *
 * <p>READ at {@link Phase#READ_AFTER_TRANSFORM}: pushes OBJECT frame, iterates fields
 * in dependency order, delegates each field to the strategy, fires READ_AFTER_OBJECT.
 *
 * <p>WRITE at {@link Phase#WRITE_AFTER_GET}: pushes OBJECT frame with builder, iterates
 * fields in dependency order, delegates each field to the strategy, fires WRITE_AFTER_OBJECT.
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public class ObjectHook implements ModuleHook {

    /** Context key used to pass the assembler to ObjectHook for read operations. */
    public static final String ASSEMBLER_KEY = "__assembler__";

    @Override
    public boolean supports(FixedTypeInfo info) {
        return info.getFixedObject() != null;
    }

    @Override
    public Set<Phase> getSupportedPhases() {
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

    // ── READ ──────────────────────────────────────────────────────────────────

    private void handleRead(FixedTypeInfo info, ParseContext ctx) {
        FixedParseStrategy strategy = ctx.get(OptionalHook.STRATEGY_KEY, FixedParseStrategy.class);
        if (strategy == null) {
            ctx.setCurrentValue(null);
            return;
        }

        Object result = FixedHelper.newInstanceOf(info.getType());

        // Get assembler from context (stored by FixedParseStrategy before calling invokeHooks)
        StringAssembler assembler = ctx.get(ASSEMBLER_KEY, StringAssembler.class);
        if (assembler == null) {
            // Fallback: try processedString
            String processedString = ctx.getProcessedString();
            assembler = processedString != null
                ? FixedStringAssembler.of(processedString)
                : FixedStringAssembler.of(StringUtils.EMPTY);
        }

        DefaultParseContext dCtx = strategy.getActiveContext();
        DefaultContextFrame objectFrame = null;
        if (dCtx != null) {
            objectFrame = new DefaultContextFrame(
                info, FrameType.OBJECT,
                dCtx.frameStack().size(), -1,
                assembler, assembler.getValue(), result, null);
            dCtx.pushFrame(objectFrame);
        }

        try {
            List<FixedTypeInfo> children = FieldDependencyResolver.sort(info.getElementTypeInfo());

            for (FixedTypeInfo fieldInfo : children) {
                Field field = fieldInfo.getField();
                if (field == null) continue;

                boolean skip = false;
                if (dCtx != null) {
                    dCtx.resetSkipField();
                    DefaultContextFrame preFrame = new DefaultContextFrame(
                        fieldInfo, FrameType.FIELD,
                        dCtx.frameStack().size(), -1,
                        assembler, null, null, null);
                    dCtx.pushFrame(preFrame);
                    dCtx.setCurrentValue(null);
                    strategy.firePhaseHookPublic(Phase.READ_PRE_CUT);
                    strategy.getModule().invokeHooks(fieldInfo, dCtx);
                    skip = dCtx.isSkipField();
                    dCtx.popFrame();
                    dCtx.resetSkipField();
                }

                if (skip) continue;

                StringAssembler childAssembler = assembler.child(fieldInfo);
                Object v = strategy.read(fieldInfo, childAssembler);

                ReflectionUtil.makeAccessible(field);
                ReflectionUtil.setField(field, result, v);

                if (objectFrame != null) {
                    objectFrame.setPartialResult(result);
                }
            }
        } finally {
            if (dCtx != null) {
                strategy.firePhaseHookPublic(Phase.READ_AFTER_OBJECT);
                dCtx.popFrame();
            }
        }

        ctx.setCurrentValue(result);
    }

    // ── WRITE ─────────────────────────────────────────────────────────────────

    private void handleWrite(FixedTypeInfo info, ParseContext ctx) {
        FixedParseStrategy strategy = ctx.get(OptionalHook.STRATEGY_KEY, FixedParseStrategy.class);
        if (strategy == null) {
            ctx.setCurrentValue(StringUtils.EMPTY);
            return;
        }

        Object value = ctx.getCurrentValue();
        DefaultParseContext dCtx = strategy.getActiveContext();
        DefaultContextFrame objectFrame = null;
        FixedStringBuilder builder = new DefaultFixedStringBuilder();

        if (dCtx != null) {
            objectFrame = new DefaultContextFrame(
                info, FrameType.OBJECT,
                dCtx.frameStack().size(), -1,
                null, null, value, builder);
            dCtx.pushFrame(objectFrame);
        }

        try {
            StringAssembler assembler = FixedStringAssembler.instance();
            List<FixedTypeInfo> children = FieldDependencyResolver.sort(
                CommonUtil.defaultIfNull(info.getElementTypeInfo(), new ArrayList<>()));

            for (FixedTypeInfo fieldInfo : children) {
                if (fieldInfo.getField() == null) continue;

                boolean skip = false;
                if (dCtx != null) {
                    dCtx.resetSkipField();
                    DefaultContextFrame preFrame = new DefaultContextFrame(
                        fieldInfo, FrameType.FIELD,
                        dCtx.frameStack().size(), -1,
                        null, null, null, null);
                    dCtx.pushFrame(preFrame);
                    dCtx.setCurrentValue(null);
                    strategy.firePhaseHookPublic(Phase.WRITE_PRE_GET);
                    strategy.getModule().invokeHooks(fieldInfo, dCtx);
                    skip = dCtx.isSkipField();
                    Object injectedValue = dCtx.getCurrentValue();
                    dCtx.popFrame();
                    dCtx.resetSkipField();

                    if (!skip && injectedValue != null) {
                        String written = strategy.write(fieldInfo, injectedValue);
                        assembler.set(fieldInfo, written);
                        builder.addPart(fieldInfo.getName(), fieldInfo, written);
                        continue;
                    }
                }

                if (skip) continue;

                ReflectionUtil.makeAccessible(fieldInfo.getField());
                Object item = ReflectionUtil.getField(fieldInfo.getField(), value);
                String written = strategy.write(fieldInfo, item);
                assembler.set(fieldInfo, written);
                builder.addPart(fieldInfo.getName(), fieldInfo, written);
            }

            // Re-write count fields updated by @FixedCount(field)
            for (FixedTypeInfo fieldInfo : children) {
                if (fieldInfo.getField() == null) continue;
                boolean isCountTarget = children.stream().anyMatch(f -> {
                    if (f.getField() == null) return false;
                    FixedCount fc = f.getField().getAnnotation(FixedCount.class);
                    return fc != null && fieldInfo.getName().equals(fc.field());
                });
                if (!isCountTarget) continue;

                ReflectionUtil.makeAccessible(fieldInfo.getField());
                Object currentVal = ReflectionUtil.getField(fieldInfo.getField(), value);
                if (currentVal == null) continue;
                String currentWritten = strategy.write(fieldInfo, currentVal);
                String previousWritten = builder.getPart(fieldInfo.getName());
                if (previousWritten != null && !currentWritten.equals(previousWritten)) {
                    assembler.set(fieldInfo, currentWritten);
                    builder.replacePart(fieldInfo.getName(), currentWritten);
                }
            }

            ctx.setCurrentValue(assembler.getValue());

        } finally {
            if (dCtx != null) {
                strategy.firePhaseHookPublic(Phase.WRITE_AFTER_OBJECT);
                dCtx.popFrame();
            }
        }
    }
}
