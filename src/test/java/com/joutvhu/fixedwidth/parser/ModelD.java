package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FixedObject(
        subTypes = {
                @FixedObject.Type(value = ModelE.class, prop = "fieldA", oneOf = {"000", "011", "022"}),
                @FixedObject.Type(value = ModelF.class, prop = "fieldA", matchWith = "[1-9]{3}")
        },
        defaultSubType = ModelG.class
)
public abstract class ModelD {
    @FixedField(label = "FIELD-A", length = 3)
    private Long fieldA;
}
