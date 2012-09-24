package org.pm4j.core.pm.api;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmLabel;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;
import org.pm4j.core.pm.impl.PmInitApi;
import org.pm4j.core.pm.impl.PmLabelImpl;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;
import org.pm4j.core.pm.impl.pathresolver.PmExpressionPathResolver;

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
    // TODO olaf: the current 'o' implementation als supports the semantic of 'x'.
    //            a configuration should allow to switch between strict and non-strict mode.
    assertEquals(null, PmExpressionApi.findByExpression(testPm, "s.(o)somethingThatDoesntExist"));
  }

  @Test
  public void testReadRepeatedExpr() {
    assertEquals(testPm.s, PmExpressionApi.getByExpression(testPm.child1.child2.aLabel, "(*)pmParent.s"));
    assertEquals("abc", PmExpressionApi.findByExpression(testPm.child1.child2.aLabel, "(*)pmParent.s.value"));
  }

  @Test
  public void testPmInjectUsingRepeatedExpression() {
    // Please consider: @PmInject provides values only after PM initialization!
    PmInitApi.ensurePmInitialization(testPm.child1.child2);

    assertEquals("@PmInject provides references to parent context information.", testPm.aDialogPm, testPm.child1.child2.refToADialogPm);
  }

  @Test
  public void testSetRepeatedExpr() {
    // TODO: add set method to api
    PathResolver pr = PmExpressionPathResolver.parse("(*)pmParent.s.value", true);
    pr.setValue(testPm.child1.child2.aLabel, "hi");
    assertEquals("hi", testPm.s.getValue());
  }

  // -- The testee classes

  /** A PM with some test member sub-structure. */
  class TestConversationPm extends PmConversationImpl {
    public final PmAttrString s = new PmAttrStringImpl(this);
    public final Child1Pm child1 = new Child1Pm(this);
    public final PmElement aDialogPm = new PmElementImpl(this);
  }
  class Child1Pm extends PmElementImpl {
    public final Child2Pm child2 = new Child2Pm(this);

    public Child1Pm(PmObject parentPm) {
      super(parentPm);
    }
  }
  class Child2Pm extends PmElementImpl {
    public final PmLabel aLabel = new PmLabelImpl(this);
    // TODO: allow property syntax by making variable usage explicite.
//    @PmInject("(*)pmParent.aDialogPm")
    @PmInject("(*)getPmParent().aDialogPm")
    private PmElement refToADialogPm;

    public Child2Pm(PmObject parentPm) {
      super(parentPm);
    }
  }
}
