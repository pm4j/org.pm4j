package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.pm4j.tools.test.PmAssert.assertNoMessagesInSubTree;

import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmAttrIntegerTest {

  private MyPm pm = new MyPm();

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
  public void testNullToValueConversion() {
    pm.withNullConverter.setValueAsString("1");
    assertEquals(1, pm.withNullConverter.getBackingValue().intValue());
    assertEquals(1, pm.withNullConverter.getValue().intValue());

    pm.withNullConverter.setValueAsString(null);
    assertEquals(0, pm.withNullConverter.getBackingValue().intValue());
    assertEquals(null, pm.withNullConverter.getValue());
  }

  public static class MyPm extends PmConversationImpl {
    @PmAttrCfg(formatResKey="pmAttrIntegerTest.multiFormatTestFormat")
    public final PmAttrIntegerImpl i = new PmAttrIntegerImpl(this);

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
