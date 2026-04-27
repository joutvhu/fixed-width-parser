package com.joutvhu.fixedwidth.parser.convert.hook;

import com.joutvhu.fixedwidth.parser.convert.ModuleHook;
import com.joutvhu.fixedwidth.parser.exception.FixedParserException;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedStringAssembler;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.FixedHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Handles {@link Map} fields.
 *
 * <p>READ at {@link Phase#READ_AFTER_TRANSFORM}: parses string to map.
 * WRITE at {@link Phase#WRITE_AFTER_GET}: converts map to string.
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public class MapHook implements ModuleHook {

    @Override
    public boolean supports(FixedTypeInfo info) {
        return Map.class.isAssignableFrom(info.getType())
            && info.getGenericTypeInfo().size() == 2;
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void handleRead(FixedTypeInfo info, ParseContext ctx) {
        Class<?> type = info.getType();
        Class<? extends Map> selectedType =
            (Class<? extends Map>) FixedHelper.selectSubTypeOf(type);
        if (selectedType == null)
            throw new FixedParserException(
                String.format("Not found subclass for %s", info.getLabel()));

        Map<Object, Object> objects = FixedHelper.newInstanceOf(selectedType);

        FixedTypeInfo keyInfo = info.getGenericTypeInfo().get(0);
        FixedTypeInfo valueInfo = info.getGenericTypeInfo().get(1);
        int keyLength = keyInfo.getLength();
        int valueLength = valueInfo.getLength();

        String processedString = ctx.getProcessedString();
        StringAssembler assembler = processedString != null
            ? FixedStringAssembler.of(processedString)
            : FixedStringAssembler.of(StringUtils.EMPTY);

        FixedParseStrategy strategy = ctx.get(OptionalHook.STRATEGY_KEY, FixedParseStrategy.class);
        if (strategy != null && keyLength > 0 && valueLength > 0) {
            int len = assembler.length();
            int cursor = 0;
            while (cursor < len) {
                String rawKey = assembler.get(cursor, keyLength);
                if (rawKey == null || rawKey.isEmpty()) break;

                StringAssembler keyAssembler = assembler.child(cursor, keyLength);
                if (keyAssembler.isBlank(keyInfo)) break;
                Object key = strategy.read(keyInfo, keyAssembler);
                cursor += keyLength;

                StringAssembler valueAssembler = assembler.child(cursor, valueLength);
                Object value = strategy.read(valueInfo, valueAssembler);
                cursor += valueLength;
                objects.put(key, value);
            }
        }
        ctx.setCurrentValue(objects);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void handleWrite(FixedTypeInfo info, ParseContext ctx) {
        Object rawValue = ctx.getCurrentValue();
        if (rawValue == null) {
            ctx.setCurrentValue(StringUtils.EMPTY);
            return;
        }
        Map<?, ?> value = (Map<?, ?>) rawValue;

        FixedTypeInfo keyInfo = info.getGenericTypeInfo().get(0);
        FixedTypeInfo valueInfo = info.getGenericTypeInfo().get(1);
        int keyLength = keyInfo.getLength();
        int valueLength = valueInfo.getLength();

        FixedParseStrategy strategy = ctx.get(OptionalHook.STRATEGY_KEY, FixedParseStrategy.class);
        if (strategy == null) {
            ctx.setCurrentValue(StringUtils.EMPTY);
            return;
        }

        StringAssembler assembler = FixedStringAssembler.instance();
        int start = 0;
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            assembler.set(start, keyLength, strategy.write(keyInfo, entry.getKey()));
            start += keyLength;
            assembler.set(start, valueLength, strategy.write(valueInfo, entry.getValue()));
            start += valueLength;
        }
        ctx.setCurrentValue(assembler.getValue());
    }
}
