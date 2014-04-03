package org.pm4j.common.pageable.inmem;

import java.util.ArrayList;
import java.util.Collection;

import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.inmem.InMemQueryEvaluator;

/**
 * A simple in memory implementation that contains a collection of beans.
 *
 * @param <T_ITEM> the used bean type
 *
 * @author olaf boede
 */
public class InMemCollectionImpl<T_ITEM> extends InMemCollectionBase<T_ITEM> {

  /** Contains the not filtered set of items in their original sort order. */
  private Collection<T_ITEM>             backingCollection;

  public InMemCollectionImpl(Collection<T_ITEM> objects, QueryOptions queryOptions) {
    super(queryOptions);
    backingCollection = objects != null
        ? objects
        : new ArrayList<T_ITEM>();
  }

  public InMemCollectionImpl(InMemQueryEvaluator<T_ITEM> inMemQueryEvaluator, Collection<T_ITEM> objects, QueryOptions queryOptions) {
    this(objects, queryOptions);
    if (inMemQueryEvaluator != null) {
      setInMemQueryEvaluator(inMemQueryEvaluator);
    }
  }


  @Override
  public Collection<T_ITEM> getBackingCollectionImpl() {
    return backingCollection;
  }

}
