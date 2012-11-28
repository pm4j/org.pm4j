package org.pm4j.core.pm.impl.changehandler;

import java.util.Map;

import org.pm4j.core.pm.PmDataInput;

/**
 * A details handler that records
 *
 * @author olaf boede
 *
 * @param <T_DETAILS_PM>
 * @param <T_MASTER_BEAN>
 * @param <T_DETAILS_BEAN>
 */
public interface DetailsPmHandlerWithChildChanges<T_DETAILS_PM extends PmDataInput, T_MASTER_BEAN, T_DETAILS_BEAN> extends DetailsPmHandler<T_DETAILS_PM> {

  /**
   * Provides all detail bean changes grouped by their master beans.
   *
   * @return a map to a {@link ChangeSetHandler} that provides
   */
  Map<T_MASTER_BEAN, ChangeSetHandler<T_DETAILS_BEAN>> getChangeSetHandler();

  // TODO olaf: we need a clear() functionality.
}
