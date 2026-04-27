package com.joutvhu.fixedwidth.parser.module;

import com.joutvhu.fixedwidth.parser.convert.reader.BooleanReader;
import com.joutvhu.fixedwidth.parser.convert.reader.CollectionReader;
import com.joutvhu.fixedwidth.parser.convert.reader.DateReader;
import com.joutvhu.fixedwidth.parser.convert.reader.EnumReader;
import com.joutvhu.fixedwidth.parser.convert.reader.MapReader;
import com.joutvhu.fixedwidth.parser.convert.reader.NumberReader;
import com.joutvhu.fixedwidth.parser.convert.reader.ObjectReader;
import com.joutvhu.fixedwidth.parser.convert.reader.OptionalReader;
import com.joutvhu.fixedwidth.parser.convert.reader.StringReader;
import com.joutvhu.fixedwidth.parser.convert.reader.UUIDReader;
import com.joutvhu.fixedwidth.parser.convert.writer.BooleanWriter;
import com.joutvhu.fixedwidth.parser.convert.writer.CollectionWriter;
import com.joutvhu.fixedwidth.parser.convert.writer.DateWriter;
import com.joutvhu.fixedwidth.parser.convert.writer.EnumWriter;
import com.joutvhu.fixedwidth.parser.convert.writer.MapWriter;
import com.joutvhu.fixedwidth.parser.convert.writer.NumberWriter;
import com.joutvhu.fixedwidth.parser.convert.writer.ObjectWriter;
import com.joutvhu.fixedwidth.parser.convert.writer.OptionalWriter;
import com.joutvhu.fixedwidth.parser.convert.writer.StringWriter;
import com.joutvhu.fixedwidth.parser.convert.writer.UUIDWriter;

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
            EnumReader.class,
            UUIDReader.class,
            OptionalReader.class,
            CollectionReader.class,
            MapReader.class,
            ObjectReader.class,

            // Writers
            StringWriter.class,
            BooleanWriter.class,
            NumberWriter.class,
            DateWriter.class,
            EnumWriter.class,
            UUIDWriter.class,
            OptionalWriter.class,
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
