package com.joutvhu.fixedwidth.parser.module;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.convert.ParsingApprover;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
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

    public FixedModule merge(FixedModule module) {
        readers.addAll(module.readers);
        writers.addAll(module.writers);
        validators.addAll(module.validators);
        return this;
    }

    private <T extends ParsingApprover> List<T> createHandlersBy(Set<Class<? extends T>> handlers, FixedTypeInfo info, FixedParseStrategy strategy, boolean takeOne) {
        List<T> result = new ArrayList<>();
        for (Class<? extends T> handlerClass : handlers) {
            T handler = IgnoreError.execute(() -> {
                Constructor<? extends T> constructor = handlerClass
                        .getConstructor(FixedTypeInfo.class, FixedParseStrategy.class);
                return constructor != null ? constructor.newInstance(info, strategy) : null;
            });
            if (handler != null) {
                result.add(handler);
                if (takeOne) return result;
            }
        }
        return result;
    }

    private <T extends ParsingApprover> T createHandlerBy(Set<Class<? extends T>> handlers,
                                                          FixedTypeInfo info, FixedParseStrategy strategy) {
        List<T> result = createHandlersBy(handlers, info, strategy, true);
        return result.isEmpty() ? null : result.get(0);
    }

    public final FixedWidthReader<Object> createReaderBy(FixedTypeInfo info, FixedParseStrategy strategy) {
        return createHandlerBy(readers, info, strategy);
    }

    public final FixedWidthWriter<Object> createWriterBy(FixedTypeInfo info, FixedParseStrategy strategy) {
        return createHandlerBy(writers, info, strategy);
    }

    public final List<FixedWidthValidator> createValidatorsBy(FixedTypeInfo info, FixedParseStrategy strategy) {
        return createHandlersBy(validators, info, strategy, false);
    }
}
