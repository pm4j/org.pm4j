package org.pm4j.common.pageable.querybased.pagequery;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.PageableCollectionTestBase;
import org.pm4j.common.query.CompOpNotEquals;
import org.pm4j.common.query.CompOpStartsWith;
import org.pm4j.common.query.QueryExprCompare;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.query.filter.FilterDefinition;
import org.pm4j.common.query.inmem.InMemQueryEvaluator;
import org.pm4j.common.util.collection.IterableUtil;

public class PageQueryCollectionTest extends PageableCollectionTestBase<PageableCollectionTestBase.Bean> {

  @Test
  public void testAllItemsSelection() {
    PageQueryAllItemsSelection<Bean, Integer> selection = new PageQueryAllItemsSelection<Bean, Integer>(service);

    assertEquals(7L, selection.getSize());
    assertEquals("[ , a, b, c, d, e, f]", IterableUtil.asCollection(selection).toString());

    QueryParams queryParams = new QueryParams();
    queryParams.setFilterExpression(new QueryExprCompare(Bean.ATTR_NAME, CompOpNotEquals.class, " "));
    selection = new PageQueryAllItemsSelection<Bean, Integer>(service, queryParams);
    assertEquals(6L, selection.getSize());
    assertEquals("[a, b, c, d, e, f]", IterableUtil.asCollection(selection).toString());

    assertTrue(selection.contains(service.getItemForId(1)));
    // That's an all-selection limitation: The not-selection check is not yet supported.
    // assertFalse(selection.contains(createItem(-1, "")));
  }

  // -- Test infrastructure

  TestService service = new TestService();

  @Override
  protected PageableCollection<Bean> makePageableCollection(String... strings) {
    int counter = 1;
    service.removeAllBeans();
    if (strings != null) {
      for (String s : strings) {
        service.addBean(new Bean(++counter, s));
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

  // --- A fake service implementation that does the job just in memory. ---

  static class TestService implements PageQueryService<Bean, Integer> {

    private Map<Integer, Bean> idToBeanMap = new LinkedHashMap<Integer, Bean>();

    @Override
    public Integer getIdForItem(Bean item) {
      return item.getId();
    }

    @Override
    public Bean getItemForId(Integer id) {
      return idToBeanMap.get(id);
    }

    @Override
    public List<Bean> getItems(QueryParams query, long startIdx, int pageSize) {
      if (startIdx >= idToBeanMap.size()) {
        return Collections.emptyList();
      }

      List<Bean> allQueryResultItems = getQueryResult(query);
      int endIdx = Math.min((int)startIdx + pageSize, allQueryResultItems.size());

      List<Bean> beanList = allQueryResultItems.subList((int)startIdx, endIdx);
      return beanList;
    }

    @Override
    public long getItemCount(QueryParams query) {
      return getQueryResult(query).size();
    }

    public void addBean(Bean b) {
      idToBeanMap.put(b.getId(), b);
    }

    public void removeAllBeans() {
      idToBeanMap.clear();
    }

    // some in memory fakes ...
    private List<Bean> getQueryResult(QueryParams query) {
      InMemQueryEvaluator<Bean> evalCtxt = new InMemQueryEvaluator<Bean>();
      List<Bean> beans = evalCtxt.sort(idToBeanMap.values(), query.getEffectiveSortOrder());

      if (query.getFilterExpression() != null) {
        beans = evalCtxt.evaluateSubSet(beans, query.getFilterExpression());
      }

      return beans;
    }
  }
}
