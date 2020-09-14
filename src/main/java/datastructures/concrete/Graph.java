package datastructures.concrete;

import datastructures.concrete.dictionaries.ChainedHashDictionary;
import datastructures.concrete.dictionaries.KVPair;
import datastructures.interfaces.IDictionary;
import datastructures.interfaces.IDisjointSet;
import datastructures.interfaces.IEdge;
import datastructures.interfaces.IList;
import datastructures.interfaces.IPriorityQueue;
import datastructures.interfaces.ISet;
import misc.Sorter;
import misc.exceptions.NoPathExistsException;

/**
 * Represents an undirected, weighted graph, possibly containing self-loops, parallel edges,
 * and unconnected components.
 * <p>
 * Note: This class is not meant to be a full-featured way of representing a graph.
 * We stick with supporting just a few, core set of operations needed for the
 * remainder of the project.
 */
public class Graph<V, E extends IEdge<V> & Comparable<E>> {
    // NOTE 1:
    //
    // Feel free to add as many fields, private helper methods, and private
    // inner classes as you want.
    //
    // And of course, as always, you may also use any of the data structures
    // and algorithms we've implemented so far.
    //
    // Note: If you plan on adding a new class, please be sure to make it a private
    // static inner class contained within this file. Our testing infrastructure
    // works by copying specific files from your project to ours, and if you
    // add new files, they won't be copied and your code will not compile.
    //
    //
    // NOTE 2:
    //
    // You may notice that the generic types of Graph are a little bit more
    // complicated than usual.
    //
    // This class uses two generic parameters: V and E.
    //
    // - 'V' is the type of the vertices in the graph. The vertices can be
    //   any type the client wants -- there are no restrictions.
    //
    // - 'E' is the type of the edges in the graph. We've constrained Graph
    //   so that E *must* always be an instance of IEdge<V> AND Comparable<E>.
    //
    //   What this means is that if you have an object of type E, you can use
    //   any of the methods from both the IEdge interface and from the Comparable
    //   interface
    //
    // If you have any additional questions about generics, or run into issues while
    // working with them, please ask ASAP either on Piazza or during office hours.
    //
    // Working with generics is really not the focus of this class, so if you
    // get stuck, let us know we'll try and help you get unstuck as best as we can.

    private IDictionary<V, IList<E>> adjList;
    private IList<E> allEdges;

    private static class TableEntry<V, E>
            implements Comparable<TableEntry<V, E>> {
        // Add any fields you think will be useful

        private final V current;
        private final V predecessor;
        private final E edge;
        private final double cost;

        public TableEntry(V current, V predecessor, double cost, E edge) {
            this.current = current;
            this.predecessor = predecessor;
            this.cost = cost;
            this.edge = edge;
        }

        public V getCurrent() {
            return current;
        }

        public V getPredecessor() {
            return predecessor;
        }

        public double getCost() {
            return cost;
        }

        public E getEdge() {
            return edge;
        }

        public int compareTo(TableEntry<V, E> other) {
            // Define compareTo to determine how your vertices will
            // be ordered in the IPriorityQueue
            if (this.cost == other.cost) {
                return 0;
            } else if (this.cost > other.cost) {
                return 1;
            } else {
                return -1;
            }
        }

        public String toString() {
            return "Curr: " + current + " Pred: " + predecessor
                    + " Cost: " + cost + " Prev Edge: " + edge;
        }
    }

    /**
     * Constructs a new graph based on the given vertices and edges.
     * <p>
     * Note that each edge in 'edges' represents a unique edge. For example, if 'edges'
     * contains an entry for '(A,B)' and for '(B,A)', that means there are two parallel
     * edges between vertex 'A' and vertex 'B'.
     *
     * @throws IllegalArgumentException if any edges have a negative weight
     * @throws IllegalArgumentException if any edges connect to a vertex not present in 'vertices'
     * @throws IllegalArgumentException if 'vertices' or 'edges' are null or contain null
     * @throws IllegalArgumentException if 'vertices' contains duplicates
     */
    public Graph(IList<V> vertices, IList<E> edges) {
        adjList = new ChainedHashDictionary<>();
        allEdges = new DoubleLinkedList<>();
        for (V vertex : vertices) {
            if (vertex == null || adjList.containsKey(vertex)) {
                throw new IllegalArgumentException();
            }
            adjList.put(vertex, new DoubleLinkedList<>());
        }
        for (E edge : edges) {
            if (edge == null || edge.getWeight() < 0) {
                throw new IllegalArgumentException();
            }
            V v1 = edge.getVertex1();
            V v2 = edge.getVertex2();
            if (!adjList.containsKey(v1) || !adjList.containsKey(v2)) {
                throw new IllegalArgumentException();
            }
            adjList.get(v1).add(edge);
            if (!v1.equals(v2)) {
                // We only want to add the second time if it's not a self loop.
                adjList.get(v2).add(edge);
            }
            allEdges.add(edge);
        }

        // Sort the edges once and for all.
        allEdges = Sorter.topKSort(allEdges.size(), allEdges);
    }

    /**
     * Sometimes, we store vertices and edges as sets instead of lists, so we
     * provide this extra constructor to make converting between the two more
     * convenient.
     *
     * @throws IllegalArgumentException if any of the edges have a negative weight
     * @throws IllegalArgumentException if one of the edges connects to a vertex not
     *                                  present in the 'vertices' list
     * @throws IllegalArgumentException if vertices or edges are null or contain null
     */
    public Graph(ISet<V> vertices, ISet<E> edges) {
        // You do not need to modify this method.
        this(setToList(vertices), setToList(edges));
    }

    // You shouldn't need to call this helper method -- it only needs to be used
    // in the constructor above.
    private static <T> IList<T> setToList(ISet<T> set) {
        if (set == null) {
            throw new IllegalArgumentException();
        }
        IList<T> output = new DoubleLinkedList<>();
        for (T item : set) {
            output.add(item);
        }
        return output;
    }

    /**
     * Returns the number of vertices contained within this graph.
     */
    public int numVertices() {
        return adjList.size();
    }

    /**
     * Returns the number of edges contained within this graph.
     */
    public int numEdges() {
        return allEdges.size();
    }

    /**
     * Returns the set of all edges that make up the minimum spanning tree of
     * this graph.
     * <p>
     * If there exists multiple valid MSTs, return any one of them.
     * <p>
     * Precondition: the graph does not contain any unconnected components.
     */
    public ISet<E> findMinimumSpanningTree() {
        IDisjointSet<V> disjoint = new ArrayDisjointSet<>();
        ISet<E> result = new ChainedHashSet<>();

        for (KVPair<V, IList<E>> pair : adjList) {
            disjoint.makeSet(pair.getKey());
        }

        for (E edge : allEdges) {
            if (result.size() >= adjList.size() - 1) {
                // Found enough edges.
                break;
            }

            V v1 = edge.getVertex1();
            V v2 = edge.getVertex2();
            if (disjoint.findSet(v1) != disjoint.findSet(v2)) {
                disjoint.union(v1, v2);
                result.add(edge);
            }
        }

        return result;
    }

    /**
     * Returns the edges that make up the shortest path from the start
     * to the end.
     * <p>
     * The first edge in the output list should be the edge leading out
     * of the starting node; the last edge in the output list should be
     * the edge connecting to the end node.
     * <p>
     * Return an empty list if the start and end vertices are the same.
     *
     * @throws NoPathExistsException    if there does not exist a path from the start to the end
     * @throws IllegalArgumentException if start or end is null or not in the graph
     */
    public IList<E> findShortestPathBetween(V start, V end) {
        if (start == null || end == null || !adjList.containsKey(start) || !adjList.containsKey(end)) {
            throw new IllegalArgumentException();
        }

        // What if we are already there?
        if (start.equals(end)) {
            return new DoubleLinkedList<>();
        }

        IPriorityQueue<TableEntry<V, E>> pq = new ArrayHeap<>();
        IDictionary<V, TableEntry<V, E>> table = new ChainedHashDictionary<>();

        for (KVPair<V, IList<E>> pair : adjList) {
            TableEntry<V, E> entry;
            if (pair.getKey().equals(start)) {
                entry = new TableEntry<V, E>(pair.getKey(), null, 0, null);
            } else {
                entry = new TableEntry<V, E>(pair.getKey(), null, Double.POSITIVE_INFINITY, null);
            }
            pq.add(entry);
            table.put(pair.getKey(), entry);
        }

        while (pq.size() > 0) {
            TableEntry<V, E> currEntry = pq.removeMin();
            for (E edge : adjList.get(currEntry.getCurrent())) {
                if (edge.getVertex1().equals(edge.getVertex2())) {
                    // No self loops.
                    continue;
                }

                // Since our graph is undirected, we must figure out which one
                // in our edge is the source and which one is the destination.
                V vertexSrc = currEntry.getCurrent();
                V vertexTo = edge.getOtherVertex(vertexSrc);


                double newCost = currEntry.getCost() + edge.getWeight();

                TableEntry<V, E> oldEntry = table.get(vertexTo);
                if (newCost < oldEntry.getCost()) {
                    TableEntry<V, E> newEntry = new TableEntry<V, E>(vertexTo,
                                                                            vertexSrc,
                                                                            newCost,
                                                                            edge);
                    table.put(vertexTo, newEntry);
                    pq.remove(oldEntry);
                    pq.add(newEntry);
                }
            }
        }

        if (table.get(end).getPredecessor() == null) {
            throw new NoPathExistsException();
        }

        IList<E> result = new DoubleLinkedList<>();
        TableEntry<V, E> curr = table.get(end);
        while (curr.getEdge() != null) {
            result.insert(0, curr.getEdge());
            curr = table.get(curr.getPredecessor());
        }
        return result;
    }
}
