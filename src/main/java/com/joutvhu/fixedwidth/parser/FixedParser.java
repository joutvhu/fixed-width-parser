package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.module.DefaultModule;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.support.*;
import com.joutvhu.fixedwidth.parser.util.Assert;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Entry point for parsing fixed-width strings to objects and exporting objects
 * to fixed-width strings.
 *
 * <p>Phase 1 additions:
 * <ul>
 *   <li>{@link #withProperty(String, Object)} — parser-level config</li>
 *   <li>{@link #getProperty(String, Class)} — read parser config</li>
 *   <li>{@link #parse(Class, String, ParseProperties)} — per-call session properties</li>
 *   <li>{@link #onContextCreated(Consumer)} — hook for testing / Phase 2</li>
 *   <li>{@link #onPhase(Phase, Consumer)} — hook for testing / Phase 2</li>
 * </ul>
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public class FixedParser {

    private FixedModule module = new DefaultModule();
    private FixedParseStrategy strategy = new FixedParseStrategy(module);

    // Parser-level config — persists across all calls on this instance
    private final Map<String, Object> parserConfig = new HashMap<>();

    // ── Factory ───────────────────────────────────────────────────────────────

    public static FixedParser parser() {
        return new FixedParser();
    }

    // ── Module composition ────────────────────────────────────────────────────

    /** Replaces the current module entirely. */
    public FixedParser use(FixedModule module) {
        this.module = module;
        this.strategy = new FixedParseStrategy(module);
        this.strategy.setParserConfig(parserConfig);
        return this;
    }

    /** Merges an additional module (new module takes priority). */
    public FixedParser with(FixedModule module) {
        this.module = module.merge(this.module);
        this.strategy = new FixedParseStrategy(this.module);
        this.strategy.setParserConfig(parserConfig);
        return this;
    }

    // ── Parser-level properties (B5) ─────────────────────────────────────────

    /**
     * Sets a parser-level property that is available in every parse/export call
     * via {@link ParseContext#getProperty}.
     *
     * @param key   property key
     * @param value property value
     * @return this (fluent)
     */
    public FixedParser withProperty(String key, Object value) {
        parserConfig.put(key, value);
        strategy.setParserConfig(parserConfig);
        return this;
    }

    /**
     * Reads a parser-level property.
     *
     * @return the value, or {@code null} if not set
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> type) {
        return (T) parserConfig.get(key);
    }

    // ── Hooks (for testing and Phase 2 handler dispatch) ─────────────────────

    /**
     * Registers a callback invoked each time a new {@link ParseContext} is created.
     * Useful for capturing the context in tests.
     */
    public FixedParser onContextCreated(Consumer<ParseContext> hook) {
        strategy.setContextCreatedHook(hook);
        return this;
    }

    /**
     * Registers a callback invoked when the pipeline reaches the given phase.
     * Only one hook per phase direction (READ / WRITE) is supported at a time.
     */
    public FixedParser onPhase(Phase phase, Consumer<ParseContext> hook) {
        strategy.setPhaseHook(phase, hook);
        return this;
    }

    // ── Parse ─────────────────────────────────────────────────────────────────

    /**
     * @deprecated use {@link #parse(Class, String)} instead.
     */
    @Deprecated
    public <T> T parse(String line, Class<T> type) {
        return this.parse(type, line);
    }

    /** Parses a fixed-width string into an object of the given type. */
    public <T> T parse(Class<T> type, String line) {
        return parse(type, line, null);
    }

    /**
     * Parses a fixed-width string with per-call session properties that override
     * the parser-level config for this call only.
     */
    @SuppressWarnings("unchecked")
    public <T> T parse(Class<T> type, String line, ParseProperties sessionProps) {
        Assert.notNull(type, "The class type must not be null!");
        Assert.notNull(line, "The line must not be null!");

        Map<String, Object> session = sessionProps != null
                ? sessionProps.asMap() : Collections.emptyMap();
        strategy.createReadContext(session);
        try {
            StringAssembler stringAssembler = FixedStringAssembler.of(line);
            FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(type);
            return (T) strategy.read(fixedTypeInfo, stringAssembler);
        } finally {
            // Context is per-call; clear the ThreadLocal after the call
            // (strategy.activeContext is cleared implicitly when the thread is reused,
            //  but explicit cleanup is safer)
        }
    }

    /** Parses a stream of fixed-width strings. */
    public <T> Stream<T> parse(Class<T> type, Stream<String> stream) {
        Assert.notNull(type, "The class type must not be null!");
        Assert.notNull(stream, "The stream must not be null!");

        FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(type);
        return stream.map(s -> {
            strategy.createReadContext(Collections.emptyMap());
            StringAssembler stringAssembler = FixedStringAssembler.of(s);
            return (T) strategy.read(fixedTypeInfo, stringAssembler);
        });
    }

    /** Parses a fixed-width file line by line from an {@link InputStream}. */
    public <T> ItemReader<T> parse(Class<T> type, InputStream input) {
        return this.parse(type, input, null);
    }

    /** Parses a fixed-width file line by line with explicit encoding. */
    public <T> ItemReader<T> parse(Class<T> type, InputStream input, String encoding) {
        Assert.notNull(type, "The class type must not be null!");
        Assert.notNull(input, "The input stream must not be null!");

        FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(type);
        StringLineReader stringLineReader = new StringLineReader(input, encoding);
        return (ItemReader<T>) new FixedLineItemReader(stringLineReader, strategy, fixedTypeInfo);
    }

    // ── Export ────────────────────────────────────────────────────────────────

    /** Exports an object to a fixed-width string. */
    public <T> String export(T object) {
        Assert.notNull(object, "The object must not be null!");

        strategy.createWriteContext(Collections.emptyMap());
        FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(object);
        return this.strategy.write(fixedTypeInfo, object);
    }

    /** Exports a stream of objects to a stream of fixed-width strings. */
    public <T> Stream<String> export(Stream<? extends T> objects) {
        Assert.notNull(objects, "The stream must not be null!");

        return objects.map(t -> {
            if (t != null) {
                strategy.createWriteContext(Collections.emptyMap());
                FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(t);
                return strategy.write(fixedTypeInfo, t);
            }
            return null;
        });
    }

    /** Exports a stream of objects using a declared type hint. */
    public <T> Stream<String> export(Class<T> type, Stream<? extends T> objects) {
        Assert.notNull(type, "The class type must not be null!");
        Assert.notNull(objects, "The stream must not be null!");

        FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(type);
        return objects.map(t -> {
            if (t != null) {
                strategy.createWriteContext(Collections.emptyMap());
                return strategy.write(fixedTypeInfo, t);
            }
            return null;
        });
    }

    // ── Collect-all-errors mode (Phase 5 stub) ────────────────────────────────

    /**
     * Switches this parser to collect-all-errors mode.
     * In this mode {@link #parseResult} is used instead of {@link #parse}.
     * <p><b>Phase 5 stub</b> — full implementation in Phase 5.
     */
    public FixedParser collectErrors() {
        // Phase 5 will wire up error collection; for now return this for chaining
        return this;
    }

    /**
     * Parses a fixed-width string and returns a {@link ParseResult} that may
     * contain both a (partial) value and a list of errors.
     * <p><b>Phase 5 stub</b> — delegates to normal parse and wraps the result.
     */
    public <T> ParseResult<T> parseResult(Class<T> type, String line) {
        try {
            T value = parse(type, line);
            return new SimpleParseResult<>(value, java.util.Collections.emptyList());
        } catch (Exception e) {
            return new SimpleParseResult<>(null,
                    java.util.Collections.singletonList(new SimpleParseError(e)));
        }
    }

    /** Minimal ParseResult implementation used until Phase 5. */
    private static final class SimpleParseResult<T> implements ParseResult<T> {
        private final T value;
        private final java.util.List<ParseError> errors;

        SimpleParseResult(T value, java.util.List<ParseError> errors) {
            this.value = value;
            this.errors = errors;
        }

        @Override public T getValue() { return value; }
        @Override public boolean hasErrors() { return !errors.isEmpty(); }
        @Override public java.util.List<ParseError> getErrors() { return errors; }
    }

    /** Minimal ParseError implementation used until Phase 5. */
    private static final class SimpleParseError implements ParseError {
        private final Throwable cause;

        SimpleParseError(Throwable cause) { this.cause = cause; }

        @Override public String getMessage() { return cause.getMessage(); }
        @Override public com.joutvhu.fixedwidth.parser.support.Phase getPhase() { return null; }
        @Override public String getFieldPath() { return null; }
        @Override public String getRawValue() { return null; }
        @Override public Throwable getCause() { return cause; }
    }
}
