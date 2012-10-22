package org.pm4j.common.pageable;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.query.Query;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.selection.SelectionHandlerBase;
import org.pm4j.common.selection.SelectionHandlerWithItemSet;

/**
 * TODO OBOEDE Comment me!
 * 
 * @param <T_ITEM>
 * 
 * @author OBOEDE
 * @since GLOBE 1.4
 * 
 */
public class PageableCollectionWithTransientItems<T_ITEM> implements PageableCollection2<T_ITEM> {
    private static final Log LOG = LogFactory.getLog(SelectionHandlerWithItemSet.class);

    private final PageableCollection2<T_ITEM> baseCollection;
    private List<T_ITEM> transientItems = new ArrayList<T_ITEM>();
    private SelectionHandlerWithTransientItems selectionHandler = new SelectionHandlerWithTransientItems();

    /**
     * @param baseCollection
     */
    public PageableCollectionWithTransientItems(PageableCollection2<T_ITEM> baseCollection) {
        assert baseCollection != null;
        this.baseCollection = baseCollection;
    }

    /**
     * Adds a transient item to handle.
     * 
     * @param item
     *            the new item.
     */
    public void addTransientItem(T_ITEM item) {
        transientItems.add(item);
    }

    /**
     * Removes a transient item.
     * 
     * @param item
     *            the transient item to delete.
     */
    public void removeTransientItem(T_ITEM item) {
        transientItems.remove(item);
    }

    /**
     * Provides the set of all transient items.
     * 
     * @return the transient item set.
     */
    public List<T_ITEM> getTransientItems() {
        return transientItems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query getQuery() {
        return baseCollection.getQuery();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryOptions getQueryOptions() {
        return baseCollection.getQueryOptions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T_ITEM> getItemsOnPage() {
        if (transientItems.isEmpty()) {
            return baseCollection.getItemsOnPage();
        } else {
            List<T_ITEM> list = new ArrayList<T_ITEM>(baseCollection.getItemsOnPage());
            list.addAll(transientItems);
            return list;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPageSize() {
        return baseCollection.getPageSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPageSize(int newSize) {
        baseCollection.setPageSize(newSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCurrentPageIdx() {
        return baseCollection.getCurrentPageIdx();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentPageIdx(int pageIdx) {
        baseCollection.setCurrentPageIdx(pageIdx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getNumOfItems() {
        return baseCollection.getNumOfItems() + transientItems.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getUnfilteredItemCount() {
        return baseCollection.getUnfilteredItemCount() + transientItems.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T_ITEM> iterator() {
        return new IteratorWithAdditionalItems<T_ITEM>(baseCollection.iterator(), transientItems.iterator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SelectionHandler<T_ITEM> getSelectionHandler() {
        return selectionHandler;
    }

    class SelectionHandlerWithTransientItems extends SelectionHandlerBase<T_ITEM> {

        private SelectionWithTransientItems<T_ITEM> emptySelection = new SelectionWithTransientItems<T_ITEM>(baseCollection.getSelectionHandler().getSelection());

        /** The set of currently selected items. */
        private SelectionWithTransientItems<T_ITEM> selection = emptySelection;

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean select(boolean select, T_ITEM item) {
            if (transientItems.contains(item)) {
                SelectionWithTransientItems<T_ITEM> newSelection = new SelectionWithTransientItems<T_ITEM>(selection.baseSelection);
                newSelection.selectedTransientItems = new ArrayList<T_ITEM>(selection.selectedTransientItems);
                if (select) {
                    newSelection.selectedTransientItems.add(item);
                } else {
                    newSelection.selectedTransientItems.remove(item);
                }
                return setSelection(newSelection);
            } else {
                if (baseCollection.getSelectionHandler().select(select, item)) {
                    SelectionWithTransientItems<T_ITEM> newSelection = new SelectionWithTransientItems<T_ITEM>(baseCollection.getSelectionHandler().getSelection());
                    newSelection.selectedTransientItems = selection.selectedTransientItems;
                    return setSelection(newSelection);
                } else {
                    return false;
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean select(boolean select, Iterable<T_ITEM> items) {
            List<T_ITEM> newTransientItems = new ArrayList<T_ITEM>();
            List<T_ITEM> newBaseItems = new ArrayList<T_ITEM>();

            for (T_ITEM i : items) {
                if (transientItems.contains(i)) {
                    newTransientItems.add(i);
                } else {
                    newBaseItems.add(i);
                }
            }

            if (!newBaseItems.isEmpty()) {
                if (!baseCollection.getSelectionHandler().select(select, newBaseItems)) {
                    return false;
                }
            }

            SelectionWithTransientItems<T_ITEM> newSelection = new SelectionWithTransientItems<T_ITEM>(baseCollection.getSelectionHandler().getSelection());
            newSelection.selectedTransientItems = new ArrayList<T_ITEM>(selection.selectedTransientItems);

            for (T_ITEM i : newTransientItems) {
                if (select) {
                    newSelection.selectedTransientItems.add(i);
                } else {
                    newSelection.selectedTransientItems.remove(i);
                }
            }

            return setSelection(newSelection);
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean selectAll(boolean select) {
            if (!baseCollection.getSelectionHandler().selectAll(select)) {
                return false;
            }
            SelectionWithTransientItems<T_ITEM> newSelection = new SelectionWithTransientItems<T_ITEM>(baseCollection.getSelectionHandler().getSelection());
            newSelection.selectedTransientItems = select ? Collections.unmodifiableList(transientItems) : Collections.EMPTY_LIST;

            return setSelection(newSelection);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Selection<T_ITEM> getSelection() {
            return selection;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean setSelection(Selection<T_ITEM> selection) {
            Selection<T_ITEM> oldSelection = this.selection;
            Selection<T_ITEM> newSelection = selection;

            try {
                fireVetoableChange(PROP_SELECTION, oldSelection, newSelection);
                this.selection = (SelectionWithTransientItems<T_ITEM>) newSelection;
                firePropertyChange(PROP_SELECTION, oldSelection, newSelection);
                return true;
            } catch (PropertyVetoException e) {
                LOG.debug("Selection change rejected because of a property change veto.", e);
                return false;
            }
        }

    }

    static class SelectionWithTransientItems<T_ITEM> implements Selection<T_ITEM> {

        private final Selection<T_ITEM> baseSelection;
        private List<T_ITEM> selectedTransientItems;

        public SelectionWithTransientItems(Selection<T_ITEM> baseSelection) {
            assert baseSelection != null;
            this.baseSelection = baseSelection;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getSize() {
            return baseSelection.getSize() + selectedTransientItems.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(T_ITEM item) {
            return baseSelection.contains(item) || selectedTransientItems.contains(item);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<T_ITEM> iterator() {
            return new IteratorWithAdditionalItems<T_ITEM>(baseSelection.iterator(), selectedTransientItems.iterator());
        }

    }

    static class IteratorWithAdditionalItems<T_ITEM> implements Iterator<T_ITEM> {

        private final Iterator<T_ITEM> baseIterator;
        private final Iterator<T_ITEM> secondIterator;

        public IteratorWithAdditionalItems(Iterator<T_ITEM> baseIterator, Iterator<T_ITEM> secondIterator) {
            assert baseIterator != null;
            assert secondIterator != null;

            this.baseIterator = baseIterator;
            this.secondIterator = secondIterator;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            return baseIterator.hasNext() || secondIterator.hasNext();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public T_ITEM next() {
            return baseIterator.hasNext() ? baseIterator.next() : secondIterator.next();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
}
