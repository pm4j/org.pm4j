package org.pm4j.common.util.beanproperty;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


public class ReleaseOnPropChangeRef<T extends PropertyChangeSupported> implements PropertyChangeListener {
  private T ref;
  private final String[] releaseOnChangeOf;

  public ReleaseOnPropChangeRef(T ref, String... releaseOnChangeOf) {
    this.releaseOnChangeOf = releaseOnChangeOf;
    setRef(ref);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    setRef(null);
  }

  public T getRef() {
    return ref;
  }

  public void setRef(T ref) {
	if (this.ref == ref) {
		return;
	}

    if (this.ref != null) {
      for (String p : releaseOnChangeOf) {
        this.ref.removePropertyChangeListener(p, this);
      }
    }
    this.ref = ref;
    if (ref != null) {
      for (String p : releaseOnChangeOf) {
        ref.addPropertyChangeListener(p, this);
      }
    }

    onSetRef();
  }

  /**
   * Gets called after setting the internal reference to another value.
   */
  protected void onSetRef() {
  }

}