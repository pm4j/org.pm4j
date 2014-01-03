package org.pm4j.deprecated.core.pm.impl.pageable;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.pm4j.deprecated.core.pm.pageable.DeprPageableCollectionUtil;
import org.pm4j.deprecated.core.pm.pageable.DeprPageableListImpl;

public class PageableItemsListImplTest {

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


  static class MyBean {
    public String name;

    public MyBean(String name) {
      this.name = name;
    }
  }

}
