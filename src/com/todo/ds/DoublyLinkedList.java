package com.todo.ds;

import java.util.function.Consumer;

public class DoublyLinkedList<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size = 0;

    public Node<T> getHead() { return head; }
    public Node<T> getTail() { return tail; }
    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public Node<T> addLast(T data) {
        Node<T> node = new Node<>(data);
        if (head == null) {
            head = tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
        size++;
        return node;
    }

    public void removeNode(Node<T> node) {
        if (node == null) return;

        if (node == head) head = node.next;
        if (node == tail) tail = node.prev;

        if (node.prev != null) node.prev.next = node.next;
        if (node.next != null) node.next.prev = node.prev;

        node.prev = null;
        node.next = null;
        size--;
    }

    public void forEach(Consumer<T> action) {
        Node<T> cur = head;
        while (cur != null) {
            action.accept(cur.data);
            cur = cur.next;
        }
    }

    // helper: find node by predicate (simple)
    public Node<T> find(java.util.function.Predicate<T> predicate) {
        Node<T> cur = head;
        while (cur != null) {
            if (predicate.test(cur.data)) return cur;
            cur = cur.next;
        }
        return null;
    }
}
