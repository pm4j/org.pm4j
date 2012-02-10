package org.pm4j.core.pm.impl;

import java.math.BigDecimal;

import org.pm4j.core.pm.PmAttrBigDecimal;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.converter.PmConverterBigDecimal;

public class PmAttrBigDecimalImpl extends PmAttrNumBase<BigDecimal> implements PmAttrBigDecimal {

  public PmAttrBigDecimalImpl(PmObject pmParent) {
    super(pmParent);
  }

  // ======== Interface implementation ======== //

  public BigDecimal getMax() {
    return new BigDecimal(Double.MAX_VALUE);
  }

  public BigDecimal getMin() {
    return new BigDecimal(Double.MIN_VALUE);
  }

  // ======== Value handling ======== //

/*
  @Override
  protected void validate(BigDecimal value) throws PmValidationException {
    super.validate(value);

    if (value != null) {
      int v = value.intValue();
      if (v < getMin().longValue()) {
        throw new PmValidationException(this, PmConstants.MSGKEY_VALIDATION_VALUE_TOO_LOW, getMin());
      }
      if (v > getMax().longValue()) {
        throw new PmValidationException(this, PmConstants.MSGKEY_VALIDATION_VALUE_TOO_HIGH, getMax());
      }
    }
  }
*/

  // ======== meta data ======== //

// TODO olaf:

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;
    myMetaData.setConverterDefault(PmConverterBigDecimal.INSTANCE);

/*
    PmAttrBigDecimalCfg annotation = AnnotationUtil.findAnnotation(this, PmAttrBigDecimalCfg.class);
    if (annotation != null) {
      int maxValue = (annotation.maxValue() == Double.MAX_VALUE)
          myMetaData.maxValue = annotation.maxValue();
      int minValue = myMetaData.minValue = annotation.minValue();

      if (minValue > maxValue) {
        throw new PmRuntimeException(this, "minValue(" + minValue + ") > maxValue(" + maxValue + ")");
      }
    }
*/
  }

/*
  protected static class MetaData extends PmAttrBase.MetaData {
    private BigDecimal maxValue = null;
    private BigDecimal minValue = null;
  }

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }
*/
}
