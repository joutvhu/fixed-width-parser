package com.joutvhu.fixedwidth.parser.codegen;

import java.util.Map;

/**
 * Compile-time generated provider for pre-computed metadata
 * of a single {@code @FixedObject} class.
 *
 * @param <T> the target model class
 * @author Giao Ho
 * @since 2.0.0
 */
public interface FixedMetaProvider<T> {
    
    /**
     * The model class this provider targets.
     */
    Class<T> targetClass();

    /**
     * Returns pre-computed field metadata keyed by field name.
     */
    Map<String, FieldMetadata> getFieldMetadata();
}
