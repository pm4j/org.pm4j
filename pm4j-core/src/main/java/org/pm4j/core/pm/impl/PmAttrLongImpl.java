package org.pm4j.core.pm.impl;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmAttrLong;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrLongCfg;
import org.pm4j.core.pm.impl.converter.PmConverterLong;

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

  // ======== meta data ======== //

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;

    myMetaData.setConverterDefault(PmConverterLong.INSTANCE);

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
    protected double getMaxValue() {
      return maxValue;
    }
  }

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }

}
