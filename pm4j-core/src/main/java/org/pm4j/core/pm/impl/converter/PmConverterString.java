package org.pm4j.core.pm.impl.converter;

import org.pm4j.core.pm.PmAttr;

/**
 * Converter for string values. Just passes the string through.
 * 
 * @author olaf boede
 */
public class PmConverterString extends PmConverterBase<String> {

  public static final PmConverterString INSTANCE = new PmConverterString();

  @Override
  public String stringToValue(PmAttr<?> pmAttr, String s) {
    return s;
  }

  /**
   * Trims the received {@link String} in methode {@link #stringToValue(PmAttr, String)}. 
   */
  public static class Trimmed extends PmConverterString {
    public static final Trimmed INSTANCE = new Trimmed();

    @Override
    public String stringToValue(PmAttr<?> pmAttr, String s) {
      return super.stringToValue(pmAttr, s.trim());
    }
  }
}
