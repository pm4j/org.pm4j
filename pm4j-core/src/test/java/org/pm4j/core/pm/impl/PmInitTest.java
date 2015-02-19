package org.pm4j.core.pm.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmObjectBase.PmInitState;

/**
 * Tests PM initialization aspects.
 *
 * @author olaf boede
 */
public class PmInitTest {

  private MyElement myElement;

  @Before
  public void setUp() {
    myElement = new MyElement(new PmConversationImpl());
  }


  @Test
  public void testTheParentShouldBeInitializedWhenChildGetsUsed() {
    assertEquals("Not initialized before calling the first PM interface method.", PmInitState.NOT_INITIALIZED, myElement.pmInitState);
    assertEquals("Not initialized before calling the first PM interface method.", PmInitState.NOT_INITIALIZED, myElement.s.pmInitState);

    myElement.s.isPmEnabled();

    assertEquals("Attribute is initialized after using the first PM interface method.", PmInitState.INITIALIZED, myElement.s.pmInitState);
    assertEquals("Parent is also initialized when a child is initialized.", PmInitState.INITIALIZED, myElement.pmInitState);
  }


  public static class MyElement extends PmObjectBase {
    public final PmAttrStringImpl s = new PmAttrStringImpl(this);

    public MyElement(PmObject pmParent) {
      super(pmParent);
    }
  }
}
