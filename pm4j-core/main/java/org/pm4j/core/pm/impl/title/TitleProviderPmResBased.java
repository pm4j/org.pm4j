package org.pm4j.core.pm.impl.title;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.util.resource.ClassPathResourceStringUtil;
import org.pm4j.common.util.resource.StringFormatUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * Provides titles based on presentation model resource keys.
 *
 * @author olaf boede
 */
public class TitleProviderPmResBased<T extends PmObjectBase> implements PmTitleProvider<T> {

  /** Local logger */
  private static final Log log = LogFactory.getLog(TitleProviderPmResBased.class);

  /**
   * An instance that can be used as a singleton.
   */
  public static final TitleProviderPmResBased<PmObjectBase> INSTANCE = new TitleProviderPmResBased<PmObjectBase>();

  /**
   * @return <code>false</code>.
   */
  @Override
  public boolean canSetTitle(T item) {
    return false;
  }

  /**
   * @throws UnsupportedOperationException
   */
  public void setTitle(T item, String titleString) {
    throw new UnsupportedOperationException("Title can't be changed. Instance: " + item + "\n\tUsed title provider: "
        + getClass());
  }

  /**
   * {@inheritDoc}
   */
  public String getTitle(T item) {
    return getLocalization(item, item.getPmResKey());
  }

  /**
   * {@inheritDoc}
   */
  public String getShortTitle(T item) {
    String s = findLocalization(item, item.getPmResKey() + PmConstants.RESKEY_POSTFIX_SHORT_TITLE);

    return (s != null)
      ? s
      : getLocalization(item, item.getPmResKey());
  }

  /**
   * {@inheritDoc}
   */
  public String getToolTip(T item) {
    return findLocalization(item, item.getPmResKey() + PmConstants.RESKEY_POSTFIX_TOOLTIP);
  }

  /**
   * Provides an icon resource string that is defined within the resource file(s).
   * <p>
   * For enabled items a resource string with the postfix '.icon' will be used.<br>
   * For disabled item a resource string with the postfix '.icon_disabled' will be used.<br>
   * If there is no '.icon_disabled' resource defined, the '.icon' resource will be used
   * for the disabled state too.
   */
  public String getIconPath(T item) {
    if (item.isPmEnabled()) {
      String resKey = item.getPmResKey() + PmConstants.RESKEY_POSTFIX_ICON;
      return findLocalization(item, resKey);
    }
    else {
      String resKey = item.getPmResKey() + PmConstants.RESKEY_POSTFIX_ICON_DISABLED;
      String path = findLocalization(item, resKey);

      // Fallback: Use the standard icon for the disabled state too when there is no specific
      //           icon path defined.
      if (path == null) {
        resKey = item.getPmResKey() + PmConstants.RESKEY_POSTFIX_ICON;
        path = findLocalization(item, resKey);
      }

      return path;
    }
  }

  public String findLocalization(T item, String key, Object... resStringArgs) {
    return ClassPathResourceStringUtil.findString(getLocale(item), item.getPmResLoaderCtxtClasses(), key, resStringArgs);
  }

  public String findLocalization(T item, Locale locale, String key, Object... resStringArgs) {
    return ClassPathResourceStringUtil.findString(locale, item.getPmResLoaderCtxtClasses(), key, resStringArgs);
  };

  public String getLocalization(T item, String key, Object... resStringArgs) {
    Locale locale = getLocale(item);
    String s = ClassPathResourceStringUtil.findString(locale, item.getPmResLoaderCtxtClasses(), key, resStringArgs);

    if (s == null) {

      if (isLenient()) {
        if (log.isWarnEnabled()) {
          log.warn(makeMissingResourceWarning(item, key, resStringArgs));
        }

        // Try to use the key directly.
        // May work fine when the key is a resource string in a default language.
        s = StringFormatUtil.messageFormat(locale, key, resStringArgs);
      } else {
        throw new PmRuntimeException(makeMissingResourceWarning(item, key, resStringArgs));
      }
    }

    return s;
  }

  /**
   * Defines if missing resources should lead to an exception or only to a debug
   * log message.
   *
   * @return <code>true</code> when missing resources are accepted.
   */
  protected boolean isLenient() {
    // TODO: Should this flag be part of the PMDefaults...
    return true;
  }

  /**
   * Provides the locale by asking the item for its locale.
   * <p>
   * Subclasses may implement that logic differently.
   *
   * @param item The item.
   * @return The current locale.
   */
  protected Locale getLocale(T item) {
    return item.getPmConversation().getPmLocale();
  }

  /**
   * Message generation helper.
   *
   * @param item
   * @param key
   * @param resStringArgs
   * @return
   */
  private String makeMissingResourceWarning(T item, String key, Object... resStringArgs) {
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

}
