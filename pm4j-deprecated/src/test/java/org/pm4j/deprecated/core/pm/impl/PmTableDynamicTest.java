package org.pm4j.deprecated.core.pm.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.deprecated.core.pm.DeprPmTableCol;

import static junit.framework.Assert.assertEquals;

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
    assertEquals(3, PmUtil.getPmChildrenOfType(tablePm, DeprPmTableCol.class));
    assertEquals(3, tablePm.getColumns().size());
  }

  public static class MyRowPm extends PmElementImpl {

  }

  public class MyTablePm extends DeprPmTableImpl<MyRowPm> {

    public MyTablePm(PmObject pmParent) {
      super(pmParent);
    }

    @Override
    protected void onPmInit() {
      super.onPmInit();

      for (int i =0; i<numOfCols;  ++i) {
        addToPmComposite(""+i, new DeprPmTableColImpl(this));
      }
    }

  };
}
