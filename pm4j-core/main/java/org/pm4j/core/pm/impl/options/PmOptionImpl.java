package org.pm4j.core.pm.impl.options;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.pm4j.core.pm.PmOption;

/**
 * A simple ui option bean.
 */
public class PmOptionImpl implements PmOption {

  private Serializable id;

  private String title;

  private Object itemValue;

  /** Option enabled flag. */
  private boolean enabled;

  public PmOptionImpl(Serializable id) {
    this(id, ObjectUtils.toString(id), id);
  }

  public PmOptionImpl(Serializable id, String title) {
    this(id, title, id);
  }

  public PmOptionImpl(Serializable id, String title, boolean enabled) {
    this(id, title, id, enabled);
  }

  public PmOptionImpl(Serializable id, String title, Object itemValue) {
    this(id, title, itemValue, true);
  }

  public PmOptionImpl(Serializable id, String title, Object itemValue, boolean enabled) {
    this.id = id;
    this.title = title;
    this.itemValue = itemValue;
    this.enabled = enabled;
  }

  public Serializable getId() {
    return id;
  }

  public String getPmTitle() {
    return title;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue() {
    return (T) itemValue;
  }

  @Override
  public String getIdAsString() {
    Serializable id = getId();
    return (id != null) ? id.toString() : null;
  }

  @Override
  public String toString() {
    return title;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;

    if (obj instanceof PmOptionImpl) {
      return ObjectUtils.equals(((PmOptionImpl)obj).id, id);
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return (id != null) ? id.hashCode() : 1;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

}
