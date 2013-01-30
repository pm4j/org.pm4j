package org.pm4j.common.util.beanproperty;

/**
 * A class that may be used to execute a method on a {@link PropertyChangeSupported}
 * instance with a switched off the veto feature.
 *
 * @author olaf boede
 */
public abstract class ForcedPropertyChange {

  public final PropertyChangeSupported targetInstance;

  /**
   * Creates the instance.
   * The forced call needs to be triggered by calling {@link #doIt()}.
   *
   * @param targetInstance the veto feature gets temporarily switched off for this instance.
   */
  public ForcedPropertyChange(PropertyChangeSupported targetInstance) {
    this.targetInstance = targetInstance;
  }

  public void doIt() {
    boolean wasVetoEnabled = targetInstance.isFireVetoEvents();
    targetInstance.setFireVetoEvents(false);
    try {
      doItImpl();
    } finally {
      targetInstance.setFireVetoEvents(wasVetoEnabled);
    }
  }


  protected abstract void doItImpl();
}
