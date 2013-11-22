package org.pm4j.core.pm.impl;

import java.util.Locale;
import java.util.TimeZone;

import org.pm4j.common.converter.string.StringConverterCtxt;
import org.pm4j.common.converter.string.StringConverterParseException;
import org.pm4j.common.converter.string.StringConverterUtil;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.api.PmLocalizeApi;

/**
 * Provides context information for string and value converter operations.
 */
public class AttrConverterCtxt implements StringConverterCtxt {
  private final PmAttrBase<?, ?> pmAttr;

  public AttrConverterCtxt(PmAttrBase<?, ?> pmAttr) {
    assert pmAttr != null;
    this.pmAttr = pmAttr;
  }

  /** @return The related attribute PM. */
  public PmAttr<?> getPmAttr() {
    return pmAttr;
  }

  @Override
  public TimeZone getConverterCtxtTimeZone() {
    return pmAttr.getPmTimeZoneImpl();
  }

  @Override
  public Locale getConverterCtxtLocale() {
    return pmAttr.getPmConversation().getPmLocale();
  }

  @Override
  public String getConverterCtxtFormatString() {
    return pmAttr.getFormatString();
  }

  @Override
  public StringConverterParseException createStringConverterParseException(String valueToConvert, Throwable exception, String... formats) {
    String msg = PmLocalizeApi.localize(pmAttr, PmConstants.MSGKEY_VALIDATION_CONVERSION_FROM_STRING_FAILED, valueToConvert);
    return new StringConverterParseException(msg, this, valueToConvert, formats);
  }

  @Override
  public String toString() {
    return pmAttr.toString();
  }

  /**
   * Support types like date/time/number that have format patterns.
   * Generates a special format error message.
   */
  public static class UsingFormats extends AttrConverterCtxt {
    public UsingFormats(PmAttrBase<?, ?> pmAttr) {
      super(pmAttr);
    }

    /** Generates a date type specific message. */
    @Override
    public StringConverterParseException createStringConverterParseException(String valueToConvert, Throwable exception, String... formats) {
      String msg = PmLocalizeApi.localize(getPmAttr(), PmConstants.MSGKEY_VALIDATION_FORMAT_FAILURE, getPmAttr().getPmTitle(), StringConverterUtil.getOutputFormat(this));
      return new StringConverterParseException(msg, this, valueToConvert, formats);
    }
  }
}