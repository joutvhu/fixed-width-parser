package com.joutvhu.fixedwidth.parser.processor;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import org.junit.jupiter.api.Test;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link FixedWidthProcessor} to ensure it correctly generates
 * the $FixedAccessor class when it processes a @FixedObject class.
 */
public class FixedWidthProcessorTest {

    @Test
    public void testProcessorGeneratesAccessor() throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        // Create a temporary directory for output
        Path tempDir = Files.createTempDirectory("apt-test");
        File outDir = tempDir.toFile();
        outDir.deleteOnExit();

        // We compile a dummy class
        String sourceCode = "package com.example;\n" +
            "import com.joutvhu.fixedwidth.parser.annotation.FixedObject;\n" +
            "import com.joutvhu.fixedwidth.parser.annotation.FixedField;\n" +
            "@FixedObject\n" +
            "public class DummyModel {\n" +
            "    @FixedField(length = 10)\n" +
            "    private String name;\n" +
            "    public String getName() { return name; }\n" +
            "    public void setName(String name) { this.name = name; }\n" +
            "}\n";

        Path sourcePath = tempDir.resolve("DummyModel.java");
        Files.write(sourcePath, sourceCode.getBytes());

        Iterable<? extends JavaFileObject> compilationUnits =
            fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourcePath.toFile()));

        // Get classpath from classloader
        String classpath = System.getProperty("java.class.path");

        Iterable<String> options = Arrays.asList(
            "-d", outDir.getAbsolutePath(),
            "-cp", classpath
        );

        JavaCompiler.CompilationTask task = compiler.getTask(
            null,
            fileManager,
            null,
            options,
            null,
            compilationUnits
        );

        // Set the processor explicitly
        task.setProcessors(Collections.singletonList(new FixedWidthProcessor()));

        boolean success = task.call();
        assertTrue(success, "Compilation should succeed");

        // Verify the generated class exists
        File generatedClass = new File(outDir, "com/example/DummyModel$FixedAccessor.class");
        assertTrue(generatedClass.exists(), "Accessor class should be generated: " + generatedClass.getAbsolutePath());
    }
}
