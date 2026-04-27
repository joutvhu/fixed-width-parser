package com.example;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import lombok.Data;

@Data
@FixedObject(
    subTypes = {
        @FixedObject.Type(value = User.class, prop = "type", oneOf = {"U"}),
        @FixedObject.Type(value = Admin.class, prop = "type", oneOf = {"A"})
    }
)
public abstract class AbstractUser {
    @FixedField(length = 1)
    protected String type;

    @FixedField(start = 1, length = 5)
    protected Long id;

    @FixedField(start = 6, length = 10)
    protected String username;
}
