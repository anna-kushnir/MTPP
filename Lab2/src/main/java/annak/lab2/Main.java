package annak.lab2;

import annak.lab2.task1.TagCounter;

import java.util.List;

import static annak.lab2.Utils.measureTime;
import static annak.lab2.Utils.printSpeedup;

public class Main {

    private static final String HTML_DIR = "lab2_html_files";
    private static final int FILES_COUNT = 2000;
    private static final int FJ_THRESHOLD = 5;

    private static final int MATRIX_SIZE = 1000;
    private static final int ARRAY_SIZE = 10_000_000;

    private static final int THREADS = Runtime.getRuntime().availableProcessors();
    private static final int HEATING_ITERATIONS = 5;

    public static void main(String[] args) {
        System.out.println("\nLogic cores count: " + THREADS);
        System.out.println("\n####### TEST 1: Map-Reduce vs Fork-Join vs Worker Pool #######");
        runTagCounterTests();
    }

    private static void runTagCounterTests() {
        System.out.println("\n>>>>> Running 1.1. Tag Counter");

        if (TagCounter.prepareHtmlFiles(HTML_DIR, FILES_COUNT))
            System.out.println("Html files were generated");
        final List<String> htmlDocs = TagCounter.loadDocuments(HTML_DIR);
        System.out.println("Html files were loaded");

        long timeSeq = 0, timeMR = 0, timeFJ = 0, timeWP = 0;
        for (int i = 1; i <= HEATING_ITERATIONS; i++) {
            timeSeq = measureTime("--- Sequential",
                    () -> TagCounter.countSequential(htmlDocs), i == HEATING_ITERATIONS);
        }

        for (int i = 1; i <= HEATING_ITERATIONS; i++) {
            timeMR = measureTime("--- Map-Reduce",
                    () -> TagCounter.countMapReduce(htmlDocs), i == HEATING_ITERATIONS);
        }
        printSpeedup(timeSeq, timeMR);

        for (int i = 1; i <= HEATING_ITERATIONS; i++) {
            timeFJ = measureTime("--- Fork-Join",
                    () -> TagCounter.countForkJoin(htmlDocs, FJ_THRESHOLD), i == HEATING_ITERATIONS);
        }
        printSpeedup(timeSeq, timeFJ);

        for (int i = 1; i <= HEATING_ITERATIONS; i++) {
            timeWP = measureTime("--- Worker Pool",
                    () -> TagCounter.countWorkerPool(htmlDocs, THREADS), i == HEATING_ITERATIONS);
        }
        printSpeedup(timeSeq, timeWP);
    }
}