package org.pm4j.core.pm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.connector.PmToNoViewTechnologyConnector;

public class PmConversationImplTest {

  @Test
  public void testGetViewConnectorOfParentConversation() {
    PmConversationImpl rootConv = new PmConversationImpl();
    PmConversationImpl childConv = new PmConversationImpl(rootConv);
    PmToNoViewTechnologyConnector myViewTechConnector = new PmToNoViewTechnologyConnector() {
      @Override
      public Object createPmToViewConnector(PmObject pm) {
        return new Integer(13);
      }
    };
    rootConv.setPmToViewTechnologyConnector(myViewTechConnector);

    assertSame(myViewTechConnector, childConv.getPmToViewTechnologyConnector());
    assertEquals(new Integer(13), childConv.getPmToViewConnector());
  }

  @Test
  public void testConversationReadonlyStateIsIsolatedFromParentReadonlyState() {
    class RootConv extends PmConversationImpl {
      boolean rootReadonly = false;
      @Override
      protected boolean isPmReadonlyImpl() {
        return rootReadonly;
      }
    }

    RootConv rootConv = new RootConv();
    PmConversationImpl childConv = new PmConversationImpl(rootConv);

    assertEquals(false, rootConv.isPmReadonly());
    assertEquals(false, childConv.isPmReadonly());

    rootConv.rootReadonly = true;
    assertEquals(true, rootConv.isPmReadonly());
    assertEquals(false, childConv.isPmReadonly());
  }

}
