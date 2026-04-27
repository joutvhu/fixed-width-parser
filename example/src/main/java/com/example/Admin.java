package com.example;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@FixedObject
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Admin extends AbstractUser {
    @FixedField(start = 16, length = 10)
    private String adminLevel;

    public Admin() {
        this.type = "A";
    }
}
