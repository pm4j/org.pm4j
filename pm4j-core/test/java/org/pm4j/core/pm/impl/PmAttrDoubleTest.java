package org.pm4j.core.pm.impl;

import java.util.Locale;

import junit.framework.TestCase;

import org.pm4j.core.pm.PmAttrDouble;

public class PmAttrDoubleTest extends TestCase {

  static class TestSession extends PmConversationImpl {
    public final PmAttrDouble d = new PmAttrDoubleImpl(this);
  }

  public void testFormatENGLISH() {
    TestSession s = new TestSession();

    s.setPmLocale(Locale.ENGLISH);
    assertEquals(null, s.d.getValueAsString());
    s.d.setValueAsString("1.23");
    assertEquals(1.23, s.d.getValue());
    assertEquals("1.23", s.d.getValueAsString());
  }

  public void testFormatGERMAN() {
    TestSession s = new TestSession();

    s.setPmLocale(Locale.GERMAN);
    s.d.setValueAsString("1,24");
    assertEquals(1.24, s.d.getValue());
    assertEquals("1,24", s.d.getValueAsString());

    s.d.setValueAsString("3,4");
    assertEquals(3.4, s.d.getValue());
    assertEquals("3,4", s.d.getValueAsString());
  }

  public void testSetAsStringWithInvalidChar() {
    TestSession s = new TestSession();
    s.setPmLocale(Locale.ENGLISH);

    s.d.setValueAsString("1.23a");

    // FIXME olaf: letters shouldn't be accepted!
    // assertEquals(false, s.isPmValid());
  }

  public void testWithMultiFormat() {
    TestSession s = new TestSession();
    s.setPmLocale(Locale.GERMAN);

    PmAttrDouble pmAttr = s.d;
    pmAttr.setValueAsString("1234,567");

    // White box test of the format definition string:
    assertEquals("####.##;#,###.##", ((PmAttrBase<?,?>)pmAttr).getFormatString());

    assertEquals("1.234,57", pmAttr.getValueAsString());

    pmAttr.setValueAsString("7654,123");
    assertEquals("7.654,12", pmAttr.getValueAsString());

    s.setPmLocale(Locale.ENGLISH);
    pmAttr.setValueAsString("7654.123");
    assertEquals("7,654.12", pmAttr.getValueAsString());

  }

}
