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
    ContainerPm container = new ContainerPm(new PmConversationImpl());

    // Container
    assertEquals("pmTitleCfgResKeyBaseTest.ContainerPm", container.getPmResKeyBase());

    // Field
    assertEquals("pmTitleCfgResKeyBaseTest.ContainerPm.field1",
        container.field1.getPmResKeyBase());
  }

  /**
   * Test the resKeyBase settings for a container that has a resKeyBase
   * configuration.
   */
  @Test
  public void testResKeyBaseSettingsOfParentContainerPm() {
    ContainerWithResKeyBasePm container = new ContainerWithResKeyBasePm(new PmConversationImpl());

    // Container
    assertEquals("resKeyBase", container.getPmResKeyBase());

    // Fields
    assertEquals("resKeyBase.field1", container.field1.getPmResKeyBase());
    assertEquals("resKeyBase.field2", container.field2.getPmResKeyBase());
  }

  /**
   * Test the resKeyBase settings for a container that gets a resKeyBase
   * configuration via inheritance.
   */
  @Test
  public void testResKeyBaseSettingsOfChildContainerPm() {
    ContainerWithTitleCfgPm container = new ContainerWithTitleCfgPm(new PmConversationImpl());

    // Container

    // Fields
    assertEquals("resKeyBase.field1", container.field1.getPmResKeyBase());
    assertEquals("resKeyBase.field2", container.field2.getPmResKeyBase());
  }

  public static class ContainerPm extends PmObjectBase {

    public ContainerPm(PmObject pmParent) {
      super(pmParent);
    }

    public final PmObjectBase field1 = new PmObjectBase(this);
  }

  @PmTitleCfg(resKeyBase = "resKeyBase")
  public static class ContainerWithResKeyBasePm extends ContainerPm {

    public ContainerWithResKeyBasePm(PmObject pmParent) {
      super(pmParent);
    }

    public final PmObjectBase field2 = new PmObjectBase(this);
  }

  @PmTitleCfg(title = "title")
  public static final class ContainerWithTitleCfgPm extends ContainerWithResKeyBasePm {

    public ContainerWithTitleCfgPm(PmObject pmParent) {
      super(pmParent);
    }
  }
}
