package org.pm4j.common.pageable.querybased.idquery;

import org.junit.Test;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.PageableCollectionTestBase;
import org.pm4j.common.pageable.querybased.MaxQueryResultsViolationException;

public class IdQueryCollectionImplWithSingleQueryStrategyTest extends PageableCollectionTestBase<PageableCollectionTestBase.Bean> {

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
    service.callCounter.assertCalls("Precondition failed: ", "{findIds=1, getItems=5}");
    service.callCounter.reset();
  }

  @Test
  public void testMethodCallCount() {
    collection.getItemsOnPage();
    service.callCounter.assertCalls("One call to get all ids and one to get the page items.", "{findIds=1, getItems=1}");

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
    service.callCounter.assertCalls("{findIds=1, getItems=4}");
  }

  @Test @Override
  public void testSwitchQueryExecOffAndOn() {
    super.testSwitchQueryExecOffAndOn();
    service.callCounter.assertCalls("{findIds=2, getItems=2}");
  }

  @Test @Override
  public void testItemsOnPage() {
    super.testItemsOnPage();
    service.callCounter.assertCalls("{findIds=1, getItems=4}");
  }

  @Override @Test
  public void testSortItems() {
    super.testSortItems();
    service.callCounter.assertCalls("{findIds=4, getItems=4}");
  }

  @Override @Test
  public void testDefaultSortOrder() {
    super.testDefaultSortOrder();
    service.callCounter.assertCalls("{findIds=5, getItems=10}");
  }

  @Override @Test
  public void testFilterItems() {
    super.testFilterItems();
    service.callCounter.assertCalls("{findIds=2, getItems=2}");
  }

  @Override @Test
  public void testSelectItems() {
    super.testSelectItems();
    service.callCounter.assertCalls("{findIds=1, getItems=1}");
  }

  @Override @Test
  public void testSelectInvertAndDeselect() {
    super.testSelectInvertAndDeselect();
    service.callCounter.assertCalls("{findIds=1, getItems=1}");
  }

  @Override @Test
  public void testAddItem() {
    super.testAddItem();
    service.callCounter.assertCalls("{findIds=2, getItems=6}");
  }

  @Override @Test
  public void testAddItemToEmptyCollection() {
    super.testAddItemToEmptyCollection();
    service.callCounter.assertCalls("{findIds=1}");
  }

  @Override @Test
  public void testAddItemInMultiSelectMode() {
    super.testAddItemInMultiSelectMode();
    service.callCounter.assertCalls("{findIds=2, getItems=6}");
  }

  @Override @Test
  public void testRemoveItems() {
    super.testRemoveItems();
    service.callCounter.assertCalls("{findIds=2, getItems=3}");
  }

  @Override @Test
  public void testRemoveOfAddedAndUpdatedItems() {
    super.testRemoveOfAddedAndUpdatedItems();
    service.callCounter.assertCalls("{findIds=2, getItems=2}");
  }

  @Override @Test
  public void testIterateAllSelectionWithBlockSize3() {
    super.testIterateAllSelectionWithBlockSize3();
    service.callCounter.assertCalls("{findIds=1, getItems=2}");
  }

  @Override @Test
  public void testIterateAllSelectionWithBlockSize6() {
    super.testIterateAllSelectionWithBlockSize6();
    service.callCounter.assertCalls("{findIds=1, getItems=1}");
  }

  @Override @Test
  public void testIteratePositiveSelectionOf3ItemsWithBlockSize2() {
    super.testIteratePositiveSelectionOf3ItemsWithBlockSize2();
    service.callCounter.assertCalls("{getItems=2}");
  }

  @Override @Test
  public void testIterateAllSelectionMinusOneWithBlockSize2() {
    super.testIterateAllSelectionMinusOneWithBlockSize2();
    service.callCounter.assertCalls("{getItems=3}");
  }

  @Override @Test
  public void testIterateEmptySelection() {
    super.testIterateEmptySelection();
    service.callCounter.assertCalls("{}");
  }

  @Override @Test
  public void testIterateSingleSelection() {
    super.testIterateSingleSelection();
    service.callCounter.assertCalls("{getItems=1}");
  }
  
  @Test
  public void testGetNumOfItems() {
    super.testGetNumOfItems();
    service.callCounter.assertCalls("{findIds=1}");
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
