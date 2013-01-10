package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTableRow;

public class PmTableRowImpl<T_BEAN> extends PmBeanImpl<T_BEAN> implements PmTableRow<T_BEAN> {

  public PmTableRowImpl() {
    super();
  }

  public PmTableRowImpl(PmObject pmParent, T_BEAN bean) {
    super(pmParent, bean);
  }

}
