package com.joutvhu.fixedwidth.parser.model;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedParam;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Top-level model classes used by HookSystemTest for round-trip property tests.
 * Kept in the model package so FixedHelper can instantiate them via reflection.
 */
public final class RoundTripModels {

    private RoundTripModels() {
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StringRTModel {
        @FixedField(length = 10)
        String value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BooleanRTModel {
        @FixedField(length = 1)
        Boolean value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LongRTModel {
        @FixedField(length = 10)
        Long value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntegerRTModel {
        @FixedField(length = 5)
        Integer value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoubleRTModel {
        @FixedField(length = 10)
        Double value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateRTModel {
        @FixedFormat(format = "yyyy-MM-dd")
        @FixedField(length = 10)
        LocalDate value;
    }

    public enum Color {RED, GREEN, BLUE}

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnumRTModel {
        @FixedField(length = 5)
        Color value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UUIDRTModel {
        @FixedField(length = 36)
        UUID value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionalRTModel {
        @FixedField(length = 5)
        Optional<String> value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectionRTModel {
        @FixedField(length = 9)
        List<@FixedParam(length = 3) String> value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MapRTModel {
        @FixedField(length = 12)
        Map<@FixedParam(length = 2) String, @FixedParam(length = 4) String> value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NestedRTModel {
        @FixedField(length = 10)
        StringRTModel nested;
    }
}
