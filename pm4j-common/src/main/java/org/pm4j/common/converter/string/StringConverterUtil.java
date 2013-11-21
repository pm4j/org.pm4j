package org.pm4j.common.converter.string;

import org.apache.commons.lang.StringUtils;

/**
 * String converter helper methods.
 *
 * @author Olaf Boede
 */
public class StringConverterUtil {

  /** The default separator string used in case of a multi-format resource string specification. */
  private static String formatSplitString = "|";

  public static void setFormatSplitString(String s) {
    assert s != null && s.length() > 0;
    formatSplitString = s;
  }

  /**
   * Default implementation, may be overridden by implementations:
   * The last format definition returned by {@link #getParseFormats(PmAttr)}.
   * <p>
   * Is used for the method {@link PmAttr#getValueAsString()}.
   * <p>
   * It is also intended to be used by UI help constructs such as calendar
   * popups which provide their data as strings.
   *
   * @return The last item of the result of {@link #getParseFormats(PmAttr)}
   */
  public static String getOutputFormat(StringConverterCtxt ctxt) {
    String[] formats = getParseFormats(ctxt);
    return formats[formats.length-1];
  }

  /**
   * Builds an array of all format strings for the <code>pmAttr</code>.
   * @param pmAttr The pmAttr.
   * @return Format strings.
   */
  public static String[] getParseFormats(StringConverterCtxt ctxt) {
    String allFormatsString = StringUtils.defaultString(ctxt.getConverterCtxtFormatString());
    String[] formats = StringUtils.split(allFormatsString, formatSplitString);
    return formats;
  }

}
