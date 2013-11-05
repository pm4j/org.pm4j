package org.pm4j.core.joda;

import org.joda.time.LocalTime;
import org.pm4j.core.pm.PmAttr;

/**
 * A PM attribute that handles {@link LocalTime} values.
 *
 * @author Olaf Kossak
 * @since 0.6.12
 */
public interface PmAttrLocalTime extends PmAttr<LocalTime> {

  /**
   * The default format resource key that is used if no attribute specific
   * format is defined.
   */
  public static final String FORMAT_DEFAULT_RES_KEY = "pmAttrTime_defaultFormat";

}
