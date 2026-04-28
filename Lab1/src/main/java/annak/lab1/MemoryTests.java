package annak.lab1;

import java.util.concurrent.*;

public class MemoryTests {

    static class TransposeMatrix {

        public static void runSequential(double[][] matrix) {
            int n = matrix.length;
            double[][] transposed = new double[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    transposed[j][i] = matrix[i][j];
                }
            }
        }

        public static void runParallel(double[][] matrix, int threads) {
            int n = matrix.length;
            double[][] transposed = new double[n][n];
            ExecutorService ex = Executors.newFixedThreadPool(threads);
            int step = n / threads;
            for (int i = 0; i < threads; i++) {
                final int start = i * step;
                final int end = (i == threads - 1) ? n : (i + 1) * step;
                ex.submit(() -> {
                    for (int r = start; r < end; r++) {
                        for (int c = 0; c < end; c++) {
                            transposed[c][r] = matrix[r][c];
                        }
                    }
                });
            }
            ex.shutdown();
            try {
                ex.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                System.out.println("ERROR: " + e.getMessage());
                System.exit(-1);
            }
        }
    }
}
