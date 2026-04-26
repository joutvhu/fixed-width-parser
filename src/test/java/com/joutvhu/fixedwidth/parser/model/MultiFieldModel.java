package com.joutvhu.fixedwidth.parser.model;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Model nhiều field với các type khác nhau.
 * Dùng để test parse/export đầy đủ và frame stack.
 */
@FixedObject
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiFieldModel {
    @FixedField(length = 3)
    private Long id;

    @FixedField(start = 3, length = 10)
    private String name;

    @FixedFormat(format = "Y|N")
    @FixedField(start = 13, length = 1)
    private Boolean active;

    @FixedFormat(format = "yyyy-MM-dd")
    @FixedField(start = 14, length = 10)
    private LocalDate date;
}
