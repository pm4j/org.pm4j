package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmAttr;

public class PmInitApi {

  /**
   * EXPERIMENTAL STATE: Helper for dynamic PM creation.
   */
  public static PmAttr<?> initDynamicPmAttr(PmAttr<?> pmAttr, String name) {
    PmAttrBase<?, ?> pm = (PmAttrBase<?, ?>)pmAttr;
    // Make the name based meta data identifier unique based on the attribute class identifier.
    // TODO: the methods getPmName,getPmReskey etc. provide strange results now :-(
    pm.zz_initMetaData((PmObjectBase)pm.getPmParent(), name+"_"+pm.getClass().getName(), false, true);
    return pmAttr;
  }

}
