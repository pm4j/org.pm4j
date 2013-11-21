package org.pm4j.core.joda.impl;

import org.joda.time.LocalDateTime;
import org.pm4j.common.converter.string.joda.LocalDateTimeStringConverter;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.joda.PmAttrLocalDateTime;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.AttrConverterCtxt;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmObjectBase;

/**
 * Pm Attribute for a {@link LocalDateTime}.
 *
 * This field can handle multiple input formats to be defined ;-separated in the
 * resources with key suffix "_format" appended to the fields resource key.
 *
 * @author olaf boede
 */
public class PmAttrLocalDateTimeImpl
  extends PmAttrBase<LocalDateTime, LocalDateTime>
  implements PmAttrLocalDateTime {

  /**
   * @param pmParent
   *          The PM parent.
   */
  public PmAttrLocalDateTimeImpl(PmObject pmParent) {
    super(pmParent);
  }

  @Override
  protected String getFormatDefaultResKey() {
    return FORMAT_DEFAULT_RES_KEY;
  }

  /** Creates a converter context that provides a specific string parse error message. */
  @Override
  protected AttrConverterCtxt makeConverterCtxt() {
    return new AttrConverterCtxt.UsingFormats(this);
  }

  /** @deprecated Compare operations base on PMs are no longer supported. That can be done on bean level. */
  @Deprecated
  @Override
  public int compareTo(PmObject otherPm) {
    return CompareUtil.compare(getValue(), ((PmAttrLocalDateTime) otherPm).getValue());
  }

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    /** Sets the default max length is the length of the date format pattern. */
    MetaData md = new MetaData(11);
    // Configure the default converters. Is done before <code>initMetaData</code> to allow
    // annotation based customization.
    md.setStringConverter(new LocalDateTimeStringConverter());
    return md;
  }

}
