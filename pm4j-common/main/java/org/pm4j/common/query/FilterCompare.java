package org.pm4j.common.query;

import org.pm4j.common.exception.CheckedExceptionWrapper;


public class FilterCompare implements FilterExpression, Cloneable {

  private AttrDefinition attr;
  private CompOp compOp;
  private Object value;

  public FilterCompare() {
  }

  public FilterCompare(AttrDefinition attr, CompOp compOp, Object value) {
    this.attr = attr;
    this.compOp = compOp;
    this.value = value;
  }

  @Override
  public FilterCompare clone() {
    try {
      return (FilterCompare) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }

  /**
   * A string that identifies the attribute this predicate is stated for.
   *
   * @return the unique identifier.
   */
  public AttrDefinition getAttr() {
    return attr;
  }

  /**
   * @return the compare operator to apply.
   */
  public CompOp getCompOp() {
    return compOp;
  }

  /**
   * Many compare operators use a value to compare to.<br>
   * E.g. 'is greater than 3'.
   *
   * @return the optional value.
   */
  public Object getValue() {
    return value;
  }

  public void setAttr(AttrDefinition attr) {
    this.attr = attr;
  }

  public void setCompOp(CompOp compOp) {
    this.compOp = compOp;
  }

  public void setValue(Object value) {
    this.value = value;
  }

}
