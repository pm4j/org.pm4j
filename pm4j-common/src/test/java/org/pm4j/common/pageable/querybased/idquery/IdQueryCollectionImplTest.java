package org.pm4j.common.pageable.querybased.idquery;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.PageableCollectionTestBase;

public class IdQueryCollectionImplTest extends PageableCollectionTestBase<PageableCollectionTestBase.Bean> {

  TestService service = new TestService();

  @Override
  protected PageableCollection<Bean> makePageableCollection(String... strings) {
    int counter = 1;
    service.removeAllFakeItems();
    if (strings != null) {
      for (String s : strings) {
        service.addFakeItem(new Bean(++counter, s));
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
    assertEquals("Call count stability check.", "{findIds=1, getItemForId=11, getItems=1}", service.callCounter.toString());
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
    assertEquals("Call count stability check.", "{findIds=1, getItemForId=9, getItems=1}", service.callCounter.toString());
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

  @Override
  public void testSortItems() {
    super.testSortItems();
    assertEquals("Call count stability check.", "{findIds=4, getItemForId=24}", service.callCounter.toString());
  }

  @Override
  public void testDefaultSortOrder() {
    super.testDefaultSortOrder();
    assertEquals("Call count stability check.", "{findIds=5, getItemForId=30, getItems=5}", service.callCounter.toString());
  }

  @Override
  public void testFilterItems() {
    super.testFilterItems();
    assertEquals("Call count stability check.", "{findIds=2, getItemForId=7}", service.callCounter.toString());
  }

  @Override
  public void testSelectItems() {
    super.testSelectItems();
    assertEquals("Call count stability check.", "{findIds=1, getItems=1}", service.callCounter.toString());
  }

  @Override
  public void testSelectInvertAndDeselect() {
    super.testSelectInvertAndDeselect();
    assertEquals("Call count stability check.", "{findIds=1, getItems=1}", service.callCounter.toString());
  }

  @Override
  public void testAddItem() {
    super.testAddItem();
    assertEquals("Call count stability check.", "{findIds=2, getItemForId=8, getItems=4}", service.callCounter.toString());
  }

  @Override
  public void testAddItemToEmptyCollection() {
    super.testAddItemToEmptyCollection();
    assertEquals("Call count stability check.", "{findIds=1}", service.callCounter.toString());
  }

  @Override
  public void testAddItemInMultiSelectMode() {
    super.testAddItemInMultiSelectMode();
    assertEquals("Call count stability check.", "{findIds=2, getItemForId=8, getItems=4}", service.callCounter.toString());
  }

  @Override
  public void testRemoveItems() {
    super.testRemoveItems();
    assertEquals("Call count stability check.", "{findIds=2, getItemForId=8, getItems=1}", service.callCounter.toString());
  }

  @Override
  public void testRemoveOfAddedAndUpdatedItems() {
    super.testRemoveOfAddedAndUpdatedItems();
    assertEquals("Call count stability check.", "{findIds=2, getItemForId=12, getItems=1}", service.callCounter.toString());
  }

  // --- A fake service implementation that does the job just in memory. ---

  static class TestService extends IdQueryServiceFakeBase<Bean, Integer> implements IdQueryService<Bean, Integer> {
    @Override public Integer getIdForItem(Bean item) {
      return item.getId();
    }
  }
}
