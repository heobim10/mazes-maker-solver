package datastructures.concrete;

import datastructures.concrete.dictionaries.ChainedHashDictionary;
import datastructures.interfaces.IDictionary;
import datastructures.interfaces.IDisjointSet;

/**
 * @see IDisjointSet for more details.
 */
public class ArrayDisjointSet<T> implements IDisjointSet<T> {
    // Note: do NOT rename or delete this field. We will be inspecting it
    // directly within our private tests.
    private int[] pointers;

    // However, feel free to add more fields and private helper methods.
    // You will probably need to add one or two more fields in order to
    // successfully implement this class.
    private int size;

    private IDictionary<T, Integer> nodeMap;

    public ArrayDisjointSet() {
        size = 0;
        pointers = new int[50];
        nodeMap = new ChainedHashDictionary<>();
    }

    @Override
    public void makeSet(T item) {
        if (nodeMap.containsKey(item)) {
            throw new IllegalArgumentException();
        }
        ensureArrayCapacity();
        nodeMap.put(item, size);
        pointers[size] = -1;
        size++;
    }

    @Override
    public int findSet(T item) {
        if (!nodeMap.containsKey(item)) {
            throw new IllegalArgumentException();
        }
        int index = nodeMap.get(item);
        return findHelper(index);
    }

    private int findHelper(int index) {
        if (pointers[index] < 0) {
            return index;
        } else {
            pointers[index] = findHelper(pointers[index]);
            return pointers[index];
        }
    }

    @Override
    public void union(T item1, T item2) {
        if (!nodeMap.containsKey(item1) || !nodeMap.containsKey(item2)) {
            throw new IllegalArgumentException();
        }
        int rep1 = findSet(item1);
        int rep2 = findSet(item2);
        if (pointers[rep1] == pointers[rep2]) {
            pointers[rep2] = rep1;
            pointers[rep1]--;
        } else if (pointers[rep1] < pointers[rep2]) {
            pointers[rep2] = rep1;
        } else {
            pointers[rep1] = rep2;
        }

    }

    private void ensureArrayCapacity() {
        if (size == pointers.length) {
            int[] newArr = new int[pointers.length * 2];
            System.arraycopy(pointers, 0, newArr, 0, size);
            pointers = newArr;
        }
    }
}
