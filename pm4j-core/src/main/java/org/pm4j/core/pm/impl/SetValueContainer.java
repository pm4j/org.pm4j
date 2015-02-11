package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmAttr;

/**
 * For internal use only!
 * <p>
 * A value that is not yet or can't be applied to an attribute.
 * 
 * @author olaf boede
 *
 * @param <T_VALUE> The type used as external value interface of the attribute.
 */
public final class SetValueContainer<T_VALUE> {

  private boolean stringValueSet;

  private boolean pmValueSet;

  private String stringValue;

  private T_VALUE pmValue;

  private final PmAttr<?> pm;

  public SetValueContainer(PmAttr<?> pm) {
    this.pm = pm;
  }

  public SetValueContainer(PmAttr<?> pm, String stringValue) {
    this.pm = pm;
    setStringValue(stringValue);
  }

  public static <T_VALUE> SetValueContainer<T_VALUE> makeWithPmValue(PmAttr<?> pm, T_VALUE pmValue) {
    SetValueContainer<T_VALUE> v = new SetValueContainer<T_VALUE>(pm);
    v.setPmValue(pmValue);
    return v;
  }
  
  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
    this.stringValueSet = true;
  }

  public T_VALUE getPmValue() {
    return pmValue;
  }

  public void setPmValue(T_VALUE pmValue) {
    this.pmValue = pmValue;
    this.pmValueSet = true;
  }

  /**
   * @return The presentation model of the attribute with the invalid value.
   */
  public PmAttr<?> getPm() {
    return pm;
  }

  public boolean isStringValueSet() {
    return stringValueSet;
  }

  public boolean isPmValueSet() {
    return pmValueSet;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("valueContainer[");
    
    if (stringValueSet) {
      sb.append("asString: '").append(stringValue).append("'");
    }
    if (pmValueSet) {
      if (stringValueSet) {
        sb.append("; ");
      }
      sb.append("pmValue: '").append(pmValue).append("'");
    }
    sb.append("]");
    
    return sb.toString();
  }

}
