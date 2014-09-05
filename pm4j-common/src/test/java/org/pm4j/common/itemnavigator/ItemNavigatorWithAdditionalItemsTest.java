package org.pm4j.common.itemnavigator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class ItemNavigatorWithAdditionalItemsTest {

  private ItemNavigator<String> baseNavigator = new ListItemNavigator<String>("foo", "bar") {
    public void clearCaches() {
      super.clearCaches();
      clearCachesOnBaseNavigatorCalled = true;
    };
  };
  private ItemNavigatorWithAdditionalItems<String> navigator = new ItemNavigatorWithAdditionalItems<String>(baseNavigator, "baz");
  private boolean clearCachesOnBaseNavigatorCalled;

  @Test
  public void testGetCurrentItem() {
    assertEquals("foo", navigator.getCurrentItem());
  }

  @Test
  public void testNavigateTo() {
    assertEquals("baz", navigator.navigateTo(2));
    assertEquals("foo", navigator.navigateTo(0));
    assertEquals("bar", navigator.navigateTo(1));
  }

  @Test
  public void testGetNumOfItems() {
    assertEquals(3, navigator.getNumOfItems());
  }

  @Test
  public void testGetCurrentItemIdx() {
    assertEquals(0, navigator.getCurrentItemIdx());
  }

  @Test
  public void testAddItem() {
    assertEquals(3, navigator.addAdditionalItem("."));
    assertEquals("[baz, .]", navigator.getAdditionalItems().toString());
  }

  @Test
  public void testGetBaseNavigator() {
    assertSame(baseNavigator, navigator.getBaseNavigator());
  }

  @Test
  public void testClearCaches() {
    navigator.clearCaches();
    assertEquals(true, clearCachesOnBaseNavigatorCalled);
  }

}
