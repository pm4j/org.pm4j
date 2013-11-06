package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.pm4j.tools.test.PmAssert.setValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pm4j.common.pageable.PageableCollectionUtil2;
import org.pm4j.common.query.FilterCompareDefinition;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.inmem.InMemSortOrder;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.pm.PmTable2.UpdateAspect;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmTableCfg2;
import org.pm4j.core.pm.annotation.PmTableColCfg2;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmTableColImpl2;
import org.pm4j.core.pm.impl.PmTableImpl2;

public class PmTable2Test {

  private TablePm myTablePm;
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
    PageableCollectionUtil2.navigateToLastPage(myTablePm.getPmPageableCollection());
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

    PmEventApi.firePmEvent(myTablePm.getPmParent(), PmEvent.VALUE_CHANGE);
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
  public void testSortByDescriptionUsingCustomComparator() {
    assertEquals("[a, b]", myTablePm.getRowPms().toString());
    setValue(myTablePm.description.getSortOrderAttr(), PmSortOrder.ASC);
    assertEquals("[b, c]", myTablePm.getRowPms().toString());
  }

  @Test
  public void testFilterByDescriptionExists() {
    assertEquals("[a, b]", myTablePm.getRowPms().toString());
    FilterCompareDefinition fd = getFilterDefinition("description");
    assertEquals(myTablePm.description.getPmTitle(), fd.getAttrTitle());
  }


  @PmTableCfg2(defaultSortCol="name")
  @PmFactoryCfg(beanPmClasses=RowPm.class)
  public static class TablePm extends PmTableImpl2<RowPm, RowBean> {

    /** A column with an annotation based filter definition. */
    @PmTableColCfg2(sortable=PmBoolean.TRUE)
    public final PmTableCol2 name = new PmTableColImpl2(this);

    /** A column with a method based filter definition. */
    @PmTableColCfg2(filterType = String.class)
    public final PmTableCol2 description = new PmTableColImpl2(this);

    /** A column with a filter annotation that defines . */
    public final PmTableCol2 counter = new PmTableColImpl2(this);

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

    /** for debugging */
    @Override
    public String toString() {
      return getPmBean().toString();
    }
  }

  public static class RowBean {
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

  private FilterCompareDefinition getFilterDefinition(String colName) {
    QueryOptions qo = myTablePm.getPmPageableBeanCollection().getQueryOptions();
    List<FilterCompareDefinition> compareDefinitions = qo.getCompareDefinitions();
    for (FilterCompareDefinition f : compareDefinitions) {
      if (f.getAttr().getName().equals(colName)) {
        return f;
      }
    }
    Assert.fail("No filter found for column: " + colName);
    return null;
  }

}
