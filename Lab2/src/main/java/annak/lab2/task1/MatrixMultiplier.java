package annak.lab2.task1;

import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class MatrixMultiplier {

    // Sequential
    public static double[][] multiplySequential(double[][] a, double[][] b) {
        int n = a.length;
        double[][] c = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++)
                    c[i][j] += a[i][k] * b[k][j];
            }
        }
        return c;
    }

    // Map-Reduce
    public static double[][] multiplyMapReduce(double[][] a, double[][] b) {
        int n = a.length;
        double[][] c = new double[n][n];
        IntStream.range(0, n).parallel().forEach(i -> {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++)
                    c[i][j] += a[i][k] * b[k][j];
            }
        });
        return c;
    }

    // Fork-Join
    public static double[][] multiplyForkJoin(double[][] a, double[][] b) {
        int n = a.length;
        double[][] c = new double[n][n];
        ForkJoinPool.commonPool().invoke(
                new MatrixTask(a, b, c, 0, n));
        return c;
    }

    private static class MatrixTask extends RecursiveAction {
        double[][] a, b, c;
        int rowStart, rowEnd;
        static final int THRESHOLD = 50;

        MatrixTask(double[][] a, double[][] b, double[][] c, int rs, int re) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.rowStart = rs;
            this.rowEnd = re;
        }

        @Override
        protected void compute() {
            if (rowEnd - rowStart <= THRESHOLD) {
                int n = a.length;
                for (int i = rowStart; i < rowEnd; i++) {
                    for (int j = 0; j < n; j++) {
                        for (int k = 0; k < n; k++)
                            c[i][j] += a[i][k] * b[k][j];
                    }
                }
            } else {
                int mid = (rowStart + rowEnd) / 2;
                invokeAll(new MatrixTask(a, b, c, rowStart, mid), new MatrixTask(a, b, c, mid, rowEnd));
            }
        }
    }

    // Worker Pool
    public static double[][] multiplyWorkerPool(double[][] a, double[][] b, int threads) {
        int n = a.length;
        double[][] c = new double[n][n];
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < n; i++) {
            final int row = i;
            pool.execute(() -> {
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < n; k++)
                        c[row][j] += a[row][k] * b[k][j];
                }
            });
        }
        pool.shutdown();
        try {
            pool.awaitTermination(10, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(-1);
        }
        return c;
    }

    public static double[][] generateMatrix(int size) {
        double[][] m = new double[size][size];
        Random rnd = new Random();
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                m[i][j] = rnd.nextDouble();
        return m;
    }
}
