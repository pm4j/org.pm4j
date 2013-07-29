package org.pm4j.core.pm.impl;

import static junit.framework.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTableCol;

public class PmTableDynamicTest {
  private int numOfCols = 3;
  private MyTablePm tablePm;

  public void setUp() {
    PmConversation c = new PmConversationImpl();
    tablePm = new MyTablePm(c);
  }

  @Test
  @Ignore
  public void testDynamicCols() {
    assertEquals(3, PmUtil.getPmChildrenOfType(tablePm, PmTableCol.class));
    assertEquals(3, tablePm.getColumns().size());
  }

  public static class MyRowPm extends PmElementImpl {

  }

  public class MyTablePm extends PmTableImpl<MyRowPm> {

    public MyTablePm(PmObject pmParent) {
      super(pmParent);
    }

    @Override
    protected void onPmInit() {
      super.onPmInit();

      for (int i =0; i<numOfCols;  ++i) {
        addToPmComposite(""+i, new PmTableColImpl(this));
      }
    }

  };
}
