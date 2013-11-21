package org.pm4j.core.pm;

import java.math.RoundingMode;

public interface PmAttrNumber<T extends Number> extends PmAttr<T> {

  public static final RoundingMode ROUNDINGMODE_DEFAULT = RoundingMode.UNNECESSARY;

  /**
   * The default format resource key as defined in the Resources_xx.properties
   * file of the pm4j Project. Used as default for all floating point numbers.
   * <p>
   * You may define your own default format within your resource files.
   */
  public static final String RESKEY_DEFAULT_FLOAT_FORMAT_PATTERN = "pmAttrNumber_defaultFloatFormat";

  /**
   * The default format resource key as defined in the Resources_xx.properties
   * file of the pm4j Project. Used as default for all integer numbers.
   * <p>
   * You may define your own default format within your resource files.
   */
  public static final String RESKEY_DEFAULT_INTEGER_FORMAT_PATTERN = "pmAttrNumber_defaultIntegerFormat";

  /**
   * @return The maximum value for this attribute.
   */
  T getMaxValue();

  /**
   * @return The minimum value for this attribute.
   */
  T getMinValue();
}
