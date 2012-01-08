package org.pm4j.core.exception;


/**
 * Common interface for pm4j Exceptions.
 * <p>
 * It optionally may provide a human readable localized message.
 *
 * @author olaf boede
 */
public interface PmUserMessageException {

  /**
   * @return The localization resource information. <code>null</code> when
   *         no localization data is provided.
   */
  PmResourceData getResourceData();
}
