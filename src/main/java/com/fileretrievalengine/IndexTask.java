package com.fileretrievalengine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class IndexTask implements Runnable {
    private final File folder;         // The folder to index
    private final com.fileretrievalengine.IndexStore indexStore; // The shared index store

    // Constructor to initialize the IndexTask with a folder and index store
    public IndexTask(File folder, com.fileretrievalengine.IndexStore indexStore) {
        this.folder = folder;
        this.indexStore = indexStore;
    }

    @Override
    public void run() {
        // Iterate through each file in the folder
        for (File file : folder.listFiles()) {
            // Check if it's a valid text file
            if (file.isFile() && file.getName().endsWith(".txt")) {
                try {
                    indexFile(file); // Index the text file
                } catch (IOException e) {
                    System.err.println("Failed to read file " + file.getName() + ": " + e.getMessage());
                }
            } else if (file.isDirectory()) {
                // If the file is a directory, create a new IndexTask for it
                new IndexTask(file, indexStore).run();
            }
        }
    }

    // Method to index a single text file
    private void indexFile(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath()); // Read all lines from the file
        for (String line : lines) {
            String[] terms = line.split("[^a-zA-Z0-9]+"); // Split the line into terms based on non-alphanumeric characters
            for (String term : terms) {
                if (!term.isEmpty()) {
                    indexStore.updateIndex(term.toLowerCase(), file.getAbsolutePath()); // Update the index with the term
                }
            }
        }
    }
}
