package org.pm4j.core.pm;

import org.junit.Assert;
import org.junit.Test;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmAttrCfg.HideIf;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.tools.test._PmAssert;

public class PmAttrVisibilityTest {

  private TestPm testPm = new TestPm();

  @Test
  public void hideIfEmpty() {
    _PmAssert.assertNotVisible(testPm.hideIfEmptyValue);
  }

  @Test
  public void showIfNotEmpty() {
    _PmAssert.setValue(testPm.hideIfEmptyValue, "hello");
    _PmAssert.assertVisible(testPm.hideIfEmptyValue);
  }

  @Test
  public void hideIfDefault() {
    Assert.assertEquals("abc", testPm.hideIfDefaultValue.getValue());
    _PmAssert.assertNotVisible(testPm.hideIfDefaultValue);
  }

  @Test
  public void showIfNotDefault() {
    // Does not set the default value automatically, because the user sets the value explicitly.
    _PmAssert.setValue(testPm.hideIfDefaultValue, null);
    _PmAssert.assertVisible(testPm.hideIfDefaultValue);
  }
  
  @Test
  public void hideIfDefaultOrEmpty1() {
    Assert.assertEquals("abc", testPm.hideIfDefaultOrEmptyValue.getValue());
    _PmAssert.assertNotVisible(testPm.hideIfDefaultOrEmptyValue);
  }
  
  @Test
  public void hideIfDefaultOrEmpty2() {
    Assert.assertEquals("abc", testPm.hideIfDefaultOrEmptyValue.getValue());
    _PmAssert.setValue(testPm.hideIfDefaultOrEmptyValue, null);
    Assert.assertEquals(null, testPm.hideIfDefaultOrEmptyValue.getValue());
    _PmAssert.assertNotVisible(testPm.hideIfDefaultOrEmptyValue);
  }
  
  @Test
  public void showIfNotDefaultAndNotEmpty() {
    _PmAssert.setValue(testPm.hideIfDefaultOrEmptyValue, "hello");
    _PmAssert.assertVisible(testPm.hideIfDefaultOrEmptyValue);
  }

  static class TestPm extends PmConversationImpl {
    @PmAttrCfg(hideIf=HideIf.EMPTY_VALUE)
    public final PmAttrString hideIfEmptyValue = new PmAttrStringImpl(this);

    @PmAttrCfg(hideIf=HideIf.DEFAULT_VALUE, defaultValue="abc")
    public final PmAttrString hideIfDefaultValue = new PmAttrStringImpl(this);

    @PmAttrCfg(hideIf={ HideIf.DEFAULT_VALUE, HideIf.EMPTY_VALUE}, defaultValue="abc")
    public final PmAttrString hideIfDefaultOrEmptyValue = new PmAttrStringImpl(this);
  }
}

