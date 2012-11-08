package org.pm4j.core.pm.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmAttrBigDecimal;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrBigDecimalCfg;
import org.pm4j.core.pm.impl.converter.PmConverterBigDecimal;

public class PmAttrBigDecimalImpl extends PmAttrNumBase<BigDecimal> implements PmAttrBigDecimal {

  public PmAttrBigDecimalImpl(PmObject pmParent) {
    super(pmParent);
  }

  // ======== Interface implementation ======== //

  public BigDecimal getMax() {
    return getOwnMetaDataWithoutPmInitCall().getMaxValue();
  }

  public BigDecimal getMin() {
    return getOwnMetaDataWithoutPmInitCall().getMinValue();
  }

  // ======== Value handling ======== //


  @Override
  protected void validate(BigDecimal value) throws PmValidationException {
    super.validate(value);

    if (value != null) {
      if (getMin() != null) {
        if (getMin().compareTo(value) > 0) {
          throw new PmValidationException(this, PmConstants.MSGKEY_VALIDATION_VALUE_TOO_LOW, getMin());
        }
      }
      if (getMax() != null) {
        if (getMax().compareTo(value) < 0) {
          throw new PmValidationException(this, PmConstants.MSGKEY_VALIDATION_VALUE_TOO_HIGH, getMax());
        }
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
    myMetaData.setConverterDefault(PmConverterBigDecimal.INSTANCE);

    PmAttrBigDecimalCfg annotation = AnnotationUtil.findAnnotation(this, PmAttrBigDecimalCfg.class);
    if (annotation != null) {
      BigDecimal maxValue = new BigDecimal(annotation.maxValueString());
      myMetaData.maxValue = maxValue;
      BigDecimal minValue = myMetaData.minValue = new BigDecimal(annotation.minValueString());

      if (minValue.compareTo(maxValue) >= 1) {
        throw new PmRuntimeException(this, "minValue(" + minValue + ") > maxValue(" + maxValue + ")");
      }
      myMetaData.stringConversionRoundingMode = annotation.stringConversionRoundingMode();
    }
  }

  protected static class MetaData extends PmAttrBase.MetaData {

    public RoundingMode stringConversionRoundingMode;

    protected BigDecimal getMaxValue() {
      return maxValue;
    }
    
    public BigDecimal getMinValue() {
      return minValue;
    }
    private BigDecimal maxValue = null;
    private BigDecimal minValue = null;
    
    @Override
    protected int getMaxLenDefault() {
      BigDecimal value = getMaxValue();
      if(value != null) {
        return value.toString().length();
      }
      return 80;
    }
  }

  private final MetaData getOwnMetaDataWithoutPmInitCall() {
    return (MetaData) getPmMetaDataWithoutPmInitCall();
  }

  @Override
  public RoundingMode getStringConversionRoundingMode() {
    return getOwnMetaDataWithoutPmInitCall().stringConversionRoundingMode == null ? RoundingMode.UNNECESSARY : getOwnMetaDataWithoutPmInitCall().stringConversionRoundingMode;
  }
}
