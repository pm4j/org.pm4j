package org.pm4j.core.pm.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.DeprPmTableCol;
import org.pm4j.core.pm.impl.PmObjectBase.PmInitState;

public class PmTableImplInitTest {

  @Test
  public void testInitPmTable() {
    MyTable t = new MyTable(new PmConversationImpl());

    assertEquals(PmInitState.NOT_INITIALIZED, t.pmInitState);

    t.col1.getPmTitle();

    assertEquals(PmInitState.INITIALIZED, t.pmInitState);
  }

  static class MyTable extends DeprPmTableImpl<PmElement> {
    public final DeprPmTableCol col1 = new DeprPmTableColImpl(this);

    public MyTable(PmObject pmParent) {
      super(pmParent);
    }

  }
}
