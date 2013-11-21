package org.pm4j.core.pm.impl;

import java.util.Date;

import org.pm4j.common.converter.string.StringConverter;
import org.pm4j.common.converter.string.StringConverterDate;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.pm.PmAttrDate;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmLocalizeApi;

/**
 * Implements a PM attribute for {@link Date} values.
 *
 * @author olaf boede
 */
public class PmAttrDateImpl extends PmAttrBase<Date, Date> implements PmAttrDate {

  public PmAttrDateImpl(PmObject pmParent) {
    super(pmParent);
  }

  /**
   * The default format key {@link #RESKEY_DEFAULT_FORMAT_PATTERN} applies when no
   * special format is defined (either by resource key with postfix or
   * annotation).
   *
   * @see PmAttrBase#getFormatString()
   */
  @Override
  protected String getFormatDefaultResKey() {
    return RESKEY_DEFAULT_FORMAT_PATTERN;
  }

  @Override
  protected AttrConverterCtxt makeConverterCtxt() {
    return new AttrConverterCtxt.UsingFormats(this);
  }

  @Override
  public String getOutputFormat() {
    return PmLocalizeApi.getOutputFormatString(this);
  }

  /** @deprecated Compare operations on PMs are no longer supported. This is done on bean level now. */
  @Deprecated
  @Override
  public int compareTo(PmObject otherPm) {
    return PmUtil.getAbsoluteName(this).equals(PmUtil.getAbsoluteName(otherPm))
              ? CompareUtil.compare(getValue(), ((PmAttrDate)otherPm).getValue())
              : super.compareTo(otherPm);
  }

  @Override
  protected StringConverter<Date> getStringConverterImpl() {
    return StringConverterDate.INSTANCE;
  }

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    // The default max length is the length of the date format pattern.
    return new MetaData(20);
  }

}
