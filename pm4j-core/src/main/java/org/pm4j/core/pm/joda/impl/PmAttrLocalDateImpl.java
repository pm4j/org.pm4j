package org.pm4j.core.pm.joda.impl;

import org.joda.time.LocalDate;
import org.pm4j.common.converter.string.joda.LocalDateStringConverter;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.AttrConverterCtxt;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.core.pm.joda.PmAttrLocalDate;

/**
 * PM attribute for a {@link LocalDate}.
 *
 * This field can handle multiple input formats to be defined ;-separated in the
 * resources with key suffix "_format" appended to the fields resource key.
 *
 * @author Harm Gnoyke
 * @author Olaf Boede
 */
public class PmAttrLocalDateImpl
  extends PmAttrBase<LocalDate, LocalDate>
  implements PmAttrLocalDate {

  /**
   * @param pmParent
   *          The PM parent.
   */
  public PmAttrLocalDateImpl(PmObject pmParent) {
    super(pmParent);
  }

  /** Provides {@link PmAttrLocalDate.FORMAT_DEFAULT_RES_KEY}. */
  @Override
  protected String getFormatDefaultResKey() {
    return PmAttrLocalDate.FORMAT_DEFAULT_RES_KEY;
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
    return CompareUtil.compare(getValue(), ((PmAttrLocalDate) otherPm).getValue());
  }

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    /** Sets the default max length is the length of the date format pattern. */
    MetaData md = new MetaData(MAX_LENGTH);
    // Configure the default converters. Is done before <code>initMetaData</code> to allow
    // annotation based customization.
    md.setStringConverter(new LocalDateStringConverter());
    return md;
  }

}
