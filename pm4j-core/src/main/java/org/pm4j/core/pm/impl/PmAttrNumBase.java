package org.pm4j.core.pm.impl;

import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.pm.PmAttrNumber;
import org.pm4j.core.pm.PmObject;

/**
 * Basic implementation for numeric attributes.
 *
 * @param <T> the numeric value type.
 *
 * @author olaf boede
 */
public abstract class PmAttrNumBase<T extends Number> extends PmAttrBase<T, T> implements PmAttrNumber<T> {

  public PmAttrNumBase(PmObject pmParent) {
    super(pmParent);
  }

  // ======== Interface implementation ======== //

  @SuppressWarnings("unchecked")
  @Override
  public int compareTo(PmObject otherPm) {
    return PmUtil.getAbsoluteName(this).equals(PmUtil.getAbsoluteName(otherPm))
      ? CompareUtil.compare(
              (Comparable<T>)getValue(),
              (Comparable<T>)((PmAttrNumber<T>)otherPm).getValue())
      : super.compareTo(otherPm);
  }

  // ======== Value handling ======== //

  protected abstract static class MetaData extends PmAttrBase.MetaData {
    public MetaData() {
      // the max length needs to be evaluated dynamically by calling getMaxLenDefault().
      super(-1);
    }

    /**
     * @return The configured maximum value limit.
     */
    protected abstract double getMaxValue();

    /**
     * The default implementation calculates the number of digits required for
     * the maximal value as provided by {@link #getMaxValue()}.
     */
    protected int getMaxLenDefault() {
      return new Double(Math.ceil(Math.log10(getMaxValue()))).intValue();
    }
  }

}
