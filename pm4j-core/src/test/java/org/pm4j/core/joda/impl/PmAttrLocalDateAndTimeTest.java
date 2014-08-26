package org.pm4j.core.joda.impl;

import static org.junit.Assert.assertEquals;
import static org.pm4j.tools.test.PmAssert.setValue;
import static org.pm4j.tools.test.PmAssert.setValueAsString;
import static org.pm4j.tools.test.PmAssert.validateNotSuccessful;
import static org.pm4j.tools.test.PmAssert.validateSuccessful;

import java.util.Locale;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.joda.impl.PmAttrLocalDateAndTimeImpl;

/**
 * Tests for {@link PmAttrLocalDateAndTimeImpl}.
 *
 * @author Olaf Boede
 */
public class PmAttrLocalDateAndTimeTest {

  private static final LocalDateTime FORBIDDEN_TIME_VALUE = new LocalDateTime(2014, 2, 2, 0, 0);

  private PmAttrLocalDateAndTimeImpl dt = new PmAttrLocalDateAndTimeImpl(new PmConversationImpl()) {
    /** Title can't be defined via annotation here. Works only on class or embedded PM field. */
    @Override
    protected String getPmTitleImpl() { return "DateTime"; }

    /** Special validation that fails at a defined test time. */
    @Override
    protected void validate(LocalDateTime value) throws PmValidationException {
      super.validate(value);
      if (FORBIDDEN_TIME_VALUE.equals(value)) {
        throw new PmValidationException(this, "forbidden value");
      }
    };
  };

  /** This test uses a fix English localization. */
  @Before
  public void setUp() {
    dt.getPmConversation().setPmLocale(Locale.ENGLISH);
  }

  @Test
  public void enterValidDateAndTimeValuesAsString() {
    setValueAsString(dt.datePart, "01/01/2013");
    setValueAsString(dt.timePart, "13:00");
    validateSuccessful(dt);
    assertEquals("01/01/2013 13:00", dt.getValueLocalized());
  }

  @Test
  public void enterValidDateTimeValueAsString() {
    setValueAsString(dt, "01/01/2013 13:00");
    validateSuccessful(dt);
    assertEquals("01/01/2013 13:00", dt.getValueLocalized());
  }

  @Test
  public void enterTimePartOnlyLeadsToMissingDateValidationError() {
    setValueAsString(dt.timePart, "11:00");
    validateNotSuccessful(dt, "Please enter a value into \"DateTime / pmAttrDateTime_datePart\".");
  }

  @Test
  public void enterDatePartOnlyLeadsToMissingTimeValidationError() {
    setValueAsString(dt.datePart, "01/01/2014");
    validateNotSuccessful(dt, "Please enter a value into \"DateTime / pmAttrDateTime_timePart\".");
  }

  /** Makes sure that one validation message gets replaced by a new one after a value change. */
  @Test
  public void enterValidateInvalidDateTimeAndThenRemoveTime() {
    setValue(dt, FORBIDDEN_TIME_VALUE);

    assertEquals("02/02/2014", dt.datePart.getValueLocalized());
    assertEquals("00:00", dt.timePart.getValueLocalized());
    assertEquals("02/02/2014 00:00", dt.getValueLocalized());
    validateNotSuccessful(dt, "forbidden value");

    setValueAsString(dt.timePart, null);

    assertEquals("02/02/2014", dt.datePart.getValueLocalized());
    assertEquals(null, dt.timePart.getValueLocalized());
    assertEquals("A complete DateTime string can't be provided if one part is null.", null, dt.getValueLocalized());
    validateNotSuccessful(dt, "Please enter a value into \"DateTime / pmAttrDateTime_timePart\".");
  }

}
