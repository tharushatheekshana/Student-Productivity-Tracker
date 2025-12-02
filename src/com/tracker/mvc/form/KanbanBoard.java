/*
 * Kanban Board for Task Management with Drag and Drop
 */
package com.tracker.mvc.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.tracker.mvc.controller.TaskController;
import com.tracker.mvc.dto.TaskDto;
import com.tracker.mvc.event.TaskChangeListener;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import raven.popup.DefaultOption;
import raven.popup.GlassPanePopup;
import raven.popup.component.SimplePopupBorder;

/**
 * @author Theekshana
 */
public class KanbanBoard extends javax.swing.JPanel {

    private TaskController taskController;
    private JPanel todoPanel;
    private JPanel inProgressPanel;
    private JPanel donePanel;
    private TaskChangeListener taskChangeListener;

    public KanbanBoard() {
        try {
            this.taskController = new TaskController();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        initComponents();
        setupKanbanColumns();
        loadTasks();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setOpaque(false); // Make main panel transparent
    }

    private void setupKanbanColumns() {
        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Kanban Board");
        titleLabel.setFont(new Font("Poppins", Font.BOLD, 24));
        titleLabel.setForeground(new Color(220, 220, 220)); // Light gray for dark theme

        JButton addTaskBtn = new JButton("+ Add Task");
        addTaskBtn.setFont(new Font("Poppins", Font.BOLD, 12));
        addTaskBtn.setBackground(new Color(0, 120, 215));
        addTaskBtn.setForeground(Color.WHITE);
        addTaskBtn.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0; focusWidth:0; innerFocusWidth:0");
        addTaskBtn.addActionListener(e -> showAddTaskDialog());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(addTaskBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- Columns Container ---
        JPanel columnsPanel = new JPanel(new GridLayout(1, 3, 20, 0)); // Increased gap
        columnsPanel.setOpaque(false);

        // To-Do Column
        JPanel todoColumn = createColumn("To-Do", new Color(255, 193, 7));
        todoPanel = new JPanel();
        setupDropTarget(todoPanel, "TODO");
        JScrollPane todoScroll = createScrollPane(todoPanel);
        todoColumn.add(todoScroll, BorderLayout.CENTER);
        columnsPanel.add(todoColumn);

        // In Progress Column
        JPanel inProgressColumn = createColumn("In Progress", new Color(33, 150, 243));
        inProgressPanel = new JPanel();
        setupDropTarget(inProgressPanel, "IN_PROGRESS");
        JScrollPane inProgressScroll = createScrollPane(inProgressPanel);
        inProgressColumn.add(inProgressScroll, BorderLayout.CENTER);
        columnsPanel.add(inProgressColumn);

        // Done Column
        JPanel doneColumn = createColumn("Done", new Color(76, 175, 80));
        donePanel = new JPanel();
        setupDropTarget(donePanel, "DONE");
        JScrollPane doneScroll = createScrollPane(donePanel);
        doneColumn.add(doneScroll, BorderLayout.CENTER);
        columnsPanel.add(doneColumn);

        add(columnsPanel, BorderLayout.CENTER);
    }

    private JPanel createColumn(String title, Color headerColor) {
        JPanel column = new JPanel(new BorderLayout(0, 10));
        // Rounded white column background
        column.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:#ffffff; border:1,1,1,1,#e5e5e5");
        column.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header label with color indicator
        JLabel headerLabel = new JLabel(title, SwingConstants.CENTER);
        headerLabel.setFont(new Font("Poppins", Font.BOLD, 16));
        headerLabel.setForeground(Color.DARK_GRAY);
        
        column.add(headerLabel, BorderLayout.NORTH);
        return column;
    }

    private JScrollPane createScrollPane(JPanel panel) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false); // Transparent to show column white background

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "trackArc:999;trackInsets:3,3,3,3;thumbInsets:3,3,3,3;background:null;width:6;");

        return scrollPane;
    }

    private void setupDropTarget(JPanel panel, String status) {
        new DropTarget(panel, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    Transferable transferable = dtde.getTransferable();
                    String taskId = (String) transferable.getTransferData(DataFlavor.stringFlavor);

                    updateTaskStatus(taskId, status);
                    dtde.dropComplete(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    dtde.dropComplete(false);
                }
            }
        });
    }

    private void updateTaskStatus(String taskId, String newStatus) {
        try {
            TaskDto task = taskController.searchTask(taskId);
            if (task != null) {
                task.setStatus(newStatus);
                if (newStatus.equals("DONE")) {
                    task.setCompleted(true);
                } else {
                    task.setCompleted(false);
                }
                taskController.updateTask(task);
                loadTasks();
                notifyTaskChanged();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating task: " + ex.getMessage());
        }
    }

    private void loadTasks() {
        todoPanel.removeAll();
        inProgressPanel.removeAll();
        donePanel.removeAll();

        try {
            ArrayList<TaskDto> tasks = taskController.getAllTasks();

            for (TaskDto task : tasks) {
                // Ensure completed tasks appear under DONE even if status was not set
                if (task.isCompleted() && (task.getStatus() == null || !"DONE".equals(task.getStatus()))) {
                    task.setStatus("DONE");
                    try {
                        taskController.updateTask(task); // persist status correction
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                // Determine current status
                String status = task.getStatus() != null ? task.getStatus() : "TODO";
                
                // Color strip based on status
                Color stripColor;
                switch (status) {
                    case "TODO" -> stripColor = new Color(255, 193, 7);
                    case "IN_PROGRESS" -> stripColor = new Color(33, 150, 243);
                    case "DONE" -> stripColor = new Color(76, 175, 80);
                    default -> stripColor = Color.GRAY;
                }

                JPanel taskCard = createTaskCard(task, stripColor);

                switch (status) {
                    case "TODO" -> todoPanel.add(taskCard);
                    case "IN_PROGRESS" -> inProgressPanel.add(taskCard);
                    case "DONE" -> donePanel.add(taskCard);
                    default -> todoPanel.add(taskCard);
                }
                
                // Add spacing
                if (status.equals("TODO")) todoPanel.add(Box.createVerticalStrut(10));
                if (status.equals("IN_PROGRESS")) inProgressPanel.add(Box.createVerticalStrut(10));
                if (status.equals("DONE")) donePanel.add(Box.createVerticalStrut(10));
            }
            
            todoPanel.revalidate();
            todoPanel.repaint();
            inProgressPanel.revalidate();
            inProgressPanel.repaint();
            donePanel.revalidate();
            donePanel.repaint();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private JPanel createTaskCard(TaskDto task, Color stripColor) {
        // Main card panel
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow (subtle)
                g2.setColor(new Color(220, 220, 220));
                g2.fillRoundRect(2, 2, getWidth()-2, getHeight()-2, 15, 15);
                
                // Background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-2, getHeight()-2, 15, 15);
                
                // Left Color Strip
                g2.setColor(stripColor);
                g2.fillRoundRect(0, 0, 6, getHeight()-2, 15, 15);
                g2.fillRect(4, 0, 2, getHeight()-2); // Square off inner side
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 15, 10, 10)); // Left padding for strip
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setPreferredSize(new Dimension(200, 90));
        card.setMinimumSize(new Dimension(180, 90));

        // Center: Subject & Description
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        centerPanel.setOpaque(false);
        
        JLabel subjectLabel = new JLabel(task.getSubject());
        subjectLabel.setFont(new Font("Poppins", Font.BOLD, 13));
        subjectLabel.setForeground(new Color(50, 50, 50));
        
        String shortDesc = task.getDescription();
        if(shortDesc != null && shortDesc.length() > 30) shortDesc = shortDesc.substring(0, 27) + "...";
        JLabel descLabel = new JLabel(shortDesc);
        descLabel.setFont(new Font("Poppins", Font.PLAIN, 11));
        descLabel.setForeground(Color.GRAY);
        
        centerPanel.add(subjectLabel);
        centerPanel.add(descLabel);
        card.add(centerPanel, BorderLayout.CENTER);

        // Bottom/Right: Date & Delete
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        
        // Delete Button (Top Right)
        JButton deleteBtn = new JButton();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/com/tracker/mvc/icon/delete.png"));
            deleteBtn.setIcon(icon);
        } catch (Exception e) {
            deleteBtn.setText("×");
            deleteBtn.setForeground(Color.RED);
        }
        deleteBtn.setBorder(null);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> deleteTask(task.getTaskId()));
        
        // Date Label (Bottom Right)
        JLabel dateLabel = new JLabel(task.getDeadline().format(DateTimeFormatter.ofPattern("MMM dd")));
        dateLabel.setFont(new Font("Poppins", Font.PLAIN, 10));
        dateLabel.setForeground(Color.LIGHT_GRAY);
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JPanel btnContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnContainer.setOpaque(false);
        btnContainer.add(deleteBtn);
        
        rightPanel.add(btnContainer, BorderLayout.NORTH);
        rightPanel.add(dateLabel, BorderLayout.SOUTH);
        
        card.add(rightPanel, BorderLayout.EAST);

        // Make card draggable
        makeDraggable(card, task.getTaskId());

        return card;
    }

    private void makeDraggable(JPanel card, String taskId) {
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        DragSource dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(card, DnDConstants.ACTION_MOVE,
                new DragGestureListener() {
                    @Override
                    public void dragGestureRecognized(DragGestureEvent dge) {
                        StringSelection transferable = new StringSelection(taskId);
                        dge.startDrag(DragSource.DefaultMoveDrop, transferable);
                    }
                });
    }

    private JLabel createEmptyLabel() {
        JLabel label = new JLabel("No tasks", SwingConstants.CENTER);
        label.setFont(new Font("Poppins", Font.ITALIC, 12));
        label.setForeground(new Color(150, 150, 150));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void showAddTaskDialog() {
        Create createPanel = new Create();

        DefaultOption option = new DefaultOption() {
            @Override
            public boolean closeWhenClickOutside() {
                return true;
            }
        };

        String[] actions = { "Cancel", "Save" };

        GlassPanePopup.showPopup(
                new SimplePopupBorder(createPanel, "Create Task", actions, (popup, i) -> {
                    if (i == 1) { // Save
                        try {
                            String subject = createPanel.getSubject();
                            String description = createPanel.getDescription();
                            String deadlineText = createPanel.getDeadline();

                            if (subject.isEmpty() || description.isEmpty() || deadlineText.isEmpty()) {
                                JOptionPane.showMessageDialog(createPanel,
                                        "Please fill all fields", "Validation Error", JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            LocalDate deadline = LocalDate.parse(deadlineText, formatter);

                            TaskDto task = new TaskDto();
                            task.setSubject(subject);
                            task.setDescription(description);
                            task.setDeadline(deadline);
                            task.setCompleted(false);
                            task.setStatus("TODO");

                            if (taskController == null) {
                                taskController = new TaskController();
                            }
                            String result = taskController.saveTask(task);
                            JOptionPane.showMessageDialog(createPanel, result);

                            loadTasks();
                            notifyTaskChanged();
                            popup.closePopup();

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(createPanel,
                                    "Error saving task: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        popup.closePopup();
                    }
                }),
                option);
    }

    private void deleteTask(String taskId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this task?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                taskController.deleteTask(taskId);
                JOptionPane.showMessageDialog(this, "Task deleted successfully");
                loadTasks();
                notifyTaskChanged();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting task: " + ex.getMessage());
            }
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame frame) {
            GlassPanePopup.install(frame);
        }
    }

    public void setTaskChangeListener(TaskChangeListener listener) {
        this.taskChangeListener = listener;
    }

    private void notifyTaskChanged() {
        if (taskChangeListener != null) {
            taskChangeListener.onTaskChanged();
        }
    }

    public void refreshKanban() {
        loadTasks();
    }
}