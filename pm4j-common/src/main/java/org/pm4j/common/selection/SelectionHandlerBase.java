package org.pm4j.common.selection;

import java.util.Collection;

import org.pm4j.common.util.beanproperty.PropertyChangeSupportedBase;

public abstract class SelectionHandlerBase<T_ITEM> extends PropertyChangeSupportedBase implements SelectionHandler<T_ITEM> {

  private SelectMode selectMode = SelectMode.SINGLE;

  @Override
  public void setSelectMode(SelectMode selectMode) {
    this.selectMode = selectMode;
  }

  public SelectMode getSelectMode() {
    return selectMode;
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

}
