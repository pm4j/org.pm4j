package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmObject;

/**
 * An implementation for a PM that represents the values of a bean.
 *
 * @author olaf boede
 *
 * @param <T_BEAN> type of handled beans.
 */
public class PmBeanImpl<T_BEAN> extends PmBeanBase<T_BEAN> {

  /**
   * A default constructor for some DI container (e.g. JSF) that don't support
   * ctor parameters.<br>
   * It is also supported by the PM factories.
   * <p>
   * Pease ensure that {@link #setPmParent(PmObject)} gets called before this PM
   * gets used.
   */
  public PmBeanImpl() {
    super();
  }

  /**
   * Creates a PM with an initial <code>null</code>-bean.
   *
   * @param pmParent
   *          the PM hierarchy parent.
   */
  public PmBeanImpl(PmObject pmParent) {
    super(pmParent, null);
  }

  /**
   * Creates a PM with an initial bean.
   *
   * @param pmParent
   *          the PM hierarchy parent.
   * @param bean
   *          the bean behind this PM.
   */
  public PmBeanImpl(PmObject pmParent, T_BEAN bean) {
    super(pmParent, bean);
  }

}
