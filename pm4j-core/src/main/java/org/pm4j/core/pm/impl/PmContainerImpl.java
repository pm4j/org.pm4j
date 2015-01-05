package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmContainer;
import org.pm4j.core.pm.PmObject;

/**
 * Implementation base class for PM container classes/instances.
 * <p>
 * TODO oboede: Will be the replacement for the weak named {@link PmElementImpl}.
 *
 * @author olaf boede
 */
public class PmContainerImpl extends PmElementImpl implements PmContainer {

  /**
   * Creates the PM and assigns the parent.
   *
   * @param pmParent
   *          the parent of this instance.<br>
   *          If you pass <code>null</code>, please make sure that you define a
   *          parent using {@link #setPmParent(PmObject)} before you use the PM
   *          instance.
   */
  public PmContainerImpl(PmObject pmParent) {
    super(pmParent);
  }

}
