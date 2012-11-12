package org.pm4j.common.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.core.util.lang.CloneUtil;

public abstract class CompOpBase<T_VALUE> implements CompOp {

  private boolean ignoreCase = true;
  private boolean ignoreSpaces = true;
  private String name;
  private ValueNeeded valueNeeded = ValueNeeded.OPTIONAL;
  private List<ValueOption> valueOptions = new ArrayList<ValueOption>();

  public CompOpBase(String name) {
    this.name = name;
  }

  @SuppressWarnings("unchecked")
  @Override
  public CompOp clone() {
    try {
      CompOpBase<T_VALUE> clone = (CompOpBase<T_VALUE>) super.clone();
      clone.valueOptions = CloneUtil.cloneList(this.valueOptions);

      return clone;
    } catch (CloneNotSupportedException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ValueNeeded getValueNeeded() {
    return valueNeeded;
  }

  protected void setValueNeeded(ValueNeeded valueNeeded) {
    this.valueNeeded = valueNeeded;
  }

  @Override
  public Collection<ValueOption> getValueOptions() {
    return valueOptions;
  }

  public void addValueOption(ValueOption valueOption) {
    valueOptions.add(valueOption);
  }

  @SuppressWarnings("unchecked")
  @Override
  public final boolean isEffectiveFilterValue(Object filterValue) {
    return isEffectiveFilterValueImpl((T_VALUE)filterValue);
  }

  /**
   * Concrete {@link CompOp} classes define here if this compare operator with
   * given compare value leads to a complete filter definition that can be
   * applied.
   *
   * @param filterValue
   *          the filter compare value to check.
   * @return <code>true</code> if the combination of this operator with the
   *         given value leads to a valid filter definition.
   */
  protected abstract boolean isEffectiveFilterValueImpl(T_VALUE filterValue);

  public boolean isIgnoreCase() {
    return ignoreCase;
  }

  public void setIgnoreCase(boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
  }

  public boolean isIgnoreSpaces() {
    return ignoreSpaces;
  }

  public void setIgnoreSpaces(boolean ignoreSpaces) {
    this.ignoreSpaces = ignoreSpaces;
  }

}
