package org.pm4j.common.util.resource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.exception.PmRuntimeException;

/**
 * TODOC:
 */
public class ClassPathResourceFinder {

  private static final Log log = LogFactory.getLog(ClassPathResourceFinder.class);

  private boolean lenient = true;

  private String resFileBaseName = "Resources";

  /** Defines the way how language resources are found by the {@link ResourceBundle}. */
  private ResourceBundle.Control resBundleStrategy = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);

  /**
   * A map of caches for each resource scope (package).
   * That structure is required since the same key may be bound to different
   * values within different scope.
   */
  private Map<Object, ResStringCache> pkgToResStringCacheMap = new HashMap<Object, ResStringCache>();

  private static final ClassPathResourceFinder INSTANCE = new ClassPathResourceFinder();

  public static final ClassPathResourceFinder instance() {
    return INSTANCE;
  }

  public String getString(Class<?> forClass, String key, Locale locale) {
    String s = findString(forClass, key, locale);

    if (s == null) {
      String msg = "String resource for key '" + key + "' and locale '" + locale + "' not found. Class context: " + forClass;
      if (lenient) {
        log.warn(msg);
        s = key;
      } else {
        throw new PmRuntimeException(msg);
      }
    }

    return s;
  }

  /**
   * Finds a value for the given key and locale.
   * The forClass parameter is used to identify a localization scope.
   * A resource is searched within the archive and package of the given class.
   * If it is not found there if will be searched in the parent packages till
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

    ResStringCache resStringCache = getPkgResStringCache(forClass);
    ResStringCache.Entry cacheEntry = resStringCache.find(key, locale);

    if (cacheEntry != null) {
      return cacheEntry.getValue();
    }
    else {
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

      // remember the result in the cache:
      resStringCache.put(key, locale, result);
      return result;
    }
  }

  private ResStringCache getPkgResStringCache(Class<?> forClass) {
    Object cacheScopeKey = forClass.getPackage();
    if (cacheScopeKey == null) {
      log.warn("No package reference for class " + forClass);
      cacheScopeKey = ClassUtils.getPackageName(forClass);
    }

    ResStringCache cache = pkgToResStringCacheMap.get(cacheScopeKey);
    if (cache == null) {
      cache = new ResStringCache();
      pkgToResStringCacheMap.put(cacheScopeKey, cache);
    }
    return cache;
  }

  private String findResStringInPgk(String pkgDir, Locale locale, String key) {
    String result = null;
    ResourceBundle myResources = null;
    try {
      String bundleName = (StringUtils.isEmpty(pkgDir)) ? resFileBaseName : (pkgDir + "/" + resFileBaseName);

      myResources = ResourceBundle.getBundle(bundleName, locale, resBundleStrategy);
    } catch (MissingResourceException e) {
      // ok. resource does not extist. try the next package level.
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
