package org.pm4j.core.pm.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmAttrCfg.AttrAccessKind;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.tools.test._PmAssert;

/**
 * Tests for {@link PmAttrUtil}.
 *
 * @author Olaf Boede
 */
public class PmAttrUtilTest {

  private TestPm testPm = new TestPm();

  @Before
  public void setUp() {
    _PmAssert.setValue(testPm.s, "V1");
    _PmAssert.setValue(testPm.sWithDefault, "V2");
    _PmAssert.assertChanged(testPm, testPm.s, testPm.sWithDefault);
  }

  @Test
  public void testResetBackingValueToDefault() {
    PmAttrUtil.resetBackingValueToDefault(testPm.s, testPm.sWithDefault);

    assertEquals(null, testPm.s.getValue());
    assertEquals(null, testPm.s.getBackingValue());
    assertEquals("DEFAULT", testPm.sWithDefault.getBackingValue());
    assertEquals("DEFAULT", testPm.sWithDefault.getValue());
  }

  @Test
  public void testResetBackingValuesToDefault() {
    PmAttrUtil.resetBackingValuesToDefault(testPm);

    assertEquals(null, testPm.s.getValue());
    assertEquals(null, testPm.s.getBackingValue());
    assertEquals("DEFAULT", testPm.sWithDefault.getBackingValue());
    assertEquals("DEFAULT", testPm.sWithDefault.getValue());
  }

  @Test
  public void testIsEmptyValue() {
    assertEquals(true, PmAttrUtil.isEmptyValue(testPm.s, ""));
    assertEquals(true, PmAttrUtil.isEmptyValue(testPm.s, null));
    assertEquals(false, PmAttrUtil.isEmptyValue(testPm.s, "x"));
  }

  @Test
  public void testGetBackingValue() {
    assertEquals("V1", PmAttrUtil.getBackingValue(testPm.s));
    assertEquals("V2", PmAttrUtil.getBackingValue(testPm.sWithDefault));

  }

  @Test
  public void testConvertStringToValue() {
    assertEquals(Integer.valueOf(7), PmAttrUtil.convertStringToValue(testPm.i, "7"));
    assertEquals("A convert call does not influence the value of the used attribute instance.",
                 null, testPm.i.getValue());
  }

  @Test
  public void testConvertStringToValueWithConverterException() {
    try {
      PmAttrUtil.convertStringToValue(testPm.i, "x");
      Assert.fail("Should throw an exception.");
    } catch (PmRuntimeException e) {
      // TODO oboede: implement a useful message.
      // assertEquals("pmException args: [Ljava.lang.Object;@14520eb pm=i(3c9217) - Exception context: Class: 'org.pm4j.core.pm.impl.PmAttrIntegerImpl' PM: 'i(3c9217)'", e.getMessage());
    }
    assertEquals("A convert call does not influence the value of the used attribute instance.",
                 null, testPm.i.getValue());
  }

  @Test
  public void testConvertValueToString() {
    assertEquals("3", PmAttrUtil.convertValueToString(testPm.i, Integer.valueOf(3)));
    assertEquals("A convert call does not influence the value of the used attribute instance.",
                 null, testPm.i.getValue());
  }

  @Test
  public void testCanWriteBackingValue() {
    assertEquals(true, PmAttrUtil.isBackingValueWriteable(testPm.i));
    assertEquals(true, PmAttrUtil.isBackingValueWriteable(testPm.s));
    assertEquals(true, PmAttrUtil.isBackingValueWriteable(testPm.sWithDefault));
    assertEquals(false, PmAttrUtil.isBackingValueWriteable((PmAttrBase<?,?>) testPm.calculatedString));
  }

  /** Internally used test PM */
  public static class TestPm extends PmConversationImpl {

    public final PmAttrStringImpl s = new PmAttrStringImpl(this);

    @PmAttrCfg(defaultValue="DEFAULT")
    public final PmAttrStringImpl sWithDefault = new PmAttrStringImpl(this);

    @PmTitleCfg(title="integer attr")
    public final PmAttrIntegerImpl i = new PmAttrIntegerImpl(this);

    /** setBackingValue() will not work for this attribute. */
    @PmAttrCfg(accessKind=AttrAccessKind.OVERRIDE)
    public final PmAttrString calculatedString = new PmAttrStringImpl(this) {
      @Override
      protected String getBackingValueImpl() {
        return "hello";
      }
    };
}

}
