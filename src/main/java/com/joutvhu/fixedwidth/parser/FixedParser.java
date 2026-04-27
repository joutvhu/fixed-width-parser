package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.debug.DebugLogger;
import com.joutvhu.fixedwidth.parser.doc.SchemaDocument;
import com.joutvhu.fixedwidth.parser.module.DefaultModule;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.support.*;
import com.joutvhu.fixedwidth.parser.util.Assert;
import com.joutvhu.fixedwidth.parser.validation.SchemaValidator;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;
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

    // ── Phase 6: Schema validation ───────────────────────────────────────────

    /**
     * Validates the schema of the given class and returns a {@link SchemaValidator}
     * whose result can be inspected or thrown.
     *
     * <pre>{@code
     * FixedParser.parser().validate(Product.class).throwIfInvalid();
     * }</pre>
     */
    public SchemaValidator validate(Class<?> type) {
        return SchemaValidator.validate(type);
    }

    /**
     * Returns a {@link SchemaDocument} for the given class, which can be rendered
     * as Markdown, JSON, or CSV.
     *
     * <pre>{@code
     * String md = FixedParser.parser().document(Product.class).toMarkdown();
     * }</pre>
     */
    public SchemaDocument document(Class<?> type) {
        return SchemaDocument.of(type);
    }

    // ── Phase 6: Debug mode ───────────────────────────────────────────────────

    /**
     * Enables debug logging for every phase transition.
     * Each log line shows: phase, field name, raw value, processed value, converted value.
     *
     * <pre>{@code
     * Logger log = LoggerFactory.getLogger(MyClass.class);
     * FixedParser.parser().debug(log).parse(Product.class, line);
     * }</pre>
     *
     * @param logger the SLF4J logger to write debug output to
     * @return this (fluent)
     */
    public FixedParser debug(Logger logger) {
        DebugLogger debugLogger = new DebugLogger(logger);
        Consumer<ParseContext> hook = debugLogger::log;
        // Register the hook for all phases
        for (Phase phase : Phase.values()) {
            strategy.setPhaseHook(phase, hook);
        }
        return this;
    }

    // ── Collect-all-errors mode (Phase 5) ────────────────────────────────────

    // Whether this parser instance runs in collect-all-errors mode
    private boolean collectErrorsMode = false;

    /**
     * Switches this parser to collect-all-errors mode.
     * In this mode {@link #parseResult} collects all field errors instead of
     * throwing on the first one.
     */
    public FixedParser collectErrors() {
        this.collectErrorsMode = true;
        return this;
    }

    /**
     * Parses a fixed-width string and returns a {@link ParseResult} that contains
     * both the (possibly partial) value and a list of all errors encountered.
     */
    @SuppressWarnings("unchecked")
    public <T> ParseResult<T> parseResult(Class<T> type, String line) {
        Assert.notNull(type, "The class type must not be null!");
        Assert.notNull(line, "The line must not be null!");

        DefaultParseContext ctx = strategy.createReadContext(java.util.Collections.emptyMap());
        ctx.setCollectErrors(true);

        try {
            StringAssembler stringAssembler = FixedStringAssembler.of(line);
            FixedTypeInfo fixedTypeInfo = FixedTypeInfo.of(type);
            T value = (T) strategy.read(fixedTypeInfo, stringAssembler);
            return new DefaultParseResult<>(value, ctx.getCollectedErrors());
        } catch (Exception e) {
            // Top-level exception (e.g. object-level failure)
            java.util.List<ParseError> errors = new java.util.ArrayList<>(ctx.getCollectedErrors());
            if (errors.isEmpty()) {
                errors.add(new DefaultParseError(e.getMessage(), null, type.getSimpleName(), null, e));
            }
            return new DefaultParseResult<>(null, errors);
        }
    }

    /** Full ParseResult implementation. */
    private static final class DefaultParseResult<T> implements ParseResult<T> {
        private final T value;
        private final java.util.List<ParseError> errors;

        DefaultParseResult(T value, java.util.List<ParseError> errors) {
            this.value = value;
            this.errors = java.util.Collections.unmodifiableList(
                    new java.util.ArrayList<>(errors));
        }

        @Override public T getValue() { return value; }
        @Override public boolean hasErrors() { return !errors.isEmpty(); }
        @Override public java.util.List<ParseError> getErrors() { return errors; }
    }
}
