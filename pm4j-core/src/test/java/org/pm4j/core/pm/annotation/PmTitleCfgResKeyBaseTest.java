package org.pm4j.core.pm.annotation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmObjectBase;

/**
 * Tests the configuration possibilities of {@link PmTitleCfg#resKeyBase()} for
 * {@link PmTitleCfg}.
 * 
 * @author mheller
 * 
 */
public class PmTitleCfgResKeyBaseTest {

  /**
   * Test the resKeyBase settings for a container that has no resKeyBase
   * configuration.
   */
  @Test
  public void testResKeyBaseSettingsOfGrandparentContainerPm() {
    GrandparentContainerPm container = new GrandparentContainerPm(new PmConversationImpl());

    // Container
    assertEquals("pmTitleCfgResKeyBaseTest.GrandparentContainerPm", container.getPmResKeyBase());

    // Field
    assertEquals("pmTitleCfgResKeyBaseTest.GrandparentContainerPm.grandparentField",
        container.grandparentField.getPmResKeyBase());
  }

  /**
   * Test the resKeyBase settings for a container that has a resKeyBase
   * configuration.
   */
  @Test
  public void testResKeyBaseSettingsOfParentContainerPm() {
    ParentContainerPm container = new ParentContainerPm(new PmConversationImpl());

    // Container
    assertEquals("parentResKeyBase", container.getPmResKeyBase());

    // Fields
    assertEquals("parentResKeyBase.grandparentField", container.grandparentField.getPmResKeyBase());
    assertEquals("parentResKeyBase.parentField", container.parentField.getPmResKeyBase());
  }

  /**
   * Test the resKeyBase settings for a container that gets a resKeyBase
   * configuration via inheritance.
   */
  @Test
  public void testResKeyBaseSettingsOfChildContainerPm() {
    ChildContainerPm container = new ChildContainerPm(new PmConversationImpl());

    // Container

    // Fields
    assertEquals("parentResKeyBase.grandparentField", container.grandparentField.getPmResKeyBase());
    assertEquals("parentResKeyBase.parentField", container.parentField.getPmResKeyBase());
  }

  public static class GrandparentContainerPm extends PmObjectBase {

    public GrandparentContainerPm(PmObject pmParent) {
      super(pmParent);
    }

    public final PmObjectBase grandparentField = new PmObjectBase(this);
  }

  @PmTitleCfg(resKeyBase = "parentResKeyBase")
  public static class ParentContainerPm extends GrandparentContainerPm {

    public ParentContainerPm(PmObject pmParent) {
      super(pmParent);
    }

    public final PmObjectBase parentField = new PmObjectBase(this);
  }

  @PmTitleCfg(title = "Child")
  public static final class ChildContainerPm extends ParentContainerPm {

    public ChildContainerPm(PmObject pmParent) {
      super(pmParent);
    }
  }
}
