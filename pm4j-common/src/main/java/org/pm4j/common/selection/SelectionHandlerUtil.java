package org.pm4j.common.selection;

import org.pm4j.common.util.CompareUtil;
import org.pm4j.common.util.beanproperty.ForcedPropertyChange;
import org.pm4j.common.util.beanproperty.PropertyChangeSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods for selection handler functionality.
 *
 * @author olaf boede
 */
public class SelectionHandlerUtil {

  private static final Logger LOG = LoggerFactory.getLogger(SelectionHandlerUtil.class);

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
   *          See: {@link PropertyChangeSupported#isFireVetoEvents()}.
   */
  public static boolean selectAllInSameForceMode(SelectionHandler<?> parentSelectionHandler, final SelectionHandler<?> nestedSelectionHandler, final boolean doSelectItems) {
    if (parentSelectionHandler.isFireVetoEvents()) {
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
   *          See: {@link PropertyChangeSupported#isFireVetoEvents()}.
   */
  public static <T_ITEM> boolean selectInSameForceMode(SelectionHandler<?> parentSelectionHandler, final SelectionHandler<T_ITEM> nestedSelectionHandler, boolean doSelectItem, T_ITEM item) {
    if (parentSelectionHandler.isFireVetoEvents()) {
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
   *          See: {@link PropertyChangeSupported#isFireVetoEvents()}.
   */
  public static <T_ITEM> boolean selectInSameForceMode(SelectionHandler<?> parentSelectionHandler, final SelectionHandler<T_ITEM> nestedSelectionHandler, boolean doSelectItem, Iterable<T_ITEM> items) {
    if (parentSelectionHandler.isFireVetoEvents()) {
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
   *          See: {@link PropertyChangeSupported#isFireVetoEvents()}.
   */
  public static <T_ITEM> boolean invertSelectionInSameForceMode(SelectionHandler<?> parentSelectionHandler, final SelectionHandler<T_ITEM> nestedSelectionHandler) {
    if (parentSelectionHandler.isFireVetoEvents()) {
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
   *          See: {@link PropertyChangeSupported#isFireVetoEvents()}.
   */
  public static <T_ITEM> boolean setSelectionInSameForceMode(SelectionHandler<?> parentSelectionHandler, final SelectionHandler<T_ITEM> nestedSelectionHandler, final Selection<T_ITEM> selection) {
    if (parentSelectionHandler.isFireVetoEvents()) {
      return nestedSelectionHandler.setSelection(selection);
    } else {
      forceSetSelection(nestedSelectionHandler, selection);
      return true;
    }
  }

  /**
   * @param lhs
   * @param rhs
   *
   * @return <code>true</code> if both selections contain the same set of items.
   */
  public static <T> boolean sameSelection(Selection<T> lhs, Selection<T> rhs) {
    // TODO: add a configuration option or parameter.
    return sameSelection(lhs, rhs, 50);
  }


  /**
   * @param lhs
   * @param rhs
   * @param maxItemByItemCompareSize
   *   If the selection combination donsn't support {@link Selection#hasSameItemSet(Selection)},
   *   the items can be compared until the given item count.<br>
   *   This happens only if both selections have in addition the same size.<br>
   *   It these not comparable selections are larger, this method returns <code>false</code>.
   *
   * @return <code>true</code> if both selections contain the same set of items.
   */
  public static <T> boolean sameSelection(Selection<T> lhs, Selection<T> rhs, int maxItemByItemCompareSize) {
    if (lhs == rhs) {
      return true;
    }
    if (lhs.getSize() != rhs.getSize()) {
      return false;
    }
    if (lhs.isEmpty()) {
      return true;
    }

    try {
      return lhs.hasSameItemSet(rhs);
    } catch (UnsupportedOperationException e) {
      if (lhs.getSize() > maxItemByItemCompareSize) {
        LOG.debug("Comparing selections of different types and more than " + maxItemByItemCompareSize +""
             + " items caused an additional selection change event.\n"
             + "lhs: " + lhs.getClass() + " rhs: " + rhs.getClass());
        return false;
      } else {
        // Item by item iteration as fall back.
        return CompareUtil.sameItemSet(lhs, rhs, (int)lhs.getSize());
      }
    }
  }

}
