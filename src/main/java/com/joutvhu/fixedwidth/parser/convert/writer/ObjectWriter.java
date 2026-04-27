package com.joutvhu.fixedwidth.parser.convert.writer;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.support.DefaultContextFrame;
import com.joutvhu.fixedwidth.parser.support.DefaultFixedStringBuilder;
import com.joutvhu.fixedwidth.parser.support.DefaultParseContext;
import com.joutvhu.fixedwidth.parser.support.FieldDependencyResolver;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedStringAssembler;
import com.joutvhu.fixedwidth.parser.support.FixedStringBuilder;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.FrameType;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.support.WriteStrategy;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Writes a Java object to a fixed-width string by iterating over its fields.
 *
 * <p>Phase 1: pushes an OBJECT-level {@link com.joutvhu.fixedwidth.parser.support.ContextFrame}
 * with a {@link FixedStringBuilder} so that handlers can inspect already-written
 * parts before the final output is assembled.
 *
 * <p>Phase 4: fields are processed in dependency order (topological sort).
 * Fields annotated with {@code @FixedConditional} are skipped (written as blank)
 * when their condition is not met.
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public class ObjectWriter extends FixedWidthWriter<Object> {

    public ObjectWriter(FixedTypeInfo info, WriteStrategy strategy) {
        super(info, strategy);
        if (info.getFixedObject() == null) this.reject();
    }

    @Override
    public String write(Object value) {
        // Push an OBJECT frame with a builder for this object
        DefaultParseContext ctx = getActiveContext();
        DefaultContextFrame objectFrame = null;
        FixedStringBuilder builder = new DefaultFixedStringBuilder();

        if (ctx != null) {
            objectFrame = new DefaultContextFrame(
                    info, FrameType.OBJECT,
                    ctx.frameStack().size(), -1,
                    null, null, value, builder);
            ctx.pushFrame(objectFrame);
        }

        try {
            StringAssembler assembler = FixedStringAssembler.instance();

            // Phase 4: sort fields in dependency order (detects circular deps)
            List<FixedTypeInfo> children = FieldDependencyResolver.sort(
                    CommonUtil.defaultIfNull(info.getElementTypeInfo(), new ArrayList<>()));

            for (FixedTypeInfo fieldInfo : children) {
                if (fieldInfo.getField() == null) continue;

                // ── WRITE_PRE_GET: fire before reading field value, check skip ─
                boolean skip = false;
                if (ctx != null) {
                    ctx.resetSkipField();
                    // Push a temporary frame so handlers can access parentFrame()
                    DefaultContextFrame preFrame = new DefaultContextFrame(
                            fieldInfo, FrameType.FIELD,
                            ctx.frameStack().size(), -1,
                            null, null, null, null);
                    ctx.pushFrame(preFrame);
                    ctx.setCurrentValue(null);
                    firePhase(Phase.WRITE_PRE_GET);
                    invokeHandlers(fieldInfo, ctx);
                    skip = ctx.isSkipField();
                    // If a handler set a value (e.g. ChecksumHandler), capture it
                    Object injectedValue = ctx.getCurrentValue();
                    ctx.popFrame();
                    ctx.resetSkipField();

                    if (!skip && injectedValue != null) {
                        // Handler computed the value — use it directly
                        String written = write(fieldInfo, injectedValue);
                        assembler.set(fieldInfo, written);
                        builder.addPart(fieldInfo.getName(), fieldInfo, written);
                        continue;
                    }
                }

                if (skip) {
                    // Skipped conditional fields are simply omitted from output
                    // (do NOT write blank — another field may occupy the same position)
                    continue;
                }

                ReflectionUtil.makeAccessible(fieldInfo.getField());
                Object item = ReflectionUtil.getField(fieldInfo.getField(), value);
                String written = write(fieldInfo, item);
                assembler.set(fieldInfo, written);

                // Record in the builder for handler access
                builder.addPart(fieldInfo.getName(), fieldInfo, written);
            }
            return assembler.getValue();

        } finally {
            if (ctx != null) {
                firePhase(Phase.WRITE_AFTER_OBJECT);
                ctx.popFrame();
            }
        }
    }

    private void firePhase(Phase phase) {
        if (strategy instanceof FixedParseStrategy) {
            ((FixedParseStrategy) strategy).firePhaseHookPublic(phase);
        }
    }

    private void invokeHandlers(FixedTypeInfo fieldInfo, DefaultParseContext ctx) {
        if (strategy instanceof FixedParseStrategy) {
            FixedParseStrategy fps = (FixedParseStrategy) strategy;
            fps.getModule().invokeAnnotationHandlers(fieldInfo, ctx);
        }
    }

    /** Returns the active context if the strategy supports it, otherwise null. */
    private DefaultParseContext getActiveContext() {
        if (strategy instanceof FixedParseStrategy) {
            return ((FixedParseStrategy) strategy).getActiveContext();
        }
        return null;
    }
}
