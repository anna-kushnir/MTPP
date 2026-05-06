package annak.lab2;

import annak.lab2.task1.ArrayStats;
import annak.lab2.task1.MatrixMultiplier;
import annak.lab2.task1.TagCounter;
import annak.lab2.task2.TransactionSystem;

import java.util.List;

import static annak.lab2.Utils.measureTime;
import static annak.lab2.Utils.printSpeedup;

public class Main {

    private static final int HEATING_ITERATIONS = 5;
    private static final String HTML_DIR = "lab2_html_files";
    private static final int HTML_FILES_COUNT = 2000;
    private static final int THRESHOLD_TAG_COUNTER = 5;

    private static final int ARRAY_SIZE = 10_000_000;
    private static final int THRESHOLD_ARRAY_STATS = 100_000;

    private static final int HEATING_ITERATIONS_MATRIX = 2;
    private static final int MATRIX_SIZE = 1500;

    private static final String TRANSACTIONS_DIR = "lab2_transactions_csv_files";
    private static final int TRANSACTIONS_FILES_COUNT = 1_000;
    private static final int TRANSACTIONS_LINES_PER_FILE = 10_000;

    private static final int THREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        System.out.println("\nLogic cores count: " + THREADS);
        System.out.println("\n####### TEST 1: Map-Reduce vs Fork-Join vs Worker Pool #######");
        runTagCounterTests();
        runArrayStatsTests();
        runMatrixTests();

        System.out.println("\n\n####### TEST 2: Pipeline vs Producer-Consumer #######");
        runTransactionTests();
    }

    private static void runTagCounterTests() {
        if (TagCounter.prepareHtmlFiles(HTML_DIR, HTML_FILES_COUNT))
            System.out.println("Html files were generated");
        final List<String> htmlDocs = TagCounter.loadDocuments(HTML_DIR);

        runPatternTests("1.1. Tag Counter",
                HEATING_ITERATIONS,
                () -> TagCounter.countSequential(htmlDocs),
                () -> TagCounter.countMapReduce(htmlDocs),
                () -> TagCounter.countForkJoin(htmlDocs, THRESHOLD_TAG_COUNTER),
                () -> TagCounter.countWorkerPool(htmlDocs, THREADS)
        );
    }

    private static void runArrayStatsTests() {
        double[] array = ArrayStats.generateArray(ARRAY_SIZE);

        runPatternTests("1.2. Array Statistics",
                HEATING_ITERATIONS,
                () -> ArrayStats.statsSequential(array),
                () -> ArrayStats.statsMapReduce(array),
                () -> ArrayStats.statsForkJoin(array, THRESHOLD_ARRAY_STATS),
                () -> ArrayStats.statsWorkerPool(array, THREADS)
        );
    }

    private static void runMatrixTests() {
        double[][] a = MatrixMultiplier.generateMatrix(MATRIX_SIZE);
        double[][] b = MatrixMultiplier.generateMatrix(MATRIX_SIZE);

        runPatternTests("1.3. Matrix Multiplication",
                HEATING_ITERATIONS_MATRIX,
                () -> MatrixMultiplier.multiplySequential(a, b),
                () -> MatrixMultiplier.multiplyMapReduce(a, b),
                () -> MatrixMultiplier.multiplyForkJoin(a, b),
                () -> MatrixMultiplier.multiplyWorkerPool(a, b, THREADS)
        );
    }

    private static void runPatternTests(String testName, int heatingIter, Runnable seqTask,
                                        Runnable mrTask, Runnable fjTask, Runnable wpTask) {
        System.out.println("\n>>>>> Running " + testName);

        long timeSeq = 0, timeMR = 0, timeFJ = 0, timeWP = 0;

        for (int i = 1; i <= heatingIter; i++) {
            timeSeq = measureTime("--- Sequential", seqTask, i == heatingIter);
        }

        for (int i = 1; i <= heatingIter; i++) {
            timeMR = measureTime("--- Map-Reduce", mrTask, i == heatingIter);
        }
        printSpeedup(timeSeq, timeMR);

        for (int i = 1; i <= heatingIter; i++) {
            timeFJ = measureTime("--- Fork-Join", fjTask, i == heatingIter);
        }
        printSpeedup(timeSeq, timeFJ);

        for (int i = 1; i <= heatingIter; i++) {
            timeWP = measureTime("--- Worker Pool", wpTask, i == heatingIter);
        }
        printSpeedup(timeSeq, timeWP);
    }

    private static void runTransactionTests() {
        System.out.println("\n>>>>> Running 2. Financial Transactions System");
        if (TransactionSystem.prepareCsvFiles(TRANSACTIONS_DIR, TRANSACTIONS_FILES_COUNT, TRANSACTIONS_LINES_PER_FILE))
            System.out.println("CSV-files with transactions were generated");

        long timeSeq = measureTime("--- Sequential",
                () -> TransactionSystem.processSequential(TRANSACTIONS_DIR), true);

        long timePp = measureTime("--- Pipeline",
                () -> TransactionSystem.processPipeline(TRANSACTIONS_DIR, THREADS), true);
        printSpeedup(timeSeq, timePp);

        long timePC = measureTime("--- Producer-Consumer",
                () -> TransactionSystem.processProducerConsumer(TRANSACTIONS_DIR, THREADS), true);
        printSpeedup(timeSeq, timePC);
    }
}