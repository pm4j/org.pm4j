package org.pm4j.core.pm;

import java.util.Arrays;

import junit.framework.TestCase;

import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.impl.DeprecatedPmTableOfPmBeansImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmTableColImpl;


public class PmTableOfPmElementsTest extends TestCase {

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
  public static class MyTablePm extends DeprecatedPmTableOfPmBeansImpl.WithCollection<ItemPm, Item> {
    public MyTablePm(PmObject pmParent) {
      super(pmParent);
    }

    public final PmTableCol name = new PmTableColImpl(this);
    public final PmTableCol description = new PmTableColImpl(this);
  }

  public void testHereSomethingInTheFuture() {
    MyTablePm table = makeTable();

    assertEquals(2, table.getGenericRows().size());
  }

  private MyTablePm makeTable() {
    PmConversation s = new PmConversationImpl();
    MyTablePm t = new MyTablePm(s);

    t.setBeans(Arrays.asList(
        new Item("a", "ah!"),
        new Item("b", "bb")
        ));

//    t.addRow()

//    for (int i=0; i<10; ++i) {
//      t.
//    }
    return t;
  }
}
