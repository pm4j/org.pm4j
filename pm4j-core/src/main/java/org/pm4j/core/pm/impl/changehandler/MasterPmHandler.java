package org.pm4j.core.pm.impl.changehandler;

import java.util.Collection;

import org.pm4j.common.modifications.Modifications;
import org.pm4j.core.pm.PmObject;

/**
 * Interface for handlers that observes changes within some details area. The
 * observed details changes can be included in value change identification of
 * the 'master' PM.
 *
 * @author olaf boede
 */
public interface MasterPmHandler {

  /**
   * Gets called before a master record selection change.
   *
   * @return <code>true</code> if the selection change can be performed.
   */
  boolean beforeMasterSelectionChange();

  /**
   * Gets called after master record selection change.
   */
  void afterMasterSelectionChange();

  /**
   * Provides the complete set of master record changes.
   * <p>
   * It does not support bean type generics because domain code usualy retrieves the changed bean
   * information usually directly from the master PM (e.g. a table PM).
   *
   * @return the current master modification state.
   */
  Modifications<?> getMasterBeanModifications();

  /**
   * This method needs to be called once before the handler gets active.
   */
  void startObservers();

  /**
   * Adds a details handler.
   *
   * @param detailsHandlers the handler to add.
   */
  void addDetailsHander(DetailsPmHandler detailsHandlers);

  /**
   * Adds a set of details handlers.
   *
   * @param detailsHandlers the handlers to add.
   */
  void addDetailsHanders(DetailsPmHandler... detailsHandlers);

  /**
   * Provides the set of details PM specific handler.
   *
   * @return The configured {@link DetailsPmHandler} set.
   */
  Collection<DetailsPmHandler> getDetailsPmHandlers();

  /**
   * @return the master PM.
   */
  PmObject getMasterPm();

}
