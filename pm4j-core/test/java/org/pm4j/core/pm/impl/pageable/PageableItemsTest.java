package org.pm4j.core.pm.impl.pageable;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.pm4j.core.pm.pageable.PageableCollectionUtil;
import org.pm4j.core.pm.pageable.PageableListImpl;

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
    PageableListImpl<MyBean> items = new PageableListImpl<MyBean>(TEST_ITEM_LIST);
    items.setPageSize(2);

    assertEquals("5 items should occupy 3 pages.", 3, PageableCollectionUtil.getNumOfPages(items));
    assertEquals("Last item of first page should have the index 2.", 2, PageableCollectionUtil.getIdxOfLastItemOnPage(items));
  }
}
