package org.pm4j.common.query;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.pm4j.common.exception.CheckedExceptionWrapper;

/**
 * A basic compare operator implementation.
 * <p>
 * Limitation: It currently can't be used as a hash map key. Because it is mutable.
 *
 * @param <T_VALUE>
 *
 * @author olaf boede
 */
public abstract class CompOpBase<T_VALUE> implements CompOp {

  private static final long serialVersionUID = 1L;

  /** Relevant for string attribues only. */
  @Deprecated
  private boolean ignoreCase = false;
  /** Relevant for string attribues only. */
  @Deprecated
  private boolean ignoreSpaces = false;

  private String name;
  private ValueNeeded valueNeeded = ValueNeeded.OPTIONAL;

  public CompOpBase(String name) {
    this.name = name;
  }

  @SuppressWarnings("unchecked")
  @Override
  public CompOp clone() {
    try {
      return (CompOpBase<T_VALUE>) super.clone();
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

  /**
   * This class has currently no parallel {@link #hashCode()} implementation because it is not immutable.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }
    @SuppressWarnings("unchecked")
    CompOpBase<T_VALUE> rhs = (CompOpBase<T_VALUE>) obj;
    return new EqualsBuilder()
          .append(name, rhs.name)
          .append(ignoreCase, rhs.ignoreCase)
          .append(ignoreSpaces, rhs.ignoreSpaces)
          .append(valueNeeded, rhs.valueNeeded)
          .isEquals();
  }

  @Deprecated
  public boolean isIgnoreCase() {
    return ignoreCase;
  }

  @Deprecated
  public void setIgnoreCase(boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
  }

  @Deprecated
  public boolean isIgnoreSpaces() {
    return ignoreSpaces;
  }

  @Deprecated
  public void setIgnoreSpaces(boolean ignoreSpaces) {
    this.ignoreSpaces = ignoreSpaces;
  }

}
