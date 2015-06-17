package org.pm4j.common.pageable.querybased.idquery;

import java.util.ArrayList;
import java.util.Collection;

import org.pm4j.common.modifications.ModificationsImpl;
import org.pm4j.common.pageable.PageableCollectionBase;
import org.pm4j.common.pageable.querybased.QueryCollectionModificationHandlerBase;
import org.pm4j.common.pageable.querybased.QueryService;
import org.pm4j.common.pageable.querybased.pagequery.ItemIdSelection;
import org.pm4j.common.query.QueryExpr;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.util.collection.ListUtil;

public class IdQueryCollectionModificationHandler<T_ITEM, T_ID> extends QueryCollectionModificationHandlerBase<T_ITEM, T_ID, QueryService<T_ITEM, T_ID>> {

  public IdQueryCollectionModificationHandler(PageableCollectionBase<T_ITEM> pageableCollection, QueryService<T_ITEM, T_ID> service) {
    super(pageableCollection, service);
  }

  protected Collection<T_ID> getItemIds(Selection<T_ITEM> selection) {
    Collection<T_ID> ids = new ArrayList<T_ID>((int)selection.getSize());
    for (T_ITEM i : selection) {
      ids.add(getService().getIdForItem(i));
    }
    return ids;
  }
  
//TODO
  
 protected void registerRemovedItems(Selection<T_ITEM> persistentRemovedItemSelection) {
   // Remember the previous set of removed items. It needs to be extended by some additional items to remove.
   Selection<T_ITEM> oldRemovedItemSelection = getModifications().getRemovedItems();
   if (! (persistentRemovedItemSelection instanceof ItemIdSelection)) {
     // TODO olaf: big inverted selections are not yet supported
     long newSize = persistentRemovedItemSelection.getSize() + oldRemovedItemSelection.getSize();
     if (newSize > 1000) {
       throw new IndexOutOfBoundsException("Maximum 1000 rows can be removed within a single save operation.");
     }
   }

   Collection<T_ID> ids = ListUtil.collectionsToList(getItemIds(persistentRemovedItemSelection), getItemIds(oldRemovedItemSelection));
   // TODO getModifications().setRemovedItems(createItemIdSelection(ids));
}

  @Override
  protected QueryExpr getRemovedItemsFilterExpr(QueryExpr queryFilterExpr) {
    // TODO Auto-generated method stub
    return null;
  }
}
