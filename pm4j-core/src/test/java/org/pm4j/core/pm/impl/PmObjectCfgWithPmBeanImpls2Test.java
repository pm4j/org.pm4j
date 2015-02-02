package org.pm4j.core.pm.impl;

import org.junit.Test;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg2;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Cache;
import org.pm4j.core.pm.annotation.PmObjectCfg;
import org.pm4j.core.pm.annotation.PmObjectCfg.Visible;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.tools.test.PmAssert;

/**
 * Test @PmObjectCfg annotation effects on PmBeans
 *
 * @author JHETMANS
 *
 */
public class PmObjectCfgWithPmBeanImpls2Test {

  private TestPm testPm = new TestPm();

  @Test
  public void testBeanPmImpl2WithoutValue() {
    PmAssert.assertNotVisible(testPm.pmBeanImpl);
  }

  @Test
  public void testBeanPmImpl2WithValue() {
    testPm.pmBeanImpl.setPmBean(new Bean());
    PmAssert.assertVisible(testPm.pmBeanImpl);
  }

  private static class TestPm extends PmConversationImpl {

    @PmBeanCfg(beanClass = Bean.class)
    @PmObjectCfg(visible = Visible.IF_NOT_EMPTY)
    @PmCacheCfg2(@Cache(property = CacheKind.VALUE))
    public final PmBean<Bean> pmBeanImpl = new PmBeanImpl2<Bean>(this);
  }

  public static class Bean {
  }
}
