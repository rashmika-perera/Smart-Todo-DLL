package com.todo;

import com.todo.model.Priority;
import com.todo.model.Status;
import com.todo.service.TodoService;

import java.time.LocalDate;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TodoService service = new TodoService();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- SMART TO-DO (DLL) ---");
            System.out.println("1) Add task");
            System.out.println("2) Display all");
            System.out.println("3) Delete task");
            System.out.println("4) Undo delete");
            System.out.println("5) Add subtask");
            System.out.println("6) Update task status");
            System.out.println("7) Auto-promote priorities");
            System.out.println("8) Work on task (boost momentum)");
            System.out.println("9) Update momentum (apply decay)");
            System.out.println("10) Show momentum insights");
            System.out.println("0) Exit");
            System.out.print("Choose: ");

            String choiceInput = sc.nextLine();
            int c;
            try {
                c = Integer.parseInt(choiceInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice. Please enter a valid number.");
                continue;
            }
            switch (c) {
                case 1 -> {
                    System.out.print("Name: ");
                    String name = sc.nextLine();
                    System.out.print("Category: ");
                    String cat = sc.nextLine();
                    System.out.print("Deadline (YYYY-MM-DD): ");
                    String dateInput = sc.nextLine();
                    LocalDate d;
                    try {
                        d = LocalDate.parse(dateInput);
                    } catch (Exception e) {
                        System.out.println("Invalid date format. Please use YYYY-MM-DD.");
                        continue;
                    }
                    System.out.print("Priority (LOW/MEDIUM/HIGH/CRITICAL): ");
                    String priorityInput = sc.nextLine();
                    Priority p;
                    try {
                        p = Priority.valueOf(priorityInput.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid priority. Please choose LOW, MEDIUM, HIGH, or CRITICAL.");
                        continue;
                    }
                    service.addTask(name, cat, d, p);
                }
                case 2 -> service.displayAll();
                case 3 -> {
                    System.out.print("Task id prefix (first 6 chars): ");
                    service.deleteTaskById(sc.nextLine());
                }
                case 4 -> service.undoDelete();
                case 5 -> {
                    System.out.print("Parent id prefix: ");
                    String pid = sc.nextLine();
                    System.out.print("Subtask name: ");
                    String sname = sc.nextLine();
                    System.out.print("Deadline (YYYY-MM-DD): ");
                    String subDateInput = sc.nextLine();
                    LocalDate sd;
                    try {
                        sd = LocalDate.parse(subDateInput);
                    } catch (Exception e) {
                        System.out.println("Invalid date format. Please use YYYY-MM-DD.");
                        continue;
                    }
                    System.out.print("Priority (LOW/MEDIUM/HIGH/CRITICAL): ");
                    String subPriorityInput = sc.nextLine();
                    Priority sp;
                    try {
                        sp = Priority.valueOf(subPriorityInput.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid priority. Please choose LOW, MEDIUM, HIGH, or CRITICAL.");
                        continue;
                    }
                    service.addSubtask(pid, sname, sd, sp);
                }
                case 6 -> {
                    System.out.print("Task id prefix: ");
                    String id = sc.nextLine();
                    System.out.print("New status (PENDING/IN_PROGRESS/COMPLETED): ");
                    String statusInput = sc.nextLine();
                    Status st;
                    try {
                        st = Status.valueOf(statusInput.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid status. Please choose PENDING, IN_PROGRESS, or COMPLETED.");
                        continue;
                    }
                    service.updateTaskStatus(id, st);
                }
                case 7 -> service.autoPromotePriorities();

                // 🆕 NEW MOMENTUM FEATURES
                case 8 -> {
                    System.out.print("Task id prefix to work on: ");
                    String workId = sc.nextLine();
                    service.workOnTask(workId);
                }
                case 9 -> service.updateMomentum();
                case 10 -> service.showMomentumInsights();

                case 0 -> {
                    System.out.println("Bye!");
                    sc.close();
                    return;
                }
                default -> System.out.println("❌ Invalid choice.");
            }
            
        }
    }
}