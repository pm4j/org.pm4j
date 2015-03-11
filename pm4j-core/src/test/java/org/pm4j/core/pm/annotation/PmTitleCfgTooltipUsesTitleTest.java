package org.pm4j.core.pm.annotation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmObjectBase;

public class PmTitleCfgTooltipUsesTitleTest {

  /**
   * Test the resKey settings for a container that has no resKey
   * configuration.
   */
  @Test
  public void testResKeySettingsOfContainerPm() {
    ContainerPm container = new ContainerPm(new PmConversationImpl());

    // Container
    assertEquals("pmTitleCfgTooltipUsesTitleTest.ContainerPm", container.getPmTitle());
  }

  /**
   * Test the resKey settings for a container that has a resKey configuration.
   */
  @Test
  public void testResKeyBaseSettingsOfContainerWithResKeyPm() {
    ContainerWithTooltipUsesTitlePm container = new ContainerWithTooltipUsesTitlePm(new PmConversationImpl());

    // Container: The tool tip should be the same as the title.
    assertEquals(container.getPmTitle(), container.getPmTooltip());
  }

  /**
   * Test the resKey settings for a container that gets a resKey configuration via inheritance.
   */
  @Test
  public void testTooltipUsesTitleSettingsOfContainerWithResKeyBasePm() {
    ContainerWithResKeyBasePm container = new ContainerWithResKeyBasePm(new PmConversationImpl());

    // Container: The tool tip should be the same as the title.
    assertEquals(container.getPmTitle(), container.getPmTooltip());
  }

  public static class ContainerPm extends PmObjectBase {

    public ContainerPm(PmObject pmParent) {
      super(pmParent);
    }
  }

  @PmTitleCfg(tooltipUsesTitle = TooltipUsesTitleEnum.TRUE, title = "Title")
  public static class ContainerWithTooltipUsesTitlePm extends ContainerPm {

    public ContainerWithTooltipUsesTitlePm(PmObject pmParent) {
      super(pmParent);
    }
  }

  @PmTitleCfg(resKeyBase = "resKeyBase")
  public static final class ContainerWithResKeyBasePm extends ContainerWithTooltipUsesTitlePm {

    public ContainerWithResKeyBasePm(PmObject pmParent) {
      super(pmParent);
    }
  }
}
