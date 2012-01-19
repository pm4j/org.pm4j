package org.pm4j.core.pm.impl.cache;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.core.pm.impl.PmUtil;

public class PmCacheStrategyRequest extends PmCacheStrategyBase<PmObjectBase> {

  private final String cacheVarKeyPfx;

  public PmCacheStrategyRequest(String cacheName, String cacheVarKeyPfx) {
    super(cacheName);
    this.cacheVarKeyPfx = "pm.rc." + cacheVarKeyPfx + "_";
  }

  @Override
  protected Object readRawValue(PmObjectBase pm) {
    return pmConversationImplOf(pm).getViewConnector()
              .readRequestAttribute(getCacheIdentity(pm));
  }

  @Override
  protected void writeRawValue(PmObjectBase pm, Object value) {
    pmConversationImplOf(pm).getViewConnector().setRequestAttribute(
        getCacheIdentity(pm), value);
  }

  @Override
  protected void clearImpl(PmObjectBase pm) {
    // TODO: Aus removeAttribute weiterleiten.
    pmConversationImplOf(pm).getViewConnector().setRequestAttribute(
        getCacheIdentity(pm), null);
  }

  private PmConversationImpl pmConversationImplOf(PmObject pm) {
    return (PmConversationImpl)pm.getPmConversation();
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
