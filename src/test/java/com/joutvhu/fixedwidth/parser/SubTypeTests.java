package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for @FixedObject subtype detection.
 * Covers: oneOf selection, matchWith selection, defaultSubType fallback,
 * and selection by start+length position.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubTypeTests {

    private FixedParser parser;

    @BeforeAll
    void beforeAll() {
        parser = FixedParser.parser();
    }

    // -------------------------------------------------------------------------
    // Models — public static required for FixedHelper.newInstanceOf()
    // -------------------------------------------------------------------------

    @FixedObject(
            subTypes = {
                    @FixedObject.Type(value = TypeA.class, prop = "kind", oneOf = {"A"}),
                    @FixedObject.Type(value = TypeB.class, prop = "kind", matchWith = "^B.*$")
            },
            defaultSubType = TypeC.class
    )
    @Getter
    @Setter
    @NoArgsConstructor
    public static abstract class BaseType {
        @FixedField(length = 1)
        private String kind;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class TypeA extends BaseType {
        @FixedField(start = 1, length = 4)
        private String nameA;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class TypeB extends BaseType {
        @FixedField(start = 1, length = 4)
        private String nameB;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class TypeC extends BaseType {
        @FixedField(start = 1, length = 4)
        private String nameC;
    }

    // Selection by start+length (not prop)
    @FixedObject(
            subTypes = {
                    @FixedObject.Type(value = PositionTypeX.class, start = 0, length = 1, oneOf = {"X"}),
                    @FixedObject.Type(value = PositionTypeY.class, start = 0, length = 1, oneOf = {"Y"})
            }
    )
    @Getter
    @Setter
    @NoArgsConstructor
    public static abstract class PositionBase {
        @FixedField(length = 1)
        private String marker;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class PositionTypeX extends PositionBase {
        @FixedField(start = 1, length = 3)
        private String data;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class PositionTypeY extends PositionBase {
        @FixedField(start = 1, length = 3)
        private String data;
    }

    // -------------------------------------------------------------------------
    // oneOf selection
    // -------------------------------------------------------------------------

    @Test
    void detectSubType_oneOf_selectsTypeA() {
        BaseType result = parser.parse(BaseType.class, "Ahello");
        assertInstanceOf(TypeA.class, result);
        assertEquals("hell", ((TypeA) result).getNameA());
    }

    // -------------------------------------------------------------------------
    // matchWith selection
    // -------------------------------------------------------------------------

    @Test
    void detectSubType_matchWith_selectsTypeB() {
        BaseType result = parser.parse(BaseType.class, "Bworld");
        assertInstanceOf(TypeB.class, result);
        assertEquals("worl", ((TypeB) result).getNameB());
    }

    // -------------------------------------------------------------------------
    // defaultSubType fallback
    // -------------------------------------------------------------------------

    @Test
    void detectSubType_noMatch_usesDefault() {
        BaseType result = parser.parse(BaseType.class, "Zfallback");
        assertInstanceOf(TypeC.class, result);
        assertEquals("fall", ((TypeC) result).getNameC());
    }

    // -------------------------------------------------------------------------
    // Selection by start+length position
    // -------------------------------------------------------------------------

    @Test
    void detectSubType_byPosition_selectsX() {
        PositionBase result = parser.parse(PositionBase.class, "Xabc");
        assertInstanceOf(PositionTypeX.class, result);
    }

    @Test
    void detectSubType_byPosition_selectsY() {
        PositionBase result = parser.parse(PositionBase.class, "Yxyz");
        assertInstanceOf(PositionTypeY.class, result);
    }

    // -------------------------------------------------------------------------
    // Export preserves concrete type
    // -------------------------------------------------------------------------

    @Test
    void exportSubType_typeA_writesCorrectly() {
        TypeA a = new TypeA();
        a.setKind("A");
        a.setNameA("test");
        String line = parser.export(a);
        assertEquals("Atest", line);
    }

    @Test
    void exportSubType_typeB_writesCorrectly() {
        TypeB b = new TypeB();
        b.setKind("B");
        b.setNameB("data");
        String line = parser.export(b);
        assertEquals("Bdata", line);
    }

    // -------------------------------------------------------------------------
    // Round-trip
    // -------------------------------------------------------------------------

    @Test
    void roundTrip_typeA() {
        TypeA original = new TypeA();
        original.setKind("A");
        original.setNameA("abcd");
        String line = parser.export(original);
        BaseType parsed = parser.parse(BaseType.class, line);
        assertInstanceOf(TypeA.class, parsed);
        assertEquals("abcd", ((TypeA) parsed).getNameA());
    }
}
