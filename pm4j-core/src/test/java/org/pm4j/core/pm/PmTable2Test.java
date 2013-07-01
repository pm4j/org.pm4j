package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.common.pageable.PageableCollectionUtil2;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmTableColImpl2;
import org.pm4j.core.pm.impl.PmTableImpl2;
import org.pm4j.core.pm.impl.PmTableRowImpl;

public class PmTable2Test {

  private TablePm myTablePm;
  private List<RowBean> editedRowBeanList;

  @Before
  public void setUp() {
    editedRowBeanList = new ArrayList<RowBean>(Arrays.asList(
        new RowBean("a", "an 'a'", 1),
        new RowBean("b", "a 'b'", 2),
        new RowBean("c", "a 'c'", 3)
    ));

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
    assertEquals("[a, b, d]", editedRowBeanList.toString());
    assertEquals("Rerender the current page. It's not changed because the current page items are cached.",
                 "[c, d]", myTablePm.getRowPms().toString());

    PmCacheApi.clearPmCache(myTablePm);
    assertEquals("After an update call the table should display the current content.",
        "[d]", myTablePm.getRowPms().toString());
  }

  @PmFactoryCfg(beanPmClasses=RowPm.class)
  public static class TablePm extends PmTableImpl2<RowPm, RowBean> {

    /** A column with an annotation based filter definition. */
    public final PmTableCol2 name = new PmTableColImpl2(this);

    /** A column with a method based filter defintion. */
    public final PmTableCol2 description = new PmTableColImpl2(this);

    /** A column with a filter annotation that defines . */
    public final PmTableCol2 counter = new PmTableColImpl2(this);

    /** Defines a page size of two items. */
    public TablePm(PmObject pmParent) {
      super(pmParent);
      setNumOfPageRowPms(2);
    }
  }

  @PmBeanCfg(beanClass=RowBean.class)
  public static class RowPm extends PmTableRowImpl<RowBean> {
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
      this.name = name;
      this.description = description;
      this.counter = counter;
    }

    /** for debugging */
    @Override
    public String toString() {
      return name;
    }
  }

}
