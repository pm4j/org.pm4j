package org.pm4j.common.itemnavigator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ItemNavigatorUtilTest {

  private ItemNavigator<String> navigator = new ListItemNavigator<String>("Hello", "World");

  @Test
  public void testInitialState() {
    assertEquals(true, ItemNavigatorUtil.hasNext(navigator));
    assertEquals(false, ItemNavigatorUtil.hasPrev(navigator));
  }

  @Test
  public void testToNextAndToPrev() {
    assertEquals("World", ItemNavigatorUtil.toNext(navigator));
    assertEquals(false, ItemNavigatorUtil.hasNext(navigator));
    assertEquals(true, ItemNavigatorUtil.hasPrev(navigator));
    assertEquals("Hello", ItemNavigatorUtil.toPrev(navigator));
    assertEquals(true, ItemNavigatorUtil.hasNext(navigator));
    assertEquals(false, ItemNavigatorUtil.hasPrev(navigator));
  }

  @Test
  public void testToLastAndToFirst() {
    assertEquals("World", ItemNavigatorUtil.toLast(navigator));
    assertEquals(false, ItemNavigatorUtil.hasNext(navigator));
    assertEquals(true, ItemNavigatorUtil.hasPrev(navigator));
    assertEquals("Hello", ItemNavigatorUtil.toFirst(navigator));
    assertEquals(true, ItemNavigatorUtil.hasNext(navigator));
    assertEquals(false, ItemNavigatorUtil.hasPrev(navigator));
  }

}
