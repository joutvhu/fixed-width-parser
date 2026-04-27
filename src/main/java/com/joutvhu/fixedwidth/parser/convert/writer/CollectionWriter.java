package com.joutvhu.fixedwidth.parser.convert.writer;

import com.joutvhu.fixedwidth.parser.annotation.FixedCount;
import com.joutvhu.fixedwidth.parser.annotation.FixedDelimiter;
import com.joutvhu.fixedwidth.parser.annotation.FixedTerminator;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.support.ContextFrame;
import com.joutvhu.fixedwidth.parser.support.DefaultParseContext;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedStringAssembler;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.support.WriteStrategy;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

/**
 * Writes a Java {@link Collection} to a fixed-width string.
 *
 * <p>Phase 5 enhancements:
 * <ul>
 *   <li>{@link FixedCount} — write exactly N elements; for {@code field} mode,
 *       updates the count field on the parent object before writing</li>
 *   <li>{@link FixedDelimiter} — join elements with a delimiter</li>
 *   <li>{@link FixedTerminator} — append a terminator after the last element</li>
 * </ul>
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public class CollectionWriter extends FixedWidthWriter<Collection<?>> {
    protected FixedTypeInfo valueInfo;
    protected Integer length = 0;

    public CollectionWriter(FixedTypeInfo info, WriteStrategy strategy) {
        super(info, strategy);
        // For delimiter mode, @FixedParam is not required
        boolean hasDelimiter = info.getField() != null
                && info.getField().getAnnotation(FixedDelimiter.class) != null;
        if (!Collection.class.isAssignableFrom(info.getType()))
            this.reject();
        if (!hasDelimiter && info.getGenericTypeInfo().size() != 1)
            this.reject();
        this.valueInfo = info.getGenericTypeInfo().isEmpty() ? null : info.getGenericTypeInfo().get(0);
        this.length = valueInfo != null ? valueInfo.getLength() : 0;
    }

    @Override
    public String write(Collection<?> value) {
        // ── @FixedDelimiter ───────────────────────────────────────────────────
        FixedDelimiter delimiter = info.getField() != null
                ? info.getField().getAnnotation(FixedDelimiter.class) : null;
        if (delimiter != null) {
            return writeDelimited(value, delimiter);
        }

        // ── @FixedTerminator ──────────────────────────────────────────────────
        FixedTerminator terminator = info.getField() != null
                ? info.getField().getAnnotation(FixedTerminator.class) : null;
        if (terminator != null) {
            return writeTerminated(value, terminator);
        }

        // ── @FixedCount(field) — update count field on parent object ──────────
        FixedCount fixedCount = info.getField() != null
                ? info.getField().getAnnotation(FixedCount.class) : null;
        if (fixedCount != null && !fixedCount.field().isEmpty()) {
            updateCountField(fixedCount.field(), value.size());
        }

        // ── Default fixed-length write ────────────────────────────────────────
        StringAssembler assembler = FixedStringAssembler.instance();
        int start = 0;
        for (Object item : value) {
            assembler.set(start, length, write(valueInfo, item));
            start += length;
        }
        return assembler.getValue();
    }

    private String writeDelimited(Collection<?> value, FixedDelimiter delimiter) {
        if (value.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object item : value) {
            if (!first) sb.append(delimiter.value());
            sb.append(item != null ? item.toString() : "");
            first = false;
        }
        return sb.toString();
    }

    private String writeTerminated(Collection<?> value, FixedTerminator terminator) {
        if (value.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Object item : value) {
            sb.append(write(valueInfo, item));
        }
        sb.append(terminator.value());
        return sb.toString();
    }

    /** Updates the count field on the parent object to reflect the actual collection size. */
    private void updateCountField(String fieldName, int count) {
        Object parentObj = getParentObject();
        if (parentObj == null) return;
        Class<?> cls = parentObj.getClass();
        while (cls != null && cls != Object.class) {
            try {
                Field f = cls.getDeclaredField(fieldName);
                ReflectionUtil.makeAccessible(f);
                // Set the count — handle Integer and int
                Class<?> fType = f.getType();
                if (fType == int.class || fType == Integer.class) {
                    ReflectionUtil.setField(f, parentObj, count);
                } else if (fType == long.class || fType == Long.class) {
                    ReflectionUtil.setField(f, parentObj, (long) count);
                } else if (fType == String.class) {
                    ReflectionUtil.setField(f, parentObj, String.valueOf(count));
                }
                return;
            } catch (NoSuchFieldException e) {
                cls = cls.getSuperclass();
            }
        }
    }

    private Object getParentObject() {
        if (!(strategy instanceof FixedParseStrategy)) return null;
        DefaultParseContext ctx = ((FixedParseStrategy) strategy).getActiveContext();
        if (ctx == null) return null;
        List<ContextFrame> frames = ctx.frameStack();
        for (int i = frames.size() - 1; i >= 0; i--) {
            ContextFrame frame = frames.get(i);
            if (frame.getPartialResult() != null) return frame.getPartialResult();
        }
        return null;
    }
}
