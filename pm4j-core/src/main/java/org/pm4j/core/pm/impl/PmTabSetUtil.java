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
   * Checks if the given tab PM is the current active tab of its direct parent tab.
   * <p>
   * If the given PM is not used in a {@link PmTabSet}, this method returns <code>false</code>.
   *
   * @param tabPm
   *          The PM to check.
   * @return <code>true</code> if it is the current tab of its direct {@link PmTabSet} parent.
   */
  public static boolean isCurrentTab(PmTab tabPm) {
    PmObject tabParentPm = tabPm.getPmParent();
    return (tabParentPm instanceof PmTabSet) &&
           ((PmTabSet)tabParentPm).getCurrentTabPm() == tabPm;
  }

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
    if (!isCurrentTab(tabPm)) {
      return true;
    }
    return tabPm.getPmParent() != null &&
           isInactiveTabChild(tabPm.getPmParent());
  }

}
