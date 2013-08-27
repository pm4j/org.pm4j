package org.pm4j.core.joda.impl;

import org.joda.time.LocalTime;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.joda.PmAttrLocalTime;
import org.pm4j.core.pm.PmAttrTime;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmObjectBase;

/**
 * PM attribute for a {@link LocalTime}.
 * 
 * This field can handle multiple input formats to be defined ;-separated in the
 * resources with key suffix "_format" appended to the fields resource key.
 * 
 * @author Olaf Kossak
 * @since 0.6.12
 */
public class PmAttrLocalTimeImpl2
    extends PmAttrBase<LocalTime, LocalTime>
    implements PmAttrLocalTime {

  /**
   * @param pmParent
   *          The PM parent.
   */
  public PmAttrLocalTimeImpl2(PmObject pmParent) {
    super(pmParent);
  }

  /**
   * Custom implementation to compare {@link LocalTime} objects
   */
  @Override
  public int compareTo(PmObject otherPm) {
    // TODO oboede: should have a default implementation for all attributes that
    // handles all
    // cases...
    return CompareUtil.compare(getValue(), ((PmAttrLocalTime) otherPm).getValue());
  }

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    /** Sets the default max length is the length of the time format pattern. */
    // TODO oboede: needs to be derived from the format.
    return new MetaData(8);
  }

  /** Adjusts the default converter. */
  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    ((PmAttrBase.MetaData) metaData).setConverterDefault(PmConverterLocalTime.INSTANCE);
  }

  /** Uses {@link PmAttrTime#RESKEY_DEFAULT_FORMAT_PATTERN}. */
  @Override
  protected String getFormatDefaultResKey() {
    return PmAttrTime.RESKEY_DEFAULT_FORMAT_PATTERN;
  }
}
