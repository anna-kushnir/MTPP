package annak.lab1;

public class Main {

    public static final long PI_ITERATIONS = 50_000_000L;
    private static final long FACTOR_NUMBER = 5_223_372_036_854_775_783L;
    private static final int PRIME_MAX = 1_000_000;

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
    }
}