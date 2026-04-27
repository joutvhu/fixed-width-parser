package com.joutvhu.fixedwidth.parser.support;

/**
 * Mutable implementation of {@link ContextFrame}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class DefaultContextFrame implements ContextFrame {

    private final FixedTypeInfo typeInfo;
    private final FrameType frameType;
    private final int depth;
    private final int index;
    private final StringAssembler assembler;
    private final FixedStringBuilder builder; // non-null only for WRITE OBJECT frames

    private final String rawString;           // immutable after construction
    private String processedString;
    private Object partialResult;

    public DefaultContextFrame(
            FixedTypeInfo typeInfo,
            FrameType frameType,
            int depth,
            int index,
            StringAssembler assembler,
            String rawString,
            Object partialResult,
            FixedStringBuilder builder) {
        this.typeInfo = typeInfo;
        this.frameType = frameType;
        this.depth = depth;
        this.index = index;
        this.assembler = assembler;
        this.rawString = rawString;
        this.processedString = rawString; // starts equal to raw
        this.partialResult = partialResult;
        this.builder = builder;
    }

    @Override public FixedTypeInfo getTypeInfo()       { return typeInfo; }
    @Override public FrameType getFrameType()          { return frameType; }
    @Override public int getDepth()                    { return depth; }
    @Override public int getIndex()                    { return index; }
    @Override public StringAssembler getAssembler()    { return assembler; }
    @Override public String getRawString()             { return rawString; }
    @Override public FixedStringBuilder getBuilder()   { return builder; }

    @Override
    public String getProcessedString() { return processedString; }

    @Override
    public void setProcessedString(String value) { this.processedString = value; }

    @Override
    public Object getPartialResult() { return partialResult; }

    public void setPartialResult(Object partialResult) { this.partialResult = partialResult; }
}
