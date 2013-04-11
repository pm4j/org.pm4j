package org.pm4j.core.pm.impl.changehandler;

import java.util.Set;

/**
 * A master-details handler with a type safe interface for the changed master beans.
 *
 * @author olaf boede
 *
 * @param <T_RECORD_BEAN> type of the master record beans to track.
 */
// TODO olaf: check if this extra interface is really needed. - Less is often more...
public interface MasterPmRecordHandler<T_RECORD_BEAN> extends MasterPmHandler {

  /**
   * Provides the set of master beans with registered details changes.
   */
  @Override
  Set<T_RECORD_BEAN> getChangedMasterBeans();

}
