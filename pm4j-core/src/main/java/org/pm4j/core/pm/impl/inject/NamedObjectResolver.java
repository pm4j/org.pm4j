package org.pm4j.core.pm.impl.inject;

/**
 * Interface for technology specific adapters which resolve objects by name.
 * <p>
 * There are implementations for specific environments like EJB, Spring etc.
 *
 * @author olaf boede
 */
public interface NamedObjectResolver {

  /**
   * Searches a named object.
   *
   * @param name Name of the object to find.
   * @return The found instance or <code>null</code>.
   */
  Object findObject(String name);

}
