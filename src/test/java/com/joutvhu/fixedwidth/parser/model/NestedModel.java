package com.joutvhu.fixedwidth.parser.model;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model lồng nhau: dùng để test frame stack depth và parent/child context.
 */
@FixedObject
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NestedModel {
    @FixedField(length = 3)
    private String code;

    @FixedField(start = 3, length = 15)
    private AddressModel address;

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressModel {
        @FixedField(length = 5)
        private String city;

        @FixedField(start = 5, length = 10)
        private String street;
    }
}
