package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.util.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Stream;

/**
 * Read line by line from {@link InputStream}
 *
 * @author Giao Ho
 * @see BufferedReader
 * @since 1.1.0
 */
public class StringLineReader {
    private String encoding = "UTF-8";
    private InputStream input;
    private BufferedReader reader;

    public StringLineReader(InputStream input) {
        Assert.notNull(input, "InputStream must not be null!");

        this.input = input;
        this.afterInit();
    }

    public StringLineReader(InputStream input, String encoding) {
        Assert.notNull(input, "InputStream must not be null!");

        if (encoding != null)
            this.encoding = encoding;
        this.input = input;
        this.afterInit();
    }

    private void afterInit() {
        InputStreamReader inputStreamReader = new InputStreamReader(input, this.encoding == null ?
                Charset.defaultCharset() : Charset.forName(this.encoding));
        this.reader = new BufferedReader(inputStreamReader);
    }

    private Stream<String> stream() {
        return reader.lines();
    }

    /**
     * Reads a next line of text.
     *
     * @return A String containing the contents of the line, not including
     * any line-termination characters, or null if the end of the stream has been reached
     * @throws IOException If an I/O error occurs
     */
    public String next() throws IOException {
        return reader.readLine();
    }

    /**
     * Close the {@link BufferedReader}
     *
     * @throws IOException If an I/O error occurs
     */
    public void close() throws IOException {
        reader.close();
    }
}
