package org.pm4j.common.pageable.inmem;

import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.PageableCollectionTestBase;
import org.pm4j.common.query.CompOpStartsWith;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.filter.FilterDefinition;

public class InMemCollectionTest extends PageableCollectionTestBase<PageableCollectionTestBase.Bean> {

  @Override
  public PageableCollection<Bean> makePageableCollection(String... strings) {
    QueryOptions qo = new QueryOptions();
    qo.addFilterCompareDefinition(new FilterDefinition(new QueryAttr("name", String.class), new CompOpStartsWith()));
    return new InMemCollectionImpl<Bean>(makeBeans(strings), qo);
  }

  @Override
  protected Bean createItem(int id, String name) {
    return new Bean(id, name);
  }

}
