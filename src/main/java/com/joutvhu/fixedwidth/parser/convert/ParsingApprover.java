package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.exception.FixedException;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

/**
 * Parsing approver
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public abstract class ParsingApprover {
    protected FixedTypeInfo info;
    protected FixedParseStrategy strategy;

    public ParsingApprover(FixedTypeInfo info, FixedParseStrategy strategy) {
        this.info = info;
        this.strategy = strategy;
    }

    /**
     * Can't use this class to read, write or validate fixed width string
     */
    protected void reject() {
        throw new FixedException("Can't use this class.");
    }
}
