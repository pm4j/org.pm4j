package org.pm4j.core.pm.impl;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.converter.string.StringConverterString;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrStringCfg;

/**
 * Implements a PM attribute for {@link String} values.
 *
 * @author olaf boede
 */
public class PmAttrStringImpl extends PmAttrBase<String, String> implements PmAttrString {

  public PmAttrStringImpl(PmObject pmParent) {
    super(pmParent);
  }

  // ======== Interface implementation ======== //

  @Override
  public boolean isMultiLine() {
    return getOwnMetaData().multiLine;
  }

  // ======== Value handling ======== //

  // TODO olaf: check if this can be removed soon. - The converter should do this job.
  /**
   * Adds trim functionality to the generic value interface.
   */
  @Override
  boolean setValueImpl(SetValueContainer<String> value) {
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

    if (myMetaData.getStringConverter() == null) {
      myMetaData.setStringConverter(myMetaData.trim
          ? StringConverterString.Trimmed.INSTANCE
          : StringConverterString.INSTANCE);
    }
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
