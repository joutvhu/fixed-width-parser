package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedParam;

import java.util.*;

public class ModelB {
    @FixedField(label = "CHILDREN", start = 8, length = 20)
    private List<@FixedParam(length = 2) String> children;

    @FixedField(label = "CHILDREN-SET", start = 8, length = 20)
    private Set<@FixedParam(length = 2) String> childrenSet;

    @FixedField(label = "CHILDREN-QUEUE", start = 8, length = 20)
    private Queue<@FixedParam(length = 2) String> childrenQueue;

    @FixedField(label = "CHILDREN-STACK", start = 8, length = 20)
    private Stack<@FixedParam(length = 2) String> childrenStack;

    @FixedField(label = "CHILDREN-MAP", start = 28, length = 30)
    private Map<@FixedParam(length = 2) String, @FixedParam(length = 2) String> map;
}
