package misc;

import datastructures.concrete.ArrayHeap;
import datastructures.concrete.DoubleLinkedList;
import datastructures.interfaces.IList;
import datastructures.interfaces.IPriorityQueue;

public class Sorter {
    /**
     * This method takes the input list and returns the top k elements
     * in sorted order.
     *
     * So, the first element in the output list should be the "smallest"
     * element; the last element should be the "largest".
     *
     * If the input list contains fewer than 'k' elements, return
     * a list containing all input.length elements in sorted order.
     *
     * This method must not modify the input list.
     *
     * @throws IllegalArgumentException  if k < 0
     * @throws IllegalArgumentException  if input is null
     */
    public static <T extends Comparable<T>> IList<T> topKSort(int k, IList<T> input) {
        // Implementation notes:
        //
        // - This static method is a _generic method_. A generic method is similar to
        //   the generic methods we covered in class, except that the generic parameter
        //   is used only within this method.
        //
        //   You can implement a generic method in basically the same way you implement
        //   generic classes: just use the 'T' generic type as if it were a regular type.
        //
        // - You should implement this method by using your ArrayHeap for the sake of
        //   efficiency.

        // Exception party!
        if (k < 0) {
            throw new IllegalArgumentException();
        }
        if (input == null) {
            throw new IllegalArgumentException();
        }

        // Ensure we don't overrun the number of elements that we need..
        if (k > input.size()) {
            k = input.size();
        }

        IList<T> ret = new DoubleLinkedList<>();
        // Evict the client if they only want 0 elements (why?)
        if (k == 0) {
            return ret;
        }

        // Insert stuff into the heap.
        // BUT BUT BUT: we only want to keep k elements in the heap, each of
        // them being the known highest elements.
        IPriorityQueue<T> heap = new ArrayHeap<>();
        for (T item : input) {
            // Start removing stuff if we already have k elements.
            if (heap.size() != 0 && heap.size() >= k) {
                T minimum = heap.peekMin();

                // Runtime saving considerations:
                // We want to make sure our current item is not lesser than the
                // minimum item already in the heap. We only want to insert
                // if the current item is larger than the minimum item.
                if (item.compareTo(minimum) >= 0) {
                    heap.add(item);
                    heap.removeMin();
                }
            } else {
                heap.add(item);
            }
        }

        // Build the output.
        for (int i = 0; i < k; i++) {
            ret.add(heap.removeMin());
        }
        return ret;
    }
}