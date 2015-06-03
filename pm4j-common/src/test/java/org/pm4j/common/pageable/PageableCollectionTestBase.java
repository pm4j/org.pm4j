package org.pm4j.common.pageable;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.junit.Before;
import org.junit.Test;
import org.pm4j.common.itemnavigator.ItemNavigator;
import org.pm4j.common.pageable.inmem.InMemCollectionItemNavigator;
import org.pm4j.common.query.CompOpStartsWith;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryExpr;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.SortOrder;
import org.pm4j.common.query.QueryUtil;
import org.pm4j.common.query.filter.FilterDefinition;
import org.pm4j.common.selection.SelectMode;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.util.beanproperty.PropertyAndVetoableChangeListener;
import org.pm4j.common.util.collection.IterableUtil;

/**
 * An abstract test that checks the algorithms that should work for all kinds of
 * {@link PageableCollection}.
 *
 * @author Olaf Boede
 */
public abstract class PageableCollectionTestBase<T> {

  protected PageableCollection<T> collection;
  protected SortOrder nameSortOrder;
  protected TestPropertyChangeListener pclAdd;
  protected TestPropertyChangeListener pclUpdate;
  protected TestPropertyChangeListener pclRemove;
  protected TestPropertyChangeListener pclPageSize;
  protected TestPropertyChangeListener pclPageIdx;


  /** Needs to be implemented by the concrete test classes. */
  protected abstract PageableCollection<T> makePageableCollection(String... strings);

  protected QueryOptions getQueryOptions() {
    QueryOptions options = new QueryOptions();
    options.addSortOrder(Bean.ATTR_NAME);
    options.addFilterCompareDefinition(new FilterDefinition(Bean.ATTR_NAME, new CompOpStartsWith()));
    return options;
  }

  /** A default implementation, which may differ for the PM collection test.
   *  There we have to navigate from the attribute to the value too. */
  private QueryExpr getFilterNameStartsWith(String startString) {
    return QueryUtil.getFilter(collection.getQueryOptions(), "name", CompOpStartsWith.NAME, startString);
  }

  protected SortOrder getOrderByName() {
    return new SortOrder(Bean.ATTR_NAME);
  }

  protected abstract T createItem(int id, String name);


  @Before
  public void setUp() {
    collection = makePageableCollection(" ", "a", "b", "c", "d", "e", "f");

    collection.setPageSize(2);
    nameSortOrder = getOrderByName();

    // to get more test coverage: perform all tests with a single deleted item.
    assertEquals("[ , a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());
    assertEquals("[ , a]", collection.getItemsOnPage().toString());
    collection.getSelectionHandler().select(true, collection.getItemsOnPage().get(0));
    assertEquals("[ ]", IterableUtil.shallowCopy(collection.getSelection()).toString());
    assertTrue(collection.getModificationHandler().removeSelectedItems());
    assertTrue(collection.getModificationHandler().getModifications().isModified());
    assertEquals("[ ]", IterableUtil.shallowCopy(collection.getModificationHandler().getModifications().getRemovedItems()).toString());
    addAllTestPropertyChangeListener();
  }

  private void addAllTestPropertyChangeListener() {
    collection.addPropertyChangeListener(PageableCollection.EVENT_ITEM_ADD, pclAdd = new TestPropertyChangeListener());
    collection.addPropertyChangeListener(PageableCollection.EVENT_ITEM_UPDATE, pclUpdate = new TestPropertyChangeListener());
    collection.addPropertyAndVetoableListener(PageableCollection.EVENT_REMOVE_SELECTION, pclRemove = new TestPropertyChangeListener());
    collection.addPropertyChangeListener(PageableCollection.PROP_PAGE_IDX, pclPageIdx = new TestPropertyChangeListener());
    collection.addPropertyChangeListener(PageableCollection.PROP_PAGE_SIZE, pclPageSize = new TestPropertyChangeListener());
  }

  @Test
  public void testFullCollectionItersationResult() {
    assertEquals("[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());
  }

  protected ItemNavigator<T> getItemNavigator() {
    return new InMemCollectionItemNavigator<T>(collection.getSelectionHandler().getSelection());
  }

  @Test
  public void testItemNavigator() {
    // multi selection for this test
    collection.getSelectionHandler().setSelectMode(SelectMode.MULTI);

    // no item selected
    ItemNavigator<T> n = getItemNavigator();
    assertEquals(0, n.getNumOfItems());

    // first item selected
    collection.getSelectionHandler().select(true, collection.getItemsOnPage().get(0));
    n = getItemNavigator();
    assertEquals(1, n.getNumOfItems());
    assertEquals("a", n.navigateTo(0).toString());

    // first two items selected
    collection.getSelectionHandler().select(true, collection.getItemsOnPage().get(1));
    n = getItemNavigator();
    assertEquals(2, n.getNumOfItems());
    // FIXME olaf: the selection sort order is not yet predictable.
//    assertEquals("a", n.navigateTo(0).toString());
//    assertEquals("b", n.navigateTo(1).toString());

    // all items selected
    collection.getSelectionHandler().selectAll(true);
    n = getItemNavigator();
    assertEquals(6, n.getNumOfItems());
    // FIXME olaf: the selection sort order is not yet predictable.
//    assertEquals("a", n.navigateTo(0).toString());
//    assertEquals("b", n.navigateTo(1).toString());
  }

  @Test
  public void testSwitchQueryExecOffAndOn() {
    assertEquals("[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());

    collection.getQueryParams().setExecQuery(false);
    assertEquals(0, collection.getNumOfItems());
    assertEquals("[]", IterableUtil.shallowCopy(collection).toString());

    collection.getQueryParams().setExecQuery(true);
    assertEquals(6, collection.getNumOfItems());
    assertEquals("[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());

    assertEquals("Add event count", 0, pclAdd.getPropChangeEventCount());
    assertEquals("Update event count", 0, pclUpdate.getPropChangeEventCount());
    assertEquals("Remove event count", 0, pclRemove.getPropChangeEventCount());
    assertEquals("Set page index event count", 0, pclPageIdx.getPropChangeEventCount());
    assertEquals("Set page size event count", 0, pclPageSize.getPropChangeEventCount());
  }

  @Test
  public void testItemsOnPage() {
    assertEquals(0, collection.getPageIdx());
    assertEquals("[a, b]", collection.getItemsOnPage().toString());

    collection.setPageIdx(1);
    assertEquals("[c, d]", collection.getItemsOnPage().toString());

    collection.setPageIdx(2);
    assertEquals("[e, f]", collection.getItemsOnPage().toString());

    collection.setPageSize(3);
    collection.setPageIdx(0);
    assertEquals("[a, b, c]", collection.getItemsOnPage().toString());

    collection.setPageIdx(2);
    assertEquals("[]", collection.getItemsOnPage().toString());
    collection.setPageIdx(3);
    assertEquals("[]", collection.getItemsOnPage().toString());

    assertEquals("Add event count", 0, pclAdd.getPropChangeEventCount());
    assertEquals("Update event count", 0, pclUpdate.getPropChangeEventCount());
    assertEquals("Remove event count", 0, pclRemove.getPropChangeEventCount());
    assertEquals("Set page index event count", 5, pclPageIdx.getPropChangeEventCount());
    assertEquals("Set page size event count", 1, pclPageSize.getPropChangeEventCount());
  }

  @Test
  public void testSortItems() {
    assertEquals("Initial (unsorted) sort order", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());

    collection.getQueryParams().setSortOrder(nameSortOrder);
    assertEquals("Ascending sort order", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());

    collection.getQueryParams().setSortOrder(nameSortOrder.getReverseSortOrder());
    assertEquals("Descending sort order", "[f, e, d, c, b, a]", IterableUtil.shallowCopy(collection).toString());

    collection.getQueryParams().setSortOrder(null);
    assertEquals("Initial (unsorted) sort order again", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());

    assertEquals("Add event count", 0, pclAdd.getPropChangeEventCount());
    assertEquals("Update event count", 0, pclUpdate.getPropChangeEventCount());
    assertEquals("Remove event count", 0, pclRemove.getPropChangeEventCount());
    assertEquals("Set page index event count", 0, pclPageIdx.getPropChangeEventCount());
    assertEquals("Set page size event count", 0, pclPageSize.getPropChangeEventCount());
  }

  @Test
  public void testDefaultSortOrder() {
    collection.setPageIdx(2);
    assertEquals("Initial (unsorted) sort order", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());
    assertEquals("[e, f]", collection.getItemsOnPage().toString());

    collection.getQueryParams().setDefaultSortOrder(nameSortOrder.getReverseSortOrder());
    assertEquals("New default: Descending sort order", "[f, e, d, c, b, a]", IterableUtil.shallowCopy(collection).toString());
    assertEquals("[b, a]", collection.getItemsOnPage().toString());

    collection.getQueryParams().setSortOrder(nameSortOrder);
    assertEquals("Sort in ascending order", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());
    assertEquals("[e, f]", collection.getItemsOnPage().toString());

    collection.getQueryParams().setSortOrder(null);
    assertEquals("Sorted in descending default sort order again.", "[f, e, d, c, b, a]", IterableUtil.shallowCopy(collection).toString());
    assertEquals("[b, a]", collection.getItemsOnPage().toString());

    collection.getQueryParams().setDefaultSortOrder(null);
    assertEquals("Initial (unsorted) sort order again.", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());
    assertEquals("[e, f]", collection.getItemsOnPage().toString());

    assertEquals("Add event count", 0, pclAdd.getPropChangeEventCount());
    assertEquals("Update event count", 0, pclUpdate.getPropChangeEventCount());
    assertEquals("Remove event count", 0, pclRemove.getPropChangeEventCount());
    assertEquals("Set page index event count", 1, pclPageIdx.getPropChangeEventCount());
    assertEquals("Set page size event count", 0, pclPageSize.getPropChangeEventCount());
  }

  @Test
  public void testFilterItems() {
    collection.getQueryParams().setQueryExpression(getFilterNameStartsWith("b"));
    assertEquals("Filtered item set.", "[b]", IterableUtil.shallowCopy(collection).toString());

    collection.getQueryParams().setQueryExpression(null);
    assertEquals("We get all items after resetting the filter.", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(collection).toString());

    assertEquals("Add event count", 0, pclAdd.getPropChangeEventCount());
    assertEquals("Update event count", 0, pclUpdate.getPropChangeEventCount());
    assertEquals("Remove event count", 0, pclRemove.getPropChangeEventCount());
    assertEquals("Set page index event count", 0, pclPageIdx.getPropChangeEventCount());
    assertEquals("Set page size event count", 0, pclPageSize.getPropChangeEventCount());
  }

  @Test
  public void testSelectItems() {
    collection.getSelectionHandler().setSelectMode(SelectMode.MULTI);
    assertEquals(SelectMode.MULTI, collection.getSelectionHandler().getSelectMode());
    assertEquals(0, collection.getSelectionHandler().getSelection().getSize());
    assertTrue(collection.getSelectionHandler().selectAll(true));
    assertEquals(6, collection.getSelectionHandler().getSelection().getSize());
    assertTrue(collection.getSelectionHandler().select(false, collection.getItemsOnPage().get(0)));
    assertEquals(5, collection.getSelectionHandler().getSelection().getSize());
    assertTrue(collection.getSelectionHandler().invertSelection());
    assertEquals(1, collection.getSelectionHandler().getSelection().getSize());

    assertEquals("Add event count", 0, pclAdd.getPropChangeEventCount());
    assertEquals("Update event count", 0, pclUpdate.getPropChangeEventCount());
    assertEquals("Remove event count", 0, pclRemove.getPropChangeEventCount());
    assertEquals("Set page index event count", 0, pclPageIdx.getPropChangeEventCount());
    assertEquals("Set page size event count", 0, pclPageSize.getPropChangeEventCount());
  }

  @Test
  public void testSelectInvertAndDeselect() {
    collection.getSelectionHandler().setSelectMode(SelectMode.MULTI);
    T firstItem = collection.getItemsOnPage().get(0);
    T secondItem = collection.getItemsOnPage().get(1);

    assertEquals("initially no item is selected", 0, collection.getSelection().getSize());

    // select the first item
    assertTrue("select the first item", collection.getSelectionHandler().select(true, firstItem) );
    assertEquals("one item is selected", 1, collection.getSelection().getSize());
    assertEquals("the first item is selected", true, collection.getSelection().contains(firstItem));
    assertEquals("the second item is not selected", false, collection.getSelection().contains(secondItem));

    // invert the selection
    assertEquals("invert the selection", true, collection.getSelectionHandler().invertSelection());
    assertEquals("the first item is not selected", false, collection.getSelection().contains(firstItem));
    assertEquals("the second item is selected", true, collection.getSelection().contains(secondItem));
    assertEquals("5 items are selected", 5, collection.getSelection().getSize());

    // deselect the second item
    assertEquals("deselect the second item", true, collection.getSelectionHandler().select(false, secondItem));
    assertEquals("the first item is not selected", false, collection.getSelection().contains(firstItem));
    assertEquals("the second item is not selected", false, collection.getSelection().contains(secondItem));
    assertEquals("4 items are selected", 4, collection.getSelection().getSize());

    // invert the selection again
    assertEquals("invert the selection", true, collection.getSelectionHandler().invertSelection());
    assertEquals("the first item is selected", true, collection.getSelection().contains(firstItem));
    assertEquals("the second item is selected", true, collection.getSelection().contains(secondItem));
    assertEquals("2 items are selected", 2, collection.getSelection().getSize());

  }
  
  /**
   * Tests {@link SelectMode#SINGLE} scenarios
   */
  @Test
  public void testSingleSelection() {
    collection.getSelectionHandler().setSelectMode(SelectMode.SINGLE);

    //select
    collection.getSelectionHandler().select(true, collection.getItemsOnPage().get(0));
    assertEquals(true, collection.getSelectionHandler().getSelection().getSize() == 1);

    //try unselect item which is not selected and expect selection not changed
    collection.getSelectionHandler().select(false, collection.getItemsOnPage().get(1));
    assertEquals(true, collection.getSelectionHandler().getSelection().getSize() == 1);

    //try unselect previously selected item and expect selection to be gone
    collection.getSelectionHandler().select(false, collection.getItemsOnPage().get(0));
    assertEquals(true, collection.getSelectionHandler().getSelection().getSize() == 0);
  }
  
  /**
   * Checks whether table selection applies the same ordering as the table
   */
  @Test
  public void testSelectionOrdering() {
    
    // select all items and check for proper ordering
    collection.setPageSize(3);
    collection.getQueryParams().setSortOrder(nameSortOrder.getReverseSortOrder());
    collection.getSelectionHandler().setSelectMode(SelectMode.MULTI);
    collection.getSelectionHandler().selectAll(true);
    
    assertEquals(true, collection.getSelectionHandler().getSelection().getSize() > 1);
    assertEquals("Selection sorted according to backing collection order", "[f, e, d, c, b, a]", IterableUtil.asCollection(collection.getSelection()).toString());

    //clear selection and select only 2 first items
    collection.getSelectionHandler().selectAll(false);
    collection.getQueryParams().setSortOrder(nameSortOrder);
    @SuppressWarnings("unchecked")
    List<T> itemsToSelect = Arrays.asList(collection.getItemsOnPage().get(1), collection.getItemsOnPage().get(2));
    collection.getSelectionHandler().select(true, itemsToSelect);
    collection.getSelectionHandler().select(true, collection.getItemsOnPage().get(0)); 

    assertEquals("Selection sorted according to backing collection order", "[a, b, c]", IterableUtil.asCollection(collection.getSelection()).toString());
  }

  @Test
  public void testAddItem() {
    // preconditions
    assertEquals("Initial collection size", 6L, collection.getNumOfItems());
    assertEquals(0, collection.getModifications().getAddedItems().size());
    assertEquals(0, collection.getModifications().getUpdatedItems().size());
    assertEquals("See setUp(): the initial remove item.", 1L, collection.getModifications().getRemovedItems().getSize());

    // add the item
    T newItem = createItem(1001, "hi");
    collection.getModificationHandler().addItem(newItem);
    assertEquals("New collection size", 7L, collection.getNumOfItems());
    assertEquals("Collection after add", "[a, b, c, d, e, f, hi]", IterableUtil.shallowCopy(collection).toString());

    assertEquals(1, collection.getModifications().getAddedItems().size());
    assertTrue(collection.getModifications().getAddedItems().contains(newItem));
    assertEquals(0, collection.getModifications().getUpdatedItems().size());
    assertEquals("See setUp(): the initial remove item.", 1L, collection.getModifications().getRemovedItems().getSize());

    // select and de-select the added item
    collection.getSelectionHandler().select(true, newItem);
    assertEquals(1L, collection.getSelection().getSize());

    collection.getSelectionHandler().select(false, newItem);
    assertEquals(0L, collection.getSelection().getSize());

    // now iterate over the set of pages:
    assertEquals("[a, b]", collection.getItemsOnPage().toString());
    collection.setPageIdx(2);
    assertEquals("[e, f]", collection.getItemsOnPage().toString());
    collection.setPageIdx(3);
    assertEquals("[hi]", collection.getItemsOnPage().toString());
    // remove an item to be able to test a mixed page containing persistent and added items:
    collection.setPageIdx(2);
    collection.getSelectionHandler().select(true, collection.getItemsOnPage().get(0));
    collection.getModificationHandler().removeSelectedItems();
    assertEquals("[f, hi]", collection.getItemsOnPage().toString());

    // check number of expected events and service calls
    assertEquals("Add event count", 1, pclAdd.getPropChangeEventCount());
    assertEquals("Update event count", 0, pclUpdate.getPropChangeEventCount());
    assertEquals("Remove event count", 1, pclRemove.getPropChangeEventCount());
    assertEquals("Set page index event count", 3, pclPageIdx.getPropChangeEventCount());
    assertEquals("Set page size event count", 0, pclPageSize.getPropChangeEventCount());
  }

  @Test
  public void testAddItemToEmptyCollection() {

    // setup empty pageable collection
    collection = makePageableCollection((String[]) null);
    addAllTestPropertyChangeListener();

    // preconditions
    assertEquals("Empty collection size", 0L, collection.getNumOfItems());
    assertEquals(0, collection.getModifications().getAddedItems().size());
    assertEquals(0, collection.getModifications().getUpdatedItems().size());
    assertEquals(0, collection.getModifications().getRemovedItems().getSize());

    // add the item
    T newItem = createItem(1001, "hi");
    collection.getModificationHandler().addItem(newItem);
    assertEquals("New collection size", 1L, collection.getNumOfItems());
    assertEquals("Collection after add", "[hi]", IterableUtil.shallowCopy(collection).toString());

    assertEquals(1, collection.getModifications().getAddedItems().size());
    assertTrue(collection.getModifications().getAddedItems().contains(newItem));
    assertEquals(0, collection.getModifications().getUpdatedItems().size());

    assertEquals(0, collection.getModifications().getRemovedItems().getSize());

    // select and de-select the added item
    collection.getSelectionHandler().select(true, newItem);
    assertEquals(1L, collection.getSelection().getSize());

    collection.getSelectionHandler().select(false, newItem);
    assertEquals(0L, collection.getSelection().getSize());

    // check number of expected events and service calls
    assertEquals("Add event count", 1, pclAdd.getPropChangeEventCount());
    assertEquals("Update event count", 0, pclUpdate.getPropChangeEventCount());
    assertEquals("Remove event count", 0, pclRemove.getPropChangeEventCount());
    assertEquals("Set page index event count", 0, pclPageIdx.getPropChangeEventCount());
    assertEquals("Set page size event count", 0, pclPageSize.getPropChangeEventCount());
  }

  @Test
  public void testAddItemInMultiSelectMode() {
    collection.getSelectionHandler().setSelectMode(SelectMode.MULTI);
    testAddItem();
  }


  // TODO: testUpdateItem()

  @Test
  public void testRemoveItems() {
    assertEquals("Initial collection size", 6L, collection.getNumOfItems());
    assertEquals(0, collection.getModifications().getAddedItems().size());
    assertEquals(0, collection.getModifications().getUpdatedItems().size());
    assertEquals("See setUp(): the initial remove item.", 1L, collection.getModifications().getRemovedItems().getSize());

    collection.getSelectionHandler().setSelectMode(SelectMode.MULTI);
    collection.getSelectionHandler().select(true, collection.getItemsOnPage());
    assertEquals(2L, collection.getSelection().getSize());

    collection.getModificationHandler().removeSelectedItems();
    assertEquals("New collection size", 4L, collection.getNumOfItems());
    assertEquals("Collection after add", "[c, d, e, f]", IterableUtil.shallowCopy(collection).toString());
    assertTrue(collection.getSelection().isEmpty());

    assertEquals(0, collection.getModifications().getAddedItems().size());
    assertEquals(0, collection.getModifications().getUpdatedItems().size());
    assertEquals("Initial removal + the two now ones.", 3L, collection.getModifications().getRemovedItems().getSize());

    assertEquals("Add event count", 0, pclAdd.getPropChangeEventCount());
    assertEquals("Update event count", 0, pclUpdate.getPropChangeEventCount());
    assertEquals("Remove veto event count", 1, pclRemove.getVetoEventCount());
    assertEquals("Remove event count", 1, pclRemove.getPropChangeEventCount());
    assertEquals("Set page index event count", 0, pclPageIdx.getPropChangeEventCount());
    assertEquals("Set page size event count", 0, pclPageSize.getPropChangeEventCount());
  }

  @Test
  public void testRemoveOfAddedAndUpdatedItems() {
    collection.getSelectionHandler().setSelectMode(SelectMode.MULTI);
    T updatedItem = collection.getItemsOnPage().get(0);
    collection.getModificationHandler().registerUpdatedItem(updatedItem, true);
    collection.getModificationHandler().addItem(createItem(1002, "added item"));

    assertEquals(1, collection.getModifications().getAddedItems().size());
    assertEquals(1, collection.getModifications().getUpdatedItems().size());
    assertEquals("See setUp(): the initial remove item.", 1L, collection.getModifications().getRemovedItems().getSize());


    collection.getSelectionHandler().selectAll(true);
    assertEquals(7L, collection.getSelection().getSize());

    collection.getModificationHandler().removeSelectedItems();
    assertEquals("New collection size", 0L, collection.getNumOfItems());
    assertEquals("Collection after remove all.", "[]", IterableUtil.shallowCopy(collection).toString());
    assertTrue(collection.getSelection().isEmpty());

    assertEquals(0, collection.getModifications().getAddedItems().size());
    assertEquals(0, collection.getModifications().getUpdatedItems().size());
    assertEquals("The added item is not part of the deleted items: 7 instead of 8...",
        7L, collection.getModifications().getRemovedItems().getSize());

    assertEquals("Add event count", 1, pclAdd.getPropChangeEventCount());
    assertEquals("Update event count", 1, pclUpdate.getPropChangeEventCount());
    assertEquals("Remove veto event count", 1, pclRemove.getVetoEventCount());
    assertEquals("Remove event count", 1, pclRemove.getPropChangeEventCount());
    assertEquals("Set page index event count", 0, pclPageIdx.getPropChangeEventCount());
    assertEquals("Set page size event count", 0, pclPageSize.getPropChangeEventCount());
  }

  @Test
  public void testIterateAllSelectionWithBlockSize3() {
    Selection<T> allItemsSelection = collection.getSelectionHandler().getAllItemsSelection();
    allItemsSelection.setIteratorBlockSizeHint(3);
    assertEquals("All items iteration result", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(allItemsSelection).toString());
  }

  @Test
  public void testIterateAllSelectionWithBlockSize6() {
    Selection<T> allItemsSelection = collection.getSelectionHandler().getAllItemsSelection();
    allItemsSelection.setIteratorBlockSizeHint(6);
    assertEquals("All items iteration result", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(allItemsSelection).toString());
  }

  @Test
  public void testIteratePositiveSelectionOf3ItemsWithBlockSize2() {
    collection.getSelectionHandler().setSelectMode(SelectMode.MULTI);
    // first item
    collection.getSelectionHandler().select(true, collection.getItemsOnPage().get(0));
    collection.setPageIdx(1L);
    // third and fourth item
    collection.getSelectionHandler().select(true, collection.getItemsOnPage().get(0));
    collection.getSelectionHandler().select(true, collection.getItemsOnPage().get(1));

    Selection<T> selection = collection.getSelection();
    selection.setIteratorBlockSizeHint(2);
    resetCallCounter();
    assertEquals("Three selected items iteration result", "[a, c, d]", IterableUtil.shallowCopy(selection).toString());
  }

  @Test
  public void testIterateAllSelectionMinusOneWithBlockSize2() {
    collection.getSelectionHandler().setSelectMode(SelectMode.MULTI);
    collection.getSelectionHandler().selectAll(true);
    collection.getSelectionHandler().select(false, collection.getItemsOnPage().get(1));
    Selection<T> selection = collection.getSelection();
    selection.setIteratorBlockSizeHint(2);
    resetCallCounter();
    assertEquals("All items minus one selection iteration result", "[a, c, d, e, f]", IterableUtil.shallowCopy(selection).toString());
  }

  @Test
  public void testIterateEmptySelection() {
    resetCallCounter();
    Selection<T> selection = collection.getSelection();
    assertEquals("Empty selection returns no items.", "[]", IterableUtil.shallowCopy(selection).toString());
  }

  @Test
  public void testGetNumOfItems() {
    resetCallCounter();
    long numOfItems = collection.getNumOfItems();
    assertEquals("The number of items must be six.", 6, numOfItems);
  }
  
  /**
   * Test to select and deselect all items of {@link MyTablePm}. When selecting all items the old selected item set 
   * should be empty and the new selected item set should have the three items [b, c, a] selected. When deselecting
   * all items the old selected item set should have the three items [b, c, a] selected and the new selected item set 
   * should be empty. 
   */
  @Test
  public void testChangeSelection() {
    LastChangeReportingChangeListener<T> changeListener = new LastChangeReportingChangeListener<T>();
    
    collection.getSelectionHandler().addPropertyChangeListener(SelectionHandler.PROP_SELECTION, changeListener);
    
    collection.getSelectionHandler().setSelectMode(SelectMode.MULTI);
    collection.getSelectionHandler().selectAll(true);
    assertEquals("Empty selection returns no items.", "[]", IterableUtil.shallowCopy(changeListener.oldValue).toString());
    assertEquals("All items selected.", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(changeListener.newValue).toString());
    
    collection.getSelectionHandler().selectAll(false);
    assertEquals("All items selected.", "[a, b, c, d, e, f]", IterableUtil.shallowCopy(changeListener.oldValue).toString());
    assertEquals("Empty selection returns no items.", "[]", IterableUtil.shallowCopy(changeListener.newValue).toString());
  }

  // -- Test infrastructure --

  /** Specific test classes reset their call counters when this gets called. */
  protected void resetCallCounter() {
  }

  protected static List<Bean> makeBeans(String... strings) {
    List<Bean> list = new ArrayList<Bean>();
    if (strings != null) {
      int id = 0;
      for (String s : strings) {
        list.add(new Bean(++id, s));
      }
    }
    return list;
  }
  
public static class LastChangeReportingChangeListener<T> implements PropertyChangeListener {
    
    private Selection<T> oldValue;
    private Selection<T> newValue;

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
      oldValue = (Selection<T>) evt.getOldValue();
      newValue = (Selection<T>) evt.getNewValue();
    }   
  }


  public static class Bean {
    public Integer id;
    public final String name;
    public static final QueryAttr ATTR_ID = new QueryAttr("id", String.class);
    public static final QueryAttr ATTR_NAME = new QueryAttr("name", String.class);

    public Bean(String name) {
      this.name = name;
    }

    public Bean(int id, String name) {
      this(name);
      this.id = id;
    }

    @Override
    public String toString() {
      return name;
    }

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof Bean)
              ? ObjectUtils.equals(id, ((Bean)obj).id)
              : super.equals(obj);
      }

    @Override
    public int hashCode() {
      return ObjectUtils.hashCode(id);
    }

  }

  // test helper

  static class TestPropertyChangeListener implements PropertyAndVetoableChangeListener {

    private List<PropertyChangeEvent> receivedPropChangeEvents = new ArrayList<PropertyChangeEvent>();
    private List<PropertyChangeEvent> receivedVetoableEvents = new ArrayList<PropertyChangeEvent>();

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      receivedPropChangeEvents.add(evt);
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
      receivedVetoableEvents.add(evt);
    }

    public int getPropChangeEventCount() {
      return receivedPropChangeEvents.size();
    }

    public int getVetoEventCount() {
      return receivedVetoableEvents.size();
    }

    public PropertyChangeEvent getLastObservedPropChangeEvent() {
      return receivedPropChangeEvents.isEmpty() ? null : receivedPropChangeEvents.get(receivedPropChangeEvents.size()-1);
    }

  }

}

