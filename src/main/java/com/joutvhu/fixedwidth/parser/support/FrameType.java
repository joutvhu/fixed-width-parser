package com.joutvhu.fixedwidth.parser.support;

/**
 * Describes the kind of node a {@link ContextFrame} represents in the object tree.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public enum FrameType {
    /** The root object being parsed or exported. */
    OBJECT,
    /** A single field inside an object. */
    FIELD,
    /** An element inside a Collection. */
    COLLECTION_ITEM,
    /** A key or value inside a Map. */
    MAP_ENTRY
}
