package org.pm4j.core.pm.joda;

import org.joda.time.LocalDate;
import org.pm4j.core.pm.PmAttr;

/**
 * A PM attribute that handles {@link LocalDate} values.
 *
 * @author olaf boede
 */
public interface PmAttrLocalDate extends PmAttr<LocalDate> {

  /**
   * Defines explicit maximum allowed length of the {@link String} representation.
   */
  public static final int MAX_LENGTH = 20;
  
  /**
   * The default format resource key that is used if no attribute specific
   * format is defined.
   */
  public static final String FORMAT_DEFAULT_RES_KEY = "pmAttrDate_defaultFormat";

}
