package org.pm4j.core.pm.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmBeanCfg;

/**
 *
 * Test using @PmBeanCfg and generic parameter in parallel, but different bean
 * classes, which are not assignable. This test must fail.
 *
 * @author okossak
 *
 */
public class PmBeanBaseDifferentBeanClassesTest {

  DomainObjectPm domainObjectPm = new DomainObjectPm(new PmConversationImpl());
  DomainObjectImpl domainObject1 = new DomainObjectImpl();

  @Test(expected = PmRuntimeException.class)
  @Ignore("FIXME oboede: see PmBeanBase ln. 383")
  public void testPmBeanImpl() {
    assertEquals("bean class must be defined by annotation", DomainObjectImpl.class, domainObjectPm.getPmBeanClass());
  }

  static class AnotherImpl {
  }

  static class DomainObjectImpl {
  }

  @PmBeanCfg(beanClass = AnotherImpl.class)
  static class DomainObjectPm
      extends PmBeanBase<DomainObjectImpl> {

    /** Constructor */
    DomainObjectPm(PmObject parent) {
      super(parent);
    }
  }

}
