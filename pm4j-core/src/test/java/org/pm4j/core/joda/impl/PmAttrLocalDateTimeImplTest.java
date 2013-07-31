package org.pm4j.core.joda.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Locale;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.tools.test.PmAssert;

/**
 * Test for {@link PmAttrLocalDateTimeImpl}.
 *
 * @author HLLY
 * @since GLOBE 1.4
 *
 */
public class PmAttrLocalDateTimeImplTest {

    private PmConversation conversation;

    @Before
    public void setUp() {
        conversation = new PmConversationImpl();
        conversation.setPmLocale(Locale.ENGLISH);
    }

    @Test
    public void conversionError() {
        PmAttrLocalDateTimeImpl datePm = new PmAttrLocalDateTimeImpl(conversation);
        datePm.setValueAsString("320/04/2012 10:00");
        assertFalse(datePm.isPmValid());
        PmAssert.assertSingleErrorMessage(datePm, "The value of the field \"pmAttrLocalDateTimeImpl\" cannot be interpreted. Please use the format \"dd/MM/yyyy hh:mm\".");
    }

    @Test
    public void parseSuccess() {
        PmAttrLocalDateTimeImpl datePm = new PmAttrLocalDateTimeImpl(conversation);
        datePm.setValueAsString("29/05/2012 10:00");
        assertTrue(datePm.isPmValid());
        assertEquals(new LocalDateTime(2012, 5, 29, 10, 0), datePm.getBackingValue());
        assertEquals("29/05/2012 10:00", datePm.getValueAsString());
    }

    @Test
    public void testBeanValueConversion() {
        PmAttrLocalDateTimeImpl datePm = new PmAttrLocalDateTimeImpl(conversation);
        LocalDateTime localDate = new LocalDateTime(2012, 6, 6, 10, 0);
        datePm.setValue(localDate);
        assertTrue(datePm.isPmValid());
        assertEquals("06/06/2012 10:00", datePm.getValueAsString());
        assertEquals(localDate, datePm.getBackingValue());
    }

    @Test
    public void testLocalDateComparison() {
        PmAttrLocalDateTimeImpl datePm1 = new PmAttrLocalDateTimeImpl(conversation);
        datePm1.setValue(new LocalDateTime(2000, 1, 1, 10, 0));
        PmAttrLocalDateTimeImpl datePm2 = new PmAttrLocalDateTimeImpl(conversation);
        datePm2.setValue(new LocalDateTime(2000, 1, 1, 11, 0));
        PmAttrLocalDateTimeImpl datePm3 = new PmAttrLocalDateTimeImpl(conversation);
        datePm3.setValue(new LocalDateTime(2001, 1, 1, 10, 0));
        PmAttrLocalDateTimeImpl datePm4 = new PmAttrLocalDateTimeImpl(conversation);
        datePm4.setValue(new LocalDateTime(2000, 1, 1, 10, 0));

        // x < y
        assertEquals(datePm1.compareTo(datePm2), -1);
        assertEquals(datePm2.compareTo(datePm3), -1);
        assertEquals(datePm1.compareTo(datePm3), -1);

        // x > y
        assertEquals(datePm2.compareTo(datePm1), 1);
        assertEquals(datePm3.compareTo(datePm2), 1);
        assertEquals(datePm3.compareTo(datePm1), 1);

        // x = y
        assertEquals(datePm1.compareTo(datePm1), 0);
        assertEquals(datePm1.compareTo(datePm4), 0);
    }

}
