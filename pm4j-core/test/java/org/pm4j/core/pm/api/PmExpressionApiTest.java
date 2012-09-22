package org.pm4j.core.pm.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;

public class PmExpressionApiTest {

  private ConversationPm cPm;

  @Before
  public void setUp() {
    cPm = new ConversationPm();
    cPm.s.setValue("abc");
  }

  @Test
  public void testSimpleNavigatingExpr() {
    Assert.assertEquals("abc", PmExpressionApi.getByExpression(cPm.child1.child2, "pmParent.pmParent.s.value"));
  }

  @Test
  public void testOptionalExpr() {
    Assert.assertEquals(null, PmExpressionApi.findByExpression(cPm, "s.(o)pmTooltip.andSomethingThatDoesntExistCantBeDetectedAfterANullValue"));
  }

  @Test
  public void testExistsOptionallyExpr() {
    Assert.assertEquals(null, PmExpressionApi.findByExpression(cPm, "s.(x)somethingThatDoesntExist"));
    // TODO olaf: the current 'o' implementation als supports the semantic of 'x'.
    //            a configuration should allow to switch between strict and non-strict mode.
    Assert.assertEquals(null, PmExpressionApi.findByExpression(cPm, "s.(o)somethingThatDoesntExist"));
  }

  @Test
  @Ignore("The * modifier is not yet implemented.")
  public void testRepeatedExpr() {
    Assert.assertEquals(null, PmExpressionApi.findByExpression(cPm.child1.child2, "(*)pmParent.s.value"));
  }


  class ConversationPm extends PmConversationImpl {
    public final PmAttrString s = new PmAttrStringImpl(this);
    public final Child1Pm child1 = new Child1Pm(this);
  }
  class Child1Pm extends PmElementImpl {
    public final Child2Pm child2 = new Child2Pm(this);

    public Child1Pm(PmObject parentPm) {
      super(parentPm);
    }
  }
  class Child2Pm extends PmElementImpl {
    public Child2Pm(PmObject parentPm) {
      super(parentPm);
    }
  }

}
