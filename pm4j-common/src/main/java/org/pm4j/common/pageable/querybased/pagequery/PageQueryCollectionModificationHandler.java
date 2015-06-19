package org.pm4j.common.pageable.querybased.pagequery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.pm4j.common.pageable.PageableCollectionBase;
import org.pm4j.common.pageable.PageableCollectionUtil;
import org.pm4j.common.pageable.querybased.QueryCollectionModificationHandlerBase;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryExpr;
import org.pm4j.common.query.QueryExprNot;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.util.collection.ListUtil;

// TODOC: Limitations; Advantage of current implementation: No bad surprise by deleting unwanted records.
public class PageQueryCollectionModificationHandler<T_ITEM, T_ID> extends QueryCollectionModificationHandlerBase<T_ITEM, T_ID, PageQueryService<T_ITEM, T_ID>> {

  public PageQueryCollectionModificationHandler(PageableCollectionBase<T_ITEM> pageableCollection, PageQueryService<T_ITEM, T_ID> service) {
    super(pageableCollection, service);
  }

//TODO
  @Override
 protected void setRemovedItemsImpl(Selection<T_ITEM> persistentRemovedItemSelection) {
   // Remember the previous set of removed items. It needs to be extended by some additional items to remove.
   Selection<T_ITEM> oldRemovedItemSelection = getModifications().getRemovedItems();
   // XXX oboede: currently ItemIdSelection is an internal precondition
   if (oldRemovedItemSelection.isEmpty() && persistentRemovedItemSelection instanceof PageQueryItemIdSelection)
   {
     getModificationsImpl().setRemovedItems(persistentRemovedItemSelection);
   } else {
     if (! (persistentRemovedItemSelection instanceof PageQueryItemIdSelection)) {
       long newSize = persistentRemovedItemSelection.getSize() + oldRemovedItemSelection.getSize();
       if (newSize > 1000) {
         throw new IndexOutOfBoundsException("Maximum 1000 rows can be removed within a single save operation.");
       }
     }

     Collection<T_ID> ids = ListUtil.collectionsToList(getItemIds(persistentRemovedItemSelection), getItemIds(oldRemovedItemSelection));
     getModificationsImpl().setRemovedItems(createItemIdSelection(ids));
   }
 }

 // TODO: check if that is still needed, because the base implementation is never used.
 /**
  * Can be overridden if the concrete collection needs a special strategy.
  *
  * @param queryService
  * @param ids
  * @return a newly created ItemIdSelection
  */
 protected PageQueryItemIdSelection<T_ITEM, T_ID> createItemIdSelection(Collection<T_ID> ids) {
   return new PageQueryItemIdSelection<T_ITEM, T_ID>(getService(), ids);
 }

 @Override
 protected QueryExpr createRemovedItemsExpr(QueryExpr queryFilterExpr) {
   // TODO: That's a simplification working only for limited selections.
   // A complete solution would use combined expressions.
    @SuppressWarnings("unchecked")
    ClickedIds<T_ID> ids = getModifications().getRemovedItems().isEmpty()
        ? new ClickedIds<T_ID>()
        : ((PageQueryItemIdSelection<T_ITEM, T_ID>)getModifications().getRemovedItems()).getClickedIds();
    QueryAttr idAttr = getPageableCollection().getQueryOptions().getIdAttribute();
    return new QueryExprNot(PageableCollectionUtil.makeSelectionQueryParams(idAttr, queryFilterExpr, ids));
  }

  @SuppressWarnings("unchecked")
  private Collection<T_ID> getItemIds(Selection<T_ITEM> selection) {
    if (selection.isEmpty()) {
      return Collections.emptyList();
    }

    if (selection instanceof PageQueryItemIdSelection) {
      return ((PageQueryItemIdSelection<T_ITEM, T_ID>)selection).getClickedIds().getIds();
    }

    List<T_ID> ids = new ArrayList<T_ID>((int)selection.getSize());

    // TODO oboede: that may take long in case of large selections (1 mio items selected...)
    // add a limit and throw an exception.

    for (T_ITEM i : selection) {
      ids.add(getService().getIdForItem(i));
    }
    return ids;
  }




}
