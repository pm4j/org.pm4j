package org.pm4j.common.pageable.querybased.idquery;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.PageableCollectionTestBase;

public class IdQueryCollectionImplWithExtraCountQueryStrategyTest extends PageableCollectionTestBase<PageableCollectionTestBase.Bean> {
  TestService service = new TestService();

  @Override
  protected PageableCollection<Bean> makePageableCollection(String... strings) {
    service.deleteAll();
    if (strings != null) {
      for (String s : strings) {
        service.save(new Bean(s));
      }
    }
    return new IdQueryCollectionImpl<Bean, Integer>(service, getQueryOptions(), IdQueryCollectionImpl.ExtraCountQueryStrategy.INSTANCE);
  }

  @Override
  protected Bean createItem(int id, String name) {
    return new Bean(id, name);
  }

  @Override
  public void setUp() {
    super.setUp();
    service.callCounter.assertCalls("Precondition failed: ", "{findIds=1, getItemCount=1, getItemForId=8, getItems=4}");
    service.callCounter.reset();
  }

  @Test
  public void testMethodCallCount() {
    collection.getItemsOnPage();
    service.callCounter.assertCalls("One call to get all ids and one to get the page items.", "{findIds=1, getItemCount=1, getItems=1}");

    service.callCounter.reset();
    collection.getItemsOnPage();
    service.callCounter.assertCalls("No additional call on re-getting the current page.", "{}");

    service.callCounter.reset();
    collection.setPageIdx(1l);
    collection.getItemsOnPage();
    service.callCounter.assertCalls("One call to get the items of the next page.", "{getItems=1}");
  }

  @Test @Override
  public void testItemNavigator() {
    super.testItemNavigator();
    service.callCounter.assertCalls("{findIds=1, getItemCount=1, getItems=4}");
  }

  @Test @Override
  public void testSwitchQueryExecOffAndOn() {
    super.testSwitchQueryExecOffAndOn();
    service.callCounter.assertCalls("{findIds=2, getItemCount=2, getItemForId=12}");
  }

  @Test @Override
  public void testItemsOnPage() {
    super.testItemsOnPage();
    service.callCounter.assertCalls("{findIds=1, getItemCount=1, getItems=4}");
  }

  @Override @Test
  public void testSortItems() {
    super.testSortItems();
    service.callCounter.assertCalls("{findIds=4, getItemCount=4, getItemForId=24}");
  }

  @Override @Test
  public void testDefaultSortOrder() {
    super.testDefaultSortOrder();
    service.callCounter.assertCalls("{findIds=5, getItemCount=5, getItemForId=30, getItems=5}");
  }

  @Override @Test
  public void testFilterItems() {
    super.testFilterItems();
    service.callCounter.assertCalls("{findIds=2, getItemCount=2, getItemForId=7}");
  }

  @Override @Test
  public void testSelectItems() {
    super.testSelectItems();
    service.callCounter.assertCalls("{findIds=1, getItemCount=1, getItems=1}");
  }

  @Override @Test
  public void testSelectInvertAndDeselect() {
    super.testSelectInvertAndDeselect();
    service.callCounter.assertCalls("{findIds=1, getItemCount=1, getItems=1}");
  }

  @Override @Test
  public void testAddItem() {
    super.testAddItem();
    service.callCounter.assertCalls("{findIds=2, getItemCount=2, getItemForId=6, getItems=6}");
  }

  @Override @Test
  public void testAddItemToEmptyCollection() {
    super.testAddItemToEmptyCollection();
    service.callCounter.assertCalls("{findIds=1, getItemCount=1}");
  }

  @Override @Test
  public void testAddItemInMultiSelectMode() {
    super.testAddItemInMultiSelectMode();
    service.callCounter.assertCalls("{findIds=2, getItemCount=2, getItemForId=6, getItems=6}");
  }

  @Override @Test
  public void testRemoveItems() {
    super.testRemoveItems();
    service.callCounter.assertCalls("{findIds=2, getItemCount=2, getItemForId=4, getItems=3}");
  }

  @Override @Test
  public void testRemoveOfAddedAndUpdatedItems() {
    super.testRemoveOfAddedAndUpdatedItems();
    service.callCounter.assertCalls("{findIds=2, getItemCount=2, getItems=3}");
  }

  @Override @Test
  public void testIterateAllSelectionWithBlockSize3() {
    super.testIterateAllSelectionWithBlockSize3();
    assertEquals("Call count stability check.", "{findIds=1, getItemCount=1, getItems=2}", service.callCounter.toString());
  }

  @Override @Test
  public void testIterateAllSelectionWithBlockSize6() {
    super.testIterateAllSelectionWithBlockSize6();
    assertEquals("Call count stability check.", "{findIds=1, getItemCount=1, getItems=1}", service.callCounter.toString());
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

  @Test
  public void testGetNumOfItems() {
    super.testGetNumOfItems();
    assertEquals("Call count stability check.", "{findIds=1, getItemCount=1}", service.callCounter.toString());
  }

  @Test(expected=MaxQueryResultsViolationException.class)
  public void testGetNumOfItemsWithMaxQueryResultsViolationException() {
    collection.getQueryParams().setMaxResults(1L);
    super.testGetNumOfItems();
  }

  @Override
  protected void resetCallCounter() {
    service.callCounter.reset();
  }


  // --- A fake service implementation that does the job just in memory. ---

  static class TestService extends IdQueryServiceFake.WithIntegerId<Bean>{}
}
