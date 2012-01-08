package org.pm4j.common.util.resource;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;

import org.pm4j.core.exception.PmRuntimeException;

/**
 * Helper functions for string formatting functionality.
 *
 * @author olaf boede
 */
public final class StringFormatUtil {

  /**
   * Uses the {@link MessageFormat} for string formatting.
   *
   * @param locale
   *          The locale used for formatting.
   * @param placeHolderString
   *          A template resource string with placeholders as documented in
   *          {@link MessageFormat}.
   * @param placeHolderArgs
   *          Optional placeholder arguments.
   * @return The formatted string.
   */
  public static String messageFormat(Locale locale, String placeHolderString, Object... placeHolderArgs) {
    if (placeHolderArgs.length == 0) {
      return placeHolderString;
    } else {
      try {
        MessageFormat mf = new MessageFormat(placeHolderString, locale);
        return mf.format(placeHolderArgs, new StringBuffer(placeHolderString.length()<<1), null).toString();
      }
      catch (RuntimeException e) {
        String msg =  "Unable to apply a MessageFormat for the following arguments: resString='" +
                      placeHolderString + "' args=" + Arrays.asList(placeHolderArgs);
        throw new PmRuntimeException(msg, e) ;
      }
    }
  }
}
