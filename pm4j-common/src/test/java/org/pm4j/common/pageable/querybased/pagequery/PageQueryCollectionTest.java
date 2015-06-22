package org.pm4j.common.pageable.querybased.pagequery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.PageableCollectionTestBase;
import org.pm4j.common.query.CompOpNotEquals;
import org.pm4j.common.query.CompOpStartsWith;
import org.pm4j.common.query.QueryExprCompare;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.query.filter.FilterDefinition;
import org.pm4j.common.util.collection.IterableUtil;

/**
 * Executes the set of standard operations to test for each
 * {@link PageableCollection} for the sub class {@link PageQueryCollection}.
 *
 * @author Olaf Boede
 */
public class PageQueryCollectionTest extends PageableCollectionTestBase<PageableCollectionTestBase.Bean> {

  @Override
  public void setUp() {
    super.setUp();
    service.callCounter.assertCalls("Precondition failed:", "{getItemCount=1, getItems=4}");
    service.callCounter.reset();
  }

  @Override
  public void testItemNavigator() {
    super.testItemNavigator();
    service.callCounter.assertCalls("{getItemCount=2, getItems=5}");
  }

  @Override
  public void testSwitchQueryExecOffAndOn() {
    super.testSwitchQueryExecOffAndOn();
    service.callCounter.assertCalls("{getItemCount=1, getItems=2}");
  }

  @Test @Override
  public void testItemsOnPage() {
    super.testItemsOnPage();
    service.callCounter.assertCalls("{getItemCount=1, getItems=6}");
  }

  @Override
  public void testSortItems() {
    super.testSortItems();
    service.callCounter.assertCalls("{getItems=4}");
  }

  @Override
  public void testDefaultSortOrder() {
    super.testDefaultSortOrder();
    service.callCounter.assertCalls("{getItemCount=5, getItems=10}");
  }

  @Override
  public void testFilterItems() {
    super.testFilterItems();
    service.callCounter.assertCalls("{getItems=2}");
  }

  @Override
  public void testSelectItems() {
    super.testSelectItems();
    service.callCounter.assertCalls("{getItemCount=3, getItems=1}");
  }

  @Override
  public void testSelectedItemsStayInQueryOrderForPositiveSelection() {
    super.testSelectedItemsStayInQueryOrderForPositiveSelection();
    service.callCounter.assertCalls("{getItemCount=3, getItems=10}");
  }

  @Override
  public void testSelectedItemsStayInQueryOrderForNegativeSelection() {
    super.testSelectedItemsStayInQueryOrderForNegativeSelection();
    service.callCounter.assertCalls("{getItemCount=7, getItems=10}");
  }

  @Override
  public void testSelectInvertAndDeselect() {
    super.testSelectInvertAndDeselect();
    service.callCounter.assertCalls("{getItemCount=3, getItems=1}");
  }

  @Override
  public void testAddItem() {
    super.testAddItem();
    service.callCounter.assertCalls("{getItemCount=2, getItems=5}");
  }

  @Override
  public void testAddItemToEmptyCollection() {
    super.testAddItemToEmptyCollection();
    // TODO oboede: getItems() shouldn't be called when it's clear that there are no items.
    service.callCounter.assertCalls("{getItemCount=1, getItems=1}");
  }

  @Override
  public void testAddItemInMultiSelectMode() {
    super.testAddItemInMultiSelectMode();
    service.callCounter.assertCalls("{getItemCount=2, getItems=5}");
  }

  @Override
  public void testRemoveItems() {
    super.testRemoveItems();
    service.callCounter.assertCalls("{getItemCount=2, getItems=2}");
  }

  @Override
  public void testRemoveOfAddedAndUpdatedItems() {
    super.testRemoveOfAddedAndUpdatedItems();
    service.callCounter.assertCalls("{getItemCount=3, getItems=3}");
  }

  @Override
  public void testIterateAllSelectionWithBlockSize3() {
    super.testIterateAllSelectionWithBlockSize3();
    service.callCounter.assertCalls("{getItems=3}");
  }

  @Override @Test
  public void testIterateAllSelectionWithBlockSize6() {
    super.testIterateAllSelectionWithBlockSize6();
    // Two calls, because the first block was completely filled.
    // The second call is in that case needed to find out that there are no more items.
    service.callCounter.assertCalls("{getItems=2}");
  }

  @Override @Test
  public void testIteratePositiveSelectionOf3ItemsWithBlockSize2() {
    super.testIteratePositiveSelectionOf3ItemsWithBlockSize2();
    service.callCounter.assertCalls("{getItems=2}");
  }

  @Override @Test
  public void testIterateAllSelectionMinusOneWithBlockSize2() {
    super.testIterateAllSelectionMinusOneWithBlockSize2();
    service.callCounter.assertCalls("{getItems=4}");
  }

  @Override @Test
  public void testIterateEmptySelection() {
    super.testIterateEmptySelection();
    service.callCounter.assertCalls("{}");
  }


  // XXX: either find a better name of move to a PageQueryAllItemsSelectionTest class.
  @Test
  public void testAllItemsSelection() {
    PageQueryAllItemsSelection<Bean, Integer> selection = new PageQueryAllItemsSelection<Bean, Integer>(service);

    assertEquals(7L, selection.getSize());
    assertEquals("[ , a, b, c, d, e, f]", IterableUtil.asCollection(selection).toString());

    service.callCounter.assertCalls("{getItemCount=1, getItems=1}");

    QueryParams queryParams = new QueryParams();
    queryParams.setQueryExpression(new QueryExprCompare(Bean.ATTR_NAME, CompOpNotEquals.class, " "));
    selection = new PageQueryAllItemsSelection<Bean, Integer>(service, queryParams);
    assertEquals(6L, selection.getSize());
    assertEquals("[a, b, c, d, e, f]", IterableUtil.asCollection(selection).toString());

    assertTrue(selection.contains(service.getItemForId(1)));
    // That's an all-selection limitation: The not-selection check is not yet supported.
    // assertFalse(selection.contains(createItem(-1, "")));
  }

  // -- Test infrastructure

  @Override
  protected void resetCallCounter() {
    service.callCounter.reset();
  }

  private BeanPageQueryServiceFake service = new BeanPageQueryServiceFake();

  @Override
  protected PageableCollection<Bean> makePageableCollection(String... strings) {
    service.deleteAll();
    if (strings != null) {
      for (String s : strings) {
        service.save(new Bean(s));
      }
    }

    QueryOptions options = new QueryOptions();
    options.addSortOrder(Bean.ATTR_NAME);
    options.addFilterCompareDefinition(new FilterDefinition(Bean.ATTR_NAME, new CompOpStartsWith()));

    return new PageQueryCollection<Bean, Integer>(service, options);
  }

  @Override
  protected Bean createItem(int id, String name) {
    return new Bean(id, name);
  }
}
