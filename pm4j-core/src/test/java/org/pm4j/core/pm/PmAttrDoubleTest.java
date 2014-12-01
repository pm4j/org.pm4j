package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.RoundingMode;
import java.util.Locale;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmAttrDoubleCfg;
import org.pm4j.core.pm.impl.PmAttrDoubleImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.tools.test.PmAssert;

/**
 * If you modify this test, please consider at least {@link PmAttrDoubleTest}.
 * @author dzabel
 *
 */
public class PmAttrDoubleTest {

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
    Double assignedValue = new Double("123");
    myPm.maxLen6.setValue(assignedValue);
    assertEquals("The assigned value should be the current one.", assignedValue, myPm.maxLen6.getValue());
    assertEquals("The assigned value should also appear as string.", "123", myPm.maxLen6.getValueAsString());
  }

  @Test
  @Ignore("FIXME: Wrong locale is choosen, germany should use a dot as decimal divider. See property _de file.")
  public void testDefaultNoRoundingGermany() {
    myPm.getPmConversation().setPmLocale(Locale.GERMAN);
    myPm.bare.setValueAsString("123,56789");
    assertTrue("By default any Double should be valid.", myPm.bare.isPmValid());
    assertEquals("By default any Double should not be formatted.","123,56789", myPm.bare.getValueAsString());
  }

  @Test
  public void testDefaultNoRoundingEnglish() {
    myPm.getPmConversation().setPmLocale(Locale.ENGLISH);
    myPm.bare.setValueAsString("123.56789");
    assertTrue("By default any Double should be valid.", myPm.bare.isPmValid());
    assertEquals("By default any Double should not be formatted.","123.56789", myPm.bare.getValueAsString());
  }

  @Test
  public void testDefaultNoRoundingWithSetValue() {
    myPm.bare.setValue(new Double("123.56789"));
    assertTrue("By default any Double should be valid.", myPm.bare.isPmValid());
    assertEquals("By default any Double should not be formatted.","123.56789", myPm.bare.getValueAsString());
    assertEquals(new Double("123.56789"),myPm.bare.getValue());
  }

  @Test
  public void testReadOnly() {
    assertTrue(myPm.readOnlyAttr.isPmReadonly());
    assertEquals(new Double(MyPm.READONLY_VALUE), myPm.readOnlyAttr.getValue());
    myPm.readOnlyAttr.setValue(new Double("0.01"));
    assertEquals(new Double(MyPm.READONLY_VALUE), myPm.readOnlyAttr.getValue());
    assertTrue(myPm.readOnlyAttr.isPmValid());
    assertEquals(MyPm.READONLY_VALUE, myPm.readOnlyAttr.getValueAsString());
  }

  @Test
  public void testGetMinMaxValue() {
    assertEquals(new Double("999.99"), myPm.minMaxAttr.getMaxValue());
    assertEquals(new Double("0.1"), myPm.minMaxAttr.getMinValue());
  }

  private void assertValue(PmAttrDouble pm, String number, boolean isValid) {
    pm.setValueAsString(number);
    pm.pmValidate();
    assertEquals(number, pm.getValueAsString());
    assertEquals(new Double(number), pm.getValue());
  }

  @Test
  public void testMinMaxValue() {
    testMinMaxValue(myPm.minMaxAttr);
    testMinMaxValue(myPm.minSingleValue);
    testMinMaxValue(myPm.maxSingleValue);
  }

  private void testMinMaxValue(PmAttrDouble pm) {
    assertValue(pm, "0", false);
    assertValue(pm, "0.1", true);
    assertValue(pm, "0.09", false);
    assertValue(pm, "999.9900001", false);
    assertValue(pm, "9.9999", true);
    assertValue(pm, "99999", false);
  }
  
  @Test
  public void testMinLenght() {
    
    //Check annotations
    assertEquals(2, myPm.minLen2.getMinLen());

    //Validate too short
    myPm.minLen2.setValue(1d);
    PmAssert.validateNotSuccessful(myPm.minLen2, "Please enter at least 2 characters in field \"pmAttrDoubleTest.MyPm.minLen2\".");

    //Validate correct
    myPm.minLen2.setValue(12d);
    PmAssert.validateSuccessful(myPm.minLen2);
  }
  
  @Test
  public void testMaxLength() {
    
    //Check annotations
    assertEquals(6, myPm.maxLen6.getMaxLen());

    //Validate too big
    myPm.maxLen6.setValue(1234567d);
    PmAssert.validateNotSuccessful(myPm.maxLen6, "Please enter maximal 6 characters in field \"pmAttrDoubleTest.MyPm.maxLen6\".");
    
    //Validate correct
    myPm.maxLen6.setValue(123456d);
    PmAssert.validateSuccessful(myPm);
  }


  @Test
  public void testRoundingHalfDown() {
    myPm.roundingHalfDown.setValueAsString("1.005");
    myPm.roundingHalfDown.pmValidate();
    assertEquals("0.005 will be removed because of the format and rounding", "1.00", myPm.roundingHalfDown.getValueAsString());
    assertEquals("Should have been rounded and formatted", new Double("1.0"), myPm.roundingHalfDown.getValue());
  }

  @Test
  public void testRoundingHalfUp() {
    myPm.roundingHalfUp.setValueAsString("1.005");
    myPm.roundingHalfUp.pmValidate();
    // the 0.005 will be rounded because of the format and rounding mode
    assertEquals("0.005 will be added because of the format and rounding", "1.01", myPm.roundingHalfUp.getValueAsString());
    assertEquals("Should have been rounded and formatted", new Double("1.01"), myPm.roundingHalfUp.getValue());
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

  @Test
  public void testValidateZero() {    
    myPm.roundingHalfUp.setValueAsString("0.0");
    myPm.roundingHalfUp.pmValidate();
    myPm.roundingHalfUp.isPmValid();
    assertTrue(myPm.roundingHalfUp.isPmValid());
  }

  @Test
  public void testCombination() {
    assertTrue(myPm.combination.isPmReadonly());
    assertEquals(new Double(MyPm.READONLY_VALUE), myPm.combination.getValue());
    myPm.combination.setValue(new Double("0.01"));
    assertEquals(new Double(MyPm.READONLY_VALUE), myPm.combination.getValue());
    assertTrue(myPm.combination.isPmValid());
    assertEquals(MyPm.READONLY_VALUE_ROUNDED, myPm.combination.getValueAsString());
  }

  static class MyPm extends PmConversationImpl {
    public static final String READONLY_VALUE = "1.515";
    public static final String READONLY_VALUE_ROUNDED = "1.52";

    @PmAttrDoubleCfg(minValue=0.1)
    public final PmAttrDouble minSingleValue = new PmAttrDoubleImpl(this);

    @PmAttrDoubleCfg(maxValue=999.9)
    public final PmAttrDouble maxSingleValue = new PmAttrDoubleImpl(this);

    @PmAttrCfg(formatResKey="pmAttrNumberTest_twoDecimalPlaces")
    @PmAttrDoubleCfg(roundingMode = RoundingMode.HALF_DOWN)
    public final PmAttrDouble roundingHalfDown = new PmAttrDoubleImpl(this);

    @PmAttrCfg(formatResKey="pmAttrNumberTest_twoDecimalPlaces")
    @PmAttrDoubleCfg(roundingMode = RoundingMode.HALF_UP)
    public final PmAttrDouble roundingHalfUp = new PmAttrDoubleImpl(this);

    @PmAttrCfg(minLen=2)
    public final PmAttrDouble minLen2 = new PmAttrDoubleImpl(this);
    
    @PmAttrCfg(maxLen=6)
    public final PmAttrDouble maxLen6 = new PmAttrDoubleImpl(this);

    public final PmAttrDouble bare = new PmAttrDoubleImpl(this);

    @PmAttrCfg(formatResKey="")
    public final PmAttrDouble formatted = new PmAttrDoubleImpl(this);

    @PmAttrDoubleCfg(minValue=0.1, maxValue=999.99)
    public final PmAttrDouble minMaxAttr = new PmAttrDoubleImpl(this);

    @PmAttrCfg(readOnly = true)
    public final PmAttrDouble readOnlyAttr = new PmAttrDoubleImpl(this) {
      protected Double getBackingValueImpl() {
        return new Double(READONLY_VALUE);
      }
    };

    // Check old style as well.
    @PmAttrCfg(readOnly = true, formatResKey="pmAttrNumberTest_twoDecimalPlaces")
    @PmAttrDoubleCfg(stringConversionRoundingMode = RoundingMode.HALF_UP, minValue = 0, maxValue = 2.01)
    public final PmAttrDouble combination = new PmAttrDoubleImpl(this) {
        @Override
        protected Double getBackingValueImpl() {
          return new Double(READONLY_VALUE);
        }
    };

  }

  @Test
  public void testValueType() {
    assertEquals(Double.class, myPm.bare.getValueType());
  }

}
