package annak.lab1;

import annak.lab1.tests.CpuTests;
import annak.lab1.tests.IoTests;
import annak.lab1.tests.MemoryTests;

import static annak.lab1.Utils.measureTime;
import static annak.lab1.Utils.printSpeedup;

public class Main {

    public static final long PI_ITERATIONS = 50_000_000L;
    private static final long FACTOR_NUMBER = 5_223_372_036_854_775_783L;
    private static final int PRIME_MAX = 1_000_000;

    private static final int MATRIX_SIZE = 10_000;

    private static final int FILES_COUNT = 1000;
    private static final String DIR_NAME = "lab1_test_files";

    public static void main(String[] args) {
        int cores = Runtime.getRuntime().availableProcessors();
        int[] threadsConfig = {2, 4, cores, cores * 2};

        System.out.println("\n####### TEST 1: CPU-BOUND #######");
        CpuTests.runTest("1.1. Pi Monte-Carlo", threadsConfig,
                CpuTests.PiMonteCarlo::runSequential, CpuTests.PiMonteCarlo::runParallel);
        CpuTests.runTest("1.2. Factorization", threadsConfig,
                () -> CpuTests.Factorization.runSequential(FACTOR_NUMBER),
                (t) -> CpuTests.Factorization.runParallel(FACTOR_NUMBER, t));
        CpuTests.runTest("1.3. Prime Numbers", threadsConfig,
                () -> CpuTests.PrimeNumbers.runSequential(PRIME_MAX),
                (t) -> CpuTests.PrimeNumbers.runParallel(PRIME_MAX, t));

        System.out.println("\n####### TEST 2: MEMORY-BOUND #######");
        double[][] matrix = new double[MATRIX_SIZE][MATRIX_SIZE];
        long timeSeqMem = measureTime("--- Sequential",
                () -> MemoryTests.TransposeMatrix.runSequential(matrix));
        for (int threads : threadsConfig) {
            long timeParMem = measureTime("--- Parallel (" + threads + ")",
                    () -> MemoryTests.TransposeMatrix.runParallel(matrix, threads));
            printSpeedup(timeSeqMem, timeParMem);
        }

        System.out.println("\n####### TEST 3: I/O-BOUND #######");
        if (IoTests.WordsCounter.prepareTestFiles(DIR_NAME, FILES_COUNT))
            System.out.println("Test files were generated");
        long timeSeqIo = measureTime("--- Sequential",
                () -> IoTests.WordsCounter.runSequential(DIR_NAME));
        for (int threads : threadsConfig) {
            long timeParIo = measureTime("--- Parallel (" + threads + ")",
                    () -> IoTests.WordsCounter.runParallel(DIR_NAME, threads));
            printSpeedup(timeSeqIo, timeParIo);
        }
    }
}