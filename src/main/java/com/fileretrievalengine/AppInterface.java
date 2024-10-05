package com.fileretrievalengine;

import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AppInterface {
    private RESTServer restServer;  // Instance of the REST server

    // Constructor that initializes the AppInterface with the provided config file path
    public AppInterface(String configPath) {
        JsonObject config = loadConfig(configPath);  // Load configuration from JSON file

        // Load the number of threads from the config, default to 4 if not specified
        int numberOfThreads = config.has("num_worker_threads") ? config.get("num_worker_threads").getAsInt() : 4;
        int port = config.get("rest_endpoint_port").getAsInt();  // Get the REST server port

        System.out.println("The application is using " + numberOfThreads + " threads.");

        ProcessingEngine processingEngine = new ProcessingEngine(numberOfThreads);  // Initialize processing engine with threads
        this.restServer = new RESTServer(port, processingEngine);  // Initialize REST server
    }

    // Method to load the configuration from the provided JSON file
    private JsonObject loadConfig(String configPath) {
        try (FileReader reader = new FileReader(configPath)) {
            return JsonParser.parseReader(reader).getAsJsonObject();  // Parse and return the JSON object
        } catch (IOException e) {
            System.out.println("Failed to load configuration: " + e.getMessage());  // Log error message
            System.exit(1);  // Terminate if config loading fails
            return null;  // Unreachable, but required by the compiler
        }
    }

    // Main program loop that starts the REST server and listens for commands
    public void start() {
        restServer.start();  // Start the REST server
        Scanner scanner = new Scanner(System.in);  // Create scanner for user input
        String command;  // Variable to hold user commands

        System.out.println("Welcome to the File Retrieval Engine!");  // Welcome message
        while (true) {
            System.out.print("> ");  // Prompt for user input
            command = scanner.nextLine().trim();  // Read and trim user command

            // Handle the quit command to shut down the server and exit
            if (command.equalsIgnoreCase("quit")) {
                restServer.stop();  // Stop the REST server
                System.out.println("Shutting down...");  // Log shutdown message
                break;  // Exit the loop
            } else {
                System.out.println("Unknown command. Please use 'quit'.");  // Handle unknown commands
            }
        }
        scanner.close();  // Close the scanner to free resources
    }

    // Main method that starts the application
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: mvn exec:java -Dexec.mainClass=com.fileretrievalengine.AppInterface -Dexec.args=<path_to_json_config>");  // Log usage instructions
            return;  // Exit if the argument is invalid
        }

        AppInterface appInterface = new AppInterface(args[0]);  // Initialize AppInterface with config path
        appInterface.start();  // Start the engine
    }
}
