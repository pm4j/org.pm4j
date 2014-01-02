package org.pm4j.common.pageable.inmem;

import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.PageableCollectionTestBase;
import org.pm4j.common.query.CompOpStartsWith;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.filter.FilterDefinition;

public class InMemoryPageableCollectionTest extends PageableCollectionTestBase<PageableCollectionTestBase.Bean> {

  @Override
  public PageableCollection2<Bean> makePageableCollection(String... strings) {
    QueryOptions qo = new QueryOptions();
    qo.addFilterCompareDefinition(new FilterDefinition(new QueryAttr("name", String.class), new CompOpStartsWith()));
    return new PageableInMemCollectionImpl<Bean>(makeBeans(strings), qo);
  }

  @Override
  protected Bean createItem(String name) {
    return new Bean(name);
  }

}
