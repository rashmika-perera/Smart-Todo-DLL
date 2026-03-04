package com.todo.ui;

import com.todo.model.Task;
import com.todo.model.Priority;
import com.todo.model.Status;
import com.todo.service.TodoService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class TodoGUI extends JFrame {
    private TodoService service;
    private JList<String> taskList;
    private DefaultListModel<String> listModel;
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
        workOnTaskBtn = new JButton("Work on Task");
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
        List<String> tasks = getAllTaskStrings();
        for (String task : tasks) {
            listModel.addElement(task);
        }
    }

    private List<String> getAllTaskStrings() {
        List<String> result = new ArrayList<>();
        service.getTasks().forEach(t -> {
            result.add(t.toString());
            t.getSubtasks().forEach(st -> result.add("   ↳ " + st.toString()));
        });
        return result;
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