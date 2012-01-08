package org.pm4j.core.pm;

import junit.framework.TestCase;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.impl.PmElementImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmInjectTest extends TestCase {

  public final class MyPm extends PmElementImpl {

    public MyPm(PmObject pmParent) {
      super(pmParent);
    }

    @PmInject(value="myProp")
    private String myInjectedProp;

    @PmInject
    private String myInjectedProp2;

    @PmInject(nullAllowed=true)
    private String injectedNullProp;

  }

  public void testInjection() {
    PmConversation pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myProp", "abc");
    pmConversation.setPmNamedObject("myInjectedProp2", "prop2");
    MyPm myPm = new MyPm(pmConversation);

    // ensure initialization before accessing injected attributes.
    myPm.isPmVisible();
    assertEquals("abc", myPm.myInjectedProp);
    assertEquals("prop2", myPm.myInjectedProp2);
    assertEquals(null, myPm.injectedNullProp);
  }

  public final class InvalidPm extends PmConversationImpl {
    @SuppressWarnings("unused")
    @PmInject private String nullNotAllowedProperty;
  }

  public void testNullNotAllowedInjectionFailsForNullValue() {
    try {
      new InvalidPm().getPmTitle();
      fail("Usage of an invalid injection configuration should throw an exception.");
    }
    catch (PmRuntimeException e) {
      e.printStackTrace();
    }
  }
}
