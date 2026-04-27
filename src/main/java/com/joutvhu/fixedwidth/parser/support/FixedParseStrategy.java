package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.DefaultParseError;
import com.joutvhu.fixedwidth.parser.ParseError;
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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Fixed width string serialization and deserialization.
 *
 * <p>Phase 1: {@link ParseContext} creation and propagation via ThreadLocal.
 * <p>Phase 2: {@link FixedModule#invokeAnnotationHandlers} called at each phase.
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public class FixedParseStrategy implements ReadStrategy, WriteStrategy {

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
        activeContext.set(ctx);
        if (contextCreatedHook != null) contextCreatedHook.accept(ctx);
        return ctx;
    }

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

    public void firePhaseHookPublic(Phase phase) {
        firePhaseHook(phase);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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

    // ── ReadStrategy ──────────────────────────────────────────────────────────

    @Override
    public Object read(FixedTypeInfo info, StringAssembler assembler) {
        FixedTypeInfo actualInfo = info.detectTypeWith(assembler);

        // Only push a FIELD frame for leaf nodes.
        // Object types get their frame pushed by ObjectReader.
        boolean isObjectType = !actualInfo.getElementTypeInfo().isEmpty()
                || actualInfo.getFixedObject() != null;

        DefaultParseContext ctx = activeContext.get();
        if (ctx != null && !isObjectType) {
            String rawString = assembler.getValue();
            int depth = ctx.frameStack().size();
            DefaultContextFrame frame = new DefaultContextFrame(
                    actualInfo, FrameType.FIELD, depth, -1,
                    assembler, rawString, null, null);
            ctx.pushFrame(frame);
            ctx.setCurrentValue(null);
            firePhaseHook(Phase.READ_AFTER_CUT);
            module.invokeAnnotationHandlers(actualInfo, ctx);
        }

        // effectiveAssembler may be replaced if a handler modifies processedString
        StringAssembler effectiveAssembler = assembler;

        try {
            // For collection types, don't short-circuit on blank — let the reader
            // handle it (e.g. return empty list when count=0)
            boolean isCollectionType = Collection.class.isAssignableFrom(actualInfo.getType());
            if (effectiveAssembler.isBlank(actualInfo) && !isNumber(actualInfo) && !isCollectionType) {
                if (actualInfo.require)
                    throw new MandatoryValueException(
                            actualInfo.buildMessage("{title} cannot be blank."));
                return null;
            }

            // ── READ_AFTER_TRANSFORM ──────────────────────────────────────────
            String validationValue = effectiveAssembler.getValue();
            if (actualInfo.getElementTypeInfo().isEmpty() && !actualInfo.getDefaultKeepPadding()) {
                validationValue = FixedStringAssembler.of(validationValue).trim(actualInfo).getValue();
            }
            if (ctx != null && !isObjectType) {
                ctx.currentFrame().setProcessedString(validationValue);
                firePhaseHook(Phase.READ_AFTER_TRANSFORM);
                // Invoke handlers — in collect mode, catch and record errors
                invokeHandlersSafe(actualInfo, ctx, validationValue);
                // Handler may have modified processedString — propagate to assembler
                String processed = ctx.getProcessedString();
                if (processed != null && !processed.equals(validationValue)) {
                    validationValue = processed;
                    effectiveAssembler = FixedStringAssembler.of(processed);
                }
            }

            // Validate — in collect mode, catch and record errors
            final String finalValidationValue = validationValue;
            if (ctx != null && ctx.isCollectErrors()) {
                try {
                    validate(actualInfo, finalValidationValue, ValidationType.BEFORE_READ);
                } catch (Exception e) {
                    recordError(ctx, actualInfo, e, assembler.getValue());
                    return null; // field failed — return null and continue
                }
            } else {
                validate(actualInfo, validationValue, ValidationType.BEFORE_READ);
            }

            // ── Reader ────────────────────────────────────────────────────────
            FixedWidthReader<Object> reader = module.createReaderBy(actualInfo, this);
            if (reader != null) {
                Object result;
                try {
                    result = reader.read(effectiveAssembler);
                } catch (Exception e) {
                    if (ctx != null && ctx.isCollectErrors()) {
                        recordError(ctx, actualInfo, e, assembler.getValue());
                        return null;
                    }
                    throw e;
                }

                if (ctx != null && !isObjectType) {
                    ctx.setCurrentValue(result);
                    firePhaseHook(Phase.READ_AFTER_CONVERT);
                    module.invokeAnnotationHandlers(actualInfo, ctx);
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

    /**
     * Invokes annotation handlers; in collect-all mode, catches exceptions and
     * records them as errors instead of propagating.
     */
    private void invokeHandlersSafe(FixedTypeInfo info, DefaultParseContext ctx,
                                    String rawValue) {
        if (ctx.isCollectErrors()) {
            try {
                module.invokeAnnotationHandlers(info, ctx);
            } catch (Exception e) {
                recordError(ctx, info, e, rawValue);
            }
        } else {
            module.invokeAnnotationHandlers(info, ctx);
        }
    }

    /**
     * Builds a field path string like {@code "ClassName.fieldName"} from the
     * current context frame stack.
     */
    private String buildFieldPath(FixedTypeInfo info, DefaultParseContext ctx) {
        if (info.getField() == null) return info.getName();
        String className = info.getField().getDeclaringClass().getSimpleName();
        return className + "." + info.getField().getName();
    }

    /** Records an error into the context's error list. */
    private void recordError(DefaultParseContext ctx, FixedTypeInfo info,
                             Exception e, String rawValue) {
        String fieldPath = buildFieldPath(info, ctx);
        String raw = rawValue != null ? rawValue.replaceAll("\\s+$", "") : null;
        ParseError error = new DefaultParseError(
                e.getMessage(), ctx.getPhase(), fieldPath, raw, e);
        ctx.addError(error);
    }

    // ── WriteStrategy ─────────────────────────────────────────────────────────

    @Override
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
        Object effectiveValue = value;
        if (ctx != null && !isObjectType) {
            int depth = ctx.frameStack().size();
            DefaultContextFrame frame = new DefaultContextFrame(
                    actualInfo, FrameType.FIELD, depth, -1,
                    null, null, null, null);
            ctx.pushFrame(frame);
            ctx.setCurrentValue(effectiveValue);
            firePhaseHook(Phase.WRITE_AFTER_GET);
            module.invokeAnnotationHandlers(actualInfo, ctx);
            effectiveValue = ctx.getCurrentValue();
        }

        try {
            FixedWidthWriter<Object> writer = module.createWriterBy(actualInfo, this);
            if (writer != null) {
                String result = writer.write(effectiveValue);

                if (ctx != null && !isObjectType) {
                    ctx.setCurrentValue(result);
                    firePhaseHook(Phase.WRITE_AFTER_CONVERT);
                    module.invokeAnnotationHandlers(actualInfo, ctx);
                    result = (String) ctx.getCurrentValue();
                }

                StringAssembler padded = FixedStringAssembler
                        .of(CommonUtil.defaultIfNull(result, StringUtils.EMPTY))
                        .pad(actualInfo);

                if (ctx != null && !isObjectType) {
                    ctx.setCurrentValue(padded.getValue());
                    firePhaseHook(Phase.WRITE_AFTER_TRANSFORM);
                    module.invokeAnnotationHandlers(actualInfo, ctx);
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
