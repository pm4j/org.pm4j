package org.pm4j.core.pm.impl;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmAttrDouble;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.annotation.PmAttrDoubleCfg;
import org.pm4j.core.pm.impl.converter.PmConverterDouble;

public class PmAttrDoubleImpl extends PmAttrNumBase<Double> implements PmAttrDouble {

  public PmAttrDoubleImpl(PmElementBase pmParent) {
    super(pmParent);
  }

  // ======== Interface implementation ======== //

  public Double getMax() {
    return getOwnMetaData().maxValue;
  }

  public Double getMin() {
    return getOwnMetaData().minValue;
  }

  // ======== Value handling ======== //

  /**
   * The default format key {@link #RESKEY_DEFAULT_FORMAT_PATTERN} applies when no
   * special format is defined (either by resource key with postfix or
   * annotation).
   *
   * @see PmAttrBase#getFormatString()
   */
  @Override
  protected String getFormatDefaultResKey() {
    return RESKEY_DEFAULT_FORMAT_PATTERN;
  }

  @Override
  protected void validate(Double value) throws PmValidationException {
    super.validate(value);

    if (value != null) {
      double v = value.doubleValue();
      if (v < getMin().doubleValue()) {
        throw new PmValidationException(this, PmConstants.MSGKEY_VALIDATION_VALUE_TOO_LOW, getMin());
      }
      if (v > getMax().doubleValue()) {
        throw new PmValidationException(this, PmConstants.MSGKEY_VALIDATION_VALUE_TOO_HIGH, getMax());
      }
    }
  }

  // ======== meta data ======== //

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;

    PmAttrDoubleCfg annotation = AnnotationUtil.findAnnotation(this, PmAttrDoubleCfg.class);
    if (annotation != null) {
      double maxValue = myMetaData.maxValue = annotation.maxValue();
      double minValue = myMetaData.minValue = annotation.minValue();

      if (minValue > maxValue) {
        throw new PmRuntimeException(this, "minValue(" + minValue + ") > maxValue(" + maxValue + ")");
      }
    }

    myMetaData.setConverterDefault(PmConverterDouble.INSTANCE);
  }

  protected static class MetaData extends PmAttrBase.MetaData {
    private double maxValue = Double.MAX_VALUE;
    private double minValue = -Double.MAX_VALUE;
  }

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }

}
