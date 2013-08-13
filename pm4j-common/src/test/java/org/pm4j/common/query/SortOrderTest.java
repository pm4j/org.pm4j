package org.pm4j.common.query;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SortOrderTest {

  private final QueryAttr qa1 = new QueryAttr("qa1", String.class);
  private final QueryAttr qa2 = new QueryAttr("qa2", String.class);
  private final QueryAttr qa3 = new QueryAttr("qa3", String.class);

  @Test
  public void testCompareSameAttributeSet() {
    SortOrder so1 = new SortOrder(qa1, qa2, qa3);
    SortOrder so2 = new SortOrder(qa1, qa2, qa3);
    assertEquals(true, SortOrder.bothOrdersUseTheSameAttributeSet(so1, so2));
  }

  @Test
  public void testCompareSameAttributeSetButWithReverseSortOrder() {
    SortOrder so1 = new SortOrder(qa1, qa2, qa3);
    SortOrder so2 = new SortOrder(qa1, qa2, qa3).getReverseSortOrder();
    assertEquals(true, SortOrder.bothOrdersUseTheSameAttributeSet(so1, so2));
  }

  @Test
  public void testCompareDifferentAttributeSets() {
    SortOrder so1 = new SortOrder(qa1, qa2, qa3);
    SortOrder so2 = new SortOrder(qa1, qa2);
    assertEquals(false, SortOrder.bothOrdersUseTheSameAttributeSet(so1, so2));
  }

}
