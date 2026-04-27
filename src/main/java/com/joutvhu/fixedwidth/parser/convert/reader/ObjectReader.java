package com.joutvhu.fixedwidth.parser.convert.reader;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.DefaultContextFrame;
import com.joutvhu.fixedwidth.parser.support.DefaultParseContext;
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
            List<FixedTypeInfo> children = info.getElementTypeInfo();
            for (FixedTypeInfo fieldInfo : children) {
                Field field = fieldInfo.getField();
                if (field != null) {
                    StringAssembler childAssembler = assembler.child(fieldInfo);
                    Object v = read(fieldInfo, childAssembler);

                    ReflectionUtil.makeAccessible(field);
                    ReflectionUtil.setField(field, result, v);

                    // Update partial result on the object frame after each field
                    if (objectFrame != null) {
                        objectFrame.setPartialResult(result);
                    }
                }
            }
        } finally {
            if (ctx != null) {
                // Fire READ_AFTER_OBJECT while the object frame is still on the stack
                if (strategy instanceof FixedParseStrategy) {
                    ((FixedParseStrategy) strategy).firePhaseHookPublic(Phase.READ_AFTER_OBJECT);
                }
                ctx.popFrame();
            }
        }

        return result;
    }

    /** Returns the active context if the strategy supports it, otherwise null. */
    private DefaultParseContext getActiveContext() {
        if (strategy instanceof FixedParseStrategy) {
            return ((FixedParseStrategy) strategy).getActiveContext();
        }
        return null;
    }
}
