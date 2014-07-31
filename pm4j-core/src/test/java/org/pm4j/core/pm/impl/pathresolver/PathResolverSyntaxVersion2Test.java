package org.pm4j.core.pm.impl.pathresolver;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.pm4j.common.expr.ExprExecExeption;
import org.pm4j.common.expr.Expression;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.expr.PathExpressionChain;
import org.pm4j.core.pm.impl.expr.PmExprExecCtxt;

public class PathResolverSyntaxVersion2Test extends PathResolverTest {

  /**
   * Calling an optional but not existing method should throw an
   * ExprExecExeption.
   */
  @Test
  public void testCallOptionalMethodButMethodDoesNotExist() {
    Pojo p = Pojo.make("head");
    Expression expr = PathExpressionChain.parse("(o)nonExistingMethod().notExistingField");
    try {
      expr.getValue(p);
      fail();
    } catch (ExprExecExeption e) {
      assertTrue(e.getMessage().startsWith(
          "Method 'nonExistingMethod' not found in class: org.pm4j.core.pm.impl.pathresolver.Pojo"));
    }
  }

  /**
   * VERSION_2 (Strict) does not try to resolve myProp with the PmConversation
   * if the hash sign in front of the expression path is missing.
   */
  @Test
  public void testStrictStyleReadFromPmConversationObject() {
    PmConversation pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myProp", Pojo.make("head"));
    Expression expr = PathExpressionChain.parse("myProp.name");
    try {
      expr.exec(new PmExprExecCtxt(pmConversation));
      fail();
    } catch (ExprExecExeption e) {
      assertTrue(e.getMessage().startsWith("Unable to resolve expression part 'myProp'"));
    }
  }

  /**
   * Checks if in strict mode the optional field breaks
   */
  @Test
  public void testStrictStyleOptionalField() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("sub.(o)notExistingField");
    try {
      expr.getValue(p);
      fail();
    } catch (ExprExecExeption e) {
      assertTrue(e.getMessage().startsWith("Unable to resolve expression part '(o)notExistingField'."));
    }
  }

  /**
   * Checks if in strict mode the optional method breaks
   */
  @Test
  public void testStrictStyleOptionalMethod() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("sub.(o)notExistingMethod()");
    try {
      expr.getValue(p);
      fail();
    } catch (ExprExecExeption e) {
      assertTrue(e.getMessage().startsWith(
          "Method 'notExistingMethod' not found in class: org.pm4j.core.pm.impl.pathresolver.Pojo"));
    }
  }

}
