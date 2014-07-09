package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrPmList;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.api.PmFactoryApi;


public class BeanPmCacheTest {

  @Before
  public void setUp() {
    MyBean.finalizeWasCalled = false;
    MyBeanPm.finalizeWasCalled = false;
  }

  @Test
  public void testForMemoryLeakOnReleasingBeanAndPmReferences() throws InterruptedException {
    MyConversation conversation = new MyConversation();
    MyBean bean = new MyBean();
    MyBeanPm beanPm = PmFactoryApi.getPmForBean(conversation, bean);

    Assert.assertEquals(bean, beanPm.getPmBean());
    Assert.assertFalse(MyBean.finalizeWasCalled);
    Assert.assertFalse(MyBeanPm.finalizeWasCalled);

    bean = null;
    beanPm = null;

    System.gc();

    // finalization gets called by a parallel thread after the synchronous gc() call.
    Thread.sleep(3);

    Assert.assertTrue("MyBean should have been finalized.", MyBean.finalizeWasCalled);
    Assert.assertTrue("MyBeanPm should have been finalized.", MyBeanPm.finalizeWasCalled);
  }

  @Test
  public void testForMemoryLeakOnReleasingPmReferences() throws InterruptedException {
    MyConversation conversation = new MyConversation();
    MyBean bean = new MyBean();
    MyBeanPm beanPm = PmFactoryApi.getPmForBean(conversation, bean);

    Assert.assertEquals(bean, beanPm.getPmBean());
    Assert.assertFalse(MyBean.finalizeWasCalled);
    Assert.assertFalse(MyBeanPm.finalizeWasCalled);

    beanPm = null;

    System.gc();

    // finalization gets called by a parallel thread after the synchronous gc() call.
    Thread.sleep(100);

    Assert.assertFalse("MyBean should not have been finalized.", MyBean.finalizeWasCalled);
    Assert.assertTrue("MyBeanPm should have been finalized.", MyBeanPm.finalizeWasCalled);
  }

  @Test
  public void testForMemoryLeakWithPmListAttr() throws InterruptedException {
    MyConversation conversation = new MyConversation();

    Assert.assertFalse(MyBean.finalizeWasCalled);

    conversation.beanList.add(new MyBean());
    Assert.assertEquals(1, conversation.pmList.getValue().size());

    Assert.assertFalse(MyBean.finalizeWasCalled);
    Assert.assertFalse(MyBeanPm.finalizeWasCalled);

    conversation.beanList.clear();

    System.gc();

    // finalization gets called by a parallel thread after the synchronous gc() call.
    Thread.sleep(3);

    Assert.assertEquals(0, conversation.pmList.getValue().size());

    Assert.assertTrue("MyBean should have been finalized.", MyBean.finalizeWasCalled);
    Assert.assertTrue("MyBeanPm should have been finalized.", MyBeanPm.finalizeWasCalled);
  }

  @Test
  public void testForMemoryLeakWithPmListAttrWithExtraBeanReference() throws InterruptedException {
    MyConversation conversation = new MyConversation();

    Assert.assertFalse(MyBean.finalizeWasCalled);

    MyBean extraBeanReference = new MyBean();
    conversation.beanList.add(extraBeanReference);
    Assert.assertEquals(1, conversation.pmList.getValue().size());

    Assert.assertFalse(MyBean.finalizeWasCalled);
    Assert.assertFalse(MyBeanPm.finalizeWasCalled);

    conversation.beanList.clear();

    System.gc();

    // finalization gets called by a parallel thread after the synchronous gc() call.
    Thread.sleep(3);

    Assert.assertEquals(0, conversation.pmList.getValue().size());

    Assert.assertFalse("MyBean should have been finalized.", MyBean.finalizeWasCalled);
    Assert.assertTrue("MyBeanPm should have been finalized.", MyBeanPm.finalizeWasCalled);
  }

  @Test
  public void testPreserveChangedChangedStateOfListItems() throws InterruptedException {
    MyConversation conversation = new MyConversation();

    conversation.beanList.add(new MyBean());

    MyBeanPm beanPm = conversation.pmList.getValue().get(0);
    beanPm.s.setValue("Hi!");

    Assert.assertTrue(beanPm.s.isPmValueChanged());

    Assert.assertFalse(MyBean.finalizeWasCalled);
    Assert.assertFalse(MyBeanPm.finalizeWasCalled);

    // release the extra reference before gc()
    beanPm = null;

    System.gc();

    // finalization gets called by a parallel thread after the synchronous gc() call.
    Thread.sleep(3);

    beanPm = conversation.pmList.getValue().get(0);

    Assert.assertEquals("Hi!", beanPm.s.getValue());
    Assert.assertTrue(beanPm.s.isPmValueChanged());

//    Assert.assertFalse("MyBean should have been finalized.", MyBean.finalizeWasCalled);
//    Assert.assertTrue("MyBeanPm should have been finalized.", MyBeanPm.finalizeWasCalled);
  }




  public static class MyBean {
    public String s;

    static boolean finalizeWasCalled = false;

    @Override
    protected void finalize() throws Throwable {
      finalizeWasCalled = true;
    }
  }

  @PmBeanCfg(beanClass=MyBean.class)
  public static class MyBeanPm extends PmBeanBase<MyBean> {
    public final PmAttrString s = new PmAttrStringImpl(this);

    static boolean finalizeWasCalled = false;

    @Override
    protected void finalize() throws Throwable {
      finalizeWasCalled = true;
    }
  }

  @PmFactoryCfg(beanPmClasses=MyBeanPm.class)
  public static class MyConversation extends PmConversationImpl {
    List<MyBean> beanList = new ArrayList<BeanPmCacheTest.MyBean>();

    @PmFactoryCfg(beanPmClasses=MyBeanPm.class)
    public final PmAttrPmList<MyBeanPm> pmList = new PmAttrPmListImpl<MyBeanPm, MyBean>(this) {
      protected Collection<MyBean> getBackingValueImpl() {
        return beanList;
      }
    };
  }

}
