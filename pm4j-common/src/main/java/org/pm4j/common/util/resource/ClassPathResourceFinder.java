package org.pm4j.common.util.resource;

import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Finds a resource string in packages related to a given class.
 */
public class ClassPathResourceFinder {

  private static final Logger LOG = LoggerFactory.getLogger(ClassPathResourceFinder.class);

  private boolean lenient = true;

  private String resFileBaseName = "Resources";

  /** Defines the way how language resources are found by the {@link ResourceBundle}. */
  private ResourceBundle.Control resBundleStrategy = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);

  private static final ClassPathResourceFinder INSTANCE = new ClassPathResourceFinder();

  public static final ClassPathResourceFinder instance() {
    return INSTANCE;
  }

  public String getString(Class<?> forClass, String key, Locale locale) {
    String s = findString(forClass, key, locale);

    if (s == null) {
      String msg = "String resource for key '" + key + "' and locale '" + locale + "' not found. Class context: " + forClass;
      if (lenient) {
        LOG.warn(msg);
        s = key;
      } else {
        throw new RuntimeException(msg);
      }
    }

    return s;
  }

  /**
   * Finds a value for the given key and locale.
   * The <code>forClass</code> parameter is used to identify a localization scope.
   * A resource is searched within the archive and package of the given class.
   * If it is not found there it will be searched within the parent packages till
   * the root package is reached.
   * <p>
   * To ensure a good performance, a cache ensures that the resource file search
   * algorithm will be performed only once per key.
   *
   * @param forClass
   * @param key
   * @param locale
   * @return
   */
  public String findString(Class<?> forClass, String key, Locale locale) {
    assert key != null;

    String result = null;
    String pkgName = ClassUtils.getPackageName(forClass);
    String relPkgDir = pkgName.replace('.', '/');

    Iterator<String> i = new CutStringTailIterator(relPkgDir, "/");

    while (i.hasNext() && (result == null)) {
      String pkgDir = i.next();

      result = findResStringInPgk(pkgDir, locale, key);
    }

    // finally try the root package
    if (result == null) {
      result = findResStringInPgk("", locale, key);
    }

    return result;
  }

  private String findResStringInPgk(String pkgDir, Locale locale, String key) {
    String result = null;
    ResourceBundle myResources = null;
    String bundleName = (StringUtils.isEmpty(pkgDir)) ? resFileBaseName : (pkgDir + "/" + resFileBaseName);
    try {
      myResources = ResourceBundle.getBundle(bundleName, locale, resBundleStrategy);
    } catch (MissingResourceException e) {
      // ok. resource does not exist. try the next package level.
      if (LOG.isTraceEnabled()) {
        LOG.trace(e.toString() + " {Bundle name: '" + bundleName + "', locale=" + locale + ", strategy: " + resBundleStrategy + "}");
      }
    }

    if (myResources != null) {
      try {
        result = myResources.getString(key);
      } catch (MissingResourceException e) {
        // ok. string was not in resource file. try the next hierarchy level.
      }
    }

    return result;
  }

  public ResourceBundle.Control getResBundleStrategy() {
    return resBundleStrategy;
  }

  public void setResBundleStrategy(ResourceBundle.Control resBundleStrategy) {
    this.resBundleStrategy = resBundleStrategy;
  }

  public boolean isLenient() {
    return lenient;
  }

  public void setLenient(boolean lenient) {
    this.lenient = lenient;
  }

}
