package annak.lab1.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static annak.lab1.Main.PI_ITERATIONS;
import static annak.lab1.Utils.measureTime;
import static annak.lab1.Utils.printSpeedup;

public class CpuTests {

    public static void runTest(String name, int[] threadsConfig, SequentialTask seq, ParallelTask par) {
        System.out.println("\n>>>>> Running " + name);
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

    public static class PiMonteCarlo {

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

    public static class Factorization {

        public static void runSequential(long n) {
            long limit = (long) Math.sqrt(n);
            long count = 0;
            for (long i = 2; i <= limit; i++) {
                if (n % i == 0) {
                    count++;
                }
            }
        }

        public static void runParallel(long n, int threads) throws Exception {
            ExecutorService ex = Executors.newFixedThreadPool(threads);
            long limit = (long) Math.sqrt(n);
            long step = limit / threads;

            for (int i = 0; i < threads; i++) {
                final long start = 2 + i * step;
                final long end = (i == threads - 1) ? limit : start + step;
                ex.submit(() -> {
                    long count = 0;
                    for (long k = start; k < end; k++) {
                        if (n % k == 0) {
                            count++;
                        }
                    }
                });
            }
            ex.shutdown();
            ex.awaitTermination(10, TimeUnit.MINUTES);
        }
    }

    public static class PrimeNumbers {

        public static void runSequential(int max) {
            int count = 0;
            for (int i = 2; i <= max; i++) {
                if (isPrime(i)) {
                    count++;
                }
            }
        }

        public static void runParallel(int max, int threads) throws Exception {
            ExecutorService ex = Executors.newFixedThreadPool(threads);
            int step = max / threads;
            for (int i = 0; i < threads; i++) {
                final int start = 2 + i * step;
                final int end = (i == threads - 1) ? max : start + step;
                ex.submit(() -> {
                    int count = 0;
                    for (int k = start; k < end; k++) {
                        if (isPrime(k)) {
                            count++;
                        }
                    }
                });
            }
            ex.shutdown();
            ex.awaitTermination(10, TimeUnit.MINUTES);
        }

        private static boolean isPrime(int n) {
            if (n < 2) return false;
            for (int i = 2; i * i <= n; i++) {
                if (n % i == 0) return false;
            }
            return true;
        }
    }
}
