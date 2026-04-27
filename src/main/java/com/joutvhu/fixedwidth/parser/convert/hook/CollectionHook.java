package com.joutvhu.fixedwidth.parser.convert.hook;

import com.joutvhu.fixedwidth.parser.annotation.FixedCount;
import com.joutvhu.fixedwidth.parser.annotation.FixedDelimiter;
import com.joutvhu.fixedwidth.parser.annotation.FixedTerminator;
import com.joutvhu.fixedwidth.parser.convert.ModuleHook;
import com.joutvhu.fixedwidth.parser.exception.FixedParserException;
import com.joutvhu.fixedwidth.parser.support.ContextFrame;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedStringAssembler;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.FixedHelper;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Handles {@link Collection} fields (List, Set, Queue, etc.).
 *
 * <p>READ at {@link Phase#READ_AFTER_TRANSFORM}: parses string to collection.
 * WRITE at {@link Phase#WRITE_AFTER_GET}: converts collection to string.
 *
 * <p>Supports {@link FixedDelimiter}, {@link FixedTerminator}, and {@link FixedCount}.
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public class CollectionHook implements ModuleHook {

    @Override
    public boolean supports(FixedTypeInfo info) {
        return Collection.class.isAssignableFrom(info.getType());
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void handleRead(FixedTypeInfo info, ParseContext ctx) {
        Class<?> type = info.getType();
        Class<? extends Collection> selectedType =
            (Class<? extends Collection>) FixedHelper.selectSubTypeOf(type);
        if (selectedType == null)
            throw new FixedParserException(
                String.format("Not found subclass for %s", info.getLabel()));

        Collection<Object> objects = FixedHelper.newInstanceOf(selectedType);

        String processedString = ctx.getProcessedString();
        StringAssembler assembler = processedString != null
            ? FixedStringAssembler.of(processedString)
            : FixedStringAssembler.of(StringUtils.EMPTY);

        FixedTypeInfo valueInfo = info.getGenericTypeInfo().isEmpty()
            ? null : info.getGenericTypeInfo().get(0);
        int length = valueInfo != null ? valueInfo.getLength() : 0;

        FixedDelimiter delimiter = info.getField() != null
            ? info.getField().getAnnotation(FixedDelimiter.class) : null;
        if (delimiter != null) {
            ctx.setCurrentValue(readDelimited(assembler, objects, delimiter));
            return;
        }

        FixedTerminator terminator = info.getField() != null
            ? info.getField().getAnnotation(FixedTerminator.class) : null;
        if (terminator != null) {
            ctx.setCurrentValue(readTerminated(assembler, objects, terminator, valueInfo, length, ctx));
            return;
        }

        int count = resolveCount(info, ctx);
        ctx.setCurrentValue(readFixedCount(assembler, objects, count, valueInfo, length, ctx));
    }

    private Collection<Object> readFixedCount(StringAssembler assembler,
                                              Collection<Object> objects, int count,
                                              FixedTypeInfo valueInfo, int length,
                                              ParseContext ctx) {
        if (length <= 0) return objects;
        FixedParseStrategy strategy = ctx.get(OptionalHook.STRATEGY_KEY, FixedParseStrategy.class);
        if (strategy == null) return objects;

        int len = assembler.length();
        int cursor = 0;
        int read = 0;
        while (cursor < len && (count < 0 || read < count)) {
            StringAssembler itemAssembler = assembler.child(cursor, length);
            if (count < 0 && itemAssembler.isBlank(valueInfo)) break;
            Object item = strategy.read(valueInfo, itemAssembler);
            objects.add(item);
            cursor += length;
            read++;
        }
        return objects;
    }

    private Collection<Object> readDelimited(StringAssembler assembler,
                                             Collection<Object> objects,
                                             FixedDelimiter delimiter) {
        String raw = assembler.getValue().replaceAll("\\s+$", "");
        if (raw.isEmpty()) return objects;
        String[] parts = raw.split(java.util.regex.Pattern.quote(delimiter.value()), -1);
        for (String part : parts) {
            String item = delimiter.trim() ? part.trim() : part;
            if (!item.isEmpty()) objects.add(item);
        }
        return objects;
    }

    private Collection<Object> readTerminated(StringAssembler assembler,
                                              Collection<Object> objects,
                                              FixedTerminator terminator,
                                              FixedTypeInfo valueInfo, int length,
                                              ParseContext ctx) {
        if (length <= 0) return objects;
        FixedParseStrategy strategy = ctx.get(OptionalHook.STRATEGY_KEY, FixedParseStrategy.class);
        if (strategy == null) return objects;

        String raw = assembler.getValue();
        String term = terminator.value();
        int terminatorIdx = raw.indexOf(term);
        String content = terminatorIdx >= 0 ? raw.substring(0, terminatorIdx) : raw;

        int cursor = 0;
        while (cursor + length <= content.length()) {
            StringAssembler itemAssembler = assembler.child(cursor, length);
            Object item = strategy.read(valueInfo, itemAssembler);
            objects.add(item);
            cursor += length;
        }
        return objects;
    }

    private int resolveCount(FixedTypeInfo info, ParseContext ctx) {
        if (info.getField() == null) return -1;
        FixedCount fixedCount = info.getField().getAnnotation(FixedCount.class);
        if (fixedCount == null) return -1;
        if (fixedCount.value() >= 0) return fixedCount.value();

        String fieldName = fixedCount.field();
        if (fieldName == null || fieldName.isEmpty()) return -1;

        Object parentObj = getParentObject(ctx);
        if (parentObj == null) return -1;
        return readIntField(parentObj, fieldName);
    }

    // ── WRITE ─────────────────────────────────────────────────────────────────

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void handleWrite(FixedTypeInfo info, ParseContext ctx) {
        Object rawValue = ctx.getCurrentValue();
        if (rawValue == null) {
            ctx.setCurrentValue(StringUtils.EMPTY);
            return;
        }
        Collection<?> value = (Collection<?>) rawValue;

        FixedTypeInfo valueInfo = info.getGenericTypeInfo().isEmpty()
            ? null : info.getGenericTypeInfo().get(0);
        int length = valueInfo != null ? valueInfo.getLength() : 0;

        FixedDelimiter delimiter = info.getField() != null
            ? info.getField().getAnnotation(FixedDelimiter.class) : null;
        if (delimiter != null) {
            ctx.setCurrentValue(writeDelimited(value, delimiter));
            return;
        }

        FixedTerminator terminator = info.getField() != null
            ? info.getField().getAnnotation(FixedTerminator.class) : null;
        if (terminator != null) {
            ctx.setCurrentValue(writeTerminated(value, terminator, valueInfo, ctx));
            return;
        }

        FixedCount fixedCount = info.getField() != null
            ? info.getField().getAnnotation(FixedCount.class) : null;
        if (fixedCount != null && !fixedCount.field().isEmpty()) {
            updateCountField(fixedCount.field(), value.size(), ctx);
        }

        ctx.setCurrentValue(writeFixedLength(value, valueInfo, length, ctx));
    }

    private String writeFixedLength(Collection<?> value, FixedTypeInfo valueInfo,
                                    int length, ParseContext ctx) {
        FixedParseStrategy strategy = ctx.get(OptionalHook.STRATEGY_KEY, FixedParseStrategy.class);
        if (strategy == null || valueInfo == null) return StringUtils.EMPTY;

        StringAssembler assembler = FixedStringAssembler.instance();
        int start = 0;
        for (Object item : value) {
            assembler.set(start, length, strategy.write(valueInfo, item));
            start += length;
        }
        return assembler.getValue();
    }

    private String writeDelimited(Collection<?> value, FixedDelimiter delimiter) {
        if (value.isEmpty()) return StringUtils.EMPTY;
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object item : value) {
            if (!first) sb.append(delimiter.value());
            sb.append(item != null ? item.toString() : "");
            first = false;
        }
        return sb.toString();
    }

    private String writeTerminated(Collection<?> value, FixedTerminator terminator,
                                   FixedTypeInfo valueInfo, ParseContext ctx) {
        if (value.isEmpty()) return StringUtils.EMPTY;
        FixedParseStrategy strategy = ctx.get(OptionalHook.STRATEGY_KEY, FixedParseStrategy.class);
        if (strategy == null || valueInfo == null) return StringUtils.EMPTY;

        StringBuilder sb = new StringBuilder();
        for (Object item : value) {
            sb.append(strategy.write(valueInfo, item));
        }
        sb.append(terminator.value());
        return sb.toString();
    }

    private void updateCountField(String fieldName, int count, ParseContext ctx) {
        Object parentObj = getParentObject(ctx);
        if (parentObj == null) return;
        Class<?> cls = parentObj.getClass();
        while (cls != null && cls != Object.class) {
            try {
                Field f = cls.getDeclaredField(fieldName);
                ReflectionUtil.makeAccessible(f);
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

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Object getParentObject(ParseContext ctx) {
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
