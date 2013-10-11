package org.pm4j.core.pm.impl.cache;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmUtil;

public class CacheStrategyConversation extends CacheStrategyBase<PmObject> {

  private final String cacheVarKeyPfx;

  public CacheStrategyConversation(String cacheName, String cacheVarKeyPfx) {
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
    return cacheVarKeyPfx + PmUtil.getAbsoluteName(pm)
        + Integer.toHexString(System.identityHashCode(pm));
  }
}
