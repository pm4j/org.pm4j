package org.pm4j.core.pm.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.converter.string.StringConverterBigDecimal;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmAttrBigDecimal;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrBigDecimalCfg;

/**
 * Implements a PM attribute for {@link BigDecimal} values.
 *
 * @author olaf boede
 */
public class PmAttrBigDecimalImpl extends PmAttrNumBase<BigDecimal> implements PmAttrBigDecimal {

  public static final int MAX_LENGTH_DEFAULT = 80;

  private static final Log LOG = LogFactory.getLog(PmAttrBigDecimalImpl.class);

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
    MetaData md = new MetaData();
    md.setStringConverter(StringConverterBigDecimal.INSTANCE);
    return md;
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    ((MetaData) metaData).readBigDecimalAnnotation(this);
  }

  protected static class MetaData extends PmAttrNumBase.MetaData {

    private BigDecimal maxValue = null;
    private BigDecimal minValue = null;

    /**
     * The default implementation reads the {@link PmAttrBigDecimalCfg} from
     * the field instance or one of the attribute base classes.
     *
     * @return the found annotation or <code>null</code>.
     */
    protected PmAttrBigDecimalCfg findBigDecimalAnnotation(PmAttrBigDecimalImpl pm) {
      return AnnotationUtil.findAnnotation(pm, PmAttrBigDecimalCfg.class);
    };

    @Override
    protected int getMaxLenDefault() {
      BigDecimal value = getMaxValue();
      if(value != null) {
        return value.toString().length();
      }
      return MAX_LENGTH_DEFAULT;
    }

    public BigDecimal getMaxValue() {  return maxValue;  }
    public BigDecimal getMinValue() { return minValue; }
    public void setMaxValue(BigDecimal maxValue) { this.maxValue = maxValue; }
    public void setMinValue(BigDecimal minValue) { this.minValue = minValue; }

    @Override
    protected double getMaxValueAsDouble() { throw new RuntimeException("Not applicable for BigDecimal."); }

    private void readBigDecimalAnnotation(PmAttrBigDecimalImpl pm) {
      PmAttrBigDecimalCfg annotation = findBigDecimalAnnotation(pm);
      if (annotation != null) {
        BigDecimal maxValue = pm.getMaxValue(annotation);
        BigDecimal minValue = pm.getMinValue(annotation);

        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) >= 1) {
          throw new PmRuntimeException(pm, "minValue(" + minValue + ") > maxValue(" + maxValue + ")");
        }
        setRoundingMode(pm.getRoundingMode(annotation));
      }
    }
  }

  private final MetaData getOwnMetaDataWithoutPmInitCall() {
    return (MetaData) getPmMetaDataWithoutPmInitCall();
  }

  /**
   * Null safe implementation
   * @param number the number
   * @return a BigDecimal
   */
  private BigDecimal convert(String number) {
    BigDecimal bd = null;
    if(!StringUtils.isBlank(number)) {
      DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
      decimalFormat.setParseBigDecimal(true);
      try {
        bd = (BigDecimal) decimalFormat.parse(number);
      } catch (ParseException e) {
        if (LOG.isTraceEnabled()) {
          LOG.trace("Error while parsing BigDecimal number: \"" + number +"\"", e);
        }
      }
    }
    return bd;
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

}
