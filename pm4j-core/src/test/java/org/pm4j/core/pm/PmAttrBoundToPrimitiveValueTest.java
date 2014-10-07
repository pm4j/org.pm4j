package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.pm4j.tools.test.PmAssert.assertMessage;
import static org.pm4j.tools.test.PmAssert.assertNoMessagesInSubTree;

import java.util.Locale;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmAttrBooleanImpl;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmAttrBoundToPrimitiveValueTest {

  private TestBean testBean = new TestBean();
  private TestPm testPm = new TestPm(new PmConversationImpl(), testBean);

  @Before
  public void setUp() {
    testPm.getPmConversation().setPmLocale(Locale.ENGLISH);
  }

  @Test(expected=PmRuntimeException.class)
  @Ignore("Will be activated in release v0.8")
  public void setNullValueDoesNotWork() {
    testPm.b.setValue(null);
  }

  // TODO oboede: will be replaced in release v0.8
  @Test
  public void setNullValueDoesNotWorkOldVersion() {
    testPm.b.setValue(null);
    assertMessage("Please enter a value into \"boolean attr\".", Severity.ERROR, testPm.b);
  }

  @Test
  public void setNullValueWithNullConversionWorks() {
    testPm.bWithNullConversion.setValue(null);
    assertNoMessagesInSubTree(testPm.bWithNullConversion);
    assertEquals(Boolean.FALSE, testPm.bWithNullConversion.getValue());
  }

  @Test
  public void defaultValueDoesNotWorkForPrimitives() {
    assertEquals("The configured default value can't work because a primitive value has never a 'null' value.",
                 0, testPm.iWithDefault.getValue().intValue());
  }

  static class TestPm extends PmBeanBase<TestBean> {
    @PmTitleCfg(title="boolean attr")
    public final PmAttrBoolean b = new PmAttrBooleanImpl(this);

    @PmTitleCfg(title="int attr")
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);

    @PmTitleCfg(title="int attr")
    @PmAttrCfg(defaultValue="1")
    public final PmAttrInteger iWithDefault = new PmAttrIntegerImpl(this);

    /** A very special attribute that accepts null values and converts them to false. */
    @PmTitleCfg(title="boolean attr with null conversion")
    public final PmAttrBoolean bWithNullConversion = new PmAttrBooleanImpl(this) {
      /** Pass null-values to the converter. Is disabled by default. */
      @Override
      protected boolean isConvertingNullValueImpl() {
        return true;
      }
      /** Converts a null-value to false. */
      @Override
      public Boolean convertPmValueToBackingValue(Boolean externalValue) {
        return externalValue == null ? Boolean.FALSE : externalValue;
      };
    };

    public TestPm(PmObject parentPm, TestBean testBean) {
      super(parentPm, testBean);
    }
  }

  public static class TestBean {
    public boolean b;
    public int i;
    public int iWithDefault;
    public boolean bWithNullConversion;
  }

}
