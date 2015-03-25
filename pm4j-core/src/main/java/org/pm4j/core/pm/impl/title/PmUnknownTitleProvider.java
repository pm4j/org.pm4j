package org.pm4j.core.pm.impl.title;

public final class PmUnknownTitleProvider<T> implements PmTitleProvider<T> {

  @Override
  public String getTitle(T item) {
    throw new UnsupportedOperationException("Method is not implemented.");
  }

  @Override
  public String getShortTitle(T item) {
    throw new UnsupportedOperationException("Method is not implemented.");
  }

  @Override
  public String getToolTip(T item) {
    throw new UnsupportedOperationException("Method is not implemented.");
  }

  @Override
  public String getIconPath(T item) {
    throw new UnsupportedOperationException("Method is not implemented.");
  }

  @Override
  public boolean canSetTitle(T item) {
    throw new UnsupportedOperationException("Method is not implemented.");
  }

  @Override
  public void setTitle(T item, String titleString) {
    throw new UnsupportedOperationException("Method is not implemented.");    
  }

}
