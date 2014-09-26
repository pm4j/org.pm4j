package org.pm4j.common.util.beanproperty;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import org.junit.Test;

public class PropertyChangeSupportedBaseTest {

  private PropertyChangeSupportedBase observed = new PropertyChangeSupportedBase();
  private int vetoCallCount = 0;

  @Test
  public void vetoDoesNotFireRevertEvents() {
    observed.addVetoableChangeListener("prop", new VetoableChangeListener() {
      @Override
      public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        ++vetoCallCount;
        throw new PropertyVetoException("NO!", evt);
      }
    });

    try {
      observed.fireVetoableChange("prop", 0, 1);
      fail("A veto should be thrown.");
    } catch (PropertyVetoException e) {
    }
    assertEquals(1, vetoCallCount);
  }

}
