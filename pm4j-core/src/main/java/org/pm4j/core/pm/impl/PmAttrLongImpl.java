package org.pm4j.core.pm.impl;

import org.pm4j.common.converter.string.StringConverterLong;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmAttrLong;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrLongCfg;

/**
 * Implements a PM attribute for {@link Long} values.
 *
 * @author olaf boede
 */
public class PmAttrLongImpl extends PmAttrNumBase<Long> implements PmAttrLong {

  public PmAttrLongImpl(PmObject pmParent) {
    super(pmParent);
  }

  // ======== Interface implementation ======== //

  public Long getMaxValue() {
    return getOwnMetaData().maxValue;
  }

  public Long getMinValue() {
    return getOwnMetaData().minValue;
  }

  // ======== Value handling ======== //

  @Override
  protected void validate(Long value) throws PmValidationException {
    super.validate(value);

    if (value != null) {
      long v = value.longValue();
      if (v < getMinValue().longValue()) {
        throw new PmValidationException(this, PmConstants.MSGKEY_VALIDATION_VALUE_TOO_LOW, getMinValue());
      }
      if (v > getMaxValue().longValue()) {
        throw new PmValidationException(this, PmConstants.MSGKEY_VALIDATION_VALUE_TOO_HIGH, getMaxValue());
      }
    }
  }

  @Override
  protected String getFormatDefaultResKey() {
    return RESKEY_DEFAULT_INTEGER_FORMAT_PATTERN;
  }

  // ======== meta data ======== //

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    MetaData md = new MetaData();
    md.setStringConverter(StringConverterLong.INSTANCE);
    return md;
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;

    PmAttrLongCfg annotation = AnnotationUtil.findAnnotation(this, PmAttrLongCfg.class);
    if (annotation != null) {
      long maxValue = myMetaData.maxValue = annotation.maxValue();
      long minValue = myMetaData.minValue = annotation.minValue();

      if (minValue > maxValue) {
        throw new PmRuntimeException(this, "minValue(" + minValue + ") > maxValue(" + maxValue + ")");
      }
    }
  }

  protected static class MetaData extends PmAttrNumBase.MetaData {
    private long maxValue = Long.MAX_VALUE;
    private long minValue = Long.MIN_VALUE;

    @Override
    protected double getMaxValueAsDouble() {
      return maxValue;
    }
  }

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }

}
