package org.pm4j.core.pm.pageable2;

import java.util.Collection;

import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.inmem.PageableInMemCollectionFactoryBase;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.impl.PmTableImpl2;
import org.pm4j.core.pm.impl.PmTableUtil2;

/**
 * Creates a pageable in-memory bean collection based on a collection provided by an expression string.
 *
 * @author oboede
 *
 * @param <T_ITEM>
 */
public class PageableInMemCollectionFactoryExprBased<T_ITEM> extends PageableInMemCollectionFactoryBase<T_ITEM> {

  private PmObject pmCtxt;
  private String expressionString;

  public PageableInMemCollectionFactoryExprBased(PmObject pmCtxt, String expressionString) {
    assert pmCtxt != null;

    this.pmCtxt = pmCtxt;
    this.expressionString = expressionString;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Collection<T_ITEM> getBackingCollectionImpl() {
    return (Collection<T_ITEM>) PmExpressionApi.findByExpression(pmCtxt, expressionString, Collection.class);
  }

  @Override
  public PageableCollection2<T_ITEM> create(QueryOptions queryOptions, QueryParams queryParams) {
    // TODO oboede: a quick hack. Add a table specific factory...
    if (queryOptions == null && pmCtxt instanceof PmTableImpl2) {
      QueryOptions opts = PmTableUtil2.makeQueryOptionsForInMemoryTable((PmTableImpl2<?, ?>)pmCtxt);
      return super.create(opts, queryParams);
    } else {
      return super.create(queryOptions, queryParams);
    }
  }

}
