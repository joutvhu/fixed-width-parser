package com.joutvhu.fixedwidth.parser.support;

/**
 * Fixed width string deserialization strategy
 *
 * @author Giao Ho
 * @since 1.1.0
 */
public interface ReadStrategy {
    Object read(FixedTypeInfo info, StringAssembler assembler);
}
