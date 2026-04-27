package com.joutvhu.fixedwidth.parser.core;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.constraint.FixedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Year;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExtendedTypeTest {

    public enum Status {
        ACTIVE, INACTIVE
    }

    @Getter
    @AllArgsConstructor
    public enum Gender {
        MALE("M"), FEMALE("F");
        private final String code;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FixedObject
    public static class ExtendedModel {
        @FixedField(label = "Status", start = 0, length = 10)
        private Status status;

        @FixedField(label = "Gender", start = 10, length = 1)
        @FixedEnum(property = "code")
        private Gender gender;

        @FixedField(label = "Year", start = 11, length = 4)
        @FixedFormat(format = "yyyy")
        private Year year;

        @FixedField(label = "YearMonth", start = 15, length = 6)
        @FixedFormat(format = "yyyyMM")
        private YearMonth yearMonth;

        @FixedField(label = "UUID", start = 21, length = 36)
        private UUID uuid;

        @FixedField(label = "Optional String", start = 57, length = 5)
        private Optional<String> optString;

        @FixedField(label = "Optional Integer", start = 62, length = 3)
        private Optional<Integer> optInt;
    }

    @Test
    public void testExtendedTypesRead() {
        String uuidStr = "550e8400-e29b-41d4-a716-446655440000";
        String line = "ACTIVE    F2023202312" + uuidStr + "HELLO123";

        ExtendedModel model = FixedParser.parser().parse(ExtendedModel.class, line);

        Assertions.assertEquals(Status.ACTIVE, model.getStatus());
        Assertions.assertEquals(Gender.FEMALE, model.getGender());
        Assertions.assertEquals(Year.of(2023), model.getYear());
        Assertions.assertEquals(YearMonth.of(2023, 12), model.getYearMonth());
        Assertions.assertEquals(UUID.fromString(uuidStr), model.getUuid());
        Assertions.assertTrue(model.getOptString().isPresent());
        Assertions.assertEquals("HELLO", model.getOptString().get());
        Assertions.assertTrue(model.getOptInt().isPresent());
        Assertions.assertEquals(123, model.getOptInt().get());
    }

    @Test
    public void testExtendedTypesWrite() {
        UUID uuid = UUID.randomUUID();
        ExtendedModel model = new ExtendedModel();
        model.setStatus(Status.INACTIVE);
        model.setGender(Gender.MALE);
        model.setYear(Year.of(2024));
        model.setYearMonth(YearMonth.of(2024, 5));
        model.setUuid(uuid);
        model.setOptString(Optional.of("ABC"));
        model.setOptInt(Optional.empty());

        String line = FixedParser.parser().export(model);

        Assertions.assertTrue(line.startsWith("INACTIVE  M2024202405" + uuid.toString()));
        Assertions.assertEquals("ABC  ", line.substring(57, 62));
        Assertions.assertEquals("   ", line.substring(62, 65));
    }
}
