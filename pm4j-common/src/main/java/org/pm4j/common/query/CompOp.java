package org.pm4j.common.query;

import java.io.Serializable;
import java.util.Collection;

import org.pm4j.common.exception.CheckedExceptionWrapper;

/**
 * The interface for filter compare operator definitions.
 *
 * @author olaf boede
 */
public interface CompOp extends Cloneable, Serializable {

  enum ValueNeeded { REQUIRED, OPTIONAL, NO }

  /**
   * @return A unique compare operator name.
   */
  String getName();

  /**
   * @return The value-required definition.
   */
  ValueNeeded getValueNeeded();

  /**
   * For each compare operator a different set of selectable value options may apply.
   *
   * @return The set of selectable value options.
   */
  Collection<ValueOption> getValueOptions();

  /**
   * Adds a compare operator specific value option.
   *
   * @param valueOption the new value option.
   */
  void addValueOption(ValueOption valueOption);

  /**
   * Checks if a filter definition with the given value is a filter condition
   * that needs to be evaluated for each item to filter.
   *
   * @param filterValue
   *          The filter value to check.
   * @return <code>true</code> if a filter with the given values provides a real
   *         filter condition.
   */
  boolean isEffectiveFilterValue(Object filterValue);

  /**
   * Public clone method interface.
   *
   * @return a clone.
   */
  CompOp clone();

  /**
   * Value option data container.
   */
  public class ValueOption implements Cloneable {
    private final Object value;
    private String title;
    private String resKey;

    public ValueOption(Object value) {
      this.value = value;
    }

    @Override
    public ValueOption clone() {
      try {
        return (ValueOption)super.clone();
      } catch (CloneNotSupportedException e) {
        throw new CheckedExceptionWrapper(e);
      }
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getResKey() {
      return resKey;
    }

    public void setResKey(String resKey) {
      this.resKey = resKey;
    }

    public Object getValue() {
      return value;
    }

  }

}
