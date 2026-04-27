package com.joutvhu.fixedwidth.parser.exception;

import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

/**
 * Thrown when no {@link com.joutvhu.fixedwidth.parser.convert.ModuleHook} with
 * {@code supports() = true} is found for the field/type being processed.
 *
 * <p>Replaces the old "Reader not found." / "Writer not found." messages.
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public class NoHookFoundException extends FixedException {

    public NoHookFoundException(FixedTypeInfo info) {
        super(buildMessage(info));
    }

    private static String buildMessage(FixedTypeInfo info) {
        return String.format(
            "No ModuleHook found for type '%s' at field '%s' (position %d, length %d). " +
            "Register a ModuleHook that supports this type in your FixedModule.",
            info.getType() != null ? info.getType().getName() : "unknown",
            info.getName() != null ? info.getName() : "unknown",
            info.getPosition() != null ? info.getPosition() : -1,
            info.getLength() != null ? info.getLength() : -1);
    }
}
