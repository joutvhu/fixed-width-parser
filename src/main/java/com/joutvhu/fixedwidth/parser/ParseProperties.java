package com.joutvhu.fixedwidth.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable set of per-call properties that override the parser-level config
 * for a single {@link FixedParser#parse} or {@link FixedParser#export} call.
 *
 * <p>Create instances via the factory methods or the {@link Builder}:
 * <pre>{@code
 * ParseProperties props = ParseProperties.of("locale", Locale.US, "timezone", ZoneId.of("UTC"));
 *
 * ParseProperties props = ParseProperties.builder()
 *     .set("locale", Locale.JAPAN)
 *     .build();
 * }</pre>
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public final class ParseProperties {

    private final Map<String, Object> map;

    private ParseProperties(Map<String, Object> map) {
        this.map = Collections.unmodifiableMap(new HashMap<>(map));
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    /**
     * Creates an empty {@code ParseProperties}.
     */
    public static ParseProperties empty() {
        return new ParseProperties(Collections.emptyMap());
    }

    /**
     * Creates a {@code ParseProperties} with a single entry.
     */
    public static ParseProperties of(String k1, Object v1) {
        Map<String, Object> m = new HashMap<>();
        m.put(k1, v1);
        return new ParseProperties(m);
    }

    /**
     * Creates a {@code ParseProperties} with two entries.
     */
    public static ParseProperties of(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> m = new HashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        return new ParseProperties(m);
    }

    /**
     * Creates a {@code ParseProperties} from an existing map.
     */
    public static ParseProperties of(Map<String, Object> map) {
        return new ParseProperties(map);
    }

    /**
     * Returns a new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    // ── Access ────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        return (T) map.get(key);
    }

    public boolean has(String key) {
        return map.containsKey(key);
    }

    /**
     * Returns an unmodifiable view of the underlying map.
     */
    public Map<String, Object> asMap() {
        return map;
    }

    /**
     * Always throws {@link UnsupportedOperationException} — instances are immutable.
     */
    public ParseProperties set(String key, Object value) {
        throw new UnsupportedOperationException("ParseProperties is immutable.");
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static final class Builder {
        private final Map<String, Object> map = new HashMap<>();

        public Builder set(String key, Object value) {
            map.put(key, value);
            return this;
        }

        public ParseProperties build() {
            return new ParseProperties(map);
        }
    }
}
