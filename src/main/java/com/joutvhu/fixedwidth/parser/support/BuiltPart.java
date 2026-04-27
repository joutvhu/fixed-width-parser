package com.joutvhu.fixedwidth.parser.support;

/**
 * Represents one field's contribution to the output string during a write operation.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public interface BuiltPart {
    /** The field name as declared in the model class. */
    String getFieldName();

    /** Metadata for the field. */
    FixedTypeInfo getTypeInfo();

    /** The padded/aligned string value that will be placed in the output. */
    String getValue();

    /** The string value before padding was applied. */
    String getRawValue();
}
