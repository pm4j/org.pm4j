package org.pm4j.common.pageable;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.common.pageable.PageableCollectionTestBase.Bean;
import org.pm4j.common.pageable.inmem.PageableInMemCollectionImpl;

public class PageableCollectionWithAdditionalItemsTest {

  private PageableCollectionWithAdditionalItems<Bean> pc;

  @Before
  public void setUp() {
    PageableCollection2<Bean> baseCollection = new PageableInMemCollectionImpl<Bean>(PageableCollectionTestBase.makeBeans("a", "b", "c"));
    pc = new PageableCollectionWithAdditionalItems<Bean>(baseCollection);
    pc.setPageSize(2);
  }

  @Test
  public void testCollectionWithoutAdditionalItems() {
    assertEquals(3L, pc.getNumOfItems());
    assertEquals(2, PageableCollectionUtil2.getNumOfPages(pc));
    assertEquals("[a, b]", pc.getItemsOnPage().toString());
  }

  @Test
  public void testCollectionWithAdditionalItems() {
    pc.getModificationHandler().addItem(new Bean("d"));
    assertEquals(4L, pc.getNumOfItems());
    assertEquals(2, PageableCollectionUtil2.getNumOfPages(pc));
    assertEquals("[a, b]", pc.getItemsOnPage().toString());

    pc.getModificationHandler().addItem(new Bean("e"));
    assertEquals(5L, pc.getNumOfItems());
    assertEquals(3, PageableCollectionUtil2.getNumOfPages(pc));
    assertEquals("[a, b]", pc.getItemsOnPage().toString());
    assertEquals(0, pc.getPageIdx());

    pc.setPageIdx(1);
    assertEquals("[c, d]", pc.getItemsOnPage().toString());

    pc.setPageIdx(2);
    assertEquals("[e]", pc.getItemsOnPage().toString());

    pc.getModificationHandler().addItem(new Bean("f"));
    assertEquals("[e, f]", pc.getItemsOnPage().toString());
  }


}
