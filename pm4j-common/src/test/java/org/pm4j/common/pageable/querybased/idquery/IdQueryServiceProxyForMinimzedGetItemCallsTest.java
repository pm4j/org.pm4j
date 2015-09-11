package org.pm4j.common.pageable.querybased.idquery;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.common.pageable.TestBean;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.PageableCollectionTestBase;
import org.pm4j.common.pageable.querybased.MaxQueryResultsViolationException;

/**
 * Performs all pageable collection tests for a service having an {@link IdQueryServiceProxyForMinimzedGetItemCalls}.
 *
 * @author Olaf Boede
 */
public class IdQueryServiceProxyForMinimzedGetItemCallsTest extends PageableCollectionTestBase<TestBean> {
  TestService service = new TestService();
  IdQueryServiceProxyForMinimzedGetItemCalls<TestBean, Integer> serviceProxy = new IdQueryServiceProxyForMinimzedGetItemCalls<TestBean, Integer>(service);

  @Override
  protected PageableCollection<TestBean> makePageableCollection(String... strings) {
    service.deleteAll();
    if (strings != null) {
      for (String s : strings) {
        service.save(new TestBean(s));
      }
    }
    return new IdQueryCollectionImpl<TestBean, Integer>(serviceProxy, getQueryOptions());
  }

  @Override
  protected TestBean createItem(int id, String name) {
    return new TestBean(id, name);
  }

  @Override
  public void setUp() {
    super.setUp();
    assertEquals("Call count stability check.", "{findIds=1, getItems=1}", service.callCounter.toString());
    service.callCounter.reset();
    serviceProxy.clearWeakMap();
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
    assertEquals("Call count stability check.", "{findIds=1, getItems=2}", service.callCounter.toString());
  }

  @Test @Override
  public void testSwitchQueryExecOffAndOn() {
    super.testSwitchQueryExecOffAndOn();
    assertEquals("Call count stability check.", "{findIds=2, getItems=1}", service.callCounter.toString());
  }

  @Test @Override
  public void testItemsOnPage() {
    super.testItemsOnPage();
    assertEquals("Call count stability check.", "{findIds=1, getItems=3}", service.callCounter.toString());
  }

  @Override @Test
  public void testSortItems() {
    super.testSortItems();
    assertEquals("Call count stability check.", "{findIds=4, getItems=1}", service.callCounter.toString());
  }

  @Override @Test
  public void testDefaultSortOrder() {
    super.testDefaultSortOrder();
    assertEquals("Call count stability check.", "{findIds=5, getItems=1}", service.callCounter.toString());
  }

  @Override @Test
  public void testFilterItems() {
    super.testFilterItems();
    assertEquals("Call count stability check.", "{findIds=2, getItems=2}", service.callCounter.toString());
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
    assertEquals("Call count stability check.", "{findIds=2, getItems=1}", service.callCounter.toString());
  }

  @Override @Test
  public void testAddItemToEmptyCollection() {
    super.testAddItemToEmptyCollection();
    assertEquals("Call count stability check.", "{findIds=1}", service.callCounter.toString());
  }

  @Override @Test
  public void testAddItemInMultiSelectMode() {
    super.testAddItemInMultiSelectMode();
    assertEquals("Call count stability check.", "{findIds=2, getItems=1}", service.callCounter.toString());
  }

  @Override @Test
  public void testRemoveItems() {
    super.testRemoveItems();
    assertEquals("Call count stability check.", "{findIds=2, getItems=2}", service.callCounter.toString());
  }

  @Override @Test
  public void testRemoveOfAddedAndUpdatedItems() {
    super.testRemoveOfAddedAndUpdatedItems();
    assertEquals("Call count stability check.", "{findIds=2, getItems=2}", service.callCounter.toString());
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
    // TODO verify:
    assertEquals("Call count stability check.", "{}", service.callCounter.toString());
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
  
  @Override @Test
  public void testIterateSingleSelection() {
    super.testIterateSingleSelection();
    assertEquals("Call count stability check.", "{}", service.callCounter.toString());
  }

  @Test
  public void testGetNumOfItems() {
    super.testGetNumOfItems();
    assertEquals("Call count stability check.", "{findIds=1}", service.callCounter.toString());
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

  static class TestService extends IdQueryServiceFake.WithIntegerId<TestBean>{}
}
