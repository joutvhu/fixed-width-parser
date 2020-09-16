package com.joutvhu.fixedwidth.parser.module;

import com.joutvhu.fixedwidth.parser.reader.impl.*;

public class DefaultModule extends FixedModule {
    public DefaultModule() {
        super(
                StringReader.class,
                BooleanReader.class,
                NumberReader.class,
                DateReader.class,
                CollectionReader.class,
                MapReader.class,
                ObjectReader.class
        );
    }
}
