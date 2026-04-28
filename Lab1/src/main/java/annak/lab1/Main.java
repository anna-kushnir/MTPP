package annak.lab1;

public class Main {
    public static void main(String[] args) {
        int cores = Runtime.getRuntime().availableProcessors();
        int[] threadsConfig = {2, 4, cores, cores * 2};

        System.out.println("\n####### TEST 1: CPU-BOUND #######");
        CpuTests.runTest("1.1. Pi Monte-Carlo", threadsConfig,
                CpuTests.PiMonteCarlo::runSequential, CpuTests.PiMonteCarlo::runParallel);
    }
}