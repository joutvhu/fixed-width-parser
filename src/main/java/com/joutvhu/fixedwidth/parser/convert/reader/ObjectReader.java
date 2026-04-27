package com.joutvhu.fixedwidth.parser.convert.reader;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.DefaultContextFrame;
import com.joutvhu.fixedwidth.parser.support.DefaultParseContext;
import com.joutvhu.fixedwidth.parser.support.FieldDependencyResolver;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.FrameType;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.support.ReadStrategy;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.FixedHelper;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Reads a fixed-width string into a Java object by iterating over its fields.
 *
 * <p>Phase 1: pushes an OBJECT-level {@link com.joutvhu.fixedwidth.parser.support.ContextFrame}
 * so that child field frames have the correct depth and can access
 * {@link com.joutvhu.fixedwidth.parser.support.ParseContext#parentFrame()}.
 *
 * <p>Phase 4: fields are processed in dependency order (topological sort).
 * Fields annotated with {@code @FixedConditional} are skipped when their
 * condition is not met.
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public class ObjectReader extends FixedWidthReader<Object> {

    public ObjectReader(FixedTypeInfo info, ReadStrategy strategy) {
        super(info, strategy);
        if (info.getFixedObject() == null) this.reject();
    }

    @Override
    public Object read(StringAssembler assembler) {
        Object result = FixedHelper.newInstanceOf(info.getType());

        // Push an OBJECT frame so child fields see the correct depth and parent
        DefaultParseContext ctx = getActiveContext();
        DefaultContextFrame objectFrame = null;
        if (ctx != null) {
            objectFrame = new DefaultContextFrame(
                info, FrameType.OBJECT,
                ctx.frameStack().size(), -1,
                assembler, assembler.getValue(), result, null);
            ctx.pushFrame(objectFrame);
        }

        try {
            // Phase 4: sort fields in dependency order (detects circular deps)
            List<FixedTypeInfo> children = FieldDependencyResolver.sort(info.getElementTypeInfo());

            for (FixedTypeInfo fieldInfo : children) {
                Field field = fieldInfo.getField();
                if (field == null) continue;

                // ── READ_PRE_CUT: fire before slicing, check skip ─────────────
                boolean skip = false;
                if (ctx != null) {
                    ctx.resetSkipField();
                    // Push a temporary frame so handlers can access parentFrame()
                    DefaultContextFrame preFrame = new DefaultContextFrame(
                        fieldInfo, FrameType.FIELD,
                        ctx.frameStack().size(), -1,
                        assembler, null, null, null);
                    ctx.pushFrame(preFrame);
                    ctx.setCurrentValue(null);
                    firePhase(Phase.READ_PRE_CUT);
                    invokeHandlers(fieldInfo, ctx);
                    skip = ctx.isSkipField();
                    ctx.popFrame();
                    ctx.resetSkipField();
                }

                if (skip) {
                    // Field is conditional and condition not met — leave as null
                    continue;
                }

                StringAssembler childAssembler = assembler.child(fieldInfo);
                Object v = read(fieldInfo, childAssembler);

                ReflectionUtil.makeAccessible(field);
                ReflectionUtil.setField(field, result, v);

                // Update partial result on the object frame after each field
                // so subsequent conditional handlers can read already-parsed values
                if (objectFrame != null) {
                    objectFrame.setPartialResult(result);
                }
            }
        } finally {
            if (ctx != null) {
                // Fire READ_AFTER_OBJECT while the object frame is still on the stack
                firePhase(Phase.READ_AFTER_OBJECT);
                ctx.popFrame();
            }
        }

        return result;
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

    /**
     * Returns the active context if the strategy supports it, otherwise null.
     */
    private DefaultParseContext getActiveContext() {
        if (strategy instanceof FixedParseStrategy) {
            return ((FixedParseStrategy) strategy).getActiveContext();
        }
        return null;
    }
}
