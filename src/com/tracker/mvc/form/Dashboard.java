/*
 * Fluent-style Dashboard with JavaFX header inside scroll:
 * - Greeting (left) + quote (right) in a full-width card
 * - Quote picked once per app start
 * - Swing container + JavaFX content
 */
package com.tracker.mvc.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.tracker.mvc.controller.TaskController;
import com.tracker.mvc.dto.TaskDto;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import org.json.JSONObject;

public class Dashboard extends JPanel {
    
    // -------- Data / controller --------
    private TaskController taskController;
    private final List<String> quotes = new ArrayList<>();
    private final Random random = new Random();

    // Quote & greeting for this app run
    private String dailyQuote;
    private String greetingText;

    // -------- Swing UI --------
    private JFXPanel fxDashboardPanel;

    // -------- JavaFX task containers --------
    private VBox fxTodayTasksBox;
    private VBox fxUpcomingTasksBox;
    
    // -------- Header UI elements --------
    private Label remindersLbl;
    private Timeline reminderTimeline;
    private List<String> currentReminders = new ArrayList<>();
    private int currentReminderIndex = 0;

    public Dashboard() {
        // FlatLaf theme defaults
        FlatLaf.registerCustomDefaultsSource("com.tracker.mvc.themes");
        
        setOpaque(false); // Transparent to show main gradient

        initSwingUI(); // Swing container (only center JFXPanel)

        // Load quotes first so we can pick dailyQuote
        loadQuotesFromJsonl();
        dailyQuote = getPlainQuoteWithAuthor();
        greetingText = getTimeBasedGreeting();

        // FIX: initialize controller BEFORE JavaFX builds header
        try {
            this.taskController = new TaskController();
        } catch (Exception ex) {
            System.out.println("Error initializing TaskController: " + ex.getMessage());
        }

        // Now JavaFX has access to valid tasks
        initDashboardJavaFX();

        loadTasks();
    }

    // Public: for layered pane overlay
    public JFXPanel getFxOverlayPanel() {
        return null;
    }

    // Public: external access to same daily quote
    public JLabel getQuoteLabel() {
        JLabel temp = new JLabel();
        String text = (dailyQuote != null && !dailyQuote.isBlank())
                ? dailyQuote
                : getPlainQuoteWithAuthor();
        temp.setText(text);
        temp.setFont(new java.awt.Font("Georgia", java.awt.Font.ITALIC, 14));
        temp.setForeground(new Color(120, 120, 120));
        return temp;
    }

    // ====================================================================== 
    // JavaFX overlay (for FXPopupHelper)
    // ====================================================================== 
    private void initOverlayJavaFX() {
        // Overlay popup logic removed
    }

    // ====================================================================== 
    // Swing container (just hosts JavaFX board)
    // ====================================================================== 
    private void initSwingUI() {
        setLayout(new BorderLayout());
        
        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setOpaque(false); 
        centerCard.setBackground(new Color(0, 0, 0, 0));
        centerCard.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel fxContainer = new JPanel(new BorderLayout());
        fxContainer.setOpaque(false); 

        fxDashboardPanel = new JFXPanel();
        fxDashboardPanel.setMinimumSize(new Dimension(0, 0));

        fxContainer.add(fxDashboardPanel, BorderLayout.CENTER);
        centerCard.add(fxContainer, BorderLayout.CENTER);

        add(centerCard, BorderLayout.CENTER);
    }

    // ====================================================================== 
    // JavaFX content: header card + task board (inside scroll)
    // ====================================================================== 
    private void initDashboardJavaFX() {
        Platform.runLater(() -> {
            fxTodayTasksBox = new VBox(12);
            fxUpcomingTasksBox = new VBox(12);
            fxTodayTasksBox.setPadding(new Insets(8));
            fxUpcomingTasksBox.setPadding(new Insets(8));

            // -------- Header card (greeting + quote) --------
            Label greetingLbl = new Label(greetingText);
            greetingLbl.setStyle(
                    "-fx-font-family: 'Poppins';" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 24;" +
                            "-fx-text-fill: #1e293b;" + // Dark slate
                            "-fx-wrap-text: false;");
            greetingLbl.setMaxWidth(Double.MAX_VALUE);
            greetingLbl.setMinWidth(Region.USE_PREF_SIZE);

            // QUOTE
            Label quoteLbl = new Label("“" + dailyQuote + "”");
            quoteLbl.setWrapText(true);
            quoteLbl.setMaxWidth(450);
            quoteLbl.setStyle(
                    "-fx-font-family: 'Georgia';" +
                            "-fx-font-style: italic;" +
                            "-fx-font-size: 14;" +
                            "-fx-text-fill: #64748b;"); // Slate gray

            // Reminders label (Class Field now)
            remindersLbl = new Label();
            remindersLbl.setWrapText(true);
            remindersLbl.setMaxWidth(450);
            remindersLbl.setStyle(
                    "-fx-font-family: 'Poppins';" +
                            "-fx-font-size: 14;" +
                            "-fx-text-fill: #e11d48;" +
                            "-fx-font-weight: bold;");
            remindersLbl.setVisible(false);

            // Initial population of reminders
            updateHeaderReminders();

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox headerRow = new HBox(20, greetingLbl, spacer, quoteLbl);
            headerRow.setAlignment(Pos.CENTER_LEFT);

            // Fade quote out
            FadeTransition fadeQuote = new FadeTransition(Duration.seconds(1.0), quoteLbl);
            fadeQuote.setFromValue(1.0);
            fadeQuote.setToValue(0.0);

            Timeline delayQuote = new Timeline(new KeyFrame(Duration.seconds(5), e -> fadeQuote.play()));
            delayQuote.play();

            fadeQuote.setOnFinished(e -> {
                int idx = headerRow.getChildren().indexOf(quoteLbl);
                headerRow.getChildren().remove(quoteLbl);
                remindersLbl.setVisible(true);
                headerRow.getChildren().add(idx, remindersLbl);

                // Start cycling reminders
                if (!currentReminders.isEmpty()) {
                    remindersLbl.setText(currentReminders.get(0));
                }
                
                reminderTimeline = new Timeline(new KeyFrame(Duration.seconds(4), ev -> {
                    if (currentReminders.isEmpty()) return;
                    currentReminderIndex = (currentReminderIndex + 1) % currentReminders.size();
                    remindersLbl.setText(currentReminders.get(currentReminderIndex));
                }));
                reminderTimeline.setCycleCount(Timeline.INDEFINITE);
                reminderTimeline.play();
            });

            // White Header Card - Fully White
            VBox headerCard = new VBox(headerRow);
            headerCard.setPadding(new Insets(20, 30, 20, 30));
            headerCard.setStyle(
                    "-fx-background-radius: 20;" +
                            "-fx-background-color: #ffffff;" + // Fully white
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0.0, 0, 5);");

            // -------- Task columns --------
            VBox todayColumn = createColumn("Today Tasks", fxTodayTasksBox);
            VBox upcomingColumn = createColumn("Upcoming Tasks", fxUpcomingTasksBox);

            todayColumn.setMinWidth(200);
            upcomingColumn.setMinWidth(200);
            HBox.setHgrow(todayColumn, Priority.ALWAYS);
            HBox.setHgrow(upcomingColumn, Priority.ALWAYS);

            HBox columns = new HBox(20, todayColumn, upcomingColumn);
            columns.setAlignment(Pos.TOP_CENTER);
            columns.setFillHeight(true);

            VBox content = new VBox(20, headerCard, columns);
            content.setPadding(new Insets(10));
            content.setStyle("-fx-background-color: transparent;");
            
            VBox.setVgrow(columns, Priority.ALWAYS);

            ScrollPane scroll = new ScrollPane(content);
            scroll.setFitToWidth(true);
            scroll.setFitToHeight(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            
            // Dark gradient background for the scroll pane area
            scroll.setStyle(
                    "-fx-background: linear-gradient(to bottom, #000000, #434343); " +
                    "-fx-background-color: linear-gradient(to bottom, #000000, #434343); " +
                    "-fx-padding: 0;");

            BorderPane root = new BorderPane(scroll);
            root.setPadding(new Insets(0));
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #000000, #434343);"); 

            Scene scene = new Scene(root);
            scene.setFill(null); // Important for JFXPanel transparency
            fxDashboardPanel.setScene(scene);
        });
    }
    
    private void updateHeaderReminders() {
        // This should run on FX thread if modifying UI directly, 
        // but here we are modifying a list that UI reads from.
        // For safety, let's do list modification on main thread or ensure FX reads it safely.
        // Actually, it's better to update the list and then update text if visible.
        
        List<String> newReminders = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        ArrayList<TaskDto> allTasks = new ArrayList<>();
        
        try {
            if (taskController != null) {
                allTasks = taskController.getAllTasks();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        for (TaskDto task : allTasks) {
            // Skip completed tasks for reminders
            if (task.isCompleted()) continue; 
            
            if (task.getDeadline() != null) {
                boolean isToday = task.getDeadline().equals(today);
                boolean isTomorrow = task.getDeadline().equals(tomorrow);

                if (isToday || isTomorrow) {
                    String when = isToday ? "today" : "tomorrow";
                    newReminders.add("Reminder: '" + task.getSubject() + "' is due " + when + ".");
                }
            }
        }
        
        if (newReminders.isEmpty()) {
            newReminders.add("No urgent reminders.");
        }
        
        // Update the class-level list safely
        // If we are on FX thread, just swap. If not, runLater.
        Platform.runLater(() -> {
            currentReminders = newReminders;
            currentReminderIndex = 0;
            if (remindersLbl != null && remindersLbl.isVisible() && !currentReminders.isEmpty()) {
                remindersLbl.setText(currentReminders.get(0));
            }
        });
    }

    private VBox createColumn(String title, VBox contentBox) {
        Label titleLbl = new Label(title);
        titleLbl.setStyle(
                "-fx-font-family: 'Poppins';" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 18;" +
                        "-fx-text-fill: #1e293b;"); // Dark text for white bg

        VBox column = new VBox(15, titleLbl, contentBox);
        column.setPadding(new Insets(20));
        // White background
        column.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0.0, 0, 3);");
        return column;
    }

    // ====================================================================== 
    // Tasks
    // ====================================================================== 
    public void loadTasks() {
        LocalDate today = LocalDate.now();

        ArrayList<TaskDto> allTasks = new ArrayList<>();
        try {
            if (taskController != null) {
                allTasks = taskController.getAllTasks();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final ArrayList<TaskDto> tasksCopy = allTasks;

        Platform.runLater(() -> {
            if (fxTodayTasksBox == null || fxUpcomingTasksBox == null) {
                return;
            }

            fxTodayTasksBox.getChildren().clear();
            fxUpcomingTasksBox.getChildren().clear();

            boolean hasTodayTasks = false;
            boolean hasUpcomingTasks = false;

            for (TaskDto task : tasksCopy) {
                // Skip completed tasks in columns if desired, or keep them. 
                // Usually dashboard shows pending tasks.
                if (task.isCompleted()) continue;

                if (task.getDeadline() == null)
                    continue;

                if (task.getDeadline().equals(today)) {
                    hasTodayTasks = true;
                    fxTodayTasksBox.getChildren().add(createFxCardForTask(task, true));
                } else if (task.getDeadline().isAfter(today)) {
                    hasUpcomingTasks = true;
                    fxUpcomingTasksBox.getChildren().add(createFxCardForTask(task, false));
                }
            }

            if (!hasTodayTasks) {
                fxTodayTasksBox.getChildren().add(createEmptyFxLabel("No tasks today"));
            }
            if (!hasUpcomingTasks) {
                fxUpcomingTasksBox.getChildren().add(createEmptyFxLabel("No upcoming tasks"));
            }
        });
    }

    private VBox createFxCardForTask(TaskDto task, boolean isToday) {
        String subject = (task.getSubject() == null || task.getSubject().isBlank())
                ? "(No subject)"
                : task.getSubject();

        Label subjectLbl = new Label(subject);
        subjectLbl.setStyle(
                "-fx-font-family: 'Poppins';" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 15;" +
                        "-fx-text-fill: #1e293b;");

        Label descriptionLbl = new Label(task.getDescription() == null ? "" : task.getDescription());
        descriptionLbl.setWrapText(true);
        descriptionLbl.setStyle(
                "-fx-font-family: 'Poppins';" +
                        "-fx-font-size: 12;" +
                        "-fx-text-fill: #64748b;");

        Label deadlineLbl = new Label("Due: " + task.getDeadline());
        deadlineLbl.setStyle(
                "-fx-font-family: 'Poppins';" +
                        "-fx-font-size: 11;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #94a3b8;");

        Label countdownLbl = new Label();
        countdownLbl.setStyle(
                "-fx-font-family: 'Poppins';" +
                        "-fx-font-size: 11;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + (isToday ? "#e11d48;" : "#0ea5e9;") + ";"); // Red for today, blue for upcoming

        updateCountdownLabel(countdownLbl, task.getDeadline());
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> updateCountdownLabel(countdownLbl, task.getDeadline())),
                new KeyFrame(Duration.seconds(1)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        
        HBox metaRow = new HBox(10, deadlineLbl, new Region(), countdownLbl);
        HBox.setHgrow(metaRow.getChildren().get(1), Priority.ALWAYS);

        VBox card = new VBox(8, subjectLbl, descriptionLbl, metaRow);
        card.setPadding(new Insets(15));
        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);
        
        // Left border color based on urgency
        String borderColor = isToday ? "#e11d48" : "#0ea5e9";
        
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 0 0 0 4;" + // Left border only
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0.0, 0, 2);");

        return card;
    }

    private Label createEmptyFxLabel(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-font-family: 'Poppins';" +
                        "-fx-font-size: 13;" +
                        "-fx-text-fill: #94a3b8;");
        label.setPadding(new Insets(10));
        return label;
    }

    private void updateCountdownLabel(Label label, LocalDate deadline) {
        if (deadline == null) {
            label.setText("");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = deadline.atTime(23, 59, 59);

        long secondsLeft = now.until(endOfDay, ChronoUnit.SECONDS);
        if (secondsLeft <= 0) {
            label.setText("Passed");
            return;
        }

        long days = secondsLeft / (24 * 3600);
        long hours = (secondsLeft % (24 * 3600)) / 3600;
        long minutes = (secondsLeft % 3600) / 60;

        String text;
        if (days > 0) text = days + "d left";
        else text = hours + "h " + minutes + "m left";
        
        label.setText(text);
    }

    // ====================================================================== 
    // Quotes
    // ====================================================================== 
    private void loadQuotesFromJsonl() {
        try (InputStream inputStream = getClass().getResourceAsStream("/com/tracker/mvc/resources/quotes.jsonl")) {
            if (inputStream == null) {
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                JSONObject obj = new JSONObject(line);
                String quoteText = obj.optString("quote");
                String author = obj.optString("author", "");
                String fullQuote = author.isEmpty() ? quoteText : quoteText + " – " + author;
                quotes.add(fullQuote);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPlainQuoteWithAuthor() {
        if (quotes.isEmpty()) {
            return "No quotes available";
        }

        List<String> shortQuotes = quotes.stream()
                .filter(q -> q.length() <= 80)
                .collect(Collectors.toList());

        if (shortQuotes.isEmpty()) {
            return "No suitable quotes available";
        }

        int index = random.nextInt(shortQuotes.size());
        String selectedQuote = shortQuotes.get(index);

        // remove leading/trailing quotes if any
        return selectedQuote.replaceAll("^\"|\"$", "");
    }

    // ====================================================================== 
    // Helpers
    // ====================================================================== 
    private String getTimeBasedGreeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour >= 0 && hour < 12) {
            return "Good morning";
        } else if (hour >= 12 && hour < 17) {
            return "Good afternoon";
        } else if (hour >= 17 && hour < 21) {
            return "Good evening";
        } else {
            return "Hello";
        }
    }

    // Refresh from outside
    public void refreshDashboard() {
        loadTasks();
        updateHeaderReminders();
    }
}