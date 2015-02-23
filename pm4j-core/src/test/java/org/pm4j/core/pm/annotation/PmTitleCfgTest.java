package org.pm4j.core.pm.annotation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmObjectBase;

public class PmTitleCfgTest {

  @Test
  public void testFieldTitleofContainerPm() {
    ContainerPm container = new ContainerPm(new PmConversationImpl());

    assertEquals("container.field", container.field.getPmResKey());
  }
  
  @Test
  public void testFieldTitleofExtendedContainerPm() {
    ExtendedContainerPm container = new ExtendedContainerPm(new PmConversationImpl());

    assertEquals("container.field", container.field.getPmResKey());
  }

  public static final class FieldPm extends PmObjectBase {

    public FieldPm(PmObject pmParent) {
      super(pmParent);
    }
  }

  @PmTitleCfg(resKeyBase = "container")
  public static class ContainerPm extends PmObjectBase {
    
    public ContainerPm (PmObject pmParent) {
      super(pmParent);
    }

    public final FieldPm field = new FieldPm(this);
  }
  
  @PmTitleCfg(resKey="extendedContainer")
  public static final class ExtendedContainerPm extends ContainerPm {
    
    public ExtendedContainerPm (PmObject pmParent) {
      super(pmParent);
    }
  }
}
