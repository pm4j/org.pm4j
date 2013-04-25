package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.annotation.PmAttrBigDecimalCfg;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.impl.PmAttrBigDecimalImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.tools.test.PmAssert;

public class PmAttrBigDecimalTest {
  
  private MyPm myPm;
  
  @Before
  public void setup() {
    myPm = new MyPm();
  }

  @Test
  public void testValueAccess() {

    assertNull("Initial value should be null", myPm.bigDecMaxLen6.getValue());
    assertNull("Initial value as string should be null", myPm.bigDecMaxLen6.getValueAsString());

    BigDecimal assignedValue = new BigDecimal("123");
    myPm.bigDecMaxLen6.setValue(assignedValue);

    assertEquals("The assigned value should be the current one.", assignedValue, myPm.bigDecMaxLen6.getValue());
    assertEquals("The assigned value should also appear as string.", "123", myPm.bigDecMaxLen6.getValueAsString());
  }

  @Test
  public void testMaxLen() {
    assertEquals(6, myPm.bigDecMaxLen6.getMaxLen());
  }

  
  @Test
  @Ignore("FIXME: Wrong locale is choosen, germany should use a dot as decimal divider. See property _de file.")
  public void testDefaultNoRoundingGermany() {
    myPm.getPmConversation().setPmLocale(Locale.GERMAN);
    myPm.bigDecimal.setValueAsString("123,56789");
    assertTrue("By default any BigDecimal should be valid.", myPm.bigDecimal.isPmValid());
    assertEquals("By default any BigDecimal should not be formatted.","123,56789", myPm.bigDecimal.getValueAsString());    
  }


  @Test
  public void testDefaultNoRoundingEnglish() {
    myPm.getPmConversation().setPmLocale(Locale.ENGLISH);
    myPm.bigDecimal.setValueAsString("123.56789");
    assertTrue("By default any BigDecimal should be valid.", myPm.bigDecimal.isPmValid());
    assertEquals("By default any BigDecimal should not be formatted.","123.56789", myPm.bigDecimal.getValueAsString());    
  }

  @Test
  public void testReadOnly() {
    MyPm myPm = new MyPm();
    assertTrue(myPm.readOnlyAttr.isPmReadonly());
    assertEquals(new BigDecimal(MyPm.READONLY_VALUE), myPm.readOnlyAttr.getValue());    
    myPm.readOnlyAttr.setValue(new BigDecimal("0.01"));
    assertEquals(new BigDecimal(MyPm.READONLY_VALUE), myPm.readOnlyAttr.getValue());    
    assertTrue(myPm.readOnlyAttr.isPmValid());
    assertEquals(MyPm.READONLY_VALUE, myPm.readOnlyAttr.getValueAsString());
  }
  
  @Test
  public void testGetMinMax() {
    assertEquals(999, myPm.minMaxAttr.getMax().longValue());
    assertEquals(6, myPm.minMaxAttr.getMaxLen());    
  }
  
  private void assertMinMax(String number, boolean isValid) {
    myPm.minMaxAttr.setValueAsString(number);
    myPm.minMaxAttr.pmValidate();
    assertEquals(number, myPm.minMaxAttr.getValueAsString());
    if(isValid) {
      assertEquals(new BigDecimal(number), myPm.minMaxAttr.getValue());
    }
    assertEquals(isValid, myPm.minMaxAttr.isPmValid());    
  }
  
  @Test
  public void testMinMax() {
    assertMinMax("0", false);
    assertMinMax("0.1", true);
    assertMinMax("0.09", false);
    assertMinMax("999.9900001", false);
    assertMinMax("9.9999", true);
    assertMinMax("99999", false);    
  }

  @Test
  public void testDefaultStringFormat() {
    assertEquals("An un-set value provides a null.", null, myPm.bigDecMaxLen6.getValueAsString());
    myPm.bigDecMaxLen6.setValueAsString("0");

    assertEquals("Default format for zero.", "0", myPm.bigDecMaxLen6.getValueAsString());

    myPm.bigDecMaxLen6.setValueAsString("0.153");
    assertEquals("0.153", myPm.bigDecMaxLen6.getValueAsString());
  }
  
  @Test
  public void testFormatted() {
    assertEquals("An un-set value provides a null.", null, myPm.bigFormatted.getValueAsString());
    myPm.bigFormatted.setValueAsString("0");

    assertEquals("Default format for zero.", "0", myPm.bigFormatted.getValueAsString());

    myPm.bigFormatted.setValueAsString("0.123");
    assertEquals("0.123", myPm.bigFormatted.getValueAsString());
  }

  static class MyPm extends PmConversationImpl {
    public static final String READONLY_VALUE = "1.51";
    
    @PmAttrCfg(maxLen=6)
    public final PmAttrBigDecimal bigDecMaxLen6 = new PmAttrBigDecimalImpl(this);

    public final PmAttrBigDecimal bigDecimal = new PmAttrBigDecimalImpl(this);
    
    @PmAttrCfg(formatResKey="")
    public final PmAttrBigDecimal bigFormatted = new PmAttrBigDecimalImpl(this);
    
    @PmAttrBigDecimalCfg(minValueString="0.1", maxValueString="999.99")
    public final PmAttrBigDecimal minMaxAttr = new PmAttrBigDecimalImpl(this);
    
    @PmAttrCfg(readOnly = true)
    public final PmAttrBigDecimal readOnlyAttr = new PmAttrBigDecimalImpl(this) {
      protected BigDecimal getBackingValueImpl() {
        return new BigDecimal(READONLY_VALUE);
      }
    };

    @Override
    protected void onPmInit() {
      super.onPmInit();
      // FIXME olaf: there is an unresolved language issue.
      // The test does not work for GERMAN, even if the related resources are defined within a default resource file.
      setPmLocale(Locale.ENGLISH);
    }
  }

}
