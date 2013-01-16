package org.pm4j.common.selection;

import java.beans.PropertyChangeEvent;
import java.beans.VetoableChangeListener;

import org.pm4j.common.util.beanproperty.PropertyChangeSupported;


/**
 * Interface for classes that handle a set of selected PM items.
 * <p>
 * Implementations may handle the selection just as a set of <code>T_ITEM</code>.
 * Others may handle it just as a set of identifiers.
 * <p>
 * Fires property change events in case of selection changes.
 *
 * @param <T_ITEM>
 *          type of items to handle.
 *
 * @author olaf boede
 */
public interface SelectionHandler<T_ITEM> extends PropertyChangeSupported {

  /**
   * The selection property change event identifier.<br>
   * The type of the old- and new value provided by the fired {@link PropertyChangeEvent}
   * is {@link Selection}.
   */
  static final String PROP_SELECTION = "selection";

  /**
   * Provides the current selection mode.
   *
   * @return the current {@link SelectMode}.
   */
  SelectMode getSelectMode();

  /**
   * Defines the selection mode to use.
   *
   * @param selectMode the new selection mode to apply.
   */
  void setSelectMode(SelectMode selectMode);

  /**
   * Adds/removes the given item to/from the set of selected items.
   * <p>
   * Has no effect if the item is already selected/de-selected.
   *
   * @param select
   *          <code>true</code> selects the provided item.<br>
   *          <code>false</code> de-selects the provided item.
   * @param item
   *          the item to select.
   * @return <code>true</code> if the change was done.<br>
   *         <code>false</code> if the change was rejected by a
   *         {@link VetoableChangeListener}.
   */
  boolean select(boolean select, T_ITEM item);

  /**
   * Adds/removes the given item to/from the set of selected items.
   * <p>
   * Has no effect if the item is already selected/de-selected.
   * <p>
   * Throws a runtime exception if the selector is not {@link #isMultiSelect()}.
   *
   * @param select
   *          <code>true</code> selects the provided item.<br>
   *          <code>false</code> de-selects the provided item.
   * @param items
   *          the items to select.
   * @return <code>true</code> if the change was done.<br>
   *         <code>false</code> if the change was rejected by a
   *         {@link VetoableChangeListener}.
   */
  boolean select(boolean select, Iterable<T_ITEM> items);

  /**
   * Selects or de-selects all items.
   * <p>
   * If the <code>select</code> parameter is <code>true</code> and
   * {@link #isMultiSelect()} is <code>false</code>, a runtime exception will be
   * thrown.
   *
   * @param select
   *          <code>true</code> to select all items.<br>
   *          <code>false</code> to de-select all items.
   * @return <code>true</code> if the change was done.<br>
   *         <code>false</code> if the change was rejected by a
   *         {@link VetoableChangeListener}.
   */
  boolean selectAll(boolean select);

  /**
   * Inverts the current selection.
   * <p>
   * Should only be called in selection mode {@link SelectMode#MULTI}.
   *
   * @return <code>true</code> if the change was done.<br>
   *         <code>false</code> if the change was rejected by a
   *         {@link VetoableChangeListener}.
   */
  boolean invertSelection();

  /**
   * @return The set of selected items.
   */
  Selection<T_ITEM> getSelection();

  /**
   * @param selection the new selection to use.
   * @return <code>true</code> if the change was done.<br>
   *         <code>false</code> if the change was rejected by a
   *         {@link VetoableChangeListener}.
   */
  boolean setSelection(Selection<T_ITEM> selection);

  // --- support for domain specific initial selections ---

  /**
   * Registers a call back that may implement the logic for initial selections.
   *
   * @param callback the call back to add.
   */
  void addSelectionHandlerCallback(SelectionHandlerCallback callback);

  /**
   * Gets called after item set modification. It informs the handler to call
   * {@link SelectionHandlerCallback#ensureSelectionState()} before delivering
   * the next selection.
   */
  void ensureSelectionStateRequired();

  /**
   * A call back that allows to inject some initial selection handling.
   */
  public interface SelectionHandlerCallback {

    /**
     * Gets called whenever the collection was freshly loaded or an item was deleted.<br>
     * It may ensure that some domain specific selection restrictions are applied.<br>
     * E.g.: a table may require to have at least one item selected.
     */
    void ensureSelectionState();
  }

}
