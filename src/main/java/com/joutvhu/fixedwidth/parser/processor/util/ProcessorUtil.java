package com.joutvhu.fixedwidth.parser.processor.util;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility helpers for the annotation processor, operating on
 * {@code javax.lang.model} types instead of runtime Reflection.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public final class ProcessorUtil {

    private ProcessorUtil() {
    }

    /**
     * Capitalises the first character of the given string.
     */
    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Checks whether the type element has a public no-arg constructor.
     */
    public static boolean hasPublicNoArgConstructor(TypeElement type) {
        for (Element enclosed : type.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement ctor = (ExecutableElement) enclosed;
                if (ctor.getParameters().isEmpty()
                    && ctor.getModifiers().contains(Modifier.PUBLIC)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether a method with the given name and no parameters exists
     * in the type or any of its supertypes.
     */
    public static boolean hasMethod(String methodName, TypeElement type, ProcessingEnvironment env) {
        TypeElement current = type;
        while (current != null) {
            for (Element enclosed : current.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.METHOD
                    && enclosed.getSimpleName().toString().equals(methodName)) {
                    ExecutableElement method = (ExecutableElement) enclosed;
                    if (method.getParameters().isEmpty()) return true;
                }
            }
            // Walk up the superclass chain
            TypeMirror superClass = current.getSuperclass();
            if (superClass.getKind() == TypeKind.NONE) break;
            Element superElement = env.getTypeUtils().asElement(superClass);
            if (superElement instanceof TypeElement) {
                current = (TypeElement) superElement;
            } else {
                break;
            }
        }
        return false;
    }

    /**
     * Checks whether a setter method with the given name and a single parameter
     * exists in the type or any of its supertypes.
     */
    public static boolean hasSetter(String methodName, TypeElement type, ProcessingEnvironment env) {
        TypeElement current = type;
        while (current != null) {
            for (Element enclosed : current.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.METHOD
                    && enclosed.getSimpleName().toString().equals(methodName)) {
                    ExecutableElement method = (ExecutableElement) enclosed;
                    if (method.getParameters().size() == 1) return true;
                }
            }
            TypeMirror superClass = current.getSuperclass();
            if (superClass.getKind() == TypeKind.NONE) break;
            Element superElement = env.getTypeUtils().asElement(superClass);
            if (superElement instanceof TypeElement) {
                current = (TypeElement) superElement;
            } else {
                break;
            }
        }
        return false;
    }

    /**
     * Collects all fields from the given type and its supertypes that are
     * annotated with the given annotation.
     */
    public static List<Element> getAnnotatedFields(TypeElement type, String annotationQualifiedName,
                                                   ProcessingEnvironment env) {
        List<Element> result = new ArrayList<>();
        TypeElement current = type;
        while (current != null) {
            for (Element enclosed : current.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.FIELD
                    && hasAnnotation(enclosed, annotationQualifiedName)) {
                    result.add(enclosed);
                }
            }
            TypeMirror superClass = current.getSuperclass();
            if (superClass.getKind() == TypeKind.NONE) break;
            Element superElement = env.getTypeUtils().asElement(superClass);
            if (superElement instanceof TypeElement) {
                current = (TypeElement) superElement;
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * Checks whether an element has an annotation with the given qualified name.
     */
    public static boolean hasAnnotation(Element element, String annotationQualifiedName) {
        return element.getAnnotationMirrors().stream()
            .anyMatch(am -> am.getAnnotationType().toString().equals(annotationQualifiedName));
    }
}
