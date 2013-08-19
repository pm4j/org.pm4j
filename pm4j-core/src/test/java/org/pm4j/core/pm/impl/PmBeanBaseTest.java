package org.pm4j.core.pm.impl;

import static junit.framework.Assert.assertSame;

import org.junit.Test;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmBeanCfg;

public class PmBeanBaseTest {
  @Test
  public void testPmBeanBaseNested() {
    PmConversation session = new PmConversationImpl();
    MyPmBeanBase beanBase = new MyPmBeanBase();
    beanBase.setPmParent(session);
    MyBean bean1 = new MyBean();
    beanBase.setPmBean(bean1);
    assertSame("bean must be propagated to nested elements", bean1, beanBase.nested.getPmBean());

    MyBean bean2 = new MyBean();
    beanBase.reloadPmBean(bean2);
    assertSame("bean reloads must be propagated to nested elements", bean2, beanBase.nested.getPmBean());
  }

  static class MyBean {

  }

  @PmBeanCfg(beanClass = MyBean.class)
  static class MyPmBeanBase extends PmBeanBase<MyBean> {
    public final MyPmBeanNested nested = new MyPmBeanNested(this);
  }

  @PmBeanCfg(beanClass = MyBean.class)
  static class MyPmBeanNested extends PmBeanBase.Nested<MyBean> {

    public MyPmBeanNested(PmObject parentPm) {
      super(parentPm);
    }

  }
}
