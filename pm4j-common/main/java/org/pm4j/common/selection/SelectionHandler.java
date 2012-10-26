package org.pm4j.common.selection;

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

  /** Property change event identifier. */
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

}