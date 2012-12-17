package org.pm4j.core.pm.impl.connector;

import org.pm4j.core.pm.PmDataInput;

/**
 * View technology specific adapter for tabs.
 *
 * @author olaf boede
 *
 */
public interface PmTabSetConnector {

  /**
   * A call back method that is called when the PM has switched to another tab.
   * The view should arrange its state here.
   *
   * @param pmTab the tab to switch to.
   */
  void switchToTab(PmDataInput pmTab);

}
