package org.pm4j.core.pm.impl;

import org.junit.Test;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmBeanBaseAssignableBeanClassesTest.DomainObjectImpl;

/**
 * 
 * Test without @PmBeanCfg and without generic parameter. This test must fail.
 * 
 * @author okossak
 * 
 */
public class PmBeanBaseMissingGenericTest {

  DomainObjectPm domainObjectPm = new DomainObjectPm(new PmConversationImpl());
  DomainObjectImpl domainObject1 = new DomainObjectImpl();

  @Test(expected = PmRuntimeException.class)
  public void testPmBeanBaseNested() {

    // call a method to invoke initialization
    domainObjectPm.getPmBeanClass();
  }

  // The bean without @PmBeanCfg and without generic parameter
  @SuppressWarnings("rawtypes")
  static class DomainObjectPm
      extends PmBeanBase {

    /** Constructor */
    DomainObjectPm(PmObject parent) {
      super(parent);
    }
  }
}
