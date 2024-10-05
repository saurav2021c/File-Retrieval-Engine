package com.fileretrievalengine;

import java.util.HashMap;
import java.util.Map;

public class IndexStore {
    private final Map<String, Map<String, Integer>> globalIndex = new HashMap<>(); // Global index to store terms and file occurrences

    // Synchronized method to update the index with a term and its file path
    public synchronized void updateIndex(String term, String filePath) {
        // Create a new entry if the term doesn't exist
        globalIndex.putIfAbsent(term, new HashMap<>());

        // Update the count for the specific file
        Map<String, Integer> fileMap = globalIndex.get(term);
        fileMap.put(filePath, fileMap.getOrDefault(filePath, 0) + 1);  // Increment the occurrence of the term in the file
    }

    // Synchronized method to search for terms in the index
    public synchronized Map<String, Integer> search(String query) {
        String[] terms = query.split(" AND ");  // Split the query into individual terms
        Map<String, Integer> resultMap = new HashMap<>();  // Store results for each file

        // Iterate over each term in the query
        for (String term : terms) {
            // Check if the term exists in the index
            if (globalIndex.containsKey(term)) {
                // Retrieve the file occurrences for the term
                Map<String, Integer> files = globalIndex.get(term);

                // Update the result map with the occurrences
                for (Map.Entry<String, Integer> entry : files.entrySet()) {
                    resultMap.put(entry.getKey(), resultMap.getOrDefault(entry.getKey(), 0) + entry.getValue());
                }
            }
        }

        return resultMap; // Return the aggregated results
    }
}
