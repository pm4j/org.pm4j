package org.pm4j.common.util.beanproperty;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;

public interface PropertyChangeSupported {

  /**
   * Adds a listener that gets informed if a query property gets changed.
   *
   * @param propertyName identifier of the property to listen to.
   * @param listener the listener to add.
   */
  void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

  /**
   * Removes a property change listener.
   *
   * @param propertyName identifier of the property to listen to.
   * @param listener the listener to remove.
   */
  void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

  /**
   * Adds a listener that gets informed if a query property gets changed.
   *
   * @param propertyName identifier of the property to listen to.
   * @param listener the listener to add.
   */
  void addVetoableChangeListener(String propertyName, VetoableChangeListener listener);

  /**
   * Removes a property change listener.
   *
   * @param propertyName identifier of the property to listen to.
   * @param listener the listener to remove.
   */
  void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener);

  void addPropertyAndVetoableListener(String propertyName, PropertyAndVetoableChangeListener listener);
  void removePropertyAndVetoableListener(String propertyName, PropertyAndVetoableChangeListener listener);
}