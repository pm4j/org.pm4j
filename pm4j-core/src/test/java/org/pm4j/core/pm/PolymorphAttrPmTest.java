package org.pm4j.core.pm;

import org.junit.Test;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmAttrCfg.AttrAccessKind;
import org.pm4j.core.pm.impl.*;

import static org.junit.Assert.assertEquals;
import static org.pm4j.tools.test._PmAssert.setValueAsString;

/**
 * Tests the an attribute is generated dynamically to support different value types, depending on some business condition.
 * <p>
 * This feature allows to generate PM having a flexible type structure.
 *
 * @author Olaf Boede
 */
public class PolymorphAttrPmTest {

  PmConversation convPm = new PmConversationImpl();
  TestItemPm pmWithStringAttr = PmInitApi.initPmTree(new TestItemPm(convPm, new TestBean(false)));
  TestItemPm pmWithIntAttr = PmInitApi.initPmTree(new TestItemPm(convPm, new TestBean(true)));

  @Test
  public void testStringAttr() {
    assertEquals(String.class, pmWithStringAttr.polymorphAttr.getValueClass());
    setValueAsString(pmWithStringAttr.polymorphAttr, "1");
    assertEquals("1", pmWithStringAttr.polymorphAttr.getValue());
  }

  @Test
  public void testIntegerAttr() {
    assertEquals(Integer.class, pmWithIntAttr.polymorphAttr.getValueClass());
    setValueAsString(pmWithIntAttr.polymorphAttr, "1");
    assertEquals(new Integer(1), pmWithIntAttr.polymorphAttr.getValue());
  }

  @Test
  public void testBothAttrTypes() {
    assertEquals(Integer.class, pmWithIntAttr.polymorphAttr.getValueClass());
    assertEquals(String.class, pmWithStringAttr.polymorphAttr.getValueClass());

    setValueAsString(pmWithIntAttr.polymorphAttr, "1");
    setValueAsString(pmWithStringAttr.polymorphAttr, "1");

    assertEquals(new Integer(1), pmWithIntAttr.polymorphAttr.getValue());
    assertEquals("1", pmWithStringAttr.polymorphAttr.getValue());
  }

  public static class TestBean {
    boolean makeIntAttr;
    public TestBean(boolean makeIntAttr) {
      this.makeIntAttr = makeIntAttr;
    }
  }

  public static class TestItemPm extends PmBeanBase<TestBean> {
    @PmAttrCfg(accessKind=AttrAccessKind.LOCALVALUE)
    public final PmAttr<?> polymorphAttr = getPmBean().makeIntAttr
        ? new PmAttrIntegerImpl(this)
        : new PmAttrStringImpl(this);

    public TestItemPm(PmObject parentPm, TestBean bean) {
      super(parentPm, bean);
    }
  }

}
