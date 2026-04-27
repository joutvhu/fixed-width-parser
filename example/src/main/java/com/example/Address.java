package com.example;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@FixedObject
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @FixedField(length = 10)
    private String city;

    @FixedField(start = 10, length = 5)
    private String zipCode;
}
