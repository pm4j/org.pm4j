package org.pm4j.core.pm.impl;

import org.junit.Test;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg2;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Cache;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Clear;
import org.pm4j.core.pm.annotation.PmObjectCfg;
import org.pm4j.core.pm.annotation.PmObjectCfg.Visible;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.tools.test._PmAssert;

/**
 * Test @PmObjectCfg annotation effects on PmBeans
 *
 * @author JHETMANS
 *
 */
public class PmObjectCfgWithPmBeansTest {

  private TestPm testPm = new TestPm();


  @Test
  public void testBeanPmImpl2WithoutValue() {
    _PmAssert.assertNotVisible(testPm.beanPm);
  }

  @Test
  public void testBeanPmImpl2WithValue() {
    testPm.beanPm.setPmBean(new Bean());
    _PmAssert.assertVisible(testPm.beanPm);
  }

  @Test
  public void testBeanPmImplWithValue() {
    _PmAssert.assertNotVisible(testPm.pmBeanImpl);
  }

  @Test
  public void testBeanPmImplWithoutValue() {
    testPm.pmBeanImpl.setPmBean(new Bean());

    _PmAssert.assertVisible(testPm.pmBeanImpl);
  }

  private static class TestPm extends PmConversationImpl {
    private boolean isReadOnly = false;

    @Override
    protected boolean isPmReadonlyImpl() {
      return isReadOnly;
    }

    @PmBeanCfg(beanClass = Bean.class)
    @PmObjectCfg(visible = Visible.IF_NOT_EMPTY)
    // FIXME oboede: it should be possible to assign a value if no clear=NEVER is configured.
    @PmCacheCfg2(@Cache(property = CacheKind.VALUE, clear=Clear.NEVER))
    public final PmBeanImpl2<Bean> beanPm = new PmBeanImpl2<Bean>(this);

    // TODO: move to a separate test class.
    @PmBeanCfg(beanClass = Bean.class)
    @PmObjectCfg(visible = Visible.IF_NOT_EMPTY)
    public final PmBean<Bean> pmBeanImpl = new PmBeanBase<Bean>(this);
  }

  public static class Bean {
    public Bean() {
    }
  }
}
