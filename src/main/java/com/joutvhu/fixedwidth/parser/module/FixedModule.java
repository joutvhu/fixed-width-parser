package com.joutvhu.fixedwidth.parser.module;

import com.joutvhu.fixedwidth.parser.handler.FixedWidthHandler;
import com.joutvhu.fixedwidth.parser.handler.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.handler.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.handler.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.IgnoreError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Constructor;
import java.util.LinkedHashSet;
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

    private <T extends FixedWidthHandler> T createHandlerBy(Set<Class<? extends T>> handlers,
                                                            FixedTypeInfo info, FixedParseStrategy strategy) {
        for (Class<? extends T> handlerClass : handlers) {
            T handler = IgnoreError.execute(() -> {
                Constructor<? extends T> constructor = handlerClass
                        .getConstructor(FixedTypeInfo.class, FixedParseStrategy.class);
                return constructor != null ? constructor.newInstance(info, strategy) : null;
            });
            if (handler != null) return handler;
        }
        return null;
    }

    public final FixedWidthReader createReaderBy(FixedTypeInfo info, FixedParseStrategy strategy) {
        return createHandlerBy(readers, info, strategy);
    }

    public final FixedWidthWriter createWriterBy(FixedTypeInfo info, FixedParseStrategy strategy) {
        return createHandlerBy(writers, info, strategy);
    }

    public final FixedWidthValidator createValidatorBy(FixedTypeInfo info, FixedParseStrategy strategy) {
        return createHandlerBy(validators, info, strategy);
    }
}
