package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmObjectBase.PmInitState;

/**
 * Some PM initialization related methods.
 *
 * @author olaf boede
 */
public class PmInitApi {

  /**
   * Checks if the given PM is completely initialized.
   *
   * @param pm The PM to check.
   * @return <code>true</code> if the given PM is initialized.
   */
  public static boolean isPmInitialized(PmObject pm) {
    return pm != null
            ? ((PmObjectBase)pm).pmInitState == PmInitState.INITIALIZED
            : false;
  }

  /**
   * EXPERIMENTAL STATE: Helper for dynamic PM creation.
   */
  public static <T> PmAttr<T> initDynamicPmAttr(PmAttr<T> pmAttr, String name) {
    PmAttrBase<?, ?> pm = (PmAttrBase<?, ?>)pmAttr;
    // Make the name based meta data identifier unique based on the attribute class identifier.
    // TODO: the methods getPmName,getPmReskey etc. provide strange results now :-(
    pm.zz_initMetaData((PmObjectBase)pm.getPmParent(), name+"_"+pm.getClass().getName(), false, true);
    return pmAttr;
  }

  public static void ensurePmInitialization(PmObject pm) {
    ((PmObjectBase)pm).zz_ensurePmInitialization();
  }

}
