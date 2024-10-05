package com.fileretrievalengine;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RESTServer {
    private final int port; // Port for the REST server
    private final com.fileretrievalengine.ProcessingEngine processingEngine; // Processing engine for handling requests
    private HttpServer server; // The HTTP server instance

    // Constructor for REST server initialization
    public RESTServer(int port, com.fileretrievalengine.ProcessingEngine processingEngine) {
        this.port = port;
        this.processingEngine = processingEngine; // Initialize with the provided processing engine
    }

    // Method to start the REST server
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0); // Create a new server instance
            server.createContext("/index", new IndexHandler()); // Set up the index handler
            server.createContext("/search", new SearchHandler()); // Set up the search handler
            server.setExecutor(null); // Use default executor
            server.start(); // Start the server
            System.out.println("REST server started at port: " + port);
        } catch (IOException e) {
            System.out.println("Failed to start REST server: " + e.getMessage());
        }
    }

    // Method to stop the REST server
    public void stop() {
        if (server != null) {
            server.stop(0); // Stop the server
            System.out.println("REST server stopped.");
        }
    }

    // Handler for the /index request
    private class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Read the request body
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject(); // Parse the request body

                // Check for dataset_path and handle accordingly
                if (!jsonRequest.has("dataset_path")) {
                    sendResponse(exchange, "{\"error\": \"dataset_path parameter is missing\"}", 400); // Bad request
                    return;
                }

                String datasetPath = jsonRequest.get("dataset_path").getAsString(); // Get dataset path

                processingEngine.indexDataset(datasetPath); // Index the dataset

                // Log request
                logIndexRequest(datasetPath); // Log the indexing request

                // Create a response
                String response = "{\"status\": \"Indexing Completed.\"}";
                sendResponse(exchange, response, 200); // Send the response
            } else {
                sendResponse(exchange, "{\"error\": \"Method not allowed\"}", 405); // Method not allowed
            }
        }

        // Logging method for index requests
        private void logIndexRequest(String datasetPath) {
            JsonObject logEntry = new JsonObject(); // Create a new log entry
            logEntry.addProperty("timestamp", formatTimestamp(java.time.LocalDateTime.now())); // Format and add timestamp
            logEntry.addProperty("request_endpoint", "index"); // Add the endpoint
            JsonObject body = new JsonObject(); // Create a body object
            body.addProperty("dataset_path", datasetPath); // Add the dataset path
            logEntry.add("body", body); // Add the body to the log entry

            System.out.println(logEntry.toString()); // Print the log entry
        }
    }

    // Handler for the /search request
    private class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Read the request body
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject(); // Parse the request body

                // Check for query and handle accordingly
                if (!jsonRequest.has("query")) {
                    sendResponse(exchange, "{\"error\": \"Query parameter is missing\"}", 400); // Bad request
                    return;
                }

                String query = jsonRequest.get("query").getAsString(); // Get the search query

                // Log request in the desired format
                logSearchRequest(query); // Log the search request

                // Execute search and get the file names
                List<String> results = processingEngine.search(query); // Perform search

                // Prepare JSON response
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("timestamp", formatTimestamp(java.time.LocalDateTime.now())); // Format and add timestamp
                jsonResponse.add("top_files", new Gson().toJsonTree(results.isEmpty() ? List.of() : results)); // Add search results

                sendResponse(exchange, jsonResponse.toString(), 200); // Send the response
            } else {
                sendResponse(exchange, "{\"error\": \"Method not allowed\"}", 405); // Method not allowed
            }
        }

        // Logging method for search requests
        private void logSearchRequest(String query) {
            JsonObject logEntry = new JsonObject(); // Create a new log entry
            logEntry.addProperty("timestamp", formatTimestamp(java.time.LocalDateTime.now())); // Format and add timestamp
            logEntry.addProperty("request_endpoint", "search"); // Add the endpoint
            JsonObject body = new JsonObject(); // Create a body object
            body.addProperty("query", query); // Add the query
            logEntry.add("body", body); // Add the body to the log entry

            System.out.println(logEntry.toString()); // Print the log entry
        }
    }

    // Method to format timestamps
    private String formatTimestamp(java.time.LocalDateTime timestamp) {
        return timestamp.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")); // Format timestamp to desired pattern
    }

    // Method to send HTTP responses
    private void sendResponse(HttpExchange exchange, String response, int responseCode) throws IOException {
        exchange.sendResponseHeaders(responseCode, response.getBytes().length); // Set response headers
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes()); // Write response to output stream
        }
    }
}
