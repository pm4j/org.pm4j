package org.pm4j.core.pm.impl.title;

import java.util.List;
import java.util.Locale;

import org.pm4j.common.util.resource.ClassPathResourceFinder;

public class ClassPathResourceStringProvider implements ResourceStringProvider {

  /**
   * A map of caches for each resource scope (package).
   * That structure is required since the same key may be bound to different
   * values within different scope.
   */
  private ResStringCacheWithScopes cache = new ResStringCacheWithScopes();

  @Override
  public String findResourceString(Locale locale, List<Class<?>> resLoadCtxtClasses, String key) {
    Object scopeObj = getScopeObj(resLoadCtxtClasses);
    ResStringCache.Entry cacheEntry = cache.find(scopeObj, key, locale);

    if (cacheEntry == null) {
      String foundString = findResourceStringInPackagePath(locale, resLoadCtxtClasses, key);
      cacheEntry = cache.put(scopeObj, key, locale, foundString);
    }

    return cacheEntry.getValue();
  }

  protected String findResourceStringInPackagePath(Locale locale, List<Class<?>> resLoadCtxtClasses, String key) {
    String resourceString = null;
    for (int i=0; i<resLoadCtxtClasses.size(); ++i) {
      resourceString = ClassPathResourceFinder.instance().findString(resLoadCtxtClasses.get(i), key, locale);
      if (resourceString != null) {
        break;
      }
    }
    return resourceString;
  }

  protected Object getScopeObj(List<Class<?>> resLoadCtxtClasses) {
    return resLoadCtxtClasses.get(0);
  }

}
