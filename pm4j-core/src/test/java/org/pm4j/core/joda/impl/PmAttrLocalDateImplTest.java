package org.pm4j.core.joda.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.pm4j.tools.test.PmAssert.setValueAsString;

import java.util.Locale;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.tools.test.PmAssert;

public class PmAttrLocalDateImplTest {

    private PmConversation conversation;

    @Before
    public void setUp() {
        conversation = new PmConversationImpl();
        conversation.setPmLocale(Locale.ENGLISH);
    }

    @Test
    public void conversionError() {
        PmAttrLocalDateImpl datePm = new PmAttrLocalDateImpl(conversation);
        datePm.setValueAsString("20/Apr/2012");
        assertFalse(datePm.isPmValid());
        PmAssert.assertSingleErrorMessage(datePm, "The value of the field \"pmAttrLocalDateImpl\" cannot be interpreted. Please use the format \"dd/MM/yyyy\".");
    }

    @Test
    public void testSetValueAsString() {
        setValueAsString(new PmAttrLocalDateImpl(conversation), "29/05/2012");
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testLocalDateComparison() {
        PmAttrLocalDateImpl datePm1 = new PmAttrLocalDateImpl(conversation);
        datePm1.setValue(new LocalDate(2000, 1, 1));
        PmAttrLocalDateImpl datePm2 = new PmAttrLocalDateImpl(conversation);
        datePm2.setValue(new LocalDate(2000, 2, 1));
        PmAttrLocalDateImpl datePm3 = new PmAttrLocalDateImpl(conversation);
        datePm3.setValue(new LocalDate(2001, 1, 1));
        PmAttrLocalDateImpl datePm4 = new PmAttrLocalDateImpl(conversation);
        datePm4.setValue(new LocalDate(2000, 1, 1));

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
