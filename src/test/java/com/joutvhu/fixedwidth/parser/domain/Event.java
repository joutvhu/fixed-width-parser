package com.joutvhu.fixedwidth.parser.domain;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedParam;

import java.util.*;

@FixedObject
public class Event {
    @FixedField(label = "EVENT-ID", length = 5)
    private Long eventId;

    @FixedField(label = "EVENT-NAME", start = 5, length = 3)
    private String eventName;

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