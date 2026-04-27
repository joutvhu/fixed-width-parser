package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

/**
 * Hook registered in {@link com.joutvhu.fixedwidth.parser.module.FixedModule},
 * which decides whether to process a field via {@link #supports}.
 *
 * <p>Replaces the old {@code FixedWidthReader} + {@code FixedWidthWriter} pair.
 * The dispatcher only calls {@link #handle} when {@link #supports} returns {@code true}.
 *
 * <p><b>Lifecycle:</b> singleton — a single instance is created when registered
 * into the module and reused for all parse/export operations. Implementation
 * must be thread-safe.
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public interface ModuleHook extends Hook {

    /**
     * Checks if this hook can handle the field/type described by {@code info}.
     * Called before {@link #handle} — the dispatcher never calls {@code handle}
     * when this method returns {@code false}.
     */
    boolean supports(FixedTypeInfo info);
}
