package org.pm4j.common.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Olaf Boede
 */
public class BeanAttrAccessorImpl implements BeanAttrAccessor {

  private static final Log    LOGGER            = LogFactory.getLog(BeanAttrAccessorImpl.class);

  public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[] {};

  private final String        fieldName;

  private final Class<?>      beanClass;

  private Method              getterMethod;

  /**
   * Cache of overridden getter methods for subclasses.
   */
  private Map<Class<?>, Method> classToGetterMap = new ConcurrentHashMap<Class<?>, Method>();

  private Method              setterMethod;

  /**
   * Cache of overridden setter methods for subclasses.
   */
  private Map<Class<?>, Method> classToSetterMap = new ConcurrentHashMap<Class<?>, Method>();

  /**
   * When there is no getter, the field might be used. Useful for prototype
   * development phases.
   */
  private Field               field;

  private final Class<?>      fieldClass;

  /**
   * Constructs an accessor based on the bean naming conventions.
   *
   * @param beanClass
   *          Class of beans to accessFlag.
   * @param fieldName
   *          Identifier of the field.
   * @param allowFieldAccess
   *          Defines if it is allowed to use direct field access when the
   *          getter/setter are not available to the field.
   */
  public BeanAttrAccessorImpl(Class<?> beanClass, String fieldName, boolean allowFieldAccess) {
    assert beanClass != null;
    assert fieldName != null;

    this.beanClass = beanClass;
    this.fieldName = fieldName;

    this.getterMethod = findPublicGetter(fieldName);

    if (this.getterMethod != null) {
      this.fieldClass = getterMethod.getReturnType();

      this.setterMethod = findPublicSetter(fieldName, this.fieldClass);

      if (setterMethod == null &&
          LOGGER.isTraceEnabled()) {
        LOGGER.trace("No setter found for attribute '" + fieldName + "' of bean class '" + beanClass + "'. Will be handeled as read-only attribute.");
      }
    }
    else {
      if (allowFieldAccess) {
        try {
          this.field = ClassUtil.findField(beanClass, fieldName);

          if (this.field == null) {
            throw new ReflectionException(
                makeErrMsg("Unable to find a public field or getter for '" + this.fieldName + "'."));
          }

          if (! this.field.isAccessible()) {
            // XXX olaf: This security hack will fail when there is a strict security
            // management. But it is required to provide direct field access
            // for non-security managed prototype applications.
            this.field.setAccessible(true);
          }
          this.fieldClass = field.getType();
        }
        catch (ReflectionException e) {
          throw e;
        }
        catch (Exception eFieldAccess) {
          throw new ReflectionException(
              makeErrMsg("Unable to find a public field or getter for '" + this.fieldName + "'."), eFieldAccess);
        }
      }
      else {
        throw new ReflectionException(makeErrMsg("Getter not accessible."));
      }
    }

  }

  /**
   * Constructs an accessor based on the bean naming conventions. Existence of a
   * matching getter and setter is required.
   *
   * @param beanClass
   *          Class of beans to accessFlag.
   * @param fieldName
   *          Identifier of the field.
   */
  public BeanAttrAccessorImpl(Class<?> beanClass, String fieldName) {
    this(beanClass, fieldName, true);
  }

  /**
   * Creates an accessor for the given field.
   *
   * @param beanClass The class to access.
   * @param field     The field to access.
   */
  public BeanAttrAccessorImpl(Class<?> beanClass, Field field) {
    assert beanClass != null;
    assert field != null;

    this.beanClass = beanClass;
    this.fieldName = field.getName();
    this.fieldClass = field.getType();
    this.field = field;

    if (! this.field.isAccessible()) {
      // XXX olaf: This security hack will fail when there is a strict security
      // management. But it is required to provide direct field access
      // for non-security managed prototype applications.
      this.field.setAccessible(true);
    }
  }


  @Override
  public String getName() {
    return fieldName;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getBeanAttrValue(Object bean) {
    if (getterMethod != null) {
      try {
        Method m = getGetterMethod(bean);
        if (! m.isAccessible()) {
          m.setAccessible(true);
        }
        return (T) m.invoke(bean, (Object[]) null);
      }
      catch (Exception e) {
        throw new ReflectionException(makeErrMsg("Unable to use method '" + getterMethod.getName()
            + "'."), e);
      }
    }
    else {
      if (field == null) {
        throw new IllegalStateException(makeErrMsg("Both, getter- and fieldaccess metadata are not initialized."));
      }

      try {
        return (T) field.get(bean);
      }
      catch (Exception e) {
        throw new ReflectionException(
            makeErrMsg("Unable to access the field member directly. Please try to define and use a getter for that field."),
            e);
      }
    }
  }

  @Override
  public void setBeanAttrValue(Object bean, Object value) {
    if (setterMethod != null) {
      try {
        getSetterMethod(bean).invoke(bean, new Object[] { value });
      }
      catch (Exception e) {
        throw new ReflectionException(makeErrMsg("Unable to use method '" + setterMethod.getName()
            + "' for parameter of type '" +
            (value != null ? value.getClass().getName() : "null") +
            "'."), e);
      }
    }
    else {
      try {
        field.set(bean, value);
      }
      catch (Exception e) {
        throw new ReflectionException(
            makeErrMsg("Unable to access the field member directly. Please make the field public or define a public setter."),
            e);
      }
    }
  }

  @Override
  public boolean canSet() {
    return (setterMethod != null) || (field != null);
  }


  @Override
  public Class<?> getFieldClass() {
    return fieldClass;
  }

  @Override
  public String toString() {
    return getterMethod != null
      ? getterMethod.toString()
      : (field != null
          ? field.toString()
          : fieldName);
  }

  // -- internal helper --

  /**
   * Provides the cached getter method.
   *
   * @param bean The bean to get it for.
   * @return The getter.
   */
  private final Method getGetterMethod(Object bean) {
    Class<?> foundBeanClass = bean.getClass();
    if (foundBeanClass.equals(getterMethod.getDeclaringClass())) {
      return getterMethod;
    }
    else {
      Method getMethod = classToGetterMap.get(foundBeanClass);
      if (getMethod == null) {
        getMethod = findPublicMethod(foundBeanClass, getterMethod.getName());
        if (getMethod == null) {
          throw new ReflectionException(makeErrMsg(
              "Unable to find method '" + getterMethod.getName() + "' in class '" + foundBeanClass + "'."));
        }
        classToGetterMap.put(foundBeanClass, getMethod);
      }
      return getMethod;
    }
  }

  /**
   * Provides the cached setter method.
   *
   * @param bean The bean to get it for.
   * @return The getter.
   */
  private final Method getSetterMethod(Object bean) {
    Class<?> foundBeanClass = bean.getClass();
    if (foundBeanClass.equals(setterMethod.getDeclaringClass())) {
      return setterMethod;
    }
    else {
      Method m = classToSetterMap.get(foundBeanClass);
      if (m == null) {
        m = findPublicMethod(foundBeanClass, setterMethod.getName(), fieldClass);
        if (m == null) {
          throw new ReflectionException(makeErrMsg(
              "Unable to find method '" + setterMethod.getName() + "' in class '" + foundBeanClass + "'."));
        }
        classToSetterMap.put(foundBeanClass, m);
      }
      return m;
    }
  }

  /**
   * Generates an error message string based on the field information.
   *
   * @param msg
   *          The event specific message.
   * @return An enhanced message like "Field accessor problem in 'names': No
   *         getter defined"
   */
  private String makeErrMsg(String msg) {
    return "Unable to access field '" + fieldName + "' of bean class '" + beanClass + "' : "
        + msg;
  }

  /**
   * Finds a getter according to the bean naming conventions.
   * <p>
   * Example: For a given field name 'foo' it searches for a method 'getFoo'. If
   * that is not found it tries to find a method with the name 'isFoo'.
   * <p>
   * FIXME ob: does not consider return fieldClass restrictions for now.
   *
   * @param fieldName
   *          The field name to find a getter for.
   * @return The found method or <code>null</code>.
   */
  private Method findPublicGetter(String fieldName) {
    Method getter = null;

    if (!StringUtils.isEmpty(fieldName)) {
      String methodName = PrefixUtil.addMethodNamePrefix(PrefixUtil.PFX_GETTER, fieldName);
      getter = findPublicMethod(beanClass, methodName);

      if (getter == null) {
        methodName = PrefixUtil.addMethodNamePrefix(PrefixUtil.PFX_BOOL_GETTER, fieldName);
        getter = findPublicMethod(beanClass, methodName);
      }
    }

    return getter;
  }

  /**
   * Finds a setter according to the bean naming conventions.
   * <p>
   * Example: For a given field name 'foo' it searches for a method 'setFoo'
   * with a single argument.
   *
   * @param fieldName
   *          The field name to find a setter for.
   * @param fieldClass
   *          The field fieldClass. The setter argument fieldClass should match.
   * @return The found method or <code>null</code>.
   */
  private Method findPublicSetter(String fieldName, Class<?> fieldClass) {
    Method setter = null;

    if (!StringUtils.isEmpty(fieldName)) {
      String methodName = PrefixUtil.addMethodNamePrefix(PrefixUtil.PFX_SETTER, fieldName);
      setter = findPublicMethod(beanClass, methodName, fieldClass);
    }

    return setter;
  }

  /** Encapsulates the best-match algorithm used here. */
  private Method findPublicMethod(Class<?> beanClass, String methodName, Class<?>... argClasses) {
    try {
      return beanClass.getMethod(methodName, argClasses);
    }
    catch (NoSuchMethodException e) {
      return null;
    }
    catch (SecurityException e) {
      throw new ReflectionException(makeErrMsg("Security manager problem: " + e.getMessage()), e);
    }
  }

}
