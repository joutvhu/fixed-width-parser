package com.joutvhu.fixedwidth.parser.processor.validation;

import com.joutvhu.fixedwidth.parser.annotation.FixedConditional;
import com.joutvhu.fixedwidth.parser.annotation.FixedCount;
import com.joutvhu.fixedwidth.parser.processor.model.ModelClass;
import com.joutvhu.fixedwidth.parser.processor.model.ModelField;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates the schema of a {@code @FixedObject} model during compilation.
 * Ports the runtime validation from {@code SchemaValidator} to the APT processor.
 * 
 * @author Giao Ho
 * @since 2.0.0
 */
public final class ProcessorSchemaCheck {

    private ProcessorSchemaCheck() {
    }

    /**
     * Runs all schema validations for the given model.
     * Reports errors directly to the compiler's Messager.
     * 
     * @param model the compiled model to validate
     * @param env the processing environment
     * @return true if valid, false if errors were reported
     */
    public static boolean validate(ModelClass model, ProcessingEnvironment env) {
        boolean valid = true;
        valid &= checkFieldOverlap(model, env);
        valid &= checkMissingDependencyFields(model, env);
        return valid;
    }

    private static boolean checkFieldOverlap(ModelClass model, ProcessingEnvironment env) {
        boolean valid = true;
        List<ModelField> fields = model.getFields();
        
        for (int i = 0; i < fields.size(); i++) {
            ModelField a = fields.get(i);
            if (a.getLength() == null || a.getLength() == 0) continue;
            int aEnd = a.getStart() + a.getLength();

            for (int j = i + 1; j < fields.size(); j++) {
                ModelField b = fields.get(j);
                if (b.getLength() == null || b.getLength() == 0) continue;
                int bEnd = b.getStart() + b.getLength();

                if (a.getStart() < bEnd && b.getStart() < aEnd) {
                    String msg = String.format(
                        "[%s] Fields '%s' (start=%d, length=%d) and '%s' (start=%d, length=%d) overlap.",
                        model.getSimpleClassName(),
                        a.getFieldName(), a.getStart(), a.getLength(),
                        b.getFieldName(), b.getStart(), b.getLength());
                    
                    env.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, model.getTypeElement());
                    valid = false;
                }
            }
        }
        return valid;
    }

    private static boolean checkMissingDependencyFields(ModelClass model, ProcessingEnvironment env) {
        boolean valid = true;
        Set<String> fieldNames = new HashSet<>();
        for (ModelField f : model.getFields()) {
            fieldNames.add(f.getFieldName());
        }

        for (ModelField f : model.getFields()) {
            FixedConditional cond = f.getElement().getAnnotation(FixedConditional.class);
            if (cond != null && !cond.dependsOnField().isEmpty()) {
                if (!fieldNames.contains(cond.dependsOnField())) {
                    String msg = String.format(
                        "[%s] Field '%s' has @FixedConditional(dependsOnField=\"%s\") but no such field exists.",
                        model.getSimpleClassName(), f.getFieldName(), cond.dependsOnField());
                    env.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, f.getElement());
                    valid = false;
                }
            }

            FixedCount count = f.getElement().getAnnotation(FixedCount.class);
            if (count != null && !count.field().isEmpty()) {
                if (!fieldNames.contains(count.field())) {
                    String msg = String.format(
                        "[%s] Field '%s' has @FixedCount(field=\"%s\") but no such field exists.",
                        model.getSimpleClassName(), f.getFieldName(), count.field());
                    env.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, f.getElement());
                    valid = false;
                }
            }
        }
        return valid;
    }
}
