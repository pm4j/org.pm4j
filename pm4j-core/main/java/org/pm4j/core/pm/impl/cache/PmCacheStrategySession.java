package org.pm4j.core.pm.impl.cache;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmUtil;

// FIXME olaf: the session property based storage will cause memory leaks!
// try to extract a buiness key to identify the cached item!
public class PmCacheStrategySession extends PmCacheStrategyBase<PmObject> {

  private final String cacheVarKeyPfx;

  public PmCacheStrategySession(String cacheName, String cacheVarKeyPfx) {
    super(cacheName);
    this.cacheVarKeyPfx = "pm.sc." + cacheVarKeyPfx + "_";
  }

  @Override
  protected Object readRawValue(PmObject pm) {
    return pm.getPmConversation().getPmNamedObject(getCacheIdentity(pm));
  }

  @Override
  protected void writeRawValue(PmObject pm, Object value) {
    pm.getPmConversation().setPmNamedObject(getCacheIdentity(pm), value);
  }

  @Override
  protected void clearImpl(PmObject pm) {
    pm.getPmConversation().setPmNamedObject(getCacheIdentity(pm), null);
  }

  /**
   * Provides an identity key for cached items of the given PM.
   *
   * @param pm
   *          The PM to get the key for.
   * @param forCache
   *          The special cache of the PM to get the key for.
   * @return A unique key
   */
  private String getCacheIdentity(PmObject pm) {
    return cacheVarKeyPfx + PmUtil.getPmAbsoluteName(pm)
        + Integer.toHexString(System.identityHashCode(pm));
  }
}
