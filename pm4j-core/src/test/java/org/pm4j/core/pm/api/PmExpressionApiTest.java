package org.pm4j.core.pm.api;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmObjectBase;

import static org.junit.Assert.assertEquals;

public class PmExpressionApiTest {

  private TestConversationPm testPm;

  @Before
  public void setUp() {
    testPm = new TestConversationPm();
    testPm.s.setValue("abc");
  }

  @Test
  public void testSimpleNavigatingExpr() {
    assertEquals("abc", PmExpressionApi.getByExpression(testPm.child1.child2, "pmParent.pmParent.s.value"));
  }

  @Test
  public void testOptionalExpr() {
    assertEquals(null, PmExpressionApi.findByExpression(testPm, "s.(o)pmTooltip.andSomethingThatDoesntExistCantBeDetectedAfterANullValue"));
  }

  @Test
  public void testExistsOptionallyExpr() {
    assertEquals(null, PmExpressionApi.findByExpression(testPm, "s.(x)somethingThatDoesntExist"));
    assertEquals(null, PmExpressionApi.findByExpression(testPm, "s.(x,o)somethingThatDoesntExist"));
  }

  // -- The testee classes

  /** A PM with some test member sub-structure. */
  class TestConversationPm extends PmConversationImpl {
    public final PmAttrString s = new PmAttrStringImpl(this);
    public final Child1Pm child1 = new Child1Pm(this);
    public final PmObject aDialogPm = new PmObjectBase(this);
  }
  class Child1Pm extends PmObjectBase {
    public final Child2Pm child2 = new Child2Pm(this);

    public Child1Pm(PmObject parentPm) {
      super(parentPm);
    }
  }
  class Child2Pm extends PmObjectBase {
    public final PmObject aLabel = new PmObjectBase(this);
    public Child2Pm(PmObject parentPm) {
      super(parentPm);
    }
  }
}
