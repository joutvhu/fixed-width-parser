package com.joutvhu.fixedwidth.parser.codegen;

import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.domain.KeepPadding;

/**
 * Pre-computed metadata for a single field in a @FixedObject model.
 * 
 * @author Giao Ho
 * @since 2.0.0
 */
public class FieldMetadata {
    private final String name;
    private final String label;
    private final Integer start;
    private final Integer length;
    private final boolean require;
    private final Character padding;
    private final Character nullPadding;
    private final KeepPadding keepPadding;
    private final Alignment alignment;

    public FieldMetadata(String name, String label, Integer start, Integer length, boolean require,
                         Character padding, Character nullPadding, KeepPadding keepPadding, Alignment alignment) {
        this.name = name;
        this.label = label;
        this.start = start;
        this.length = length;
        this.require = require;
        this.padding = padding;
        this.nullPadding = nullPadding;
        this.keepPadding = keepPadding;
        this.alignment = alignment;
    }

    public String getName() { return name; }
    public String getLabel() { return label; }
    public Integer getStart() { return start; }
    public Integer getLength() { return length; }
    public boolean isRequire() { return require; }
    public Character getPadding() { return padding; }
    public Character getNullPadding() { return nullPadding; }
    public KeepPadding getKeepPadding() { return keepPadding; }
    public Alignment getAlignment() { return alignment; }
}
