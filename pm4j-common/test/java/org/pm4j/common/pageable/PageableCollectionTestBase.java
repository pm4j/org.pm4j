package org.pm4j.common.pageable;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.common.query.CompOpStringStartsWith;
import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.QueryUtil;
import org.pm4j.common.query.SortOrder;
import org.pm4j.common.util.collection.IterableUtil;

/**
 * An abstract test that checks the algorithms that should work for all kinds of
 * {@link PageableCollection2}.
 *
 * @author olaf boede
 */
public abstract class PageableCollectionTestBase<T> {

  protected PageableCollection2<T> collection;
  protected SortOrder nameSortOrder;

  /** Needs to be implemented by the concrete test classes. */
  protected abstract PageableCollection2<T> makePageableCollection(String... strings);

  /** A default implementation, which may differ for the PM collection test.
   *  There we have to navigate from the attribute to the value too. */
  private FilterExpression getFilterNameStartsWith(String startString) {
    return QueryUtil.getFilter(collection.getQueryOptions(), "name", CompOpStringStartsWith.NAME, startString);
  }

  protected abstract SortOrder getOrderByName();


  @Before
  public void setUp() {
    collection = makePageableCollection("a", "b", "c", "d", "e", "f");
    collection.setPageSize(2);
    nameSortOrder = getOrderByName();
  }

  @Test
  public void testFullCollectionIterationResult() {
    assertEquals("[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());
  }

  @Test
  public void testItemsOnPage() {
    assertEquals(1, collection.getCurrentPageIdx());
    assertEquals("[a, b]", collection.getItemsOnPage().toString());

    collection.setCurrentPageIdx(2);
    assertEquals("[c, d]", collection.getItemsOnPage().toString());

    collection.setCurrentPageIdx(3);
    assertEquals("[e, f]", collection.getItemsOnPage().toString());

    collection.setPageSize(3);
    collection.setCurrentPageIdx(1);
    assertEquals("[a, b, c]", collection.getItemsOnPage().toString());

    collection.setCurrentPageIdx(3);
    assertEquals("[]", collection.getItemsOnPage().toString());
    collection.setCurrentPageIdx(4);
    assertEquals("[]", collection.getItemsOnPage().toString());
  }



  @Test
  public void testSortItems() {
    assertEquals("Initial (unsorted) sort order", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());

    collection.getQuery().setSortOrder(nameSortOrder);
    assertEquals("Ascending sort order", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());

    collection.getQuery().setSortOrder(nameSortOrder.getReverseSortOrder());
    assertEquals("Descending sort order", "[f, e, d, c, b, a]", IterableUtil.shallowCopy(collection).toString());

    collection.getQuery().setSortOrder(null);
    assertEquals("Initial (unsorted) sort order again", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());
  }

  @Test
  public void testDefaultSortOrder() {
    collection.setCurrentPageIdx(3);
    assertEquals("Initial (unsorted) sort order", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());
    assertEquals("[e, f]", collection.getItemsOnPage().toString());

    collection.getQuery().setDefaultSortOrder(nameSortOrder.getReverseSortOrder());
    assertEquals("New default: Descending sort order", "[f, e, d, c, b, a]", IterableUtil.shallowCopy(collection).toString());
    assertEquals("[b, a]", collection.getItemsOnPage().toString());

    collection.getQuery().setSortOrder(nameSortOrder);
    assertEquals("Sort in ascending order", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());
    assertEquals("[e, f]", collection.getItemsOnPage().toString());

    collection.getQuery().setSortOrder(null);
    assertEquals("Sorted in descending default sort order again.", "[f, e, d, c, b, a]", IterableUtil.shallowCopy(collection).toString());
    assertEquals("[b, a]", collection.getItemsOnPage().toString());

    collection.getQuery().setDefaultSortOrder(null);
    assertEquals("Initial (unsorted) sort order again.", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());
    assertEquals("[e, f]", collection.getItemsOnPage().toString());
  }

  @Test
  public void testFilterItems() {
    collection.getQuery().setFilterExpression(getFilterNameStartsWith("b"));
    assertEquals("Filtered item set.", "[b]", IterableUtil.shallowCopy(collection).toString());

    collection.getQuery().setFilterExpression(null);
    assertEquals("We get all items after resetting the filter.", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());
  }

  protected List<Bean> makeBeans(String... strings) {
    List<Bean> list = new ArrayList<Bean>();
    for (String s : strings) {
      list.add(new Bean(s));
    }
    return list;
  }


  public static class Bean {
    public final Integer id;
    public final String name;

    public Bean(String name) {
      this.id = null;
      this.name = name;
    }

    public Bean(int id, String name) {
      this.id = id;
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }

    public Integer getId() {
      return id;
    }
  }
}
