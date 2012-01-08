package org.pm4j.core.pm;

import junit.framework.TestCase;

import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.impl.PmAttrBooleanImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmInitTest extends TestCase {

  public static class MyBean {
    public boolean b1;
    public String s1;
  }

  @PmBeanCfg(beanClass=MyBean.class, autoCreateBean=true)
  public static class MyBeanPm extends PmBeanBase<MyBean> {
    public final PmAttrBoolean b1 = new PmAttrBooleanImpl(this);
    public final PmAttrString s1 = new PmAttrStringImpl(this);
  }

  public void testGetRelativeName() {
    PmConversation session = new PmConversationImpl(MyBeanPm.class);
    MyBean bean = new MyBean();
    MyBeanPm pm = PmFactoryApi.getPmForBean(session, bean);

    assertEquals("b1", pm.b1.getPmRelativeName());
  }

  public void testGetRelativeName2() {
    PmConversation session = new PmConversationImpl(MyBeanPm.class);
    MyBeanPm pm = new MyBeanPm();
    pm.setPmParent(session);
    pm.b1.getPmRelativeName();
    assertEquals("b1", pm.b1.getPmRelativeName());
  }

  public void testGetRelativeName3() {
    PmConversation session = new PmConversationImpl(MyBeanPm.class);
    MyBean bean = new MyBean();
    MyBeanPm pm = new MyBeanPm();
    pm.setPmParent(session);
    pm.setPmBean(bean);

    assertEquals("b1", pm.b1.getPmRelativeName());
  }

}
