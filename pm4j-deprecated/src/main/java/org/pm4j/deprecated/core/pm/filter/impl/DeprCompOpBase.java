package org.pm4j.deprecated.core.pm.filter.impl;

import java.util.Collection;
import java.util.Collections;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.deprecated.core.pm.filter.DeprCompOp;

@Deprecated
public abstract class DeprCompOpBase<T_VALUE> implements DeprCompOp {

  private String name;
  private String title;
  private ValueNeeded valueNeeded = ValueNeeded.OPTIONAL;

  public DeprCompOpBase(String name, String title) {
    this.name = name;
    this.title = title;
  }

  public DeprCompOpBase(PmObject pmCtxt, String name, String resKey) {
    this(name, PmLocalizeApi.localize(pmCtxt, resKey));
  }


  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public ValueNeeded getValueNeeded() {
    return valueNeeded;
  }

  protected void setValueNeeded(ValueNeeded valueNeeded) {
    this.valueNeeded = valueNeeded;
  }

  @Override
  public Collection<?> getValueOptions() {
    Collection<T_VALUE> typeSaveOptions = getValueOptionsImpl();
    return typeSaveOptions != null
        ? (Collection<?>)typeSaveOptions
        : Collections.EMPTY_LIST;
  }

  /**
   * Type safe method for the option values to provide.
   * <p>
   * The default implementation provides no options.
   *
   * @return The option values.<br>
   *         May return <code>null</code> for no options.
   */
  protected Collection<T_VALUE> getValueOptionsImpl() {
    return null;
  }


  @SuppressWarnings("unchecked")
  @Override
  public final boolean doesValueMatch(Object itemValue, Object filterValue) {
    return doesValueMatchImpl((T_VALUE)itemValue, (T_VALUE)filterValue);
  }

  protected abstract boolean doesValueMatchImpl(T_VALUE itemValue, T_VALUE filterValue);

  @SuppressWarnings("unchecked")
  @Override
  public final boolean isEffectiveFilterValue(Object filterValue) {
    return isEffectiveFilterValueImpl((T_VALUE)filterValue);
  }

  protected abstract boolean isEffectiveFilterValueImpl(T_VALUE filterValue);

}
