package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrEnumImpl.MetaData;

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


  @Override
  public boolean isSupportingAsStringValues() {
    return false;
  }

  // ======== meta data ======== //

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

  protected static class MetaData extends PmAttrBase.MetaData {
    /**
     * The default for this type is 100.
     */
    @Override
    protected int getMaxLenDefault() {
      return 100;
    }
  }
}
