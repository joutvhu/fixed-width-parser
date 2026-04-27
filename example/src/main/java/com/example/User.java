package com.example;

import com.joutvhu.fixedwidth.parser.annotation.FixedCount;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@FixedObject
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class User extends AbstractUser {
    @FixedField(start = 16, length = 15)
    private Address address;

    @FixedField(start = 31, length = 2)
    private Integer roleCount;

    @FixedField(start = 33, length = 10)
    @FixedCount(field = "roleCount")
    private List<@FixedParam(length = 5) String> roles;

    @FixedField(start = 43, length = 20)
    @FixedCount(value = 2)
    private Map<@FixedParam(length = 5) String, @FixedParam(length = 5) String> metadata;

    public User() {
        this.type = "U";
    }
}
