package annak.lab2.task1;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class ArrayStats {

    // Sequential
    public static void statsSequential(double[] arr) {
        double[] sorted = arr.clone();
        Arrays.sort(sorted);
        double min = sorted[0];
        double max = sorted[sorted.length - 1];
        double median = sorted[sorted.length / 2];
        double sum = 0;
        for (double v : arr) sum += v;
        double avg = sum / arr.length;
    }

    // Map-Reduce
    public static void statsMapReduce(double[] arr) {
        double[] sorted = arr.clone();
        Arrays.parallelSort(sorted);
        double median = sorted[sorted.length / 2];

        DoubleSummaryStatistics stats = Arrays.stream(arr).parallel().summaryStatistics();
        double min = stats.getMin();
        double max = stats.getMax();
        double avg = stats.getAverage();
    }

    // Fork-Join
    public static void statsForkJoin(double[] arr, int threshold) {
        double[] sorted = arr.clone();
        Arrays.parallelSort(sorted);
        double median = sorted[sorted.length / 2];

        StatsTask.threshold = threshold;
        Stats result = ForkJoinPool.commonPool().invoke(
                new StatsTask(arr, 0, arr.length));
        double avg = result.sum / arr.length;
    }

    private static class Stats {
        double min, max, sum;
        public Stats(double min, double max, double sum) {
            this.min = min; this.max = max; this.sum = sum;
        }
    }

    private static class StatsTask extends RecursiveTask<Stats> {
        double[] arr;
        int start, end;
        static int threshold;

        StatsTask(double[] arr, int start, int end) {
            this.arr = arr;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Stats compute() {
            if (end - start <= threshold) {
                double min = Double.MAX_VALUE,
                        max = -Double.MAX_VALUE,
                        sum = 0;
                for (int i = start; i < end; i++) {
                    min = Math.min(min, arr[i]);
                    max = Math.max(max, arr[i]);
                    sum += arr[i];
                }
                return new Stats(min, max, sum);
            }
            int mid = (start + end) / 2;
            StatsTask left = new StatsTask(arr, start, mid);
            left.fork();
            Stats rightResult = new StatsTask(arr, mid, end).compute();
            Stats leftResult = left.join();

            return new Stats(
                    Math.min(leftResult.min, rightResult.min),
                    Math.max(leftResult.max, rightResult.max),
                    leftResult.sum + rightResult.sum
            );
        }
    }

    // Worker Pool
    public static void statsWorkerPool(double[] arr, int threads) {
        double[] sorted = arr.clone();
        Arrays.parallelSort(sorted);
        double median = sorted[sorted.length / 2];

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<Stats>> futures = new java.util.ArrayList<>();
        int chunk = arr.length / threads;

        for (int i = 0; i < threads; i++) {
            final int start = i * chunk;
            final int end = (i == threads - 1) ? arr.length : start + chunk;
            futures.add(pool.submit(() -> {
                double min = Double.MAX_VALUE,
                        max = -Double.MAX_VALUE,
                        sum = 0;
                for (int j = start; j < end; j++) {
                    min = Math.min(min, arr[j]); max = Math.max(max, arr[j]); sum += arr[j];
                }
                return new Stats(min, max, sum);
            }));
        }

        try {
            double globalMin = Double.MAX_VALUE,
                    globalMax = -Double.MAX_VALUE,
                    globalSum = 0;
            for (Future<Stats> f : futures) {
                Stats s = f.get();
                globalMin = Math.min(globalMin, s.min);
                globalMax = Math.max(globalMax, s.max);
                globalSum += s.sum;
            }
            double avg = globalSum / arr.length;

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(-1);
        }
        pool.shutdown();
    }

    public static double[] generateArray(int size) {
        return new Random().doubles(size, -1000.0, 1000.0).toArray();
    }
}
