package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;


public class PmAttrBeforeAndAfterValueChangeMethodsTest {

  private MyPm pm = new MyPm();
  private String expectedOldValue, expectedNewValue;
  private boolean allowValueChange = true;


  public class MyPm extends PmElementImpl {

    public final PmAttrString myAttr = new PmAttrStringImpl(this) {

      @Override
      protected boolean beforeValueChange(String oldValue, String newValue) {
        assertEquals(expectedOldValue, oldValue);
        assertEquals(expectedNewValue, newValue);
        assertEquals("The attribute value is unchanged when this method gets called.", expectedOldValue, getValue());
        return allowValueChange;
      }

      @Override
      protected void afterValueChange(String oldValue, String newValue) {
        assertEquals(expectedOldValue, oldValue);
        assertEquals(expectedNewValue, newValue);
        assertEquals("The attribute value is changed when this method gets called.", expectedNewValue, getValue());
      }
    };
  }

  @Before
  public void setUp() {
    pm = new MyPm();
    pm.setPmParent(new PmConversationImpl());
    expectedOldValue = null;
    expectedNewValue = "a";
    allowValueChange = true;
  }


  @Test
  public void testBeforeAndAfterDoMethods() {
    pm.myAttr.setValue(expectedNewValue);
    assertEquals("The attribute value is changed.", expectedNewValue, pm.myAttr.getValue());
  }

  @Test
  public void testBeforeAndAfterDoMethodsWithBeforeDoVeto() {
    allowValueChange = false;

    pm.myAttr.setValue(expectedNewValue);
    assertEquals("The attribute value is unchanged because of the beforDo result.", null, pm.myAttr.getValue());
  }

}
