package com.joutvhu.fixedwidth.parser.support;

import java.io.IOException;

/**
 * Read item by item
 *
 * @author Giao Ho
 * @since 1.1.0
 */
public interface ItemReader<T> {
    T next();

    boolean hasNext();

    void close() throws IOException;
}
