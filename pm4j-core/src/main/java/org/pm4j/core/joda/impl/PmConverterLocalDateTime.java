package org.pm4j.core.joda.impl;

import java.text.ParseException;
import java.util.Locale;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.converter.MultiFormatParserBase;
import org.pm4j.core.pm.impl.converter.PmConverterSerializeableBase;

/**
 * Converter for Joda LocalDateTime type capable to parse different input
 * formats. To be used only with {@link PmAttrLocalDateTimeOnDateTimeImpl}.
 *
 * The converter is not aware of time zones. It is only used to convert the
 * entered Strings to the correct Date. The interpretation of the time zone is
 * done in the {@link PmAttrLocalDateTimeOnDateTimeImpl}.
 *
 * @author Harm Gnoyke
 * @since GLOBE 1.3
 *
 */
public class PmConverterLocalDateTime extends PmConverterSerializeableBase<LocalDateTime> {
  /**
   * Instance of this class.
   */
  public static final PmConverterLocalDateTime INSTANCE = new PmConverterLocalDateTime();

  private MultiFormatParserBase<LocalDateTime> multiFormatParser = new MultiFormatParserBase<LocalDateTime>() {

    /**
     * {@inheritDoc}
     */
    @Override
    protected LocalDateTime parseValue(String input, String format, Locale locale, PmAttr<?> pmAttr)
        throws ParseException {
      try {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(format).withLocale(locale);
        return fmt.parseLocalDateTime(input);
      } catch (Exception e) {
        ParseException pe = new ParseException("Error parsing Date with Joda", 0);
        pe.initCause(e);
        throw pe;
      }
    }
  };

  /**
   * Private constructor to prevent initialization of this class.
   */
  private PmConverterLocalDateTime() {
  }

  /**
   * FIXME oboede: this should be the job of the converter. Needs to be checked.
   */
  @Override
  public LocalDateTime stringToValue(PmAttr<?> pmAttr, String input) {
    return multiFormatParser.parseString(pmAttr, input);
  }

  @Override
  public String valueToString(PmAttr<?> pmAttr, LocalDateTime v) {
    String outputFormat = multiFormatParser.getOutputFormat(pmAttr);
    PmConversation conversation = pmAttr.getPmConversation();
    Locale locale = conversation.getPmLocale();
    DateTimeFormatter fmt = DateTimeFormat.forPattern(outputFormat).withLocale(locale);
    return fmt.print(v);
  }
}
