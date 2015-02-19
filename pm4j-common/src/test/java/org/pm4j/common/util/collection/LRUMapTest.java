package org.pm4j.common.util.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.TreeMap;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class LRUMapTest {

    @Test
    public void testDefaultMaxSize() {
        // The default size of the LRUMap is 1000 elements
        // This tests puts 1001 elements into the map and checks
        // that afterwards only 1000 elements are contained.
        LRUMap<Integer, String> lruMap = new LRUMap<Integer, String>();
        for (Integer i = 0; i < 1001; i++) {
            lruMap.put(i, String.valueOf(i));
        }
        assertEquals(1000, lruMap.size());
    }

    @Test
    public void testLRULogicWithoutReadAccess() {
        // Construct a LRUMap with maxSize=3 and put 4 elements into the LRUMap.
        // Check that the element that has been put into the map first has been removed.
        LRUMap<Integer, String> lruMap = new LRUMap<Integer, String>(3);
        lruMap.put(0, "0");
        lruMap.put(1, "1");
        lruMap.put(2, "2");
        lruMap.put(3, "3");
        // check results
        assertEquals(3, lruMap.size());
        assertFalse(lruMap.containsKey(0));
        assertEquals("1", lruMap.get(1));
        assertEquals("2", lruMap.get(2));
        assertEquals("3", lruMap.get(3));
    }

    @Test
    public void testLRULogicWithReadAccess() {
        // Construct a LRUMap with maxSize=3, populate it with 3 items and read the first (with
        // index 0).
        LRUMap<Integer, String> lruMap = new LRUMap<Integer, String>(3);
        lruMap.put(0, "0");
        lruMap.put(1, "1");
        lruMap.put(2, "2");
        lruMap.get(0);
        // Now add another item into the LRUMap.
        // According to the LRU strategy the item with index 1 should have been swapped out now.
        lruMap.put(3, "3");
        // check results
        assertEquals(3, lruMap.size());
        assertEquals("0", lruMap.get(0));
        assertFalse(lruMap.containsKey(1));
        assertEquals("2", lruMap.get(2));
        assertEquals("3", lruMap.get(3));
    }

    @Test
    public void testPutAll() {
        // Construct a LRUMap with maxSize=3 and populate it with a TreeMap of size=4.
        // According to the LRU strategy the LRUMap must contain 3 elements and the element
        // that has been put first into the TreeMap should not be contained.
        TreeMap<Integer, String> treeMap = new TreeMap<Integer, String>();
        LRUMap<Integer, String> lruMap = new LRUMap<Integer, String>(3);
        treeMap.put(0, "0");
        treeMap.put(1, "1");
        treeMap.put(2, "2");
        treeMap.put(3, "3");
        lruMap.putAll(treeMap);
        // check results
        assertEquals(3, lruMap.size());
        assertFalse(lruMap.containsKey(0));
        assertEquals("1", lruMap.get(1));
        assertEquals("2", lruMap.get(2));
        assertEquals("3", lruMap.get(3));
    }

}
