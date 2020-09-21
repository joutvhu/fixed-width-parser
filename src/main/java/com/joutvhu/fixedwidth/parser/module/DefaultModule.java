package com.joutvhu.fixedwidth.parser.module;

import com.joutvhu.fixedwidth.parser.convert.reader.*;
import com.joutvhu.fixedwidth.parser.convert.validator.*;
import com.joutvhu.fixedwidth.parser.convert.writer.*;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public class DefaultModule extends FixedModule {
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
                NumberValidator.class,
                DateValidator.class,
                BooleanValidator.class
        );
    }
}
