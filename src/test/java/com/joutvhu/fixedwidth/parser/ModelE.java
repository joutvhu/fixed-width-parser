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
public class ModelE extends ModelD {
    @FixedFormat(format = "#,###,###")
    @FixedField(label = "FIELD-B", start = 3, length = 9)
    private Integer fieldB;
}
