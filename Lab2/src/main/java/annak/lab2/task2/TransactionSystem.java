package annak.lab2.task2;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Stream;

public class TransactionSystem {

    private static final Path END_PROCESSING_FLAG = Paths.get("END_PROCESSING_FLAG");

    public static boolean prepareCsvFiles(String dir_name, int numFiles, int linesPerLine) {
        Path dirPath = Paths.get(dir_name);
        if (Files.exists(dirPath)) {
            return false;
        }

        try {
            Files.createDirectory(dirPath);
            Random rnd = new Random();
            for (int f = 0; f < numFiles; f++) {
                Path filePath = dirPath.resolve("tx_" + f + ".csv");
                try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                    for (int i = 0; i < linesPerLine; i++) {
                        int userId = rnd.nextInt(100);
                        double amount = 10.0 + rnd.nextDouble() * 990.0;
                        String date = LocalDate.now().minusDays(rnd.nextInt(365)).toString();
                        String type = (rnd.nextBoolean()) ? "ELECTRONICS" : "FOOD";

                        String line = String.format(Locale.US, "%d,%.2f,USD,%s,%s\n", userId, amount, date, type);
                        writer.write(line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(-1);
        }
        return true;
    }

    private static double processSingleLine(String csvLine) {
        String[] parts = csvLine.split(",");
        int userId = Integer.parseInt(parts[0]);
        double amount = Double.parseDouble(parts[1]);

        double converted = amount * 40.0;
        double cashback = (userId % 2 == 0) ? converted * 0.2 : 0;

        return converted - cashback;
    }

    // Sequential
    public static double processSequential(String dir_name) {
        double totalSum = 0;
        try (Stream<Path> paths = Files.list(Paths.get(dir_name))) {
            List<Path> files = paths.toList();
            for (Path file : files) {
                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        totalSum += processSingleLine(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return totalSum;
    }

    // Pipeline (CompletableFuture with chunks)
    public static double processPipeline(String dir_name, int threads) {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<CompletableFuture<Double>> futures = new ArrayList<>();

        try (Stream<Path> paths = Files.list(Paths.get(dir_name))) {
            List<Path> allFiles = paths.toList();
            int chunkSize = (int) Math.ceil((double) allFiles.size() / threads);

            for (int i = 0; i < threads; i++) {
                final int start = i * chunkSize;
                final int end = Math.min(start + chunkSize, allFiles.size());

                if (start >= allFiles.size()) break;
                List<Path> chunkFiles = allFiles.subList(start, end);

                futures.add(CompletableFuture.supplyAsync(() -> {
                    double chunkSum = 0;
                    for (Path file : chunkFiles) {
                        try (BufferedReader reader = Files.newBufferedReader(file)) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                chunkSum += processSingleLine(line);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return chunkSum;
                }, executor));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        double totalSum = futures.stream().mapToDouble(CompletableFuture::join).sum();
        executor.shutdown();
        return totalSum;
    }

    // Producer-Consumer (BlockingQueue)
    public static double processProducerConsumer(String dir_name, int threads) {
        BlockingQueue<Path> queue = new ArrayBlockingQueue<>(100);
        DoubleAdder totalSum = new DoubleAdder();

        Thread producer = new Thread(() -> {
            try (Stream<Path> paths = Files.list(Paths.get(dir_name))) {
                paths.forEach(p -> {
                    try {
                        queue.put(p);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });

                for (int i = 0; i < threads; i++) {
                    queue.put(END_PROCESSING_FLAG);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        List<Thread> consumers = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            Thread consumer = new Thread(() -> {
                try {
                    while (true) {
                        Path file = queue.take();
                        if (file == END_PROCESSING_FLAG) break;

                        double localSum = 0;
                        try (BufferedReader reader = Files.newBufferedReader(file)) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                localSum += processSingleLine(line);
                            }
                        }
                        totalSum.add(localSum);
                    }
                } catch (Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                    System.exit(-1);
                }
            });
            consumers.add(consumer);
            consumer.start();
        }

        producer.start();
        try {
            producer.join();
            for (Thread c : consumers) c.join();
        } catch (InterruptedException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(-1);
        }

        return totalSum.sum();
    }
}