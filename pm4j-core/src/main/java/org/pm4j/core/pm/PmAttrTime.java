package org.pm4j.core.pm;

import org.joda.time.LocalTime;

public interface PmAttrTime extends PmAttr<LocalTime> {

  /**
   * The default format resource key as defined in the Resources_xx.properties
   * file of the pm4j Project.
   */
  public static final String RESKEY_DEFAULT_FORMAT_PATTERN = "pmAttrTime_defaultFormat";

  /**
   * The last format definition provided by the format resource definition.<br>
   * E.g. 'myElem.myAttr_format=HH:mm|HH:mm:ss' would return here 'mm:HH:ss'
   * <p>
   * Is used for the method {@link PmAttr#getValueAsString()}.
   * <p>
   * It is also intended to be used by UI help constructs such as calendar
   * popups which provide their data as strings.
   *
   * @return The last item of the result of {@link #getParseFormats()}
   */
  String getOutputFormat();

}