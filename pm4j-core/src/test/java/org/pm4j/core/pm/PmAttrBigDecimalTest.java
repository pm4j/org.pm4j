package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.annotation.PmAttrBigDecimalCfg;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.impl.PmAttrBigDecimalImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmAttrBigDecimalTest {

  @Test
  public void testValueAccess() {
    MyPm myPm = new MyPm();

    assertNull("Initial value should be null", myPm.bigDecMaxLen6.getValue());
    assertNull("Initial value as string should be null", myPm.bigDecMaxLen6.getValueAsString());

    BigDecimal assignedValue = new BigDecimal("123");
    myPm.bigDecMaxLen6.setValue(assignedValue);

    assertEquals("The assigned value should be the current one.", assignedValue, myPm.bigDecMaxLen6.getValue());
    assertEquals("The assigned value should also appear as string.", "123", myPm.bigDecMaxLen6.getValueAsString());
  }

  @Test
  public void testMaxLen() {
    MyPm myPm = new MyPm();

    assertEquals(6, myPm.bigDecMaxLen6.getMaxLen());

  }

  
  @Test
  @Ignore("FIXME: Wrong locale is choosen, germany should use a dot as decimal divider. See property _de file.")
  public void testDefaultNoRoundingGermany() {
    MyPm myPm = new MyPm();
    myPm.getPmConversation().setPmLocale(Locale.GERMAN);
    myPm.bigDecimal.setValueAsString("123,56789");
    assertTrue("By default any BigDecimal should be valid.", myPm.bigDecimal.isPmValid());
    assertEquals("By default any BigDecimal should not be formatted.","123,56789", myPm.bigDecimal.getValueAsString());    
  }


  @Test
  public void testDefaultNoRoundingEnglish() {
    MyPm myPm = new MyPm();
    myPm.getPmConversation().setPmLocale(Locale.ENGLISH);
    myPm.bigDecimal.setValueAsString("123.56789");
    assertTrue("By default any BigDecimal should be valid.", myPm.bigDecimal.isPmValid());
    assertEquals("By default any BigDecimal should not be formatted.","123.56789", myPm.bigDecimal.getValueAsString());    
  }

  
  @Test
  public void testMinMax() {
    MyPm myPm = new MyPm();

    assertEquals(999999, myPm.bigAttrCfg.getMax().longValue());
    assertEquals(9, myPm.bigAttrCfg.getMaxLen());

    myPm.bigAttrCfg.setValueAsString("0.3");
    myPm.bigAttrCfg.pmValidate();
    assertTrue(myPm.bigAttrCfg.isPmValid());

    myPm.bigAttrCfg.setValueAsString("0");
    myPm.bigAttrCfg.pmValidate();
    assertFalse(myPm.bigAttrCfg.isPmValid());

    myPm.bigAttrCfg.setValueAsString("1000000");
    myPm.bigAttrCfg.pmValidate();
    assertFalse(myPm.bigAttrCfg.isPmValid());
  }

  @Test
  public void testDefaultStringFormat() {
    MyPm myPm = new MyPm();

    assertEquals("An un-set value provides a null.", null, myPm.bigDecMaxLen6.getValueAsString());
    myPm.bigDecMaxLen6.setValueAsString("0");

    assertEquals("Default format for zero.", "0", myPm.bigDecMaxLen6.getValueAsString());

    myPm.bigDecMaxLen6.setValueAsString("0.153");
    assertEquals("0.153", myPm.bigDecMaxLen6.getValueAsString());
  }

  
  
  @Test
  public void testFormatted() {
    MyPm myPm = new MyPm();

    assertEquals("An un-set value provides a null.", null, myPm.bigFormatted.getValueAsString());
    myPm.bigFormatted.setValueAsString("0");

    assertEquals("Default format for zero.", "0", myPm.bigFormatted.getValueAsString());

    myPm.bigFormatted.setValueAsString("0.123");
    assertEquals("0.123", myPm.bigFormatted.getValueAsString());

  }



  static class MyPm extends PmConversationImpl {
    @PmAttrCfg(maxLen=6)
    public final PmAttrBigDecimal bigDecMaxLen6 = new PmAttrBigDecimalImpl(this);

    public final PmAttrBigDecimal bigDecimal = new PmAttrBigDecimalImpl(this);
    
    @PmAttrCfg(formatResKey="")
    public final PmAttrBigDecimal bigFormatted = new PmAttrBigDecimalImpl(this);
    
    @PmAttrBigDecimalCfg(minValueString="0.1", maxValueString="999999.99")
    public final PmAttrBigDecimal bigAttrCfg = new PmAttrBigDecimalImpl(this);

    @Override
    protected void onPmInit() {
      super.onPmInit();
      // FIXME olaf: there is an unresolved language issue.
      // The test does not work for GERMAN, even if the related resources are defined within a default resource file.
      setPmLocale(Locale.ENGLISH);
    }
  }

}
