package org.pm4j.common.util.reflection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * Utilties for method name creation.
 *
 * @author Olaf Boede
 */
public final class PrefixUtil {

  public static final String PFX_GETTER      = "get";

  public static final String PFX_BOOL_GETTER = "is";

  public static final String PFX_SETTER      = "set";

  /** Modifier flags that should not be defined for a usual getter or setter. */
  private static final int NON_GETTER_SETTER_MODIFIER = Modifier.NATIVE | Modifier.STATIC;

  /**
   * @param getterName
   *          The name of a getter method. Like 'getIt()', 'isIt()' or 'it()'.
   * @return The matching setter method name. E.g. 'setIt()'.
   *         <p>
   *         <code>null</code> if the given method name does not match the
   *         getter name conventions.
   */
  public static String setterNameForGetter(String getterName) {
    Validate.notEmpty(getterName);

    StringBuffer baseName = new StringBuffer(getterName.length() + PFX_SETTER.length());
    String propertyName = getterBaseName(getterName);
    if ( getterName.length() != propertyName.length() ) {
      baseName.append(PFX_SETTER);
    } else {
      propertyName = StringUtils.uncapitalize(propertyName);
    }
    baseName.append(propertyName);

    return baseName.toString();
  }

  /**
   * @param setterName
   *          The name of a setter method. Like 'setIt(...)', 'setIt(..)' or 'it(...)'.
   * @param type the of the property
   * @return The matching setter method name. E.g. 'getIt()', 'isIt()' or 'it()'.
   *         <p>
   *         <code>null</code> if the given method name does not match the
   *         setter name conventions.
   */
  public static String getterNameForSetter(String setterName, Class<?> type) {
    Validate.notEmpty(setterName);

    StringBuffer baseName = new StringBuffer(setterName.length() + PFX_GETTER.length());
    String propertyName = setterBaseName(setterName);
    if ( setterName.length() != propertyName.length() ) {
      baseName.append( ( type == boolean.class || type == Boolean.class) ? PFX_BOOL_GETTER : PFX_GETTER);
    } else {
      propertyName = StringUtils.uncapitalize(propertyName);
    }
    baseName.append(propertyName);

    return baseName.toString();
  }

  /**
   * @param methodName
   *          Name of the method to check.
   * @return <code>true</code> when the name begins with 'is' or 'get'
   *         followed by an uppercase letter.
   */
  public static boolean hasGetterPrefix(String methodName) {
    return isPrefixedMethodName(PFX_GETTER, methodName) || isPrefixedMethodName(PFX_BOOL_GETTER, methodName);
  }

  /**
   * @param prefix
   *          The expected method name prefix.
   * @param methodName
   *          Name of the method to check.
   * @return <code>true</code> when the name begins with 'is' or 'get'
   *         followed by an uppercase letter.
   */
  public static boolean isPrefixedMethodName(String prefix, String methodName) {
    Validate.notEmpty(prefix, "Prefix parameter should not be empty.");

    if ((methodName != null) && methodName.startsWith(prefix)
        && methodName.length() > prefix.length()) {
      char c = methodName.charAt(prefix.length());

      return Character.isUpperCase(c);
    }
    else {
      return false;
    }
  }

  /**
   * Returns the CamelCase string that followed the (optional) getter prefix ('is' or 'get').
   * @param getterName A name like 'getXy', 'isXy' or 'xy'.
   * @return A capitalized string like 'Xy'.
   */
  public static String getterBaseName(String getterName) {
    String result;

    if (isPrefixedMethodName(PFX_GETTER, getterName)) {
      result = getterName.substring(PFX_GETTER.length());
    }
    else if (isPrefixedMethodName(PFX_BOOL_GETTER, getterName)) {
      result = getterName.substring(PFX_BOOL_GETTER.length());
    }
    else {
      result = StringUtils.capitalize(getterName);
    }

    return result;
  }

  /**
   * Returns the CamelCase string that followed the (optional) getter prefix ('set').
   * @param setterName A name like 'setXy' or 'xy'.
   * @return A capitalized string like 'Xy'.
   */
  public static String setterBaseName(String setterName) {
    String result;

    if (isPrefixedMethodName(PFX_SETTER, setterName)) {
      result = setterName.substring(PFX_SETTER.length());
    }
    else {
      result = StringUtils.capitalize(setterName);
    }

    return result;
  }

  /**
   * @param getterName A name like 'getXy' or 'isXy'.
   * @return the corresponding property name. E.g. 'xy'
   */
  public static String propNameForGetter(String getterName) {
    return StringUtils.uncapitalize(getterBaseName(getterName));
  }

  /**
   * Adds a prefix to a name according to the camelCase naming rules.
   *
   * @param prefix
   *          A prefix string like 'get'
   * @param fieldName
   *          A name for that we need a prefixed method name.
   * @return A camelCase formatted method name. E.g. <code>getFoo</code>.
   */
  public static String addMethodNamePrefix(String prefix, String fieldName) {
    Validate.notEmpty(prefix);
    Validate.notEmpty(fieldName);

    String pfx = (prefix == null) ? "" : prefix;
    StringBuffer buf = new StringBuffer(pfx.length() + fieldName.length());

    buf.append(pfx);

    char firstNameChar = (pfx.length() > 0) ? Character.toUpperCase(fieldName.charAt(0))
        : fieldName.charAt(0);

    buf.append(firstNameChar);

    if (fieldName.length() > 1) {
      buf.append(fieldName.substring(1));
    }

    return buf.toString();
  }

  /**
   * Checks if the given method has the typical getter-signature.
   *
   * @param m The method to check.
   * @return <code>true</code> when the method looks like a getter.
   */
  public static boolean hasGetterPrefix(Method m) {
    if (m.getReturnType() != null &&
        !m.getReturnType().equals(Void.class) &&
        (m.getModifiers() & NON_GETTER_SETTER_MODIFIER) == 0) {
      if (m.getParameterTypes().length == 0) {
        return PrefixUtil.hasGetterPrefix(m.getName());
      }
    }
    // no getter
    return false;
  }

  /**
   * Searches a setter for a given getter method.
   *
   * @param getterMethod The known getter method.
   * @return The found setter or <code>null</code>.
   */
  public static Method findSetterForGetter(Method getterMethod) {
    String setterName = setterNameForGetter(getterMethod.getName());
    return PrefixUtil.findSetterForName(getterMethod.getDeclaringClass(), setterName, getterMethod.getReturnType());
  }

   public static Method findSetterForName(Class<?> clazz, String setterName, Class<?> type) {
     Method setterMethod = null;

    // The getMethod(String) function is not used to be able to handle polymorphy.
    for (Method sm : clazz.getMethods()) {
      if (setterName.equals(sm.getName())) {
        // check the parameter set:
        if ((sm.getParameterTypes().length == 1) &&
            (sm.getParameterTypes()[0].equals(type)) &&
            (sm.getModifiers() & NON_GETTER_SETTER_MODIFIER) == 0) {
          setterMethod = sm;
          break;
        }
      }
    }

    return setterMethod;
  }

  /**
   * Searches a getter for a given setter method.
   *
   * @param getterMethod The known setter method.
   * @return The found getter or <code>null</code>.
   */
  public static Method findGetterForSetter(Method setterMethod) {
    Class<?> type = setterMethod.getParameterTypes()[0];
    String getterName = getterNameForSetter(setterMethod.getName(), type);
    return PrefixUtil.findGetterForName(setterMethod.getDeclaringClass(), getterName, type);
  }

  public static Method findGetterForName(Class<?> clazz, String getterName, Class<?> type) {
    Method getterMethod = null;

    // The getMethod(String) function is not used to be able to handle polymorphy.
    for (Method gm : clazz.getMethods()) {
      if (getterName.equals(gm.getName())) {
        // check the signature:
        if ((gm.getParameterTypes().length == 0) &&
            (gm.getReturnType().equals(type)) &&
            (gm.getModifiers() & NON_GETTER_SETTER_MODIFIER) == 0) {
          getterMethod = gm;
          break;
        }
      }
    }

    return getterMethod;
  }

  /**
   * Private utility ctor.
   */
  private PrefixUtil() {
  // nothing to do
  }

}
