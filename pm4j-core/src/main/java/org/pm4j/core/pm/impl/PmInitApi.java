package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitResult;
import org.pm4j.core.pm.impl.PmObjectBase.PmInitState;

/**
 * Some PM initialization related methods.
 *
 * @author Olaf Boede
 */
public class PmInitApi {

  /**
   * Ensures that the passed PM gets initialized.
   *
   * @param pm The PM to initialize.
   * @return the PM reference again for inline usage.
   */
  public static <T extends PmObject> T ensurePmInitialization(T pm) {
    ((PmObjectBase)pm).zz_ensurePmInitialization();
    return pm;
  }

  /**
   * Ensures that the passed PM and all of it's PM children are initialized.
   *
   * @param rootPm The root PM of the PM tree part to initialize.
   * @return the PM reference again for inline usage.
   */
  public static <T extends PmObject> T ensurePmSubTreeInitialization(T rootPm) {
    PmVisitorApi.visit(rootPm, new PmVisitCallBack() {
      @Override
      public PmVisitResult visit(PmObject pm) {
        ensurePmInitialization(pm);
        return PmVisitResult.CONTINUE;
      }
    });
    return rootPm;
  }

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

  public static PmInitState getPmInitState(PmObjectBase pm) {
    return pm.pmInitState;
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

}
