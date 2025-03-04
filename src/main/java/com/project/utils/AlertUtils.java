package com.project.utils;

import javafx.animation.PauseTransition;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class AlertUtils {
    public static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showMsg(String msg){
        if(msg.isEmpty()) return;
        // Create the notification
        Stage notificationStage = new Stage();
        notificationStage.initStyle(StageStyle.UNDECORATED);
        notificationStage.setAlwaysOnTop(true);

        Label messageLabel = new Label(msg);
        messageLabel.setStyle("-fx-background-color: #000; -fx-text-fill: white; -fx-padding: 10px; -fx-border-radius: 5; -fx-background-radius: 5;");

        StackPane root = new StackPane(messageLabel);
        root.setStyle("-fx-padding: 10px;");
        Scene scene = new Scene(root);
        notificationStage.setScene(scene);

//        // Position the notification
//        notificationStage.setX(primaryStage.getX() + primaryStage.getWidth() / 2 - 100);
//        notificationStage.setY(primaryStage.getY() + primaryStage.getHeight() / 2 - 50);

        // Show the notification
        notificationStage.show();

        // Close the notification after 2 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> notificationStage.close());
        delay.play();
    }
}
