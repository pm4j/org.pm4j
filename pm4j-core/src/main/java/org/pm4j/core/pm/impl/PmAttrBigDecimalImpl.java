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

/**
 * Implements a PM attribute for {@link BigDecimal} values.
 *
 * @author olaf boede
 */
public class PmAttrBigDecimalImpl extends PmAttrNumBase<BigDecimal> implements PmAttrBigDecimal {

  public static final int MAX_LENGTH_DEFAULT = 80;
  
  public PmAttrBigDecimalImpl(PmObject pmParent) {
    super(pmParent);
  }

  // ======== Interface implementation ======== //

  public BigDecimal getMaxValue() {
    return getOwnMetaDataWithoutPmInitCall().getMaxValue();
  }

  public BigDecimal getMinValue() {
    return getOwnMetaDataWithoutPmInitCall().getMinValue();
  }

  // ======== Value handling ======== //


  @Override
  protected void validate(BigDecimal value) throws PmValidationException {
    super.validate(value);

    if (value != null) {
      if (getMinValue() != null) {
        if (getMinValue().compareTo(value) > 0) {
          throw new PmValidationException(this, PmConstants.MSGKEY_VALIDATION_VALUE_TOO_LOW, getMinValue());
        }
      }
      if (getMaxValue() != null) {
        if (getMaxValue().compareTo(value) < 0) {
          throw new PmValidationException(this, PmConstants.MSGKEY_VALIDATION_VALUE_TOO_HIGH, getMaxValue());
        }
      }

    }
  }

  @Override
  protected String getFormatDefaultResKey() {
    return RESKEY_DEFAULT_FLOAT_FORMAT_PATTERN;
  }
  
  // ======== meta data ======== //

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

  /**
   * Null safe implementation
   * @param number the number
   * @return a BigDecimal
   */
  private BigDecimal convert(String number) {
    boolean isEmpty =  number == null || "".equals(number);
    return isEmpty ? null : new BigDecimal(number);
  }
  
  /**
   * Just to consider deprecated annotation attributes.
   * @param annotation the annotation.
   * @return the minimum value.
   */
  @SuppressWarnings("deprecation")
  private BigDecimal getMinValue(PmAttrBigDecimalCfg annotation) {
    BigDecimal value = convert(annotation.minValue());
    if(value == null) {
      // Support deprecated attribute.
      value = convert(annotation.minValueString());
    }
    return value;
  }

  /**
   * Just to consider deprecated annotation attributes.
   * @param annotation the annotation.
   * @return the maximum value.
   */
  @SuppressWarnings("deprecation")
  private BigDecimal getMaxValue(PmAttrBigDecimalCfg annotation) {
    BigDecimal value = convert(annotation.maxValue());
    if(value == null) {
      // Support deprecated attribute.
      value = convert(annotation.maxValueString());
    }
    return value;
  }

  /**
   * Just to consider deprecated annotation attributes.
   * @param annotation the annotation.
   * @return the rounding mode.
   */
  @SuppressWarnings("deprecation")
  private RoundingMode getRoundingMode(PmAttrBigDecimalCfg annotation) {
    RoundingMode rm = annotation.roundingMode();
    if(rm == ROUNDINGMODE_DEFAULT) {
      rm = annotation.stringConversionRoundingMode();
    }
    return rm;
  }
  
  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;
    myMetaData.setConverterDefault(PmConverterBigDecimal.INSTANCE);

    PmAttrBigDecimalCfg annotation = AnnotationUtil.findAnnotation(this, PmAttrBigDecimalCfg.class);
    if (annotation != null) {
      BigDecimal maxValue = myMetaData.maxValue = getMaxValue(annotation);
      BigDecimal minValue = myMetaData.minValue = getMinValue(annotation);

      if (minValue != null && maxValue != null && minValue.compareTo(maxValue) >= 1) {
        throw new PmRuntimeException(this, "minValue(" + minValue + ") > maxValue(" + maxValue + ")");
      }
      myMetaData.roundingMode = getRoundingMode(annotation);
    }
  }

  protected static class MetaData extends PmAttrBase.MetaData {

    private BigDecimal maxValue = null;
    private BigDecimal minValue = null;
    public RoundingMode roundingMode = ROUNDINGMODE_DEFAULT;

    public MetaData() {
      // the max length needs to be evaluated dynamically by calling getMaxLenDefault().
      super(-1);
    }

    protected BigDecimal getMaxValue() {
      return maxValue;
    }

    public BigDecimal getMinValue() {
      return minValue;
    }

    @Override
    protected int getMaxLenDefault() {
      BigDecimal value = getMaxValue();
      if(value != null) {
        return value.toString().length();
      }
      return MAX_LENGTH_DEFAULT;
    }
  }

  private final MetaData getOwnMetaDataWithoutPmInitCall() {
    return (MetaData) getPmMetaDataWithoutPmInitCall();
  }
  
  /**
   * @return rounding mode when converting to pm value. Changing this to a value
   *         different than RoundingMode.UNNECESSARY will allow to set more
   *         fraction digits than specified in the format. Those additional
   *         digits will then be rounded.
   */
  public RoundingMode getRoundingMode() {
    return getOwnMetaDataWithoutPmInitCall().roundingMode;
  }
}
