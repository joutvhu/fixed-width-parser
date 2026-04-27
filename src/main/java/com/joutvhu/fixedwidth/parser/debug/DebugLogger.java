package com.joutvhu.fixedwidth.parser.debug;

import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logs parse/export pipeline events at each {@link Phase} when debug mode is enabled.
 *
 * <p>Each log line shows: direction, phase, field path, raw value, and processed/converted value.
 *
 * <p>Usage:
 * <pre>{@code
 * Logger log = Logger.getLogger(MyClass.class.getName());
 * FixedParser.parser().debug(log).parse(Product.class, line);
 * }</pre>
 *
 * @author Giao Ho
 * @since 1.7.0
 */
public class DebugLogger {

    private final Logger logger;

    public DebugLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Called at each phase transition. Logs field path, phase, raw value,
     * processed string, and current converted value.
     */
    public void log(ParseContext ctx) {
        if (!logger.isLoggable(Level.FINE)) return;

        Phase phase = ctx.getPhase();
        String fieldPath = buildFieldPath(ctx);
        String raw = ctx.getRawString();
        String processed = ctx.getProcessedString();
        Object current = ctx.getCurrentValue();

        logger.fine(String.format("[%s] field=%s raw=%s processed=%s value=%s",
            phase,
            fieldPath,
            quote(raw),
            quote(processed),
            current));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String buildFieldPath(ParseContext ctx) {
        if (ctx.currentFrame() == null) return "<root>";
        return ctx.currentFrame().getTypeInfo().getName();
    }

    private static String quote(String s) {
        if (s == null) return "null";
        return "\"" + s + "\"";
    }
}
