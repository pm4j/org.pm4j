package org.pm4j.core.pm.impl;

import org.pm4j.core.exception.PmRuntimeException;

public class PmLocalizeApiHandler {

  /**
   * Provides a localization based on a key and option resource string arguments.
   * <p>
   * The result of that operation strongly depends on the kind of title provider used.
   *
   * @param key A resource key.
   * @param resStringArgs Optional resource string arguments.
   * @return The localized string or <code>null</code> when no localization is available.
   */
  public String findLocalization(PmObjectBase pm, String key, Object... resStringArgs) {
    return pm.getPmTitleDef().findLocalization(pm, key, resStringArgs);
  }

  /**
   * Provides a localization based on a key and option resource string arguments.
   * <p>
   * The result of that operation strongly depends on the kind of title provider used.
   *
   * @param key A resource key.
   * @param resStringArgs Optional resource string arguments.
   * @return The localized string.
   * @throws PmRuntimeException when no localization for the given key was found.
   */
  public String localize(PmObjectBase pm, String key, Object... resStringArgs) {
    return pm.getPmTitleDef().getLocalization(pm, key, resStringArgs);
  }

}
