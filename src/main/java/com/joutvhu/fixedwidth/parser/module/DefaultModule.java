package com.joutvhu.fixedwidth.parser.module;

import com.joutvhu.fixedwidth.parser.reader.impl.*;
import com.joutvhu.fixedwidth.parser.writer.impl.*;

public class DefaultModule extends FixedModule {
    public static final FixedModule INSTANCE = new DefaultModule();

    public DefaultModule() {
        super(
                StringReader.class,
                BooleanReader.class,
                NumberReader.class,
                DateReader.class,
                CollectionReader.class,
                MapReader.class,
                ObjectReader.class,

                StringWriter.class,
                BooleanWriter.class,
                NumberWriter.class,
                DateWriter.class,
                CollectionWriter.class,
                MapWriter.class,
                ObjectWriter.class
        );
    }
}
