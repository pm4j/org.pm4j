package org.pm4j.core.pm.impl;

import java.util.TimeZone;

/**
 * Common interface for PMs that provide {@link TimeZone} information.
 *
 * @author oboede
 */
public interface PmWithTimeZone {

  /**
   * The time zone used for this PM.
   *
   * @return The {@link TimeZone}. Never <code>null</code>.
   */
  TimeZone getPmTimeZone();

}
