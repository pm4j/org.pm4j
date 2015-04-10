package org.pm4j.core.pm.impl;

import org.apache.commons.lang.Validate;
import org.pm4j.core.pm.PmEventListener;

/**
 * An abstract {@link PmEventListener} having a {@link #toString()} that reports a name.
 * <p>
 * That's sometimes useful to support IDE debugging of anonymous listener instances.
 *
 * @author Olaf Boede
 */
public abstract class PmEventListenerBase implements PmEventListener {

  private final String name;

  /**
   * @param name A name that gets reported by the {@link #toString()} method.
   */
  public PmEventListenerBase(String name) {
    Validate.notEmpty(name);
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
