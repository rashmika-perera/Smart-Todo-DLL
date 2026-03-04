package com.todo.model;

import com.todo.ds.DoublyLinkedList;
import java.time.LocalDate;
import java.util.UUID;

public class Task {
    private String id = UUID.randomUUID().toString();
    private String name;
    private String category;
    private LocalDate deadline;
    private Priority priority;
    private Status status;

    private int momentum = 0;

    private final DoublyLinkedList<Task> subtasks = new DoublyLinkedList<>();
    private String parentId;

    public Task(String name, String category, LocalDate deadline, Priority priority) {
        this.name = name;
        this.category = category;
        this.deadline = deadline;
        this.priority = priority;
        this.status = Status.PENDING;
    }

    // Constructor for DB loading
    public Task(String id, String name, String category, LocalDate deadline, Priority priority, Status status, int momentum) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
        this.momentum = momentum;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public LocalDate getDeadline() { return deadline; }
    public Priority getPriority() { return priority; }
    public Status getStatus() { return status; }
    public DoublyLinkedList<Task> getSubtasks() { return subtasks; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public int getMomentum() { return momentum; }
    public void setMomentum(int momentum) { this.momentum = momentum; }

    public void setPriority(Priority priority) { this.priority = priority; }
    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        String momentumIndicator = getMomentumIndicator();
        return String.format("[%s] %s | MomentumIndicator:%s | Cat:%s | Due:%s | Priority:%s | Status:%s | Momentum:%d",
                id.substring(0, 6), name, momentumIndicator, category, deadline, priority, status, momentum);
    }

    private String getMomentumIndicator() {
        if (momentum >= 90) return "🔥🔥🔥🔥";
        if (momentum >= 70) return "🔥🔥🔥";
        if (momentum >= 50) return "🔥🔥";
        if (momentum >= 20) return "🔥";
        if (momentum >= 0) return "❄️";
        return "❄️❄️❄️";
    }
}