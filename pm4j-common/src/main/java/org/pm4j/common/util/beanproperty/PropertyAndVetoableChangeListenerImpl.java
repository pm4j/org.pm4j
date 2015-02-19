package org.pm4j.common.util.beanproperty;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

/**
 * A basic listener implementation that does nothing.<br>
 * A convenience class for cases the only need to implement one of the methods.
 *
 * @author olaf boede
 *
 */
public class PropertyAndVetoableChangeListenerImpl implements PropertyAndVetoableChangeListener {

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
  }

  @Override
  public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
  }
}
