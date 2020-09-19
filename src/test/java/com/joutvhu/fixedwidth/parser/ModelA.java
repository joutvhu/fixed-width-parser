package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FixedObject
public class ModelA {
    @FixedField(label = "FIELD-A", length = 3)
    private Long fieldA;

    @FixedField(label = "FIELD-B", start = 3, length = 3)
    private String fieldB;

    @FixedFormat(format = "Y_N")
    @FixedField(label = "FIELD-C", start = 6, length = 1)
    private Boolean fieldC;

    @FixedFormat(format = "yyyy-MM-dd HH:mm:ss")
    @FixedField(label = "FIELD-D", start = 7, length = 19)
    private LocalDateTime fieldD;

    @FixedField(label = "FIELD-A", start = 26, length = 5)
    private Double fieldE;
}
