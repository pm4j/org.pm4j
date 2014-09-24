package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.annotation.PmCacheCfg.Clear;

/**
 * Provides functionality for pm4j internal use only!
 *
 * @author okossak
 */
public class InternalPmImplUtil {

  private InternalPmImplUtil() {
  }

  /**
   * Returns configuration of cache clear behavior, when cache clear method is called.
   * Default behavior is that cache is cleared, but optionally it can be configured,
   * that cache is never cleared.
   * 
   * @return The configuration of cache clear behavior.
   */
  public static Clear getPmCacheClear(PmObjectBase pm) {
    return pm.getPmMetaData().cacheClearBehavior;
  }

}
