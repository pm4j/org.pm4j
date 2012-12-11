package org.pm4j.core.pm.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmCacheApiHandler;

public class PmCacheApi {

  /**
   * The PM content caches.
   */
  public static enum CacheKind {
    VISIBILITY,
    ENABLEMENT,
    TITLE,
    VALUE,
    OPTIONS,
    ALL;

    public static final Set<CacheKind> ALL_SET = Collections.unmodifiableSet(
        new HashSet<CacheKind>(Arrays.asList(CacheKind.VISIBILITY,
            CacheKind.ENABLEMENT,
            CacheKind.TITLE,
            CacheKind.VALUE,
            CacheKind.OPTIONS)));
  }

  private static final PmCacheApiHandler apiHandler = new PmCacheApiHandler();

  /**
   * Clears cached content (if there was something cached).<br>
   * Causes a reload of the content with the next request.
   *
   * @param cacheKinds
   *          The set of caches to be cleared. If no cacheKind is specified, all
   *          cache kinds will be cleared.
   * @deprecated Please use {@link #clearPmCache(PmObject, CacheKind...)}
   */
  @Deprecated
  public static void clearCachedPmValues(PmObject pm, CacheKind... cacheKinds) {
    apiHandler.clearCachedPmValues(pm, cacheKinds);
  }

  /**
   * Clears cached content (if there was something cached).<br>
   * Causes a reload of the content with the next request.
   *
   * @param cacheKinds
   *          The set of caches to be cleared. If no cacheKind is specified, all
   *          cache kinds will be cleared.
   */
  public static void clearPmCache(PmObject pm, CacheKind... cacheKinds) {
    apiHandler.clearCachedPmValues(pm, cacheKinds);
  }

  /**
   * An alternate signature for
   * {@link #clearCachedPmValues(org.pm4j.core.pm.PmObject.CacheKind...)}
   * that is a little more efficient, since it can quickly check the set if a
   * specific cache kind is to clear.
   * <p>
   * Subclasses that need to extend the clear implementation should override
   * this method.
   *
   * @param cacheSet
   *          Specification of the cache kinds to clear.
   */
  public static void clearPmCache(PmObject pm, Set<CacheKind> cacheSet) {
    apiHandler.clearCachedPmValues(pm, cacheSet);
  }

  /**
   * Calls {@link #clearCachedPmValues()} on all PMs in the pmParent hierarchy.
   *
   * @param includeSession
   *          <code>true</code> causes a clear within the complete context path,
   *          including the {@link PmConversation} instance(s) within the path.<br/>
   *          <code>false</code> causes a clear on all context items exclusive
   *          the containing session (and its contexts).
   * @param cacheKinds
   *          The set of caches to be cleared. If no cacheKind is specified, all
   *          cache kinds will be cleared.
   */
  public static void clearCachedPmValuesInCtxtPath(PmObject pm, boolean includeSession, PmCacheApi.CacheKind... cacheKinds) {
    apiHandler.clearCachedPmValuesInCtxtPath(pm, includeSession, cacheKinds);
  }


}
