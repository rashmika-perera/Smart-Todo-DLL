package com.todo.service;

import com.todo.ds.DoublyLinkedList;
import com.todo.ds.Node;
import com.todo.model.Priority;
import com.todo.model.Status;
import com.todo.model.Task;

import java.time.LocalDate;

public class TodoService {

    // main tasks list
    private final DoublyLinkedList<Task> tasks = new DoublyLinkedList<>();

    // deleted tasks stack (DLL) for undo
    private final DoublyLinkedList<UndoRecord> undo = new DoublyLinkedList<>();

    // Momentum tracker
    private final MomentumTracker momentumTracker = new MomentumTracker();

    private static class UndoRecord {
        Task task;
        // where it was in the list (we store neighbors)
        Node<Task> prev;
        Node<Task> next;

        UndoRecord(Task task, Node<Task> prev, Node<Task> next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }

    public TodoService() {
        // Initialize DB
        DatabaseManager.initializeDatabase();
        // Load tasks from DB on startup
        this.tasks.clear(); // Ensure clear start
        DoublyLinkedList<Task> loadedTasks = DatabaseManager.loadAllTasks();
        loadedTasks.forEach(this.tasks::addLast);
    }

    public DoublyLinkedList<Task> getTasks() {
        return tasks;
    }

    public void addTask(String name, String category, LocalDate deadline, Priority priority) {
        Task newTask = new Task(name, category, deadline, priority);
        tasks.addLast(newTask);
        DatabaseManager.saveTask(newTask, null); // Save to DB
        System.out.println("✅ Task added.");
    }

    public void displayAll() {
        if (tasks.isEmpty()) {
            System.out.println("(No tasks)");
            return;
        }
        tasks.forEach(t -> {
            System.out.println(t);
            if (t.getSubtasks().isEmpty()) return;
            t.getSubtasks().forEach(st -> System.out.println("   ↳ " + st));
        });
    }

    public void deleteTaskById(String idPrefix) {
        Node<Task> node = tasks.find(t -> t.getId().startsWith(idPrefix));
        if (node == null) {
            System.out.println("❌ Task not found.");
            return;
        }
        // save undo info
        undo.addLast(new UndoRecord(node.data, node.prev, node.next));
        tasks.removeNode(node);
        DatabaseManager.deleteTask(node.data.getId()); // Delete from DB
        System.out.println("🗑️ Task deleted. (You can undo)");
    }

    public void undoDelete() {
        Node<UndoRecord> last = undo.getTail();
        if (last == null) {
            System.out.println("❌ Nothing to undo.");
            return;
        }

        UndoRecord rec = last.data;
        undo.removeNode(last);

        // restore by inserting between prev and next
        Node<Task> newNode = new Node<>(rec.task);

        // If list empty
        if (tasks.getHead() == null) {
            // easiest: addLast
            tasks.addLast(rec.task);
            DatabaseManager.saveTask(rec.task, rec.task.getParentId()); // Restore to DB
            System.out.println("↩️ Undo restored (as first task).");
            return;
        }

        // If prev is null -> insert at head
        if (rec.prev == null) {
            Node<Task> oldHead = tasks.getHead();
            newNode.next = oldHead;
            oldHead.prev = newNode;
            // set head logic skipped for brevity, simplistic fallback:
            System.out.println("⚠️ Add insertFirst() in DLL for perfect restore.");
            tasks.addLast(rec.task); // fallback

            DatabaseManager.saveTask(rec.task, rec.task.getParentId()); // Restore to DB
            return;
        }

        // Otherwise, insert after prev (basic restore)
        Node<Task> prev = rec.prev;
        Node<Task> next = rec.next;

        prev.next = newNode;
        newNode.prev = prev;

        if (next != null) {
            newNode.next = next;
            next.prev = newNode;
        }

        DatabaseManager.saveTask(rec.task, rec.task.getParentId()); // Restore to DB

        System.out.println("↩️ Undo restored task.");
    }

    private Task findTaskByIdPrefix(String idPrefix) {
        // First, check main tasks
        Node<Task> node = tasks.find(t -> t.getId().startsWith(idPrefix));
        if (node != null) return node.data;
        // Then, check subtasks of each main task
        for (Node<Task> mainNode = tasks.getHead(); mainNode != null; mainNode = mainNode.next) {
            Node<Task> subNode = mainNode.data.getSubtasks().find(st -> st.getId().startsWith(idPrefix));
            if (subNode != null) return subNode.data;
        }
        return null;
    }

    private Task findParent(Task subtask) {
        for (Node<Task> mainNode = tasks.getHead(); mainNode != null; mainNode = mainNode.next) {
            if (mainNode.data.getSubtasks().find(st -> st == subtask) != null) {
                return mainNode.data;
            }
        }
        return null;
    }

    // Conditional workflow: parent cannot be completed if any subtask not completed
    public void updateTaskStatus(String idPrefix, Status newStatus) {
        Task task = findTaskByIdPrefix(idPrefix);
        if (task == null) { System.out.println("❌ Task not found."); return; }

        momentumTracker.recordInteraction(task, MomentumTracker.InteractionType.WORK);

        boolean isMainTask = tasks.find(t -> t == task) != null;

        if (isMainTask && newStatus == Status.COMPLETED) {
            boolean allDone = true;
            Node<Task> st = task.getSubtasks().getHead();
            while (st != null) {
                if (st.data.getStatus() != Status.COMPLETED) { allDone = false; break; }
                st = st.next;
            }
            if (!allDone) {
                System.out.println("❌ Can't complete parent. Complete all subtasks first.");
                return;
            }
        }

        task.setStatus(newStatus);

        // Save changes to DB
        DatabaseManager.saveTask(task, task.getParentId());

        // Handle subtask status changes affecting parent
        if (!isMainTask) {
            Task parent = findParent(task);
            if (parent != null) {
                // NEW: Boost parent momentum when subtask completed
                if (newStatus == Status.COMPLETED) {
                    momentumTracker.recordInteraction(parent, MomentumTracker.InteractionType.SUBTASK_COMPLETE);
                }

                // Check if all subtasks are completed
                boolean allCompleted = true;
                Node<Task> st = parent.getSubtasks().getHead();
                while (st != null) {
                    if (st.data.getStatus() != Status.COMPLETED) {
                        allCompleted = false;
                        break;
                    }
                    st = st.next;
                }
                if (allCompleted) {
                    parent.setStatus(Status.COMPLETED);
                    DatabaseManager.saveTask(parent, null); // Save parent
                } else if (newStatus == Status.IN_PROGRESS && parent.getStatus() == Status.PENDING) {
                    parent.setStatus(Status.IN_PROGRESS);
                    DatabaseManager.saveTask(parent, null); // Save parent
                }
            }
        }

        System.out.println("✅ Status updated.");

        // Auto-reorder by momentum
        momentumTracker.reorderByMomentum(tasks);
    }

    public void addSubtask(String parentIdPrefix, String name, LocalDate deadline, Priority priority) {
        Node<Task> node = tasks.find(t -> t.getId().startsWith(parentIdPrefix));
        if (node == null) { System.out.println("❌ Parent task not found."); return; }

        // Record interaction when adding subtask
        momentumTracker.recordInteraction(node.data, MomentumTracker.InteractionType.COMMENT);

        Task sub = new Task(name, node.data.getCategory(), deadline, priority);
        node.data.getSubtasks().addLast(sub);

        // Save subtask
        DatabaseManager.saveTask(sub, node.data.getId());

        System.out.println("✅ Subtask added.");
    }

    // Priority promotion by deadline/aging (simple version)
    public String autoPromotePriorities() {
        LocalDate today = LocalDate.now();
        int[] promoted = {0};
        tasks.forEach(t -> {
            if (t.getDeadline() == null) return;

            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, t.getDeadline());

            if (daysLeft < 0) {
                t.setPriority(Priority.CRITICAL);
                promoted[0]++;
            } else if (daysLeft <= 1 && t.getPriority().ordinal() < Priority.HIGH.ordinal()) {
                t.setPriority(Priority.HIGH);
                promoted[0]++;
            } else if (daysLeft <= 3 && t.getPriority().ordinal() < Priority.MEDIUM.ordinal()) {
                t.setPriority(Priority.MEDIUM);
                promoted[0]++;
            }
        });
        return "⚡ Auto promotion done (based on deadlines). Promoted " + promoted[0] + " tasks.";
    }

    public void updateMomentum() {
        momentumTracker.applyDecay(tasks);
        momentumTracker.reorderByMomentum(tasks);

        // Save all updated momentums
        tasks.forEach(task -> DatabaseManager.saveTask(task, null));

        System.out.println("⏰ Momentum updated based on time decay.");
    }

    public String showMomentumInsights() {
        return momentumTracker.showInsights(tasks);
    }

    // Work on a task (simulate interaction)
    public void workOnTask(String idPrefix) {
        Task task = findTaskByIdPrefix(idPrefix);
        if (task == null) {
            System.out.println("❌ Task not found.");
            return;
        }

        momentumTracker.recordInteraction(task, MomentumTracker.InteractionType.WORK);
        momentumTracker.reorderByMomentum(tasks);
        // Save momentum update
        DatabaseManager.saveTask(task, task.getParentId());

        System.out.println("✅ Worked on task: " + task.getName());
    }
}