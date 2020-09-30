package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FixedObject
public class ModelF extends ModelD {
    @FixedFormat(format = "#,##0.000")
    @FixedField(label = "FIELD-C", start = 3, length = 9)
    private Double fieldC;

    public ModelF(Long fieldA, @FixedFormat(format = "#,##0.000") Double fieldC) {
        super(fieldA);
        this.fieldC = fieldC;
    }
}
