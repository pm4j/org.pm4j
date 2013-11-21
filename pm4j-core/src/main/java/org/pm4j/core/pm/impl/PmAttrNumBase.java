package org.pm4j.core.pm.impl;

import java.math.RoundingMode;

import org.pm4j.common.converter.string.StringConverterParseException;
import org.pm4j.common.converter.value.ValueConverterCtxtNumber;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.pm.PmAttrNumber;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmLocalizeApi;

/**
 * Basic implementation for numeric attributes.
 *
 * @param <T> the numeric value type.
 *
 * @author olaf boede
 */
public abstract class PmAttrNumBase<T extends Number>
    extends PmAttrBase<T, T>
    implements PmAttrNumber<T> {

  public PmAttrNumBase(PmObject pmParent) {
    super(pmParent);
  }

  @Override
  protected AttrConverterCtxt makeConverterCtxt() {
    return new NumConverterCtxt(this);
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

  /** Provides context information for string and value converter operations. */
  protected class NumConverterCtxt extends AttrConverterCtxt implements ValueConverterCtxtNumber {
    public NumConverterCtxt(PmAttrBase<?, ?> pmAttr) {
      super(pmAttr);
    }

    @Override
    public RoundingMode getConverterCtxtRoundingMode() {
      return getOwnMetaDataWithoutPmInitCall().roundingMode;
    }

    /** Generates a numeric type specific message. */
    @Override
    public StringConverterParseException createStringConverterParseException(String valueToConvert, Throwable exception, String... formats) {
      String msg = PmLocalizeApi.localize(getPmAttr(), PmConstants.MSGKEY_VALIDATION_NUMBER_CONVERSION_FROM_STRING_FAILED, valueToConvert);
      return new StringConverterParseException(msg, this, valueToConvert, formats);
    }
  }

  // ======== Meta data ======== //

  protected abstract static class MetaData extends PmAttrBase.MetaData {

    private RoundingMode roundingMode = ROUNDINGMODE_DEFAULT;


    public MetaData() {
      // the max length needs to be evaluated dynamically by calling getMaxLenDefault().
      super(-1);
    }

    /**
     * @return The configured maximum value limit.
     */
    protected abstract double getMaxValueAsDouble();

    /**
     * The default implementation calculates the number of digits required for
     * the maximal value as provided by {@link #getMaxValueAsDouble()}.
     */
    protected int getMaxLenDefault() {
      // TODO there is already a task defined fixing this implementation.
      return new Double(Math.ceil(Math.log10(getMaxValueAsDouble()))).intValue();
    }

    public void setRoundingMode(RoundingMode roundingMode) { this.roundingMode = roundingMode; }
  }

  private final MetaData getOwnMetaDataWithoutPmInitCall() {
    return (MetaData) getPmMetaDataWithoutPmInitCall();
  }

}
