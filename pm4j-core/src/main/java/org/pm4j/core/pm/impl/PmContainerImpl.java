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

  /**
   * A convenience base class that provides a default constructor.
   * <p>
   * Please make sure that {@link #setPmParent(PmObject)} gets called before the
   * PM gets used.
   * <p>
   * This kind of deferred initialization is useful for some object management
   * systems that don't support constructor parameters.<br>
   * An example: JSF managed beans use only setters to resolve dependencies.
   */
  public static class WithDeferredParentAssignment extends PmContainerImpl {

    /**
     * Default contructor.<br>
     * Please don't forget to call {@link #setPmParent(PmObject)} before this
     * instance gets used.
     */
    public WithDeferredParentAssignment() {
      super(null);
    }

    /**
     * The class also provides a parent defining constructor.
     *
     * @param pmParent the PM tree parent.
     */
    public WithDeferredParentAssignment(PmObject pmParent) {
      super(pmParent);
    }
  }
}
