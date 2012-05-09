package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmAttrValueChangeDecorator;
import org.pm4j.core.pm.impl.PmConversationImpl;


public class PmAttrValueChangeDecoratorTest {

  private MyPm pm = new MyPm();
  private String expectedOldValue, expectedNewValue;
  private boolean allowValueChange = true;

  /**
   * The decorator to test. Has some test logic to apply before and after setting the value.
   */
  private PmCommandDecorator valueChangeDecorator = new PmAttrValueChangeDecorator<String>() {

    @Override
    protected boolean beforeChange(PmAttr<String> pmAttr, String oldValue, String newValue) {
      assertEquals(expectedOldValue, oldValue);
      assertEquals(expectedNewValue, newValue);
      assertEquals("The attribute value is unchanged when this method gets called.", expectedOldValue, pmAttr.getValue());
      return allowValueChange;
    }

    @Override
    protected void afterChange(PmAttr<String> pmAttr, String oldValue, String newValue) {
      assertEquals(expectedOldValue, oldValue);
      assertEquals(expectedNewValue, newValue);
      assertEquals("The attribute value is changed when this method gets called.", expectedNewValue, pmAttr.getValue());
    }
  };

  public class MyPm extends PmConversationImpl {
    public final PmAttrString stringAttr = new PmAttrStringImpl(this);
  }

  @Before
  public void setUp() {
    pm = new MyPm();
    expectedOldValue = null;
    expectedNewValue = "a";
    allowValueChange = true;
  }


  @Test
  public void testValueChangeDecorator() {
    PmEventApi.addValueChangeDecorator(pm.stringAttr, valueChangeDecorator);

    pm.stringAttr.setValue(expectedNewValue);
    assertEquals("The attribute value is changed.", expectedNewValue, pm.stringAttr.getValue());
  }

  @Test
  public void testValueChangeDecoratorWithBeforeDoVeto() {
    allowValueChange = false;
    PmEventApi.addValueChangeDecorator(pm.stringAttr, valueChangeDecorator);

    pm.stringAttr.setValue(expectedNewValue);
    assertEquals("The attribute value is unchanged.", null, pm.stringAttr.getValue());
  }

}
