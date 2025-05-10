package com.project.application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ReportController {

    public ReportController(String sectionName) {
        String pythonExecutable = "python";
        String pythonScriptPath;

        if ("Unitech Industries".equals(sectionName)) {
            pythonScriptPath = "src/main/resources/scripts/unitech_ind_imports_data_analysis.py";
        } else {
            pythonScriptPath = "src/main/resources/scripts/surya_ind_imports_data_analysis.py";
        }

        // Create loading UI
        Stage loadingStage = new Stage();
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: white;");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        Label loadingLabel = new Label("Loading, please wait...");
        loadingLabel.setStyle("-fx-font-size: 14px;");

        root.getChildren().addAll(progressIndicator, loadingLabel);
        Scene loadingScene = new Scene(root, 250, 150);
        loadingStage.setScene(loadingScene);
        loadingStage.setTitle("Loading");
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.centerOnScreen();
        loadingStage.show();

        // Start animation
        Timeline timeline = getTimeline(loadingLabel);

        // Run Python script in a background thread
        new Thread(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable, pythonScriptPath);
                Process process = processBuilder.start();

                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorOutput = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }

                int exitCode = process.waitFor();

                Platform.runLater(() -> {
                    timeline.stop(); // Stop the animation
                    if (exitCode == 0) {
                        loadingStage.close();
                    } else {
                        loadingLabel.setText("An error occurred during report generation.");
                        System.err.println("Python error:\n" + errorOutput);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    timeline.stop();
                    loadingLabel.setText("An error occurred while running the script.");
                });
            }
        }).start();
    }

    private static Timeline getTimeline(Label loadingLabel) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(200), e -> {
                    final String[] animation = {"|", "/", "-", "\\"};
                    int i = (int) (System.currentTimeMillis() / 200) % animation.length;
                    loadingLabel.setText("      Launching Data Analysis Module... " + animation[i]);
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        return timeline;
    }
}
