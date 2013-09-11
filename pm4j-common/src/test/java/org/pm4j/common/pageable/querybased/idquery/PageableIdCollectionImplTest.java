package org.pm4j.common.pageable.querybased.idquery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.PageableCollectionTestBase;
import org.pm4j.common.pageable.querybased.idquery.PageableIdQueryCollectionImpl;
import org.pm4j.common.pageable.querybased.idquery.PageableIdQueryService;
import org.pm4j.common.query.CompOpStringStartsWith;
import org.pm4j.common.query.FilterCompareDefinition;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.query.SortOrder;
import org.pm4j.common.query.inmem.InMemQueryEvaluator;
import org.pm4j.common.query.inmem.InMemSortOrder;
import org.pm4j.common.util.CompareUtil;

public class PageableIdCollectionImplTest extends PageableCollectionTestBase<PageableCollectionTestBase.Bean> {
  TestService service = new TestService();

  @Override
  protected PageableCollection2<org.pm4j.common.pageable.PageableCollectionTestBase.Bean> makePageableCollection(
      String... strings) {
    int counter = 1;
    service.removeAllBeans();
    if (strings != null) {
      for (String s : strings) {
        service.addBean(new Bean(++counter, s));
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

  // --- A fake service implementation that does the job just in memory. ---

  // TODO oboede: Missing Dao/Service fake infrastructure im pm4j common!
  static class TestService implements PageableIdQueryService<Bean, Integer> {

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
    public List<Bean> getItems(List<Integer> ids) {
      List<Bean> beans = new ArrayList<Bean>(ids.size());
      for (Integer id : ids) {
        beans.add(idToBeanMap.get(id));
      }
      return beans;
    }

    @Override
    public List<Integer> findIds(QueryParams query, long startIdx, int pageSize) {
      if (startIdx >= idToBeanMap.size()) {
        return Collections.emptyList();
      }

      List<Bean> allQueryResultItems = getQueryResult(query);
      int endIdx = Math.min((int) startIdx + pageSize, allQueryResultItems.size());

      List<Bean> beanList = allQueryResultItems.subList((int) startIdx, endIdx);
      List<Integer> idList = new ArrayList<Integer>();
      for (Bean b : beanList) {
        idList.add(b.getId());
      }
      return idList;
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