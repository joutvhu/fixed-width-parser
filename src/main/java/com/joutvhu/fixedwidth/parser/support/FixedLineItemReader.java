package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.util.IgnoreError;

import java.io.IOException;

/**
 * Read line by line from {@link StringLineReader}
 * Using {@link FixedParseStrategy} and {@link FixedTypeInfo} to parse the line
 *
 * @author Giao Ho
 * @since 1.1.0
 */
public class FixedLineItemReader implements ItemReader<Object> {
    private StringLineReader stringLineReader;
    private FixedParseStrategy strategy;
    private FixedTypeInfo fixedTypeInfo;

    private boolean hadNext = true;
    private String line = null;

    public FixedLineItemReader(StringLineReader stringReader, FixedParseStrategy strategy, FixedTypeInfo fixedTypeInfo) {
        this.stringLineReader = stringReader;
        this.strategy = strategy;
        this.fixedTypeInfo = fixedTypeInfo;
    }

    /**
     * Parse next line
     *
     * @return item object
     */
    @Override
    public Object next() {
        if (hasNext()) {
            StringAssembler stringAssembler = FixedStringAssembler.of(line);
            this.line = null;
            return strategy.read(fixedTypeInfo, stringAssembler);
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        if (hadNext && line == null) {
            this.line = IgnoreError.execute(stringLineReader::next);
            this.hadNext = line != null;
        }
        return hadNext;
    }

    @Override
    public void close() throws IOException {
        stringLineReader.close();
    }
}
