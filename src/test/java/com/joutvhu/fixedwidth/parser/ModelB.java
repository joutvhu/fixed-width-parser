package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FixedObject
public class ModelB {
    @FixedField(label = "COLLECTION", length = 30)
    private Collection<@FixedParam(length = 2) String> collectionValues;

    @FixedField(label = "LIST", length = 30)
    private List<@FixedParam(length = 2) String> listValue;

    @FixedField(label = "SET", length = 30)
    private Set<@FixedParam(length = 2) String> setValues;

    @FixedField(label = "QUEUE", length = 30)
    private Queue<@FixedParam(length = 2) String> queueValues;

    @FixedField(label = "STACK", length = 30)
    private Stack<@FixedParam(length = 2) String> stackValues;

    @FixedField(label = "MAP", length = 30)
    private Map<@FixedParam(length = 2) String, @FixedParam(length = 2) String> mapValues;
}
