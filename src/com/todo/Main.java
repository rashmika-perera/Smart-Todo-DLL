package com.todo;

import com.todo.ui.TodoGUI;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TodoGUI().setVisible(true);
        });
    }
}