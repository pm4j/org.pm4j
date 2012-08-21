package org.pm4j.core.pm.impl.changehandler;

import java.util.Set;

import org.pm4j.core.pm.impl.PmTableUtil;

/**
 * Interface for handlers that observes changes within some details area. The
 * observed details changes can be inclueded in value change identification of
 * the 'master' PM.
 * <p>
 * For master tables you may add a details change handler by calling
 * {@link PmTableUtil#addDetailsPmHandler(org.pm4j.core.pm.PmTable, DetailsPmHandler)}
 * .<br>
 * After that call, the changes observed by the details handler are considered
 * in the <code>isPmValueChanged()</code> result of the master table.
 *
 * @author olaf boede
 */
public interface DetailsPmHandler {

  /**
   * Indicates if something was changed within the observed details area.
   *
   * @return The change state.
   */
  boolean isDetailsChangeRegistered();

  /**
   * Provides the set of changed detail beans.
   * <p>
   * A change handler that can't provide the detailled set of changed beans may
   * provide here <code>null</code>.<br>
   * In this case the table uses only the information provided by
   * {@link #isDetailsChangeRegistered()}.
   *
   * @return The changed bean set.<br>
   *         <code>null</code> if that detailed information level is not
   *         supported.
   */
  Set<?> getChangedDetailBeans();

}
