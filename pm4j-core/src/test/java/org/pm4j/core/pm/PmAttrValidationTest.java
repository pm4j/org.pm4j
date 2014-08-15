package org.pm4j.core.pm;

import org.junit.Test;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmInitApi;
import org.pm4j.tools.test.PmAssert;

public class PmAttrValidationTest {

  private TestPm pm = PmInitApi.ensurePmInitialization(new TestPm());

  @Test
  public void testValidateVisibleAttr() {
    PmAssert.validateNotSuccessful(pm, "Please enter a value into \"Required Integer\".");
  }

  @Test
  public void testDontValidateInvisibleAttr() {
    pm.requiredIntVisible = false;
    PmAssert.validateSuccessful(pm);
  }


  public static class TestPm extends PmConversationImpl {
    boolean requiredIntVisible = true;

    @PmAttrCfg(required=true)
    @PmTitleCfg(title="Required Integer")
    public final PmAttrInteger requiredInt = new PmAttrIntegerImpl(this) {
      @Override
      protected boolean isPmVisibleImpl() {
        return requiredIntVisible;
      }
    };

    /** Checks the newer validation logic. */
    @Override
    protected boolean isDeprValidation() {
      return false;
    };

  }

}
