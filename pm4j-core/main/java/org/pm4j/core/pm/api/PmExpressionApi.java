package org.pm4j.core.pm.api;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmExpressionApiHandler;

public class PmExpressionApi {

  private static final PmExpressionApiHandler apiHandler = new PmExpressionApiHandler();

  /**
   * Finds a named property from the following scopes (if available):
   * <ol>
   * <li>navigation scope</li>
   * <li>conversation scope</li>
   * <li>@link {@link PmConversation} property</li>
   * <li>http-request and session properties</li>
   * <li>application configuration property (e.g. Spring)</li>
   * </ol>
   *
   * @param expression
   *          Name of the property to find.
   * @return The found property value or <code>null</code> when not found.
   */
  public static Object findByExpression(PmObject pm, String expression) {
    return apiHandler.findByExpression(pm, expression);
  }

  /**
   * Imperative version of {@link #findPmProperty(String)}. Throws an exception
   * if there is no value for the given property.
   *
   * @param name
   *          Name of the property to find.
   * @return The found property value.
   * @throws PmRuntimeException
   *           if there is no value for the given property name.
   */
  public static Object getByExpression(PmObject pm, String name) throws PmRuntimeException {
    Object result = apiHandler.findByExpression(pm, name);
    if (result == null) {
      throw new PmRuntimeException(pm, "No property value found for path '" + name + "'.");
    }
    return result;
  }

  /**
   * Supports type safe access for PM properties.
   * <p>
   * Generates a clear {@link PmRuntimeException} message with debug hints in
   * case of a property type mismatch.
   *
   * @param <T>
   *          The expected property type.
   * @param pm
   *          The method calls {@link PmObject#findPmProperty(String)} on this
   *          object.
   * @param propName
   *          Path of the property to find.
   * @param propType
   *          The found property will be checked to be an instance of this
   *          class.
   * @return The found property or <code>null</code>.
   * @throws PmRuntimeException
   *           if the found property value is not compatible to the given
   *           propType parameter.
   */
  @SuppressWarnings("unchecked")
  public static <T> T findByExpression(PmObject pm, String propName, Class<T> propType) {
    Object o = findByExpression(pm, propName);
    if (o != null &&
        ! propType.isAssignableFrom(o.getClass())) {
      throw new PmRuntimeException(pm, "Invalid property type of pmProperty '" + propName +
          "'. It has the type '" + o.getClass() + "' expected class is '" + propType);
    }
    return (T)o;
  }

  /**
   * Imperative version of {@link #findByExpression(PmObject, String, Class)}.
   *
   * @throws PmRuntimeException if no property value was found.
   */
  public static <T> T getByExpression(PmObject pm, String propName, Class<T> propType) {
    T t = findByExpression(pm, propName, propType);
    if (t == null) {
      throw new PmRuntimeException(pm, "No property value found for path '" + propName + "'.");
    }
    return t;
  }


  /**
   * Finds an object within the named object scopes of the application.
   * <p>
   * In difference to {@link #findPmProperty(String)} this method does not
   * support object navigation expressions.
   *
   * @param objName Name of the object to find.
   * @return The found instance of <code>null</code>.
   */
  public static Object findNamedObject(PmObject pm, String objName) {
    return apiHandler.findNamedObject(pm, objName);
  }

}
