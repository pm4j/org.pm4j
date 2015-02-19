package org.pm4j.common.util.beanproperty;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


// TODO olaf: The setRef() and setRefQuietly() semantic is not intuitive. Check.
public class ReleaseOnPropChangeRef<T extends PropertyChangeSupported> implements PropertyChangeListener {
  private T ref;
  private final String[] releaseOnChangeOf;

  public ReleaseOnPropChangeRef(T ref, String... releaseOnChangeOf) {
    this.releaseOnChangeOf = releaseOnChangeOf;
    setRefQuietly(ref);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    setRef(null);
  }

  public T getRef() {
    return ref;
  }

  public void setRefQuietly(T ref) {
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
  }

  public void setRef(T ref) {
	setRefQuietly(ref);
    onSetRef();
  }

  /**
   * Gets called after setting the internal reference to another value.
   */
  protected void onSetRef() {
  }

}