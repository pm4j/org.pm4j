package org.pm4j.common.util.beanproperty;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

import org.pm4j.common.exception.CheckedExceptionWrapper;

/**
 * Provides some property change support.
 *
 * @author Olaf Boede
 */
public class PropertyChangeSupportedBase implements PropertyChangeSupported, Cloneable {

  private PropertyChangeSupport pcs;
  private VetoableChangeSupport vpcs;
  /** A switch that may be used the switch firing of veto events off. */
  private boolean fireVetoEvents = true;
  /** A switch that may be used to switch firing of all property events off. */
  private boolean firePropertyEvents = true;

  @Override
  public <T extends PropertyChangeListener> T addPropertyChangeListener(String propertyName, T listener) {
    getPcs().addPropertyChangeListener(propertyName, listener);
    return listener;
  }

  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    getPcs().removePropertyChangeListener(propertyName, listener);
  }

  public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (firePropertyEvents) {
      getPcs().firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  public void firePropertyChange(PropertyChangeEvent event) {
    if (firePropertyEvents) {
      getPcs().firePropertyChange(event);
    }
  }

  @Override
  public <T extends VetoableChangeListener> T addVetoableChangeListener(String propertyName, T listener) {
    getVpcs().addVetoableChangeListener(propertyName, listener);
    return listener;
  }

  @Override
  public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
    getVpcs().removeVetoableChangeListener(propertyName, listener);
  }

  @Override
  public <T extends PropertyAndVetoableChangeListener> T addPropertyAndVetoableListener(String propertyName, T listener) {
    addPropertyChangeListener(propertyName, listener);
    addVetoableChangeListener(propertyName, listener);
    return listener;
  }

  @Override
  public void removePropertyAndVetoableListener(String propertyName, PropertyAndVetoableChangeListener listener) {
    removePropertyChangeListener(propertyName, listener);
    removeVetoableChangeListener(propertyName, listener);
  }

  @Override
  public void setFireVetoEvents(boolean vetoEventEnabled) {
    this.fireVetoEvents = vetoEventEnabled;
  }

  @Override
  public boolean isFireVetoEvents() {
    return fireVetoEvents;
  }

  public boolean isFirePropertyEvents() {
    return firePropertyEvents;
  }

  public void setFirePropertyEvents(boolean firingPropertyEvents) {
    this.firePropertyEvents = firingPropertyEvents;
  }

  public void fireVetoableChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException {
    if (firePropertyEvents && fireVetoEvents) {
      getVpcs().fireVetoableChange(propertyName, oldValue, newValue);
    }
  }

  public void fireVetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
    if (firePropertyEvents && fireVetoEvents) {
      getVpcs().fireVetoableChange(event);
    }
  }

  /**
   * Does not clone the observer references. These observers are only registered for the original.
   */
  @Override
  protected PropertyChangeSupportedBase clone() {
  try {
    PropertyChangeSupportedBase clone = (PropertyChangeSupportedBase) super.clone();
    clone.pcs = null;
    clone.vpcs = null;
    clone.fireVetoEvents = true;
    return clone;
  } catch (CloneNotSupportedException e) {
    throw new CheckedExceptionWrapper(e);
  }
  }

  private PropertyChangeSupport getPcs() {
    if (pcs == null) {
      pcs = new PropertyChangeSupport(this);
    }
    return pcs;
  }

  private VetoableChangeSupport getVpcs() {
    if (vpcs == null) {
      vpcs = new VetoableChangeSupport(this);
    }
    return vpcs;
  }

}
