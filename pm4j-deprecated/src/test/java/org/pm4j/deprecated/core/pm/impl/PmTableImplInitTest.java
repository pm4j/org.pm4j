package org.pm4j.deprecated.core.pm.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmInitApi;
import org.pm4j.core.pm.impl.PmObjectBase.PmInitState;
import org.pm4j.deprecated.core.pm.DeprPmTableCol;
import org.pm4j.deprecated.core.pm.impl.DeprPmTableColImpl;
import org.pm4j.deprecated.core.pm.impl.DeprPmTableImpl;

public class PmTableImplInitTest {

  @Test
  public void testInitPmTable() {
    MyTable t = new MyTable(new PmConversationImpl());

    assertEquals(PmInitState.NOT_INITIALIZED, PmInitApi.getPmInitState(t));

    t.col1.getPmTitle();

    assertEquals(PmInitState.INITIALIZED, PmInitApi.getPmInitState(t));
  }

  static class MyTable extends DeprPmTableImpl<PmElement> {
    public final DeprPmTableCol col1 = new DeprPmTableColImpl(this);

    public MyTable(PmObject pmParent) {
      super(pmParent);
    }

  }
}
