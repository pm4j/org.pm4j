package org.pm4j.common.query;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.common.util.reflection.ClassUtil;


/**
 * A filter condition that compares an attribute and a value using a
 * defined {@link CompOp}.
 * <p>
 * Limitation: It currently can't be used as a hash map key. Because it is mutable.
 *
 * @author olaf boede
 */
public class QueryExprCompare implements QueryExpr, Cloneable, Serializable {

  private static final long serialVersionUID = 1L;
  private QueryAttr attr;
  private CompOp compOp;
  private Object value;

  /**
   * Creates a compare filter.
   * <p>
   * This is a compact constructor version taking just the class of the compare
   * operator as a parameter.
   * <p>
   * If you need a stateful compare operator (e.g. with an uppercase value
   * specification) use the constructor taking a {@link CompOp} instance parameter:
   * {@link #FilterCompare(QueryAttr, CompOp, Object)}.
   *
   * @param attr the attribute to compare.
   * @param compOpType the type of compare operator to apply.
   * @param value the value to compare to.
   */
  public QueryExprCompare(QueryAttr attr, Class<? extends CompOp> compOpType, Object value) {
    this(attr, (CompOp)ClassUtil.newInstance(compOpType), value);
  }

  /**
   * Creates a compare filter.
   *
   * @param attr the attribute to compare.
   * @param compOp the compare operator to apply.
   * @param value the value to compare to.
   */
  public QueryExprCompare(QueryAttr attr, CompOp compOp, Object value) {
    this.attr = attr;
    this.compOp = compOp;
    this.value = value;
  }

  /**
   * A default constructor. Useful for some generic use cases.
   */
  public QueryExprCompare() {
  }

  @Override
  public QueryExprCompare clone() {
    try {
      return (QueryExprCompare) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }

  /**
   * A string that identifies the attribute this predicate is stated for.
   *
   * @return the unique identifier.
   */
  public QueryAttr getAttr() {
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

  public void setAttr(QueryAttr attr) {
    this.attr = attr;
  }

  public void setCompOp(CompOp compOp) {
    this.compOp = compOp;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return attr + " " + compOp + " " + value;
  }

  /**
   * This class has currently no parallel {@link #hashCode()} implentation because it is not immutable.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof QueryExprCompare)) {
      return false;
    }
    QueryExprCompare rhs = (QueryExprCompare) obj;
    return new EqualsBuilder().append(attr, rhs.attr).append(compOp, rhs.compOp).append(value, rhs.value).isEquals();
  }
}
