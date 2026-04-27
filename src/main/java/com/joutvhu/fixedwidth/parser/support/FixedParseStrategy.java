package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.DefaultParseError;
import com.joutvhu.fixedwidth.parser.ParseError;
import com.joutvhu.fixedwidth.parser.convert.hook.OptionalHook;
import com.joutvhu.fixedwidth.parser.exception.MandatoryValueException;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Fixed width string serialization and deserialization.
 *
 * <p>Phase 3 (unified-hook-system): all dispatch goes through
 * {@link FixedModule#invokeHooks(FixedTypeInfo, ParseContext)}.
 * The old {@code createReaderBy}/{@code createWriterBy}/{@code invokeAnnotationHandlers}
 * methods have been removed from {@link FixedModule}.
 *
 * <p>The strategy stores itself in the context under the key
 * {@link OptionalHook#STRATEGY_KEY} so that hooks that need recursive dispatch
 * (Optional, Collection, Map, Object) can call back into the strategy.
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public class FixedParseStrategy {

    private FixedModule module;
    private Map<String, Object> parserConfig = Collections.emptyMap();
    private Consumer<ParseContext> contextCreatedHook = null;
    private final java.util.Map<Phase, Consumer<ParseContext>> phaseHooks = new java.util.HashMap<>();
    private final ThreadLocal<DefaultParseContext> activeContext = new ThreadLocal<>();

    public FixedParseStrategy(FixedModule module) {
        this.module = module;
    }

    public FixedModule getModule() {
        return module;
    }

    public void setParserConfig(Map<String, Object> config) {
        this.parserConfig = config != null ? config : Collections.emptyMap();
    }

    public void setContextCreatedHook(Consumer<ParseContext> hook) {
        this.contextCreatedHook = hook;
    }

    public void setPhaseHook(Phase phase, Consumer<ParseContext> hook) {
        phaseHooks.put(phase, hook);
    }

    // ── Context management ────────────────────────────────────────────────────

    public DefaultParseContext createReadContext(Map<String, Object> sessionProps) {
        DefaultParseContext ctx = new DefaultParseContext(
            Phase.READ_PRE_CUT, parserConfig, sessionProps);
        // Store strategy reference so hooks can call back into read/write
        ctx.put(OptionalHook.STRATEGY_KEY, this);
        activeContext.set(ctx);
        if (contextCreatedHook != null) contextCreatedHook.accept(ctx);
        return ctx;
    }

    public DefaultParseContext createWriteContext(Map<String, Object> sessionProps) {
        DefaultParseContext ctx = new DefaultParseContext(
            Phase.WRITE_PRE_GET, parserConfig, sessionProps);
        // Store strategy reference so hooks can call back into read/write
        ctx.put(OptionalHook.STRATEGY_KEY, this);
        activeContext.set(ctx);
        if (contextCreatedHook != null) contextCreatedHook.accept(ctx);
        return ctx;
    }

    public DefaultParseContext getActiveContext() {
        return activeContext.get();
    }

    private void firePhaseHook(Phase phase) {
        DefaultParseContext ctx = activeContext.get();
        if (ctx == null) return;
        ctx.setPhase(phase);
        Consumer<ParseContext> hook = phaseHooks.get(phase);
        if (hook != null) hook.accept(ctx);
    }

    public void firePhaseHookPublic(Phase phase) {
        firePhaseHook(phase);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isNumber(FixedTypeInfo info) {
        Class<?> type = info.getType();
        return TypeConstants.INTEGER_NUMBER_TYPES.contains(type) ||
            TypeConstants.DECIMAL_NUMBER_TYPES.contains(type);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public Object read(FixedTypeInfo info, StringAssembler assembler) {
        FixedTypeInfo actualInfo = info.detectTypeWith(assembler);

        // Object types (those with @FixedObject and fields) manage their own OBJECT frame
        // via ObjectHook. Leaf fields get a FIELD frame here.
        boolean isObjectType = !actualInfo.getElementTypeInfo().isEmpty()
            || actualInfo.getFixedObject() != null;

        DefaultParseContext ctx = activeContext.get();
        String rawString = assembler.getValue();

        if (ctx != null && !isObjectType) {
            // Leaf fields: push a FIELD frame
            int depth = ctx.frameStack().size();
            DefaultContextFrame frame = new DefaultContextFrame(
                actualInfo, FrameType.FIELD, depth, -1,
                assembler, rawString, null, null);
            ctx.pushFrame(frame);
            ctx.setCurrentValue(null);
            firePhaseHook(Phase.READ_AFTER_CUT);
            invokeHooksSafe(actualInfo, ctx, rawString);
        } else if (ctx != null && isObjectType) {
            // Object types: store assembler in context so ObjectHook can access it
            ctx.put("__assembler__", assembler);
            ctx.setCurrentValue(null);
        }

        StringAssembler effectiveAssembler = assembler;

        try {
            // For collection types, don't short-circuit on blank
            boolean isCollectionType = Collection.class.isAssignableFrom(actualInfo.getType());
            if (effectiveAssembler.isBlank(actualInfo) && !isNumber(actualInfo) && !isCollectionType
                && !isObjectType) {
                if (actualInfo.require)
                    throw new MandatoryValueException(
                        actualInfo.buildMessage("{title} cannot be blank."));
                return null;
            }

            if (ctx != null && !isObjectType) {
                String validationValue = effectiveAssembler.getValue();
                if (actualInfo.getElementTypeInfo().isEmpty() && !actualInfo.getDefaultKeepPadding()) {
                    validationValue = FixedStringAssembler.of(validationValue).trim(actualInfo).getValue();
                }
                ctx.currentFrame().setProcessedString(validationValue);
                firePhaseHook(Phase.READ_AFTER_TRANSFORM);
                invokeHooksSafe(actualInfo, ctx, validationValue);

                // Hook may have modified processedString
                String processed = ctx.getProcessedString();
                if (processed != null && !processed.equals(validationValue)) {
                    effectiveAssembler = FixedStringAssembler.of(processed);
                }

                Object result = ctx.getCurrentValue();

                if (ctx.isSkipField()) {
                    return null;
                }

                if (result != null) {
                    // READ_AFTER_CONVERT
                    firePhaseHook(Phase.READ_AFTER_CONVERT);
                    invokeHooksSafe(actualInfo, ctx, null);
                    result = ctx.getCurrentValue();
                }

                if (result == null && actualInfo.require)
                    throw new MandatoryValueException(
                        actualInfo.buildMessage("{label} cannot be null."));
                return result;
            }

            // Object types: invoke hooks at READ_AFTER_TRANSFORM — ObjectHook handles the rest
            if (isObjectType && ctx != null) {
                // Return null when the entire input is blank (no meaningful data)
                if (effectiveAssembler.isBlank(actualInfo)) {
                    return null;
                }
                firePhaseHook(Phase.READ_AFTER_TRANSFORM);
                invokeHooksSafe(actualInfo, ctx, rawString);
                Object result = ctx.getCurrentValue();
                if (result == null && actualInfo.require)
                    throw new MandatoryValueException(
                        actualInfo.buildMessage("{label} cannot be null."));
                return result;
            }

            return null;

        } finally {
            if (ctx != null && !isObjectType) ctx.popFrame();
        }
    }

    /**
     * Invokes hooks; in collect-all mode, catches exceptions and records them.
     */
    private void invokeHooksSafe(FixedTypeInfo info, DefaultParseContext ctx, String rawValue) {
        if (ctx.isCollectErrors()) {
            try {
                module.invokeHooks(info, ctx);
            } catch (Exception e) {
                recordError(ctx, info, e, rawValue);
            }
        } else {
            module.invokeHooks(info, ctx);
        }
    }

    private String buildFieldPath(FixedTypeInfo info, DefaultParseContext ctx) {
        if (info.getField() == null) return info.getName();
        String className = info.getField().getDeclaringClass().getSimpleName();
        return className + "." + info.getField().getName();
    }

    private void recordError(DefaultParseContext ctx, FixedTypeInfo info,
                             Exception e, String rawValue) {
        String fieldPath = buildFieldPath(info, ctx);
        String raw = rawValue != null ? rawValue.replaceAll("\\s+$", "") : null;
        ParseError error = new DefaultParseError(
            e.getMessage(), ctx.getPhase(), fieldPath, raw, e);
        ctx.addError(error);
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    public String write(FixedTypeInfo info, Object value) {
        if (value == null) {
            if (info.require)
                throw new MandatoryValueException(info.buildMessage("{label} cannot be null."));
            return FixedStringAssembler.black(info).getValue();
        }

        FixedTypeInfo actualInfo = info.detectTypeWith(value);

        boolean isObjectType = !actualInfo.getElementTypeInfo().isEmpty()
            || actualInfo.getFixedObject() != null;

        DefaultParseContext ctx = activeContext.get();
        if (ctx != null && !isObjectType) {
            int depth = ctx.frameStack().size();
            DefaultContextFrame frame = new DefaultContextFrame(
                actualInfo, FrameType.FIELD, depth, -1,
                null, null, null, null);
            ctx.pushFrame(frame);
            ctx.setCurrentValue(value);
            firePhaseHook(Phase.WRITE_AFTER_GET);
            // Hooks at WRITE_AFTER_GET perform the actual conversion and
            // set ctx.setCurrentValue() with the serialized string
            module.invokeHooks(actualInfo, ctx);
            Object converted = ctx.getCurrentValue();

            if (ctx.isSkipField()) {
                ctx.popFrame();
                return FixedStringAssembler.black(actualInfo).getValue();
            }

            try {
                String result = converted instanceof String
                    ? (String) converted
                    : (converted != null ? converted.toString() : StringUtils.EMPTY);

                // WRITE_AFTER_CONVERT
                ctx.setCurrentValue(result);
                firePhaseHook(Phase.WRITE_AFTER_CONVERT);
                module.invokeHooks(actualInfo, ctx);
                result = (String) ctx.getCurrentValue();

                StringAssembler padded = FixedStringAssembler
                    .of(CommonUtil.defaultIfNull(result, StringUtils.EMPTY))
                    .pad(actualInfo);

                ctx.setCurrentValue(padded.getValue());
                firePhaseHook(Phase.WRITE_AFTER_TRANSFORM);
                module.invokeHooks(actualInfo, ctx);

                if (padded.isBlank(actualInfo)) {
                    if (actualInfo.require)
                        throw new MandatoryValueException(
                            actualInfo.buildMessage("{title} cannot be blank."));
                }
                return padded.getValue();

            } finally {
                ctx.popFrame();
            }
        }

        // Object types: invoke hooks at WRITE_AFTER_GET — ObjectHook handles the rest
        // Don't fire the user phase hook here; ObjectHook pushes the OBJECT frame first,
        // then strategy.write() fires WRITE_AFTER_GET for each leaf field with the frame on stack.
        if (isObjectType && ctx != null) {
            ctx.setCurrentValue(value);
            ctx.setPhase(Phase.WRITE_AFTER_GET);
            module.invokeHooks(actualInfo, ctx);
            Object result = ctx.getCurrentValue();
            return result instanceof String ? (String) result : StringUtils.EMPTY;
        }

        return StringUtils.EMPTY;
    }
}
