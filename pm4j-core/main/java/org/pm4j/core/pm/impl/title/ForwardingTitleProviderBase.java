package org.pm4j.core.pm.impl.title;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;

public abstract class ForwardingTitleProviderBase<T extends PmObject> implements PmTitleProvider<T> {

  protected abstract PmObject getTitleProvidingObject(T frontObject);

  public boolean canSetTitle(T item) {
    return getTitleProvidingObject(item).canSetPmTitle();
  }

  public String getTitle(T item) {
    return getTitleProvidingObject(item).getPmTitle();
  }

  public String getShortTitle(T item) {
    return getTitleProvidingObject(item).getPmShortTitle();
  }

  public String getToolTip(T item) {
    return getTitleProvidingObject(item).getPmTooltip();
  }

  public String getIconPath(T item) {
    return getTitleProvidingObject(item).getPmIconPath();
  }

  public void setTitle(T item, String titleString) {
    getTitleProvidingObject(item).setPmTitle(titleString);
  }

  public String findLocalization(T item, String key, Object... resStringArgs) {
    throw new PmRuntimeException("Not implemented for this titleprovider (" + getClass() + "). Key=" + key + " item=" + item);
  }

  public String getLocalization(T item, String key, Object... resStringArgs) {
    throw new PmRuntimeException("Not implemented for this titleprovider (" + getClass() + "). Key=" + key + " item=" + item);
  }

  public String findResStringForKey(T item, String key) {
    throw new PmRuntimeException("Not implemented for this titleprovider (" + getClass() + "). Key=" + key + " item=" + item);
  }

}
