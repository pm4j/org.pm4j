package org.pm4j.common.pageable.inmem;

import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.PageableCollectionTestBase;
import org.pm4j.common.query.CompOpStringStartsWith;
import org.pm4j.common.query.FilterCompareDefinition;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryOptions;

public class InMemoryPageableCollectionTest extends PageableCollectionTestBase<PageableCollectionTestBase.Bean> {

  @Override
  public PageableCollection2<Bean> makePageableCollection(String... strings) {
    QueryOptions qo = new QueryOptions();
    qo.addFilterCompareDefinition(new FilterCompareDefinition(new QueryAttr("name", String.class), new CompOpStringStartsWith()));
    return new PageableInMemCollectionImpl<Bean>(makeBeans(strings), qo);
  }

  @Override
  protected Bean createItem(String name) {
    return new Bean(name);
  }

}
