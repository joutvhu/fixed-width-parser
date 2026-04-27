package com.joutvhu.fixedwidth.parser.codegen;

/**
 * Utility for detecting GraalVM Native Image execution context.
 *
 * <p>GraalVM sets the system property {@code org.graalvm.nativeimage.imagecode}
 * to {@code "runtime"} when running inside a Native Image binary. This class
 * provides a zero-dependency check for that condition.
 *
 * <p>No {@code org.graalvm.nativeimage:svm} compile dependency is needed —
 * the property is part of the GraalVM public contract and is documented in
 * the Native Image compatibility guide.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public final class NativeImageUtil {

    /**
     * System property set by GraalVM Native Image at runtime.
     * Value is {@code "runtime"} when executing inside a native binary.
     */
    private static final String NATIVE_IMAGE_PROPERTY = "org.graalvm.nativeimage.imagecode";

    /**
     * Cached result — evaluated once at class-init time.
     * The JVM property cannot change after startup, so caching is safe.
     */
    private static final boolean IN_NATIVE_IMAGE =
        "runtime".equals(System.getProperty(NATIVE_IMAGE_PROPERTY));

    private NativeImageUtil() {
    }

    /**
     * Returns {@code true} when the current process is a GraalVM Native Image
     * binary (i.e. {@code org.graalvm.nativeimage.imagecode == "runtime"}).
     *
     * <p>Returns {@code false} on a standard JVM, regardless of GraalVM version.
     */
    public static boolean isNativeImage() {
        return IN_NATIVE_IMAGE;
    }

    /**
     * Throws {@link UnsupportedOperationException} with a clear diagnostic
     * message when called inside a Native Image binary.
     *
     * <p>Call this at the entry point of any code path that relies on
     * {@link java.lang.reflect.Field#setAccessible} or
     * {@link java.lang.reflect.Field#get}/{@link java.lang.reflect.Field#set},
     * so that users get an actionable error instead of a cryptic
     * {@code MissingReflectionRegistrationError}.
     *
     * @param modelClass the model class that triggered the fallback
     * @throws UnsupportedOperationException always, when running in Native Image
     */
    public static void assertNotNativeImageReflection(Class<?> modelClass) {
        if (IN_NATIVE_IMAGE) {
            throw new UnsupportedOperationException(
                "fixed-width-parser: Reflection-based field access is not supported in GraalVM Native Image. "
                    + "Add the annotation processor to your build so that a $FixedAccessor is generated "
                    + "for '" + modelClass.getName() + "' — then no reflect-config.json entry is needed.\n"
                    + "  Gradle:  annotationProcessor 'com.github.joutvhu:fixed-width-parser:<version>'\n"
                    + "  Maven:   <annotationProcessorPaths> with the same artifact");
        }
    }
}
