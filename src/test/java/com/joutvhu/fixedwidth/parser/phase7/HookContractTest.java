package com.joutvhu.fixedwidth.parser.phase7;

import com.joutvhu.fixedwidth.parser.convert.Hook;
import com.joutvhu.fixedwidth.parser.convert.ModuleHook;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Task 1.1 — Unit tests for Hook interface contract.
 * Requirements: 1.3, 1.4, 1.5, 1.6
 */
class HookContractTest {

    // Minimal Hook implementation for testing defaults
    static class NoOpHook implements Hook {
        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {}
    }

    // Hook that overrides getSupportedPhases
    static class ReadOnlyHook implements Hook {
        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {}

        @Override
        public Set<Phase> getSupportedPhases() {
            return EnumSet.of(Phase.READ_AFTER_TRANSFORM);
        }
    }

    // Minimal ModuleHook implementation
    static class NoOpModuleHook implements ModuleHook {
        @Override
        public boolean supports(FixedTypeInfo info) { return true; }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {}
    }

    @Test
    void defaultGetSupportedPhases_returnsAllPhases() {
        Hook hook = new NoOpHook();
        Set<Phase> phases = hook.getSupportedPhases();
        assertNotNull(phases);
        assertEquals(EnumSet.allOf(Phase.class), phases);
    }

    @Test
    void defaultGetDependencies_returnsEmptySet() {
        Hook hook = new NoOpHook();
        Set<String> deps = hook.getDependencies(null);
        assertNotNull(deps);
        assertTrue(deps.isEmpty());
    }

    @Test
    void overriddenGetSupportedPhases_returnsSubset() {
        Hook hook = new ReadOnlyHook();
        Set<Phase> phases = hook.getSupportedPhases();
        assertEquals(1, phases.size());
        assertTrue(phases.contains(Phase.READ_AFTER_TRANSFORM));
    }

    @Test
    void moduleHook_isAlsoHook() {
        ModuleHook hook = new NoOpModuleHook();
        assertInstanceOf(Hook.class, hook);
    }

    @Test
    void moduleHook_defaultGetSupportedPhases_returnsAllPhases() {
        ModuleHook hook = new NoOpModuleHook();
        assertEquals(EnumSet.allOf(Phase.class), hook.getSupportedPhases());
    }

    @Test
    void moduleHook_defaultGetDependencies_returnsEmptySet() {
        ModuleHook hook = new NoOpModuleHook();
        assertTrue(hook.getDependencies(null).isEmpty());
    }
}
