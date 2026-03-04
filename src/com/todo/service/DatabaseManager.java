package com.todo.service;

import com.todo.model.Priority;
import com.todo.model.Status;
import com.todo.model.Task;
import com.todo.ds.DoublyLinkedList;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    // SQLite connection string
    private static final String URL = "jdbc:sqlite:smart_todo.db";

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        // SQLite foreign keys are disabled by default. We must enable them per connection.
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void initializeDatabase() {
        String createTasksTable = "CREATE TABLE IF NOT EXISTS tasks (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "category TEXT, " +
                "deadline TEXT, " +
                "priority TEXT, " +
                "status TEXT, " +
                "momentum INTEGER DEFAULT 0, " +
                "parent_id TEXT, " +
                "FOREIGN KEY (parent_id) REFERENCES tasks(id) ON DELETE CASCADE" +
                ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTasksTable);
            System.out.println("📦 Database initialized.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveTask(Task task, String parentId) {
        String query = "INSERT INTO tasks (id, name, category, deadline, priority, status, momentum, parent_id) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                       "ON CONFLICT(id) DO UPDATE SET " +
                       "name=excluded.name, " +
                       "category=excluded.category, " +
                       "deadline=excluded.deadline, " +
                       "priority=excluded.priority, " +
                       "status=excluded.status, " +
                       "momentum=excluded.momentum, " +
                       "parent_id=excluded.parent_id";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, task.getId());
            stmt.setString(2, task.getName());
            stmt.setString(3, task.getCategory());
            stmt.setString(4, task.getDeadline() != null ? task.getDeadline().toString() : null);
            stmt.setString(5, task.getPriority().name());
            stmt.setString(6, task.getStatus().name());
            stmt.setInt(7, task.getMomentum());
            stmt.setString(8, parentId);

            stmt.executeUpdate();

            // Recursively save subtasks
            if (task.getSubtasks() != null) {
                task.getSubtasks().forEach(sub -> saveTask(sub, task.getId()));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteTask(String id) {
        String query = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DoublyLinkedList<Task> loadAllTasks() {
        DoublyLinkedList<Task> allTasks = new DoublyLinkedList<>();
        Map<String, Task> taskMap = new HashMap<>();

        String query = "SELECT * FROM tasks";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String category = rs.getString("category");
                String deadlineStr = rs.getString("deadline");
                LocalDate deadline = (deadlineStr != null) ? LocalDate.parse(deadlineStr) : null;
                Priority priority = Priority.valueOf(rs.getString("priority"));
                Status status = Status.valueOf(rs.getString("status"));
                int momentum = rs.getInt("momentum");
                String parentId = rs.getString("parent_id");

                Task task = new Task(id, name, category, deadline, priority, status, momentum);
                task.setParentId(parentId);

                taskMap.put(id, task);
            }

            // Reconstruct hierarchy
            for (Task task : taskMap.values()) {
                if (task.getParentId() == null) {
                    allTasks.addLast(task);
                } else {
                    Task parent = taskMap.get(task.getParentId());
                    if (parent != null) {
                        parent.getSubtasks().addLast(task);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return allTasks;
    }
}
