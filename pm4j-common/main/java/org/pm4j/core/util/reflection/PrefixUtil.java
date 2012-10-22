package org.pm4j.core.util.reflection;

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
  private static final int NON_GETTER_MODIFIER = Modifier.NATIVE | Modifier.STATIC;
  
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

    baseName.append(PFX_SETTER);
    baseName.append(getterBaseName(getterName));

    return baseName.toString();
  }

  /**
   * @param methodName
   *          Name of the method to check.
   * @return <code>true</code> when the name begins with 'is' or 'get'
   *         followed by an uppercase letter.
   */
  public static boolean isGetterName(String methodName) {
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
  public static boolean isGetter(Method m) {
    if (m.getReturnType() != null && 
        !m.getReturnType().equals(Void.class) && 
        (m.getModifiers() & NON_GETTER_MODIFIER) == 0) {
      if (m.getParameterTypes().length == 0) {
        return PrefixUtil.isGetterName(m.getName());
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
    Method setterMethod = null;
    String expectedSetterName = PrefixUtil.setterNameForGetter(getterMethod.getName());
    
    // The getMethod(String) function is not used to be able to handle polymorphy.
    for (Method sm : getterMethod.getDeclaringClass().getMethods()) {
      if (expectedSetterName.equals(sm.getName())) {
        // check the parameter set:
        if ((sm.getParameterTypes().length == 1) &&
            (sm.getParameterTypes()[0].equals(getterMethod.getReturnType())) &&
            (sm.getModifiers() & NON_GETTER_MODIFIER) == 0) {
          setterMethod = sm;
          break;
        }
      }
    }

    return setterMethod;
  }
  
  /**
   * Private utility ctor.
   */
  private PrefixUtil() {
  // nothing to do
  }

}
