package org.pm4j.core.pm.api;

import java.text.MessageFormat;

import org.pm4j.common.util.collection.ArrayUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmLocalizeApiHandler;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.core.pm.impl.ResKeyUtil;
import org.pm4j.core.pm.impl.title.ClassPathResourceStringProvider;
import org.pm4j.core.pm.impl.title.ResourceStringProvider;

/**
 * The localization api provides support for resource string localization.
 * <p>
 * For information about resource string parameter interpretation see
 * {@link MessageFormat}.
 * <p>
 * The resources are located using a {@link ResourceStringProvider} which may be
 * configured by a call to
 * {@link #setResourceStringProvider(ResourceStringProvider)}.
 * <p>
 * The default {@link ResourceStringProvider} is
 * {@link ClassPathResourceStringProvider} which parses the package tree to find
 * a resource file with the matching resource key.
 *
 * @author olaf boede
 */
public final class PmLocalizeApi {

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
    return apiHandler.localize((PmObjectBase)pm, key, resStringArgs);
  }

  /**
   * Finds a localization for the given enum value.
   * <p>
   * The used resource key is a concatenation of the Enum class key generated by
   * {@link ResKeyUtil#shortResKeyForClass(Class)}, an appended '.' plus the enum value
   * <code>name()</code>.
   *
   * @param pmCtxt
   * @param enumValue
   * @return The corresponding localized string.
   */
  public static String localizeEnumValue(PmObject pmCtxt, Enum<?> enumValue) {
    String key = ResKeyUtil.shortResKeyForClass(enumValue.getDeclaringClass()) + "." + enumValue.name();
    return localize(pmCtxt, key);
  }

  public static String localizeBooleanValue(PmAttr<?> pmAttr, Boolean value) {
    return PmLocalizeApi.localize(pmAttr, ResKeyUtil.classNameAndValue(value));
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
   * If one of number related the postfixed keys (e.g. myKey_none) is not defined,
   * the base key will be used as the fallback defintion (e.g. myKey instead of myKey_none).
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

    String text = findLocalization(pm, key, resArgsWithNumber);
    return (text != null)
        ? text
        : localize(pm, keybase, resArgsWithNumber);

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
   * @deprecated Please use PmLocalizeApi.findLocalization(this, getResKey() + "myPostfix")
   */
  @Deprecated
  public static String findLocalizationWithPfx(PmObject pm, String key, Object... resStringArgs) {
    return findLocalization(pm, ((PmObjectBase)pm).getPmResKeyBase() + key, resStringArgs);
  }


  /**
   * @param resourceStringProvider The algorithm that provides resource strings for given resource keys.
   */
  public static void setResourceStringProvider(ResourceStringProvider resourceStringProvider) {
    apiHandler.setResourceStringProvider(resourceStringProvider);
  }

  /**
   * Provides the localized output format string for the given attribute.
   *
   * @param pmAttr the attribute to get the output format for.
   * @return the found output format or <code>null</code>.
   */
  public static String getOutputFormatString(PmAttr<?> pmAttr) {
    return apiHandler.getOutputFormatString((PmAttrBase<?, ?>) pmAttr);
  }

}
