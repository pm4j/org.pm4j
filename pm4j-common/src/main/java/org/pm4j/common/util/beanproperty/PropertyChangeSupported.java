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

  /**
   * Allows to control wether veto events get fired or not.<br>
   * May be used to force a property change by temporarily disabling vetos.
   *
   * @param vetoEventEnabled <code>true</code> allows vetos.
   */
  void setFireVetoEvents(boolean vetoEventEnabled);

  /**
   * Tells if veto events are fired.
   *
   * @return <code>true</code> if veto events get fired.
   */
  boolean isFireVetoEvents();

  /**
   * Tells if firing of any property and veto event is enabled.
   *
   * @return <code>true</code> if veto events get fired.
   */
  boolean isFirePropertyEvents();

  /**
   * Defines if firing of any property and veto event is enabled.
   *
   * @param firingPropertyEvents <code>true</code> allows events.
   */
  void setFirePropertyEvents(boolean firingPropertyEvents);

  /**
   * Adds the given listener to the set of property change and veto listeners.
   *
   * @param propertyName identifier of the property to listen to.
   * @param listener the listener to add.
   */
  void addPropertyAndVetoableListener(String propertyName, PropertyAndVetoableChangeListener listener);

  /**
   * Removes the given listener from the set of property change and veto listeners.
   *
   * @param propertyName identifier of the property to listen to.
   * @param listener the listener to remove.
   */
  void removePropertyAndVetoableListener(String propertyName, PropertyAndVetoableChangeListener listener);
}