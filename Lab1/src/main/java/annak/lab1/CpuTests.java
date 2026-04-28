package annak.lab1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static annak.lab1.Utils.measureTime;
import static annak.lab1.Utils.printSpeedup;

public class CpuTests {

    private final static long PI_ITERATIONS = 50_000_000L;

    public static void runTest(String name, int[] threadsConfig, SequentialTask seq, ParallelTask par) {
        System.out.println(">>>>> Running " + name);
        long timeSeq = measureTime("--- Sequential", () -> {
            try {
                seq.run();
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                System.exit(-1);
            }
        });

        for (int threads : threadsConfig) {
            long timePar = measureTime("--- Parallel (" + threads + ")", () -> {
                try {
                    par.run(threads);
                } catch (Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                    System.exit(-1);
                }
            });
            printSpeedup(timeSeq, timePar);
        }
    }

    public interface SequentialTask { void run() throws Exception; }
    public interface ParallelTask { void run(int t) throws Exception; }

    static class PiMonteCarlo {

        public static void runSequential() {
            long inside = 0;
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (long i = 0; i < PI_ITERATIONS; i++) {
                double x = random.nextDouble(), y = random.nextDouble();
                if (x * x + y * y <= 1) inside++;
            }
        }

        public static void runParallel(int threads) throws Exception {
            ExecutorService ex = Executors.newFixedThreadPool(threads);
            List<Callable<Long>> tasks = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                tasks.add(() -> {
                    long count = 0;
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    for (long j = 0; j < PI_ITERATIONS / threads; j++) {
                        double x = random.nextDouble(), y = random.nextDouble();
                        if (x * x + y * y <= 1) count++;
                    }
                    return count;
                });
            }
            ex.invokeAll(tasks);
            ex.shutdown();
        }
    }
}
