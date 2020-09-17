package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.exception.FixedException;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.reader.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.util.IgnoreError;
import com.joutvhu.fixedwidth.parser.writer.FixedWidthWriter;

import java.lang.reflect.Constructor;
import java.util.Set;

public class FixedParseStrategy {
    private FixedModule module;

    public FixedParseStrategy(FixedModule module) {
        this.module = module;
    }

    public Object read(FixedTypeInfo info, StringAssembler assembler) {
        info.detectType(assembler);
        Set<Class<? extends FixedWidthReader>> readers = module.getReaders();
        for (Class<? extends FixedWidthReader> readerClass : readers) {
            FixedWidthReader reader = IgnoreError.execute(() -> {
                Constructor<? extends FixedWidthReader> constructor =
                        readerClass.getConstructor(FixedTypeInfo.class, FixedParseStrategy.class);
                return constructor != null ? constructor.newInstance(info, this) : null;
            });
            if (reader != null)
                return reader.read(assembler);
        }
        throw new FixedException("Reader not found.");
    }

    public String write(FixedTypeInfo info, Object value) {
        Set<Class<? extends FixedWidthWriter>> writers = module.getWriters();
        for (Class<? extends FixedWidthWriter> writerClass : writers) {
            FixedWidthWriter writer = IgnoreError.execute(() -> {
                Constructor<? extends FixedWidthWriter> constructor =
                        writerClass.getConstructor(FixedTypeInfo.class, FixedParseStrategy.class);
                return constructor != null ? constructor.newInstance(info, this) : null;
            });
            if (writer != null) {
                String result = writer.write(value);
                return StringAssembler.of(result).pad(info).getValue();
            }
        }
        throw new FixedException("Writer not found.");
    }
}
