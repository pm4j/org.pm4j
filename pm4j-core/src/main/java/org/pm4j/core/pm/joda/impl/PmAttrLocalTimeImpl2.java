package org.pm4j.core.pm.joda.impl;

import org.joda.time.LocalTime;
import org.pm4j.common.converter.string.joda.LocalTimeStringConverter;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.AttrConverterCtxt;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.core.pm.joda.PmAttrLocalTime;

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

  /** Uses {@link PmAttrLocalTime#FORMAT_DEFAULT_RES_KEY}. */
  @Override
  protected String getFormatDefaultResKey() {
    return PmAttrLocalTime.FORMAT_DEFAULT_RES_KEY;
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
    return CompareUtil.compare(getValue(), ((PmAttrLocalTime) otherPm).getValue());
  }

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    /** Sets the default max length is the length of the time format pattern. */
    // TODO oboede: needs to be derived from the format.
    MetaData md = new MetaData(8);
    // Configure the default converters. Is done before <code>initMetaData</code> to allow
    // annotation based customization.
    md.setStringConverter(new LocalTimeStringConverter());
    return md;
  }

}
