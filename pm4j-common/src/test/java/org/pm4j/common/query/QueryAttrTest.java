package org.pm4j.common.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class QueryAttrTest {

  QueryAttr a1 = new QueryAttr("a1", String.class);
  QueryAttr a1again = new QueryAttr("a1", String.class);
  QueryAttr a2 = new QueryAttr("a2", String.class);

  @Test
  public void testEquals() {
    assertEquals(a1, a1);
    assertEquals(a1, a1again);
    assertFalse(a1.equals(a2));
  }

  @Test
  public void testHashCode() {
    assertEquals(a1.hashCode(), a1.hashCode());
    assertEquals(a1.hashCode(), a1again.hashCode());
    assertTrue(a1.hashCode() != a2.hashCode());
  }

}
