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
public class User {
    @FixedField(length = 5)
    private Long id;

    @FixedField(start = 5, length = 10)
    private String username;

    @FixedField(start = 15, length = 20)
    private String email;
}
