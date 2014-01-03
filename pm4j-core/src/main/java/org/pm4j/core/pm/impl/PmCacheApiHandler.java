package org.pm4j.core.pm.impl;

import java.util.List;
import java.util.Set;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmCacheApi;

public class PmCacheApiHandler {

  /**
   * Clears cached content (if there was something cached).<br>
   * Causes a reload of the content with the next request.
   *
   * @param cacheKinds
   *          The set of caches to be cleared. If no cacheKind is specified, all
   *          cache kinds will be cleared.
   */
  public void clearPmCache(PmObject pm, PmCacheApi.CacheKind... cacheKinds) {
    clearPmCache(pm, PmUtil.cacheKindArrayToSet(cacheKinds));
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
  public void clearPmCache(PmObject pm, Set<PmCacheApi.CacheKind> cacheSet) {
    ((PmObjectBase)pm).clearCachedPmValues(cacheSet);
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
  public void clearPmCacheInCtxtPath(PmObject pm, boolean includeSession, PmCacheApi.CacheKind... cacheKinds) {
    List<PmObject> list = PmUtil.getPmHierarchy(pm, includeSession);

    Set<PmCacheApi.CacheKind> cacheKindSet = PmUtil.cacheKindArrayToSet(cacheKinds);
    // ensures a top-down processing order.
    for (int i=list.size()-1; i>=0; --i) {
      clearPmCache(list.get(i), cacheKindSet);
    }
  }

}
