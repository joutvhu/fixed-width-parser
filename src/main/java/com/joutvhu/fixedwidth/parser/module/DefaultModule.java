package com.joutvhu.fixedwidth.parser.module;

import com.joutvhu.fixedwidth.parser.convert.reader.*;
import com.joutvhu.fixedwidth.parser.convert.validator.BooleanValidator;
import com.joutvhu.fixedwidth.parser.convert.validator.DateValidator;
import com.joutvhu.fixedwidth.parser.convert.validator.OptionValidator;
import com.joutvhu.fixedwidth.parser.convert.validator.RegexValidator;
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
                DateValidator.class,
                BooleanValidator.class
        );
    }
}
