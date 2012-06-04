package org.pm4j.core.pm.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrProxy;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmConversation;

public class PmAttrProxyTest {

  private PmAttrProxy<String> stringAttrProxy;
  private PmAttrString stringAttr;

  @Before
  public void setUp() {
    PmConversation c = new PmConversationImpl();
    stringAttr = new PmAttrStringImpl(c);
    stringAttrProxy = new PmAttrProxyImpl<String>(c);
  }

  @Test
  public void testForwardToDelegate() {
    stringAttrProxy.setDelegate(stringAttr);

    stringAttrProxy.setValue("hello");
    assertEquals("The attribute behind the proxy should receive the value set on the proxy.",
        "hello", stringAttr.getValue());
    assertEquals("The proxy should provide the value of its delegate.",
        "hello", stringAttrProxy.getValue());

    assertEquals("The proxy should provide the visbibility information of the delegate.",
        true, stringAttrProxy.isPmVisible());
    stringAttr.setPmVisible(false);
    assertEquals("The proxy should provide the visbibility information of the delegate.",
        false, stringAttrProxy.isPmVisible());
    stringAttr.setPmVisible(true);

    assertEquals("The proxy should provide the enablement information of the delegate.",
        true, stringAttrProxy.isPmEnabled());
    stringAttr.setPmEnabled(false);
    assertEquals("The proxy should provide the enablement information of the delegate.",
        false, stringAttrProxy.isPmEnabled());
    stringAttr.setPmEnabled(true);  }

}
