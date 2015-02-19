package org.pm4j.jsf;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.impl.PmObjectBase;

/**
 * Java functions that are used in facelets taglib function declarations.
 *
 * @author olaf boede
 */
public final class TagFn {

  private static final Logger log = LoggerFactory.getLogger(TagFn.class);

  public static Object conditionalValue(boolean condition, Object trueReturn,
      Object falseReturn) {
    return condition ? trueReturn : falseReturn;
  }

  /**
   * Provides the localized string for the given resource key.
   * <p>
   * Uses the locale that is provided by the given PM context instance.
   * <p>
   * Searches for localized resources in the package/archive context of the
   * given PM context instance.
   * <p>
   * Documentation for the place holder syntax may be found in
   * {@link MessageFormat}.
   *
   * @param ctxt
   *          An instance that provides the requested local and the localization
   *          package context.
   * @param resKey
   *          The key to get a resource for.
   * @param resStringArgs
   *          Arguments for placeholders within the localized string.
   * @return The localized resource.<br/> If no localization was found, the
   *         value of the key will be returned.
   */
  public static String localize(PmObjectBase ctxt, String resKey, Object... resStringArgs) {
    if (ctxt == null) {
      return "Localization context parameter is null in call for getting the resource string for key: "
          + resKey
          + "\nPlease check if the 'pm' attribute is set for your ui tag.";
    }

    return PmLocalizeApi.localize(ctxt, resKey, resStringArgs);
  }

  /**
   * Translate function signature with zero arguments.
   * Special signature for a taglib function.
   *
   * @see #localize(PmObject, String, Object[])
   */
  public static String localizeWithFixArgSet(PmObjectBase ctxt, String resKey) {
    return localize(ctxt, resKey);
  }

  /**
   * Translate function signature with a single argument.
   * Special signature for a taglib function.
   *
   * @see #localize(PmObject, String, Object[])
   */
  public static String localizeWithFixArgSet(PmObjectBase ctxt, String resKey, Object arg1) {
    return localize(ctxt, resKey, arg1);
  }

  /**
   * Translate function signature with two arguments.
   * Special signature for a taglib function.
   *
   * @see #localize(PmObject, String, Object[])
   */
  public static String localizeWithFixArgSet(PmObjectBase ctxt, String resKey, Object arg1, Object arg2) {
    return localize(ctxt, resKey, arg1, arg2);
  }

  /**
   * Translate function signature with three arguments.
   * Special signature for a taglib function.
   *
   * @see #localize(PmObject, String, Object[])
   */
  public static String localizeWithFixArgSet(PmObjectBase ctxt, String resKey, Object arg1, Object arg2, Object arg3) {
      return localize(ctxt, resKey, arg1, arg2, arg3);
  }


  /**
   * Filters a list of presentation models based an include/exclude specification.
   * <p>
   * The filter strings are matched against the {@link PmObject#getPmName()}
   * values of the given presentation models.
   * <p>
   * TODO olaf: add a sort feature based on the order of the include specification.
   *
   * @param baseList
   * @param includeString Names of presentation models to be included in the result.
   * @param excludeString Names of presentation models to be excluded from the result.<br>
   *                      Will be ignored when an include is specified.
   * @return The filtered presentation model result list.
   */
  public static List<PmObject> filterPmList(
      List<PmObject> baseList, String includeString,
      String excludeString) {
    if (baseList == null || baseList.size() == 0) {
      return Collections.emptyList();
    }

    List<PmObject> resultList;
    if (StringUtils.isNotBlank(includeString)) {
      Set<String> nameSet = commaSepStringToStringSet(includeString);
      resultList = new ArrayList<PmObject>(nameSet.size());
      for (PmObject pm : baseList) {
        if (nameSet.contains(pm.getPmName())) {
          resultList.add(pm);
        }
      }
      if (resultList.size() != nameSet.size()) {
        log.warn("Only " + resultList.size() + " of " + nameSet.size()
            + " include attributes where found. Attribute list:\n"
            + includeString);
      }
    } else if (StringUtils.isNotBlank(excludeString)) {
      Set<String> excludeSet = commaSepStringToStringSet(excludeString);
      resultList = new ArrayList<PmObject>();
      for (PmObject pm : baseList) {
        if (!excludeSet.contains(pm.getPmName())) {
          resultList.add(pm);
        }
      }
      int notFoundCount = resultList.size() + excludeSet.size()
          - baseList.size();
      if (notFoundCount > 0) {
        log.warn(notFoundCount + " attributes of exclude condition not found."
            + " Attribute list:\n" + excludeString);
      }
    } else {
      resultList = baseList;
    }
    return resultList;
  }

  /**
   * Internal helper.
   *
   * @param commaSepString
   * @return
   */
  private static Set<String> commaSepStringToStringSet(String commaSepString) {
    if (StringUtils.isBlank(commaSepString)) {
      return Collections.emptySet();
    } else {
      String[] strings = commaSepString.split(",");
      Set<String> result = new HashSet<String>(strings.length);
      for (int i = 0; i < strings.length; ++i) {
        String s = strings[i].trim();
        if (s.length() > 0) {
          result.add(s);
        }
      }
      return result;
    }
  }
}
