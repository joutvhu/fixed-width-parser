package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.constraint.FixedRegex;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FixedObject
public class ModelG extends ModelD {
    @FixedRegex(regex = "[a-zA-Z]")
    @FixedField(start = 3, length = 1)
    private Character fieldD;

    public ModelG(Long fieldA, @FixedRegex(regex = "[a-zA-Z]") Character fieldD) {
        super(fieldA);
        this.fieldD = fieldD;
    }
}
