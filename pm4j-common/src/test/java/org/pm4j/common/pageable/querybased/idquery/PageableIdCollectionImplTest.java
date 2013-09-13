package org.pm4j.common.pageable.querybased.idquery;

import java.util.Comparator;

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
