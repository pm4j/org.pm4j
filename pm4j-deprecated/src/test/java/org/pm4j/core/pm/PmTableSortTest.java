package org.pm4j.core.pm;

import static junit.framework.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.DeprPmTableCfg;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.DeprPmTableColImpl;
import org.pm4j.core.pm.impl.DeprPmTableImpl;
import org.pm4j.core.pm.pageable.DeprPageablePmsForBeans;


public class PmTableSortTest {

  @PmFactoryCfg(beanPmClasses=ItemPm.class)
  public static class MyTablePm extends DeprPmTableImpl<ItemPm> {
    public final DeprPmTableCol name = new DeprPmTableColImpl(this);
    public final DeprPmTableCol description = new DeprPmTableColImpl(this);

    public MyTablePm(PmObject pmParent) { super(pmParent); }
  }

  @DeprPmTableCfg(defaultSortCol="name")
  public static class MyTablePmWithDefaultSortCol extends MyTablePm {
    public MyTablePmWithDefaultSortCol(PmObject pmParent) { super(pmParent); }
  }

  @DeprPmTableCfg(initialBeanSortComparator = IdxComparator.class)
  public static class MyTablePmWithInitialSortOrder extends MyTablePm {
    public MyTablePmWithInitialSortOrder(PmObject pmParent) { super(pmParent); }
  }

  @PmBeanCfg(beanClass=Item.class)
  public static class ItemPm extends PmBeanBase<Item> {
    public final PmAttrString name = new PmAttrStringImpl(this);
    public final PmAttrString description = new PmAttrStringImpl(this);
  }


  private MyTablePm myTablePm;
  private MyTablePm myTablePmWithDefaultSortCol;
  private MyTablePm myTablePmWithInitialSortOrder;
  private List<Item> rowBeanList;

  @Before
  public void setUp() {
    rowBeanList = new ArrayList<PmTableSortTest.Item>(Arrays.asList(
        new Item("a", "an 'a'", 2),
        new Item("c", "a 'c'", 1),
        new Item("b", "a 'b'", 3)
    ));

    myTablePm = new MyTablePm(new PmConversationImpl());
    myTablePm.setPageableCollection(new DeprPageablePmsForBeans<ItemPm, Item>(myTablePm, rowBeanList), false);

    myTablePmWithDefaultSortCol = new MyTablePmWithDefaultSortCol(new PmConversationImpl());
    myTablePmWithDefaultSortCol.setPageableCollection(new DeprPageablePmsForBeans<ItemPm, Item>(myTablePm, rowBeanList), false);

    myTablePmWithInitialSortOrder = new MyTablePmWithInitialSortOrder(new PmConversationImpl());
    myTablePmWithInitialSortOrder.setPageableCollection(new DeprPageablePmsForBeans<ItemPm, Item>(myTablePm, rowBeanList), false);
  }

  @Test
  public void testDefaultSortColumnDefinedByAnnotation() {
    assertEquals("The name column is sorted in ascending order.", PmSortOrder.ASC, myTablePmWithDefaultSortCol.name.getSortOrderAttr().getValue());
    assertEquals("The description column not sorted.", PmSortOrder.NEUTRAL, myTablePmWithDefaultSortCol.description.getSortOrderAttr().getValue());

    assertEquals("Table is sorted by: defaultSortCol='name'.", "a", myTablePmWithDefaultSortCol.getRows().get(0).name.getValue());
    assertEquals("Table is sorted by: defaultSortCol='name'.", "b", myTablePmWithDefaultSortCol.getRows().get(1).name.getValue());
    assertEquals("Table is sorted by: defaultSortCol='name'.", "c", myTablePmWithDefaultSortCol.getRows().get(2).name.getValue());
  }


  @Test
  public void testDefaultSortColumnDefinedByMethod() {
    MyTablePm table = new MyTablePm(new PmConversationImpl()) {
      @Override
      protected SortOrderSpec getDefaultSortOrder() {
        return new SortOrderSpec(name, PmSortOrder.DESC);
      }
    };
    table.setPageableCollection(new DeprPageablePmsForBeans<ItemPm, Item>(table, rowBeanList), false);

    assertEquals("The table sorts by 'name' in descending order.", "c", table.getRows().get(0).name.getValue());
    assertEquals("The table sorts by 'name' in descending order.", "b", table.getRows().get(1).name.getValue());
    assertEquals("The table sorts by 'name' in descending order.", "a", table.getRows().get(2).name.getValue());
  }

  @Test
  public void testTableWithInitialSortOrder() {
    assertEquals("The table sorts by bean attribute 'idx'.", 1, myTablePmWithInitialSortOrder.getRows().get(0).getPmBean().idx);
    assertEquals("The table sorts by bean attribute 'idx'.", 2, myTablePmWithInitialSortOrder.getRows().get(1).getPmBean().idx);
    assertEquals("The table sorts by bean attribute 'idx'.", 3, myTablePmWithInitialSortOrder.getRows().get(2).getPmBean().idx);

    myTablePmWithInitialSortOrder.name.getSortOrderAttr().setValue(PmSortOrder.DESC);
    assertEquals("The table now sorts by name in descending order.", "c", myTablePmWithInitialSortOrder.getRows().get(0).name.getValue());
    assertEquals("The table now sorts by name in descending order.", "b", myTablePmWithInitialSortOrder.getRows().get(1).name.getValue());
    assertEquals("The table now sorts by name in descending order.", "a", myTablePmWithInitialSortOrder.getRows().get(2).name.getValue());

    myTablePmWithInitialSortOrder.name.getSortOrderAttr().setValue(PmSortOrder.NEUTRAL);
    assertEquals("The table sorts again by bean attribute 'idx'.", 1, myTablePmWithInitialSortOrder.getRows().get(0).getPmBean().idx);
    assertEquals("The table sorts again by bean attribute 'idx'.", 2, myTablePmWithInitialSortOrder.getRows().get(1).getPmBean().idx);
    assertEquals("The table sorts again by bean attribute 'idx'.", 3, myTablePmWithInitialSortOrder.getRows().get(2).getPmBean().idx);
  }



  public static class Item {
    public String name;
    public String description;
    public int idx;

    public Item(String name, String description, int idx) {
      this.name = name;
      this.description = description;
      this.idx = idx;
    }
  }

  public static class IdxComparator implements Comparator<Item> {
    @Override public int compare(Item o1, Item o2) { return CompareUtil.compare(o1.idx, o2.idx); }
  }

}
