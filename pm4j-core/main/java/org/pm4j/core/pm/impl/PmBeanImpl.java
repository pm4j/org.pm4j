package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmObject;

/**
 * An implementation for a PM that represents the values of a bean.
 *
 * @author olaf boede
 *
 * @param <T_BEAN>
 */
public class PmBeanImpl<T_BEAN> extends PmBeanBase<T_BEAN> {

  public PmBeanImpl() {
    super();
  }

  public PmBeanImpl(PmObject pmParent, T_BEAN bean) {
    super(pmParent, bean);
  }

}
