package org.pm4j.common.itemnavigator;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.ObjectUtils;
import org.junit.Test;

public class ConvertingItemNavigatorTest {

  private ItemNavigator<Integer> intNavigator = new ListItemNavigator<Integer>(1, 2, 11, 22, 111, 222);
  private ItemNavigator<String> convertingNavigator = new ConvertingItemNavigator<String, Integer>(intNavigator,
      new ConvertingItemNavigator.Converter<String, Integer>() {
        @Override
        public String toNavigatorItemType(Integer selectedItem) {
          ++toNavigatorItemTypeCallCount;
          return ObjectUtils.toString(selectedItem);
        }
      });
  private int toNavigatorItemTypeCallCount = 0;

  @Test
  public void testGetCurrentItem() {
    assertEquals("1", convertingNavigator.getCurrentItem());
    assertEquals(Integer.valueOf(1), intNavigator.getCurrentItem());
    assertEquals(1, toNavigatorItemTypeCallCount);
  }

  @Test
  public void testNavigateTo() {
    assertEquals("22", convertingNavigator.navigateTo(3));
    assertEquals(Integer.valueOf(22), intNavigator.getCurrentItem());
    assertEquals(1, toNavigatorItemTypeCallCount);
  }

  @Test
  public void testGetNumOfItems() {
    assertEquals(6, convertingNavigator.getNumOfItems());
    assertEquals(0, toNavigatorItemTypeCallCount);
  }

  @Test
  public void testGetCurrentItemIdx() {
    assertEquals(0, convertingNavigator.getCurrentItemIdx());
    assertEquals(0, toNavigatorItemTypeCallCount);
  }

  @Test
  public void testGetCurrentItemCalledTwiceCallsConverterMethodOnlyOnce() {
    assertEquals("1", convertingNavigator.getCurrentItem());
    assertEquals("1", convertingNavigator.getCurrentItem());
    assertEquals(1, toNavigatorItemTypeCallCount);
  }

  @Test
  public void testGetCurrentItemCalledTwiceWithClearCachesCallsConverterMethodTwice() {
    assertEquals("1", convertingNavigator.getCurrentItem());
    convertingNavigator.clearCaches();
    assertEquals("1", convertingNavigator.getCurrentItem());
    assertEquals(2, toNavigatorItemTypeCallCount);
  }

  @Test
  public void testNavigateToSamePositionTwiceCallsConverterMethodOnlyOnce() {
    assertEquals("22", convertingNavigator.navigateTo(3));
    assertEquals("22", convertingNavigator.navigateTo(3));
    assertEquals(Integer.valueOf(22), intNavigator.getCurrentItem());
    assertEquals(1, toNavigatorItemTypeCallCount);
  }
}
