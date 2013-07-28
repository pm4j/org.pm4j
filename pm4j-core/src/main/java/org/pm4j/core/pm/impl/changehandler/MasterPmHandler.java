package org.pm4j.core.pm.impl.changehandler;

import java.util.Collection;

import org.pm4j.common.pageable.Modifications;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmObject;

/**
 * Interface for handlers that observes changes within some details area. The
 * observed details changes can be included in value change identification of
 * the 'master' PM.
 * <p>
 * For master tables you may add a details change handler by calling
 * {@link PmTableUtil#addMasterDetailsPmHandler(org.pm4j.core.pm.PmTable, DetailsPmHandlerImpl)}
 * .<br>
 * After that call, the changes observed by the details handler are considered
 * in the <code>isPmValueChanged()</code> result of the master table.
 *
 * @author olaf boede
 */
// TODO olaf the handler should offer/support a command decorator but it shouldn't be one.
// It's not clear what 'before/afterDo' means for a master handler.
public interface MasterPmHandler extends PmCommandDecorator {

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
   * This is usually internally done within {@link PmTableUtil#addMasterDetailsPmHandler(PmTable, MasterDetailsPmHandler)}.
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
