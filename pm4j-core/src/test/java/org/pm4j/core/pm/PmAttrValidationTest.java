package org.pm4j.core.pm;

import java.util.Locale;

import org.junit.Test;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;
import org.pm4j.core.pm.impl.PmInitApi;
import org.pm4j.tools.test._PmAssert;

public class PmAttrValidationTest {

  private TestPm pm = PmInitApi.ensurePmInitialization(new TestPm(new TestConversationPm()));

  @Test
  public void testValidateVisibleAttr() {
    _PmAssert.validateNotSuccessful(pm, "Please enter a value into \"Required Integer\".");
  }

  @Test
  public void testDontValidateInvisibleAttr() {
    pm.requiredIntVisible = false;
    _PmAssert.validateSuccessful(pm);
  }


  public static class TestPm extends PmElementImpl {
    boolean requiredIntVisible = true;

    @PmAttrCfg(required=true)
    @PmTitleCfg(title="Required Integer")
    public final PmAttrInteger requiredInt = new PmAttrIntegerImpl(this) {
      @Override
      protected boolean isPmVisibleImpl() {
        return requiredIntVisible;
      }
    };

    public TestPm(PmObject parentPm) {
      super(parentPm);
    }

  }

  /** Test conversation setup. */
  public static class TestConversationPm extends PmConversationImpl {
    /** Checks the newer validation logic. */
    @Override
    protected boolean isDeprValidation() {
      return false;
    };

    /** Ensure that the message language, because the tested messages are localized. */
    @Override
    public Locale getPmLocale() {
      return Locale.ENGLISH;
    }
  }

}
