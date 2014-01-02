package org.pm4j.common.pageable.querybased;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.PageableCollectionTestBase;
import org.pm4j.common.pageable.querybased.pagequery.PageableQueryCollection;
import org.pm4j.common.pageable.querybased.pagequery.PageableQueryService;
import org.pm4j.common.query.CompOpStartsWith;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.query.filter.FilterDefinition;
import org.pm4j.common.query.inmem.InMemQueryEvaluator;

public class PageableQueryCollectionTest extends PageableCollectionTestBase<PageableCollectionTestBase.Bean> {

  TestService service = new TestService();

  @Override
  protected PageableCollection2<Bean> makePageableCollection(String... strings) {
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

    return new PageableQueryCollection<Bean, Integer>(service, options);
  }

  @Override
  protected Bean createItem(String name) {
    return new Bean(name);
  }

  // --- A fake service implementation that does the job just in memory. ---

  static class TestService implements PageableQueryService<Bean, Integer> {

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
