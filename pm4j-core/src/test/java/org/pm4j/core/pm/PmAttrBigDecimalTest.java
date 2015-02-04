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
import org.pm4j.tools.test._PmAssert;

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
  @Ignore("oboede: deactivated because of intergration problems.")
  public void testMinLength() {

    //Check annotations
    assertEquals(2, myPm.minLen2.getMinLen());

    //Validate too short
    myPm.minLen2.setValue(new BigDecimal("1"));
    _PmAssert.validateNotSuccessful(myPm.minLen2, "Please enter at least 2 characters in field \"pmAttrBigDecimalTest.MyPm.minLen2\".");

    //Validate correct
    myPm.minLen2.setValue(new BigDecimal("12"));
    _PmAssert.validateSuccessful(myPm.minLen2);
  }

  public void testMaxLength() {

    //Check annotations
    assertEquals(6, myPm.maxLen6.getMaxLen());

    //Validate too big
    myPm.maxLen6.setValue(new BigDecimal("1234567"));
    _PmAssert.validateNotSuccessful(myPm.maxLen6, "Please enter maximal 6 characters in field \"pmAttrBigDecimalTest.MyPm.maxLen6\".");

    //Validate correct
    myPm.maxLen6.setValue(new BigDecimal("123456"));
    _PmAssert.validateSuccessful(myPm.maxLen6);
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
  public void testGetMinMaxValue() {
    assertEquals(new BigDecimal("999.99"), myPm.minMaxAttr.getMaxValue());
    assertEquals(new BigDecimal("0.1"), myPm.minMaxAttr.getMinValue());
  }

  private void assertValue(PmAttrBigDecimal pm, String number, boolean isValid) {
    pm.setValueAsString(number);
    pm.pmValidate();
    assertEquals(number, pm.getValueAsString());
    assertEquals(new BigDecimal(number), pm.getValue());
  }

  @Test
  public void testMinMaxValue() {
    testMinMaxValue(myPm.minMaxAttr);
    testMinMaxValue(myPm.minSingleValue);
    testMinMaxValue(myPm.maxSingleValue);
  }

  private void testMinMaxValue(PmAttrBigDecimal pm) {
    assertValue(pm, "0", false);
    assertValue(pm, "0.1", true);
    assertValue(pm, "0.09", false);
    assertValue(pm, "999.9900001", false);
    assertValue(pm, "9.9999", true);
    assertValue(pm, "99999", false);
  }

  @Test
  public void testRoundingHalfDown() {
    myPm.roundingHalfDown.setValueAsString("1.005");
    assertEquals("0.005 will be removed because of the format and rounding", "1.00", myPm.roundingHalfDown.getValueAsString());
    assertEquals("Should have been rounded and formatted", new BigDecimal("1.00"), myPm.roundingHalfDown.getValue());
  }

  @Test
  public void testRoundingHalfUpThreeDecimalPlaces() {
    myPm.roundingHalfUp.setValueAsString("1.005");
    // the 0.005 will be rounded because of the format and rounding mode
    assertEquals("0.005 will be added because of the format and rounding", "1.01", myPm.roundingHalfUp.getValueAsString());
    assertEquals("Should have been rounded and formatted", new BigDecimal("1.01"), myPm.roundingHalfUp.getValue());
  }

  @Test
  public void testRoundingHalfUpFourDecimalPlaces() {
    myPm.roundingHalfUp.setValueAsString("12.3456");
    assertEquals("Four decimal places shall be rounded to two", "12.35", myPm.roundingHalfUp.getValueAsString());
    assertEquals("BigDecimal shall be identical to string value", new BigDecimal("12.35"), myPm.roundingHalfUp.getValue());
  }

  @Test
  public void testRoundingUnnecessary() {
    myPm.roundingUnnecessary.setValueAsString("1.005");
    // the 0.005 will be rounded because of the format and rounding mode
    assertEquals("Should not have been changed", "1.005", myPm.roundingUnnecessary.getValueAsString());
    assertEquals("Should be null", null, myPm.roundingUnnecessary.getValue());
    _PmAssert.assertSingleErrorMessage(myPm.roundingUnnecessary, "Unable to convert the entered string to a numeric value in field \"1.005\".");
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
  public void testCombination() {
    assertTrue(myPm.combination.isPmReadonly());
    assertEquals(new BigDecimal(MyPm.READONLY_VALUE), myPm.combination.getValue());
    myPm.combination.setValue(new BigDecimal("0.01"));
    assertEquals(new BigDecimal(MyPm.READONLY_VALUE), myPm.combination.getValue());
    assertTrue(myPm.combination.isPmValid());
    assertEquals(MyPm.READONLY_VALUE_ROUNDED, myPm.combination.getValueAsString());
  }

  static class MyPm extends PmConversationImpl {
    public static final String READONLY_VALUE = "1.515";
    public static final String READONLY_VALUE_ROUNDED = "1.52";

    @PmAttrBigDecimalCfg(minValue="0.1")
    public final PmAttrBigDecimal minSingleValue = new PmAttrBigDecimalImpl(this);

    @PmAttrBigDecimalCfg(maxValue="999.9")
    public final PmAttrBigDecimal maxSingleValue = new PmAttrBigDecimalImpl(this);

    @PmAttrCfg(formatResKey="pmAttrNumberTest_twoDecimalPlaces")
    @PmAttrBigDecimalCfg(roundingMode = RoundingMode.HALF_DOWN)
    public final PmAttrBigDecimal roundingHalfDown = new PmAttrBigDecimalImpl(this);

    @PmAttrCfg(formatResKey="pmAttrNumberTest_twoDecimalPlaces")
    @PmAttrBigDecimalCfg(roundingMode = RoundingMode.HALF_UP)
    public final PmAttrBigDecimal roundingHalfUp = new PmAttrBigDecimalImpl(this);

    @PmAttrCfg(formatResKey="pmAttrNumberTest_twoDecimalPlaces")
    public final PmAttrBigDecimal roundingUnnecessary = new PmAttrBigDecimalImpl(this);

    @PmAttrCfg(minLen=2)
    public final PmAttrBigDecimal minLen2 = new PmAttrBigDecimalImpl(this);

    @PmAttrCfg(maxLen=6)
    public final PmAttrBigDecimal maxLen6 = new PmAttrBigDecimalImpl(this);

    public final PmAttrBigDecimal bare = new PmAttrBigDecimalImpl(this);

    @PmAttrCfg(formatResKey="")
    public final PmAttrBigDecimal formatted = new PmAttrBigDecimalImpl(this);

    @PmAttrBigDecimalCfg(minValue="0.1", maxValue="999.99")
    public final PmAttrBigDecimal minMaxAttr = new PmAttrBigDecimalImpl(this);

    @PmAttrCfg(readOnly = true)
    public final PmAttrBigDecimal readOnlyAttr = new PmAttrBigDecimalImpl(this) {
      @Override
      protected BigDecimal getBackingValueImpl() {
        return new BigDecimal(READONLY_VALUE);
      }
    };

    // Check old style as well.
    @PmAttrCfg(readOnly = true, formatResKey="pmAttrNumberTest_twoDecimalPlaces")
    @PmAttrBigDecimalCfg(stringConversionRoundingMode = RoundingMode.HALF_UP, minValueString = "0", maxValueString = "2.01")
    public final PmAttrBigDecimal combination = new PmAttrBigDecimalImpl(this) {
        @Override
        protected BigDecimal getBackingValueImpl() {
          return new BigDecimal(READONLY_VALUE);
        }
    };
  }

}
