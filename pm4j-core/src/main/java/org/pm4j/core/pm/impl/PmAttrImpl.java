package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmObject;

/**
 * Base implementation for user type attributes.
 *
 * @author olaf boede
 *
 * @param <T_VALUE> The value type to support.
 */
public class PmAttrImpl<T_VALUE> extends PmAttrBase<T_VALUE, T_VALUE> {

  public PmAttrImpl(PmObject pmParent) {
    super(pmParent);
  }

}
