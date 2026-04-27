package com.joutvhu.fixedwidth.parser.model;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Polymorphic model: used to test subtype detection.
 * TypeA when kind="A", TypeB when kind="B", TypeC is default.
 */
@FixedObject(
    subTypes = {
        @FixedObject.Type(value = SubTypeModel.TypeA.class, prop = "kind", oneOf = {"A"}),
        @FixedObject.Type(value = SubTypeModel.TypeB.class, prop = "kind", matchWith = "^B.*$")
    },
    defaultSubType = SubTypeModel.TypeC.class
)
@Getter
@Setter
@NoArgsConstructor
public abstract class SubTypeModel {
    @FixedField(length = 1)
    private String kind;

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class TypeA extends SubTypeModel {
        @FixedField(start = 1, length = 4)
        private String dataA;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class TypeB extends SubTypeModel {
        @FixedField(start = 1, length = 4)
        private String dataB;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class TypeC extends SubTypeModel {
        @FixedField(start = 1, length = 4)
        private String dataC;
    }
}
