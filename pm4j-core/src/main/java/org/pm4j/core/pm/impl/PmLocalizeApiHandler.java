package org.pm4j.core.pm.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.title.ClassPathResourceStringProvider;
import org.pm4j.core.pm.impl.title.ResourceStringProvider;

public class PmLocalizeApiHandler {

  private static final Log LOG = LogFactory.getLog(PmLocalizeApiHandler.class);

  private ResourceStringProvider resourceStringProvider = new ClassPathResourceStringProvider();

  private boolean lenient = true;

  /**
   * Provides a localization based on a key and option resource string arguments.
   * <p>
   * The result of that operation strongly depends on the kind of title provider used.
   *
   * @param key A resource key.
   * @param resStringArgs Optional resource string arguments.
   * @return The localized string or <code>null</code> when no localization is available.
   */
  public String findLocalization(PmObjectBase pm, String key, Object... resStringArgs) {
    Locale locale = getLocale(pm);
    String resourceString = resourceStringProvider.findResourceString(locale, pm.getPmResLoaderCtxtClasses(), key);
    return (resourceString != null)
              ? messageFormat(locale, resourceString, resStringArgs)
              : null;
  }

  /**
   * Provides a localization based on a key and option resource string arguments.
   * <p>
   * The result of that operation strongly depends on the kind of title provider used.
   *
   * @param key A resource key.
   * @param resStringArgs Optional resource string arguments.
   * @return The localized string.
   * @throws PmRuntimeException when no localization for the given key was found.
   */
  public String localize(PmObjectBase pm, String key, Object... resStringArgs) {
    String s = findLocalization(pm, key, resStringArgs);

    if (s == null) {

      if (lenient) {
        if (LOG.isWarnEnabled()) {
          LOG.warn(makeMissingResourceWarning(pm, key, resStringArgs));
        }

        // Return the key directly.
        s = key;
      } else {
        throw new PmRuntimeException(makeMissingResourceWarning(pm, key, resStringArgs));
      }
    }

    return s;
  }

  public void setResourceStringProvider(ResourceStringProvider resourceStringProvider) {
    this.resourceStringProvider = resourceStringProvider;
  }

  protected Locale getLocale(PmObject pm) {
    return pm.getPmConversation().getPmLocale();
  }

  /**
   * Message generation helper.
   *
   * @param item
   * @param key
   * @param resStringArgs
   * @return
   */
  // TODO olaf: the hint is resource provider specific. Delegate this call.
  private String makeMissingResourceWarning(PmObjectBase item, String key, Object... resStringArgs) {
    StringBuilder sb = new StringBuilder(200);
    sb.append("String resource for key '")
      .append(key)
      .append("' and locale '")
      .append(getLocale(item))
      .append("' not found. \n")
      .append("The following resouce folders (and their parent directories) have been considered:\n");

    for (Map.Entry<String, Class<?>> e : findRelevantResLoaderCtxtClasses(item.getPmResLoaderCtxtClasses()).entrySet()) {
      sb.append(" ").append(e.getKey()).append("\n");
    }

    sb.append(" PM context:").append(PmUtil.getPmLogString(item));

    return sb.toString();
  }

  private static Map<String, Class<?>> findRelevantResLoaderCtxtClasses(Collection<Class<?>> ctxtClasses) {
    Map<String, Class<?>> urlToClassMap = new LinkedHashMap<String, Class<?>>();

    for (Class<?> c : ctxtClasses) {
      while (c.getEnclosingClass() != null) {
        c = c.getEnclosingClass();
      }

      String classFilePath = "/" + c.getCanonicalName().replaceAll("\\.", "/") + ".class";
      String classFileUrl = ObjectUtils.toString(c.getResource(classFilePath));
      int classPostfixLength = c.getSimpleName().length() + ".class".length() + 1;

      String packageUrl = classFileUrl.substring(0, classFileUrl.length() - classPostfixLength);

      boolean isSubUrlOfOtherUrl = false;
      for (String url : new ArrayList<String>(urlToClassMap.keySet())) {
        if (url.startsWith(packageUrl)) {
          isSubUrlOfOtherUrl = true;
        }
        else {
          if (packageUrl.startsWith(url)) {
            urlToClassMap.remove(url);
          }
        }
      }

      if (! isSubUrlOfOtherUrl) {
        urlToClassMap.put(packageUrl, c);
      }
    }

    return urlToClassMap;
  }

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
  protected String messageFormat(Locale locale, String placeHolderString, Object... placeHolderArgs) {
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
