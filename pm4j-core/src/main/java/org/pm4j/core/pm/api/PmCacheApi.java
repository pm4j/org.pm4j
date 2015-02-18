package org.pm4j.core.pm.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmCacheApiHandler;

/**
 * API for PM aspect cache handling.
 *
 * @author Olaf Boede
 */
public class PmCacheApi {

  /**
   * The PM content caches.
   */
  public static enum CacheKind {
    VISIBILITY,
    ENABLEMENT,
    TITLE,
    TOOLTIP,
    VALUE,
    OPTIONS,
    NODES,
    ALL;

    public static final Set<CacheKind> ALL_SET = Collections.unmodifiableSet(
        new HashSet<CacheKind>(Arrays.asList(
            CacheKind.VISIBILITY,
            CacheKind.ENABLEMENT,
            CacheKind.TITLE,
            CacheKind.TOOLTIP,
            CacheKind.VALUE,
            CacheKind.OPTIONS,
            CacheKind.NODES)));
  }

  private static final PmCacheApiHandler apiHandler = new PmCacheApiHandler();

  /**
   * Clears cached content (if there was something cached).<br>
   * Causes a reload of the content with the next request.
   * <p>
   * The caches of the child PM instances get cleared recursively as well.
   *
   * @param cacheKinds
   *          The set of caches to be cleared. If no cacheKind is specified, all
   *          cache kinds will be cleared.
   */
  public static void clearPmCache(PmObject pm, CacheKind... cacheKinds) {
    apiHandler.clearPmCache(pm, cacheKinds);
  }

  /**
   * An alternate signature for
   * {@link #clearCachedPmValues(org.pm4j.core.pm.PmObject.CacheKind...)}
   * that is a little more efficient, since it can quickly check the set if a
   * specific cache kind is to clear.
   * <p>
   * XXX oboede: is that really needed as part of the public API?
   *
   * @param cacheSet
   *          Specification of the cache kinds to clear.
   */
  public static void clearPmCache(PmObject pm, Set<CacheKind> cacheSet) {
    apiHandler.clearPmCache(pm, cacheSet);
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
  public static void clearPmCacheInCtxtPath(PmObject pm, boolean includeSession, PmCacheApi.CacheKind... cacheKinds) {
    apiHandler.clearPmCacheInCtxtPath(pm, includeSession, cacheKinds);
  }


}
