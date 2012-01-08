package org.pm4j.common.util.resource;

import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.exception.PmRuntimeException;

public final class ClassPathResourceStringUtil {

  private static final Log log = LogFactory.getLog(ClassPathResourceStringUtil.class);

  public static String getString(Locale locale, Class<?> forClass, String key, Object... args) {
    String langRes = ClassPathResourceFinder.instance().getString(forClass, key, locale);
    String result = StringFormatUtil.messageFormat(locale, langRes, args);
    return result;
  }

  public static String findString(Locale locale, Class<?> forClass, String key, Object... args) {
    String langRes = ClassPathResourceFinder.instance().findString(forClass, key, locale);
    return (langRes != null)
      ? StringFormatUtil.messageFormat(locale, langRes, args)
      : null;
  }

  public static String findString(Locale locale, List<Class<?>> resLoadCtxtClasses, String key, Object... args) {
    String langRes = null;
    for (int i=0; i<resLoadCtxtClasses.size(); ++i) {
      langRes = findString(locale, resLoadCtxtClasses.get(i), key, args);
      if (langRes != null) {
        break;
      }
    }
    return langRes;
  }

  public static String getString(Locale locale, List<Class<?>> resLoadCtxtClasses, String key, Object... args) {
    String result = findString(locale, resLoadCtxtClasses, key, args);

    if (result == null) {
      String msg = "String resource for key '" + key + "' and locale '" + locale + "' not found. Class context: " + resLoadCtxtClasses;
      if (ClassPathResourceFinder.instance().isLenient()) {
        log.warn(msg);
        result = key;
      } else {
        throw new PmRuntimeException(msg);
      }
    }

    return result;
  }

}
