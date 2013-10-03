package org.pm4j.core.joda.impl;

import java.text.ParseException;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.converter.MultiFormatParserBase;
import org.pm4j.core.pm.impl.converter.PmConverterBase;

/**
 * Converter for Joda LocalDate type capable to parse different input formats.
 * 
 * @author Harm Gnoyke
 * @since GLOBE 1.3
 * 
 */
public class PmConverterLocalDate extends PmConverterBase<LocalDate> {

    /**
     * Instance of this class.
     */
    public static final PmConverterLocalDate INSTANCE = new PmConverterLocalDate();

    private MultiFormatParserBase<LocalDate> multiFormatParser = new MultiFormatParserBase<LocalDate>() {

        /**
         * {@inheritDoc}
         */
        @Override
        protected LocalDate parseValue(String input, String format, Locale locale, PmAttr<?> pmAttr) throws ParseException {
            try {
                DateTimeFormatter fmt = DateTimeFormat.forPattern(format).withLocale(locale);
                return fmt.parseLocalDate(input);
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
    private PmConverterLocalDate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDate stringToValue(PmAttr<?> pmAttr, String input) {
        LocalDate returnDate = multiFormatParser.parseString(pmAttr, input);
        return returnDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String valueToString(PmAttr<?> pmAttr, LocalDate v) {
        String outputFormat = multiFormatParser.getOutputFormat(pmAttr);
        PmConversation conversation = pmAttr.getPmConversation();
        Locale locale = conversation.getPmLocale();
        DateTimeFormatter fmt = DateTimeFormat.forPattern(outputFormat).withLocale(locale);
        return fmt.print(v);
    }

}
