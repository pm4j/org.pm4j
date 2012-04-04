package org.pm4j.core.pm;

import static junit.framework.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmTableColImpl;
import org.pm4j.core.pm.impl.PmTableImpl;
import org.pm4j.core.pm.pageable.PageablePmsForBeans;


public class PmTableTest {

  private MyTablePm myTablePm;
  private List<Item> editedRowBeanList;

  @Before
  public void setUp() {
    editedRowBeanList = new ArrayList<PmTableTest.Item>(Arrays.asList(
        new Item("a", "an 'a'"),
        new Item("b", "a 'b'"),
        new Item("c", "a 'c'")
    ));

    myTablePm = new MyTablePm(new PmConversationImpl());
    myTablePm.setPageableCollection(new PageablePmsForBeans<ItemPm, Item>(myTablePm, editedRowBeanList), false);
  }

  @Test
  public void testTable() {
    assertEquals("The table displays 3 rows for 3 beans.", 3, myTablePm.getRows().size());
  }


  public static class Item {
    public String name;
    public String description;

    public Item(String name, String description) {
      this.name = name;
      this.description = description;
    }
  }

  @PmBeanCfg(beanClass=Item.class)
  public static class ItemPm extends PmBeanBase<Item> {
    public final PmAttrString name = new PmAttrStringImpl(this);
    public final PmAttrString description = new PmAttrStringImpl(this);
  }

  @PmFactoryCfg(beanPmClasses=ItemPm.class)
  public static class MyTablePm extends PmTableImpl<ItemPm> {
    public MyTablePm(PmObject pmParent) {
      super(pmParent);
    }

    public final PmTableCol name = new PmTableColImpl(this);
    public final PmTableCol description = new PmTableColImpl(this);
  }

}
