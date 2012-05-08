package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmAttrValueChangeDecorator;
import org.pm4j.core.pm.impl.PmConversationImpl;


public class PmAttrValueChangeObservationTest {

  @Test
  public void testBeforeAndAfterDoMethods() {
    pm.attrWithBeforeAndAfterDoImpl.setValue(expectedNewValue);
    assertEquals("The attribute value is changed.", expectedNewValue, pm.attrWithBeforeAndAfterDoImpl.getValue());
  }

  @Test
  public void testBeforeAndAfterDoMethodsWithBeforeDoVeto() {
    allowValueChange = false;

    pm.attrWithBeforeAndAfterDoImpl.setValue(expectedNewValue);
    assertEquals("The attribute value is unchanged because of the beforDo result.", null, pm.attrWithBeforeAndAfterDoImpl.getValue());
  }

  @Test
  public void testSetValueListener() {
    pm.stringAttr.addValueChangeDecorator(valueChangeDecorator);

    pm.stringAttr.setValue(expectedNewValue);
    assertEquals("The attribute value is changed.", expectedNewValue, pm.stringAttr.getValue());
  }

  @Test
  public void testSetValueListenerWithBeforeDoVeto() {
    allowValueChange = false;
    pm.stringAttr.addValueChangeDecorator(valueChangeDecorator);

    pm.stringAttr.setValue(expectedNewValue);
    assertEquals("The attribute value is unchanged.", null, pm.stringAttr.getValue());
  }


  // -- test setup --

  public class MyPm extends PmConversationImpl {

    public final PmAttrString attrWithBeforeAndAfterDoImpl = new PmAttrStringImpl(this) {

      @Override
      protected boolean beforeValueChange(String oldValue, String newValue) {
        checkExpectedBeforeValues(this, oldValue, newValue);
        return allowValueChange;
      }

      @Override
      protected void afterValueChange(String oldValue, String newValue) {
        checkExpectedAfterValues(this, oldValue, newValue);
      }
    };

    public final PmAttrString stringAttr = new PmAttrStringImpl(this);
  }

  private MyPm pm = new MyPm();
  private String expectedOldValue, expectedNewValue;
  private boolean allowValueChange = true;

  private PmCommandDecorator valueChangeDecorator = new PmAttrValueChangeDecorator<String>() {

    @Override
    protected boolean beforeChange(PmAttr<String> pmAttr, String oldValue, String newValue) {
      checkExpectedBeforeValues(pmAttr, oldValue, newValue);
      return allowValueChange;
    }

    @Override
    protected void afterChange(PmAttr<String> pmAttr, String oldValue, String newValue) {
      checkExpectedAfterValues(pmAttr, oldValue, newValue);
    }
  };

  @Before
  public void setUp() {
    pm = new MyPm();
    expectedOldValue = null;
    expectedNewValue = "a";
    allowValueChange = true;
  }

  private void checkExpectedBeforeValues(PmAttr<String> attr, String oldValue, String newValue) {
    assertEquals(expectedOldValue, oldValue);
    assertEquals(expectedNewValue, newValue);
    assertEquals("The attribute value is unchanged when this method gets called.", expectedOldValue, attr.getValue());
  }

  private void checkExpectedAfterValues(PmAttr<String> attr, String oldValue, String newValue) {
    assertEquals(expectedOldValue, oldValue);
    assertEquals(expectedNewValue, newValue);
    assertEquals("The attribute value is changed when this method gets called.", expectedNewValue, attr.getValue());
  }

}
