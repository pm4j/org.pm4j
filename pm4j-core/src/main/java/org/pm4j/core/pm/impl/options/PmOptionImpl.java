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

  private Object value;

  private Object backingValue;

  /** Option enabled flag. */
  private boolean enabled = true;

  public PmOptionImpl(Serializable id) {
    this(id, ObjectUtils.toString(id), id);
  }

  public PmOptionImpl(Serializable id, String title) {
    this(id, title, id);
  }

  public PmOptionImpl(Serializable id, String title, Object backingValue) {
    this(id, title, backingValue, null);
  }

  /**
   * @param id
   *          The option id. (Will be used as option value's too. See above.)
   * @param title
   *          The option title.
   * @param backingValue
   *          The backing attribute value that corresponds to this option. May be <code>null</code>.
   * @param value
   *          The attribute value that corresponds to this option. May be <code>null</code>.
   */
  public PmOptionImpl(Serializable id, String title, Object backingValue, Object value) {
    this.id = id;
    this.title = title;
    this.backingValue = backingValue;
    this.value = value;
  }

  public Serializable getId() {
    return id;
  }

  public String getPmTitle() {
    return title;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue() {
    return (T) value;
  }

  @SuppressWarnings("unchecked")
  public <T> T getBackingValue() {
    return (T) backingValue;
  }

  /**
   * @param backingValue the backingValue to set
   */
  public void setBackingValue(Object backingValue) {
    this.backingValue = backingValue;
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
