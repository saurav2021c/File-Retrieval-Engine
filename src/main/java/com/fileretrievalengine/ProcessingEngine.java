package com.fileretrievalengine;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ProcessingEngine {
    private final IndexStore indexStore;        // Shared index store for all threads
    private final ExecutorService executorService; // Thread pool for managing indexing tasks

    // Constructor to initialize the index store and thread pool
    public ProcessingEngine(int numberOfThreads) {
        this.indexStore = new IndexStore(); // Initialize the index store
        this.executorService = Executors.newFixedThreadPool(numberOfThreads); // Create a fixed thread pool
    }

    // Method to index a dataset and measure the time taken
    public void indexDataset(String datasetPath) {
        System.out.println("Indexing started..."); // Indicate the start of indexing
        File datasetDirectory = new File(datasetPath); // Create a File object for the dataset path

        if (datasetDirectory.isDirectory()) { // Check if the path is a directory
            long startTime = System.nanoTime();  // Start time for performance measurement

            // Submit indexing tasks for each folder in the dataset
            for (File folder : datasetDirectory.listFiles()) {
                if (folder.isDirectory()) {
                    executorService.submit(new IndexTask(folder, indexStore)); // Submit a task for indexing the folder
                }
            }

            executorService.shutdown();  // Prevent accepting new tasks
            try {
                // Wait for all indexing tasks to complete
                if (!executorService.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();  // Force shutdown if not finished
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow(); // Force shutdown on interruption
                Thread.currentThread().interrupt(); // Preserve the interrupt status
            }

            long endTime = System.nanoTime();  // End time for performance measurement
            double timeTakenInSeconds = (endTime - startTime) / 1_000_000_000.0;  // Convert nanoseconds to seconds

            System.out.printf("Total time taken to index: %.2f seconds%n", timeTakenInSeconds); // Print the time taken
        } else {
            System.out.println("Provided path is not a directory."); // Handle case when path is invalid
        }
    }

    // Method to search for files containing the given query
    public List<String> search(String query) {
        Map<String, Integer> results = indexStore.search(query); // Search the index for the query
        return results.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // Sort by frequency of terms
                .limit(10) // Limit to top 10 results
                .map(entry -> new File(entry.getKey()).getName())  // Extract the file names
                .collect(Collectors.toList()); // Collect results into a list
    }

    // Method to shut down the executor service
    public void shutdown() {
        executorService.shutdown(); // Shut down the thread pool
    }
}
