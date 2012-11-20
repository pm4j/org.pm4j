package org.pm4j.core.pm.impl;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrStringCfg;
import org.pm4j.core.pm.impl.converter.PmConverterString;

public class PmAttrStringImpl extends PmAttrBase<String, String> implements PmAttrString {

  public PmAttrStringImpl(PmObject pmParent) {
    super(pmParent);
  }

  // ======== Interface implementation ======== //

  @Override
  public boolean isMultiLine() {
    return getOwnMetaData().multiLine;
  }

  @Override
  protected void validate(String value) throws PmValidationException {
    super.validate(value);

    if (value != null) {
      if (value.length() < getMinLen()) {
        throw new PmValidationException(this, PmConstants.MSGKEY_VALIDATION_VALUE_TOO_SHORT, getMinLen());
      }
      if (value.length() > getMaxLen()) {
        throw new PmValidationException(this, PmConstants.MSGKEY_VALIDATION_VALUE_TOO_LONG, getMaxLen());
      }
    }
  }

  // ======== Value handling ======== //

  // TODO olaf: check if this can be removed soon. - The converter should do this job.
  /**
   * Adds trim functionality to the generic value interface.
   */
  @Override
  protected boolean setValueImpl(SetValueContainer<String> value) {
    // XXX olaf: move to converter method? - might be the better place...
    if (getOwnMetaData().trim) {
      if (value.isStringValueSet()) {
        value.setStringValue(StringUtils.trim(value.getStringValue()));
      }
      else if (value.isPmValueSet())  {
        value.setPmValue(StringUtils.trim(value.getPmValue()));
      }
    }

    return super.setValueImpl(value);
  }

  @Override
  protected boolean isEmptyValue(String value) {
    return StringUtils.isEmpty(value);
  }

  // ======== meta data ======== //

  @Override
  protected PmAttrBase.MetaData makeMetaData() {
    return new MetaData();
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;

    PmAttrStringCfg annotation = AnnotationUtil.findAnnotation(this, PmAttrStringCfg.class);
    if (annotation != null) {
      myMetaData.multiLine = annotation.multiLine();
      myMetaData.trim = annotation.trim();
    }

    myMetaData.setConverterDefault(myMetaData.trim
          ? PmConverterString.Trimmed.INSTANCE
          : PmConverterString.INSTANCE);
  }

  protected static class MetaData extends PmAttrBase.MetaData {
    private boolean multiLine = false;
    private boolean trim = true;

    public MetaData() {
      super(PmAttrStringCfg.DEFAULT_MAXLEN);
    }
  }

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }

}
