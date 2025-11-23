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
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
    }

    private void setupKanbanColumns() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Kanban Board");
        titleLabel.setFont(new Font("Poppins", Font.BOLD, 20));
        titleLabel.setForeground(new Color(50, 50, 50));

        JButton addTaskBtn = new JButton("+ Add Task");
        addTaskBtn.setFont(new Font("Poppins", Font.PLAIN, 11));
        addTaskBtn.addActionListener(e -> showAddTaskDialog());

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createHorizontalStrut(300));
        headerPanel.add(addTaskBtn);

        add(headerPanel, BorderLayout.NORTH);

        // Create main panel with 3 columns
        JPanel columnsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        columnsPanel.setBackground(new Color(245, 245, 245));
        columnsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

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
        JPanel column = new JPanel(new BorderLayout());
        column.setBackground(Color.WHITE);
        column.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        JLabel headerLabel = new JLabel(title, SwingConstants.CENTER);
        headerLabel.setFont(new Font("Poppins", Font.BOLD, 13));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBackground(headerColor);
        headerLabel.setOpaque(true);
        headerLabel.setBorder(new EmptyBorder(8, 8, 8, 8));

        column.add(headerLabel, BorderLayout.NORTH);
        return column;
    }

    private JScrollPane createScrollPane(JPanel panel) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "trackArc:999;trackInsets:3,3,3,3;thumbInsets:3,3,3,3;background:$Table.background;width:8;");

        // Set preferred size for scroll pane to ensure proper scrolling
        scrollPane.setPreferredSize(new Dimension(250, 400));

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

                JPanel taskCard = createTaskCard(task);

                String status = task.getStatus() != null ? task.getStatus() : "TODO";
                switch (status) {
                    case "TODO" ->
                        todoPanel.add(taskCard);
                    case "IN_PROGRESS" ->
                        inProgressPanel.add(taskCard);
                    case "DONE" ->
                        donePanel.add(taskCard);
                    default ->
                        todoPanel.add(taskCard);
                }
            }

            // Add empty labels if no tasks
            if (todoPanel.getComponentCount() == 0) {
                todoPanel.add(createEmptyLabel());
            }
            if (inProgressPanel.getComponentCount() == 0) {
                inProgressPanel.add(createEmptyLabel());
            }
            if (donePanel.getComponentCount() == 0) {
                donePanel.add(createEmptyLabel());
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

    private JPanel createTaskCard(TaskDto task) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(8, 8, 8, 8)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setPreferredSize(new Dimension(200, 100));
        card.setMinimumSize(new Dimension(180, 100));

        // Subject
        JLabel subjectLabel = new JLabel("<html><b>" + truncateText(task.getSubject(), 30) + "</b></html>");
        subjectLabel.setFont(new Font("Poppins", Font.PLAIN, 12));
        card.add(subjectLabel, BorderLayout.NORTH);

        // Description
        JLabel descLabel = new JLabel("<html>" + truncateText(task.getDescription(), 40) + "</html>");
        descLabel.setFont(new Font("Poppins", Font.PLAIN, 10));
        descLabel.setForeground(new Color(100, 100, 100));
        card.add(descLabel, BorderLayout.CENTER);

        // Deadline
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        bottomPanel.setBackground(Color.WHITE);
        JLabel deadlineLabel = new JLabel("📅 " + task.getDeadline().format(DateTimeFormatter.ofPattern("MMM dd")));
        deadlineLabel.setFont(new Font("Poppins", Font.PLAIN, 9));
        deadlineLabel.setForeground(task.isOverdue() ? Color.RED : new Color(100, 100, 100));
        bottomPanel.add(deadlineLabel);

        // Delete button
        JButton deleteBtn;
        try {
            // Try to load bin icon from resources (add your own icon as needed)
            java.net.URL iconUrl = getClass().getResource("/com/tracker/mvc/icon/delete.png");
            if (iconUrl != null) {
                ImageIcon binIcon = new ImageIcon(iconUrl);
                deleteBtn = new JButton(binIcon);
            } else {
                deleteBtn = new JButton("×");
                deleteBtn.setFont(new Font("Arial", Font.BOLD, 14));
                deleteBtn.setForeground(Color.RED);
            }
        } catch (Exception ex) {
            deleteBtn = new JButton("×");
            deleteBtn.setFont(new Font("Arial", Font.BOLD, 14));
            deleteBtn.setForeground(Color.RED);
        }
        deleteBtn.setBorderPainted(false);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> deleteTask(task.getTaskId()));
        bottomPanel.add(deleteBtn);

        card.add(bottomPanel, BorderLayout.SOUTH);

        // Make card draggable
        makeDraggable(card, task.getTaskId());

        return card;
    }

    private String truncateText(String text, int maxLength) {
        if (text == null)
            return "";
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength) + "...";
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
