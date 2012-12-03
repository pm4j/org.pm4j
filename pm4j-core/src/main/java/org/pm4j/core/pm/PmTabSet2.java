package org.pm4j.core.pm;

import java.util.List;


/**
 * A PM for a tab set.
 *
 * @author olaf boede
 */
public interface PmTabSet2 extends PmDataInput {

  /**
   * This method gets called whenever the user or internal UI logic attempts
   * to switch from one opened tab to another one.
   *
   * @param toTab
   *          The tab to switch to.
   * @return <code>true</code> if the PM logic implementation allows the tab switch. <br>
   *         <code>false</code> if the PM logic implementation prevents the tab switch.
   */
  boolean switchToTabPm(PmTab toTab);

  /**
   * @return The currently active tab.
   */
  PmTab getCurrentTabPm();

  /**
   * @return The set of tabs.
   */
  List<PmTab> getTabPms();


}
