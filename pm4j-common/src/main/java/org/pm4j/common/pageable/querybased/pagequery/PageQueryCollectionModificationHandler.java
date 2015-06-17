package org.pm4j.common.pageable.querybased.pagequery;

import java.util.Collection;
import java.util.Collections;

import org.pm4j.common.modifications.ModificationsImpl;
import org.pm4j.common.pageable.PageableCollectionBase;
import org.pm4j.common.pageable.PageableCollectionUtil;
import org.pm4j.common.pageable.querybased.QueryCollectionModificationHandlerBase;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryExpr;
import org.pm4j.common.query.QueryExprNot;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.util.collection.ListUtil;

public class PageQueryCollectionModificationHandler<T_ITEM, T_ID> extends QueryCollectionModificationHandlerBase<T_ITEM, T_ID, PageQueryService<T_ITEM, T_ID>> {

  public PageQueryCollectionModificationHandler(PageableCollectionBase<T_ITEM> pageableCollection, PageQueryService<T_ITEM, T_ID> service) {
    super(pageableCollection, service);
  }
  

//TODO
  @Override
 protected void registerRemovedItems(Selection<T_ITEM> persistentRemovedItemSelection) {
   // Remember the previous set of removed items. It needs to be extended by some additional items to remove.
   Selection<T_ITEM> oldRemovedItemSelection = getModifications().getRemovedItems();
   // XXX oboede: currently ItemIdSelection is an internal precondition
   if (oldRemovedItemSelection.isEmpty() && persistentRemovedItemSelection instanceof ItemIdSelection)
   {
     getModifications().setRemovedItems(persistentRemovedItemSelection);
   } else {
     if (! (persistentRemovedItemSelection instanceof ItemIdSelection)) {
       // TODO olaf: big inverted selections are not yet supported
       long newSize = persistentRemovedItemSelection.getSize() + oldRemovedItemSelection.getSize();
       if (newSize > 1000) {
         throw new IndexOutOfBoundsException("Maximum 1000 rows can be removed within a single save operation.");
       }
     }

     Collection<T_ID> ids = ListUtil.collectionsToList(getItemIds(persistentRemovedItemSelection), getItemIds(oldRemovedItemSelection));
     getModifications().setRemovedItems(createItemIdSelection(ids));
   }
 }
 
 /**
  * Can be overridden if the concrete collection needs a special strategy.
  *
  * @param queryService
  * @param ids
  * @return a newly created ItemIdSelection
  */
 protected ItemIdSelection<T_ITEM, T_ID> createItemIdSelection(Collection<T_ID> ids) {
   return new ItemIdSelection<T_ITEM, T_ID>(getService(), ids);
 }
  
 @Override
 protected QueryExpr getRemovedItemsFilterExpr(QueryExpr queryFilterExpr) {
    @SuppressWarnings("unchecked")
    ClickedIds<T_ID> ids = getModifications().getRemovedItems().isEmpty()
        ? new ClickedIds<T_ID>()
        : ((ItemIdSelection<T_ITEM, T_ID>)getModifications().getRemovedItems()).getClickedIds();
    QueryAttr idAttr = getPageableCollection().getQueryOptions().getIdAttribute();
    return new QueryExprNot(PageableCollectionUtil.makeSelectionQueryParams(idAttr, queryFilterExpr, ids));
  }

  @SuppressWarnings("unchecked")
  protected Collection<T_ID> getItemIds(Selection<T_ITEM> selection) {
    if (selection instanceof ItemIdSelection) {
      return ((ItemIdSelection<T_ITEM, T_ID>)selection).getClickedIds().getIds();
    } 
    return Collections.emptyList();
  }


}
