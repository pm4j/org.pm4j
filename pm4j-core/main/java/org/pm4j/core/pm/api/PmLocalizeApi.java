package org.pm4j.core.pm.api;

import org.pm4j.common.util.collection.ArrayUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmLocalizeApiHandler;
import org.pm4j.core.pm.impl.PmObjectBase;

public class PmLocalizeApi {

  private static PmLocalizeApiHandler apiHandler = new PmLocalizeApiHandler();

  /**
   * Provides a localization based on a key and option resource string arguments.
   * <p>
   * The result of that operation strongly depends on the kind of title provider used.
   *
   * @param key A resource key.
   * @param resStringArgs Optional resource string arguments.
   * @return The localized string or <code>null</code> when no localization is available.
   */
  public static String findLocalization(PmObject pm, String key, Object... resStringArgs) {
    return apiHandler.findLocalization((PmObjectBase)pm, key, resStringArgs);
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
  public static String localize(PmObject pm, String key, Object... resStringArgs) {
    return apiHandler.findLocalization((PmObjectBase)pm, key, resStringArgs);
  }

  /**
   * Provides a localization based on a key and option resource string
   * arguments.
   * <p>
   * Examples:
   * <ul>
   * <li>localizeOneOrMany("myKey", 1) - provides the string for the resource
   * key 'myKey_one'</li>
   * <li>localizeOneOrMany("myKey", 2) - provides the string for the resource
   * key 'myKey_many'</li>
   * <li>localizeOneOrMany("myKey", 0) - provides the string for the resource
   * key 'myKey_none'</li>
   * </ul>
   *
   * @param keybase
   *          A resource key base that will be concatenated with 'one' or
   *          'many'.
   * @param number
   *          depending on the number the postfix '_one', '_many' or '_none'
   *          will be added to keybase
   * @param resArgs
   *          The arguments for the resource string.
   * @return The localized string.
   */
  public static String localizeOneOrMany(PmObject pm, String keybase, int number, Object... resArgs) {
    String key = keybase + (number > 1 ? "_many" : (number == 1 ? "_one" : "_none"));

    Object[] resArgsWithNumber = ArrayUtil.copyOf(resArgs, resArgs.length+1, 1);
    resArgsWithNumber[0] = number;

    return localize(pm, key, resArgsWithNumber);
  }

  /**
   * Calls {@link #localizeOneOrMany(String, int, Object...)} with the resource
   * key provided by {@link #getPmResKey()}.
   */
  public static String localizeOneOrMany(PmObject pm, int number, Object... resArgs) {
    return localizeOneOrMany(pm, ((PmObjectBase)pm).getPmResKey(), number, resArgs);
  }

  /**
   * Provides a localization based on a key and option resource string arguments.
   * <p>
   * Used localization key: {@link #getPmResKeyBase()}+key.<br>
   *
   * @param key A resource key.
   * @param resStringArgs Optional resource string arguments.
   * @return The localized string.
   * @throws PmRuntimeException if no localization for the given key was found.
   */
  public static String findLocalizationWithPfx(PmObject pm, String key, Object... resStringArgs) {
    return findLocalization(pm, ((PmObjectBase)pm).getPmResKeyBase() + key, resStringArgs);
  }

  /**
   * Provides a localization based on a key and option resource string arguments.
   *
   * @param key A resource key with
   * @param resStringArgs Optional resource string arguments.
   * @return The localized string or <code>null</code> when no localization is available.
   */
  public static String localizeWithPfx(PmObject pm, String key, Object... resStringArgs) {
    return localize(pm, ((PmObjectBase)pm).getPmResKeyBase() + key, resStringArgs);
  }


}
