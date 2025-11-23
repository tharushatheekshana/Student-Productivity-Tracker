package com.tracker.mvc.view;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import javafx.embed.swing.JFXPanel;

/**
 * Sequential toast helper: only one toast visible; others are queued.
 */
public class FXPopupHelper {

    // Card sizing
    private static final double CARD_MIN_W = 300;
    private static final double CARD_MAX_W = 360;
    private static final double H_PADDING = 40;
    private static final int MIN_OVERLAY_H = 140;

    // Layer + swing overlay
    public static AnchorPane toastLayer;
    private static JFXPanel overlayPanel;

    // Queue control
    private static final java.util.ArrayDeque<Runnable> toastQueue = new java.util.ArrayDeque<>();
    private static volatile boolean toastShowing = false;

    public static void init(AnchorPane rootPane, JFXPanel overlay) {
        toastLayer = new AnchorPane();
        toastLayer.setPickOnBounds(false);
        toastLayer.setMouseTransparent(true);
        AnchorPane.setTopAnchor(toastLayer, 24.0);
        AnchorPane.setRightAnchor(toastLayer, 24.0);
        toastLayer.setPrefWidth(CARD_MAX_W);
        rootPane.getChildren().add(toastLayer);

        overlayPanel = overlay;
        if (overlayPanel != null) {
            javax.swing.SwingUtilities.invokeLater(() -> overlayPanel.setVisible(false));
        }
    }

    /**
     * Enqueue a reminder toast. Only one is displayed at a time.
     */
    public static void showReminderFXPopup(String taskName, String dueString) {
        if (toastLayer == null) {
            throw new IllegalStateException("Call init() before showing toasts");
        }
        enqueue(() -> buildAndShowToast(taskName, dueString));
    }

    // ---------- Queue engine ----------
    private static void enqueue(Runnable showTask) {
        synchronized (toastQueue) {
            toastQueue.addLast(showTask);
            if (!toastShowing) {
                toastShowing = true;
                scheduleNext();
            }
        }
    }

    private static void scheduleNext() {
        final Runnable next;
        synchronized (toastQueue) {
            next = toastQueue.pollFirst();
            if (next == null) {
                toastShowing = false;
                return;
            }
        }
        Platform.runLater(next);
    }

    // ---------- Build + lifecycle ----------
    private static void buildAndShowToast(String taskName, String dueString) {
        if (overlayPanel != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                overlayPanel.setVisible(true);
                java.awt.Rectangle b = overlayPanel.getBounds();
                overlayPanel.setBounds(b.x, b.y, b.width, Math.max(MIN_OVERLAY_H, 140));
            });
        }

        // Card UI
        VBox card = new VBox();
        card.setStyle(
                "-fx-background-color: white; "
                + "-fx-background-radius: 14; "
                + "-fx-effect: dropshadow(gaussian, rgba(60,60,60,0.15), 12, 0, 0, 4);"
        );
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setSpacing(6);
        card.setFillWidth(true);
        card.setMinWidth(CARD_MIN_W);
        card.setMaxWidth(CARD_MAX_W);

        Text title = new Text("Deadline Reminder");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 18));
        title.setFill(Color.web("#2E3A59"));

        // Inline message using TextFlow (fix for 3-row issue)
        Text t1 = new Text("Your task '");
        t1.setFont(Font.font("Poppins", 14));

        Text tTask = new Text(taskName);
        tTask.setFont(Font.font("Poppins", FontPosture.ITALIC, 14));

        Text t2 = new Text("' is due " + dueString + ". Don’t forget to complete it!");
        t2.setFont(Font.font("Poppins", 14));

        TextFlow flow = new TextFlow(t1, tTask, t2);
        flow.setTextAlignment(TextAlignment.LEFT);
        flow.setLineSpacing(0);
        // Set width so the paragraph wraps as a single line
        flow.setPrefWidth(CARD_MAX_W - H_PADDING);
        // Ensure the flow can use the full width of card
        flow.setMaxWidth(CARD_MAX_W - H_PADDING);

        Button closeBtn = new Button("\u2715");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #999; -fx-font-size: 12;");
        closeBtn.setFocusTraversable(false);

        StackPane stack = new StackPane(card, closeBtn);
        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(closeBtn, new Insets(5, 5, 0, 0));

        card.getChildren().addAll(title, flow);
        card.setOpacity(0);

        toastLayer.getChildren().add(stack);
        stack.setTranslateY(0); // only one visible

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        closeBtn.setOnAction(e -> fadeOutAndFinish(stack));

        new Thread(() -> {
            try {
                Thread.sleep(3500);
            } catch (InterruptedException ignored) {
            }
            Platform.runLater(() -> fadeOutAndFinish(stack));
        }, "ToastTimer").start();
    }

    private static void fadeOutAndFinish(StackPane toast) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            toastLayer.getChildren().remove(toast);

            if (overlayPanel != null && toastLayer.getChildren().isEmpty()) {
                javax.swing.SwingUtilities.invokeLater(() -> overlayPanel.setVisible(false));
            }
            scheduleNext();
        });
        fadeOut.play();
    }

    // Quote toast (inline message with TextFlow)
    public static void showQuoteFXPopup(String quote) {
        if (toastLayer == null) {
            throw new IllegalStateException("Call init() before showing toasts");
        }
        enqueue(() -> {
            if (overlayPanel != null) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    overlayPanel.setVisible(true);
                    java.awt.Rectangle b = overlayPanel.getBounds();
                    overlayPanel.setBounds(b.x, b.y, b.width, Math.max(140, 140));
                });
            }

            javafx.scene.text.Text title = new javafx.scene.text.Text("Motivation");
            title.setFont(javafx.scene.text.Font.font("Poppins", javafx.scene.text.FontWeight.BOLD, 18));
            title.setFill(javafx.scene.paint.Color.web("#2E3A59"));

            javafx.scene.text.Text t = new javafx.scene.text.Text(quote);
            t.setFont(javafx.scene.text.Font.font("Georgia", javafx.scene.text.FontPosture.ITALIC, 14));

            javafx.scene.text.TextFlow flow = new javafx.scene.text.TextFlow(t);
            double w = CARD_MAX_W - H_PADDING;
            flow.setPrefWidth(w);

            javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(6);
            card.setPadding(new javafx.geometry.Insets(16, 20, 16, 20));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(60,60,60,0.15), 12, 0, 0, 4);");
            card.getChildren().addAll(title, flow);
            card.setMinWidth(CARD_MIN_W);
            card.setMaxWidth(CARD_MAX_W);
            card.setOpacity(0);

            javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("\u2715");
            closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #999; -fx-font-size: 12;");
            closeBtn.setFocusTraversable(false);

            javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane(card, closeBtn);
            javafx.scene.layout.StackPane.setAlignment(closeBtn, javafx.geometry.Pos.TOP_RIGHT);
            javafx.scene.layout.StackPane.setMargin(closeBtn, new javafx.geometry.Insets(5, 5, 0, 0));
            toastLayer.getChildren().add(stack);
            stack.setTranslateY(0);

            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(250), card);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            closeBtn.setOnAction(e -> fadeOutAndFinish(stack));

            new Thread(() -> {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException ignored) {
                }
                javafx.application.Platform.runLater(() -> fadeOutAndFinish(stack));
            }, "QuoteToastTimer").start();
        });
    }

}
