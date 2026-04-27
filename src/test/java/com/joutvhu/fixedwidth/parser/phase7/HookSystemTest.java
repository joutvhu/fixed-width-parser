package com.joutvhu.fixedwidth.parser.phase7;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.convert.ModuleHook;
import com.joutvhu.fixedwidth.parser.exception.NoHookFoundException;
import com.joutvhu.fixedwidth.parser.model.CollectionModel;
import com.joutvhu.fixedwidth.parser.model.MultiFieldModel;
import com.joutvhu.fixedwidth.parser.model.NestedModel;
import com.joutvhu.fixedwidth.parser.model.SimpleStringModel;
import com.joutvhu.fixedwidth.parser.model.SubTypeModel;
import com.joutvhu.fixedwidth.parser.model.UnsupportedFieldModel;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tasks 13, 13.1, 13.2 — Integration tests, round-trip property tests,
 * and module merge priority tests for the unified hook system.
 *
 * <p>Requirements: 2.4, 2.5, 3.3, 7.4, 8.1–8.6, 9.5, 10.1
 */
class HookSystemTest {

    private final FixedParser parser = FixedParser.parser();

    // ── Task 13: Integration tests ────────────────────────────────────────────

    @Test
    void simpleStringModel_parse() {
        SimpleStringModel m = parser.parse(SimpleStringModel.class, "hello     ");
        assertNotNull(m);
        assertEquals("hello     ", m.getValue());
    }

    @Test
    void simpleStringModel_export() {
        assertEquals("hello     ", parser.export(new SimpleStringModel("hello")));
    }

    @Test
    void multiFieldModel_parse() {
        MultiFieldModel m = parser.parse(MultiFieldModel.class, "040hello     Y2024-01-15");
        assertNotNull(m);
        assertEquals(40L, m.getId());
        assertEquals("hello     ", m.getName());
        assertTrue(m.getActive());
        assertEquals(LocalDate.of(2024, 1, 15), m.getDate());
    }

    @Test
    void nestedModel_parse() {
        // code=3, address=start=3,length=15 → total 18 chars
        // address: city=length=5, street=start=5,length=10
        NestedModel m = parser.parse(NestedModel.class, "ABCPARISMAIN ST   ");
        assertNotNull(m);
        assertEquals("ABC", m.getCode());
        assertNotNull(m.getAddress());
        assertEquals("PARIS", m.getAddress().getCity());
        assertEquals("MAIN ST   ", m.getAddress().getStreet());
    }

    @Test
    void subTypeModel_typeA() {
        SubTypeModel m = parser.parse(SubTypeModel.class, "ADATA");
        assertInstanceOf(SubTypeModel.TypeA.class, m);
        assertEquals("A", m.getKind());
        assertEquals("DATA", ((SubTypeModel.TypeA) m).getDataA());
    }

    @Test
    void subTypeModel_typeB() {
        SubTypeModel m = parser.parse(SubTypeModel.class, "BDATA");
        assertInstanceOf(SubTypeModel.TypeB.class, m);
        assertEquals("B", m.getKind());
        assertEquals("DATA", ((SubTypeModel.TypeB) m).getDataB());
    }

    @Test
    void subTypeModel_typeC_default() {
        SubTypeModel m = parser.parse(SubTypeModel.class, "CDATA");
        assertInstanceOf(SubTypeModel.TypeC.class, m);
        assertEquals("C", m.getKind());
        assertEquals("DATA", ((SubTypeModel.TypeC) m).getDataC());
    }

    @Test
    void noHookFoundException_unsupportedType() {
        assertThrows(NoHookFoundException.class,
            () -> parser.parse(UnsupportedFieldModel.class, "RED  "));
    }

    @Test
    void annotationComposition_formatAnnotation_stillWorks() {
        // id=3, name=start=3,length=10, active=start=13,length=1, date=start=14,length=10
        // "001" + "test      " + "Y" + "2023-06-15" = 24 chars
        MultiFieldModel m = parser.parse(MultiFieldModel.class, "001test      Y2023-06-15");
        assertNotNull(m);
        assertEquals(LocalDate.of(2023, 6, 15), m.getDate());
        assertTrue(m.getActive());
    }

    // ── Task 13.1: Round-trip property tests ──────────────────────────────────

    @Test
    void roundTrip_string() {
        SimpleStringModel original = new SimpleStringModel("hello");
        String exported = parser.export(original);
        SimpleStringModel parsed = parser.parse(SimpleStringModel.class, exported);
        assertEquals(original.getValue(), parsed.getValue().stripTrailing());
    }

    @Test
    void roundTrip_multiField() {
        MultiFieldModel original = new MultiFieldModel(42L, "world     ", false, LocalDate.of(2023, 6, 15));
        String exported = parser.export(original);
        MultiFieldModel parsed = parser.parse(MultiFieldModel.class, exported);
        assertEquals(original.getId(), parsed.getId());
        assertEquals(original.getActive(), parsed.getActive());
        assertEquals(original.getDate(), parsed.getDate());
    }

    @Test
    void roundTrip_nested() {
        NestedModel original = new NestedModel("XYZ",
            new NestedModel.AddressModel("TOKYO", "SAKURA ST "));
        String exported = parser.export(original);
        NestedModel parsed = parser.parse(NestedModel.class, exported);
        assertEquals(original.getCode(), parsed.getCode());
        assertEquals(original.getAddress().getCity(), parsed.getAddress().getCity());
        assertEquals(original.getAddress().getStreet(), parsed.getAddress().getStreet());
    }

    @Test
    void roundTrip_collection() {
        CollectionModel original = new CollectionModel(
            Arrays.asList("AAA", "BBB", "CCC"),
            new LinkedHashMap<>(Map.of("AA", "BBCC", "DD", "EE  ")));
        String exported = parser.export(original);
        CollectionModel parsed = parser.parse(CollectionModel.class, exported);
        assertEquals(original.getItems(), parsed.getItems());
    }

    // ── Task 13.2: Module merge priority tests ────────────────────────────────

    static final List<String> mergeCallLog = new java.util.ArrayList<>();

    static class PriorityHookA implements ModuleHook {
        @Override
        public boolean supports(FixedTypeInfo info) {
            return String.class.equals(info.getType());
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
            mergeCallLog.add("A");
            ctx.setCurrentValue(ctx.getProcessedString());
        }
    }

    static class PriorityHookB implements ModuleHook {
        @Override
        public boolean supports(FixedTypeInfo info) {
            return String.class.equals(info.getType());
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
            mergeCallLog.add("B");
            ctx.setCurrentValue(ctx.getProcessedString());
        }
    }

    static class ModuleA extends FixedModule {
        ModuleA() {
            super(PriorityHookA.class);
        }
    }

    static class ModuleB extends FixedModule {
        ModuleB() {
            super(PriorityHookB.class);
        }
    }

    @Test
    void moduleMerge_newModuleHookCheckedFirst() {
        // FixedParser.parser().with(moduleA) → base has DefaultModule + moduleA merged
        // .with(moduleB) → moduleB hooks go before moduleA hooks
        // So PriorityHookB should be called (first-match wins)
        mergeCallLog.clear();

        // Build a module that has B before A (B added last = highest priority)
        ModuleA moduleA = new ModuleA();
        ModuleB moduleB = new ModuleB();
        // merge: moduleB hooks go first
        moduleA.merge(moduleB);

        // moduleA now has B first, then A
        Class<?>[] hookOrder = moduleA.getHooks().toArray(new Class[0]);
        assertEquals(PriorityHookB.class, hookOrder[0],
            "After merge, new module's hook (B) should come first");
        assertEquals(PriorityHookA.class, hookOrder[1]);
    }

    @Test
    void moduleMerge_singletonNotRecreated() {
        ModuleA moduleA = new ModuleA();
        ModuleHook originalInstance = moduleA.getHookInstance(PriorityHookA.class);
        assertNotNull(originalInstance);

        // Merge another module that also has PriorityHookA
        ModuleA anotherModuleA = new ModuleA();
        moduleA.merge(anotherModuleA);

        // The original instance must be preserved (putIfAbsent semantics)
        assertSame(originalInstance, moduleA.getHookInstance(PriorityHookA.class),
            "Singleton instance must not be recreated after merge");
    }

    @Test
    void moduleMerge_newModuleHooksAddedBeforeExisting() {
        ModuleA base = new ModuleA();
        ModuleB newModule = new ModuleB();

        base.merge(newModule);

        // After merge: B (from newModule) should be before A (from base)
        Class<?>[] order = base.getHooks().toArray(new Class[0]);
        assertEquals(2, order.length);
        assertEquals(PriorityHookB.class, order[0]);
        assertEquals(PriorityHookA.class, order[1]);
    }
}
