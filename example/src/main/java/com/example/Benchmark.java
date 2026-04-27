package com.example;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.codegen.FixedCompanionRegistry;
import com.joutvhu.fixedwidth.parser.codegen.NativeImageUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase 4 throughput benchmark — processes a fixed-width data file and
 * reports parse throughput (rows/sec) and cold-start latency.
 *
 * <p>Run after generating data with {@code python generate-data.py}:
 * <pre>
 *   python generate-data.py 1000000 data/products.txt
 *   ./gradlew :example:run --args="data/products.txt"
 * </pre>
 *
 * <p>The benchmark measures two things:
 * <ol>
 *   <li><b>Cold-start latency</b> — time from first call to {@link FixedParser#parse}
 *       until the first record is returned (includes metadata build + accessor lookup).</li>
 *   <li><b>Throughput</b> — rows parsed per second over the full file.</li>
 * </ol>
 */
public class Benchmark {

    private static final int WARMUP_ROWS = 1_000;

    public static void main(String[] args) throws IOException {
        String dataFile = args.length > 0 ? args[0] : "data/products.txt";
        Path path = Paths.get(dataFile);

        if (!Files.exists(path)) {
            System.err.println("Data file not found: " + dataFile);
            System.err.println("Generate it first:");
            System.err.println("  python generate-data.py 1000000 " + dataFile);
            System.exit(1);
        }

        printEnvironmentInfo();

        long totalRows = Files.lines(path).count();
        System.out.printf("%nData file : %s%n", path.toAbsolutePath());
        System.out.printf("Total rows: %,d%n%n", totalRows);

        FixedParser parser = FixedParser.parser();

        // ── Cold-start measurement ────────────────────────────────────────────
        // Read the very first line and time the parse (includes metadata build).
        String firstLine;
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            firstLine = br.readLine();
        }

        long coldStart = System.nanoTime();
        Product first = parser.parse(Product.class, firstLine);
        long coldEnd = System.nanoTime();
        System.out.printf("Cold-start latency : %,d ns (first parse including metadata build)%n",
            (coldEnd - coldStart) / 1_000);
        System.out.printf("  First record id  : %d%n%n", first.getId());

        // ── Warmup ────────────────────────────────────────────────────────────
        System.out.printf("Warming up (%,d rows) ...%n", WARMUP_ROWS);
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            for (int i = 0; i < WARMUP_ROWS; i++) {
                String line = br.readLine();
                if (line == null) break;
                parser.parse(Product.class, line);
            }
        }

        // ── Throughput measurement ────────────────────────────────────────────
        System.out.printf("Measuring throughput over %,d rows ...%n", totalRows);
        long parsed = 0;
        long parseErrors = 0;

        long throughputStart = System.nanoTime();
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    parser.parse(Product.class, line);
                    parsed++;
                } catch (Exception e) {
                    parseErrors++;
                }
            }
        }
        long throughputEnd = System.nanoTime();

        double elapsedSec = (throughputEnd - throughputStart) / 1_000_000_000.0;
        double rowsPerSec = parsed / elapsedSec;

        System.out.printf("%nResults:%n");
        System.out.printf("  Rows parsed    : %,d%n", parsed);
        System.out.printf("  Parse errors   : %,d%n", parseErrors);
        System.out.printf("  Elapsed        : %.2f s%n", elapsedSec);
        System.out.printf("  Throughput     : %,.0f rows/sec%n", rowsPerSec);
        System.out.printf("  Avg per row    : %.2f ns%n", elapsedSec * 1_000_000 / parsed);
    }

    private static void printEnvironmentInfo() {
        System.out.println("=== fixed-width-parser Phase 4 Benchmark ===");
        System.out.println();
        System.out.printf("JVM            : %s %s%n",
            System.getProperty("java.vm.name"),
            System.getProperty("java.version"));
        System.out.printf("Native Image   : %s%n", NativeImageUtil.isNativeImage());

        boolean hasAccessor = FixedCompanionRegistry.getAccessor(Product.class) != null;
        boolean hasMeta     = FixedCompanionRegistry.getMetaProvider(Product.class) != null;
        System.out.printf("$FixedWidth companion : %s%n", hasAccessor ? "present (fast path)" : "absent (Reflection fallback)");
        System.out.printf("  - accessor     : %s%n", hasAccessor ? "yes" : "no");
        System.out.printf("  - meta         : %s%n", hasMeta     ? "yes" : "no");

        if (!hasAccessor) {
            System.out.println();
            System.out.println("WARNING: No $FixedWidth companion found for Product.");
            System.out.println("  Add 'annotationProcessor project(\":\")' to example/build.gradle");
            System.out.println("  and rebuild to enable the fast path.");
        }
        System.out.println();
    }
}
