package org.pm4j.core.pm.joda;

import org.joda.time.LocalDateTime;
import org.pm4j.core.pm.PmAttr;

/**
 * A PM attribute that handles {@link LocalDateTime} values.
 *
 * @author olaf boede
 */
public interface PmAttrLocalDateTime extends PmAttr<LocalDateTime> {

  /**
   * Defines explicit maximum allowed length of the {@link String} representation.
   */
  public static final int MAX_LENGTH = 29;
  
  /**
   * The default format resource key that is used if no attribute specific
   * format is defined.
   */
  public static final String FORMAT_DEFAULT_RES_KEY = "pmAttrDateTime_defaultFormat";

}
