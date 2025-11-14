package com.tracker.mvc.view;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FXPopupHelper {

    public static void showReminderFXPopup(String taskName, String dueString) {
        // Initialize JavaFX platform (required in Swing apps)
        new JFXPanel();
        Platform.runLater(() -> {
            Stage popupStage = new Stage();
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.initModality(Modality.APPLICATION_MODAL);

            Label bell = new Label("\uD83D\uDD14");
            bell.setFont(Font.font("Segoe UI Emoji", 36));

            Label message = new Label(
                    "Title: Deadline Reminder\n"
                    + "Text: \"Your task '" + taskName + "' is due " + dueString + ". Don’t forget to complete it!\""
            );
            message.setFont(Font.font("Times New Roman", 18));
            message.setWrapText(true);
            message.setStyle("-fx-padding: 10 0 0 8;");

            VBox box = new VBox(6, bell, message);
            box.setStyle(
                    "-fx-padding: 24;"
                    + "-fx-background-color: white;"
                    + "-fx-border-radius: 16;"
                    + "-fx-background-radius: 16;"
                    + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.13), 12, 0, 0, 4);"
            );

            Scene scene = new Scene(box, 540, 144);
            popupStage.setScene(scene);
            popupStage.setAlwaysOnTop(true);
            popupStage.centerOnScreen();
            popupStage.show();

            // Auto close after 3 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }
                Platform.runLater(popupStage::close);
            }).start();
        });
    }
}
