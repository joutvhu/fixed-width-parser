package com.joutvhu.fixedwidth.parser.support;

import java.lang.annotation.Annotation;

public interface TypeInfo {
    <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    Class<?> detectTypeWith(StringAssembler assembler);

    void afterTypeDetected();
}
