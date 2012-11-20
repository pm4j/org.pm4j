package org.pm4j.core.pm.impl;

import junit.framework.TestCase;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.api.PmFactoryApi;

public class PmElementFactoryImplTest extends TestCase {

  static class A {};
  static class B extends A {};
  static class C extends A {};
  static class D extends C {};

  @PmBeanCfg(beanClass=A.class)
  public static class APm extends PmBeanBase<A> { }
  @PmBeanCfg(beanClass=B.class)
  public static class BPm extends PmBeanBase<B> { }
  @PmBeanCfg(beanClass=C.class)
  public static class CPm extends PmBeanBase<C> { }

  public void testFactoryMapping() {
    PmConversation s = new PmConversationImpl(APm.class, BPm.class, CPm.class);

    assertEquals(APm.class, PmFactoryApi.getPmForBean(s, new A()).getClass());
    assertEquals(BPm.class, PmFactoryApi.getPmForBean(s, new B()).getClass());
    assertEquals(CPm.class, PmFactoryApi.getPmForBean(s, new C()).getClass());
    assertEquals(CPm.class, PmFactoryApi.getPmForBean(s, new D()).getClass());
//    assertEquals(APm.class, s.getPmForBean(new A()).getClass());
  }

}
