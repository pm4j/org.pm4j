package org.pm4j.core.pm.impl.converter;

import org.pm4j.core.pm.PmAttr;

/**
 * Common interface for format string based converters.
 *
 * @author olaf boede
 */
public interface MultiFormatConverter {

  /**
   * Provides the localized output format string for the given attribute.
   *
   * @param pmAttr the attribute to get the format for.
   * @return the output format.
   */
  public String getOutputFormat(PmAttr<?> pmAttr);

}
