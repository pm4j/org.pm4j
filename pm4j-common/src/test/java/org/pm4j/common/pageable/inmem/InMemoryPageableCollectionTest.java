package org.pm4j.common.pageable.inmem;

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

public class InMemoryPageableCollectionTest extends PageableCollectionTestBase<PageableCollectionTestBase.Bean> {

  @Override
  public PageableCollection2<Bean> makePageableCollection(String... strings) {
    QueryOptions qo = new QueryOptions();
    qo.addFilterCompareDefinition(new FilterCompareDefinition(new QueryAttr("name", String.class), new CompOpStringStartsWith()));
    return new PageableInMemCollectionImpl<Bean>(makeBeans(strings), qo);
  }


  @Override
  protected SortOrder getOrderByName() {
    return new InMemSortOrder(new Comparator<Bean>() {
      @Override
      public int compare(Bean o1, Bean o2) {
        return CompareUtil.compare(o1.name, o2.name);
      }
    });
  }

  @Override
  protected Bean createItem(String name) {
    return new Bean(name);
  }

}
