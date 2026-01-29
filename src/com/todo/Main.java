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
            
        }
    }
}