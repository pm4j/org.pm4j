package org.pm4j.common.converter.string;


/**
 * Converter for string values. Just passes the string through.
 *
 * @author Olaf Boede
 */
public class StringConverterString implements StringConverter<String> {

  public static final StringConverterString INSTANCE = new StringConverterString();

  @Override
  public String stringToValue(StringConverterCtxt ctxt, String s) {
    return s;
  }

  @Override
  public String valueToString(StringConverterCtxt ctxt, String v) {
    return v;
  }

  /**
   * Trims the received {@link String} in method {@link #stringToValue(PmAttr, String)}.
   */
  public static class Trimmed extends StringConverterString {
    public static final Trimmed INSTANCE = new Trimmed();

    @Override
    public String stringToValue(StringConverterCtxt ctxt, String s) {
      String trimmed = (s != null) ? s.trim() : null;
      return super.stringToValue(ctxt, trimmed);
    }
  }
}
