package org.pm4j.core.pm.impl.changehandler;

import java.util.Set;

/**
 * A master-details handler with a type safe interface for the changed master beans.
 *
 * @author olaf boede
 *
 * @param <T_RECORD_BEAN> type of the master record beans to track.
 */
public interface MasterPmRecordHandler<T_RECORD_BEAN> extends MasterPmHandler {

  @Override
  public Set<T_RECORD_BEAN> getChangedMasterBeans();

}
