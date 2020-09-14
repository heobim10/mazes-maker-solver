package datastructures.concrete;

import datastructures.interfaces.IList;
import misc.exceptions.EmptyContainerException;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Note: For more info on the expected behavior of your methods:
 *
 * @see datastructures.interfaces.IList
 * (You should be able to control/command+click "IList" above to open the file from IntelliJ.)
 */
public class DoubleLinkedList<T> implements IList<T> {
    // You may not rename these fields or change their types.
    // We will be inspecting these in our private tests.
    // You also may not add any additional fields.
    private Node<T> front;
    private Node<T> back;
    private int size;

    public DoubleLinkedList() {
        this.front = null;
        this.back = null;
        this.size = 0;
    }

    @Override
    public void add(T item) {
        Node<T> node = new Node<T>(item);
        if (size == 0) {
            front = node;
        } else {
            back.next = node;
            node.prev = back;
        }
        back = node;
        size++;
    }

    @Override
    public T remove() {
        if (size == 0) {
            throw new EmptyContainerException();
        }

        return delete(size() - 1);
    }

    @Override
    public T get(int index) {
        ensureIndexBound(index);
        Node<T> node = indexToNode(index);
        return node.data;
    }

    @Override
    public void set(int index, T item) {
        ensureIndexBound(index);

        // Get to the node that we are taking over.
        Node<T> curr = indexToNode(index);

        // `node` is our new node.
        Node<T> node = new Node<T>(curr.prev, item, curr.next);

        if (curr.next != null) { // End case handling.
            curr.next.prev = node;
        } else {
            back = node;
        }

        if (curr.prev != null) { // Front case handling.
            curr.prev.next = node;
        } else {
            front = node;
        }

    }

    @Override
    public void insert(int index, T item) {
        if (index < 0 || index >= size() + 1) {
            throw new IndexOutOfBoundsException();
        }

        // Get to the node that is being shifted.
        Node<T> curr = indexToNode(index);


        if (curr == null || index == size()) { // End or empty case
            add(item);
        } else {
            // Make new node.
            Node<T> node = new Node<T>(curr.prev, item, curr);

            if (curr.prev == null) { // Front case
                front = node;
            } else {
                curr.prev.next = node;
            }

            curr.prev = node;
            size++;
        }

    }

    @Override
    public T delete(int index) {
        ensureIndexBound(index);

        Node<T> result;
        if (index == 0) { // Front case
            result = front;
            if (front.next == null) { // One case
                back = null;
            } else {
                front.next.prev = null;
            }
            front = front.next;
        } else if (index == size() - 1) { // End case
            result = back;
            back.prev.next = null;
            back = back.prev;
        } else { // Normal case
            Node<T> curr = indexToNode(index);
            result = curr;
            curr.prev.next = curr.next;
            curr.next.prev = curr.prev;
        }

        size--;
        return result.data;
    }

    @Override
    public int indexOf(T item) {
        // Empty case.
        if (size == 0) {
            return -1;
        }

        Node<T> curr = front;
        int result = 0;
        while (curr != null) {
            if (Objects.equals(curr.data, item)) {
                return result;
            }

            result++;
            curr = curr.next;
        }

        // Catchall return.
        return -1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean contains(T other) {
        return indexOf(other) != -1;
    }

    @Override
    public Iterator<T> iterator() {
        // Note: we have provided a part of the implementation of
        // an iterator for you. You should complete the methods stubs
        // in the DoubleLinkedListIterator inner class at the bottom
        // of this file. You do not need to change this method.
        return new DoubleLinkedListIterator<>(this.front);
    }

    /* The following methods are helper methods implemented by ourselves */

    // Ensures a user-given index is in bounds.
    // Pre: index accepts a potential index. If 0 < index <= size(), throw
    // IndexOutOfBoundsException(), otherwise no behavior.
    private void ensureIndexBound(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }
    }

    // Find a Node<T> at the requested index.
    // Depending on if the requested index is closer to the front or back,
    // start traversing from the front or back, respectively.
    // Pre: An integer containing a valid index is given.
    // Post: The corresponding node is returned.
    private Node<T> indexToNode(int index) {
        Node<T> curr;
        if (index <= size / 2) {
            curr = front;
            for (int i = 0; i < index; i++) {
                curr = curr.next;
            }
        } else {
            curr = back;
            for (int i = size - 1; i > index; i--) {
                curr = curr.prev;
            }
        }

        return curr;
    }

    private static class Node<E> {
        // You may not change the fields in this node or add any new fields.
        public final E data;
        public Node<E> prev;
        public Node<E> next;

        public Node(Node<E> prev, E data, Node<E> next) {
            this.data = data;
            this.prev = prev;
            this.next = next;
        }

        public Node(E data) {
            this(null, data, null);
        }

        // Feel free to add additional constructors or methods to this class.
    }

    private static class DoubleLinkedListIterator<T> implements Iterator<T> {
        // You should not need to change this field, or add any new fields.
        private Node<T> current;

        public DoubleLinkedListIterator(Node<T> current) {
            // You do not need to make any changes to this constructor.
            this.current = current;
        }

        /**
         * Returns 'true' if the iterator still has elements to look at;
         * returns 'false' otherwise.
         */
        public boolean hasNext() {
            return current != null;
        }

        /**
         * Returns the next item in the iteration and internally updates the
         * iterator to advance one element forward.
         *
         * @throws NoSuchElementException if we have reached the end of the
         *                                iteration and there are no more elements to look at.
         */
        public T next() {
            if (current == null) {
                throw new NoSuchElementException();
            }

            T item = current.data;
            current = current.next;
            return item;
        }
    }
}