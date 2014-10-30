package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTab;
import org.pm4j.core.pm.PmTabSet;

/**
 * Convenience helper methods for {@link PmTabSet}.
 *
 * @author Olaf Boede
 */
public class PmTabSetUtil {

  /**
   * Checks if the given PM is part of a tab that is currently not opened.
   * That means, that the given PM is hidden.
   *
   * @param pm
   *          The PM to check.
   * @return <code>true</code> if it is a hidden tab child.
   */
  public static boolean isInactiveTabChild(PmObject pm) {
    PmTab tabPm = PmUtil.findPmParentOfType(pm, PmTab.class);
    if (tabPm == null) {
      return false;
    }

    PmObject tabParentPm = tabPm.getPmParent();
    if (tabParentPm == null) {
      return false;
    }

    if (tabParentPm instanceof PmTabSet) {
      return ((PmTabSet)tabParentPm).getCurrentTabPm() != tabPm ||
             isInactiveTabChild(tabParentPm);
    }

    return isInactiveTabChild(tabParentPm);
  }

}
