package org.pm4j.core.pm;

import static junit.framework.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.annotation.PmProduces;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmInitApi;

/**
 * Tests some {@link PmProduces}.
 *
 * @author OBOEDE
 */
public class PmProducesTest {

  public final class MyPm extends PmConversationImpl {

    @PmProduces(name = "sharedStringAttrPm")
    public final PmAttrString producingStringAttr = new PmAttrStringImpl(this);

    @PmInject("#sharedStringAttrPm")
    private PmAttrString injectedRefToProducedStringAttr;
  }

  private MyPm myPm = PmInitApi.ensurePmSubTreeInitialization(new MyPm());

  @Test
  public void testProducedInjection() {
    assertSame(myPm.producingStringAttr, myPm.injectedRefToProducedStringAttr);
  }

  public void testNullNotAllowedInjectionFailsForNullValue() {
    try {
      new InvalidPm().getPmTitle();
      fail("Usage of an invalid injection configuration should throw an exception.");
    }
    catch (PmRuntimeException e) {
      // ok. That should be thrown.
    }
  }

  public final class InvalidPm extends PmConversationImpl {
    @PmInject private String nullNotAllowedProperty;
  }


}
