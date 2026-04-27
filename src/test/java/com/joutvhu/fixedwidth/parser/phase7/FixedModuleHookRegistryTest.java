package com.joutvhu.fixedwidth.parser.phase7;

import com.joutvhu.fixedwidth.parser.convert.ModuleHook;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Task 4.1 — Unit tests for FixedModule hook registry.
 * Requirements: 2.4, 2.5, 7.1, 7.2, 9.2, 9.5
 */
class FixedModuleHookRegistryTest {

    // ── Test hooks ────────────────────────────────────────────────────────────

    static class HookA implements ModuleHook {
        @Override
        public boolean supports(FixedTypeInfo info) {
            return true;
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
        }
    }

    static class HookB implements ModuleHook {
        @Override
        public boolean supports(FixedTypeInfo info) {
            return true;
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
        }
    }

    static class HookC implements ModuleHook {
        @Override
        public boolean supports(FixedTypeInfo info) {
            return true;
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
        }
    }

    // Concrete FixedModule for testing
    static class TestModule extends FixedModule {
        TestModule() {
            super();
        }

        TestModule(Class<?>... classes) {
            super(classes);
        }
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    void registerHook_createsSingletonInstanceImmediately() {
        TestModule module = new TestModule();
        module.registerHook(HookA.class);

        ModuleHook instance = module.getHookInstance(HookA.class);
        assertNotNull(instance);
        assertInstanceOf(HookA.class, instance);
    }

    @Test
    void registerHook_sameClassTwice_doesNotDuplicate() {
        TestModule module = new TestModule();
        module.registerHook(HookA.class);
        ModuleHook first = module.getHookInstance(HookA.class);
        module.registerHook(HookA.class);
        ModuleHook second = module.getHookInstance(HookA.class);

        assertSame(first, second);
        assertEquals(1, module.getHooks().size());
    }

    @Test
    void constructor_withHookClasses_registersAll() {
        TestModule module = new TestModule(HookA.class, HookB.class);

        assertEquals(2, module.getHooks().size());
        assertNotNull(module.getHookInstance(HookA.class));
        assertNotNull(module.getHookInstance(HookB.class));
    }

    @Test
    void merge_newModuleHooksPlacedFirst() {
        TestModule base = new TestModule(HookA.class);
        TestModule newModule = new TestModule(HookB.class);

        base.merge(newModule);

        // HookB (from newModule) should come before HookA (from base)
        Class<?>[] hookOrder = base.getHooks().toArray(new Class[0]);
        assertEquals(HookB.class, hookOrder[0]);
        assertEquals(HookA.class, hookOrder[1]);
    }

    @Test
    void merge_doesNotRecreateExistingInstances() {
        TestModule base = new TestModule(HookA.class);
        ModuleHook originalInstance = base.getHookInstance(HookA.class);

        TestModule newModule = new TestModule(HookA.class);
        base.merge(newModule);

        // The original instance should be preserved (putIfAbsent semantics)
        assertSame(originalInstance, base.getHookInstance(HookA.class));
    }

    @Test
    void merge_addsNewHookInstances() {
        TestModule base = new TestModule(HookA.class);
        TestModule newModule = new TestModule(HookB.class);

        base.merge(newModule);

        assertNotNull(base.getHookInstance(HookA.class));
        assertNotNull(base.getHookInstance(HookB.class));
    }
}
