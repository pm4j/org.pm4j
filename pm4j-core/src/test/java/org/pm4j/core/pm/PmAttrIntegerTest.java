package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.pm4j.tools.test.PmAssert.assertNoMessagesInSubTree;
import static org.pm4j.tools.test.PmAssert.setValueAsString;

import java.util.Locale;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.tools.test.PmAssert;

public class PmAttrIntegerTest {

  private MyPm pm;
  
  @Before
  public void setup() {
    pm = new MyPm();
    pm.setPmLocale(Locale.ENGLISH);
  }

  @Test
  public void testNullValue() {
    assertEquals("Initial value should be null.", null, pm.i.getValue());
    assertEquals("Initial valueAsString should be null.", null, pm.i.getValueAsString());

    pm.i.setValueAsString("");

    assertEquals("valueAsString should be null, even if it was set to an empty string.", null, pm.i.getValueAsString());
  }

  @Test
  public void testValueType() {
    assertEquals(Integer.class, new MyPm().i.getValueType());
  }

  @Test
  public void testFormatted() {
    pm.i.setValueAsString("123");
    assertEquals("123", pm.i.getValueAsString());
  }

  @Test
  @Ignore("FIXME oboede: does not yet work!")
  public void testWithMultiFormat() {
    pm.setPmLocale(Locale.GERMAN);

    // White box test of the format definition string:
    assertEquals("#.###|#", pm.i.getFormatString());

    pm.i.setValueAsString("1234.567");
    assertNoMessagesInSubTree(pm.i);
    // decimal places will be cut off, default format adds separator every 3 digits:
    assertEquals("1234567", pm.i.getValueAsString());

    pm.i.setValueAsString("7654,123");
    assertEquals("7654", pm.i.getValueAsString());

    pm.setPmLocale(Locale.ENGLISH);
    assertEquals("#.###|#", pm.i.getFormatString());
    pm.i.setValueAsString("7654.123");
    assertEquals("7654", pm.i.getValueAsString());
  }

  @Test
  public void testDefaultValueAndResetValue() {
    // The default value will only be applied on getValue/getValueAsString/resetPmValues calls.
    assertEquals(null, pm.withDefaultValue.getBackingValue());
    assertEquals("3", pm.withDefaultValue.getValueAsString());
    assertEquals(new Integer(3), pm.withDefaultValue.getBackingValue());
    setValueAsString(pm.withDefaultValue, "4");
    // In changed state the default value will not re-appear. The user manually changed the
    // value to 'null'.
    setValueAsString(pm.withDefaultValue, null);

    // resetPmValues resets every attribute value to its default.
    pm.withDefaultValue.resetPmValues();
    assertEquals(new Integer(3), pm.withDefaultValue.getBackingValue());
    assertEquals("3", pm.withDefaultValue.getValueAsString());
  }

  @Test
  public void testDefaultValueAndReactivateDefaultOnAllChangeEvent() {
    assertEquals(null, pm.withDefaultValue.getBackingValue());
    assertEquals("3", pm.withDefaultValue.getValueAsString());
    setValueAsString(pm.withDefaultValue, null);
    // An all change event resets the changed state of the attribute.
    PmEventApi.firePmEvent(pm.withDefaultValue, PmEvent.ALL_CHANGE_EVENTS);
    assertEquals("3", pm.withDefaultValue.getValueAsString());
  }

  @Test
  public void testNullToValueConversion() {
    pm.withNullConverter.setValueAsString("1");
    assertEquals(1, pm.withNullConverter.getBackingValue().intValue());
    assertEquals(1, pm.withNullConverter.getValue().intValue());

    pm.withNullConverter.setValueAsString(null);
    assertEquals(0, pm.withNullConverter.getBackingValue().intValue());
    assertEquals(null, pm.withNullConverter.getValue());
  }
  
  @Test
  public void testMinLength() {
    
    //Check annotations
    assertEquals(2, pm.minLen2.getMinLen());

    //Validate too short
    pm.minLen2.setValue(1);
    PmAssert.validateNotSuccessful(pm.minLen2, "Please enter at least 2 characters in field \"pmAttrIntegerTest.MyPm.minLen2\".");

    //Validate correct
    pm.minLen2.setValue(12);
    PmAssert.validateSuccessful(pm.minLen2);
  }

  @Test
  public void testMaxLength() {
    
    //Check annotations
    assertEquals(6, pm.maxLen6.getMaxLen());

    //Validate too big
    pm.maxLen6.setValue(1234567);
    PmAssert.validateNotSuccessful(pm.maxLen6, "Please enter maximal 6 characters in field \"pmAttrIntegerTest.MyPm.maxLen6\".");
    
    //Validate correct
    pm.maxLen6.setValue(123456);
    PmAssert.validateSuccessful(pm);

  }
  
  public static class MyPm extends PmConversationImpl {
    
    @PmAttrCfg(formatResKey="pmAttrIntegerTest.multiFormatTestFormat")
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);
    
    @PmAttrCfg(minLen=2)
    public final PmAttrIntegerImpl minLen2 = new PmAttrIntegerImpl(this);
    
    @PmAttrCfg(maxLen=6)
    public final PmAttrIntegerImpl maxLen6 = new PmAttrIntegerImpl(this);

    @PmAttrCfg(defaultValue="3")
    public final PmAttrIntegerImpl withDefaultValue = new PmAttrIntegerImpl(this);

    /** A very special attribute that translates <code>null</code> to zero. */
    public final PmAttrIntegerImpl withNullConverter = new PmAttrIntegerImpl(this) {
      @Override
      protected boolean isConvertingNullValueImpl() {
        return true;
      }

      public Integer convertBackingValueToPmValue(Integer backingValue) {
        return backingValue != null && backingValue == 0
            ? null : backingValue;
      }

      public Integer convertPmValueToBackingValue(Integer externalValue) {
        return externalValue == null ? 0 : externalValue;
      }
    };
  }

}
