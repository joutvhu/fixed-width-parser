package com.joutvhu.fixedwidth.parser.module;

import com.joutvhu.fixedwidth.parser.annotation.FixedHandler;
import com.joutvhu.fixedwidth.parser.convert.Hook;
import com.joutvhu.fixedwidth.parser.convert.ModuleHook;
import com.joutvhu.fixedwidth.parser.exception.NoHookFoundException;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.util.IgnoreError;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Fixed module — manages module-hooks and dispatches annotation-hooks.
 *
 * <p>Replaces the old reader/writer registry with a unified {@link Hook} model:
 * <ul>
 *   <li>Module-hooks ({@link ModuleHook}) are registered as singletons.</li>
 *   <li>Annotation-hooks are discovered at dispatch time via {@link FixedHandler}
 *       and instantiated per-invocation.</li>
 * </ul>
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public abstract class FixedModule {

    private static final Logger log = Logger.getLogger(FixedModule.class.getName());

    /**
     * Ordered set of registered module-hook classes (registration order = priority).
     */
    private Set<Class<? extends ModuleHook>> hooks = new LinkedHashSet<>();

    /**
     * Singleton instances, keyed by hook class.
     */
    private Map<Class<? extends ModuleHook>, ModuleHook> hookInstances = new LinkedHashMap<>();

    protected FixedModule() {
    }

    @SuppressWarnings("unchecked")
    public FixedModule(Class<?>... classes) {
        for (Class<?> c : classes) {
            if (ModuleHook.class.isAssignableFrom(c)) {
                registerHook((Class<? extends ModuleHook>) c);
            }
        }
    }

    // ── Hook registry ─────────────────────────────────────────────────────────

    /**
     * Registers a module-hook class and creates its singleton instance immediately.
     */
    public final void registerHook(Class<? extends ModuleHook> hookClass) {
        if (hooks.contains(hookClass)) return;
        ModuleHook instance = IgnoreError.execute(() -> {
            Constructor<? extends ModuleHook> ctor;
            try {
                ctor = hookClass.getConstructor();
            } catch (NoSuchMethodException e) {
                ctor = hookClass.getDeclaredConstructor();
                ctor.setAccessible(true);
            }
            return ctor.newInstance();
        });
        if (instance == null) {
            log.warning("Could not instantiate ModuleHook: " + hookClass.getName() +
                ". Ensure it has a public no-arg constructor.");
            return;
        }
        hooks.add(hookClass);
        hookInstances.put(hookClass, instance);
    }

    // ── Module composition ────────────────────────────────────────────────────

    /**
     * Merges another module into this one. The other module's hooks take priority
     * (placed before this module's hooks).
     */
    public FixedModule merge(FixedModule module) {
        // New module hooks go first (higher priority)
        Set<Class<? extends ModuleHook>> merged = new LinkedHashSet<>(module.hooks);
        merged.addAll(this.hooks);
        this.hooks = merged;

        // Merge singleton instances — don't recreate existing ones
        for (Map.Entry<Class<? extends ModuleHook>, ModuleHook> entry
            : module.hookInstances.entrySet()) {
            this.hookInstances.putIfAbsent(entry.getKey(), entry.getValue());
        }
        return this;
    }

    // ── Hook dispatch ─────────────────────────────────────────────────────────

    /**
     * Central dispatch point — replaces invokeAnnotationHandlers() + createReaderBy() + createWriterBy().
     *
     * <p>Order:
     * <ol>
     *   <li>Annotation-hooks (per-invocation, in annotation declaration order)</li>
     *   <li>First module-hook where {@code supports()} returns {@code true} (singleton)</li>
     * </ol>
     *
     * <p>If {@code ctx.isSkipField()} after any hook → stop immediately.
     * If no module-hook supports the field → throw {@link NoHookFoundException}.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public final void invokeHooks(FixedTypeInfo info, ParseContext ctx) {
        if (ctx == null) return;
        Phase currentPhase = ctx.getPhase();

        // ── Step 1: Annotation-hooks ──────────────────────────────────────────
        for (Annotation annotation : collectAnnotations(info)) {
            FixedHandler fixedHandler = annotation.annotationType()
                .getAnnotation(FixedHandler.class);
            if (fixedHandler == null) continue;

            Class<? extends Hook> hookClass = fixedHandler.value();
            Hook hook = IgnoreError.execute(() -> {
                Constructor<? extends Hook> ctor;
                try {
                    ctor = hookClass.getConstructor();
                } catch (NoSuchMethodException e) {
                    ctor = hookClass.getDeclaredConstructor();
                    ctor.setAccessible(true);
                }
                return ctor.newInstance();
            });
            if (hook == null) {
                log.warning("Could not instantiate annotation-hook: " + hookClass.getName() +
                    ". Ensure it has a public no-arg constructor.");
                continue;
            }

            if (hook.getSupportedPhases().contains(currentPhase)) {
                hook.handle(info, ctx);
                if (ctx.isSkipField()) return;
            }
        }

        // ── Step 2: Module-hook (first-match) ────────────────────────────────
        ModuleHook selected = null;
        for (Class<? extends ModuleHook> hookClass : hooks) {
            ModuleHook candidate = hookInstances.get(hookClass);
            if (candidate != null && candidate.supports(info)) {
                selected = candidate;
                break;
            }
        }

        if (selected == null) {
            throw new NoHookFoundException(info);
        }

        if (selected.getSupportedPhases().contains(currentPhase)) {
            selected.handle(info, ctx);
        }
    }

    // ── Annotation collection (kept for dispatch) ─────────────────────────────

    /**
     * Collects all annotations relevant to the given {@link FixedTypeInfo}:
     * field annotations, annotated-type annotations, and class-level annotations.
     * Also unwraps composed annotations (meta-annotations) up to depth 3.
     */
    public List<Annotation> collectAnnotations(FixedTypeInfo info) {
        List<Annotation> result = new ArrayList<>();
        if (info.getField() != null) {
            for (Annotation a : info.getField().getAnnotations()) {
                result.add(a);
                collectComposedAnnotations(a, result, 1);
            }
        }
        if (info.getAnnotatedType() != null) {
            for (Annotation a : info.getAnnotatedType().getAnnotations()) {
                result.add(a);
                collectComposedAnnotations(a, result, 1);
            }
        }
        if (info.getType() != null) {
            for (Annotation a : info.getType().getAnnotations()) {
                result.add(a);
                collectComposedAnnotations(a, result, 1);
            }
        }
        return result;
    }

    private void collectComposedAnnotations(Annotation annotation, List<Annotation> result, int depth) {
        if (depth > 3) return;
        for (Annotation meta : annotation.annotationType().getAnnotations()) {
            if (meta.annotationType().getName().startsWith("java.lang.annotation.")) continue;
            result.add(meta);
            collectComposedAnnotations(meta, result, depth + 1);
        }
    }

    // ── Accessors for testing ─────────────────────────────────────────────────

    /**
     * Returns an unmodifiable view of the registered hook classes (in priority order).
     */
    public Set<Class<? extends ModuleHook>> getHooks() {
        return java.util.Collections.unmodifiableSet(hooks);
    }

    /**
     * Returns the singleton instance for the given hook class, or null if not registered.
     */
    public ModuleHook getHookInstance(Class<? extends ModuleHook> hookClass) {
        return hookInstances.get(hookClass);
    }
}
