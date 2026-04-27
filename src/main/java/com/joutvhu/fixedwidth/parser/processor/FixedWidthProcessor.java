package com.joutvhu.fixedwidth.parser.processor;

import com.joutvhu.fixedwidth.parser.processor.generator.AccessorGenerator;
import com.joutvhu.fixedwidth.parser.processor.model.ModelClass;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Set;

/**
 * JSR-269 annotation processor that scans for {@code @FixedObject}-annotated
 * classes and generates {@code $FixedAccessor} companion classes.
 *
 * <p>The generated classes eliminate Reflection-based field get/set calls
 * on the parse/export hot path. If this processor is not on the annotation
 * processor classpath, the library falls back to Reflection transparently.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
@SupportedAnnotationTypes("com.joutvhu.fixedwidth.parser.annotation.FixedObject")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class FixedWidthProcessor extends AbstractProcessor {

    private final Set<String> processedClasses = new HashSet<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element.getKind() != ElementKind.CLASS) continue;

                TypeElement typeElement = (TypeElement) element;
                String qualifiedName = typeElement.getQualifiedName().toString();

                // Avoid processing the same class twice across incremental rounds
                if (processedClasses.contains(qualifiedName)) continue;
                processedClasses.add(qualifiedName);

                try {
                    ModelClass model = ModelClass.from(typeElement, processingEnv);

                    processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE,
                        "fixed-width-parser: generating accessor for " + qualifiedName,
                        typeElement);

                    new AccessorGenerator(processingEnv).generate(model);
                } catch (Exception e) {
                    processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.WARNING,
                        "fixed-width-parser: failed to generate accessor for "
                            + qualifiedName + ": " + e.getMessage(),
                        typeElement);
                }
            }
        }
        // Return false so that other processors can also process @FixedObject if needed
        return false;
    }
}
