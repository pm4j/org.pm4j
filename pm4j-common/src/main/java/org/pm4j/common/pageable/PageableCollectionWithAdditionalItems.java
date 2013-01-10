package org.pm4j.common.pageable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.selection.SelectionHandlerWithAdditionalItems;
import org.pm4j.common.selection.SelectionWithAdditionalItems;
import org.pm4j.common.util.collection.CombinedIterator;
import org.pm4j.common.util.collection.ListUtil;

/**
 * A pageable collection that combines items provided by a backing base
 * {@link PageableCollection2} with a list of transient items.
 *
 * @param <T_ITEM>
 *          the type of handled items.
 *
 * @author olaf boede
 */
public class PageableCollectionWithAdditionalItems<T_ITEM> implements PageableCollection2<T_ITEM> {

  private final PageableCollection2<T_ITEM>  baseCollection;
  private final List<T_ITEM>                 additionalItems;
  private int                                currentPageIdx = 1;
  private final SelectionHandlerWithAdditionalItems<T_ITEM> selectionHandler;
  private final ModificationHandler<T_ITEM>  modificationHandler;
  private List<T_ITEM>                       itemsOnPage;

  /**
   * @param baseCollection the handled set of (persistent) items.
   * @param itemToBeanConverter a converter that allows to convert the items to the corresponding
   */
  public PageableCollectionWithAdditionalItems(PageableCollection2<T_ITEM> baseCollection) {
    assert baseCollection != null;

    this.baseCollection = baseCollection;
    this.additionalItems   = new ArrayList<T_ITEM>();
    this.selectionHandler = new SelectionHandlerWithAdditionalItems<T_ITEM>(baseCollection, additionalItems);
    this.modificationHandler = new ModificationHandlerWithAdditionalItems();

    // On each query parameter change the locally cached current page needs to be cleared.
    PropertyChangeListener resetItemsOnPageListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        itemsOnPage = null;
      }
    };
    getQueryParams().addPropertyChangeListener(QueryParams.PROP_EFFECTIVE_FILTER, resetItemsOnPageListener);
    getQueryParams().addPropertyChangeListener(QueryParams.PROP_EFFECTIVE_SORT_ORDER, resetItemsOnPageListener);
  }

  @Override
  public ModificationHandler<T_ITEM> getModificationHandler() {
    return modificationHandler;
  }

  @Override
  public Modifications<T_ITEM> getModifications() {
    return modificationHandler.getModifications();
  }

  /**
   * Provides the set of all transient items.
   *
   * @return the transient item set.
   */
  public List<T_ITEM> getAdditionalItems() {
    return additionalItems;
  }

  /**
   * Clears all transient items.
   */
  public void clearAdditionalItems() {
    additionalItems.clear();
    itemsOnPage = null;
  }

  /**
   * Provides the base collection without the set of additional items.
   *
   * @return the base collection.
   */
  public PageableCollection2<T_ITEM> getBaseCollection() {
    return baseCollection;
  }

  @Override
  public QueryParams getQueryParams() {
    return baseCollection.getQueryParams();
  }

  @Override
  public QueryOptions getQueryOptions() {
    return baseCollection.getQueryOptions();
  }

  //
  @Override
  public List<T_ITEM> getItemsOnPage() {
    if (itemsOnPage == null) {
      int pageSize = getPageSize();
      int numOfPagesFilledByBaseCollectionPages = (int)baseCollection.getNumOfItems() / pageSize;

      if (additionalItems.isEmpty() ||
          (currentPageIdx <= numOfPagesFilledByBaseCollectionPages)) {
        itemsOnPage = baseCollection.getItemsOnPage();
      } else {
        boolean mixedPage = (currentPageIdx == numOfPagesFilledByBaseCollectionPages+1) &&
                            (baseCollection.getNumOfItems() % pageSize) != 0;
        if (mixedPage) {
          List<T_ITEM> list = new ArrayList<T_ITEM>(baseCollection.getItemsOnPage());
          for (T_ITEM i : additionalItems) {
            if (list.size() >= pageSize) {
              break;
            }
            list.add(i);
          }
          itemsOnPage = list;
        } else {
          long firstItemIdx = (currentPageIdx-1) * pageSize;
          int offset = (int)(firstItemIdx - baseCollection.getNumOfItems());
          itemsOnPage = new ArrayList<T_ITEM>(ListUtil.subListPage(additionalItems, offset, pageSize));
        }
      }
    }

    return itemsOnPage;
  }

  @Override
  public int getPageSize() {
    return baseCollection.getPageSize();
  }

  @Override
  public void setPageSize(int newSize) {
    baseCollection.setPageSize(newSize);
    itemsOnPage = null;
  }

  @Override
  public int getCurrentPageIdx() {
    return currentPageIdx;
  }

  @Override
  public void setCurrentPageIdx(int pageIdx) {
    currentPageIdx = pageIdx;
    baseCollection.setCurrentPageIdx(pageIdx);
    itemsOnPage = null;
  }

  @Override
  public long getNumOfItems() {
    return baseCollection.getNumOfItems() + additionalItems.size();
  }

  @Override
  public Iterator<T_ITEM> iterator() {
    return new CombinedIterator<T_ITEM>(baseCollection.iterator(), additionalItems.iterator());
  }

  @Override
  public SelectionHandler<T_ITEM> getSelectionHandler() {
    return selectionHandler;
  }

  @Override
  public Selection<T_ITEM> getSelection() {
    return selectionHandler.getSelection();
  }

  @Override
  public void clearCaches() {
    baseCollection.clearCaches();
    itemsOnPage = null;
  }

  /**
   * Adds/removes directly on the additionalItems collection.<br>
   * Delegates modifications to other items to the backing collection.
   */
  class ModificationHandlerWithAdditionalItems implements ModificationHandler<T_ITEM> {

    private CollectionWithAdditionalItemsModifications modifications = new CollectionWithAdditionalItemsModifications();

    @Override
    public void addItem(T_ITEM item) {
      assert item != null;
      additionalItems.add(item);
      // the page item cache needs to be updated if the current is the last page.
      if (getCurrentPageIdx() == PageableCollectionUtil2.getNumOfPages(PageableCollectionWithAdditionalItems.this)) {
        itemsOnPage = null;
      }
    }

    @Override
    public void updateItem(T_ITEM item) {
      assert item != null;
      if (!additionalItems.contains(item)) {
        baseCollection.getModificationHandler().updateItem(item);
      }
    };

    @Override
    public boolean removeSelectedItems() {
      SelectionWithAdditionalItems<T_ITEM> items = (SelectionWithAdditionalItems<T_ITEM>) selectionHandler.getSelection();

      // first do the operation that can fail:
      if (!baseCollection.getModificationHandler().removeSelectedItems()) {
        return false;
      }
      additionalItems.removeAll(items.getAdditionalSelectedItems());
      selectionHandler.selectAll(false);
      clearCaches();
      return true;
    }

    @Override
    public void clearRegisteredModifications() {
      baseCollection.getModificationHandler().clearRegisteredModifications();
      additionalItems.clear();
      modifications = new CollectionWithAdditionalItemsModifications();
    }

    @Override
    public Modifications<T_ITEM> getModifications() {
      return modifications;
    }

    /**
     * Represents the modifications of the base collection and of the set of additional items.
     */
    class CollectionWithAdditionalItemsModifications implements Modifications<T_ITEM> {

      @Override
      public boolean isModified() {
        return !additionalItems.isEmpty() ||
               baseCollection.getModificationHandler().getModifications().isModified();
      }

      @Override
      public Collection<T_ITEM> getAddedItems() {
        return additionalItems;
      }

      @Override
      public Collection<T_ITEM> getUpdatedItems() {
        return baseCollection.getModificationHandler().getModifications().getUpdatedItems();
      }

      @Override
      public Selection<T_ITEM> getRemovedItems() {
        return baseCollection.getModificationHandler().getModifications().getRemovedItems();
      }
    };
  }

}
