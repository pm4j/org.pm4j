package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.annotation.PmAttrBigDecimalCfg;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.impl.PmAttrBigDecimalImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

/**
 * If you modify this test, please consider at least {@link PmAttrDoubleTest}.
 * @author dzabel
 *
 */
public class PmAttrBigDecimalTest {
  
  private MyPm myPm;
  
  @Before
  public void setup() {
    myPm = new MyPm();
    myPm.setPmLocale(Locale.ENGLISH);
  }

  @Test
  public void testValueAccess() {
    assertNull("Initial value should be null", myPm.maxLen6.getValue());
    assertNull("Initial value as string should be null", myPm.maxLen6.getValueAsString());
    BigDecimal assignedValue = new BigDecimal("123");
    myPm.maxLen6.setValue(assignedValue);
    assertEquals("The assigned value should be the current one.", assignedValue, myPm.maxLen6.getValue());
    assertEquals("The assigned value should also appear as string.", "123", myPm.maxLen6.getValueAsString());
  }

  @Test
  public void testMaxLen() {
    assertEquals(6, myPm.maxLen6.getMaxLen());
  }

  
  @Test
  @Ignore("FIXME: Wrong locale is choosen, germany should use a dot as decimal divider. See property _de file.")
  public void testDefaultNoRoundingGermany() {
    myPm.getPmConversation().setPmLocale(Locale.GERMAN);
    myPm.bare.setValueAsString("123,56789");
    assertTrue("By default any BigDecimal should be valid.", myPm.bare.isPmValid());
    assertEquals("By default any BigDecimal should not be formatted.","123,56789", myPm.bare.getValueAsString());    
  }


  @Test
  public void testDefaultNoRoundingEnglish() {
    myPm.getPmConversation().setPmLocale(Locale.ENGLISH);
    myPm.bare.setValueAsString("123.56789");
    assertTrue("By default any BigDecimal should be valid.", myPm.bare.isPmValid());
    assertEquals("By default any BigDecimal should not be formatted.","123.56789", myPm.bare.getValueAsString());    
  }

  @Test
  public void testDefaultNoRoundingWithSetValue() {
    myPm.bare.setValue(new BigDecimal("123.56789"));
    assertTrue("By default any BigDecimal should be valid.", myPm.bare.isPmValid());
    assertEquals("By default any BigDecimal should not be formatted.","123.56789", myPm.bare.getValueAsString());
    assertEquals(new BigDecimal("123.56789"),myPm.bare.getValue());
  }
  
  @Test
  public void testReadOnly() {
    assertTrue(myPm.readOnlyAttr.isPmReadonly());
    assertEquals(new BigDecimal(MyPm.READONLY_VALUE), myPm.readOnlyAttr.getValue());    
    myPm.readOnlyAttr.setValue(new BigDecimal("0.01"));
    assertEquals(new BigDecimal(MyPm.READONLY_VALUE), myPm.readOnlyAttr.getValue());    
    assertTrue(myPm.readOnlyAttr.isPmValid());
    assertEquals(MyPm.READONLY_VALUE, myPm.readOnlyAttr.getValueAsString());
  }
  
  @Test
  public void testGetMinMax() {
    assertEquals(new BigDecimal("999.99"), myPm.minMaxAttr.getMax());
    assertEquals(new BigDecimal("0.1"), myPm.minMaxAttr.getMin());
  }
  
  @Test
  public void testGetMaxLen() {
    assertEquals(6, myPm.maxLen6.getMaxLen());    
  }
  
  private void assertValue(PmAttrBigDecimal pm, String number, boolean isValid) {
    pm.setValueAsString(number);
    pm.pmValidate();
    assertEquals(number, pm.getValueAsString());
    assertEquals(new BigDecimal(number), pm.getValue());
  }
  
 
  private void testMinMax(PmAttrBigDecimal pm) {
    assertValue(pm, "0", false);
    assertValue(pm, "0.1", true);
    assertValue(pm, "0.09", false);
    assertValue(pm, "999.9900001", false);
    assertValue(pm, "9.9999", true);
    assertValue(pm, "99999", false);    
  }

  @Test
  public void testMinMax() {
    testMinMax(myPm.minMaxAttr);
    testMinMax(myPm.minSingleValue);
    testMinMax(myPm.maxSingleValue);
  }
  
  @Test
  public void testRoundingHalfDown() {
    myPm.roundingHalfDown.setValueAsString("1.005");
    myPm.roundingHalfDown.pmValidate();
    assertEquals("0.005 will be removed because of the format and rounding", "1.00", myPm.roundingHalfDown.getValueAsString());
    assertEquals("Should not have been changed", new BigDecimal("1.005"), myPm.roundingHalfDown.getValue());
  }

  @Test
  public void testRoundingHalfUp() {
    myPm.roundingHalfUp.setValueAsString("1.005");
    myPm.roundingHalfUp.pmValidate();
    // the 0.005 will be rounded because of the format and rounding mode
    assertEquals("0.005 will be added because of the format and rounding", "1.01", myPm.roundingHalfUp.getValueAsString());
    assertEquals("Should not have been changed", new BigDecimal("1.005"), myPm.roundingHalfUp.getValue());
  }

  @Test
  public void testDefaultStringFormat() {
    assertEquals("An un-set value provides a null.", null, myPm.maxLen6.getValueAsString());
    myPm.maxLen6.setValueAsString("0");
    assertEquals("Default format for zero.", "0", myPm.maxLen6.getValueAsString());
    myPm.maxLen6.setValueAsString("0.153");
    assertEquals("0.153", myPm.maxLen6.getValueAsString());
  }
  
  @Test
  public void testFormatted() {
    assertEquals("An un-set value provides a null.", null, myPm.formatted.getValueAsString());
    myPm.formatted.setValueAsString("0");
    assertEquals("Default format for zero.", "0", myPm.formatted.getValueAsString());
    myPm.formatted.setValueAsString("0.123");
    assertEquals("0.123", myPm.formatted.getValueAsString());
  }


  
  static class MyPm extends PmConversationImpl {
    public static final String READONLY_VALUE = "1.51";

    @PmAttrBigDecimalCfg(minValueString="0.1")
    public final PmAttrBigDecimal minSingleValue = new PmAttrBigDecimalImpl(this);

    @PmAttrBigDecimalCfg(maxValueString="999.9")
    public final PmAttrBigDecimal maxSingleValue = new PmAttrBigDecimalImpl(this);

    @PmAttrCfg(formatResKey="pmAttrNumber_twoDecimalPlaces")
    @PmAttrBigDecimalCfg(stringConversionRoundingMode = RoundingMode.HALF_DOWN)
    public final PmAttrBigDecimal roundingHalfDown = new PmAttrBigDecimalImpl(this);

    @PmAttrCfg(formatResKey="pmAttrNumber_twoDecimalPlaces")
    @PmAttrBigDecimalCfg(stringConversionRoundingMode = RoundingMode.HALF_UP)
    public final PmAttrBigDecimal roundingHalfUp = new PmAttrBigDecimalImpl(this);
    
    @PmAttrCfg(maxLen=6)
    public final PmAttrBigDecimal maxLen6 = new PmAttrBigDecimalImpl(this);

    public final PmAttrBigDecimal bare = new PmAttrBigDecimalImpl(this);
    
    @PmAttrCfg(formatResKey="")
    public final PmAttrBigDecimal formatted = new PmAttrBigDecimalImpl(this);
    
    @PmAttrBigDecimalCfg(minValueString="0.1", maxValueString="999.99")
    public final PmAttrBigDecimal minMaxAttr = new PmAttrBigDecimalImpl(this);
    
    @PmAttrCfg(readOnly = true)
    public final PmAttrBigDecimal readOnlyAttr = new PmAttrBigDecimalImpl(this) {
      protected BigDecimal getBackingValueImpl() {
        return new BigDecimal(READONLY_VALUE);
      }
    };

  }

}
