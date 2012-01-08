package org.pm4j.core.pm;

import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

import junit.framework.TestCase;

public class PmAttrIntegerTest extends TestCase {

  public void testNullValue() {
    MyPm pm = new MyPm();

    assertEquals("Initial value should be null.", null, pm.i.getValue());
    assertEquals("Initial valueAsString should be null.", null, pm.i.getValueAsString());

    pm.i.setValueAsString("");

    assertEquals("valueAsString should be null, even if it was set to an empty string.", null, pm.i.getValueAsString());
  }

  public static class MyPm extends PmConversationImpl {
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);
  }

}
