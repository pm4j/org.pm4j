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
    assertEquals("Call count stability check.", "{getItemCount=5, getItemForId=2, getItems=5}", service.callCounter.toString());
    service.callCounter.reset();
  }

  @Override
  public void testItemNavigator() {
    super.testItemNavigator();
    assertEquals("Call count stability check.", "{getItemCount=2, getItemForId=3, getItems=2}", service.callCounter.toString());
  }

  @Override
  public void testSwitchQueryExecOffAndOn() {
    super.testSwitchQueryExecOffAndOn();
    assertEquals("Call count stability check.", "{getItemCount=8, getItems=6}", service.callCounter.toString());
  }

  @Test @Override
  public void testItemsOnPage() {
    super.testItemsOnPage();
    assertEquals("Call count stability check.", "{getItemCount=1, getItems=6}", service.callCounter.toString());
  }

  @Override
  public void testSortItems() {
    super.testSortItems();
    assertEquals("Call count stability check.", "{getItemCount=16, getItems=12}", service.callCounter.toString());
  }

  @Override
  public void testDefaultSortOrder() {
    super.testDefaultSortOrder();
    assertEquals("Call count stability check.", "{getItemCount=20, getItems=20}", service.callCounter.toString());
  }

  @Override
  public void testFilterItems() {
    super.testFilterItems();
    assertEquals("Call count stability check.", "{getItemCount=6, getItems=4}", service.callCounter.toString());
  }

  @Override
  public void testSelectItems() {
    super.testSelectItems();
    assertEquals("Call count stability check.", "{getItemCount=3, getItems=1}", service.callCounter.toString());
  }

  @Override
  public void testSelectInvertAndDeselect() {
    super.testSelectInvertAndDeselect();
    assertEquals("Call count stability check.", "{getItemCount=3, getItems=1}", service.callCounter.toString());
  }

  @Override
  public void testAddItem() {
    super.testAddItem();
    assertEquals("Call count stability check.", "{getItemCount=6, getItems=7}", service.callCounter.toString());
  }

  @Override
  public void testAddItemToEmptyCollection() {
    super.testAddItemToEmptyCollection();
    assertEquals("Call count stability check.", "{getItemCount=2}", service.callCounter.toString());
  }

  @Override
  public void testAddItemInMultiSelectMode() {
    super.testAddItemInMultiSelectMode();
    assertEquals("Call count stability check.", "{getItemCount=6, getItems=7}", service.callCounter.toString());
  }

  @Override
  public void testRemoveItems() {
    super.testRemoveItems();
    assertEquals("Call count stability check.", "{getItemCount=4, getItems=3}", service.callCounter.toString());
  }

  @Override
  public void testRemoveOfAddedAndUpdatedItems() {
    super.testRemoveOfAddedAndUpdatedItems();
    assertEquals("Call count stability check.", "{getItemCount=3, getItems=2}", service.callCounter.toString());
  }

  @Override
  public void testIterateAllSelectionWithBlockSize3() {
    super.testIterateAllSelectionWithBlockSize3();
    assertEquals("Call count stability check.", "{getItems=3}", service.callCounter.toString());
  }

  @Override @Test
  public void testIterateAllSelectionWithBlockSize6() {
    super.testIterateAllSelectionWithBlockSize6();
    // Two calls, because the first block was completely filled.
    // The second call is in that case needed to find out that there are no more items.
    assertEquals("Call count stability check.", "{getItems=2}", service.callCounter.toString());
  }

  @Override @Test
  public void testIteratePositiveSelectionOf3ItemsWithBlockSize2() {
    super.testIteratePositiveSelectionOf3ItemsWithBlockSize2();
    assertEquals("Call count stability check.", "{getItemForId=3}", service.callCounter.toString());
  }

  @Override @Test
  public void testIterateAllSelectionMinusOneWithBlockSize2() {
    super.testIterateAllSelectionMinusOneWithBlockSize2();
    assertEquals("Call count stability check.", "{getItems=4}", service.callCounter.toString());
  }

  @Override @Test
  public void testIterateEmptySelection() {
    super.testIterateEmptySelection();
    assertEquals("Call count stability check.", "{}", service.callCounter.toString());
  }


  // XXX: either find a better name of move to a PageQueryAllItemsSelectionTest class.
  @Test
  public void testAllItemsSelection() {
    PageQueryAllItemsSelection<Bean, Integer> selection = new PageQueryAllItemsSelection<Bean, Integer>(service);

    assertEquals(7L, selection.getSize());
    assertEquals("[ , a, b, c, d, e, f]", IterableUtil.asCollection(selection).toString());

    assertEquals("Call count stability check.", "{getItemCount=1, getItems=1}", service.callCounter.toString());

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
