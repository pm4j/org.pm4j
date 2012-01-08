package org.pm4j.core.pm.impl;

import javax.validation.constraints.Size;
import javax.validation.metadata.ConstraintDescriptor;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmRuntimeException;
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
  public int getMaxLen() {
    return getOwnMetaData().maxLen;
  }

  @Override
  public int getMinLen() {
    return getOwnMetaData().minLen;
  }

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

    PmAttrStringCfg annotation = findAnnotation(PmAttrStringCfg.class);
    if (annotation != null) {
      if (annotation.maxLen() != PmAttrStringCfg.DEFAULT_MAXLEN) {
        myMetaData.maxLen = annotation.maxLen();
      }
      if (annotation.minLen() != 0) {
        myMetaData.minLen = annotation.minLen();
      }

      myMetaData.multiLine = annotation.multiLine();
      myMetaData.trim = annotation.trim();

      if (myMetaData.minLen > myMetaData.maxLen) {
        throw new PmRuntimeException(this, "minLen(" + myMetaData.minLen +
                                           ") > maxLen(" + myMetaData.maxLen + ")");
      }

      // FIXME olaf: What about the case of an optional field with a min-length in case of entered data?
      if (myMetaData.minLen > 0) {
        myMetaData.setRequired(true);
      }
    }

    myMetaData.setConverterDefault(myMetaData.trim
          ? PmConverterString.Trimmed.INSTANCE
          : PmConverterString.INSTANCE);
  }

  @Override
  protected void initMetaDataBeanConstraint(ConstraintDescriptor<?> cd) {
    super.initMetaDataBeanConstraint(cd);

    if (cd.getAnnotation() instanceof Size) {
      MetaData metaData = getOwnMetaData();
      Size annotation = (Size)cd.getAnnotation();

      if (annotation.min() > 0) {
        metaData.minLen = annotation.min();
      }
      if (annotation.max() < Integer.MAX_VALUE) {
        metaData.maxLen = annotation.max();
      }
    }
  }

  protected static class MetaData extends PmAttrBase.MetaData {
    private int maxLen = PmAttrStringCfg.DEFAULT_MAXLEN;
    private int minLen = 0;
    private boolean multiLine = false;
    private boolean trim = true;
  }

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }

}
