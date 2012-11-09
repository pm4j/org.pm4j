package org.pm4j.core.pm;

import org.pm4j.common.selection.SelectionHandler;

/**
 * Interface for PMs such as tables and lists that support item selections.
 *
 * @param <T_ROW_OBJ> type of selectable items. In case of a table it is the type of handled row PMs.
 *
 * @author olaf boede
 */
public interface PmSelectable<T_ROW_OBJ> {

  /**
   * Provides all selection related operations.
   *
   * @return the {@link SelectionHandler}.
   */
  SelectionHandler<T_ROW_OBJ> getPmSelectionHandler();

}
