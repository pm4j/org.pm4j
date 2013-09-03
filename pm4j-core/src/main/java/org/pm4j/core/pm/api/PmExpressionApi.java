package org.pm4j.core.pm.api;

import org.pm4j.common.expr.Expression.SyntaxVersion;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmExpressionApiHandler;
import org.pm4j.core.pm.impl.connector.NamedObjectResolver;

/**
 * An API that allows to resolve values by expressions.
 * <p>
 * Some PM expression documentation may be found on the page:<br>
 * https://github.com/pm4j/org.pm4j/wiki/Resolving-path-expressions .
 * <p>
 * TODO olaf: complete the expression syntax documentation!
 *
 * @author olaf boede
 */
public class PmExpressionApi {

  private static final PmExpressionApiHandler apiHandler = new PmExpressionApiHandler();

  /**
   * Finds an object for the given expression.
   *
   * @param pm
   *          the PM context.
   * @param expression
   *          the expession used to resolve the object.
   * @return the found object or <code>null</code> when not found.
   */
  public static Object findByExpression(PmObject pm, String expression) {
    return apiHandler.findByExpression(pm, expression);
  }

  /**
   * Imperative version of {@link #findPmProperty(String)}. Throws an exception
   * if there is no value for the given property.
   *
   * @param pm
   *          the PM context.
   * @param expression
   *          the expession used to resolve the object.
   * @return the found object.
   * @throws PmRuntimeException
   *           if there is no value for the given property name.
   */
  public static Object getByExpression(PmObject pm, String expression) throws PmRuntimeException {
    Object result = apiHandler.findByExpression(pm, expression);
    if (result == null) {
      throw new PmRuntimeException(pm, "Unable to resolve the expression '" + expression + "'.");
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
   *          the expected result type.
   * @param pm
   *          the PM context.
   * @param expression
   *          the expession used to resolve the object.
   * @param resultType
   *          the expectes result type.
   * @return The found object or <code>null</code>.
   * @throws PmRuntimeException
   *           if the found object is not compatible to the given
   *           <code>resultType</code>.
   */
  @SuppressWarnings("unchecked")
  public static <T> T findByExpression(PmObject pm, String expression, Class<T> resultType) {
    Object o = findByExpression(pm, expression);
    if (o != null && !resultType.isAssignableFrom(o.getClass())) {
      throw new PmRuntimeException(pm, "Expression '" + expression + "' provides an object with an unexpected type."
          + "\n Found type: '" + o.getClass() + "' but the code expects a kind of: '" + resultType + "'.");
    }
    return (T) o;
  }

  /**
   * Imperative version of {@link #findByExpression(PmObject, String, Class)}.
   *
   * @throws PmRuntimeException
   *           if no object was found.
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
   * In difference to {@link #findByExpression(PmObject, String)} this method
   * does not support object navigation expressions.
   * <p>
   * Uses the {@link NamedObjectResolver} configured within the {@link PmConversation}.
   *
   * @param objName
   *          name of the object to find.
   * @return the found instance of <code>null</code>.
   */
  public static Object findNamedObject(PmObject pm, String objName) {
    return apiHandler.findNamedObject(pm, objName);
  }

  /**
   * @param pm The PM to check.
   * @return The expression syntax version used for the given PM.
   */
  public static SyntaxVersion getSyntaxVersion(PmObject pm) {
    return pm.getPmConversation().getPmDefaults().getExpressionSyntaxVersion();
  }

}
