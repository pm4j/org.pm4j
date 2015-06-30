package org.pm4j.core.pm.impl;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmObject.PmMatcher;
import org.pm4j.core.pm.annotation.PmTitleCfg;

/**
 * Test for {@link PmMatcherBuilder}.
 *
 * @author Olaf Boede
 */
public class PmMatcherBuilderTest {

  TestPm pm = new TestPm();

  @Test
  public void matchInstance() {
    PmMatcher matcher = new PmMatcherBuilder().pm(pm).build();

    assertEquals(true, matcher.doesMatch(pm));
    assertEquals(false, matcher.doesMatch(pm.stringAttr));
  }

  @Test
  public void matchInstances() {
    PmMatcher matcher = new PmMatcherBuilder().pm(pm, pm.stringAttr).build();

    assertEquals(true, matcher.doesMatch(pm));
    assertEquals(true, matcher.doesMatch(pm.stringAttr));
  }


  @PmTitleCfg(title = "Test PM")
  public static class TestPm extends PmConversationImpl {
    @PmTitleCfg(title = "String Attr")
    public final PmAttrString stringAttr = new PmAttrStringImpl(this);

    public TestPm() {
      super(Locale.ENGLISH);
    }
  }

}
