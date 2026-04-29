package annak.lab2;

public class Utils {

    public static long measureTime(String name, Runnable task, boolean print) {
        System.gc();
        long start = System.currentTimeMillis();
        task.run();
        long time = System.currentTimeMillis() - start;
        if (print) System.out.printf("%s: %d ms%n", name, time);
        return time;
    }

    public static void printSpeedup(long s, long p) {
        System.out.printf("    -> Speedup: %.2f%n",
                (double) s / (p == 0 ? 1 : p));
    }
}
