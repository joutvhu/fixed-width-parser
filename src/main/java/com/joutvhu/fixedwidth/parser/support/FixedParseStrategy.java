package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.convert.ValidationType;
import com.joutvhu.fixedwidth.parser.exception.FixedException;
import com.joutvhu.fixedwidth.parser.exception.MandatoryValueException;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Fixed width string serialization and deserialization.
 *
 * <p>Phase 1 adds {@link ParseContext} creation and propagation.
 * The context is created once per top-level call and threaded through
 * all nested read/write operations via a {@link ThreadLocal}.
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public class FixedParseStrategy implements ReadStrategy, WriteStrategy {

    private FixedModule module;

    // Parser-level config — shared across all calls on this strategy instance
    private Map<String, Object> parserConfig = Collections.emptyMap();

    // Optional hook for tests / Phase 2 handler dispatch
    private Consumer<ParseContext> contextCreatedHook = null;
    private final java.util.Map<Phase, Consumer<ParseContext>> phaseHooks = new java.util.HashMap<>();

    // Active context for the current call — ThreadLocal so strategy is thread-safe
    private final ThreadLocal<DefaultParseContext> activeContext = new ThreadLocal<>();

    public FixedParseStrategy(FixedModule module) {
        this.module = module;
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

    // ── Context management ───────────────────────────────────────────────────

    /**
     * Creates a new context for a top-level read call.
     */
    public DefaultParseContext createReadContext(Map<String, Object> sessionProps) {
        DefaultParseContext ctx = new DefaultParseContext(
                Phase.READ_PRE_CUT, parserConfig, sessionProps);
        activeContext.set(ctx);
        if (contextCreatedHook != null) contextCreatedHook.accept(ctx);
        return ctx;
    }

    /**
     * Creates a new context for a top-level write call.
     */
    public DefaultParseContext createWriteContext(Map<String, Object> sessionProps) {
        DefaultParseContext ctx = new DefaultParseContext(
                Phase.WRITE_PRE_GET, parserConfig, sessionProps);
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

    /** Public variant used by ObjectReader / ObjectWriter. */
    public void firePhaseHookPublic(Phase phase) {
        firePhaseHook(phase);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean isNumber(FixedTypeInfo info) {
        Class<?> type = info.getType();
        return TypeConstants.INTEGER_NUMBER_TYPES.contains(type) ||
                TypeConstants.DECIMAL_NUMBER_TYPES.contains(type);
    }

    private void validate(FixedTypeInfo info, String value, ValidationType type) {
        List<FixedWidthValidator> validators = module.createValidatorsBy(info, this);
        for (FixedWidthValidator validator : validators) {
            validator.validate(value, type);
        }
    }

    // ── ReadStrategy ─────────────────────────────────────────────────────────

    @Override
    public Object read(FixedTypeInfo info, StringAssembler assembler) {
        FixedTypeInfo actualInfo = info.detectTypeWith(assembler);

        // Only push a FIELD frame for leaf nodes (non-object types).
        // Object types get their frame pushed by ObjectReader itself,
        // which also has access to the child assemblers.
        boolean isObjectType = !actualInfo.getElementTypeInfo().isEmpty()
                || actualInfo.getFixedObject() != null;

        DefaultParseContext ctx = activeContext.get();
        if (ctx != null && !isObjectType) {
            // ── READ_AFTER_CUT: push field frame with raw string ──────────────
            // assembler is already a child assembler (sliced by ObjectReader),
            // so assembler.getValue() IS the raw substring for this field.
            String rawString = assembler.getValue();
            int depth = ctx.frameStack().size();
            DefaultContextFrame frame = new DefaultContextFrame(
                    actualInfo, FrameType.FIELD,
                    depth, -1,
                    assembler, rawString, null, null);
            ctx.pushFrame(frame);
            ctx.setCurrentValue(null);
            firePhaseHook(Phase.READ_AFTER_CUT);
        }

        try {
            if (assembler.isBlank(actualInfo) && !isNumber(actualInfo)) {
                if (actualInfo.require)
                    throw new MandatoryValueException(
                            actualInfo.buildMessage("{title} cannot be blank."));
                return null;
            }

            // ── READ_AFTER_TRANSFORM ──────────────────────────────────────────
            String validationValue = assembler.getValue();
            if (actualInfo.getElementTypeInfo().isEmpty() && !actualInfo.getDefaultKeepPadding()) {
                validationValue = FixedStringAssembler.of(validationValue).trim(actualInfo).getValue();
            }
            if (ctx != null && !isObjectType) {
                ctx.currentFrame().setProcessedString(validationValue);
                firePhaseHook(Phase.READ_AFTER_TRANSFORM);
            }
            validate(actualInfo, validationValue, ValidationType.BEFORE_READ);

            // ── Reader ────────────────────────────────────────────────────────
            FixedWidthReader<Object> reader = module.createReaderBy(actualInfo, this);
            if (reader != null) {
                Object result = reader.read(assembler);

                // ── READ_AFTER_CONVERT ────────────────────────────────────────
                if (ctx != null && !isObjectType) {
                    ctx.setCurrentValue(result);
                    firePhaseHook(Phase.READ_AFTER_CONVERT);
                    result = ctx.getCurrentValue();
                }

                if (result == null && actualInfo.require)
                    throw new MandatoryValueException(
                            actualInfo.buildMessage("{label} cannot be null."));
                return result;
            }
            throw new FixedException("Reader not found.");

        } finally {
            if (ctx != null && !isObjectType) ctx.popFrame();
        }
    }

    // ── WriteStrategy ────────────────────────────────────────────────────────

    @Override
    public String write(FixedTypeInfo info, Object value) {
        if (value == null) {
            if (info.require)
                throw new MandatoryValueException(info.buildMessage("{label} cannot be null."));
            return FixedStringAssembler.black(info).getValue();
        }

        FixedTypeInfo actualInfo = info.detectTypeWith(value);

        // Only push a FIELD frame for leaf nodes — ObjectWriter handles object frames.
        boolean isObjectType = !actualInfo.getElementTypeInfo().isEmpty()
                || actualInfo.getFixedObject() != null;

        DefaultParseContext ctx = activeContext.get();
        if (ctx != null && !isObjectType) {
            int depth = ctx.frameStack().size();
            DefaultContextFrame frame = new DefaultContextFrame(
                    actualInfo, FrameType.FIELD,
                    depth, -1,
                    null, null, null, null);
            ctx.pushFrame(frame);
            ctx.setCurrentValue(value);
            firePhaseHook(Phase.WRITE_AFTER_GET);
            value = ctx.getCurrentValue();
        }

        try {
            FixedWidthWriter<Object> writer = module.createWriterBy(actualInfo, this);
            if (writer != null) {
                Object finalValue = value;
                String result = writer.write(finalValue);

                if (ctx != null && !isObjectType) {
                    ctx.setCurrentValue(result);
                    firePhaseHook(Phase.WRITE_AFTER_CONVERT);
                    result = (String) ctx.getCurrentValue();
                }

                StringAssembler padded = FixedStringAssembler
                        .of(CommonUtil.defaultIfNull(result, StringUtils.EMPTY))
                        .pad(actualInfo);

                if (ctx != null && !isObjectType) {
                    ctx.setCurrentValue(padded.getValue());
                    firePhaseHook(Phase.WRITE_AFTER_TRANSFORM);
                }

                if (padded.isBlank(actualInfo)) {
                    if (actualInfo.require)
                        throw new MandatoryValueException(
                                actualInfo.buildMessage("{title} cannot be blank."));
                } else {
                    validate(actualInfo, padded.getValue(), ValidationType.AFTER_WRITE);
                }
                return padded.getValue();
            }
            throw new FixedException("Writer not found.");

        } finally {
            if (ctx != null && !isObjectType) ctx.popFrame();
        }
    }
}
