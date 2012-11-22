package org.pm4j.core.pm.impl.pathresolver;

import junit.framework.TestCase;

import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.common.expr.ExprExecExeption;
import org.pm4j.common.expr.Expression;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.expr.PathExpressionChain;
import org.pm4j.core.pm.impl.expr.PmExprExecCtxt;

public class PathResolverTest extends TestCase {

  /**
   * Reading the name Attribute without navigation.
   */
  public void testReadFlatFromPojo() {
    Pojo p = new Pojo("head");
    Expression expr = PathExpressionChain.parse("name", true);
    assertEquals("head", expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Writing a new name to Pojo without navigation.
   */
  public void testWriteFlatToPojoPath() {
    Pojo p = new Pojo("head");
    Expression expr = PathExpressionChain.parse("name", true);
    expr.execAssign(new ExprExecCtxt(p), "NewHead");
    assertEquals("NewHead", p.name);
  }

  /**
   * Reading hierarchical field sub.sub.name from Pojo
   */
  public void testReadHierarchicalFromPojoPath() {
    Pojo p = Pojo.make("head", "sub", "subSub");
    Expression expr = PathExpressionChain.parse("sub.sub.name", true);
    assertEquals("subSub", expr.exec(new ExprExecCtxt(p)));
    }

  /**
   * Writing hierarchical field sub.sub.name for Pojo
   */
  public void testWriteHierarchicalToPojoPath() {
    Pojo p = Pojo.make("head", "subName", "subSubName");
    Expression expr = PathExpressionChain.parse("sub.sub.name", true);
    expr.execAssign(new ExprExecCtxt(p), "newValue");
    assertEquals("newValue", p.sub.sub.name);
    }

  /**
   * Reading a named object from PmConversation. Notice the '#' sign at the
   * navigation expression and the PmExprExecCtxt which is a sub class of ExprExecCtxt.
   */
  public void testReadFromPmConversationObject() {
    PmConversation pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myProp", Pojo.make("head", "subName", "subSubName"));
    Expression expr = PathExpressionChain.parse("#myProp.sub.sub.name", true);
    assertEquals("subSubName", expr.exec(new PmExprExecCtxt(pmConversation)));
    }

  /**
   * Reads a not filled optional field from Pojo. The optional is unnecessary,
   * because there is no further call on name, which provokes a NPE. But the
   * expression is allowed.
   */
  public void testReadFlatOptionalFieldWithNullValue() {
    Pojo p = new Pojo(null);
    Expression expr = PathExpressionChain.parse("(o)name", true);
    assertNull(expr.exec(new ExprExecCtxt(p)));
        }

  /**
   * Reads an existing optional field. The optional is unnecessary but allowed.
   */
  public void testReadOptionalHierarchicalField() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("sub.(o)name", true);
    assertEquals("subName", expr.exec(new ExprExecCtxt(p)));
        }

  /**
   * Reads the optional sub element of
   */
  public void testReadOptionalHierarchicalUnsetField() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("sub.(o)sub.name", true);
    assertNull(expr.exec(new ExprExecCtxt(p)));
      }

  // /**
  // * An optional but not existing field throws an Exception.
  // */
  // public void testReadOptionalButNotExistingField() {
  // Pojo p = Pojo.make("head", "subName");
  // Expression expr= PathExpressionChain.parse("sub.(o)notExistingField",
  // true);
  // try {
  // expr.exec(new ExprExecCtxt(p));
  // fail();
  // } catch (ExprExecExeption e) {
  // // TODO: assert e values.
  // }
  // }

  /**
   * Reading a optional existing field, which does not exist, leads to a null
   * value.
   */
  public void testReadOptionalExistingWhichIsNotExistingField() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("sub.(x)notExistingField", true);
    assertNull(expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Reading the name of an optional and not set sub element does not lead to a
   * NullPointerException.
   */
  public void testReadOptionalAndNonExistingFieldPrventNpe() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("sub.(o)sub.name", true);
    assertNull(expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Calls the getSubMethod() of the "head" Pojo and reads the name of the sub
   * element called subName.
   */
  public void testCallMethodAndReadExistingAttribut() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("getSubMethod().name", true);
    assertEquals("subName", expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Calls the getSubMethod() of the "head" and a getSubMethod() of the subName
   * element returning the subSub element. The name is subSubName.
   */
  public void testCallsAChainedMethodAndReadExistingAttribut() {
    Pojo p = Pojo.make("head", "subName", "subSubName");
    Expression expr = PathExpressionChain.parse("getSubMethod().getSubMethod().name", true);
    assertEquals("subSubName", expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Calls the getSubMethod() of the "head" which returns the subName element.
   * Calling getSubMethod() on the subName Element results in null. Resolving
   * the name attribut would lead to a NullPointerExeption, but the sub element
   * of subName is optional.
   */
  public void testCallsAChainedMethodAndReadExistingAttribut2() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("getSubMethod().(o)getSubMethod().name", true);
    assertNull(expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Calls the getSubMethod() of the "head" Pojo and reads the name of the sub
   * element, the field exists, but the name value is null
   */
  public void testCallMethodAndReadExistingAttributWithNullValue() {
    Pojo p = Pojo.make("head", null);
    Expression expr = PathExpressionChain.parse("getSubMethod().name", true);
    assertNull(expr.exec(new ExprExecCtxt(p)));
  }

  // /**
  // * Calling an optional but not existing method should throw an
  // ExprExecExeption.
  // */
  // public void testCallOptionalMethodReturnValueButMethodDoesNotExist() {
  // Pojo p = Pojo.make("head");
  // Expression expr = PathExpressionChain.parse("(o)nonExistingMethod().name",
  // true);
  // try {
  // expr.exec(new ExprExecCtxt(p));
  // fail();
  // } catch (ExprExecExeption e) {
  // // TODO: Read assert exception properties.
  // }
  // }
  //
  // /**
  // * Calling an optional existing method which does not exist,
  // */
  // public void testCallExistingOptionalMethod() {
  // Pojo p = Pojo.make("head");
  // Expression expr = PathExpressionChain.parse("(x)nonExistingMethod().name",
  // true);
  // assertNull(expr.exec(new ExprExecCtxt(p)));
  // }

  /**
   * Calling an optional or optional existing method which does not exist, should 
   * result in a null value.
   */
  public void testCallNotExistingOptionalOrOptionalExistingMethod() {
    Pojo p = Pojo.make("head");
    Expression expr = PathExpressionChain.parse("(x,o)nonExistingMethod().name", true);
    assertNull(expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Calling an optional or optional existing method which exists, but the method return value
   * is null and should not throw a NullPointerException.
   */
  public void testCallExistingOptionalMethod() {
    Pojo p = Pojo.make("head");
    Expression expr = PathExpressionChain.parse("(x,o)getSubMethod().name", true);
    assertNull(expr.exec(new ExprExecCtxt(p)));
  }

  
  /**
   * It is possible to pass arguments but the scope is fixed to this, the same object.
   */
  public void testCallMethodWithParam() {
    Expression expr = PathExpressionChain.parse("addAPlus(this.name)", true);
    assertEquals("myName+", expr.exec(new ExprExecCtxt(new Pojo("myName"))));
  }

  public void testConcatStrings() {
    Expression expr = PathExpressionChain.parse(
        "'Name of head instance: ' + name + '. Name of sub instance: ' + sub.name + '.'", true);
    assertEquals("Name of head instance: head. Name of sub instance: sub.",
        expr.exec(new ExprExecCtxt(Pojo.make("head", "sub"))));
  }

}
