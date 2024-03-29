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
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.util.collection.ListUtil;

/*package*/ abstract class PageQueryCollectionModificationHandler<T_ITEM, T_ID> extends QueryCollectionModificationHandlerBase<T_ITEM, T_ID, PageQueryService<T_ITEM, T_ID>> {

  private static final long MAX_SELECTION_SIZE = 1000;

  public PageQueryCollectionModificationHandler(PageableCollectionBase<T_ITEM> pageableCollection, PageQueryService<T_ITEM, T_ID> service) {
    super(pageableCollection, service);
  }

  @Override
  protected void setRemovedItemsImpl(Selection<T_ITEM> persistentRemovedItemSelection) {
   // XXX MHOENNIG->OBOEDE: I have no idea of how clean this method up with not too much effort

   // Remember the previous set of removed items. It needs to be extended by some additional items to remove.
   Selection<T_ITEM> oldRemovedItemSelection = getModifications().getRemovedItems();
   if (oldRemovedItemSelection.isEmpty() && persistentRemovedItemSelection instanceof PageQueryItemIdSelection) {
     getModificationsImpl().setRemovedItems(persistentRemovedItemSelection);
   } else {
     if (! (persistentRemovedItemSelection instanceof PageQueryItemIdSelection)) {
       // Prevent iteration over an unlimited selection.
       long newSize = persistentRemovedItemSelection.getSize() + oldRemovedItemSelection.getSize();
       if (newSize > MAX_SELECTION_SIZE) {
         throw new MaxItemIdSelectionExceededException(newSize, MAX_SELECTION_SIZE);
       }
     }

     // We are using a collection of IDs instead of a direct database query,
     // because otherwise by combining the selection of distinct IDs, inverting deselecting distinct IDs
     // and inverting again, otherwise it could happen that items are contained in the selection which
     // have not explicitly been selected; this is fatal especially if this selection is used for deletion later on.
     Collection<T_ID> ids = ListUtil.collectionsToList(getItemIds(persistentRemovedItemSelection), getItemIds(oldRemovedItemSelection));
     getModificationsImpl().setRemovedItems(createRemovedItemsSelection(ids));
   }
  }

 /**
  * Must be overridden to create properly parameterized PageQueryItemIdSelection.
  *
  * @param queryService
  * @param ids
  * @return a newly created ItemIdSelection
  */
 protected abstract PageQueryItemIdSelection<T_ITEM, T_ID> createRemovedItemsSelection(Collection<T_ID> ids);

 @SuppressWarnings("unchecked")
 final PageQueryCollection<T_ITEM, T_ID> getPageQueryCollection() {
   return (PageQueryCollection<T_ITEM, T_ID>) getPageableCollection();
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

    if ( selection.getSize() > MAX_SELECTION_SIZE ) {
      throw new MaxItemIdSelectionExceededException(MAX_SELECTION_SIZE, selection.getSize());
    }
    List<T_ID> ids = new ArrayList<T_ID>((int)selection.getSize());
    for (T_ITEM i : selection) {
      ids.add(getService().getIdForItem(i));
    }
    return ids;
  }




}
