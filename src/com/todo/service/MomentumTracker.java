package com.todo.service;

import com.todo.ds.DoublyLinkedList;
import com.todo.ds.Node;
import com.todo.model.Task;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 🔥 Task Momentum System
 * Tracks task "heat" and automatically reorders tasks in DLL based on momentum
 */
public class MomentumTracker {

    // Store last interaction time for each task
    private final Map<String, LocalDateTime> lastInteractionMap = new HashMap<>();

    // Momentum configuration constants
    private static final int MOMENTUM_WORK_BOOST = 10;
    private static final int MOMENTUM_SUBTASK_BOOST = 5;
    private static final int MOMENTUM_COMMENT_BOOST = 3;
    private static final int MOMENTUM_DAILY_DECAY = 5;
    private static final int MOMENTUM_THREE_DAY_DECAY = 15;
    private static final int MOMENTUM_WEEKLY_DECAY = 30;

    /**
     * Record that user interacted with a task
     */
    public void recordInteraction(Task task, InteractionType type) {
        lastInteractionMap.put(task.getId(), LocalDateTime.now());

        int boost = switch (type) {
            case WORK -> MOMENTUM_WORK_BOOST;
            case SUBTASK_COMPLETE -> MOMENTUM_SUBTASK_BOOST;
            case COMMENT -> MOMENTUM_COMMENT_BOOST;
        };

        task.setMomentum(task.getMomentum() + boost);
        System.out.println("🔥 Task momentum increased by " + boost + " → " + task.getMomentum());
    }

    /**
     * Apply time-based decay to all tasks
     */
    public void applyDecay(DoublyLinkedList<Task> tasks) {
        LocalDateTime now = LocalDateTime.now();

        tasks.forEach(task -> {
            LocalDateTime lastInteraction = lastInteractionMap.get(task.getId());

            if (lastInteraction == null) {
                // First time seeing this task, initialize
                lastInteractionMap.put(task.getId(), now);
                return;
            }

            long daysSinceInteraction = ChronoUnit.DAYS.between(lastInteraction, now);

            if (daysSinceInteraction >= 7) {
                task.setMomentum(Math.max(0, task.getMomentum() - MOMENTUM_WEEKLY_DECAY));
            } else if (daysSinceInteraction >= 3) {
                task.setMomentum(Math.max(0, task.getMomentum() - MOMENTUM_THREE_DAY_DECAY));
            } else if (daysSinceInteraction >= 1) {
                task.setMomentum(Math.max(0, task.getMomentum() - MOMENTUM_DAILY_DECAY));
            }
        });
    }

    /**
     * Reorder tasks in DLL based on momentum (bubble highest to top)
     * Uses DLL's O(1) pointer swapping advantage!
     */
    public void reorderByMomentum(DoublyLinkedList<Task> tasks) {
        if (tasks.size() <= 1) return;

        boolean swapped;
        int swapCount = 0;

        do {
            swapped = false;
            Node<Task> current = tasks.getHead();

            while (current != null && current.next != null) {
                if (current.data.getMomentum() < current.next.data.getMomentum()) {
                    // Swap nodes in DLL - O(1) operation!
                    swapNodes(tasks, current, current.next);
                    swapped = true;
                    swapCount++;
                    // current stays the same (it's now in the next position)
                } else {
                    current = current.next;
                }
            }
        } while (swapped);

        if (swapCount > 0) {
            System.out.println("🔄 Reordered " + swapCount + " tasks by momentum");
        }
    }

    /**
     * Swap two adjacent nodes in DLL - demonstrates O(1) pointer manipulation
     */
    private void swapNodes(DoublyLinkedList<Task> list, Node<Task> node1, Node<Task> node2) {
        if (node1 == null || node2 == null || node1.next != node2) return;

        Node<Task> prevNode = node1.prev;
        Node<Task> nextNode = node2.next;

        // Update head/tail if needed
        if (node1 == list.getHead()) {
            // node2 becomes new head
            setHead(list, node2);
        }
        if (node2 == list.getTail()) {
            // node1 becomes new tail
            setTail(list, node1);
        }

        // Swap pointers
        node2.prev = prevNode;
        node2.next = node1;
        node1.prev = node2;
        node1.next = nextNode;

        // Fix surrounding nodes
        if (prevNode != null) {
            prevNode.next = node2;
        }
        if (nextNode != null) {
            nextNode.prev = node1;
        }
    }

    // Helper methods to set head/tail (using reflection workaround)
    private void setHead(DoublyLinkedList<Task> list, Node<Task> newHead) {
        try {
            var field = DoublyLinkedList.class.getDeclaredField("head");
            field.setAccessible(true);
            field.set(list, newHead);
        } catch (Exception e) {
            // Fallback: handled by normal pointer updates
        }
    }

    private void setTail(DoublyLinkedList<Task> list, Node<Task> newTail) {
        try {
            var field = DoublyLinkedList.class.getDeclaredField("tail");
            field.setAccessible(true);
            field.set(list, newTail);
        } catch (Exception e) {
            // Fallback: handled by normal pointer updates
        }
    }

    /**
     * Get momentum heat indicator
     */
    public static String getMomentumIndicator(int momentum) {
        if (momentum >= 90) return "🔥🔥🔥🔥";
        if (momentum >= 70) return "🔥🔥🔥";
        if (momentum >= 50) return "🔥🔥";
        if (momentum >= 20) return "🔥";
        if (momentum >= 0) return "❄️";
        return "❄️❄️❄️";
    }

    /**
     * Show momentum insights
     */
    public void showInsights(DoublyLinkedList<Task> tasks) {
        System.out.println("\n📊 === MOMENTUM INSIGHTS ===");

        Task hottest = null;
        Task coldest = null;
        int frozenCount = 0;

        Node<Task> current = tasks.getHead();
        while (current != null) {
            Task task = current.data;

            if (hottest == null || task.getMomentum() > hottest.getMomentum()) {
                hottest = task;
            }
            if (coldest == null || task.getMomentum() < coldest.getMomentum()) {
                coldest = task;
            }
            if (task.getMomentum() < 0) {
                frozenCount++;
            }

            current = current.next;
        }

        if (hottest != null) {
            System.out.println("🔥 Hottest: " + hottest.getName() + " (" + hottest.getMomentum() + ")");
        }
        if (coldest != null) {
            System.out.println("❄️  Coldest: " + coldest.getName() + " (" + coldest.getMomentum() + ")");
        }
        if (frozenCount > 0) {
            System.out.println("⚠️  WARNING: " + frozenCount + " frozen tasks need attention!");
        }
        System.out.println();
    }

    public enum InteractionType {
        WORK, SUBTASK_COMPLETE, COMMENT
    }
}