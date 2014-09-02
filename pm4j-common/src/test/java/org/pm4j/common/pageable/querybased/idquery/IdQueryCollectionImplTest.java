package org.pm4j.common.pageable.querybased.idquery;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.PageableCollectionTestBase;

public class IdQueryCollectionImplTest extends PageableCollectionTestBase<PageableCollectionTestBase.Bean> {

  TestService service = new TestService();

  @Override
  protected PageableCollection<Bean> makePageableCollection(String... strings) {
    service.deleteAll();
    if (strings != null) {
      for (String s : strings) {
        service.save(new Bean(s));
      }
    }
    return new IdQueryCollectionImpl<Bean, Integer>(service, getQueryOptions());
  }

  @Override
  protected Bean createItem(int id, String name) {
    return new Bean(id, name);
  }

  @Override
  public void setUp() {
    super.setUp();
    assertEquals("Call count stability check.", "{findIds=1, getItemForId=8, getItems=4}", service.callCounter.toString());
    service.callCounter.reset();
  }

  @Test
  public void testMethodCallCount() {
    collection.getItemsOnPage();
    assertEquals("One call to get all ids and one to get the page items.", "{findIds=1, getItems=1}", service.callCounter.toString());

    service.callCounter.reset();
    collection.getItemsOnPage();
    assertEquals("No additional call on re-getting the current page.", "{}", service.callCounter.toString());

    service.callCounter.reset();
    collection.setPageIdx(1l);
    collection.getItemsOnPage();
    assertEquals("One call to get the items of the next page.", "{getItems=1}", service.callCounter.toString());
  }

  @Test @Override
  public void testItemNavigator() {
    super.testItemNavigator();
    assertEquals("Call count stability check.", "{findIds=1, getItems=4}", service.callCounter.toString());
  }

  @Test @Override
  public void testSwitchQueryExecOffAndOn() {
    super.testSwitchQueryExecOffAndOn();
    assertEquals("Call count stability check.", "{findIds=2, getItemForId=12}", service.callCounter.toString());
  }

  @Test @Override
  public void testItemsOnPage() {
    super.testItemsOnPage();
    assertEquals("Call count stability check.", "{findIds=1, getItems=4}", service.callCounter.toString());
  }

  @Override @Test
  public void testSortItems() {
    super.testSortItems();
    assertEquals("Call count stability check.", "{findIds=4, getItemForId=24}", service.callCounter.toString());
  }

  @Override @Test
  public void testDefaultSortOrder() {
    super.testDefaultSortOrder();
    assertEquals("Call count stability check.", "{findIds=5, getItemForId=30, getItems=5}", service.callCounter.toString());
  }

  @Override @Test
  public void testFilterItems() {
    super.testFilterItems();
    assertEquals("Call count stability check.", "{findIds=2, getItemForId=7}", service.callCounter.toString());
  }

  @Override @Test
  public void testSelectItems() {
    super.testSelectItems();
    assertEquals("Call count stability check.", "{findIds=1, getItems=1}", service.callCounter.toString());
  }

  @Override @Test
  public void testSelectInvertAndDeselect() {
    super.testSelectInvertAndDeselect();
    assertEquals("Call count stability check.", "{findIds=1, getItems=1}", service.callCounter.toString());
  }

  @Override @Test
  public void testAddItem() {
    super.testAddItem();
    assertEquals("Call count stability check.", "{findIds=2, getItemForId=6, getItems=6}", service.callCounter.toString());
  }

  @Override @Test
  public void testAddItemToEmptyCollection() {
    super.testAddItemToEmptyCollection();
    assertEquals("Call count stability check.", "{findIds=1}", service.callCounter.toString());
  }

  @Override @Test
  public void testAddItemInMultiSelectMode() {
    super.testAddItemInMultiSelectMode();
    assertEquals("Call count stability check.", "{findIds=2, getItemForId=6, getItems=6}", service.callCounter.toString());
  }

  @Override @Test
  public void testRemoveItems() {
    super.testRemoveItems();
    assertEquals("Call count stability check.", "{findIds=2, getItemForId=4, getItems=3}", service.callCounter.toString());
  }

  @Override @Test
  public void testRemoveOfAddedAndUpdatedItems() {
    super.testRemoveOfAddedAndUpdatedItems();
    assertEquals("Call count stability check.", "{findIds=2, getItems=3}", service.callCounter.toString());
  }

  @Override @Test
  public void testIterateAllSelectionWithBlockSize3() {
    super.testIterateAllSelectionWithBlockSize3();
    assertEquals("Call count stability check.", "{findIds=1, getItems=2}", service.callCounter.toString());
  }

  @Override @Test
  public void testIterateAllSelectionWithBlockSize6() {
    super.testIterateAllSelectionWithBlockSize6();
    assertEquals("Call count stability check.", "{findIds=1, getItems=1}", service.callCounter.toString());
  }

  @Override @Test
  public void testIteratePositiveSelectionOf3ItemsWithBlockSize2() {
    super.testIteratePositiveSelectionOf3ItemsWithBlockSize2();
    assertEquals("Call count stability check.", "{getItems=2}", service.callCounter.toString());
  }

  @Override @Test
  public void testIterateAllSelectionMinusOneWithBlockSize2() {
    super.testIterateAllSelectionMinusOneWithBlockSize2();
    assertEquals("Call count stability check.", "{getItems=3}", service.callCounter.toString());
  }

  @Override @Test
  public void testIterateEmptySelection() {
    super.testIterateEmptySelection();
    assertEquals("Call count stability check.", "{}", service.callCounter.toString());
  }

  @Override
  protected void resetCallCounter() {
    service.callCounter.reset();
  }

  // --- A fake service implementation that does the job just in memory. ---

  static class TestService extends IdQueryServiceFake.WithIntegerId<Bean>{}
}
