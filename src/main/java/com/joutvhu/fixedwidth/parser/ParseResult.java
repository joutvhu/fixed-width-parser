package com.joutvhu.fixedwidth.parser;

import java.util.List;

/**
 * The outcome of a parse operation run in collect-all-errors mode
 * (see {@link FixedParser#collectErrors()}).
 *
 * <p>Even when errors are present the result may contain a partially-populated
 * object; fields that could not be parsed are left at their Java default value.
 *
 * @param <T> the target object type
 * @author Giao Ho
 * @since 2.0.0
 */
public interface ParseResult<T> {

    /** The (possibly partial) parsed object. Never {@code null}. */
    T getValue();

    /** {@code true} if at least one error was collected. */
    boolean hasErrors();

    /** All errors collected during the parse, in encounter order. */
    List<ParseError> getErrors();
}
