/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.tracker.mvc.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.tracker.mvc.controller.TaskController;
import com.tracker.mvc.form.Dashboard;
import com.tracker.mvc.form.Calender;
import com.tracker.mvc.form.KanbanBoard;
import com.tracker.mvc.form.Timetable;
import com.tracker.mvc.form.Report;
import com.tracker.mvc.form.TaskView;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 *
 * @author Theekshana
 */
public class DashboardView extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(DashboardView.class.getName());

    private final Dashboard dashboardForm = new Dashboard();
    private final TaskView form1 = new TaskView();
    private final Timetable form2 = new Timetable();
    private final Report form3 = new Report();
    private final Calender form4 = new Calender();
    private final KanbanBoard kanbanBoard = new KanbanBoard();

    private JFXPanel toastFxPanel;
    private final int TOAST_PANEL_HEIGHT = 140;
    private final int TOAST_PANEL_PADDING_TOP = 24;

    /**
     * Creates new form DashboardView
     */
    public DashboardView() {
        try {
            new TaskController();
        } catch (Exception ex) {
            System.getLogger(DashboardView.class.getName())
                    .log(System.Logger.Level.ERROR, (String) null, ex);
        }

        initComponents();

        // Initialize and add JavaFX toast overlay panel at full width along top
        toastFxPanel = new JFXPanel();
        toastFxPanel.setOpaque(false);
        toastFxPanel.setVisible(false);
        getLayeredPane().add(toastFxPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        // Set initial bounds to full width
        resizeToastPanel();

        // Re-center the toast overlay on JFrame resize
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                resizeToastPanel();
            }
        });

        Platform.runLater(() -> {
            AnchorPane toastRoot = new AnchorPane();
            toastRoot.setStyle("-fx-background-color: transparent;");
            Scene toastScene = new Scene(toastRoot, toastFxPanel.getWidth(), TOAST_PANEL_HEIGHT);
            toastScene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            toastFxPanel.setScene(toastScene);

            // Add listener (using lambda with final variable trick)
            javafx.beans.value.ChangeListener<Number>[] listener = new javafx.beans.value.ChangeListener[1];
            listener[0] = (obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0) {
                    toastRoot.widthProperty().removeListener(listener[0]);
                }
            };
            toastRoot.widthProperty().addListener(listener[0]);
        });

        // showMotivationalToastCentered();

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        menu.initMoving(this);
        this.setLocationRelativeTo(null);

        menu.addEventMenuSelected((int index) -> {
            try {
                switch (index) {
                    case 0 ->
                        setForm(dashboardForm);
                    case 1 ->
                        setForm(form1);
                    case 2 ->
                        setForm(form4);
                    case 3 ->
                        setForm(form2);
                    case 4 ->
                        setForm(form3);
                    case 5 ->
                        setForm(kanbanBoard);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error switching forms: " + e.getMessage());
            }
        });

        // Set up task change listeners - when TaskView or Kanban changes, refresh both
        form1.setTaskChangeListener(() -> {
            try {
                dashboardForm.refreshDashboard();
                kanbanBoard.refreshKanban();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error refreshing dashboard: " + e.getMessage());
            }
        });

        kanbanBoard.setTaskChangeListener(() -> {
            try {
                dashboardForm.refreshDashboard();
                form1.refreshTaskTable(); // Update Task Table
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error refreshing from kanban: " + e.getMessage());
            }
        });

        setForm(dashboardForm);
    }

    public TaskView getForm1() {
        return form1;
    }

    // Resize toast overlay panel to full width with fixed height at top offset
    private void resizeToastPanel() {
        int frameWidth = getWidth();
        int frameHeight = getHeight();
        toastFxPanel.setBounds(0, 0, frameWidth, frameHeight);
    }

    // Show motivational toast popup centered horizontally inside overlay panel
    private void showMotivationalToastCentered() {
        // Removed motivational toast popup logic
    }

    private void setForm(JComponent com) {
        mainPanel.removeAll();
        mainPanel.add(com);
        mainPanel.repaint();
        mainPanel.revalidate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelBorder1 = new com.tracker.mvc.swing.PanelBorder();
        menu = new com.tracker.mvc.component.Menu();
        mainPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        panelBorder1.setAlignmentX(0.0F);
        panelBorder1.setAlignmentY(0.0F);

        menu.setAlignmentX(0.0F);
        menu.setAlignmentY(0.0F);

        mainPanel.setAlignmentX(0.0F);
        mainPanel.setAlignmentY(0.0F);
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout panelBorder1Layout = new javax.swing.GroupLayout(panelBorder1);
        panelBorder1.setLayout(panelBorder1Layout);
        panelBorder1Layout.setHorizontalGroup(
            panelBorder1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBorder1Layout.createSequentialGroup()
                .addComponent(menu, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 677, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelBorder1Layout.setVerticalGroup(
            panelBorder1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBorder1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(menu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelBorder1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelBorder1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new DashboardView().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel mainPanel;
    private com.tracker.mvc.component.Menu menu;
    private com.tracker.mvc.swing.PanelBorder panelBorder1;
    // End of variables declaration//GEN-END:variables

}
