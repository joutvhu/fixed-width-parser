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
    @FixedField(label = "FIELD-D", start = 3, length = 1)
    private Character fieldD;
}