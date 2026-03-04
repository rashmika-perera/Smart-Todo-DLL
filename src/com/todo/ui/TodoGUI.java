package com.todo.ui;

import com.todo.model.Task;
import com.todo.model.Priority;
import com.todo.model.Status;
import com.todo.service.TodoService;
import com.todo.ds.DoublyLinkedList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class TodoGUI extends JFrame {
    private TodoService service;
    private JList<TaskUIItem> taskList;
    private DefaultListModel<TaskUIItem> listModel;
    private JButton addTaskBtn, deleteTaskBtn, undoDeleteBtn, addSubtaskBtn, updateStatusBtn, autoPromoteBtn, workOnTaskBtn, updateMomentumBtn, showInsightsBtn;
    private JTextArea messageArea;

    public TodoGUI() {
        service = new TodoService();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Smart To-Do (DLL)");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Task list
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setCellRenderer(new TaskCellRenderer());
        JScrollPane scrollPane = new JScrollPane(taskList);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 5));

        addTaskBtn = new JButton("Add Task");
        deleteTaskBtn = new JButton("Delete Task");
        undoDeleteBtn = new JButton("Undo Delete");
        addSubtaskBtn = new JButton("Add Subtask");
        updateStatusBtn = new JButton("Update Status");
        autoPromoteBtn = new JButton("Auto Promote");
        workOnTaskBtn = new JButton("Boost Task");
        updateMomentumBtn = new JButton("Update Momentum");
        showInsightsBtn = new JButton("Show Insights");

        buttonPanel.add(addTaskBtn);
        buttonPanel.add(deleteTaskBtn);
        buttonPanel.add(undoDeleteBtn);
        buttonPanel.add(addSubtaskBtn);
        buttonPanel.add(updateStatusBtn);
        buttonPanel.add(autoPromoteBtn);
        buttonPanel.add(workOnTaskBtn);
        buttonPanel.add(updateMomentumBtn);
        buttonPanel.add(showInsightsBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        // Message area
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        messageScrollPane.setPreferredSize(new Dimension(0, 100));
        add(messageScrollPane, BorderLayout.NORTH);

        // Action listeners
        addTaskBtn.addActionListener(new AddTaskListener());
        deleteTaskBtn.addActionListener(new DeleteTaskListener());
        undoDeleteBtn.addActionListener(new UndoDeleteListener());
        addSubtaskBtn.addActionListener(new AddSubtaskListener());
        updateStatusBtn.addActionListener(new UpdateStatusListener());
        autoPromoteBtn.addActionListener(new AutoPromoteListener());
        workOnTaskBtn.addActionListener(new WorkOnTaskListener());
        updateMomentumBtn.addActionListener(new UpdateMomentumListener());
        showInsightsBtn.addActionListener(new ShowInsightsListener());

        refreshTaskList();
    }

    private void refreshTaskList() {
        listModel.clear();
        service.getTasks().forEach(t -> {
            listModel.addElement(new TaskUIItem(t, false));
            t.getSubtasks().forEach(st -> listModel.addElement(new TaskUIItem(st, true)));
        });
    }

    private static class TaskUIItem {
        private final Task task;
        private final boolean isSubtask;

        public TaskUIItem(Task task, boolean isSubtask) {
            this.task = task;
            this.isSubtask = isSubtask;
        }
    }

    private class TaskCellRenderer extends JPanel implements ListCellRenderer<TaskUIItem> {
        private JLabel titleLabel = new JLabel();
        private JLabel metaLabel = new JLabel();
        private JLabel statusLabel = new JLabel();
        private JProgressBar momentumBar = new JProgressBar(0, 100);

        public TaskCellRenderer() {
            setLayout(new BorderLayout(10, 5));
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            setBackground(Color.WHITE);

            JPanel centerPanel = new JPanel(new GridLayout(2, 1));
            centerPanel.setOpaque(false);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            metaLabel.setForeground(Color.GRAY);
            centerPanel.add(titleLabel);
            centerPanel.add(metaLabel);

            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setOpaque(false);
            statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));

            momentumBar.setPreferredSize(new Dimension(90, 18));
            momentumBar.setStringPainted(true);
            momentumBar.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
            momentumBar.setBorderPainted(false);
            momentumBar.setBackground(new Color(230, 230, 230));

            rightPanel.add(statusLabel, BorderLayout.CENTER);
            rightPanel.add(momentumBar, BorderLayout.SOUTH);

            add(centerPanel, BorderLayout.CENTER);
            add(rightPanel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends TaskUIItem> list, TaskUIItem value, int index, boolean isSelected, boolean cellHasFocus) {
            Task task = value.task;

            // Background & Selection
            if (isSelected) {
                setBackground(new Color(230, 240, 255));
            } else {
                setBackground(Color.WHITE);
            }

            // Indentation
            int leftInset = value.isSubtask ? 40 : 10;

            // Priority Color
            Color priorityColor;
            switch(task.getPriority()) {
                case HIGH: priorityColor = new Color(255, 80, 80); break;
                case MEDIUM: priorityColor = new Color(255, 180, 50); break;
                case LOW: priorityColor = new Color(100, 200, 120); break;
                default: priorityColor = Color.LIGHT_GRAY;
            }

            // Create card-like effect with border
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, leftInset, 5, 10),
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, priorityColor),
                        BorderFactory.createEmptyBorder(0, 10, 0, 0)
                    )
                )
            ));

            // Text Data
            titleLabel.setText(task.getName());
            metaLabel.setText(String.format("%s • %s • %s • %s",
                task.getPriority(),
                task.getCategory(),
                task.getDeadline(),
                task.getId().substring(0, 6)));

            // Strikethrough if completed
            Map<TextAttribute, Object> attributes = new HashMap<>(titleLabel.getFont().getAttributes());
            if (task.getStatus() == Status.COMPLETED) {
                attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                titleLabel.setForeground(Color.LIGHT_GRAY);
            } else {
                attributes.put(TextAttribute.STRIKETHROUGH, false);
                titleLabel.setForeground(Color.DARK_GRAY);
            }
            titleLabel.setFont(new Font(attributes));

            // Status Label
            statusLabel.setText(task.getStatus().name());
            statusLabel.setForeground(task.getStatus() == Status.COMPLETED ? new Color(100, 180, 100) : Color.GRAY);

            // Momentum Bar
            int m = task.getMomentum();
            momentumBar.setValue(m);

            String icon;
            if (m >= 90) icon = "🔥🔥🔥";
            else if (m >= 60) icon = "🔥🔥";
            else if (m >= 30) icon = "🔥";
            else icon = "❄️";

            momentumBar.setString(m + " " + icon);

            if (m >= 80) momentumBar.setForeground(new Color(255, 80, 80));
            else if (m >= 50) momentumBar.setForeground(new Color(255, 165, 0));
            else momentumBar.setForeground(new Color(100, 180, 255));

            return this;
        }
    }

    // Inner classes for listeners
    private class AddTaskListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextField nameField = new JTextField();
            JTextField categoryField = new JTextField();
            JTextField deadlineField = new JTextField();
            JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());

            Object[] message = {
                    "Name:", nameField,
                    "Category:", categoryField,
                    "Deadline (YYYY-MM-DD):", deadlineField,
                    "Priority:", priorityBox
            };

            int option = JOptionPane.showConfirmDialog(TodoGUI.this, message, "Add Task", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String name = nameField.getText();
                String category = categoryField.getText();
                String deadlineStr = deadlineField.getText();
                Priority priority = (Priority) priorityBox.getSelectedItem();

                try {
                    LocalDate deadline = LocalDate.parse(deadlineStr);
                    service.addTask(name, category, deadline, priority);
                    refreshTaskList();
                    messageArea.append("Task added: " + name + "\n");
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(TodoGUI.this, "Invalid date format. Use YYYY-MM-DD.");
                }
            }
        }
    }

    private class DeleteTaskListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String idPrefix = JOptionPane.showInputDialog(TodoGUI.this, "Enter task ID prefix (first 6 chars):");
            if (idPrefix != null) {
                service.deleteTaskById(idPrefix);
                refreshTaskList();
                messageArea.append("Task deleted with ID prefix: " + idPrefix + "\n");
            }
        }
    }

    private class UndoDeleteListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            service.undoDelete();
            refreshTaskList();
            messageArea.append("Last delete action undone.\n");
        }
    }

    private class AddSubtaskListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextField parentIdField = new JTextField();
            JTextField nameField = new JTextField();
            JTextField deadlineField = new JTextField();
            JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());

            Object[] message = {
                    "Parent ID prefix:", parentIdField,
                    "Subtask Name:", nameField,
                    "Deadline (YYYY-MM-DD):", deadlineField,
                    "Priority:", priorityBox
            };

            int option = JOptionPane.showConfirmDialog(TodoGUI.this, message, "Add Subtask", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String parentId = parentIdField.getText();
                String name = nameField.getText();
                String deadlineStr = deadlineField.getText();
                Priority priority = (Priority) priorityBox.getSelectedItem();

                try {
                    LocalDate deadline = LocalDate.parse(deadlineStr);
                    service.addSubtask(parentId, name, deadline, priority);
                    refreshTaskList();
                    messageArea.append("Subtask added: " + name + " under parent ID: " + parentId + "\n");
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(TodoGUI.this, "Invalid date format. Use YYYY-MM-DD.");
                }
            }
        }
    }

    private class UpdateStatusListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String idPrefix = JOptionPane.showInputDialog(TodoGUI.this, "Enter task ID prefix:");
            if (idPrefix != null) {
                JComboBox<Status> statusBox = new JComboBox<>(Status.values());
                int option = JOptionPane.showConfirmDialog(TodoGUI.this, new Object[]{"New Status:", statusBox}, "Update Status", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    Status status = (Status) statusBox.getSelectedItem();
                    service.updateTaskStatus(idPrefix, status);
                    refreshTaskList();
                    messageArea.append("Task status updated for ID prefix: " + idPrefix + " to " + status + "\n");
                }
            }
        }
    }

    private class AutoPromoteListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            service.autoPromotePriorities();
            refreshTaskList();
            messageArea.append("Task priorities auto-promoted.\n");
        }
    }

    private class WorkOnTaskListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String idPrefix = JOptionPane.showInputDialog(TodoGUI.this, "Enter task ID prefix to work on:");
            if (idPrefix != null) {
                service.workOnTask(idPrefix);
                refreshTaskList();
                messageArea.append("Working on task with ID prefix: " + idPrefix + "\n");
            }
        }
    }

    private class UpdateMomentumListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            service.updateMomentum();
            refreshTaskList();
            messageArea.append("Task momentum updated.\n");
        }
    }

    private class ShowInsightsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String insights = service.showMomentumInsights();
            messageArea.append(insights);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TodoGUI().setVisible(true);
        });
    }
}