package datastructures.concrete.dictionaries;

import datastructures.interfaces.IDictionary;
import misc.exceptions.NoSuchKeyException;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @see IDictionary and the assignment page for more details on what each method should do
 */
public class ChainedHashDictionary<K, V> implements IDictionary<K, V> {
    private final double lambda;

    // You MUST use this field to store the contents of your dictionary.
    // You may not change or rename this field: we will be inspecting
    // it using our private tests.
    private IDictionary<K, V>[] chains;

    // You're encouraged to add extra fields (and helper methods) though!
    private int size;

    public static final int DEFAULT_CAP = 100;

    public ChainedHashDictionary() {
        this(3.0);
    }

    public ChainedHashDictionary(double lambda) {
        this(lambda, DEFAULT_CAP);
    }

    private ChainedHashDictionary(double lambda, int cap) {
        this.chains = makeArrayOfChains(cap);
        this.size = 0;
        this.lambda = lambda;
    }

    /**
     * This method will return a new, empty array of the given size
     * that can contain IDictionary<K, V> objects.
     * <p>
     * Note that each element in the array will initially be null.
     */
    @SuppressWarnings("unchecked")
    private IDictionary<K, V>[] makeArrayOfChains(int arraySize) {
        // Note: You do not need to modify this method.
        // See ArrayDictionary's makeArrayOfPairs(...) method for
        // more background on why we need this method.
        return (IDictionary<K, V>[]) new IDictionary[arraySize];
    }

    @Override
    public V get(K key) {
        int index = indexHelper(key, chains.length);
        if (chains[index] != null) {
            return chains[index].get(key);
        }

        throw new NoSuchKeyException(); // Catchall throw;
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        int index = indexHelper(key, chains.length);
        if (chains[index] != null) {
            return chains[index].getOrDefault(key, defaultValue);
        }

        return defaultValue;
    }

    @Override
    public void put(K key, V value) {
        // Resize if needed.
        if (1.0 * size / chains.length >= lambda) {
            doResize();
        }

        int index = indexHelper(key, chains.length);
        if (chains[index] == null) {
            chains[index] = new ArrayDictionary<>();
        }

        int prevSize = chains[index].size();
        chains[index].put(key, value);
        if (chains[index].size() > prevSize) {
            size++;
        }
    }

    @Override
    public V remove(K key) {
        int index = indexHelper(key, chains.length);
        if (chains[index] != null) {
            V value = chains[index].remove(key);
            size--; // If an exception is thrown, we won't get here.
            return value;
        }

        throw new NoSuchKeyException();
    }

    @Override
    public boolean containsKey(K key) {
        int index = indexHelper(key, chains.length);
        if (chains[index] == null) {
            return false;
        }
        return chains[index].containsKey(key);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<KVPair<K, V>> iterator() {
        // Note: you do not need to change this method
        return new ChainedIterator<>(this.chains, this.size);
    }

    /**
     * Hints:
     * <p>
     * 1. You should add extra fields to keep track of your iteration
     * state. You can add as many fields as you want. If it helps,
     * our reference implementation uses three (including the one we
     * gave you).
     * <p>
     * 2. Before you try and write code, try designing an algorithm
     * using pencil and paper and run through a few examples by hand.
     * <p>
     * We STRONGLY recommend you spend some time doing this before
     * coding. Getting the invariants correct can be tricky, and
     * running through your proposed algorithm using pencil and
     * paper is a good way of helping you iron them out.
     * <p>
     * 3. Think about what exactly your *invariants* are. As a
     * reminder, an *invariant* is something that must *always* be
     * true once the constructor is done setting up the class AND
     * must *always* be true both before and after you call any
     * method in your class.
     * <p>
     * Once you've decided, write them down in a comment somewhere to
     * help you remember.
     * <p>
     * You may also find it useful to write a helper method that checks
     * your invariants and throws an exception if they're violated.
     * You can then call this helper method at the start and end of each
     * method if you're running into issues while debugging.
     * <p>
     * (Be sure to delete this method once your iterator is fully working.)
     * <p>
     * Implementation restrictions:
     * <p>
     * 1. You **MAY NOT** create any new data structures. Iterators
     * are meant to be lightweight and so should not be copying
     * the data contained in your dictionary to some other data
     * structure.
     * <p>
     * 2. You **MAY** call the `.iterator()` method on each IDictionary
     * instance inside your 'chains' array, however.
     */
    private static class ChainedIterator<K, V> implements Iterator<KVPair<K, V>> {
        private IDictionary<K, V>[] chains;
        private int size; // Actual number of elements.
        private int currIndex; // Current number of elements iterated.
        private int arrIndex; // Current index of ArrayDictionary in use.
        private Iterator<KVPair<K, V>> curIt; // Current iterator in use.


        // Invariants / Assumptions:
        // 0. Basically - no write changes.
        // 1. The size (# elements) remains the same throughout.
        // 2. The location of the elements remain the same throughout.
        public ChainedIterator(IDictionary<K, V>[] chains, int size) {
            this.chains = chains;
            this.size = size;
            this.currIndex = 0;
            this.arrIndex = -1;
        }

        @Override
        public boolean hasNext() {
            return currIndex < size;
        }

        @Override
        public KVPair<K, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            // If the current iterator is null / has run out, look for the next
            // iterator. We are making the assumption that this can be found,
            // as we already checked if there are more elements to iterate through.
            while (curIt == null || !curIt.hasNext()) {
                arrIndex++;
                IDictionary<K, V> dictator = chains[arrIndex];
                if (dictator != null) {
                    curIt = dictator.iterator();
                }
            }

            // After finding the iterator, have the dictionary return it.
            currIndex++;
            return curIt.next();
        }
    }

    private int indexHelper(K key, int cap) {
        if (key != null) {
            int hash = Math.abs(key.hashCode());
            return hash % cap;
        } else {
            return 0;
        }
    }

    private void doResize() {
        IDictionary<K, V>[] newArr = makeArrayOfChains(chains.length * 2);

        for (KVPair<K, V> pair : this) {
            int index = indexHelper(pair.getKey(), newArr.length);
            if (newArr[index] == null) {
                newArr[index] = new ArrayDictionary<>();
            }
            newArr[index].put(pair.getKey(), pair.getValue());
        }

        chains = newArr;
    }
}