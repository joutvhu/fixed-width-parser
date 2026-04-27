package com.joutvhu.fixedwidth.parser.convert.handler;

import com.joutvhu.fixedwidth.parser.annotation.FixedEncoding;
import com.joutvhu.fixedwidth.parser.convert.AnnotationHandler;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Re-encodes a field's raw string using the charset declared by {@link FixedEncoding}.
 *
 * <p>On READ ({@link Phase#READ_AFTER_CUT}): converts the raw bytes from the
 * declared charset back to a Java {@code String} (UTF-16).
 *
 * <p>On WRITE ({@link Phase#WRITE_AFTER_CONVERT}): converts the Java string to
 * bytes in the declared charset and back to a {@code String} so that the
 * resulting bytes match the target encoding when the output is written.
 *
 * <p>If the declared charset is the same as the platform default, this handler
 * is a no-op.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class EncodingHandler implements AnnotationHandler<FixedEncoding> {

    @Override
    public Set<Phase> getPhases(FixedEncoding annotation) {
        return new HashSet<>(Arrays.asList(
            Phase.READ_AFTER_CUT,
            Phase.WRITE_AFTER_CONVERT));
    }

    @Override
    public void handle(FixedEncoding annotation, FixedTypeInfo info, ParseContext ctx) {
        Charset charset = resolveCharset(annotation.value(), info);
        if (charset == null) return;

        if (ctx.getPhase() == Phase.READ_AFTER_CUT) {
            handleRead(charset, ctx);
        } else {
            handleWrite(charset, ctx);
        }
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /**
     * Re-interprets the raw string bytes using the declared charset.
     * Updates {@code processedString} in the context so downstream handlers
     * and readers see the correctly decoded value.
     */
    private void handleRead(Charset charset, ParseContext ctx) {
        String raw = ctx.getRawString();
        if (raw == null || raw.isEmpty()) return;

        // Treat the raw string as if its bytes were encoded in the declared charset,
        // then decode back to a Java String (UTF-16).
        byte[] bytes = raw.getBytes(Charset.defaultCharset());
        String decoded = new String(bytes, charset);
        ctx.setProcessedString(decoded);
    }

    // ── WRITE ─────────────────────────────────────────────────────────────────

    /**
     * Converts the current string value to bytes in the declared charset and
     * back to a String, so the output bytes match the target encoding.
     */
    private void handleWrite(Charset charset, ParseContext ctx) {
        Object current = ctx.getCurrentValue();
        if (!(current instanceof String)) return;

        String value = (String) current;
        if (value.isEmpty()) return;

        byte[] bytes = value.getBytes(charset);
        String reEncoded = new String(bytes, Charset.defaultCharset());
        ctx.setCurrentValue(reEncoded);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Charset resolveCharset(String charsetName, FixedTypeInfo info) {
        try {
            Charset declared = Charset.forName(charsetName);
            // No-op if same as platform default
            if (declared.equals(Charset.defaultCharset())) return null;
            return declared;
        } catch (UnsupportedCharsetException e) {
            throw new com.joutvhu.fixedwidth.parser.exception.FixedParserException(
                "Unknown charset '" + charsetName + "' declared on field: " + info.getName(), e);
        }
    }
}
