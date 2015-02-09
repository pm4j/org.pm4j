package org.pm4j.core.pm.impl;

import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmBeanCfg;

/**
 * 
 * Test using @PmBeanCfg and generic parameter in parallel, but generic
 * parameter is an interface, and annotation parameter a specific class,
 * implementing the interface. This usecase is allowed.
 * 
 * @author okossak
 * 
 */
public class PmBeanBaseAssignableBeanClassesTest {

  DomainObjectPm domainObjectPm = new DomainObjectPm(new PmConversationImpl());
  DomainObjectImpl domainObject1 = new DomainObjectImpl();

  @Test
  public void testPmBeanImpl() {
    assertEquals("bean class must be defined by annotation", DomainObjectImpl.class, domainObjectPm.getPmBeanClass());

    domainObjectPm.setPmBean(domainObject1);
    assertSame("bean must be same", domainObject1, domainObjectPm.getPmBean());
  }

  static interface DomainObject {
  }

  static class DomainObjectImpl
      implements DomainObject {
  }

  @PmBeanCfg(beanClass = DomainObjectImpl.class)
  static class DomainObjectPm
      extends PmBeanBase<DomainObject> {

    /** Constructor */
    DomainObjectPm(PmObject parent) {
      super(parent);
    }
  }

}
