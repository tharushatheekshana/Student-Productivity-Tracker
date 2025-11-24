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
    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
        int w = getWidth();
        int h = getHeight();
        java.awt.GradientPaint gp = new java.awt.GradientPaint(
                0, 0, Color.decode("#000000"), // top color
                0, h, Color.decode("#434343") // bottom color
        );

        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
    }

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

    public Dashboard() {
        // FlatLaf theme defaults
        FlatLaf.registerCustomDefaultsSource("com.tracker.mvc.themes");

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
        // showMotivationalPopupEveryStart();
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
        setOpaque(false); // Let paintComponent handle background

        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setOpaque(true);
        centerCard.setBackground(new Color(0, 0, 0, 0));
        centerCard.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel fxContainer = new JPanel(new BorderLayout());
        fxContainer.setOpaque(true);
        fxContainer.setBackground(new Color(240, 244, 255)); // not pure white
        fxContainer.putClientProperty(FlatClientProperties.STYLE,
                "arc:24; " +
                        "borderWidth:1; " +
                        "borderColor:#d3ddf3; " +
                        "background:$Panel.background; " +
                        "shadow:shadowHigh;");

        fxDashboardPanel = new JFXPanel();
        fxDashboardPanel.setPreferredSize(new Dimension(0, 480));

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
            fxTodayTasksBox.setPadding(new Insets(4));
            fxUpcomingTasksBox.setPadding(new Insets(4));

            // -------- Header card (greeting + quote) --------
            Label greetingLbl = new Label(greetingText);
            greetingLbl.setStyle(
                    "-fx-font-family: 'Poppins';" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 22;" +
                            "-fx-text-fill: #0F172A;" +
                            "-fx-wrap-text: false;");
            greetingLbl.setMaxWidth(Double.MAX_VALUE);
            greetingLbl.setMinWidth(Region.USE_PREF_SIZE);

            // QUOTE (this will fade out)
            Label quoteLbl = new Label("“" + dailyQuote + "”");
            quoteLbl.setWrapText(true);
            quoteLbl.setMaxWidth(420);
            quoteLbl.setStyle(
                    "-fx-font-family: 'Georgia';" +
                            "-fx-font-style: italic;" +
                            "-fx-font-size: 13;" +
                            "-fx-text-fill: #475569;");

            // Reminders label (hidden initially, will cycle through reminders)
            Label remindersLbl = new Label();
            remindersLbl.setWrapText(true);
            remindersLbl.setMaxWidth(420);
            remindersLbl.setStyle(
                    "-fx-font-family: 'Poppins';" +
                            "-fx-font-size: 13;" +
                            "-fx-text-fill: #e11d48;" +
                            "-fx-font-weight: bold;");
            remindersLbl.setVisible(false);

            // Prepare reminders: today and tomorrow's tasks
            List<String> reminders = new ArrayList<>();
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
                if (task.getDeadline() != null && !task.getDeadline().isBefore(today)) {
                    String when;
                    if (task.getDeadline().equals(today)) {
                        when = "today";
                    } else if (task.getDeadline().equals(tomorrow)) {
                        when = "tomorrow";
                    } else {
                        when = "on " + task.getDeadline().toString();
                    }
                    String reminderText = "“Your task '" + task.getSubject() + "' is due " + when
                            + ". Don’t forget to complete it!”";
                    reminders.add(reminderText);
                }
            }
            if (reminders.isEmpty()) {
                reminders.add("No reminders");
            }

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox headerRow = new HBox(18, greetingLbl, spacer, quoteLbl);
            headerRow.setAlignment(Pos.CENTER_LEFT);

            // When quote fades out, show reminders in its place
            FadeTransition fadeQuote = new FadeTransition(Duration.seconds(1.5), quoteLbl);
            fadeQuote.setFromValue(1.0);
            fadeQuote.setToValue(0.0);

            Timeline delayQuote = new Timeline(new KeyFrame(Duration.seconds(7), e -> fadeQuote.play()));
            delayQuote.play();

            fadeQuote.setOnFinished(e -> {
                int idx = headerRow.getChildren().indexOf(quoteLbl);
                headerRow.getChildren().remove(quoteLbl);
                remindersLbl.setVisible(true);
                headerRow.getChildren().add(idx, remindersLbl);

                // Cycle through reminders one by one every 3 seconds
                final int[] reminderIndex = { 0 };
                remindersLbl.setText(reminders.get(reminderIndex[0]));
                Timeline reminderTimeline = new Timeline(new KeyFrame(Duration.seconds(3), ev -> {
                    reminderIndex[0] = (reminderIndex[0] + 1) % reminders.size();
                    remindersLbl.setText(reminders.get(reminderIndex[0]));
                }));
                reminderTimeline.setCycleCount(Timeline.INDEFINITE);
                reminderTimeline.play();
            });

            // Fluent gradient header card with color
            VBox headerCard = new VBox(headerRow);
            headerCard.setPadding(new Insets(18, 24, 18, 24));
            headerCard.setStyle(
                    "-fx-background-radius: 22;" +
                            "-fx-background-color: white;" +
                            "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.15), 20, 0.0, 0, 8);");

            // ---- QUOTE fades out after 7 seconds (greeting stays) ----
            // (handled above, only one fadeQuote definition)

            // -------- Task columns --------
            VBox todayColumn = createColumn("Today Tasks", fxTodayTasksBox);
            VBox upcomingColumn = createColumn("Upcoming Tasks", fxUpcomingTasksBox);

            todayColumn.setPrefWidth(380);
            upcomingColumn.setPrefWidth(380);

            HBox columns = new HBox(24, todayColumn, upcomingColumn);
            columns.setAlignment(Pos.TOP_CENTER);
            columns.setFillHeight(true);

            // -------- Scroll content (header + columns) --------
            VBox content = new VBox(18, headerCard, columns);
            content.setPadding(new Insets(18, 18, 18, 18));
            content.setStyle("-fx-background-color: transparent;");

            ScrollPane scroll = new ScrollPane(content);
            scroll.setFitToWidth(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scroll.setStyle(
                    "-fx-background: linear-gradient(to bottom, #000000, #434343); " +
                            "-fx-background-color: linear-gradient(to bottom, #000000, #434343); " +
                            "-fx-background-insets: 0;");

            BorderPane root = new BorderPane(scroll);
            root.setPadding(new Insets(0));
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #000000, #434343);"); // Match sidebar

            Scene scene = new Scene(root);
            fxDashboardPanel.setScene(scene);
        });
    }

    private VBox createColumn(String title, VBox contentBox) {
        Label titleLbl = new Label(title);
        titleLbl.setStyle(
                "-fx-font-family: 'Poppins';" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 18;" +
                        "-fx-text-fill: #111827;");

        VBox column = new VBox(12, titleLbl, contentBox);
        column.setPadding(new Insets(16));
        column.setStyle(
                // softer bluish column card, not harsh white
                "-fx-background-color: rgba(247,249,255,0.92);" +
                        "-fx-background-radius: 24;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.14), 18, 0.0, 0, 8);");
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
                if (task.getDeadline() == null)
                    continue;

                if (task.getDeadline().equals(today)) {
                    hasTodayTasks = true;
                    fxTodayTasksBox.getChildren().add(createFxCardForTask(task));
                } else if (task.getDeadline().isAfter(today)) {
                    hasUpcomingTasks = true;
                    fxUpcomingTasksBox.getChildren().add(createFxCardForTask(task));
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

    private VBox createFxCardForTask(TaskDto task) {
        String subject = (task.getSubject() == null || task.getSubject().isBlank())
                ? "(No subject)"
                : task.getSubject();

        Label subjectLbl = new Label(subject);
        subjectLbl.setStyle(
                "-fx-font-family: 'Poppins';" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 15;" +
                        "-fx-text-fill: #111827;");

        Label descriptionLbl = new Label(task.getDescription() == null ? "" : task.getDescription());
        descriptionLbl.setWrapText(true);
        descriptionLbl.setStyle(
                "-fx-font-family: 'Poppins';" +
                        "-fx-font-size: 13;" +
                        "-fx-text-fill: #4b5563;");

        Label deadlineLbl = new Label("Due: " + task.getDeadline());
        deadlineLbl.setStyle(
                "-fx-font-family: 'Poppins';" +
                        "-fx-font-size: 12;" +
                        "-fx-text-fill: #9ca3af;");

        Label countdownLbl = new Label();
        countdownLbl.setStyle(
                "-fx-font-family: 'Poppins';" +
                        "-fx-font-size: 12;" +
                        "-fx-text-fill: #0a84ff;");

        updateCountdownLabel(countdownLbl, task.getDeadline());
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> updateCountdownLabel(countdownLbl, task.getDeadline())),
                new KeyFrame(Duration.seconds(1)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        VBox card = new VBox(6, subjectLbl, descriptionLbl, deadlineLbl, countdownLbl);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                // softer bluish task card, not pure white
                "-fx-background-color: #FAFCFF;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: rgba(148,163,184,0.35);" +
                        "-fx-border-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.12), 12, 0.0, 0, 4);");

        return card;
    }

    private Label createEmptyFxLabel(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-font-family: 'Poppins';" +
                        "-fx-font-size: 13;" +
                        "-fx-text-fill: #9ca3af;");
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
            label.setText("Deadline passed");
            label.setStyle(
                    "-fx-font-family: 'Poppins';" +
                            "-fx-font-size: 12;" +
                            "-fx-text-fill: #e11d48;");
            return;
        }

        long days = secondsLeft / (24 * 3600);
        long hours = (secondsLeft % (24 * 3600)) / 3600;
        long minutes = (secondsLeft % 3600) / 60;
        long seconds = secondsLeft % 60;

        String text = String.format("%dd %02dh %02dm %02ds", days, hours, minutes, seconds);
        label.setText(text);
    }

    // ======================================================================
    // Quotes
    // ======================================================================
    private void loadQuotesFromJsonl() {
        try (InputStream inputStream = getClass().getResourceAsStream("/com/tracker/mvc/resources/quotes.jsonl")) {
            if (inputStream == null) {
                System.err.println("quotes.jsonl resource not found.");
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
    }
}
