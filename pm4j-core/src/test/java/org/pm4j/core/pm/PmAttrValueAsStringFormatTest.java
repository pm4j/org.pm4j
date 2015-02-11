package org.pm4j.core.pm;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.impl.PmAttrBigDecimalImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmObjectBase;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.Assert.*;

public class PmAttrValueAsStringFormatTest {

  public static class MyPm extends PmObjectBase {

    /** Default format: <code>#.####...</code> */
    public final PmAttrBigDecimal bigDecimalWithDefaultFormat = new PmAttrBigDecimalImpl(this);

    /** Format declaration in resource file: <code>#,##0.00;(#,##0.00)</code> */
    public final PmAttrBigDecimal bigDecimalWithEmbracedNegativeFormat = new PmAttrBigDecimalImpl(this);

    /** Format declaration in resource file: <code>0.#|#,##0.00</code> */
    public final PmAttrBigDecimal bigDecimalWithMultiFormat = new PmAttrBigDecimalImpl(this);
  }

  private MyPm myPm;

  @Before
  public void setup() {
    myPm = new MyPm();
    myPm.setPmParent(new PmConversationImpl());
    myPm.getPmConversation().setPmLocale(Locale.ENGLISH);
  }

  @Test
  public void testBigDecimalWithDefaultFormat() {
    assertEquals("Initial null value.", null, myPm.bigDecimalWithDefaultFormat.getValueAsString());

    myPm.bigDecimalWithDefaultFormat.setValueAsString("0");
    assertTrue("The attribute should not report a conversion error.", myPm.bigDecimalWithDefaultFormat.isPmValid());
    assertEquals("Zero value.", "0", myPm.bigDecimalWithDefaultFormat.getValueAsString());

    myPm.bigDecimalWithDefaultFormat.setValueAsString("1000.23");
    assertTrue("The attribute should not report a conversion error.", myPm.bigDecimalWithDefaultFormat.isPmValid());
    assertEquals("Bigger positive value.", "1000.23", myPm.bigDecimalWithDefaultFormat.getValueAsString());

    myPm.bigDecimalWithDefaultFormat.setValueAsString("-1000.23");
    assertTrue("The attribute should not report a conversion error.", myPm.bigDecimalWithDefaultFormat.isPmValid());
    assertEquals("Bigger positive value.", "-1000.23", myPm.bigDecimalWithDefaultFormat.getValueAsString());
  }

  @Test
  public void testBigDecimalWithEmbracedNegativeFormat() {
    assertEquals("Initial null value.", null, myPm.bigDecimalWithEmbracedNegativeFormat.getValueAsString());

    myPm.bigDecimalWithEmbracedNegativeFormat.setValueAsString("0");
    assertTrue("The attribute should not report a conversion error.", myPm.bigDecimalWithEmbracedNegativeFormat.isPmValid());
    assertEquals("Zero value.", "0.00", myPm.bigDecimalWithEmbracedNegativeFormat.getValueAsString());

    myPm.bigDecimalWithEmbracedNegativeFormat.setValueAsString("1,000.23");
    assertTrue("The attribute should not report a conversion error.", myPm.bigDecimalWithEmbracedNegativeFormat.isPmValid());
    assertEquals("Bigger positive value.", "1,000.23", myPm.bigDecimalWithEmbracedNegativeFormat.getValueAsString());

    myPm.bigDecimalWithEmbracedNegativeFormat.setValueAsString("1000.23");
    assertTrue("The comma is optional. The attribute should not report a conversion error.", myPm.bigDecimalWithEmbracedNegativeFormat.isPmValid());
    assertEquals("Bigger positive value.", "1,000.23", myPm.bigDecimalWithEmbracedNegativeFormat.getValueAsString());

    myPm.bigDecimalWithEmbracedNegativeFormat.setValueAsString("(1000.23)");
    assertTrue("The attribute should not report a conversion error.", myPm.bigDecimalWithEmbracedNegativeFormat.isPmValid());
    assertEquals("Bigger negative value.", "(1,000.23)", myPm.bigDecimalWithEmbracedNegativeFormat.getValueAsString());

    myPm.bigDecimalWithEmbracedNegativeFormat.setValueAsString("-0.23");
    assertFalse("The attribute is not configured to process the usual '-' prefixed format.", myPm.bigDecimalWithEmbracedNegativeFormat.isPmValid());
    assertEquals("The string value now shows the invalid value.", "-0.23", myPm.bigDecimalWithEmbracedNegativeFormat.getValueAsString());
    assertEquals("The real value is still the old one.", new BigDecimal("-1000.23"), myPm.bigDecimalWithEmbracedNegativeFormat.getValue());
  }

  @Test
  public void testBigDecimalWithMultiFormat() {
    assertEquals("Initial null value.", null, myPm.bigDecimalWithMultiFormat.getValueAsString());

    myPm.bigDecimalWithMultiFormat.setValueAsString("0");
    assertTrue("The attribute should not report a conversion error.", myPm.bigDecimalWithMultiFormat.isPmValid());
    assertEquals("Zero value.", "0.00", myPm.bigDecimalWithMultiFormat.getValueAsString());

    myPm.bigDecimalWithMultiFormat.setValueAsString("1,000.23");
    assertTrue("The attribute should not report a conversion error.", myPm.bigDecimalWithMultiFormat.isPmValid());
    assertEquals("Bigger positive value.", "1,000.23", myPm.bigDecimalWithMultiFormat.getValueAsString());

    myPm.bigDecimalWithMultiFormat.setValueAsString("1000.23");
    assertTrue("The comma is optional. The attribute should not report a conversion error.", myPm.bigDecimalWithMultiFormat.isPmValid());
    assertEquals("Bigger positive value.", "1,000.23", myPm.bigDecimalWithMultiFormat.getValueAsString());

    myPm.bigDecimalWithMultiFormat.setValueAsString("-1000.23");
    assertTrue("The attribute is configured to process the usual '-' prefixed format too (default decimal format behaviour).", myPm.bigDecimalWithMultiFormat.isPmValid());
    assertEquals("The big decimal value must fit.", new BigDecimal("-1000.23"), myPm.bigDecimalWithMultiFormat.getValue());
    assertEquals("Bigger positive value.", "-1,000.23", myPm.bigDecimalWithMultiFormat.getValueAsString());

    myPm.bigDecimalWithMultiFormat.setValueAsString("8.9000,2356");
    assertFalse("Parsing must fail, german decimal characters", myPm.bigDecimalWithMultiFormat.isPmValid());
    assertEquals("The string value now shows the invalid value.", "8.9000,2356", myPm.bigDecimalWithMultiFormat.getValueAsString());
    assertEquals("The big decimal value is still the old value.", new BigDecimal("-1000.23"), myPm.bigDecimalWithMultiFormat.getValue());
    
    myPm.bigDecimalWithMultiFormat.setValueAsString("80.235");
    assertFalse("Parsing must fail, to much fragtion digits", myPm.bigDecimalWithMultiFormat.isPmValid());
    assertEquals("The big decimal value is still the old value.", new BigDecimal("-1000.23"), myPm.bigDecimalWithMultiFormat.getValue());
    assertEquals("The string value now shows the invalid value.", "80.235", myPm.bigDecimalWithMultiFormat.getValueAsString());

    // DecimalFormat does evaluate max integer 
//    myPm.bigDecimalWithMultiFormat.setValueAsString("88880.25");
//    assertEquals("The big decimal value is still the old value.", new BigDecimal("-1000.23"), myPm.bigDecimalWithMultiFormat.getValue());
//    assertFalse("Parsing must fail, to much integer digits", myPm.bigDecimalWithMultiFormat.isPmValid());
//    
  }


}
