package annak.lab2;

import annak.lab2.task1.TagCounter;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static annak.lab2.Utils.measureTime;
import static annak.lab2.Utils.printSpeedup;

public class Main {

    private static final String HTML_DIR = "lab2_html_files";
    private static final int FILES_COUNT = 2000;

    private static final int MATRIX_SIZE = 1000;
    private static final int ARRAY_SIZE = 10_000_000;

    private static final int THREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        System.out.println("\nLogic cores count: " + THREADS);
        runTagCounterTests();
    }

    private static void runTagCounterTests() {
        System.out.println("\n####### TEST 1: Map-Reduce vs Fork-Join vs Worker Pool #######");
        System.out.println("\n>>>>> Running 1.1. Tag Counter");

        if (TagCounter.prepareHtmlFiles(HTML_DIR, FILES_COUNT))
            System.out.println("Html files were generated");
        final List<String> htmlDocs;
        htmlDocs = TagCounter.loadDocuments(HTML_DIR);
        System.out.println("Html files were loaded");

        long timeSeq = measureTime("--- Sequential",
                () -> TagCounter.countSequential(htmlDocs));

        long timeMR = measureTime("--- Map-Reduce",
                () -> TagCounter.countMapReduce(htmlDocs));
        printSpeedup(timeSeq, timeMR);

        long timeFJ = measureTime("--- Fork-Join", () -> {
            ForkJoinPool.commonPool().invoke(
                    new TagCounter.TagCounterTask(htmlDocs, 0, htmlDocs.size())
            );
        });
        printSpeedup(timeSeq, timeFJ);
    }
}