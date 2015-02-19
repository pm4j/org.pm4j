package org.pm4j.core.pm.impl;

import org.junit.Test;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmObjectCfg;
import org.pm4j.core.pm.annotation.PmObjectCfg.Visible;
import org.pm4j.core.pm.impl.PmObjectCfgWithPmBeanImpls2Test.Bean;
import org.pm4j.tools.test._PmAssert;

/**
 * Test @PmObjectCfg annotation effects on PmBeans
 *
 * @author JHETMANS
 *
 */
public class PmObjectCfgWithPmBeanImplsTest {

  private HelperParentPm pm = new HelperParentPm();

  @Test
  public void testBeanPmImplWithValue() {
    _PmAssert.assertNotVisible(pm.pmBeanImpl);
  }

  @Test
  public void testBeanPmImplWithoutValue() {
    pm.pmBeanImpl.setPmBean(new Bean());

    _PmAssert.assertVisible(pm.pmBeanImpl);
  }

  private static class HelperParentPm extends PmConversationImpl {
    
    @PmBeanCfg(beanClass = Bean.class)
    @PmObjectCfg(visible = Visible.IF_NOT_EMPTY)
    public final PmBean<Bean> pmBeanImpl = new PmBeanBase<Bean>(this);
  }
}
