package org.pm4j.core.joda.impl;

import java.text.ParseException;
import java.util.Locale;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.converter.MultiFormatParserBase;
import org.pm4j.core.pm.impl.converter.PmConverterBase;

/**
 * Converter for Joda LocalTime type capable to parse different input
 * formats.
 * 
 * @author Olaf Kossak
 * @since 0.6.12
 * 
 */
public class PmConverterLocalTime
    extends PmConverterBase<LocalTime> {

  /**
   * Instance of this class.
   */
  public static final PmConverterLocalTime INSTANCE = new PmConverterLocalTime();

  private MultiFormatParserBase<LocalTime> multiFormatParser = new MultiFormatParserBase<LocalTime>() {

    /**
     * {@inheritDoc}
     */
    @Override
    protected LocalTime parseValue(String input, String format, Locale locale, PmAttr<?> pmAttr) throws ParseException {
      try {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(format).withLocale(locale);
        return fmt.parseLocalTime(input);
      } catch (Exception e) {
        ParseException pe = new ParseException("Error parsing Time with Joda", 0);
        pe.initCause(e);
        throw pe;
      }
    }
  };

  /**
   * Private constructor to prevent initialization of this class.
   */
  private PmConverterLocalTime() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LocalTime stringToValue(PmAttr<?> pmAttr, String input) {
    return multiFormatParser.parseString(pmAttr, input);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String valueToString(PmAttr<?> pmAttr, LocalTime v) {
    String outputFormat = multiFormatParser.getOutputFormat(pmAttr);
    PmConversation conversation = pmAttr.getPmConversation();
    Locale locale = conversation.getPmLocale();
    DateTimeFormatter fmt = DateTimeFormat.forPattern(outputFormat).withLocale(locale);
    return fmt.print(v);
  }

}
