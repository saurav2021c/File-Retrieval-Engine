package com.fileretrievalengine;

import java.time.LocalDateTime; // Import for handling date and time
import java.time.format.DateTimeFormatter; // Import for formatting date and time
import com.google.gson.JsonObject; // Import for using JSON objects

public class RequestLogger {
    public static void logRequest(String endpoint, JsonObject requestBody) {
        JsonObject logEntry = new JsonObject(); // Create a new JSON object for logging

        // Format the timestamp to include only the date and hour:minute
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String formattedTimestamp = LocalDateTime.now().format(formatter); // Get the current time and format it

        logEntry.addProperty("timestamp", formattedTimestamp); // Add formatted timestamp to the log entry
        logEntry.addProperty("request_endpoint", endpoint); // Add the request endpoint to the log entry

        // Create the body JSON object to hold the request details
        JsonObject body = new JsonObject();

        // Log the query only if it exists in the request body
        if (requestBody.has("query")) {
            body.addProperty("query", requestBody.get("query").getAsString());  // Add query if it exists
        }

        // Log the dataset path only if it exists in the request body
        if (requestBody.has("dataset_path")) {
            body.addProperty("dataset_path", requestBody.get("dataset_path").getAsString()); // Log dataset path if it exists
        }

        logEntry.add("body", body); // Add the body details to the log entry

        // Print the log entry as a formatted string
        System.out.println(logEntry.toString()); // Output the log entry
    }
}
