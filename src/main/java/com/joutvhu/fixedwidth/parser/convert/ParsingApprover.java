package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.exception.FixedException;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

/**
 * Parsing approver
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public abstract class ParsingApprover<T> {
    protected T strategy;
    protected FixedTypeInfo info;

    public ParsingApprover(FixedTypeInfo info) {
        this.info = info;
    }

    public ParsingApprover(FixedTypeInfo info, T strategy) {
        this.info = info;
        this.strategy = strategy;
    }

    /**
     * Can't use this class to read, write or validate fixed width string
     */
    protected void reject() {
        throw new FixedException("Cannot use this class.");
    }
}
