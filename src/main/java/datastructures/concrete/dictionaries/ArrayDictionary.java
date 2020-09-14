package datastructures.concrete.dictionaries;

import datastructures.interfaces.IDictionary;
import misc.exceptions.NoSuchKeyException;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * @see datastructures.interfaces.IDictionary
 */
public class ArrayDictionary<K, V> implements IDictionary<K, V> {
    // You may not change or rename this field.
    // We will be inspecting it in our private tests.
    private Pair<K, V>[] pairs;

    // You may add extra fields or helper methods though!
    private int size;
    public static final int DEFAULT_SIZE = 100;

    public ArrayDictionary() {
        this(DEFAULT_SIZE);
    }

    private ArrayDictionary(int size) {
        this.pairs = makeArrayOfPairs(size);
        this.size = 0;
    }

    /**
     * This method will return a new, empty array of the given size
     * that can contain Pair<K, V> objects.
     * <p>
     * Note that each element in the array will initially be null.
     */
    @SuppressWarnings("unchecked")
    private Pair<K, V>[] makeArrayOfPairs(int arraySize) {
        // It turns out that creating arrays of generic objects in Java
        // is complicated due to something known as 'type erasure'.
        //
        // We've given you this helper method to help simplify this part of
        // your assignment. Use this helper method as appropriate when
        // implementing the rest of this class.
        //
        // You are not required to understand how this method works, what
        // type erasure is, or how arrays and generics interact. Do not
        // modify this method in any way.
        return (Pair<K, V>[]) (new Pair[arraySize]);
    }

    @Override
    public V get(K key) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(pairs[i].key, key)) {
                return pairs[i].value;
            }
        }
        throw new NoSuchKeyException();
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(pairs[i].key, key)) {
                return pairs[i].value;
            }
        }
        return defaultValue;
    }

    @Override
    public void put(K key, V value) {
        if (size == pairs.length) {
            Pair<K, V>[] newPairs = makeArrayOfPairs(pairs.length * 2);
            for (int i = 0; i < pairs.length; i++) {
                newPairs[i] = pairs[i];
            }
            pairs = newPairs;
        }
        boolean foundDuplicate = false;
        for (int i = 0; i < size && !foundDuplicate; i++) {
            if (Objects.equals(pairs[i].key, key)) {
                foundDuplicate = true;
                pairs[i].value = value;
            }
        }
        if (!foundDuplicate) {
            pairs[size] = new Pair<K, V>(key, value);
            size++;
        }
    }

    @Override
    public V remove(K key) {
        V value;
        for (int i = 0; i < size; i++) {
            if (Objects.equals(pairs[i].key, key)) {
                value = pairs[i].value;
                pairs[i] = pairs[size - 1];
                size--;
                return value;
            }
        }
        throw new NoSuchKeyException();
    }

    @Override
    public boolean containsKey(K key) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(pairs[i].key, key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<KVPair<K, V>> iterator() {
        return new ArrayDictionaryIterator<K, V>(pairs, size);
    }


    private static class ArrayDictionaryIterator<K, V> implements Iterator<KVPair<K, V>> {
        private Pair<K, V>[] arr;
        private int size;
        private int curIndex;

        public ArrayDictionaryIterator(Pair<K, V>[] arr, int size) {
            this.arr = arr;
            this.size = size;
            this.curIndex = 0;
        }

        public boolean hasNext() {
            return curIndex < size;
        }

        public KVPair<K, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            KVPair<K, V> ret = new KVPair<K, V>(arr[curIndex].key, arr[curIndex].value);
            curIndex++;
            return ret;
        }
    }

    private static class Pair<K, V> {
        public K key;
        public V value;

        // You may add constructors and methods to this class as necessary.
        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return this.key + "=" + this.value;
        }
    }
}