package com.project.application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SelectSection extends Application {
    private final HBox sectionPane = new HBox(30);
    private final Button section1 = new Button("Unitech Industries");
    private final Button section2 = new Button("Surya Industries");
    private String sectionName;

    @Override
    public void start(Stage primaryStage) {
        section1.setPrefWidth(250);
        section1.setStyle("-fx-background-color: #a9cce3;-fx-border-color: #7fb3d5;-fx-text-fill: white;-fx-font-size: 24px;-fx-font-weight: bold;-fx-padding: 10;-fx-border-width: 4px;-fx-opacity:0.9;");
        section1.setOnMouseEntered(e -> section1.setStyle("-fx-opacity:1;-fx-border-color: #5499c7;-fx-background-color: #2980b9;-fx-text-fill: white;-fx-font-size: 24px;-fx-font-weight: bold;-fx-padding: 10;-fx-border-width: 4px;"));
        section1.setOnMouseExited(e -> section1.setStyle("-fx-background-color: #a9cce3;-fx-border-color: #7fb3d5;-fx-text-fill: white;-fx-font-size: 24px;-fx-font-weight: bold;-fx-padding: 10;-fx-border-width: 4px;-fx-opacity:0.9;"));

        section2.setPrefWidth(250);
        section2.setStyle("-fx-background-color: #a9cce3;-fx-border-color: #7fb3d5;-fx-text-fill: white;-fx-font-size: 24px;-fx-font-weight: bold;-fx-padding: 10;-fx-border-width: 4px;-fx-opacity:0.9;");
        section2.setOnMouseEntered(e -> section2.setStyle("-fx-opacity:1;-fx-border-color: #5499c7;-fx-background-color: #2980b9;-fx-text-fill: white;-fx-font-size: 24px;-fx-font-weight: bold;-fx-padding: 10;-fx-border-width: 4px;"));
        section2.setOnMouseExited(e -> section2.setStyle("-fx-background-color: #a9cce3;-fx-border-color: #7fb3d5;-fx-text-fill: white;-fx-font-size: 24px;-fx-font-weight: bold;-fx-padding: 10;-fx-border-width: 4px;-fx-opacity:0.9;"));

        section1.setFocusTraversable(false);
        section2.setFocusTraversable(false);

        section1.setOnAction(e -> handleSectionSelection(primaryStage, section1.getText().trim()));
        section2.setOnAction(e -> handleSectionSelection(primaryStage, section2.getText().trim()));

        sectionPane.getChildren().addAll(section1, section2);
        sectionPane.setPadding(new Insets(30, 30, 30, 30));

        Scene mainScene = new Scene(sectionPane);

        primaryStage.setTitle("Select Section");
        primaryStage.setScene(mainScene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void handleSectionSelection(Stage primaryStage, String section) {
        sectionName = section;
        Stage loaderStage = showLoader(primaryStage);

        new Thread(() -> {
            try {
                // Ensure MainPage is loaded on JavaFX thread
                Platform.runLater(() -> {
                    try {
                        new MainPage(sectionName); // Load MainPage
                        loaderStage.close(); // Close the loader once MainPage is loaded
                        primaryStage.close();
                    } catch (Exception ex) {
                        loaderStage.close();
                        showError("Failed to load MainPage.");
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    loaderStage.close();
                    showError("An error occurred while loading MainPage.");
                });
            }
        }).start();
    }

    // Helper function to show an alert in case of errors
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }


    protected Stage showLoader(Stage owner) {
        Stage loaderStage = new Stage();
        loaderStage.initModality(Modality.WINDOW_MODAL);
        loaderStage.initOwner(owner);
        loaderStage.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");
        root.setAlignment(javafx.geometry.Pos.CENTER);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        Label loadingLabel = new Label("Loading, please wait...");
        loadingLabel.setStyle("-fx-font-size: 14px;");

        root.getChildren().addAll(progressIndicator, loadingLabel);

        Scene scene = new Scene(root, 250, 150);
        loaderStage.setScene(scene);
        loaderStage.show();

        return loaderStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
