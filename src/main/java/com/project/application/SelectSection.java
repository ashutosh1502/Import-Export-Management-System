package com.project.application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;


public class SelectSection extends Application {
    private final HBox sectionPane=new HBox(30);
    private final Button section1=new Button("Unitech Industries");
    private final Button section2=new Button("Surya Industries");
    private String sectionName;
//    private static final Logger LOGGER=LoggerFactory.getLogger(SelectSection.class);

    @Override
    public void start(Stage primaryStage) {
        section1.setPrefWidth(250);
        section1.setStyle("-fx-background-color: #a9cce3;-fx-border-color: #7fb3d5;-fx-text-fill: white;-fx-font-size: 24px;-fx-font-weight: bold;-fx-padding: 10;-fx-border-width: 4px;-fx-opacity:0.9;");
        section1.setOnMouseEntered(e ->{
            section1.setStyle("-fx-opacity:1;-fx-border-color: #5499c7;-fx-background-color: #2980b9;-fx-text-fill: white;-fx-font-size: 24px;-fx-font-weight: bold;-fx-padding: 10;-fx-border-width: 4px;");
        });
        section1.setOnMouseExited(e -> {
            section1.setStyle("-fx-background-color: #a9cce3;-fx-border-color: #7fb3d5;-fx-text-fill: white;-fx-font-size: 24px;-fx-font-weight: bold;-fx-padding: 10;-fx-border-width: 4px;-fx-opacity:0.9;");
        });
        section2.setPrefWidth(250);
        section2.setStyle("-fx-background-color: #a9cce3;-fx-border-color: #7fb3d5;-fx-text-fill: white;-fx-font-size: 24px;-fx-font-weight: bold;-fx-padding: 10;-fx-border-width: 4px;-fx-opacity:0.9;");
        section2.setOnMouseEntered(e -> {
            section2.setStyle("-fx-opacity:1;-fx-border-color: #5499c7;-fx-background-color: #2980b9;-fx-text-fill: white;-fx-font-size: 24px;-fx-font-weight: bold;-fx-padding: 10;-fx-border-width: 4px;");
        });
        section2.setOnMouseExited(e -> {
            section2.setStyle("-fx-background-color: #a9cce3;-fx-border-color: #7fb3d5;-fx-text-fill: white;-fx-font-size: 24px;-fx-font-weight: bold;-fx-padding: 10;-fx-border-width: 4px;-fx-opacity:0.9;");
        });
        section1.setFocusTraversable(false);
        section2.setFocusTraversable(false);
        section1.setOnAction(e ->{
            try {
                sectionName = section1.getText().trim();
//                System.out.println(sectionName);
                new MainPage(sectionName);
                primaryStage.close();
            } catch (Exception ex) {
//                LOGGER.error("Failed to load MainPage for section: {}", sectionName, ex);
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load MainPage.");
                alert.showAndWait();
            }
        });
        section2.setOnAction(e ->{
            try {
                sectionName = section2.getText().trim();
                new MainPage(sectionName);
                primaryStage.close();
            } catch (Exception ex) {
//                LOGGER.error("Failed to load MainPage for section: {}",sectionName,ex);
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load MainPage.");
                alert.showAndWait();
            }
        });
        sectionPane.getChildren().addAll(section1,section2);
        sectionPane.setPadding(new Insets(30,30,30,30));

        Scene mainScene = new Scene(sectionPane);

        primaryStage.setTitle("Select Section");
        primaryStage.setScene(mainScene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
