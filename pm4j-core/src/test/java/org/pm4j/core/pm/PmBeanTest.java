package org.pm4j.core.pm;

import org.junit.Assert;
import org.junit.Test;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmBeanTest {

  private PmConversation pmConversation = new PmConversationImpl();

  @Test
  public void testBeanAttributeAccess() {
    Bean bean = new Bean();
    BeanPm pm = new BeanPm(pmConversation, bean);

    bean.i = 3;
    Assert.assertEquals("Initial bean value should be accessible.", 3, pm.i.getValue().intValue());
  }


  @Test
  public void testMissingBeanAttributeErrorMessage() {
    Bean bean = new Bean();
    BeanPmWithUnmappedAttribute pm = new BeanPmWithUnmappedAttribute(pmConversation, bean);
    try {
      pm.attrWithoutMatchingBeanAttr.getPmTitle();
    }
    catch (PmRuntimeException e) {
      Assert.assertEquals(true, e.getMessage().startsWith("Unable to access field 'attrWithoutMatchingBeanAttr'"));
    }
  }

  @Test
  public void testBeanPmWithIllegalPmAccessInConstructor() {
    try {
      new BeanPmWithIllegalPmAccessInConstructor(pmConversation, new Bean());
    }
    catch (PmRuntimeException e) {
      Assert.assertEquals(true, e.getMessage().startsWith("Unable to access field 'attrWithoutMatchingBeanAttr'"));
    }
  }
  
  public static class Bean {
    public int i;
  }

  @PmBeanCfg(beanClass=Bean.class)
  public static class BeanPm extends PmBeanBase<Bean>{
    public PmAttrInteger i = new PmAttrIntegerImpl(this);

    public BeanPm(PmObject parentPm, Bean b) {
      super(parentPm, b);
    }
  }

  @PmBeanCfg(beanClass=Bean.class)
  public static class BeanPmWithUnmappedAttribute extends PmBeanBase<Bean> {
    public PmAttrString attrWithoutMatchingBeanAttr = new PmAttrStringImpl(this);

    public BeanPmWithUnmappedAttribute(PmObject parentPm, Bean b) {
      super(parentPm, b);
    }
  }

  @PmBeanCfg(beanClass=Bean.class)
  public static class BeanPmWithIllegalPmAccessInConstructor extends PmBeanBase<Bean> {
    public PmAttrString attrWithoutMatchingBeanAttr = new PmAttrStringImpl(this);

    public BeanPmWithIllegalPmAccessInConstructor(PmObject parentPm, Bean b) {
      super(parentPm, b);
      attrWithoutMatchingBeanAttr.getValue();
    }
  }


}
