package annak.lab2.task1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TagCounter {

    // Sequential
    public static Map<String, Integer> countSequential(List<String> docs) {
        Map<String, Integer> map = new HashMap<>();
        for (String doc : docs) {
            for (String word : doc.split("\\s+")) {
                if (isTag(word)) {
                    map.merge(word, 1, Integer::sum);
                }
            }
        }
        return map;
    }

    // Map-Reduce (Streams)
    public static Map<String, Integer> countMapReduce(List<String> docs) {
        return docs.parallelStream()
                .flatMap(doc -> Arrays.stream(doc.split("\\s+")))
                .filter(TagCounter::isTag)
                .collect(Collectors.toConcurrentMap(t -> t, t -> 1, Integer::sum));
    }

    // Fork-Join
    public static class TagCounterTask extends RecursiveTask<Map<String, Integer>> {
        List<String> docs;
        int start, end;

        public TagCounterTask(List<String> docs, int start, int end) {
            this.docs = docs;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Map<String, Integer> compute() {
            if (end - start <= 2) {
                Map<String, Integer> map = new HashMap<>();
                for (int i = start; i < end; i++) {
                    for (String word : docs.get(i).split("\\s+")) {
                        if (isTag(word)) {
                            map.merge(word, 1, Integer::sum);
                        }
                    }
                    return map;
                }
            }

            int mid = (start + end) / 2;
            TagCounterTask left = new TagCounterTask(docs, start, mid);
            left.fork();
            Map<String, Integer> right = new TagCounterTask(docs, mid, end).compute();
            Map<String, Integer> leftRes = left.join();
            leftRes.forEach((k, v) -> right.merge(k, v, Integer::sum));
            return right;
        }
    }

    private static boolean isTag(String s) {
        return s.startsWith("<") && s.endsWith(">") && s.length() > 2;
    }

    public static boolean prepareHtmlFiles(String dir_name, int count) {
        Path path = Paths.get(dir_name);
        if (Files.exists(path)) return false;
        try {
            Files.createDirectory(path);
            String[] sampleTags = {"<html>", "</html>", "<body>", "</body>",
                    "<div>", "</div>", "<p>", "</p>", "<span>", "</span>",
                    "<a>", "</a>", "<h1>", "</h1>", "<h2>", "</h2>"};
            Random random = new Random();

            for (int i = 0; i < count; i++) {
                StringBuilder sb = new StringBuilder();
                int fileSize = 500 + random.nextInt(5000);
                for (int j = 0; j < fileSize; j++) {
                    if (random.nextInt(10) > 6) {
                        sb.append(sampleTags[random.nextInt(sampleTags.length)]).append(" ");
                    } else {
                        sb.append("text").append(j).append(" ");
                    }
                }
                Files.writeString(path.resolve("file_" + i + ".html"), sb.toString());
            }
        } catch (IOException e) {
            System.out.println("Something went wrong while generating files");
            System.exit(-1);
        }
        return true;
    }

    public static List<String> loadDocuments(String dir_name) {
        try (Stream<Path> paths = Files.list(Paths.get(dir_name))) {
            return paths.map(p -> {
                try {
                    return Files.readString(p);
                } catch (IOException e) {
                    return "";
                }
            }).collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println("Something wrong while loading html files");
            System.exit(-1);
        }
        return List.of();
    }
}
