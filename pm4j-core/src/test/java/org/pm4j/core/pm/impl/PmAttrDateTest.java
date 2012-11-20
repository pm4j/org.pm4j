package org.pm4j.core.pm.impl;

import java.util.GregorianCalendar;
import java.util.Locale;

import junit.framework.TestCase;

import org.pm4j.core.pm.PmAttrDate;
import org.pm4j.core.pm.annotation.PmAttrCfg;

public class PmAttrDateTest extends TestCase {

  public static final class DateTestPm extends PmConversationImpl {

    /**
     * An attribute that uses the default date format. It is defined in
     * Resources_xx.properties of pm4j-core.
     */
    public final PmAttrDate withDefaultFormat = new PmAttrDateImpl(this);

    /**
     * An attribute that uses the a format defined by a resource key that has
     * the '.format' postfix.<br>
     * The format key for this nested class attribute:
     *
     * <pre>
     * pmAttrDateTest.DateTestPm.withResKeyPostfixFormat.format
     * </pre>
     *
     * Localizations are defined in Resources_xx.properties of this package.
     */
    public final PmAttrDate withResKeyPostfixFormat = new PmAttrDateImpl(this);

    /**
     * An attribute that uses the a format defined by a annotation based
     * resource key configuration.<br>
     * Localizations are defined in Resources_xx.properties of this package.
     */
    @PmAttrCfg(formatResKey = "my_fix_date_test_format")
    public final PmAttrDate withFixResKeyFormat = new PmAttrDateImpl(this);

    public final PmAttrDate withMultiFormat = new PmAttrDateImpl(this);
  }

  public void testDefaultFormatAttr() {
    DateTestPm pmElement = new DateTestPm();
    PmAttrDate pmAttr = pmElement.withDefaultFormat;

    pmAttr.setValue(new GregorianCalendar(1999, 8, 9).getTime());

    pmElement.setPmLocale(Locale.ENGLISH);

    // White box test of the format definition string:
    assertEquals("dd/MM/yyyy", ((PmAttrBase<?,?>)pmAttr).getFormatString());

    assertEquals("09/09/1999", pmAttr.getValueAsString());
    pmAttr.setValueAsString("01/02/1276");
    assertEquals("01/02/1276", pmAttr.getValueAsString());

    pmElement.setPmLocale(Locale.GERMAN);
    pmAttr.setValueAsString("09.09.1999");
    assertEquals("09.09.1999", pmAttr.getValueAsString());

    // Invalid value test:
    pmAttr.setValueAsString("asterix");
    assertEquals("asterix", pmAttr.getValueAsString());
    pmElement.clearPmInvalidValues();
    assertEquals("09.09.1999", pmAttr.getValueAsString());
  }

  public void testWithResKeyPostfixFormat() {
    DateTestPm pmElement = new DateTestPm();
    PmAttrDate pmAttr = pmElement.withResKeyPostfixFormat;

    pmAttr.setValue(new GregorianCalendar(1999, 8, 9).getTime());

    pmElement.setPmLocale(Locale.ENGLISH);

    // White box test of the format definition string:
    assertEquals("dd/MM/yy", ((PmAttrBase<?,?>)pmAttr).getFormatString());

    assertEquals("09/09/99", pmAttr.getValueAsString());
    pmAttr.setValueAsString("01/02/1276");
    assertEquals("01/02/76", pmAttr.getValueAsString());

    pmElement.setPmLocale(Locale.GERMAN);
    pmAttr.setValueAsString("09.09.99");
    assertEquals("09.09.99", pmAttr.getValueAsString());
  }

  public void testWithFixResKeyFormatt() {
    DateTestPm pmElement = new DateTestPm();
    PmAttrDate pmAttr = pmElement.withFixResKeyFormat;

    pmAttr.setValue(new GregorianCalendar(1999, 8, 9).getTime());

    pmElement.setPmLocale(Locale.ENGLISH);

    // White box test of the format definition string:
    assertEquals("yyyy-MM-dd'T'HH:mm:ss.SSSZ", ((PmAttrBase<?,?>)pmAttr).getFormatString());
    assertEquals("1999-09-09T00:00:00.000+0200", pmAttr.getValueAsString());
  }

  public void testWithMultiFormat() {
    DateTestPm pmElement = new DateTestPm();
    PmAttrDate pmAttr = pmElement.withMultiFormat;

    pmAttr.setValue(new GregorianCalendar(1999, 8, 9).getTime());

    pmElement.setPmLocale(Locale.GERMAN);

    // White box test of the format definition string:
    assertEquals("d. MMMMM yyyy|dd.MM.yy|yyyy/MM/dd|d. MMMMM yyyy", ((PmAttrBase<?,?>)pmAttr).getFormatString());

    assertEquals("9. September 1999", pmAttr.getValueAsString());

    pmAttr.setValueAsString("10.9.99");
    assertEquals("10. September 1999", pmAttr.getValueAsString());

    pmAttr.setValueAsString("11. Sep 1999");
    assertEquals("11. September 1999", pmAttr.getValueAsString());

    pmAttr.setValueAsString("1999/09/12");
    assertEquals("12. September 1999", pmAttr.getValueAsString());
}
}
