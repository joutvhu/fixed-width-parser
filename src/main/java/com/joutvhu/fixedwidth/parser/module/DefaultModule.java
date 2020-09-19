package com.joutvhu.fixedwidth.parser.module;

import com.joutvhu.fixedwidth.parser.handle.reader.*;
import com.joutvhu.fixedwidth.parser.handle.validator.*;
import com.joutvhu.fixedwidth.parser.handle.writer.*;

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
                ObjectWriter.class,

                RegexValidator.class,
                OptionValidator.class,
                DateValidator.class,
                BooleanValidator.class
        );
    }
}
