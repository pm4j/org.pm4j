package org.pm4j.core.pm;

import java.util.Locale;

import junit.framework.TestCase;

import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmAttrIntegerTest extends TestCase {

  public void testNullValue() {
    MyPm pm = new MyPm();

    assertEquals("Initial value should be null.", null, pm.i.getValue());
    assertEquals("Initial valueAsString should be null.", null, pm.i.getValueAsString());

    pm.i.setValueAsString("");

    assertEquals("valueAsString should be null, even if it was set to an empty string.", null, pm.i.getValueAsString());
  }

//  public void testFormatted() {
//    MyPm pm = new MyPm();
//
//    pm.i.setValueAsString("123");
//
//    assertEquals("123", pm.i.getValueAsString());
//  }
//
//  public void testWithMultiFormat() {
//    MyPm pm = new MyPm();
//    pm.setPmLocale(Locale.GERMAN);
//
//    pm.i.setValueAsString("1234,567");
//
//    // White box test of the format definition string:
//    assertEquals("#;#,###", ((PmAttrBase<?,?>)pm.i).getFormatString());
//
//    // decimal places will be cut off, default format adds separator every 3 digits:
//    assertEquals("1.234", pm.i.getValueAsString());
//
//    pm.i.setValueAsString("7654,123");
//    assertEquals("7.654", pm.i.getValueAsString());
//
//    pm.setPmLocale(Locale.ENGLISH);
//    pm.i.setValueAsString("7654.123");
//    assertEquals("7,654", pm.i.getValueAsString());
//
//  }

  public static class MyPm extends PmConversationImpl {
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);
  }

}
