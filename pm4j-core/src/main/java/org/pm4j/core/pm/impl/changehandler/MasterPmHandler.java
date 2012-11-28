package org.pm4j.core.pm.impl.changehandler;

import java.util.Collection;
import java.util.Set;

import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.impl.PmTableUtil;

/**
 * Interface for handlers that observes changes within some details area. The
 * observed details changes can be inclueded in value change identification of
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
public interface MasterPmHandler extends PmCommandDecorator {

  /**
   * Indicates if changes occurred within the handled details area.
   * <p>
   * Considered time: The time span after the last {@link PmEvent#VALUE_CHANGE}
   * sent by the master PM.<br>
   * If there was not yet a master value change, it's the live time of the master.
   *
   * @return <code>true</code> .
   */
  // TODO olaf: change to ChangeSet(Handler) interface
  boolean isChangeRegistered();

  /**
   * Provides the set of master record beans, a change in the details area was observed for.
   * <p>
   * A change handler that can't provide the detailled set of changed beans may
   * provide here <code>null</code>.<br>
   * In this case the table uses only the information provided by
   * {@link #isChangeRegistered()}.
   *
   * @return The changed bean set.<br>
   *         <code>null</code> if that detailed information level is not
   *         supported.
   */
  // TODO olaf: change to ChangeSet(Handler) interface
  Set<?> getChangedMasterBeans();

  /**
   * Provides all detail bean changes grouped by their master beans.
   *
   * @return a map to a {@link ChangeSetHandler} that provides
   */
  // alternative: getDetailsHandler().getChangeSets()
//  Map<T_MASTER_BEAN, ChangeSet<T_DETAILS_BEAN>> getDetailsChangeSets();


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
  void addDetailsHander(DetailsPmHandler<?> detailsHandlers);

  /**
   * Adds a set of details handlers.
   *
   * @param detailsHandlers the handlers to add.
   */
  void addDetailsHanders(DetailsPmHandler<?>... detailsHandlers);

  /**
   * Provides the set of details PM specific handler.
   *
   * @return The configured {@link DetailsPmHandler} set.
   */
  Collection<DetailsPmHandler<?>> getDetailsPmHandlers();

}
