package org.pm4j.core.pm;

import java.util.Date;

public interface PmAttrDate extends PmAttr<Date> {
  
  /**
   * Defines the default maximum string length.
   * You may define your specific string length by defining it in @PmAttrCfg(maxLen).
   */
  public static final int MAX_LENGTH = 20;

  /**
   * The default format resource key as defined in the Resources_xx.properties
   * file of the pm4j Project.
   */
  public static final String RESKEY_DEFAULT_FORMAT_PATTERN = "pmAttrDate_defaultFormat";

  /**
   * The last format definition provided by the format resource definition.<br>
   * E.g. 'myElem.myAttr_format=yyyy.MM.dd;y.M.d' would return here 'y.M.d'
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