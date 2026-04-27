package com.joutvhu.fixedwidth.parser.model;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal model: a single string field.
 * Used to test basic features without noise from other fields.
 */
@FixedObject
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleStringModel {
    @FixedField(length = 10)
    private String value;
}
