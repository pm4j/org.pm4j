package org.pm4j.common.util.collection;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;


public class IterableUtilTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testJoinNoIterables() {
    assertEquals("[]", IterableUtil.asCollection(IterableUtil.join()).toString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testJoinSingleIterable() {
    assertEquals("[A, B]", IterableUtil.asCollection(IterableUtil.join(Arrays.asList("A", "B"))).toString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testJoinTwoIterables() {
    Iterable<String> i = IterableUtil.join(
        Arrays.asList("A", "B"),
        Arrays.asList("C", "D")
    );
    assertEquals("[A, B, C, D]", IterableUtil.asCollection(i).toString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testJoinTwoIterablesWithGaps() {
    Iterable<String> i = IterableUtil.join(
        null,
        Arrays.asList("A", "B"),
        null,
        Arrays.asList("C", "D"),
        null
    );
    assertEquals("[A, B, C, D]", IterableUtil.asCollection(i).toString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testJoinTwoIterablesWithGapsAndEmptyIterables() {
    Iterable<String> i = IterableUtil.join(
        Collections.EMPTY_LIST, null,
        Arrays.asList("A", "B"),
        Collections.EMPTY_LIST, null, Collections.EMPTY_LIST,
        Arrays.asList("C", "D"),
        null, Collections.EMPTY_LIST
    );
    assertEquals("[A, B, C, D]", IterableUtil.asCollection(i).toString());
  }

}
