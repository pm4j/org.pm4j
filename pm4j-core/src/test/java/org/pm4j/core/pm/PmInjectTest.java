package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.annotation.PmInject.Mode;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmInitApi;
import org.pm4j.core.pm.impl.PmObjectBase;

/**
 * Tests some PmInject variants.
 *
 * @author Olaf Boede
 */
public class PmInjectTest {

  public final class MyPm extends PmObjectBase {

    public MyPm(PmObject pmParent) {
      super(pmParent);
    }

    @PmInject(value="#myProp")
    private String myInjectedProp;

    @PmInject
    private String myInjectedProp2;

    @PmInject(nullAllowed=true)
    private String injectedNullProp;

    @PmInject(mode=Mode.PARENT_OF_TYPE)
    private PmObject injectedParentOfType;

    private String setterInjectedProp;

    public String getSetterInjectedProp() {
      return this.setterInjectedProp;
    }

    @PmInject("#myProp")
    public void setSetterInjectedProp(String s) {
      this.setterInjectedProp = s;
    }

  }

  private PmConversation pmConversation;
  private MyPm myPm;

  @Before
  public void setUp() {
    pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myProp", "abc");
    pmConversation.setPmNamedObject("myInjectedProp2", "prop2");
    myPm = PmInitApi.initPmTree(new MyPm(pmConversation));
  }

  @Test
  public void testInjection() {
    // ensure initialization before accessing injected attributes.
    myPm.isPmVisible();
    assertEquals("abc", myPm.myInjectedProp);
    assertEquals("prop2", myPm.myInjectedProp2);
    assertEquals(null, myPm.injectedNullProp);
    assertEquals("The immediate parent is the first one that implements the requested interface.", pmConversation, myPm.injectedParentOfType);
    assertEquals("abc", myPm.setterInjectedProp);
  }

  @Test
  public void testNullNotAllowedInjectionFailsForNullValue() {
    try {
      new InvalidPm().getPmTitle();
      fail("Usage of an invalid injection configuration should throw an exception.");
    }
    catch (PmRuntimeException e) {
      // ok. That should be thrown.
    }
  }

  @Test
  public void testNotResolvableInjectionWithLenientConfiguration() {
    InvalidPm pm = new InvalidPm();
    pm.getPmDefaults().setDiResolverLenientNullCheck(true);
    assertEquals("The test should ignore the unused and not resolvable @PmInject.",
                 "PM with not resolvable PmInject.", pm.getPmTitle());
  }


  @PmTitleCfg(title="PM with not resolvable PmInject.")
  public final class InvalidPm extends PmConversationImpl {
    @PmInject private String nullNotAllowedProperty;
  }


}
