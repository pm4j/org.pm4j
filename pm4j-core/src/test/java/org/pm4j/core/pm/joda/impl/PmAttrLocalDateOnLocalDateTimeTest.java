package org.pm4j.core.pm.joda.impl;

import static org.junit.Assert.assertEquals;
import static org.pm4j.tools.test.PmAssert.setValueAsString;

import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmAttrLocalDateOnLocalDateTimeTest {

  private TestPm testPm = new TestPm();

  @Test
  public void testDt() {
    setValueAsString(testPm.dt, "01/01/2014");
    assertEquals("2014-01-01T00:00:00.000", testPm.dt.getBackingValue().toString());
    testPm.dt.setBackingValue(new LocalDateTime(2014, 01, 01, 23, 00));
    assertEquals("01/01/2014", testPm.dt.getValueAsString());
  }

  class TestPm extends PmConversationImpl {
    public final PmAttrLocalDateOnLocalDateTime dt = new PmAttrLocalDateOnLocalDateTime(this);

    public TestPm() {
      setPmLocale(Locale.ENGLISH);
      setPmTimeZone(TimeZone.getTimeZone("ETC-12"));
    }
  }

}
