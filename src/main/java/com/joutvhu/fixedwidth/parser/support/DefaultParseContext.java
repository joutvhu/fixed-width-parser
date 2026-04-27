package com.joutvhu.fixedwidth.parser.support;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link ParseContext}.
 *
 * <p>Properties are stored in two maps:
 * <ul>
 *   <li>{@code globalProps} — lives for the entire session</li>
 *   <li>{@code scopedKeys} per frame — keys that should be removed on pop</li>
 * </ul>
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class DefaultParseContext implements ParseContext {

    private Phase currentPhase;
    private Object currentValue;
    private boolean skipField = false;

    private final Deque<DefaultContextFrame> stack = new ArrayDeque<>();
    private final Map<String, Object> globalProps = new HashMap<>();

    // Per-frame scoped key tracking: frame depth → set of scoped keys
    private final Deque<Set<String>> scopedKeyStack = new ArrayDeque<>();

    // Parser-level config (read-only reference, set by FixedParser)
    private final Map<String, Object> parserConfig;

    // Session-level properties (per-call overrides, set by FixedParser)
    private final Map<String, Object> sessionProps;

    public DefaultParseContext(
            Phase initialPhase,
            Map<String, Object> parserConfig,
            Map<String, Object> sessionProps) {
        this.currentPhase = initialPhase;
        this.parserConfig = parserConfig != null ? parserConfig : Collections.emptyMap();
        this.sessionProps = sessionProps != null ? sessionProps : Collections.emptyMap();
    }

    // ── Phase ────────────────────────────────────────────────────────────────

    @Override
    public Phase getPhase() { return currentPhase; }

    public void setPhase(Phase phase) { this.currentPhase = phase; }

    // ── Frame stack ──────────────────────────────────────────────────────────

    public void pushFrame(DefaultContextFrame frame) {
        stack.push(frame);
        scopedKeyStack.push(new HashSet<>());
    }

    public DefaultContextFrame popFrame() {
        if (!scopedKeyStack.isEmpty()) {
            Set<String> scopedKeys = scopedKeyStack.pop();
            scopedKeys.forEach(globalProps::remove);
        }
        return stack.isEmpty() ? null : stack.pop();
    }

    /** Fires the phase hook registered on the owning strategy, if any. */
    public void firePhaseHook(Phase phase) {
        // Delegated to strategy — set phase on context first
        this.currentPhase = phase;
    }

    @Override
    public ContextFrame currentFrame() {
        return stack.isEmpty() ? null : stack.peek();
    }

    @Override
    public ContextFrame parentFrame() {
        if (stack.size() < 2) return null;
        DefaultContextFrame[] arr = stack.toArray(new DefaultContextFrame[0]);
        return arr[1]; // index 1 = second from top
    }

    @Override
    public List<ContextFrame> frameStack() {
        List<ContextFrame> list = new ArrayList<>(stack);
        Collections.reverse(list); // root first
        return Collections.unmodifiableList(list);
    }

    // ── Current value ────────────────────────────────────────────────────────

    @Override
    public Object getCurrentValue() { return currentValue; }

    @Override
    public void setCurrentValue(Object value) { this.currentValue = value; }

    @Override
    public void skipCurrentField() { this.skipField = true; }

    public boolean isSkipField() { return skipField; }

    public void resetSkipField() { this.skipField = false; }

    // ── Properties ───────────────────────────────────────────────────────────

    @Override
    public void put(String key, Object value) { putGlobal(key, value); }

    @Override
    public void putGlobal(String key, Object value) { globalProps.put(key, value); }

    @Override
    public void putScoped(String key, Object value) {
        globalProps.put(key, value);
        if (!scopedKeyStack.isEmpty()) {
            scopedKeyStack.peek().add(key);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = globalProps.get(key);
        return value != null ? (T) value : null;
    }

    @Override
    public boolean has(String key) { return globalProps.containsKey(key); }

    @Override
    public void remove(String key) { globalProps.remove(key); }

    // ── Parser / session properties ──────────────────────────────────────────

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> type, T defaultValue) {
        // Priority: session → parser config → default
        if (sessionProps.containsKey(key)) return (T) sessionProps.get(key);
        if (parserConfig.containsKey(key)) return (T) parserConfig.get(key);
        return defaultValue;
    }

    @Override
    public <T> T requireProperty(String key, Class<T> type) {
        T value = getProperty(key, type, null);
        if (value == null)
            throw new IllegalStateException(
                    "Required parser property '" + key + "' not found.");
        return value;
    }
}
