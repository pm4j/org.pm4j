package org.pm4j.core.pm;

import junit.framework.TestCase;

import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class BufferedPmValueTest extends TestCase {

  public void testPmBeanBuffer() {
    PmConversation myPmContext = new PmConversationImpl(MyBeanClassPm.class);
    myPmContext.setBufferedPmValueMode(true);

    MyBeanClass bean = new MyBeanClass();
    MyBeanClassPm beanPm = PmFactoryApi.getPmForBean(myPmContext, bean);

    beanPm.i.setValue(1);
    assertEquals(1, beanPm.i.getValue().intValue());
    assertEquals(0, bean.i);

    beanPm.commitBufferedPmChanges();
    assertEquals(1, bean.i);

    beanPm.i.setValue(2);
    assertEquals(2, beanPm.i.getValue().intValue());
    assertEquals(1, bean.i);

    beanPm.rollbackBufferedPmChanges();
    assertEquals(1, beanPm.i.getValue().intValue());
    assertEquals(1, bean.i);
  }

  public void testPmAttributeBuffer() {
    PmConversation myPmContext = new PmConversationImpl(MyBeanClassPm.class);
    myPmContext.setBufferedPmValueMode(true);

    MyBeanClass bean = new MyBeanClass();
    MyBeanClassPm beanPm = PmFactoryApi.getPmForBean(myPmContext, bean);

    beanPm.i.setValue(1);
    beanPm.j.setValue(1);
    assertEquals(1, beanPm.i.getValue().intValue());
    assertEquals(1, beanPm.j.getValue().intValue());
    assertEquals(0, bean.i);
    assertEquals(0, bean.j);

    beanPm.i.commitBufferedPmChanges();
    assertEquals(1, beanPm.i.getValue().intValue());
    assertEquals(1, beanPm.j.getValue().intValue());
    assertEquals(1, bean.i);
    assertEquals(0, bean.j);

    beanPm.rollbackBufferedPmChanges();
    assertEquals(1, beanPm.i.getValue().intValue());
    assertEquals(0, beanPm.j.getValue().intValue());
    assertEquals(1, bean.i);
    assertEquals(0, bean.j);
  }

  // --- Test data classes ---

  public static class MyBeanClass {
    public int i, j;
  };

  @PmBeanCfg(beanClass=MyBeanClass.class)
  public static class MyBeanClassPm extends PmBeanBase<MyBeanClass> {
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);
    public final PmAttrInteger j = new PmAttrIntegerImpl(this);
  }
}
