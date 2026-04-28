package annak.lab1.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IoTests {

    public static class WordsCounter {

        public static void runSequential(String dir_path) {
            try (Stream<Path> s = Files.walk(Paths.get(dir_path))) {
                s.filter(Files::isRegularFile).forEach(WordsCounter::countWordsInFile);
            } catch (IOException e) {
                System.out.println("Something wrong with test directory");
                System.exit(-1);
            }
        }

        public static void runParallel(String dir_path, int threads) {
            ExecutorService ex = Executors.newFixedThreadPool(threads);
            try (Stream<Path> s = Files.walk(Paths.get(dir_path))) {
                List<Path> files = s.filter(Files::isRegularFile).collect(Collectors.toList());
                for (Path p : files) {
                    ex.submit(() -> countWordsInFile(p));
                }
                ex.shutdown();
                try {
                    ex.awaitTermination(1, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    System.out.println("ERROR: " + e.getMessage());
                    System.exit(-1);
                }
            } catch (IOException e) {
                System.out.println("Something wrong with test directory");
                System.exit(-1);
            }
        }

        private static void countWordsInFile(Path path) {
            try {
                Files.lines(path).flatMap(l -> Arrays.stream(l.split("\\s+"))).count();
            } catch (IOException e) {
                System.out.println("Something wrong with file: " + path.getFileName());
                System.exit(-1);
            }
        }

        public static boolean prepareTestFiles(String dir_name, int filesCount) {
            Path root = Paths.get(dir_name);
            if (Files.exists(root)) return false;
            try {
                Files.createDirectory(root);
                for (int i = 0; i < 10; i++) {
                    Path subDir = root.resolve("subdir_" + i);
                    Files.createDirectory(subDir);
                    for (int j = 0; j < filesCount / 10; j++) {
                        Files.writeString(subDir.resolve("file_" + j + ".txt"), "word ".repeat(1000));
                    }
                }
            } catch (IOException e) {
                System.out.println("Something went wrong while generating files");
                System.exit(-1);
            }
            return true;
        }
    }
}
