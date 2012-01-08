package org.pm4j.core.pm.impl.pathresolver;



/**
 * Interface for path algorithms that provide access to relatively addressed
 * objects.
 *
 * @author olaf boede
 */
public interface PathResolver {

  /**
   * @param startObj
   *          The object to start the navigation from.
   * @return The found value according to the path specification.
   */
  Object getValue(Object startObj);

  /**
   * @param pmParent
   *          The instance, used to create the execution context from.<br>
   *          Usually used to provide access to variables.
   * @param startObj
   *          The instance to start the evaluation from.
   * @return The evaluation result.
   */
  Object getValue(Object pmParent, Object startObj);

  /**
   * @param startObj
   *          The object to start the navigation from.
   * @param value
   *          The value to set to the field, addressed by the path.
   */
  void setValue(Object startObj, Object value);

  /**
   * @return <code>true</code> if the resolver may return <code>null</code>.
   */
  boolean isNullAllowed();

  /**
   * Defines if the resolver may return <code>null</code>.
   *
   * @param allowed
   */
  void setNullAllowed(boolean allowed);
}
