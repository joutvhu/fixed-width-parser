package com.joutvhu.fixedwidth.parser.model;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedPadding;
import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.domain.KeepPadding;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 3-field model used by Phase 4 tests to verify accessor generation and
 * Native Image detection logic.
 *
 * <p>Kept as a top-level class so the annotation processor registers it
 * in the test-classpath SPI file and {@link
 * com.joutvhu.fixedwidth.parser.codegen.AccessorRegistry} can find it.
 */
@FixedObject
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductModel {

    @FixedField(start = 0, length = 5)
    private String code;

    @FixedField(start = 5, length = 10)
    private String name;

    @FixedField(start = 15, length = 8)
    @FixedPadding(value = '0', alignment = Alignment.RIGHT, keep = KeepPadding.DROP)
    private Integer price;
}
