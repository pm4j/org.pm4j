package org.pm4j.core.pm.impl;

import org.junit.Test;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmObjectCfg;
import org.pm4j.core.pm.annotation.PmObjectCfg.Visible;
import org.pm4j.core.pm.impl.PmObjectCfgWithPmBeanImpls2Test.Bean;
import org.pm4j.tools.test.PmAssert;

/**
 * Test @PmObjectCfg annotation effects on PmBeans
 *
 * @author JHETMANS
 *
 */
public class PmObjectCfgWithPmBeanImplsTest {

  private TestPm testPm = new TestPm();

  @Test
  public void testBeanPmImplWithValue() {
    PmAssert.assertNotVisible(testPm.pmBeanImpl);
  }

  @Test
  public void testBeanPmImplWithoutValue() {
    testPm.pmBeanImpl.setPmBean(new Bean());

    PmAssert.assertVisible(testPm.pmBeanImpl);
  }

  private static class TestPm extends PmConversationImpl {
    
    @PmBeanCfg(beanClass = Bean.class)
    @PmObjectCfg(visible = Visible.IF_NOT_EMPTY)
    public final PmBean<Bean> pmBeanImpl = new PmBeanImpl<Bean>(this);
  }
}
