/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.tracker.mvc.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.tracker.mvc.controller.TaskController;
import com.tracker.mvc.dto.TaskDto;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import javaswingdev.chart.ModelPieChart;
import javaswingdev.chart.PieChart;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 *
 * @author Theekshana
 */
public class Report extends javax.swing.JPanel {

    private PieChart weeklyChart;
    private TaskController taskController;
    private PieChart productivityChart;
    
    // Summary Labels
    private JLabel lblTotalTasksVal;
    private JLabel lblCompletedVal;
    private JLabel lblOverdueVal;

    /**
     * Creates new form Form1
     */
    public Report() {
        try {
            taskController = new TaskController();
        } catch (Exception e) {
            e.printStackTrace();
        }

        initComponents();        
        jComboBox1.addActionListener(e -> loadTasksAndUpdateChart());
        setupWeeklyChart();      
        setupProductivityChart();

        int currentMonth = LocalDate.now().getMonthValue();
        jComboBox1.setSelectedIndex(currentMonth - 1);

        loadTasksAndUpdateChart(); 

        new Timer(10000, e -> loadTasksAndUpdateChart()).start();

        btnExport.addActionListener(e -> exportToPDF());
    }

    private void initComponents() {
        setOpaque(false);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Header Section ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Progress & Reports");
        titleLabel.setFont(new Font("Poppins", Font.BOLD, 24));
        titleLabel.setForeground(new Color(220, 220, 220)); // Light text for dark theme

        JPanel controlsPanel = new JPanel();
        controlsPanel.setOpaque(false);
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.X_AXIS));

        jComboBox1 = new JComboBox<>(new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" });
        jComboBox1.setFont(new Font("Poppins", Font.PLAIN, 14));
        
        btnExport = new JButton("Export PDF");
        btnExport.setFont(new Font("Poppins", Font.BOLD, 14));
        btnExport.setBackground(new Color(0, 120, 215));
        btnExport.setForeground(Color.WHITE);

        controlsPanel.add(jComboBox1);
        controlsPanel.add(Box.createHorizontalStrut(10));
        controlsPanel.add(btnExport);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(controlsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- Content Section (Summary + Charts) ---
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // 1. Summary Cards Row
        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 20, 0));
        summaryRow.setOpaque(false);
        summaryRow.setMaximumSize(new Dimension(2000, 120)); // Fix height for stats

        lblTotalTasksVal = new JLabel("0");
        lblCompletedVal = new JLabel("0");
        lblOverdueVal = new JLabel("0");

        summaryRow.add(createSummaryCard("Total Tasks", lblTotalTasksVal, new Color(100, 149, 237)));
        summaryRow.add(createSummaryCard("Completed", lblCompletedVal, new Color(144, 238, 144)));
        summaryRow.add(createSummaryCard("Overdue", lblOverdueVal, new Color(255, 99, 71)));

        contentPanel.add(summaryRow);
        contentPanel.add(Box.createVerticalStrut(20));

        // 2. Charts Row
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsRow.setOpaque(false);

        // Wrap charts in styled cards
        chartContainer = new JPanel(new BorderLayout());
        chartContainer1 = new JPanel(new BorderLayout());
        
        // We wrap the chartContainers in Cards to give them the white look
        JPanel chartCard1 = createChartCard("Weekly Progress", chartContainer);
        JPanel chartCard2 = createChartCard("Productivity", chartContainer1);

        chartsRow.add(chartCard1);
        chartsRow.add(chartCard2);

        contentPanel.add(chartsRow);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:#ffffff; border:1,1,1,1,#e5e5e5");
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Poppins", Font.PLAIN, 14));
        titleLbl.setForeground(Color.GRAY);

        valueLabel.setFont(new Font("Poppins", Font.BOLD, 36));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }

    private JPanel createChartCard(String title, JPanel chartPanel) {
        JPanel card = new JPanel(new BorderLayout());
        card.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:#ffffff; border:1,1,1,1,#e5e5e5");
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Poppins", Font.BOLD, 16));
        titleLbl.setHorizontalAlignment(JLabel.CENTER);
        titleLbl.setForeground(Color.DARK_GRAY);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(chartPanel, BorderLayout.CENTER);
        
        return card;
    }

    // Variables declaration 
    private javax.swing.JButton btnExport;
    private javax.swing.JPanel chartContainer;
    private javax.swing.JPanel chartContainer1;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1; // Unused now
    private javax.swing.JLabel jLabel2; // Unused now
    // End of variables declaration

//    private void setupWeeklyChart() {
//        weeklyChart = new PieChart();
//        weeklyChart.setChartType(PieChart.PeiChartType.DONUT_CHART);
//        chartContainer.add(weeklyChart, BorderLayout.CENTER);
//        chartContainer.revalidate();
//        chartContainer.repaint();
//    }
    // ---- Chart setup ----
    private void setupWeeklyChart() {
        weeklyChart = new PieChart();
        weeklyChart.setChartType(PieChart.PeiChartType.DONUT_CHART);
        chartContainer.add(weeklyChart, BorderLayout.CENTER);

    }

    private void setupProductivityChart() {
        productivityChart = new PieChart();
        productivityChart.setChartType(PieChart.PeiChartType.DONUT_CHART);
        chartContainer1.add(productivityChart, BorderLayout.CENTER);
    }

    // ---- Data loading ----
    private void loadTasksAndUpdateChart() {
        try {
            List<TaskDto> tasks = taskController.getAllTasks();

            // Get selected month (0-based index from JComboBox)
            int selectedMonth = jComboBox1.getSelectedIndex() + 1; // Jan=1, Feb=2...

            int completed = 0;
            int overdue = 0;
            int upcoming = 0;
            int totalInMonth = 0;
            LocalDate today = LocalDate.now();

            for (TaskDto task : tasks) {
                // Only include tasks in the selected month
                if (task.getDeadline().getMonthValue() == selectedMonth) {
                    totalInMonth++;
                    if (task.isCompleted()) {
                        completed++;
                    } else if (task.getDeadline().isBefore(today)) {
                        overdue++;
                    } else {
                        upcoming++;
                    }
                }
            }
            
            // Update Summary Labels
            if (lblTotalTasksVal != null) lblTotalTasksVal.setText(String.valueOf(totalInMonth));
            if (lblCompletedVal != null) lblCompletedVal.setText(String.valueOf(completed));
            if (lblOverdueVal != null) lblOverdueVal.setText(String.valueOf(overdue));

            updateWeeklyChart(completed, overdue, upcoming);
            updateProductivityChart(completed, totalInMonth);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + e.getMessage());
        }
    }

    private void updateWeeklyChart(int completed, int overdue, int upcoming) {
        weeklyChart.clearData();

        if (completed > 0) {
            weeklyChart.addData(new ModelPieChart("Completed", completed, new Color(144, 238, 144)));
        }
        if (overdue > 0) {
            weeklyChart.addData(new ModelPieChart("Overdue", overdue, new Color(255, 99, 71)));
        }
        if (upcoming > 0) {
            weeklyChart.addData(new ModelPieChart("Upcoming", upcoming, new Color(255, 215, 0)));
        }

        if (completed == 0 && overdue == 0 && upcoming == 0) {
            weeklyChart.addData(new ModelPieChart("No Tasks", 1, Color.LIGHT_GRAY));
        }

        weeklyChart.repaint();
    }

    private void updateProductivityChart(int completed, int total) {
        productivityChart.clearData();

        if (total > 0) {
            productivityChart.addData(new ModelPieChart("Completed %", completed, new Color(100, 149, 237)));
            productivityChart.addData(new ModelPieChart("Remaining %", total - completed, new Color(211, 211, 211)));
        } else {
            productivityChart.addData(new ModelPieChart("No Data", 1, Color.LIGHT_GRAY));
        }

        productivityChart.repaint();
    }

    private void exportToPDF() {
        try {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Save Report As");
            fileChooser.setSelectedFile(new File("WeeklyReport.pdf"));

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection != javax.swing.JFileChooser.APPROVE_OPTION) {
                return; // user cancelled
            }

            File pdfFile = fileChooser.getSelectedFile();
            if (!pdfFile.getName().toLowerCase().endsWith(".pdf")) {
                pdfFile = new File(pdfFile.getAbsolutePath() + ".pdf");
            }

            // 1️⃣ Create PDF document
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // 2️⃣ Load fonts
            PDType0Font poppinsBold = PDType0Font.load(document,
                    getClass().getResourceAsStream("/com/tracker/mvc/font/Poppins-Bold.ttf"));
            PDType0Font poppinsRegular = PDType0Font.load(document,
                    getClass().getResourceAsStream("/com/tracker/mvc/font/Poppins-Regular.ttf"));

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // 3️⃣ Main title
            contentStream.beginText();
            contentStream.setFont(poppinsBold, 18);
            contentStream.newLineAtOffset(50, 780);
            contentStream.showText("Weekly Tasks Report");
            contentStream.endText();

            // 4️⃣ Month info
            contentStream.beginText();
            contentStream.setFont(poppinsRegular, 14);
            contentStream.newLineAtOffset(50, 760);
            contentStream.showText("Month: " + jComboBox1.getSelectedItem());
            contentStream.endText();

            float scale = 0.6f;
            float chartY = 650;

            // 5️⃣ Chart titles
            contentStream.beginText();
            contentStream.setFont(poppinsBold, 14);
            contentStream.newLineAtOffset(50, chartY + 10);
            contentStream.showText("Weekly Progress");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(poppinsBold, 14);
            contentStream.newLineAtOffset(320, chartY + 10);
            contentStream.showText("Productivity Streaks");
            contentStream.endText();

            // 6️⃣ Capture weekly chart as image
            BufferedImage chart1Image = new BufferedImage(chartContainer.getWidth(), chartContainer.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d1 = chart1Image.createGraphics();
            chartContainer.printAll(g2d1);
            g2d1.dispose();
            PDImageXObject pdfImg1 = LosslessFactory.createFromImage(document, chart1Image);
            contentStream.drawImage(pdfImg1, 50, chartY - chart1Image.getHeight() * scale,
                    chart1Image.getWidth() * scale, chart1Image.getHeight() * scale);

            // ---- Weekly chart legend ----
            String[] weeklyLabels = {"Completed ", "Overdue", "Upcoming"};
            Color[] weeklyColors = {new Color(144, 238, 144), new Color(255, 99, 71), new Color(255, 215, 0)};
            float legendX = 50;
            float legendY = chartY - chart1Image.getHeight() * scale - 20;
            float squareSize = 10;
            float spacing = 70;

            for (int i = 0; i < weeklyLabels.length; i++) {
                contentStream.setNonStrokingColor(weeklyColors[i]);
                contentStream.addRect(legendX, legendY, squareSize, squareSize);
                contentStream.fill();

                contentStream.beginText();
                contentStream.setFont(poppinsRegular, 10);
                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.newLineAtOffset(legendX + squareSize + 3, legendY);
                contentStream.showText(weeklyLabels[i]);
                contentStream.endText();

                legendX += spacing;
            }

            // 7️⃣ Capture productivity chart as image
            BufferedImage chart2Image = new BufferedImage(chartContainer1.getWidth(), chartContainer1.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d2 = chart2Image.createGraphics();
            chartContainer1.printAll(g2d2);
            g2d2.dispose();
            PDImageXObject pdfImg2 = LosslessFactory.createFromImage(document, chart2Image);
            contentStream.drawImage(pdfImg2, 320, chartY - chart2Image.getHeight() * scale,
                    chart2Image.getWidth() * scale, chart2Image.getHeight() * scale);

            // ---- Productivity chart legend ----
            String[] prodLabels = {"Completed %", "Remaining %"};
            Color[] prodColors = {new Color(100, 149, 237), new Color(211, 211, 211)};
            legendX = 320;
            legendY = chartY - chart2Image.getHeight() * scale - 20;

            for (int i = 0; i < prodLabels.length; i++) {
                contentStream.setNonStrokingColor(prodColors[i]);
                contentStream.addRect(legendX, legendY, squareSize, squareSize);
                contentStream.fill();

                contentStream.beginText();
                contentStream.setFont(poppinsRegular, 10);
                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.newLineAtOffset(legendX + squareSize + 3, legendY);
                contentStream.showText(prodLabels[i]);
                contentStream.endText();

                legendX += spacing;
            }

            // 8️⃣ Draw task cards
            List<TaskDto> tasks = taskController.getAllTasks();
            int selectedMonth = jComboBox1.getSelectedIndex() + 1;

            float x = 50;
            float y = chartY - chart1Image.getHeight() * scale - 50;
            float cardWidth = 500;
            float cardHeight = 60;
            float padding = 5;

            for (TaskDto task : tasks) {
                if (task.getDeadline().getMonthValue() == selectedMonth) {

                    contentStream.setNonStrokingColor(new Color(240, 240, 240));
                    contentStream.addRect(x, y - cardHeight, cardWidth, cardHeight);
                    contentStream.fill();
                    contentStream.setNonStrokingColor(Color.BLACK);

                    contentStream.beginText();
                    contentStream.setFont(poppinsBold, 12);
                    contentStream.newLineAtOffset(x + padding, y - 15);
                    contentStream.showText(task.getSubject());
                    contentStream.endText();

                    String status = task.isCompleted() ? "Completed"
                            : task.getDeadline().isBefore(LocalDate.now()) ? "Overdue" : "Upcoming";
                    contentStream.beginText();
                    contentStream.setFont(poppinsRegular, 10);
                    contentStream.newLineAtOffset(x + padding, y - 30);
                    contentStream.showText("Status: " + status + " | Due: " + task.getDeadline());
                    contentStream.endText();

                    String description = task.getDescription() != null ? task.getDescription() : "";
                    contentStream.beginText();
                    contentStream.setFont(poppinsRegular, 10);
                    contentStream.newLineAtOffset(x + padding, y - 45);

                    int maxChars = 80;
                    int start = 0;
                    while (start < description.length()) {
                        int end = Math.min(start + maxChars, description.length());
                        contentStream.showText(description.substring(start, end));
                        contentStream.newLineAtOffset(0, -12);
                        start = end;
                    }
                    contentStream.endText();

                    y -= (cardHeight + 10);

                    if (y < 50) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        y = 750;
                    }
                }
            }

            contentStream.close();
            document.save(pdfFile);
            document.close();

            JOptionPane.showMessageDialog(this, "PDF exported successfully: " + pdfFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting PDF: " + e.getMessage());
        }
    }
}
