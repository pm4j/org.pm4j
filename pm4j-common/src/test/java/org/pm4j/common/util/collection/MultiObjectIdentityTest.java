package org.pm4j.common.util.collection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MultiObjectIdentityTest {

  private MultiObjectValue m123 = new MultiObjectValue(1, 2, 3);
  private MultiObjectValue m123a = new MultiObjectValue(1, 2, 3);

  @Test
  public void testEquals() {
    assertEquals(m123, m123a);
  }

  @Test
  public void testHashCode() {
    assertEquals(m123.hashCode(), m123a.hashCode());
  }

  @Test
  public void testToString() {
    assertEquals("[1, 2, 3]", m123.toString());
  }

}
