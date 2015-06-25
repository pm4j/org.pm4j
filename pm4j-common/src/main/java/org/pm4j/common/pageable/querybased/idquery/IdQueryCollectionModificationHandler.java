package org.pm4j.common.pageable.querybased.idquery;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.pm4j.common.pageable.PageableCollectionBase;
import org.pm4j.common.pageable.querybased.QueryCollectionModificationHandlerBase;
import org.pm4j.common.pageable.querybased.QueryService;
import org.pm4j.common.query.CompOpIn;
import org.pm4j.common.query.QueryExpr;
import org.pm4j.common.query.QueryExprCompare;
import org.pm4j.common.query.QueryExprNot;
import org.pm4j.common.selection.Selection;

/*package*/ class IdQueryCollectionModificationHandler<T_ITEM, T_ID> extends QueryCollectionModificationHandlerBase<T_ITEM, T_ID, QueryService<T_ITEM, T_ID>> {

  public IdQueryCollectionModificationHandler(PageableCollectionBase<T_ITEM> pageableCollection, QueryService<T_ITEM, T_ID> service) {
    super(pageableCollection, service);
  }

 @Override
 protected void setRemovedItemsImpl(Selection<T_ITEM> persistentRemovedItemSelection) {
    HashSet<T_ID> ids = new HashSet<T_ID>(getIds(persistentRemovedItemSelection));
    ids.addAll(getIds(getModifications().getRemovedItems()));
    getModificationsImpl().setRemovedItems(new IdQuerySelection<T_ITEM, T_ID>((IdQueryService<T_ITEM, T_ID>)getService(), ids));
  }

  @Override
  protected QueryExpr createRemovedItemsExpr(QueryExpr queryFilterExpr) {
    return new QueryExprNot(new QueryExprCompare(getPageableCollection().getQueryOptions().getIdAttribute(), CompOpIn.class, getIds(getModifications().getRemovedItems())));
  }

  @SuppressWarnings("unchecked")
  private Collection<T_ID> getIds(Selection<T_ITEM> selection) {
    if (selection.isEmpty()) {
      return Collections.emptySet();
    } else {
      return ((IdQuerySelection<T_ITEM, T_ID>)selection).getIds();
    }
  }

}
