package com.joutvhu.fixedwidth.parser.convert.reader;

import com.joutvhu.fixedwidth.parser.annotation.FixedCount;
import com.joutvhu.fixedwidth.parser.annotation.FixedDelimiter;
import com.joutvhu.fixedwidth.parser.annotation.FixedTerminator;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.exception.FixedParserException;
import com.joutvhu.fixedwidth.parser.support.ContextFrame;
import com.joutvhu.fixedwidth.parser.support.DefaultParseContext;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ReadStrategy;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.FixedHelper;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

/**
 * Reads a fixed-width string into a Java {@link Collection}.
 *
 * <p>Phase 5 enhancements:
 * <ul>
 *   <li>{@link FixedCount} — read exactly N elements (fixed or from another field)</li>
 *   <li>{@link FixedDelimiter} — split by a delimiter string</li>
 *   <li>{@link FixedTerminator} — stop reading at a terminator string</li>
 * </ul>
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public class CollectionReader extends FixedWidthReader<Collection<?>> {
    protected FixedTypeInfo valueInfo;
    protected Integer length = 0;

    public CollectionReader(FixedTypeInfo info, ReadStrategy strategy) {
        super(info, strategy);
        // For delimiter mode, @FixedParam is not required (elements are variable-length)
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
    public Collection<?> read(StringAssembler assembler) {
        Class<?> type = info.getType();
        Class<? extends Collection> selectedType = (Class<? extends Collection>) FixedHelper.selectSubTypeOf(type);
        if (selectedType == null)
            throw new FixedParserException(String.format("Not found subclass for %s", info.getLabel()));

        Collection<Object> objects = FixedHelper.newInstanceOf(selectedType);

        // ── @FixedDelimiter ───────────────────────────────────────────────────
        FixedDelimiter delimiter = info.getField() != null
            ? info.getField().getAnnotation(FixedDelimiter.class) : null;
        if (delimiter != null) {
            return readDelimited(assembler, objects, delimiter);
        }

        // ── @FixedTerminator ──────────────────────────────────────────────────
        FixedTerminator terminator = info.getField() != null
            ? info.getField().getAnnotation(FixedTerminator.class) : null;
        if (terminator != null) {
            return readTerminated(assembler, objects, terminator);
        }

        // ── @FixedCount or default fixed-length iteration ─────────────────────
        int count = resolveCount();
        return readFixedCount(assembler, objects, count);
    }

    /**
     * Read exactly {@code count} elements (or all if count < 0).
     */
    private Collection<Object> readFixedCount(StringAssembler assembler,
                                              Collection<Object> objects, int count) {
        if (length <= 0) return objects;
        int len = assembler.length();
        int cursor = 0;
        int read = 0;
        while (cursor < len && (count < 0 || read < count)) {
            StringAssembler itemAssembler = assembler.child(cursor, length);
            if (count < 0 && itemAssembler.isBlank(valueInfo)) break;
            Object item = read(valueInfo, itemAssembler);
            objects.add(item);
            cursor += length;
            read++;
        }
        return objects;
    }

    /**
     * Read elements separated by a delimiter.
     */
    private Collection<Object> readDelimited(StringAssembler assembler,
                                             Collection<Object> objects,
                                             FixedDelimiter delimiter) {
        String raw = assembler.getValue();
        // Trim trailing spaces from the full field value
        raw = raw.replaceAll("\\s+$", "");
        if (raw.isEmpty()) return objects;

        String[] parts = raw.split(java.util.regex.Pattern.quote(delimiter.value()), -1);
        for (String part : parts) {
            String item = delimiter.trim() ? part.trim() : part;
            if (!item.isEmpty()) objects.add(item);
        }
        return objects;
    }

    /**
     * Read elements until a terminator string is encountered.
     */
    private Collection<Object> readTerminated(StringAssembler assembler,
                                              Collection<Object> objects,
                                              FixedTerminator terminator) {
        if (length <= 0) return objects;
        String raw = assembler.getValue();
        String term = terminator.value();
        int terminatorIdx = raw.indexOf(term);
        String content = terminatorIdx >= 0 ? raw.substring(0, terminatorIdx) : raw;

        int cursor = 0;
        while (cursor + length <= content.length()) {
            StringAssembler itemAssembler = assembler.child(cursor, length);
            Object item = read(valueInfo, itemAssembler);
            objects.add(item);
            cursor += length;
        }
        return objects;
    }

    /**
     * Resolves the element count from {@link FixedCount}:
     * <ul>
     *   <li>Fixed value: returns {@code annotation.value()}</li>
     *   <li>Field reference: reads the count from the parent object's partial result</li>
     *   <li>No annotation: returns {@code -1} (read until blank/end)</li>
     * </ul>
     */
    private int resolveCount() {
        if (info.getField() == null) return -1;
        FixedCount fixedCount = info.getField().getAnnotation(FixedCount.class);
        if (fixedCount == null) return -1;

        // Fixed count
        if (fixedCount.value() >= 0) return fixedCount.value();

        // Count from another field
        String fieldName = fixedCount.field();
        if (fieldName == null || fieldName.isEmpty()) return -1;

        Object parentObj = getParentObject();
        if (parentObj == null) return -1;

        return readIntField(parentObj, fieldName);
    }

    /**
     * Gets the parent object from the context frame stack.
     */
    private Object getParentObject() {
        if (!(strategy instanceof FixedParseStrategy)) return null;
        DefaultParseContext ctx = ((FixedParseStrategy) strategy).getActiveContext();
        if (ctx == null) return null;
        // Walk up the frame stack to find the nearest OBJECT frame
        List<ContextFrame> frames = ctx.frameStack();
        for (int i = frames.size() - 1; i >= 0; i--) {
            ContextFrame frame = frames.get(i);
            if (frame.getPartialResult() != null) return frame.getPartialResult();
        }
        return null;
    }

    private int readIntField(Object obj, String fieldName) {
        Class<?> cls = obj.getClass();
        while (cls != null && cls != Object.class) {
            try {
                Field f = cls.getDeclaredField(fieldName);
                ReflectionUtil.makeAccessible(f);
                Object val = ReflectionUtil.getField(f, obj);
                if (val instanceof Number) return ((Number) val).intValue();
                if (val instanceof String) return Integer.parseInt((String) val);
                return -1;
            } catch (NoSuchFieldException e) {
                cls = cls.getSuperclass();
            }
        }
        return -1;
    }
}
