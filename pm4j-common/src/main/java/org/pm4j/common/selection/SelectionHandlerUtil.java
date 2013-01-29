package org.pm4j.common.selection;

import org.pm4j.common.util.beanproperty.ForcedPropertyChange;
import org.pm4j.common.util.beanproperty.PropertyChangeSupported;

/**
 * Helper methods for selection handler functionality.
 *
 * @author olaf boede
 */
public class SelectionHandlerUtil {

  /**
   * Switches veto event notification off and sets the selection.<br>
   * See also: {@link SelectionHandler#selectAll(boolean)}.
   */
  public static void forceSelectAll(final SelectionHandler<?> selectionHandler, final boolean doSelectItems) {
    new ForcedPropertyChange(selectionHandler) {
      @Override
      protected void doItImpl() {
        selectionHandler.selectAll(doSelectItems);
      }
    }.doIt();
  }

  /**
   * Switches veto event notification off and sets the selection.<br>
   * See also: {@link SelectionHandler#select(boolean, Object)}.
   */
  public static <T_ITEM> void forceSelect(final SelectionHandler<T_ITEM> selectionHandler, final boolean doSelectItem, final T_ITEM item) {
    new ForcedPropertyChange(selectionHandler) {
      @Override
      protected void doItImpl() {
        selectionHandler.select(doSelectItem, item);
      }
    }.doIt();
  }

  /**
   * Switches veto event notification off and sets the selection.<br>
   * See also: {@link SelectionHandler#select(boolean, Iterable)}.
   */
  public static <T_ITEM> void forceSelect(final SelectionHandler<T_ITEM> selectionHandler, final boolean doSelectItem, final Iterable<T_ITEM> items) {
    new ForcedPropertyChange(selectionHandler) {
      @Override
      protected void doItImpl() {
        selectionHandler.select(doSelectItem, items);
      }
    }.doIt();
  }

  /**
   * Switches veto event notification off and sets the selection.<br>
   * See also: {@link SelectionHandler#invertSelection()}.
   */
  public static <T_ITEM> void forceInvertSelection(final SelectionHandler<T_ITEM> selectionHandler) {
    new ForcedPropertyChange(selectionHandler) {
      @Override
      protected void doItImpl() {
        selectionHandler.invertSelection();
      }
    }.doIt();
  }

  /**
   * Switches veto event notification off and sets the selection.<br>
   * See also: {@link SelectionHandler#setSelection(Selection)}.
   */
  public static <T_ITEM> void forceSetSelection(final SelectionHandler<T_ITEM> selectionHandler, final Selection<T_ITEM> selection) {
    new ForcedPropertyChange(selectionHandler) {
      @Override
      protected void doItImpl() {
        selectionHandler.setSelection(selection);
      }
    }.doIt();
  }

  /**
   * Executes the select activity in the same (forced or unforced) mode as
   * defined for the given parent selection handler.<br>
   * See also: {@link SelectionHandler#selectAll(boolean)}.
   *
   * @param parentSelectionHandler
   *          the handler the <code>vetoEventEnabled</code> property is read
   *          from.<br>
   *          See: {@link PropertyChangeSupported#isVetoEventEnabled()}.
   */
  public static boolean selectAllInSameForceMode(SelectionHandler<?> parentSelectionHandler, final SelectionHandler<?> nestedSelectionHandler, final boolean doSelectItems) {
    if (parentSelectionHandler.isVetoEventEnabled()) {
      return nestedSelectionHandler.selectAll(doSelectItems);
    } else {
      forceSelectAll(nestedSelectionHandler, doSelectItems);
      return true;
    }
  }

  /**
   * Executes the select activity in the same (forced or unforced) mode as
   * defined for the given parent selection handler.<br>
   * See also: {@link SelectionHandler#select(boolean, Object)}.
   *
   * @param parentSelectionHandler
   *          the handler the <code>vetoEventEnabled</code> property is read
   *          from.<br>
   *          See: {@link PropertyChangeSupported#isVetoEventEnabled()}.
   */
  public static <T_ITEM> boolean selectInSameForceMode(SelectionHandler<?> parentSelectionHandler, final SelectionHandler<T_ITEM> nestedSelectionHandler, boolean doSelectItem, T_ITEM item) {
    if (parentSelectionHandler.isVetoEventEnabled()) {
      return nestedSelectionHandler.select(doSelectItem, item);
    } else {
      forceSelect(nestedSelectionHandler, doSelectItem, item);
      return true;
    }
  }

  /**
   * Executes the select activity in the same (forced or unforced) mode as
   * defined for the given parent selection handler.<br>
   * See also: {@link SelectionHandler#select(boolean, Iterable)}.
   *
   * @param parentSelectionHandler
   *          the handler the <code>vetoEventEnabled</code> property is read
   *          from.<br>
   *          See: {@link PropertyChangeSupported#isVetoEventEnabled()}.
   */
  public static <T_ITEM> boolean selectInSameForceMode(SelectionHandler<?> parentSelectionHandler, final SelectionHandler<T_ITEM> nestedSelectionHandler, boolean doSelectItem, Iterable<T_ITEM> items) {
    if (parentSelectionHandler.isVetoEventEnabled()) {
      return nestedSelectionHandler.select(doSelectItem, items);
    } else {
      forceSelect(nestedSelectionHandler, doSelectItem, items);
      return true;
    }
  }

  /**
   * Executes the select activity in the same (forced or unforced) mode as
   * defined for the given parent selection handler.<br>
   * See also: {@link SelectionHandler#invertSelection()}.
   *
   * @param parentSelectionHandler
   *          the handler the <code>vetoEventEnabled</code> property is read
   *          from.<br>
   *          See: {@link PropertyChangeSupported#isVetoEventEnabled()}.
   */
  public static <T_ITEM> boolean invertSelectionInSameForceMode(SelectionHandler<?> parentSelectionHandler, final SelectionHandler<T_ITEM> nestedSelectionHandler) {
    if (parentSelectionHandler.isVetoEventEnabled()) {
      return nestedSelectionHandler.invertSelection();
    } else {
      forceInvertSelection(nestedSelectionHandler);
      return true;
    }
  }

  /**
   * Executes the select activity in the same (forced or unforced) mode as
   * defined for the given parent selection handler.<br>
   * See also: {@link SelectionHandler#setSelection(Selection)}.
   *
   * @param parentSelectionHandler
   *          the handler the <code>vetoEventEnabled</code> property is read
   *          from.<br>
   *          See: {@link PropertyChangeSupported#isVetoEventEnabled()}.
   */
  public static <T_ITEM> boolean setSelectionInSameForceMode(SelectionHandler<?> parentSelectionHandler, final SelectionHandler<T_ITEM> nestedSelectionHandler, final Selection<T_ITEM> selection) {
    if (parentSelectionHandler.isVetoEventEnabled()) {
      return nestedSelectionHandler.setSelection(selection);
    } else {
      forceSetSelection(nestedSelectionHandler, selection);
      return true;
    }
  }


}
