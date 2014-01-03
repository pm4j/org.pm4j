package org.pm4j.deprecated.core.pm;

import static junit.framework.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.deprecated.core.pm.DeprPmTableCol;
import org.pm4j.deprecated.core.pm.filter.DeprFilterBase;
import org.pm4j.deprecated.core.pm.impl.DeprPmTableColImpl;
import org.pm4j.deprecated.core.pm.impl.DeprPmTableImpl;
import org.pm4j.deprecated.core.pm.pageable.DeprPageablePmsForBeans;


public class PmTableTest {

  private MyTablePm myTablePm;
  private List<Item> editedRowBeanList;

  @Before
  public void setUp() {
    ItemPm.numOfCtorCalls = 0;

    editedRowBeanList = new ArrayList<PmTableTest.Item>(Arrays.asList(
        new Item("a", "an 'a'"),
        new Item("b", "a 'b'"),
        new Item("c", "a 'c'")
    ));

    myTablePm = new MyTablePm(new PmConversationImpl());
    myTablePm.setPageableCollection(new DeprPageablePmsForBeans<ItemPm, Item>(myTablePm, editedRowBeanList), false);
  }

  @Test
  public void testTable() {
    assertEquals("The table displays 3 rows for 3 beans.", 3, myTablePm.getRows().size());
  }

  @Test
  public void testTableWithFixFilter() {
    assertEquals("The unfiltered table has 3 rows.", 3, myTablePm.getRows().size());
    assertEquals(3, ItemPm.numOfCtorCalls);

    myTablePm.setFixFilter("f", new ItemNameStartsWithFilter("a"));
    assertEquals("There is only one item that starts with 'a'.", 1, myTablePm.getRows().size());
    // TODO olaf: is unstable. Fails if the whole test suite is started from eclipse.
    //   assertEquals(3, ItemPm.numOfCtorCalls);
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
    private static int numOfCtorCalls;
    public final PmAttrString name = new PmAttrStringImpl(this);
    public final PmAttrString description = new PmAttrStringImpl(this);

    public ItemPm() {
      ++numOfCtorCalls;
    }
  }


  @PmFactoryCfg(beanPmClasses=ItemPm.class)
  public static class MyTablePm extends DeprPmTableImpl<ItemPm> {
    public MyTablePm(PmObject pmParent) {
      super(pmParent);
    }

    public final DeprPmTableCol name = new DeprPmTableColImpl(this);
    public final DeprPmTableCol description = new DeprPmTableColImpl(this);
  }

  public static class ItemNameStartsWithFilter extends DeprFilterBase<ItemPm> {

    private String name;

    public ItemNameStartsWithFilter(String name) {
      super(FilterKind.ITEM_FILTER);
      this.name = name;
    }

    @Override
    protected boolean doesItemMatchImpl(ItemPm item) {
      return item.name.getValue().startsWith(name);
    }


  }
}
