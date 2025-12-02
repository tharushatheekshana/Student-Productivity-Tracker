/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.tracker.mvc.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.tracker.mvc.controller.TimetableController;
import com.tracker.mvc.dto.TimetableDto;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import raven.datetime.TimePicker;

/**
 *
 * @author Theekshana
 */
public class Timetable extends javax.swing.JPanel {

    private TimetableController timetableController;

    // Components
    private TimePicker timePicker1;
    private TimePicker timePicker2;
    private JFormattedTextField txtTime;
    private JFormattedTextField txtTime1;
    private JTextField txtAddSubject;
    private JComboBox<String> jComboBox1;
    private JTable tblTimetable;
    private JTable tblGenTimetable;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnRemove;
    private JButton btnGenTimetable;

    /**
     * Creates new form Form1
     */
    public Timetable() {
        try {
            this.timetableController = new TimetableController();
        } catch (Exception ex) {
            System.getLogger(Timetable.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        initComponents();
        
        // Initialize Time Pickers
        timePicker1 = new TimePicker();
        timePicker2 = new TimePicker();
        timePicker1.setEditor(txtTime);
        timePicker2.setEditor(txtTime1);

        loadTimetable();
        loadGenTimetable();

        tblTimetable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tblTimetableRowSelected();
            }
        });
    }

    private void initComponents() {
        setOpaque(false);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Header ---
        JLabel titleLabel = new JLabel("Study Schedule");
        titleLabel.setFont(new Font("Poppins", Font.BOLD, 24));
        titleLabel.setForeground(new Color(220, 220, 220));
        add(titleLabel, BorderLayout.NORTH);

        // --- Main Content (2 Columns) ---
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setOpaque(false);

        // Left Card: Configuration
        contentPanel.add(createConfigPanel());

        // Right Card: Generated Schedule
        contentPanel.add(createSchedulePanel());

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createConfigPanel() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:#ffffff; border:1,1,1,1,#e5e5e5");
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel subtitle = new JLabel("Configuration");
        subtitle.setFont(new Font("Poppins", Font.BOLD, 16));
        subtitle.setForeground(Color.DARK_GRAY);
        card.add(subtitle, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Row 1: Times
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("Start Time"), gbc);
        gbc.gridx = 1;
        formPanel.add(createLabel("End Time"), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        txtTime = new JFormattedTextField();
        formPanel.add(txtTime, gbc);
        
        gbc.gridx = 1;
        txtTime1 = new JFormattedTextField();
        formPanel.add(txtTime1, gbc);

        // Row 2: Subject & Priority
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createLabel("Subject"), gbc);
        gbc.gridx = 1;
        formPanel.add(createLabel("Priority"), gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        txtAddSubject = new JTextField();
        formPanel.add(txtAddSubject, gbc);

        gbc.gridx = 1;
        jComboBox1 = new JComboBox<>(new String[] { "High", "Medium", "Low" });
        formPanel.add(jComboBox1, gbc);

        // Row 3: Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        btnPanel.setOpaque(false);
        
        btnAdd = createButton("Add", new Color(0, 120, 215));
        btnAdd.addActionListener(this::btnAddActionPerformed);
        
        btnUpdate = createButton("Update", new Color(100, 149, 237));
        btnUpdate.addActionListener(this::btnAdd1ActionPerformed); // kept original handler name
        
        btnRemove = createButton("Remove", new Color(255, 99, 71));
        btnRemove.addActionListener(this::btnRemoveActionPerformed);

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnRemove);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        formPanel.add(btnPanel, gbc);

        // Row 4: Subject List Table
        gbc.gridy = 5; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        tblTimetable = new JTable();
        tblTimetable.setFont(new Font("Poppins", Font.PLAIN, 12));
        tblTimetable.setRowHeight(25);
        tblTimetable.getTableHeader().setFont(new Font("Poppins", Font.BOLD, 12));
        tblTimetable.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"Subject", "Priority"}));
        tblTimetable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblTimetableMouseClicked(evt);
            }
        });
        JScrollPane scroll = new JScrollPane(tblTimetable);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(0, 300));
        formPanel.add(scroll, gbc);

        // Row 5: Generate Button
        gbc.gridy = 6; gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        btnGenTimetable = createButton("Generate Timetable", new Color(40, 167, 69));
        btnGenTimetable.setFont(new Font("Poppins", Font.BOLD, 14));
        btnGenTimetable.addActionListener(this::btnGenTimetableActionPerformed);
        formPanel.add(btnGenTimetable, gbc);

        card.add(formPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSchedulePanel() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:#ffffff; border:1,1,1,1,#e5e5e5");
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel subtitle = new JLabel("Your Schedule");
        subtitle.setFont(new Font("Poppins", Font.BOLD, 16));
        subtitle.setForeground(Color.DARK_GRAY);
        card.add(subtitle, BorderLayout.NORTH);

        tblGenTimetable = new JTable();
        tblGenTimetable.setFont(new Font("Poppins", Font.PLAIN, 12));
        tblGenTimetable.setRowHeight(25);
        tblGenTimetable.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"Time Slot", "Subject"}));
        
        JScrollPane scroll = new JScrollPane(tblGenTimetable);
        scroll.getViewport().setBackground(Color.WHITE);
        
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Poppins", Font.PLAIN, 12));
        lbl.setForeground(Color.GRAY);
        return lbl;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Poppins", Font.PLAIN, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        return btn;
    }

    // --- Actions (Delegating to existing methods) ---

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {
        addSubject();
    }

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {
        removeSelectedSubject();
    }

    private void btnGenTimetableActionPerformed(java.awt.event.ActionEvent evt) {
        genTimetable();
    }

    private void btnAdd1ActionPerformed(java.awt.event.ActionEvent evt) {
        updateSubjectAndPriority(); // Originally this was the update method
    }

    private void tblTimetableMouseClicked(java.awt.event.MouseEvent evt) {
        // Existing empty handler
    }

    // --- Logic Methods (Unchanged mostly, just context) ---

    private void loadTimetable() {
        String[] columns = {"Subject", "Priority"};
        DefaultTableModel dtm = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try {
            ArrayList<TimetableDto> subjects = timetableController.getAllSubjects();
            for (TimetableDto dto : subjects) {
                dtm.addRow(new Object[]{dto.getName(), dto.getPriority()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading timetable: " + ex.getMessage());
            ex.printStackTrace();
        }

        tblTimetable.setModel(dtm);
    }

    private void addSubject() {
        TimetableDto dto = new TimetableDto();

        String subjectName = txtAddSubject.getText().trim();
        if (subjectName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a subject name.");
            return;
        }

        String priority = (String) jComboBox1.getSelectedItem();
        dto.setName(subjectName);
        dto.setPriority(priority);

        try {
            String result = timetableController.saveSubject(dto);
            JOptionPane.showMessageDialog(this, result);
            loadTimetable(); // refresh table
            txtAddSubject.setText(""); // clear input
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving subject: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void removeSelectedSubject() {
        int selectedRow = tblTimetable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subject to remove.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String subjectName = (String) tblTimetable.getValueAt(selectedRow, 0);

        try {
            // Find the subject DTO by name
            ArrayList<TimetableDto> subjects = timetableController.getAllSubjects();
            TimetableDto dtoToDelete = null;
            for (TimetableDto dto : subjects) {
                if (dto.getName().equals(subjectName)) {
                    dtoToDelete = dto;
                    break;
                }
            }

            if (dtoToDelete != null) {
                String result = timetableController.deleteSubject(dtoToDelete.getId());
                JOptionPane.showMessageDialog(this, result);
                loadTimetable(); // refresh table
            } else {
                JOptionPane.showMessageDialog(this, "Subject not found.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error removing subject: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadGenTimetable() {
        String[] columns = {"Time Slot", "Subject"};
        DefaultTableModel dtm = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblGenTimetable.setModel(dtm);
    }

    private void genTimetable() {
        DefaultTableModel modelSubjects = (DefaultTableModel) tblTimetable.getModel();
        DefaultTableModel modelGen = (DefaultTableModel) tblGenTimetable.getModel();
        modelGen.setRowCount(0); // Clear previous timetable

        if (modelSubjects.getRowCount() == 0) {
            return;
        }

        // ✅ Get start and end time from time pickers
        LocalTime startTime = timePicker1.getSelectedTime();
        LocalTime endTime = timePicker2.getSelectedTime();

        if (startTime == null || endTime == null) {
            JOptionPane.showMessageDialog(this, "Please select both start and end times.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            JOptionPane.showMessageDialog(this, "End time must be after start time.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Collect subjects by priority
        ArrayList<String> high = new ArrayList<>();
        ArrayList<String> medium = new ArrayList<>();
        ArrayList<String> low = new ArrayList<>();

        for (int i = 0; i < modelSubjects.getRowCount(); i++) {
            String subject = (String) modelSubjects.getValueAt(i, 0);
            String priority = (String) modelSubjects.getValueAt(i, 1);
            switch (priority) {
                case "High" ->
                    high.add(subject);
                case "Medium" ->
                    medium.add(subject);
                case "Low" ->
                    low.add(subject);
            }
        }

        ArrayList<String> orderedSubjects = new ArrayList<>();
        orderedSubjects.addAll(high);
        orderedSubjects.addAll(medium);
        orderedSubjects.addAll(low);

        // Calculate time slot duration
        long totalMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        int slotCount = orderedSubjects.size();
        long slotMinutes = totalMinutes / slotCount;

        LocalTime slotStart = startTime;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        for (String subject : orderedSubjects) {
            LocalTime slotEnd = slotStart.plusMinutes(slotMinutes);
            modelGen.addRow(new Object[]{slotStart.format(fmt) + " - " + slotEnd.format(fmt), subject});
            slotStart = slotEnd;
        }
    }

    private void updateSubjectAndPriority() {
        int selectedRow = tblTimetable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subject to update.");
            return;
        }

        String subjectName = txtAddSubject.getText().trim();
        if (subjectName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a subject name.");
            return;
        }

        String priority = (String) jComboBox1.getSelectedItem();

        try {
            // Find the subject DTO by selected row
            ArrayList<TimetableDto> subjects = timetableController.getAllSubjects();
            TimetableDto dtoToUpdate = null;
            String oldName = (String) tblTimetable.getValueAt(selectedRow, 0);
            for (TimetableDto dto : subjects) {
                if (dto.getName().equals(oldName)) {
                    dtoToUpdate = dto;
                    break;
                }
            }

            if (dtoToUpdate != null) {
                dtoToUpdate.setName(subjectName);
                dtoToUpdate.setPriority(priority);

                String result = timetableController.updateSubject(dtoToUpdate);
                JOptionPane.showMessageDialog(this, result);
                loadTimetable(); // refresh table
                txtAddSubject.setText(""); // clear input
            } else {
                JOptionPane.showMessageDialog(this, "Subject not found.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating subject: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void tblTimetableRowSelected() {
        int selectedRow = tblTimetable.getSelectedRow();
        if (selectedRow != -1) {
            String subject = (String) tblTimetable.getValueAt(selectedRow, 0);
            String priority = (String) tblTimetable.getValueAt(selectedRow, 1);

            txtAddSubject.setText(subject);
            jComboBox1.setSelectedItem(priority);
        }
    }

}