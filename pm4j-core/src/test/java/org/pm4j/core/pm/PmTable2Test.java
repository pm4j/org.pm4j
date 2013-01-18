package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmTableColImpl2;
import org.pm4j.core.pm.impl.PmTableImpl2;
import org.pm4j.core.pm.impl.PmTableRowImpl;
import org.pm4j.core.pm.pageable2.PageablePmBeanCollection;

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

    myTablePm = new TablePm(new PmConversationImpl());

//    QueryOptions qo = new QueryOptions();
//    qo.addSortOrder("name.valueLocalized", new InMemSortOrder<Comparable<?>>(false));
//    qo.setDefaultSortOrder(defaultSortOrder)

    PageablePmBeanCollection<RowPm, RowBean> pc = new PageablePmBeanCollection<RowPm, RowBean>(myTablePm, PmTableRow.class, editedRowBeanList);

    myTablePm.setPmPageableCollection(pc);
  }

  @Test
  public void testTable() {
    assertEquals(3, myTablePm.getTotalNumOfPmRows());
  }

  @Test
  public void testEmptyTable() {
    myTablePm.setPmPageableCollection(new PageablePmBeanCollection<RowPm, RowBean>(myTablePm, PmTableRow.class, new ArrayList<RowBean>()));

    assertEquals(0, myTablePm.getTotalNumOfPmRows());
    assertEquals(0, myTablePm.getRowPms().size());
  }

  @PmFactoryCfg(beanPmClasses=RowPm.class)
  public static class TablePm extends PmTableImpl2<RowPm, RowBean> {

    /** A column with an annotation based filter definition. */
    public final PmTableCol2 name = new PmTableColImpl2(this);

    /** A column with a method based filter defintion. */
    public final PmTableCol2 description = new PmTableColImpl2(this);

    /** A column with a filter annotation that defines . */
    public final PmTableCol2 counter = new PmTableColImpl2(this);

    /** ctor */
    public TablePm(PmObject pmParent) { super(pmParent); }
  }

  @PmBeanCfg(beanClass=RowBean.class)
  public static class RowPm extends PmTableRowImpl<RowBean> {
    public final PmAttrString name = new PmAttrStringImpl(this);
    public final PmAttrString description = new PmAttrStringImpl(this);
    public final PmAttrInteger counter = new PmAttrIntegerImpl(this);
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
  }

}
