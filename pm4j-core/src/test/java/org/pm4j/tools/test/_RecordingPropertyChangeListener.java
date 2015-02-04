package org.pm4j.tools.test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pm4j.common.util.beanproperty.PropertyAndVetoableChangeListener;

/**
 * Records the observed property change and veto events.<br>
 * Is helpful for tests that ensure stability of events.
 *
 * @author Olaf Boede
 */
public class _RecordingPropertyChangeListener implements PropertyAndVetoableChangeListener {

  private List<PropertyChangeEvent> propertyChangeCalls = new ArrayList<PropertyChangeEvent>();
  private List<PropertyChangeEvent> vetoableChangeCalls = new ArrayList<PropertyChangeEvent>();

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    propertyChangeCalls.add(evt);
  }

  @Override
  public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
    vetoableChangeCalls.add(evt);
  }

  /**
   * @return the propertyChangeCalls
   */
  public List<PropertyChangeEvent> getPropertyChangeCalls() {
    return Collections.unmodifiableList(propertyChangeCalls);
  }

  /**
   * @return the vetoableChangeCalls
   */
  public List<PropertyChangeEvent> getVetoableChangeCalls() {
    return Collections.unmodifiableList(vetoableChangeCalls);
  }

  public int getNumOfPropertyChangesCalls() {
    return propertyChangeCalls.size();
  }

  public int getNumOfVetoableChangesCalls() {
    return vetoableChangeCalls.size();
  }

}
