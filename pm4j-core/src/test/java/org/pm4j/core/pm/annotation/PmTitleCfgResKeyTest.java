package org.pm4j.core.pm.annotation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmObjectBase;

/**
 * Tests the configuration possibilities of {@link PmTitleCfg#resKey()} for
 * {@link PmTitleCfg}.
 * 
 * @author mheller
 * 
 */
public class PmTitleCfgResKeyTest {

  /**
   * Test the resKey settings for a container that has no resKey configuration.
   */
  @Test
  public void testResKeySettingsOfContainerPm() {
    ContainerPm container = new ContainerPm(new PmConversationImpl());

    // Container
    assertEquals("pmTitleCfgResKeyTest.ContainerPm", container.getPmResKey());
  }

  /**
   * Test the resKey settings for a container that has a resKey configuration.
   */
  @Test
  public void testResKeyBaseSettingsOfContainerWithResKeyPm() {
    ContainerWithResKeyPm container = new ContainerWithResKeyPm(new PmConversationImpl());

    // Container
    assertEquals("resKey", container.getPmResKey());
  }

  /**
   * Test the resKey settings for a container that gets a resKey configuration
   * via inheritance.
   */
  @Test
  public void testResKeyBaseSettingsOfContainerWithTitleCfgPm() {
    ContainerWithTitleCfgPm container = new ContainerWithTitleCfgPm(new PmConversationImpl());

    // Container
    assertEquals("resKey", container.getPmResKey());
  }

  public static class ContainerPm extends PmObjectBase {

    public ContainerPm(PmObject pmParent) {
      super(pmParent);
    }
  }

  @PmTitleCfg(resKey = "resKey")
  public static class ContainerWithResKeyPm extends ContainerPm {

    public ContainerWithResKeyPm(PmObject pmParent) {
      super(pmParent);
    }
  }

  @PmTitleCfg(title = "title")
  public static final class ContainerWithTitleCfgPm extends ContainerWithResKeyPm {

    public ContainerWithTitleCfgPm(PmObject pmParent) {
      super(pmParent);
    }
  }
}
