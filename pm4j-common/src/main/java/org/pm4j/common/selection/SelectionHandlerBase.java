package org.pm4j.common.selection;

import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pm4j.common.util.beanproperty.PropertyChangeSupportedBase;

public abstract class SelectionHandlerBase<T_ITEM> extends PropertyChangeSupportedBase implements SelectionHandler<T_ITEM> {

  private SelectMode selectMode = SelectMode.SINGLE;
  private List<SelectionHandlerCallback> selectionHandlerCallBacks = new ArrayList<SelectionHandlerCallback>();
  /** A flag that helps to minimize the number of {@link #ensureSelectionState()} calls.  */
  boolean ensureSelectionStateCalled;

  /**
   * @param selection the new selection to use.
   * @return <code>true</code> if the change was done.<br>
   *         <code>false</code> if the change was rejected by a
   *         {@link VetoableChangeListener}.
   *
   * TODO oboede: reduce visiblity to protected as soon as this method disappears from the interface.
   */
  public abstract boolean setSelection(Selection<T_ITEM> selection);

  @Override
  public void setSelectMode(SelectMode selectMode) {
    this.selectMode = selectMode;
    ensureSelectionStateCalled = false;
  }

  public SelectMode getSelectMode() {
    return selectMode;
  }

  @Override
  public void addSelectionHandlerCallback(SelectionHandlerCallback callback) {
    if (callback != null && !selectionHandlerCallBacks.contains(callback)) {
      selectionHandlerCallBacks.add(callback);
      ensureSelectionStateCalled = false;
    }
  }

  @Override
  public void ensureSelectionStateRequired() {
    ensureSelectionStateCalled = false;
  }

  protected void beforeAddSingleItemSelection(Collection<?> currentSelection) {
    switch (selectMode) {
      case SINGLE: currentSelection.clear(); break;
      case MULTI: break;
      default: throw new RuntimeException("Selection for select mode '" + selectMode + "' is not supported.");
    }
  }

  protected void checkMultiSelectResult(Collection<?> newSelection) {
    switch (selectMode) {
      case SINGLE: if (newSelection.size() > 1) {
          throw new RuntimeException("Only one item can be selected in select mode " + selectMode);
        }
      break;
      case MULTI: break;
      default: if (newSelection.size() > 0) {
          throw new RuntimeException("Selection for current select mode is not supported: " + selectMode);
        }
    }

  }

  /**
   * Makes sure that the selection is initialized by using the registered {@link SelectionHandlerCallback}s.
   */
  protected void ensureSelectionState() {
    if (!ensureSelectionStateCalled) {
      try {
        // prevent loops:
        ensureSelectionStateCalled = true;
        for (SelectionHandlerCallback c : selectionHandlerCallBacks) {
          c.ensureSelectionState();
        }
      } catch (RuntimeException e) {
        ensureSelectionStateCalled = false;
        throw e;
      }
    }
  }

}
