package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmObject;

/**
 * A presentation model that is not in front of a bean. It holds its data values locally.
 *
 * @author olaf boede
 * @deprecated Please use {@link org.pm4j.core.pm.impl.PmObjectBase}.
 */
@Deprecated
public class PmElementImpl extends PmElementBase {

  /**
   * Default constructor. Creates a not yet initialized element.
   * Before further usage the method {@link #setPmParent(PmObject)}
   * should be called.
   * <p>
   * Used for dependency injection frameworks that don't support
   * constructor initialization. For example JSF.
   */
  public PmElementImpl() {
  }

  /**
   * Creates an element within its session scope.
   *
   * @param pmParent The PM context the element is created in.
   */
  public PmElementImpl(PmObject pmParent) {
    super(pmParent);
  }

}
