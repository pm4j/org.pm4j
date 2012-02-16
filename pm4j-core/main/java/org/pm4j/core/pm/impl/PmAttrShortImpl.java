package org.pm4j.core.pm.impl;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmAttrShort;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrShortCfg;
import org.pm4j.core.pm.impl.converter.PmConverterShort;

/**
 * PM attribute implementation for {@link Short} values.
 *
 * @author olaf boede
 */
public class PmAttrShortImpl extends PmAttrNumBase<Short> implements PmAttrShort {

  public PmAttrShortImpl(PmObject pmParent) {
    super(pmParent);
  }

  // ======== Interface implementation ======== //

  public Short getMax() {
    return getOwnMetaDataWithoutPmInitCall().maxValue;
  }

  public Short getMin() {
    return getOwnMetaDataWithoutPmInitCall().minValue;
  }

  // ======== Value handling ======== //

  @Override
  protected void validate(Short value) throws PmValidationException {
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

  // ======== meta data ======== //

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;
    myMetaData.setConverterDefault(PmConverterShort.INSTANCE);

    PmAttrShortCfg annotation = AnnotationUtil.findAnnotation(this, PmAttrShortCfg.class);
    if (annotation != null) {
      short maxValue = myMetaData.maxValue = annotation.maxValue();
      short minValue = myMetaData.minValue = annotation.minValue();

      if (minValue > maxValue) {
        throw new PmRuntimeException(this, "minValue(" + minValue + ") > maxValue(" + maxValue + ")");
      }
    }
  }

  protected static class MetaData extends PmAttrNumBase.MetaData {
    private short maxValue = Short.MAX_VALUE;
    private short minValue = Short.MIN_VALUE;

    @Override
    protected double getMaxValue() {
      return maxValue;
    }
  }

  private final MetaData getOwnMetaDataWithoutPmInitCall() {
    return (MetaData) getPmMetaDataWithoutPmInitCall();
  }

}
