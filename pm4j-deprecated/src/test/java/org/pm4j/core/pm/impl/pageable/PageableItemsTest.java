package org.pm4j.core.pm.impl.pageable;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import org.pm4j.core.pm.pageable.DeprPageableCollectionUtil;
import org.pm4j.core.pm.pageable.DeprPageableListImpl;

public class PageableItemsTest {

  static class MyBean {
    public String name;

    public MyBean(String name) {
      this.name = name;
    }
  }

  private static MyBean[] TEST_ITEM_ARRAY = new MyBean[] {
    new MyBean("a"), new MyBean("b"), new MyBean("c"), new MyBean("d"), new MyBean("e")
  };

  private static List<MyBean> TEST_ITEM_LIST = Arrays.asList(TEST_ITEM_ARRAY);

  @Test
  public void testPageNavigation() {
    DeprPageableListImpl<MyBean> items = new DeprPageableListImpl<MyBean>(TEST_ITEM_LIST);
    items.setPageSize(2);

    assertEquals("5 items should occupy 3 pages.", 3, DeprPageableCollectionUtil.getNumOfPages(items));
    assertEquals("Last item of first page should have the index 2.", 2, DeprPageableCollectionUtil.getIdxOfLastItemOnPage(items));
  }

  @Test
  public void testPageableCollectionWithInitialSortOrder() {
    Comparator<MyBean> reverseOrderComparator = new Comparator<MyBean>() {
      @Override
      public int compare(MyBean o1, MyBean o2) {
        return - o1.name.compareTo(o2.name);
      }
    };
    DeprPageableListImpl<MyBean> items = new DeprPageableListImpl<MyBean>(TEST_ITEM_LIST, reverseOrderComparator);

    assertEquals("e", items.getItemsOnPage().get(0).name);
    assertEquals("d", items.getItemsOnPage().get(1).name);
  }

  @Test
  public void testPageableCollectionWithoutInitialSortOrder() {
    DeprPageableListImpl<MyBean> items = new DeprPageableListImpl<MyBean>(TEST_ITEM_LIST);

    assertEquals("a", items.getItemsOnPage().get(0).name);
    assertEquals("b", items.getItemsOnPage().get(1).name);
  }

}
