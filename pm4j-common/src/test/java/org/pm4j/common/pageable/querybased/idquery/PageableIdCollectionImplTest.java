package org.pm4j.common.pageable.querybased.idquery;

import static junit.framework.Assert.assertEquals;

import java.util.Comparator;

import org.junit.Test;
import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.PageableCollectionTestBase;
import org.pm4j.common.query.CompOpStringStartsWith;
import org.pm4j.common.query.FilterCompareDefinition;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.SortOrder;
import org.pm4j.common.query.inmem.InMemSortOrder;
import org.pm4j.common.util.CompareUtil;

public class PageableIdCollectionImplTest extends PageableCollectionTestBase<PageableCollectionTestBase.Bean> {

  TestService service = new TestService();

  @Override
  protected PageableCollection2<Bean> makePageableCollection(String... strings) {
    int counter = 1;
    service.removeAllFakeItems();
    if (strings != null) {
      for (String s : strings) {
        service.addFakeItem(new Bean(++counter, s));
      }
    }
    return new PageableIdQueryCollectionImpl<Bean, Integer>(service, null);
  }

  @Override
  protected SortOrder getOrderByName() {
    return service.getQueryOptions().getSortOrder("name");
  }

  @Override
  protected Bean createItem(String name) {
    return new Bean(name);
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

  static class TestService extends PageableIdQueryDaoFakeBase<Bean, Integer> implements PageableIdQueryService<Bean, Integer> {

    @Override
    public Integer getIdForItem(Bean item) {
      return item.getId();
    }

    // some in memory fakes ...
    @Override
    public QueryOptions getQueryOptions() {
      QueryOptions options = new QueryOptions();
      QueryAttr nameAttr = new QueryAttr("name", String.class);

      options.addSortOrder("name", new InMemSortOrder(new Comparator<Bean>() {
        @Override
        public int compare(Bean o1, Bean o2) {
          return CompareUtil.compare(o1.name, o2.name);
        }
      }));

      options.addFilterCompareDefinition(new FilterCompareDefinition(nameAttr, new CompOpStringStartsWith()));

      return options;
    }
  }
}
