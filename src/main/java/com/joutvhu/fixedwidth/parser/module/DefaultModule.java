package com.joutvhu.fixedwidth.parser.module;

import com.joutvhu.fixedwidth.parser.convert.reader.*;
import com.joutvhu.fixedwidth.parser.convert.validator.OptionValidator;
import com.joutvhu.fixedwidth.parser.convert.validator.RegexValidator;
import com.joutvhu.fixedwidth.parser.convert.writer.*;

/**
 * Default module.
 *
 * <p>Phase 2: {@link RegexValidator} and {@link OptionValidator} are kept for
 * backward compatibility but their logic is now also handled by
 * {@link com.joutvhu.fixedwidth.parser.convert.handler.RegexHandler} and
 * {@link com.joutvhu.fixedwidth.parser.convert.handler.OptionHandler} via
 * {@code @FixedHandler}.  NumberValidator, DateValidator, and BooleanValidator
 * have been removed from this module — their validation is now performed
 * exclusively by the handler mechanism.
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public class DefaultModule extends FixedModule {
    public DefaultModule() {
        super(
                // Readers
                StringReader.class,
                BooleanReader.class,
                NumberReader.class,
                DateReader.class,
                CollectionReader.class,
                MapReader.class,
                ObjectReader.class,

                // Writers
                StringWriter.class,
                BooleanWriter.class,
                NumberWriter.class,
                DateWriter.class,
                CollectionWriter.class,
                MapWriter.class,
                ObjectWriter.class

                // Validators: RegexValidator and OptionValidator removed —
                // replaced by @FixedHandler on @FixedRegex and @FixedOption.
                // NumberValidator, DateValidator, BooleanValidator removed —
                // replaced by FormatDispatchHandler on @FixedFormat.
        );
    }
}
