package datastructures;

import datastructures.concrete.ArrayDisjointSet;
import datastructures.interfaces.IDisjointSet;
import misc.BaseTest;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestArrayDisjointSet extends BaseTest {
    private <T> IDisjointSet<T> createForest(T[] items) {
        IDisjointSet<T> forest = new ArrayDisjointSet<>();
        for (T item : items) {
            forest.makeSet(item);
        }
        return forest;
    }

    private <T> void check(IDisjointSet<T> forest, T[] items, int[] expectedIds) {
        for (int i = 0; i < items.length; i++) {
            assertEquals(expectedIds[i], forest.findSet(items[i]));
        }
    }

    @Test(timeout = SECOND)
    public void testMakeSetAndFindSetSimple() {
        String[] items = new String[]{"a", "b", "c", "d", "e"};
        IDisjointSet<String> forest = this.createForest(items);

        for (int i = 0; i < 5; i++) {
            check(forest, items, new int[]{0, 1, 2, 3, 4});
        }
    }

    @Test(timeout = SECOND)
    public void testCustomMakeWithCustomType() {
        Double[] items = new Double[7];
        for (int i = 0; i < 7; i++) {
            items[i] = i + 0.0;
        }
        IDisjointSet<Double> forest = this.createForest(items);

        for (int i = 0; i < 7; i++) {
            check(forest, items, new int[]{0, 1, 2, 3, 4, 5, 6});
        }
    }

    @Test(timeout = SECOND)
    public void testUnionSimple() {
        String[] items = new String[]{"a", "b", "c", "d", "e"};
        IDisjointSet<String> forest = this.createForest(items);

        forest.union("a", "b");
        int id1 = forest.findSet("a");
        assertTrue(id1 == 0 || id1 == 1);
        assertEquals(id1, forest.findSet("b"));

        forest.union("c", "d");
        int id2 = forest.findSet("c");
        assertTrue(id2 == 2 || id2 == 3);
        assertEquals(id2, forest.findSet("d"));

        assertEquals(4, forest.findSet("e"));
    }

    @Test(timeout = SECOND)
    public void testUnionUnequalTrees() {
        String[] items = new String[]{"a", "b", "c", "d", "e"};
        IDisjointSet<String> forest = this.createForest(items);

        forest.union("a", "b");
        int id = forest.findSet("a");

        forest.union("a", "c");

        for (int i = 0; i < 5; i++) {
            check(forest, items, new int[]{id, id, id, 3, 4});
        }
    }

    @Test(timeout = SECOND)
    public void testIllegalFindSet() {
        String[] items = new String[]{"a", "b", "c", "d", "e"};
        IDisjointSet<String> forest = this.createForest(items);

        try {
            forest.findSet("f");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // All ok -- expected result
        }
    }

    @Test(timeout = SECOND)
    public void testIllegalUnionThrowsException() {
        String[] items = new String[]{"a", "b", "c", "d", "e"};
        IDisjointSet<String> forest = this.createForest(items);

        try {
            forest.union("a", "f");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // All ok -- expected result
        }
    }

    @Test(timeout = 4 * SECOND)
    public void testLargeForest() {
        IDisjointSet<Integer> forest = new ArrayDisjointSet<>();
        forest.makeSet(0);

        int numItems = 5000;
        for (int i = 1; i < numItems; i++) {
            forest.makeSet(i);
            forest.union(0, i);
        }

        int cap = 6000;
        int id = forest.findSet(0);
        for (int i = 0; i < cap; i++) {
            for (int j = 0; j < numItems; j++) {
                assertEquals(id, forest.findSet(j));
            }
        }
    }

    @Test(timeout = SECOND)
    public void testCustomNullKey() {
        IDisjointSet<String> forest = new ArrayDisjointSet<>();
        forest.makeSet(null);
        forest.makeSet("Smashing Pumpkins");
        forest.makeSet("Green Day");

        assertEquals(0, forest.findSet(null));
        assertEquals(2, forest.findSet("Green Day"));
        forest.union(null, "Green Day");
        assertEquals(0, forest.findSet("Green Day"));

        try {
            forest.makeSet(null);
            fail();
        } catch (IllegalArgumentException e) {
            // okay!
        }
    }

    @Test(timeout = SECOND)
    public void testCustomSameRankTieBreak() {
        String[] items = new String[]{"a", "b"};
        IDisjointSet<String> forest1 = this.createForest(items);
        IDisjointSet<String> forest2 = this.createForest(items);
        forest1.union("a", "b");
        forest2.union("b", "a");
        assertEquals(0, forest1.findSet("a"));
        assertEquals(0, forest1.findSet("b"));
        assertEquals(1, forest2.findSet("a"));
        assertEquals(1, forest2.findSet("b"));
    }

    @Test(timeout = SECOND)
    public void testCustomUnionByRankSameRankComprehensive() {
        String[] items = new String[]{"a", "b", "c", "d", "e", "f", "g"};
        IDisjointSet<String> forest = this.createForest(items);

        forest.union("a", "b");
        forest.union("c", "d");
        forest.union("e", "f");

        assertEquals(forest.findSet("a"), 0);
        assertEquals(forest.findSet("b"), 0);
        assertEquals(forest.findSet("c"), 2);
        assertEquals(forest.findSet("d"), 2);
        assertEquals(forest.findSet("e"), 4);
        assertEquals(forest.findSet("f"), 4);
        assertEquals(forest.findSet("g"), 6);

        // Might as well as just union the rest.
        forest.union("a", "c");
        assertEquals(forest.findSet("c"), 0);
        assertEquals(forest.findSet("d"), 0);
        forest.union("f", "c");
        assertEquals(forest.findSet("e"), 0);
        assertEquals(forest.findSet("f"), 0);
        forest.union("g", "f");
        assertEquals(forest.findSet("g"), 0);
    }

    @Test(timeout = SECOND)
    public void testCustomUnionByRankUnequal() {
        String[] items = new String[]{"a", "b", "c", "d", "e", "f", "g"};
        IDisjointSet<String> forest = this.createForest(items);

        for (int i = 1; i < items.length - 1; i++) {
            forest.union(items[i], items[i + 1]);
        }
        for (int i = 1; i < items.length; i++) {
            assertEquals(1, forest.findSet(items[i]));
        }

        assertEquals(0, forest.findSet("a"));
        forest.union("a", "f");
        assertEquals(1, forest.findSet("a"));
    }
}
