package datastructures.concrete;

import datastructures.concrete.dictionaries.ChainedHashDictionary;
import datastructures.interfaces.IDictionary;
import datastructures.interfaces.IPriorityQueue;
import misc.exceptions.EmptyContainerException;
import misc.exceptions.InvalidElementException;

/**
 * @see IPriorityQueue for details on what each method must do.
 */
public class ArrayHeap<T extends Comparable<T>> implements IPriorityQueue<T> {
    // See spec: you must implement a implement a 4-heap.
    private static final int NUM_CHILDREN = 4;
    private static final int DEFAULT_CAPACITY = 50;

    // You MUST use this field to store the contents of your heap.
    // You may NOT rename this field: we will be inspecting it within
    // our private tests.
    private T[] heap;

    // Feel free to add more fields and constants.
    private IDictionary<T, Integer> map;
    private int size;

    public ArrayHeap() {
        heap = makeArrayOfT(DEFAULT_CAPACITY);
        map = new ChainedHashDictionary<>();
        size = 0;
    }

    /**
     * This method will return a new, empty array of the given size
     * that can contain elements of type T.
     * <p>
     * Note that each element in the array will initially be null.
     */
    @SuppressWarnings("unchecked")
    private T[] makeArrayOfT(int arraySize) {
        // This helper method is basically the same one we gave you
        // in ArrayDictionary and ChainedHashDictionary.
        //
        // As before, you do not need to understand how this method
        // works, and should not modify it in any way.
        return (T[]) (new Comparable[arraySize]);
    }

    /**
     * A method stub that you may replace with a helper method for percolating
     * upwards from a given index, if necessary.
     */
    private void percolateUp(int index) {
        int parentIndex = (index - 1) / NUM_CHILDREN;
        int parentDistance = heap[parentIndex].compareTo(heap[index]);

        if (parentDistance > 0) {
            map.put(heap[parentIndex], index);
            map.put(heap[index], parentIndex);
            swap(index, parentIndex);
            percolateUp(parentIndex);
        }
    }

    /**
     * A method stub that you may replace with a helper method for percolating
     * downwards from a given index, if necessary.
     */
    private void percolateDown(int index) {
        int childIndex = index;

        for (int i = 1; i <= NUM_CHILDREN; i++) {
            int thisIndex = NUM_CHILDREN * index + i;

            if (thisIndex >= size) {
                break;
            }
            int thisDistance = heap[thisIndex].compareTo(heap[childIndex]);
            if (thisDistance < 0) {
                childIndex = thisIndex;
            }
        }

        if (childIndex != index) {
            map.put(heap[childIndex], index);
            map.put(heap[index], childIndex);
            swap(index, childIndex);

            percolateDown(childIndex);
        }
    }

    /**
     * A method stub that you may replace with a helper method for determining
     * which direction an index needs to percolate and percolating accordingly.
     */
    private void percolate(int index) {
        if (index == 0) {
            percolateDown(index);
        } else {
            int parent = (index - 1) / NUM_CHILDREN;
            int distance = heap[index].compareTo(heap[parent]);
            if (distance < 0) {
                percolateUp(index);
            } else if (distance > 0) {
                percolateDown(index);
            }
        }
    }

    /**
     * A method stub that you may replace with a helper method for swapping
     * the elements at two indices in the 'heap' array.
     */
    private void swap(int a, int b) {
        T oldB = heap[b];
        heap[b] = heap[a];
        heap[a] = oldB;
    }

    @Override
    public T removeMin() {
        T target = peekMin();

        map.put(heap[size - 1], 0);
        map.remove(target);
        heap[0] = heap[size - 1];
        heap[size - 1] = null;
        size--;
        percolateDown(0);

        return target;
    }

    @Override
    public T peekMin() {
        if (size == 0) {
            throw new EmptyContainerException();
        }

        return heap[0];
    }

    @Override
    public void add(T item) {
        if (item == null) {
            throw new IllegalArgumentException();
        }

        if (contains(item)) {
            throw new InvalidElementException();
        }

        ensureCapacity();

        map.put(item, size);
        heap[size] = item;
        size++;
        percolateUp(size - 1);
    }

    @Override
    public boolean contains(T item) {
        if (item == null) {
            throw new IllegalArgumentException();
        }
        return map.containsKey(item);
    }

    @Override
    public void remove(T item) {
        if (item == null) {
            throw new IllegalArgumentException();
        }
        if (!contains(item)) {
            throw new InvalidElementException();
        }
        int index = map.remove(item);
        if (index == size - 1) {
            heap[index] = null;
            size--;
        } else {
            T last = heap[size - 1];
            swap(index, size - 1);
            map.put(last, index);
            heap[size - 1] = null;
            size--;
            percolate(index);
        }
    }

    @Override
    public void replace(T oldItem, T newItem) {
        if (newItem == null) {
            throw new IllegalArgumentException();
        }
        if (oldItem == null || contains(newItem) || !contains(oldItem)) {
            throw new InvalidElementException();
        }
        int index = map.remove(oldItem);
        heap[index] = newItem;
        map.put(newItem, index);
        percolate(index);
    }

    @Override
    public int size() {
        return this.size;
    }

    private void ensureCapacity() {
        if (size == heap.length) {
            T[] newArr = makeArrayOfT(size * 2);
            System.arraycopy(heap, 0, newArr, 0, heap.length);
            heap = newArr;
        }
    }
}