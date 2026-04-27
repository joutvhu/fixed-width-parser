package com.example;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedPadding;
import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.domain.KeepPadding;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 10-field model used for the Phase 4 throughput benchmark.
 *
 * <p>Total record width: 80 characters.
 *
 * <pre>
 * Offset  Length  Field
 *  0       8      id          (right-aligned, zero-padded)
 *  8      12      sku
 * 20      20      name
 * 40       8      price       (right-aligned, zero-padded)
 * 48       6      quantity    (right-aligned, zero-padded)
 * 54       2      category
 * 56       1      active      (Y/N)
 * 57       8      createdDate (yyyyMMdd)
 * 65       6      warehouseId
 * 71       9      barcode
 * </pre>
 */
@Data
@FixedObject
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @FixedField(start = 0, length = 8)
    @FixedPadding(value = '0', alignment = Alignment.RIGHT, keep = KeepPadding.DROP)
    private Long id;

    @FixedField(start = 8, length = 12)
    private String sku;

    @FixedField(start = 20, length = 20)
    private String name;

    @FixedField(start = 40, length = 8)
    @FixedPadding(value = '0', alignment = Alignment.RIGHT, keep = KeepPadding.DROP)
    private Long price;

    @FixedField(start = 48, length = 6)
    @FixedPadding(value = '0', alignment = Alignment.RIGHT, keep = KeepPadding.DROP)
    private Integer quantity;

    @FixedField(start = 54, length = 2)
    private String category;

    @FixedField(start = 56, length = 1)
    private String active;

    @FixedField(start = 57, length = 8)
    private String createdDate;

    @FixedField(start = 65, length = 6)
    private String warehouseId;

    @FixedField(start = 71, length = 9)
    private String barcode;
}
