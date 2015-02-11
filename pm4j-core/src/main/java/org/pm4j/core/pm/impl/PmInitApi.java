package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.pm4j.common.util.collection.ListUtil;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmObject;
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
  public static <T extends PmObject> T initThisPmOnly(T pm) {
    ((PmObjectBase)pm).zz_ensurePmInitialization();
    return pm;
  }

  /**
   * Ensures that the passed PM and all of it's PM children are initialized.
   *
   * @param rootPm The root PM of the PM tree part to initialize.
   * @return the PM reference again for inline usage.
   */
  public static <T extends PmObject> T initPmTree(T rootPm) {
    PmVisitorImpl visitor = new PmVisitorImpl(new PmVisitCallBack() {
        @Override
        public PmVisitResult visit(PmObject pm) {
          initThisPmOnly(pm);
          return PmVisitResult.CONTINUE;
        }
      }
    ) {
      /**
       * Just visit the dynamic sub-PMs that already exist.
       * Don't ask that tables for their rows or tree nodes for their children
       * That could cause to generate expensive service calls in invisible areas.
       * And possibly the related domain code can't handle such calls in that early state.
       */
      @Override
      protected Iterable<PmObject> getChildren(PmObject pm) {
        Collection<PmObject> allChildren = new ArrayList<PmObject>();
        // get the set of fix embedded children.
        allChildren.addAll(((PmObjectBase) pm).getPmChildren());
        ListUtil.addItemsNotYetInCollection(allChildren, ((PmObjectBase) pm).getFactoryGeneratedChildPms());
        return allChildren;
      }
    };

    visitor.visit(rootPm);
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
    pm.zz_initMetaData((PmObjectBase)pm.getPmParent(), name, false, true);
    return pmAttr;
  }

}
