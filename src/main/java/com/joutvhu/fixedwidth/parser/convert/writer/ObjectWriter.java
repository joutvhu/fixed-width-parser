package com.joutvhu.fixedwidth.parser.convert.writer;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.support.DefaultContextFrame;
import com.joutvhu.fixedwidth.parser.support.DefaultFixedStringBuilder;
import com.joutvhu.fixedwidth.parser.support.DefaultParseContext;
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
            List<FixedTypeInfo> children = CommonUtil.defaultIfNull(
                    info.getElementTypeInfo(), new ArrayList<>());

            for (FixedTypeInfo fieldInfo : children) {
                ReflectionUtil.makeAccessible(fieldInfo.getField());
                Object item = ReflectionUtil.getField(fieldInfo.getField(), value);
                String written = write(fieldInfo, item);
                assembler.set(fieldInfo, written);

                // Also record in the builder for handler access
                builder.addPart(
                        fieldInfo.getName() != null ? fieldInfo.getName() : fieldInfo.getLabel(),
                        fieldInfo, written);
            }
            return assembler.getValue();

        } finally {
            if (ctx != null) {
                if (strategy instanceof FixedParseStrategy) {
                    ((FixedParseStrategy) strategy).firePhaseHookPublic(Phase.WRITE_AFTER_OBJECT);
                }
                ctx.popFrame();
            }
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
