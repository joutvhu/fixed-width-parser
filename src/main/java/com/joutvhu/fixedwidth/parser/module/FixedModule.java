package com.joutvhu.fixedwidth.parser.module;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.convert.ParsingApprover;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ReadStrategy;
import com.joutvhu.fixedwidth.parser.support.WriteStrategy;
import com.joutvhu.fixedwidth.parser.util.IgnoreError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Fixed module.
 * Management readers, writers and validators
 *
 * @author Giao Ho
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class FixedModule {
    private Set<Class<? extends FixedWidthReader>> readers = new LinkedHashSet<>();
    private Set<Class<? extends FixedWidthWriter>> writers = new LinkedHashSet<>();
    private Set<Class<? extends FixedWidthValidator>> validators = new LinkedHashSet<>();

    public FixedModule(Class<?>... classes) {
        for (Class<?> c : classes) {
            if (FixedWidthReader.class.isAssignableFrom(c))
                this.readers.add((Class<? extends FixedWidthReader>) c);
            else if (FixedWidthWriter.class.isAssignableFrom(c))
                this.writers.add((Class<? extends FixedWidthWriter>) c);
            else if (FixedWidthValidator.class.isAssignableFrom(c))
                this.validators.add((Class<? extends FixedWidthValidator>) c);
        }
    }

    /**
     * Merge another module to this module
     *
     * @param module another module
     * @return this
     */
    public FixedModule merge(FixedModule module) {
        readers.addAll(module.readers);
        writers.addAll(module.writers);
        validators.addAll(module.validators);
        return this;
    }

    /**
     * Create readers, writers or validators by class types
     *
     * @param takeOne      create only one instance
     * @param handlers     {@link Set} of class types
     * @param info         the {@link FixedTypeInfo}
     * @param strategy     the {@link FixedParseStrategy}
     * @param strategyType strategy class type
     * @param <T>          result type
     * @return list of reader, writer or validator
     */
    private <T extends ParsingApprover> List<T> createHandlersBy(
            boolean takeOne, Set<Class<? extends T>> handlers, FixedTypeInfo info,
            FixedParseStrategy strategy, Class<?> strategyType) {
        List<T> result = new ArrayList<>();
        for (Class<? extends T> handlerClass : handlers) {
            T handler = IgnoreError.execute(() -> {
                Constructor<? extends T> constructor;
                if (strategyType != null) {
                    constructor = handlerClass.getConstructor(FixedTypeInfo.class, strategyType);
                    return constructor.newInstance(info, strategy);
                } else {
                    constructor = handlerClass.getConstructor(FixedTypeInfo.class);
                    return constructor.newInstance(info);
                }
            });

            if (handler != null) {
                result.add(handler);
                if (takeOne) return result;
            }
        }
        return result;
    }

    /**
     * Create one reader, writer or validator by class types
     *
     * @param handlers {@link Set} of class types
     * @param info     the {@link FixedTypeInfo}
     * @param strategy the {@link FixedParseStrategy}
     * @param <T>      result type
     * @return reader, writer or validator
     */
    private <T extends ParsingApprover> T createHandlerBy(
            Set<Class<? extends T>> handlers, FixedTypeInfo info,
            FixedParseStrategy strategy, Class<?> strategyType) {
        List<T> result = createHandlersBy(true, handlers, info, strategy, strategyType);
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Create reader by {@link FixedTypeInfo} and {@link FixedParseStrategy}
     *
     * @param info     the {@link FixedTypeInfo}
     * @param strategy the {@link FixedParseStrategy}
     * @return reader
     */
    public final FixedWidthReader<Object> createReaderBy(FixedTypeInfo info, FixedParseStrategy strategy) {
        return createHandlerBy(readers, info, strategy, ReadStrategy.class);
    }

    /**
     * Create writer by {@link FixedTypeInfo} and {@link FixedParseStrategy}
     *
     * @param info     the {@link FixedTypeInfo}
     * @param strategy the {@link FixedParseStrategy}
     * @return writer
     */
    public final FixedWidthWriter<Object> createWriterBy(FixedTypeInfo info, FixedParseStrategy strategy) {
        return createHandlerBy(writers, info, strategy, WriteStrategy.class);
    }

    /**
     * Create validators by {@link FixedTypeInfo} and {@link FixedParseStrategy}
     *
     * @param info     the {@link FixedTypeInfo}
     * @param strategy the {@link FixedParseStrategy}
     * @return validators
     */
    public final List<FixedWidthValidator> createValidatorsBy(FixedTypeInfo info, FixedParseStrategy strategy) {
        return createHandlersBy(false, validators, info, strategy, null);
    }
}
