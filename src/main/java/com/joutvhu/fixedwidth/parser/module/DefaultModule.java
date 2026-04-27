package com.joutvhu.fixedwidth.parser.module;

import com.joutvhu.fixedwidth.parser.convert.hook.BooleanHook;
import com.joutvhu.fixedwidth.parser.convert.hook.CollectionHook;
import com.joutvhu.fixedwidth.parser.convert.hook.DateHook;
import com.joutvhu.fixedwidth.parser.convert.hook.EnumHook;
import com.joutvhu.fixedwidth.parser.convert.hook.MapHook;
import com.joutvhu.fixedwidth.parser.convert.hook.NumberHook;
import com.joutvhu.fixedwidth.parser.convert.hook.ObjectHook;
import com.joutvhu.fixedwidth.parser.convert.hook.OptionalHook;
import com.joutvhu.fixedwidth.parser.convert.hook.StringHook;
import com.joutvhu.fixedwidth.parser.convert.hook.UUIDHook;

/**
 * Default module — registers all 10 built-in {@link com.joutvhu.fixedwidth.parser.convert.ModuleHook}s.
 *
 * <p>Registration order matters: the first hook whose {@code supports()} returns {@code true}
 * is used. {@link ObjectHook} must be last because it matches any type annotated with
 * {@code @FixedObject}, which would otherwise shadow more specific hooks.
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public class DefaultModule extends FixedModule {
    public DefaultModule() {
        super(
            StringHook.class,
            BooleanHook.class,
            NumberHook.class,
            DateHook.class,
            EnumHook.class,
            UUIDHook.class,
            OptionalHook.class,
            CollectionHook.class,
            MapHook.class,
            ObjectHook.class   // must be last — handles @FixedObject
        );
    }
}
