package org.pm4j.core.pm.impl;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.pm.PmAttrNumber;
import org.pm4j.core.pm.PmObject;

public abstract class PmAttrNumBase<T extends Number> extends PmAttrBase<T, T> implements PmAttrNumber<T> {

  public PmAttrNumBase(PmObject pmParent) {
    super(pmParent);
  }

  // ======== Interface implementation ======== //

  public int getMaxLen() {
    // TODO olaf: cache that info in meta data
    return new Double(Math.ceil(Math.log10(getMax().longValue()))).intValue();
  }

  @SuppressWarnings("unchecked")
  @Override
  public int compareTo(PmObject otherPm) {
    return PmUtil.getAbsoluteName(this).equals(PmUtil.getAbsoluteName(otherPm))
      ? CompareUtil.compare(
              (Comparable<T>)getValue(),
              (Comparable<T>)((PmAttrNumber<T>)otherPm).getValue())
      : super.compareTo(otherPm);
  }

  // ======== Value handling ======== //

  /**
   * @param formatString
   *          The (language specific) format string. May be empty or null.
   * @return The associated number format.<br>
   *         In case of an empty or <code>null</code> formatString, a
   *         {@link DecimalFormat} for the current PM locale will be returned.
   */
  protected NumberFormat getNumberFormat(String formatString) {
    Locale locale = getPmConversation().getPmLocale();
    return (StringUtils.isBlank(formatString))
        ? NumberFormat.getNumberInstance(locale)
        : new DecimalFormat(formatString, new DecimalFormatSymbols(locale));
  }

}
