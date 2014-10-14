package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.pm4j.common.pageable.PageableCollection.EVENT_REMOVE_SELECTION;
import static org.pm4j.tools.test.PmAssert.setValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.PageableCollectionUtil;
import org.pm4j.common.query.CompOpNotEquals;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryExprCompare;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.filter.FilterDefinition;
import org.pm4j.common.query.inmem.InMemSortOrder;
import org.pm4j.common.selection.SelectMode;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.pm.PmTable.UpdateAspect;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmTableCfg;
import org.pm4j.core.pm.annotation.PmTableColCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmInitApi;
import org.pm4j.core.pm.impl.PmTableColImpl;
import org.pm4j.core.pm.impl.PmTableImpl;
import org.pm4j.tools.test.RecordingPmEventListener;
import org.pm4j.tools.test.RecordingPropertyChangeListener;

public class PmTableTest {

  private TablePm myTablePm;
  private RecordingPmEventListener valueChangeEventListener = new RecordingPmEventListener();

  private List<RowBean> editedRowBeanList = new ArrayList<RowBean>(Arrays.asList(
      new RowBean("b", "a 'b'", 2),
      new RowBean("a", "an 'a'", 1),
      new RowBean("c", "a 'c'", 3)
  ));
  private List<RowBean> alternateRowBeanList = new ArrayList<RowBean>(Arrays.asList(
      new RowBean("b", "new b", 2),
      new RowBean("a", "new a", 1),
      new RowBean("c", "new c", 3)
  ));


  @Before
  public void setUp() {
    myTablePm = new TablePm(new PmConversationImpl()) {
      /** We use here an in-memory data table.
       * The table represents the items of the collection provided by this method. */
      @Override
      protected Collection<RowBean> getPmBeansImpl() {
        return editedRowBeanList;
      }
    };
    PmEventApi.addPmEventListener(myTablePm, PmEvent.VALUE_CHANGE, valueChangeEventListener);
    
    PmInitApi.ensurePmSubTreeInitialization(myTablePm);
  }

  @Test
  public void testTable() {
    assertEquals(3, myTablePm.getTotalNumOfPmRows());
    assertEquals("[a, b]", myTablePm.getRowPms().toString());
  }

  @Test
  public void testReReadCollectionAfterCallingUpdateTablePm() {
    myTablePm.setNumOfPageRowPms(10);
    assertEquals("[a, b, c]", myTablePm.getRowPms().toString());
    editedRowBeanList.add(new RowBean("d", "a d", 44));
    assertEquals("There is no value change event that informs the table about the change.", 3, myTablePm.getTotalNumOfPmRows());
    assertEquals("[a, b, c]", myTablePm.getRowPms().toString());

    myTablePm.updatePmTable(UpdateAspect.CLEAR_CHANGES);
    assertEquals("[a, b, c, d]", myTablePm.getRowPms().toString());
  }

  @Test
  public void testReReadCollectionAfterClearingCaches() {
    myTablePm.setNumOfPageRowPms(10);
    assertEquals("[a, b, c]", myTablePm.getRowPms().toString());
    editedRowBeanList.add(new RowBean("d", "a d", 44));
    assertEquals("[a, b, c]", myTablePm.getRowPms().toString());

    PmCacheApi.clearPmCache(myTablePm, CacheKind.VALUE);
    assertEquals("[a, b, c, d]", myTablePm.getRowPms().toString());
  }

  @Test
  public void testDisplayBeanListChanges() {
    assertEquals(3, myTablePm.getTotalNumOfPmRows());
    assertEquals("[a, b]", myTablePm.getRowPms().toString());

    // add an item to the represented list.
    editedRowBeanList.add(new RowBean("d", "d", 4));
    assertEquals("Number of items is cached. Thus the change will not be reflected automatically",
                 3, myTablePm.getTotalNumOfPmRows());

    PmCacheApi.clearPmCache(myTablePm);
    assertEquals("After clearing the PM value caches we see the data change.",
                 4, myTablePm.getTotalNumOfPmRows());
    assertEquals("First page content is not changed.", "[a, b]", myTablePm.getRowPms().toString());
    PageableCollectionUtil.navigateToLastPage(myTablePm.getPmPageableCollection());
    assertEquals("After a page switch the new item is visible.", "[c, d]", myTablePm.getRowPms().toString());

    // remove an item on the current page.
    editedRowBeanList.remove(2);
    assertEquals("[b, a, d]", editedRowBeanList.toString());
    assertEquals("Rerender the current page. It's not changed because the current page items are cached.",
                 "[c, d]", myTablePm.getRowPms().toString());

    PmCacheApi.clearPmCache(myTablePm);
    assertEquals("After an update call the table should display the current content.",
        "[d]", myTablePm.getRowPms().toString());
  }

  @Test
  public void testExchangeEqualBeanInBackingCollectionAndFireValueChangeOnParent() {
    assertEquals(3, myTablePm.getTotalNumOfPmRows());
    assertEquals("an 'a'", myTablePm.getRowPms().get(0).description.getValue());

    editedRowBeanList = alternateRowBeanList;

    // the table did not yet receive an event that informs about the backing collection change
    // it provides data from it's page cache.
    assertEquals("an 'a'", myTablePm.getRowPms().get(0).description.getValue());

    PmEventApi.firePmEvent(myTablePm, PmEvent.ALL_CHANGE_EVENTS);
    assertEquals("new a", myTablePm.getRowPms().get(0).description.getValue());
  }

  @Test
  public void testExchangeEqualBeanInBackingCollectionAndCallUpdatePmTable() {
    assertEquals(3, myTablePm.getTotalNumOfPmRows());
    assertEquals("an 'a'", myTablePm.getRowPms().get(0).description.getValue());

    editedRowBeanList = alternateRowBeanList;

    // the table did not yet receive an event that informs about the backing collection change
    // it provides data from it's page cache.
    assertEquals("an 'a'", myTablePm.getRowPms().get(0).description.getValue());

    myTablePm.updatePmTable(UpdateAspect.CLEAR_CHANGES);
    assertEquals("new a", myTablePm.getRowPms().get(0).description.getValue());
  }

  @Test
  public void testSortByName() {
    assertEquals("[a, b]", myTablePm.getRowPms().toString());
    setValue(myTablePm.name.getSortOrderAttr(), PmSortOrder.DESC);
    assertEquals("[c, b]", myTablePm.getRowPms().toString());
  }

  @Test
  @Ignore("Will be fixed with task 135890")
  public void testInitialSortOrderConfiguredInTableAnnotation() {
    assertEquals(PmSortOrder.ASC, myTablePm.name.getSortOrderAttr().getValue());
  }

  @Test
  public void testResetPmValuesClearsSortOrder() {
    setValue(myTablePm.name.getSortOrderAttr(), PmSortOrder.DESC);
    assertEquals("[c, b]", myTablePm.getRowPms().toString());
    myTablePm.resetPmValues();
    assertEquals("[a, b]", myTablePm.getRowPms().toString());
    assertEquals(PmSortOrder.ASC, myTablePm.name.getSortOrderAttr().getValue());
  }

  @Test
  public void testSortByDescriptionUsingCustomComparator() {
    assertEquals("[a, b]", myTablePm.getRowPms().toString());
    setValue(myTablePm.description.getSortOrderAttr(), PmSortOrder.ASC);
    assertEquals("[b, c]", myTablePm.getRowPms().toString());
  }

  @Test
  public void testFilterByDescriptionExists() {
    assertEquals("[a, b]", myTablePm.getRowPms().toString());
    FilterDefinition fd = getFilterDefinition("description");
    assertEquals(myTablePm.description.getPmTitle(), fd.getAttrTitle());
  }

  @Test
  public void testFilterByPathColumn() {
    assertEquals("[a, b]", myTablePm.getRowPms().toString());
    FilterDefinition fd = getFilterDefinition("pathColumn");
    assertEquals(myTablePm.pathColumn.getPmTitle(), fd.getAttrTitle());
  }

  @Test
  public void testExecFilter() {
    myTablePm.setNumOfPageRowPms(10);
    assertEquals("[a, b, c]", myTablePm.getRowPms().toString());
    QueryExprCompare notA = new QueryExprCompare(RowBean.ATTR_NAME, CompOpNotEquals.class, "a");
    myTablePm.getPmPageableBeanCollection().getQueryParams().setFilterExpression(notA);
    assertEquals("[b, c]", myTablePm.getRowPms().toString());
  }

  @Test
  @Ignore("TODO: DZA 136039: Vetoable property change does not work with FilterExpressions")
  public void testExecVetoFilter() {
    myTablePm.setNumOfPageRowPms(10);
    // all 3 rows should be visible
    assertEquals("[a, b, c]", myTablePm.getRowPms().toString());

    // add a filter change decorator which prevents any filter change
    myTablePm.addPmDecorator(new PmCommandDecorator() {
      @Override
      public boolean beforeDo(PmCommand cmd) {
        return false;
      }

      @Override
      public void afterDo(PmCommand cmd) {
      }
    }, PmTable.TableChange.FILTER);

    // Nevertheless try to register a filter that filters any 'a' 
    QueryExprCompare noA = new QueryExprCompare(RowBean.ATTR_NAME, CompOpNotEquals.class, "a");
    myTablePm.getPmPageableBeanCollection().getQueryParams().setFilterExpression(noA);

    // The added Filter does not apply because the filter change decorator prevents the application.
    assertEquals("[a, b, c]", myTablePm.getRowPms().toString());
  }

  @Test
  public void testRemoveFirstItemOnPmCollectionLevel() {
    PageableCollection<RowPm> pc = myTablePm.getPmPageableCollection();
    pc.getSelectionHandler().setSelectMode(SelectMode.SINGLE);
    assertEquals("[a, b]", myTablePm.getRowPms().toString());
    RecordingPropertyChangeListener deletePropertyChangeListener = new RecordingPropertyChangeListener();
    RecordingPropertyChangeListener deleteBeanPropertyChangeListener = new RecordingPropertyChangeListener();
    pc.addPropertyAndVetoableListener(EVENT_REMOVE_SELECTION, deletePropertyChangeListener);
    myTablePm.getPmPageableBeanCollection().addPropertyAndVetoableListener(EVENT_REMOVE_SELECTION, deleteBeanPropertyChangeListener);

    pc.getSelectionHandler().select(true, myTablePm.getRowPms().get(0));
    pc.getModificationHandler().removeSelectedItems();

    assertEquals("[b, c]", myTablePm.getRowPms().toString());
    assertEquals(1L, pc.getModifications().getRemovedItems().getSize());
    assertEquals(1, deletePropertyChangeListener.getNumOfPropertyChangesCalls());
    assertEquals(1, deletePropertyChangeListener.getNumOfVetoableChangesCalls());
    assertEquals(1, deleteBeanPropertyChangeListener.getNumOfPropertyChangesCalls());
    assertEquals(1, deleteBeanPropertyChangeListener.getNumOfVetoableChangesCalls());
    assertEquals(1, valueChangeEventListener.getCallCount());
  }

  @Test
  public void testRemoveFirstItemOnPmBeanCollectionLevel() {
    PageableCollection<RowBean> pc = myTablePm.getPmPageableBeanCollection();
    pc.getSelectionHandler().setSelectMode(SelectMode.SINGLE);
    assertEquals("[a, b]", myTablePm.getRowPms().toString());
    RecordingPropertyChangeListener deletePropertyChangeListener = new RecordingPropertyChangeListener();
    RecordingPropertyChangeListener deleteBeanPropertyChangeListener = new RecordingPropertyChangeListener();
    pc.addPropertyAndVetoableListener(EVENT_REMOVE_SELECTION, deletePropertyChangeListener);
    myTablePm.getPmPageableBeanCollection().addPropertyAndVetoableListener(EVENT_REMOVE_SELECTION, deleteBeanPropertyChangeListener);

    pc.getSelectionHandler().select(true, myTablePm.getRowPms().get(0).getPmBean());
    pc.getModificationHandler().removeSelectedItems();

    assertEquals("[b, c]", myTablePm.getRowPms().toString());
    assertEquals(1L, pc.getModifications().getRemovedItems().getSize());
    assertEquals(1, deletePropertyChangeListener.getNumOfPropertyChangesCalls());
    assertEquals(1, deletePropertyChangeListener.getNumOfVetoableChangesCalls());
    assertEquals(1, deleteBeanPropertyChangeListener.getNumOfPropertyChangesCalls());
    assertEquals(1, deleteBeanPropertyChangeListener.getNumOfVetoableChangesCalls());
    assertEquals(1, valueChangeEventListener.getCallCount());
  }

  @PmTableCfg(initialSortCol="name")
  @PmFactoryCfg(beanPmClasses=RowPm.class)
  public static class TablePm extends PmTableImpl<RowPm, RowBean> {

    /** A column with an annotation based filter definition. */
    @PmTableColCfg(sortable=PmBoolean.TRUE)
    public final PmTableCol name = new PmTableColImpl(this);

    /** A column with a method based filter definition. */
    @PmTableColCfg(filterType = String.class)
    public final PmTableCol description = new PmTableColImpl(this);

    /** A column with a filter annotation that defines . */
    public final PmTableCol counter = new PmTableColImpl(this);

    /** A column with a valuePath based attribute. No chance to derive the query attribute from the column name. */
    @PmTitleCfg(title="PathCol")
    @PmTableColCfg(filterType=String.class, queryAttrPath="name")
    public final PmTableCol pathColumn = new PmTableColImpl(this);

    /** Defines a page size of two items. */
    public TablePm(PmObject pmParent) {
      super(pmParent);
      setNumOfPageRowPms(2);
    }

    /**
     * Adds a custom comparator for the description column.
     */
    @Override
    protected QueryOptions getPmQueryOptions() {
      QueryOptions qo = super.getPmQueryOptions();
      qo.addSortOrder("description", new InMemSortOrder(new Comparator<RowBean>() {
          @Override
          public int compare(RowBean o1, RowBean o2) {
            return CompareUtil.compare(o1.description, o2.description);
          }
        }));
      return qo;
    }
  }

  @PmBeanCfg(beanClass=RowBean.class)
  public static class RowPm extends PmBeanImpl<RowBean> {
    public final PmAttrString name = new PmAttrStringImpl(this);
    public final PmAttrString description = new PmAttrStringImpl(this);
    public final PmAttrInteger counter = new PmAttrIntegerImpl(this);
    @PmAttrCfg(valuePath="pmBean.name")
    public final PmAttrString pathColumn = new PmAttrStringImpl(this);

    /** for debugging */
    @Override
    public String toString() {
      return getPmBean().toString();
    }
  }

  public static class RowBean {
    public static final QueryAttr ATTR_NAME = new QueryAttr("name", String.class);
    public static final QueryAttr ATTR_DESCRIPTION = new QueryAttr("description", String.class);
    public static final QueryAttr ATTR_COUNTER = new QueryAttr("counter", Integer.class);

    public String name;
    public String description;
    public Integer counter;

    public RowBean(String name, String description, int counter) {
      assert name != null;
      this.name = name;
      this.description = description;
      this.counter = counter;
    }

    /** for debugging */
    @Override
    public String toString() {
      return name;
    }

    @Override
    public boolean equals(Object obj) {
      return StringUtils.equals(this.name, ((RowBean)obj).name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }
  }

  private FilterDefinition getFilterDefinition(String colName) {
    QueryOptions qo = myTablePm.getPmPageableBeanCollection().getQueryOptions();
    List<FilterDefinition> compareDefinitions = qo.getCompareDefinitions();
    for (FilterDefinition f : compareDefinitions) {
      if (f.getAttr().getName().equals(colName)) {
        return f;
      }
    }
    Assert.fail("No filter found for column: " + colName);
    return null;
  }

}
