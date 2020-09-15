package com.joutvhu.fixedwidth.parser.module;

import com.joutvhu.fixedwidth.parser.reader.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.writer.FixedWidthWriter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class FixedModule {
    private Set<Class<? extends FixedWidthReader>> readers = new LinkedHashSet<>();
    private Set<Class<? extends FixedWidthWriter>> writers = new LinkedHashSet<>();

    public FixedModule(Class<?> ...classes) {
        for (Class<?> c : classes) {
            if (FixedWidthReader.class.isAssignableFrom(c))
                this.readers.add((Class<? extends FixedWidthReader>) c);
            else if (FixedWidthWriter.class.isAssignableFrom(c))
                this.writers.add((Class<? extends FixedWidthWriter>) c);
        }
    }

    public FixedModule merge(FixedModule module) {
        readers.addAll(module.readers);
        writers.addAll(module.writers);
        return this;
    }
}
