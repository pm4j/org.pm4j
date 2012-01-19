package org.pm4j.core.pm.impl;

import junit.framework.TestCase;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;

public class NestedPmTest extends TestCase {

  public static class RootPm extends PmConversationImpl {
    public final ChildPm childElem = new ChildPm(this);
  }

  public static class ChildPm extends PmElementImpl {
    public ChildPm(PmObject pmParent) {
      super(pmParent);
    }

    public final PmConversationImplChild pmConversationChildElem = new PmConversationImplChild(this);

    public final PmCommand               cmdInChildElem          = new PmCommandImpl(this);

  }

  public static class PmConversationImplChild extends PmConversationImpl {

    public final PmCommand cmdInPmConversationChildElem = new PmCommandImpl(this);
    public PmConversationImplChild(PmObject pmParent) {
      super(pmParent);
    }

  }

  public void testAccessNestedPm() {
    // will be initialized with freshly created meta data
    RootPm pm = new RootPm();

    assertEquals("childElem", pm.childElem.getPmName());
    assertEquals("childElem", pm.childElem.getPmRelativeName());
    assertEquals("org.pm4j.core.pm.impl.NestedPmTest$RootPm_childElem", PmUtil.getAbsoluteName(pm.childElem));
    assertEquals(
        "org.pm4j.core.pm.impl.NestedPmTest$RootPm_childElem_pmConversationChildElem_cmdInPmConversationChildElem",
        PmUtil.getAbsoluteName(pm.childElem.pmConversationChildElem.cmdInPmConversationChildElem));

    assertEquals("cmdInChildElem", pm.childElem.cmdInChildElem.getPmName());
    assertEquals("childElem_cmdInChildElem", pm.childElem.cmdInChildElem.getPmRelativeName());
    assertEquals("org.pm4j.core.pm.impl.NestedPmTest$RootPm_childElem_cmdInChildElem", PmUtil.getAbsoluteName(pm.childElem.cmdInChildElem));

    assertEquals("nestedPmTest.RootPm", pm.getPmResKey());
    assertEquals("nestedPmTest.RootPm.childElem", pm.childElem.getPmResKey());
    assertEquals("nestedPmTest.RootPm.childElem.cmdInChildElem",
                 ((PmObjectBase)pm.childElem.cmdInChildElem).getPmResKey());

    // will recycle the meta data created
    RootPm pm2 = new RootPm();

    assertEquals("childElem", pm2.childElem.getPmName());
    assertEquals("org.pm4j.core.pm.impl.NestedPmTest$RootPm_childElem", PmUtil.getAbsoluteName(pm2.childElem));

    assertEquals("cmdInChildElem", pm2.childElem.cmdInChildElem.getPmName());
    assertEquals("org.pm4j.core.pm.impl.NestedPmTest$RootPm_childElem_cmdInChildElem", PmUtil.getAbsoluteName(pm2.childElem.cmdInChildElem));
  }

}
