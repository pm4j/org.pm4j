package org.pm4j.core.pm.joda.impl;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.pm4j.common.converter.string.joda.LocalDateStringConverter;
import org.pm4j.common.converter.value.ValueConverter;
import org.pm4j.common.converter.value.ValueConverterCtxt;
import org.pm4j.common.converter.value.joda.JodaTimeZoneConverterBase;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.core.pm.joda.PmAttrLocalDate;

/**
 * An attribute that allows to enter a date in a specific time zone.<br>
 * It's backing value represents the given date and 00:00h in UTC.
 * <p>
 * The time zone is by default the time zone of the {@link PmConversation}. (The user time zone)
 * <br>
 * The time zone may be changed by overriding {@link #getConverterCtxtTimeZone()}.
 *
 * @author oboede
 */
public class PmAttrLocalDateOnLocalDateTime
    extends PmAttrBase<LocalDate, LocalDateTime>
    implements PmAttrLocalDate {

    /**
     * @param pmParent
     */
    public PmAttrLocalDateOnLocalDateTime(PmObject pmParent) {
        super(pmParent);
    }

    /** Provides {@link PmAttrLocalDate.FORMAT_DEFAULT_RES_KEY}. */
    @Override
    protected String getFormatDefaultResKey() {
        return PmAttrLocalDate.FORMAT_DEFAULT_RES_KEY;
    }

    @Override
    protected PmObjectBase.MetaData makeMetaData() {
        // Sets the default max length is the length of the date format pattern.
        MetaData md = new MetaData(11);
        // Configure the default converters. Is done before <code>initMetaData</code> to allow
        // annotation based customization.
        md.setValueConverter(new DefaultValueConverter());
        md.setStringConverter(new LocalDateStringConverter());

        return md;
    }

    /**
     * A type converter without time zone logic.
     */
    public static class DefaultValueConverter implements ValueConverter<LocalDate, LocalDateTime> {
        @Override
        public LocalDate toExternalValue(ValueConverterCtxt ctxt, LocalDateTime i) {
            return i.toLocalDate();
        }

        @Override
        public LocalDateTime toInternalValue(ValueConverterCtxt ctxt, LocalDate e) {
            return e.toLocalDateTime(new LocalTime(0, 0, 0));
        }
    }

    /**
     * Converts the external time zone related date value to a backing local date time in UTC.
     */
    public static class ValueConverterWithTimeZone extends JodaTimeZoneConverterBase<LocalDate, LocalDateTime> {
        @Override
        public LocalDate toExternalValue(ValueConverterCtxt ctxt, LocalDateTime i) {
            DateTime intDt = i.toDateTime(getInternalValueDateTimeZone(ctxt));
            DateTime extDt = intDt.withZone(getExternalValueDateTimeZone(ctxt));
            return extDt.toLocalDate();
        }
        @Override
        public LocalDateTime toInternalValue(ValueConverterCtxt ctxt, LocalDate e) {
            DateTime extDt = e.toDateMidnight(getExternalValueDateTimeZone(ctxt)).toDateTime();
            return extDt.toDateTime(getInternalValueDateTimeZone(ctxt)).toLocalDateTime();
        }
    }

}
