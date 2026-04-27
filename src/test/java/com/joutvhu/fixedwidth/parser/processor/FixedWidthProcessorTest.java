package com.joutvhu.fixedwidth.parser.processor;

import org.junit.jupiter.api.Test;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link FixedWidthProcessor} to ensure it correctly generates
 * the $FixedAccessor and $FixedMeta classes and rejects invalid models at
 * compile time.
 */
public class FixedWidthProcessorTest {

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static final String CLASSPATH = System.getProperty("java.class.path");

    /** Compiles {@code source} in a temp dir and returns the result. */
    private CompileResult compile(String fileName, String source) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        Path tempDir = Files.createTempDirectory("apt-test");
        Path sourcePath = tempDir.resolve(fileName);
        Files.write(sourcePath, source.getBytes());

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        Iterable<? extends JavaFileObject> units =
            fileManager.getJavaFileObjectsFromFiles(Collections.singletonList(sourcePath.toFile()));

        JavaCompiler.CompilationTask task = compiler.getTask(
            null, fileManager, diagnostics,
            Arrays.asList("-d", tempDir.toString(), "-cp", CLASSPATH),
            null, units);
        task.setProcessors(Collections.singletonList(new FixedWidthProcessor()));

        boolean success = task.call();
        return new CompileResult(success, diagnostics, tempDir.toFile());
    }

    private static class CompileResult {
        final boolean success;
        final DiagnosticCollector<JavaFileObject> diagnostics;
        final File outDir;

        CompileResult(boolean success, DiagnosticCollector<JavaFileObject> diagnostics, File outDir) {
            this.success = success;
            this.diagnostics = diagnostics;
            this.outDir = outDir;
        }

        boolean hasError(String fragment) {
            return diagnostics.getDiagnostics().stream()
                .anyMatch(d -> d.getKind() == Diagnostic.Kind.ERROR
                    && d.getMessage(null).contains(fragment));
        }

        boolean classExists(String relativePath) {
            return new File(outDir, relativePath).exists();
        }
    }

    // ── Valid model ───────────────────────────────────────────────────────────

    @Test
    void validModel_compilesSuccessfully_andGeneratesAccessor() throws Exception {
        String source = "package com.example;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedObject;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedField;\n"
            + "@FixedObject\n"
            + "public class DummyModel {\n"
            + "    @FixedField(start = 0, length = 10)\n"
            + "    private String name;\n"
            + "    public String getName() { return name; }\n"
            + "    public void setName(String name) { this.name = name; }\n"
            + "}\n";

        CompileResult r = compile("DummyModel.java", source);

        assertTrue(r.success, "Compilation should succeed");
        assertTrue(r.classExists("com/example/DummyModel$FixedAccessor.class"),
            "Accessor class should be generated");
    }

    @Test
    void validModel_compilesSuccessfully_andGeneratesMeta() throws Exception {
        String source = "package com.example;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedObject;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedField;\n"
            + "@FixedObject\n"
            + "public class MetaModel {\n"
            + "    @FixedField(start = 0, length = 5)\n"
            + "    private String code;\n"
            + "    public String getCode() { return code; }\n"
            + "    public void setCode(String code) { this.code = code; }\n"
            + "}\n";

        CompileResult r = compile("MetaModel.java", source);

        assertTrue(r.success, "Compilation should succeed");
        assertTrue(r.classExists("com/example/MetaModel$FixedMeta.class"),
            "Meta class should be generated");
    }

    @Test
    void adjacentFields_notOverlapping_compilesSuccessfully() throws Exception {
        String source = "package com.example;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedObject;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedField;\n"
            + "@FixedObject\n"
            + "public class AdjacentModel {\n"
            + "    @FixedField(start = 0, length = 5)\n"
            + "    private String a;\n"
            + "    @FixedField(start = 5, length = 5)\n"
            + "    private String b;\n"
            + "    public String getA() { return a; }\n"
            + "    public void setA(String a) { this.a = a; }\n"
            + "    public String getB() { return b; }\n"
            + "    public void setB(String b) { this.b = b; }\n"
            + "}\n";

        CompileResult r = compile("AdjacentModel.java", source);

        assertTrue(r.success, "Adjacent fields should not trigger overlap error");
    }

    @Test
    void unlimitedLengthField_notCheckedForOverlap_compilesSuccessfully() throws Exception {
        // length=0 means unlimited — should not be flagged as overlapping
        String source = "package com.example;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedObject;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedField;\n"
            + "@FixedObject\n"
            + "public class UnlimitedModel {\n"
            + "    @FixedField(start = 0, length = 5)\n"
            + "    private String code;\n"
            + "    @FixedField(start = 5, length = 0)\n"
            + "    private String rest;\n"
            + "    public String getCode() { return code; }\n"
            + "    public void setCode(String code) { this.code = code; }\n"
            + "    public String getRest() { return rest; }\n"
            + "    public void setRest(String rest) { this.rest = rest; }\n"
            + "}\n";

        CompileResult r = compile("UnlimitedModel.java", source);

        assertTrue(r.success, "length=0 field should not trigger overlap check");
    }

    // ── Field overlap ─────────────────────────────────────────────────────────

    @Test
    void overlappingFields_failsCompilation_withOverlapError() throws Exception {
        String source = "package com.example;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedObject;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedField;\n"
            + "@FixedObject\n"
            + "public class OverlapModel {\n"
            + "    @FixedField(start = 0, length = 10)\n"
            + "    private String f1;\n"
            + "    @FixedField(start = 5, length = 10)\n"
            + "    private String f2;\n"
            + "    public String getF1() { return f1; }\n"
            + "    public void setF1(String f1) { this.f1 = f1; }\n"
            + "    public String getF2() { return f2; }\n"
            + "    public void setF2(String f2) { this.f2 = f2; }\n"
            + "}\n";

        CompileResult r = compile("OverlapModel.java", source);

        assertFalse(r.success, "Compilation should fail due to overlapping fields");
        assertTrue(r.hasError("overlap"), "Error should mention 'overlap'");
        assertFalse(r.classExists("com/example/OverlapModel$FixedAccessor.class"),
            "Accessor should NOT be generated for invalid models");
    }

    @Test
    void overlappingFields_errorMentionsBothFieldNames() throws Exception {
        String source = "package com.example;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedObject;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedField;\n"
            + "@FixedObject\n"
            + "public class OverlapNamed {\n"
            + "    @FixedField(start = 0, length = 10)\n"
            + "    private String alpha;\n"
            + "    @FixedField(start = 5, length = 10)\n"
            + "    private String beta;\n"
            + "    public String getAlpha() { return alpha; }\n"
            + "    public void setAlpha(String v) { this.alpha = v; }\n"
            + "    public String getBeta() { return beta; }\n"
            + "    public void setBeta(String v) { this.beta = v; }\n"
            + "}\n";

        CompileResult r = compile("OverlapNamed.java", source);

        assertFalse(r.success);
        assertTrue(r.hasError("alpha") || r.hasError("beta"),
            "Error should mention the overlapping field names");
    }

    // ── Missing dependency ────────────────────────────────────────────────────

    @Test
    void missingFixedCountDependency_failsCompilation() throws Exception {
        String source = "package com.example;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedObject;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedField;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedCount;\n"
            + "@FixedObject\n"
            + "public class DependencyModel {\n"
            + "    @FixedField(start = 0, length = 10)\n"
            + "    @FixedCount(field = \"missingField\")\n"
            + "    private java.util.List<String> items;\n"
            + "    public java.util.List<String> getItems() { return items; }\n"
            + "    public void setItems(java.util.List<String> items) { this.items = items; }\n"
            + "}\n";

        CompileResult r = compile("DependencyModel.java", source);

        assertFalse(r.success, "Compilation should fail due to missing dependency field");
        assertTrue(r.hasError("no such field exists"), "Error should mention missing field");
    }

    @Test
    void missingFixedConditionalDependency_failsCompilation() throws Exception {
        String source = "package com.example;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedObject;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedField;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedConditional;\n"
            + "@FixedObject\n"
            + "public class ConditionalModel {\n"
            + "    @FixedField(start = 0, length = 5)\n"
            + "    private String type;\n"
            + "    @FixedConditional(dependsOnField = \"ghostField\", whenValue = \"X\")\n"
            + "    @FixedField(start = 5, length = 10)\n"
            + "    private String data;\n"
            + "    public String getType() { return type; }\n"
            + "    public void setType(String type) { this.type = type; }\n"
            + "    public String getData() { return data; }\n"
            + "    public void setData(String data) { this.data = data; }\n"
            + "}\n";

        CompileResult r = compile("ConditionalModel.java", source);

        assertFalse(r.success, "Compilation should fail due to missing @FixedConditional dependency");
        assertTrue(r.hasError("no such field exists"), "Error should mention missing field");
    }

    @Test
    void validConditionalDependency_compilesSuccessfully() throws Exception {
        String source = "package com.example;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedObject;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedField;\n"
            + "import com.joutvhu.fixedwidth.parser.annotation.FixedConditional;\n"
            + "@FixedObject\n"
            + "public class ValidConditional {\n"
            + "    @FixedField(start = 0, length = 5)\n"
            + "    private String type;\n"
            + "    @FixedConditional(dependsOnField = \"type\", whenValue = \"X\")\n"
            + "    @FixedField(start = 5, length = 10)\n"
            + "    private String data;\n"
            + "    public String getType() { return type; }\n"
            + "    public void setType(String type) { this.type = type; }\n"
            + "    public String getData() { return data; }\n"
            + "    public void setData(String data) { this.data = data; }\n"
            + "}\n";

        CompileResult r = compile("ValidConditional.java", source);

        assertTrue(r.success, "Valid @FixedConditional reference should compile successfully");
    }
}
