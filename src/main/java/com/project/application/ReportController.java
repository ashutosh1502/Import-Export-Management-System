package com.project.application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
//import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReportController {

    public ReportController(String sectionName) {
//        System.out.println("Fetched report controller"); // Debug-test

        String pythonExecutable = "python";
        String pythonScriptPath;

        if(sectionName.equals("Unitech Industries")){
            pythonScriptPath="src/main/resources/scripts/unitech_ind_imports_data_analysis.py";
        }
        else
            pythonScriptPath="src/main/resources/scripts/surya_ind_imports_data_analysis.py";

        // Create the JavaFX loading stage
        Stage loadingStage = new Stage();
        Label loading = new Label("      Launching Data Analysis Module...");
        Scene loadingScene = new Scene(loading, 300, 70);
        loadingStage.setTitle("Loading");
        loadingStage.setScene(loadingScene);
        loadingStage.centerOnScreen();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.show();

        // Loading animation thread
//         loadingThread = new Thread(() -> {
//            String[] animation = {"|", "/", "-", "\\"};
//            int i = 0;
//            while (isRunning[0]) {
//                final String loadingText = "      Launching Data Analysis Module... " + animation[i % animation.length];
//                i++;
//                // Update the UI on the JavaFX Application Thread
//                Platform.runLater(() -> loading.setText(loadingText));
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//            // Once the process finishes, update the UI
//            Platform.runLater(() -> {
//                loading.setText("      Report generation completed!");
//                loadingStage.close(); // Close the loading window
//            });
//        });
//        loadingThread.start();
        // Replace the loadingThread logic
        var ref = new Object() {
            boolean isRunning = true;
        };
        Platform.runLater(() -> {
            Timeline timeline = getTimeline(loading);

            // Stop the timeline when the process finishes
            new Thread(() -> {
                try {
                    Thread.sleep(2500); // Simulating task execution delay
                } catch (InterruptedException ignored) {}
                ref.isRunning = false;
                Platform.runLater(timeline::stop);
            }).start();
        });


        // Python script execution thread
        String finalPythonScriptPath = pythonScriptPath;
        new Thread(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable, finalPythonScriptPath);

                Process process = processBuilder.start();
                Thread.sleep(2500);
                ref.isRunning=false;
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                StringBuilder errorOutput = new StringBuilder();
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }

                int exitCode = process.waitFor();

                if (exitCode != 0) {
//                    System.err.println("\nPython script exited with an error (code " + exitCode + "):");
                    System.err.println(errorOutput);
                    Platform.runLater(() -> loading.setText("An error occurred during report generation."));
                }
            } catch (Exception e) {
//                System.err.println("An error occurred while running the Python script:");
                e.printStackTrace();
                Platform.runLater(() -> loading.setText("An error occurred while running the script."));
            } finally {
                ref.isRunning = false;
                Platform.runLater(loadingStage::close);
            }
        }).start();
    }

    private static Timeline getTimeline(Label loading) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(200), e -> {
                    final String[] animation = {"|", "/", "-", "\\"};
                    int i = (int) (System.currentTimeMillis() / 200) % animation.length;
                    String loadingText = "      Launching Data Analysis Module... " + animation[i];
                    loading.setText(loadingText);
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        return timeline;
    }
}
