package com.joutvhu.fixedwidth.parser.model;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model with a field of an unsupported type (no @FixedObject, no registered hook).
 * Used by HookSystemTest to verify NoHookFoundException is thrown.
 */
@FixedObject
@Data
@NoArgsConstructor
public class UnsupportedFieldModel {

    @FixedField(length = 5)
    UnsupportedFieldType unsupported;

    /**
     * Custom class with no @FixedObject and no registered hook.
     */
    public static class UnsupportedFieldType {
    }
}
