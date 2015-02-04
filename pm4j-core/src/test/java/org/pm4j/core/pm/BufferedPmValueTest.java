package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.pm4j.tools.test._PmAssert.setValue;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class BufferedPmValueTest {
  private PmConversation pmConversation = new PmConversationImpl(MyBeanClassPm.class);
  private MyBeanClass bean = new MyBeanClass();
  private MyBeanClassPm beanPm = PmFactoryApi.getPmForBean(pmConversation, bean);

  @Before
  public void setUp() {
    pmConversation.setBufferedPmValueMode(true);
  }

  @Test
  public void testPmBeanBuffer() {
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

  @Test
  public void testPmAttributeBuffer() {
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

  @Test
  public void testSetAndReset() {
    setValue(beanPm.i, 1);
    assertEquals(0, bean.i);
    beanPm.i.commitBufferedPmChanges();
    assertEquals(1, beanPm.i.getValue().intValue());
    assertEquals(1, bean.i);

    beanPm.resetPmValues();
    assertEquals(13, beanPm.i.getValue().intValue());
    assertEquals("It's buffered. Also a reset should be committed",
                 1, bean.i);
    beanPm.i.commitBufferedPmChanges();
    assertEquals(13, bean.i);
  }

  // --- Test data classes ---

  public static class MyBeanClass {
    public int i, j;
  };

  @PmBeanCfg(beanClass=MyBeanClass.class)
  public static class MyBeanClassPm extends PmBeanBase<MyBeanClass> {
    @PmAttrCfg(defaultValue="13")
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);
    @PmAttrCfg(defaultValue="0")
    public final PmAttrInteger j = new PmAttrIntegerImpl(this);
  }
}
