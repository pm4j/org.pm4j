package org.pm4j.core.pm;

import org.junit.Test;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;

import static org.junit.Assert.assertEquals;

public class PmAttrValueAccessKindTest {

  private MyBean myBean = new MyBean();
  private MyPm myPm = new MyPm(new PmConversationImpl(), myBean);

  @Test
  public void testAccessGetterAndSetter() {
    assertEquals(null, myPm.stringWithGetterAndSetter.getValue());
    myPm.stringWithGetterAndSetter.setValue("x");
    assertEquals("x", myPm.stringWithGetterAndSetter.getValue());
  }

  @Test
  public void testAccessBeanField() {
    assertEquals(null, myPm.stringField.getValue());
    myPm.stringField.setValue("x");
    assertEquals("x", myPm.stringField.getValue());
  }

  @Test
  public void testAccessBeanFieldWithOverriddenGetBackingValue() {
    assertEquals(0, myPm.intField.getValue().intValue());
    myPm.intField.setValue(3);
    assertEquals(3, myPm.intField.getValue().intValue());
  }



  @PmBeanCfg(beanClass=MyBean.class)
  public static class MyPm extends PmBeanBase<MyBean> {
    /** In a subclass of PmBean this field gets bound to the corresponding field or getter/setter pair. */
    public final PmAttrString stringField = new PmAttrStringImpl(this);
    /** In a subclass of PmBean this field gets bound to the corresponding field or getter/setter pair. */
    public final PmAttrString stringWithGetterAndSetter = new PmAttrStringImpl(this);
    /** In a subclass of PmBean this field gets bound to the corresponding field or getter/setter pair. */
    public final PmAttrInteger intField = new PmAttrIntegerImpl(this) {
      /** An call to super.getBackingValueImpl() provides the value using the configured value access strategy. */
      protected Integer getBackingValueImpl() {
        return super.getBackingValueImpl();
      }
    };

    public MyPm(PmObject parentPm, MyBean myBean) {
      super(parentPm, myBean);
    }
  }


  public static class MyBean {
    public String stringField;
    private String _stringWithGetterAndSetter;
    public int intField;

    public String getStringWithGetterAndSetter() {
      return _stringWithGetterAndSetter;
    }
    public void setStringWithGetterAndSetter(String stringWithGetterAndSetter) {
      this._stringWithGetterAndSetter = stringWithGetterAndSetter;
    }
  }
}
