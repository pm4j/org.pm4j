package org.pm4j.core.pm.impl.connector;

import org.pm4j.core.pm.PmDataInput;

/**
 * View technology specific adapter for tabs.
 *
 * @author olaf boede
 *
 */
public interface PmTabSetConnector {

  void _switchToTab(PmDataInput pmTab);

}
