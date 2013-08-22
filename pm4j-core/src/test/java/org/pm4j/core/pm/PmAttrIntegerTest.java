package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmAttrIntegerTest {

  @Test
  public void testNullValue() {
    MyPm pm = new MyPm();

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
    MyPm pm = new MyPm();
    pm.i.setValueAsString("123");
    assertEquals("123", pm.i.getValueAsString());
  }

  @Test @Ignore("FIXME oboede: does not yet work!")
  public void testWithMultiFormat() {
    MyPm pm = new MyPm();
    pm.setPmLocale(Locale.GERMAN);


    // White box test of the format definition string:
    assertEquals("#.###|#", ((PmAttrBase<?,?>)pm.i).getFormatString());

    pm.i.setValueAsString("1234.567");
    // decimal places will be cut off, default format adds separator every 3 digits:
    assertEquals("1234567", pm.i.getValueAsString());

    pm.i.setValueAsString("7654,123");
    assertEquals("7654", pm.i.getValueAsString());

    pm.setPmLocale(Locale.ENGLISH);
    assertEquals("#.###|#", ((PmAttrBase<?,?>)pm.i).getFormatString());
    pm.i.setValueAsString("7654.123");
    assertEquals("7654", pm.i.getValueAsString());
  }

  public static class MyPm extends PmConversationImpl {
    @PmAttrCfg(formatResKey="pmAttrIntegerTest.multiFormatTestFormat")
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);
  }

}
