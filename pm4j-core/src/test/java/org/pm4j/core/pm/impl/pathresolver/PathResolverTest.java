package org.pm4j.core.pm.impl.pathresolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.common.expr.ExprExecExeption;
import org.pm4j.common.expr.Expression;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.expr.PathExpressionChain;
import org.pm4j.core.pm.impl.expr.PmExprExecCtxt;

public class PathResolverTest {

  /**
   * Reading the name Attribute without navigation.
   */
  @Test
  public void testReadFlatFromPojo() {
    assertEquals("head", PathExpressionChain.parse("name").getValue(new Pojo("head")));
  }

  /**
   * Writing a new name to Pojo without navigation.
   */
  @Test
  public void testWriteFlatToPojoPath() {
    Pojo p = new Pojo("head");
    Expression expr = PathExpressionChain.parse("name");
    expr.execAssign(new ExprExecCtxt(p), "NewHead");
    assertEquals("NewHead", p.name);
  }

  /**
   * Reading hierarchical field sub.sub.name from Pojo
   */
  @Test
  public void testReadHierarchicalFromPojoPath() {
    Pojo p = Pojo.make("head", "sub", "subSub");
    Expression expr = PathExpressionChain.parse("sub.sub.name");
    assertEquals("subSub", expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Writing hierarchical field sub.sub.name for Pojo
   */
  @Test
  public void testWriteHierarchicalToPojoPath() {
    Pojo p = Pojo.make("head", "subName", "subSubName");
    Expression expr = PathExpressionChain.parse("sub.sub.name");
    expr.execAssign(new ExprExecCtxt(p), "newValue");
    assertEquals("newValue", p.sub.sub.name);
  }

  /**
   * Trying to write hierarchical field sub.sub.name for Pojo. But there is only
   * sub.name
   */
  @Test
  public void testWriteHierarchicalToPojoPathWithMissingElement() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("sub.sub.name");
    try {
      expr.execAssign(new ExprExecCtxt(p), "newValue");
      fail();
    } catch (ExprExecExeption e) {
      assertTrue(e.getMessage().startsWith("Mandatory expression returns 'null'."));
    }
  }

  /**
   * Writing hierarchical field sub.(o)sub.name for Pojo. The optional part is
   * not existing an should not end in an exception.
   */
  @Test
  public void testWriteHierarchicalToOptionalNotExistingPojoPath() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("sub.(o)sub.name");
    expr.execAssign(new ExprExecCtxt(p), "newValue");
    assertEquals("subName", p.sub.name);
  }

  /**
   * Reading a named object from PmConversation. Notice the '#' sign at the
   * navigation expression and the PmExprExecCtxt which is a sub class of
   * ExprExecCtxt.
   */
  @Test
  public void testReadFromPmConversationObject() {
    PmConversation pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myProp", Pojo.make("head", "subName", "subSubName"));
    Expression expr = PathExpressionChain.parse("#myProp.sub.sub.name");
    assertEquals("subSubName", expr.exec(new PmExprExecCtxt(pmConversation)));
  }

  /**
   * Reading named Object with optional field, but the object does not exist.
   */
  @Test
  public void testReadFromPmConversationObjectWithOptionalFieldNegativ() {
    Pojo p = Pojo.make("head");
    PmConversation pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myProp", p);
    Expression expr = PathExpressionChain.parse("#myProp.(o)sub.name");
    assertNull(expr.exec(new PmExprExecCtxt(pmConversation)));
  }

  /**
   * Reading named Object with optional field. But the object exists.
   */
  @Test
  public void testReadFromPmConversationObjectWithOptionalFieldPositv() {
    Pojo p = Pojo.make("head", "subName");
    PmConversation pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myProp", p);
    Expression expr = PathExpressionChain.parse("#myProp.(o)sub.name");
    assertEquals("subName", expr.exec(new PmExprExecCtxt(pmConversation)));
  }

  /**
   * Reading named Object with optional method. But the object does not exist.
   */
  @Test
  public void testReadFromPmConversationObjectWithOptionalMethodNegativ() {
    Pojo p = Pojo.make("head");
    PmConversation pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myProp", p);
    Expression expr = PathExpressionChain.parse("#myProp.(o)getSubMethod().name");
    assertNull(expr.exec(new PmExprExecCtxt(pmConversation)));
  }

  /**
   * Reading named Object with optional method. But the object exists.
   */
  @Test
  public void testReadFromPmConversationObjectWithOptionalMethodPositv() {
    Pojo p = Pojo.make("head", "subName");
    PmConversation pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myProp", p);
    Expression expr = PathExpressionChain.parse("#myProp.(o)getSubMethod().name");
    assertEquals("subName", expr.exec(new PmExprExecCtxt(pmConversation)));
  }

  /**
   * Reading named Object with existing optional method. But the object exists.
   */
  @Test
  public void testReadFromPmConversationObjectWithExistingOptionalMethodPositv() {
    Pojo p = Pojo.make("head", "subName");
    PmConversation pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myProp", p);
    Expression expr = PathExpressionChain.parse("#myProp.(x)getSubMethod().name");
    assertEquals("subName", expr.exec(new PmExprExecCtxt(pmConversation)));
  }

  /**
   * Reading named Object with not existing optional method. But the method does
   * not exist.
   */
  @Test
  public void testReadFromPmConversationObjectWithExistingOptionalMethodNegativ() {
    Pojo p = Pojo.make("head", "subName");
    PmConversation pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myProp", p);
    Expression expr = PathExpressionChain.parse("#myProp.(x)getNotExistingMethod().notExistingField");
    assertNull(expr.exec(new PmExprExecCtxt(pmConversation)));
  }

  /**
   * Reading named Object with not existing method. The method does not exist.
   */
  @Test
  public void testReadFromPmConversationObjectWithNotExistingOptionalMethodNegativ() {
    Pojo p = Pojo.make("head", "subName");
    PmConversation pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myProp", p);
    Expression expr = PathExpressionChain.parse("#myProp.getNotExistingMethod()");
    try {
      expr.exec(new PmExprExecCtxt(pmConversation));
      fail();
    } catch (ExprExecExeption e) {
      assertTrue(e.getMessage().startsWith(
          "Method 'getNotExistingMethod' not found in class: org.pm4j.core.pm.impl.pathresolver.Pojo"));
    }
  }

  /**
   * Reading named Object with optional method.
   */
  @Test
  public void testReadFromPmConversationObjectWithNotExistingOptionalMethod() {
    Pojo p = Pojo.make("head");
    PmConversation pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myProp", p);
    Expression expr = PathExpressionChain.parse("#myProp.(o)getSubMethod().name");
    assertNull(expr.exec(new PmExprExecCtxt(pmConversation)));
  }

  /**
   * Reads a not filled optional field from Pojo. The optional is unnecessary,
   * because there is no further call on name, which provokes a NPE. But the
   * expression is allowed.
   */
  @Test
  public void testReadFlatOptionalFieldWithNullValue() {
    Pojo p = new Pojo(null);
    Expression expr = PathExpressionChain.parse("(o)name");
    assertNull(expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Reads an existing optional field. The optional is unnecessary but allowed.
   */
  @Test
  public void testReadOptionalHierarchicalField() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("sub.(o)name");
    assertEquals("subName", expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Reads the optional, not existing sub element of subName.
   */
  @Test
  public void testReadOptionalHierarchicalUnsetField() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("sub.(o)sub.name");
    assertNull(expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Reading a optional existing field, which does not exist, leads to a null
   * value.
   */
  @Test
  public void testReadOptionalExistingWhichIsNotExistingField() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("sub.(x)notExistingField");
    assertNull(expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Reading a optional existing field, which does not exist, leads to a null
   * value.
   */
  @Test
  public void testReadOptionalExistingWhichIsNotExistingFieldWithMethodCall() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("sub.(x)notExistingField.getNotExisting()");
    assertNull(expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Reading a optional existing field, which does not exist, leads not to a
   * NullPointerException.
   */
  @Test
  public void testReadOptionalExistingOptionalFieldWhichIsNotExistingField() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("sub.(x,o)sub.name");
    assertNull(expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Different Order
   */
  @Test
  public void testReadOptionalWithDifferentOrder() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("sub.(o,x)sub.name");
    assertNull(expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Reading a optional existing field, which exists, leads not to a
   * NullPointerException.
   */
  @Test
  public void testReadOptionalExistingOptionalFieldWhichIsExistingField() {
    Pojo p = Pojo.make("head", "subName", "subSubName");
    Expression expr = PathExpressionChain.parse("sub.(x,o)sub.name");
    assertEquals("subSubName", expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Reading the name of an optional and not set sub element does not lead to a
   * NullPointerException.
   */
  @Test
  public void testReadOptionalAndNonExistingFieldPrventNpe() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("sub.(o)sub.name");
    assertNull(expr.exec(new ExprExecCtxt(p)));
  }

  /**
   * Calls the getSubMethod() of the "head" Pojo and reads the name of the sub
   * element called subName.
   */
  @Test
  public void testCallMethodAndReadExistingAttribut() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("getSubMethod().name");
    assertEquals("subName", expr.getValue(p));
  }

  /**
   * Calls the getSubMethod() of the "head" and a getSubMethod() of the subName
   * element returning the subSub element. The name is subSubName.
   */
  @Test
  public void testCallsAChainedMethodAndReadExistingAttribut() {
    Pojo p = Pojo.make("head", "subName", "subSubName");
    Expression expr = PathExpressionChain.parse("getSubMethod().getSubMethod().name");
    assertEquals("subSubName", expr.getValue(p));
  }

  /**
   * Calls the getSubMethod() of the "head" which returns the subName element.
   * Calling getSubMethod() on the subName Element results in null. Resolving
   * the name attribut would lead to a NullPointerExeption, but the sub element
   * of subName is optional.
   */
  @Test
  public void testCallsAChainedMethodAndReadExistingAttribut2() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("getSubMethod().(o)getSubMethod().name");
    assertNull(expr.getValue(p));
  }

  /**
   * Calls the getSubMethod() of the "head" Pojo and reads the name of the sub
   * element, the field exists, but the name value is null
   */
  @Test
  public void testCallMethodAndReadExistingAttributWithNullValue() {
    Pojo p = Pojo.make("head", null);
    Expression expr = PathExpressionChain.parse("getSubMethod().name");
    assertNull(expr.getValue(p));
  }

  /**
   * Calling an optional existing method which does not exist,
   */
  @Test
  public void testCallOptionalExistingMethod() {
    Pojo p = Pojo.make("head");
    Expression expr = PathExpressionChain.parse("(x)nonExistingMethod().name");
    assertNull(expr.getValue(p));
  }

  /**
   * Calling an optional or optional existing method which does not exist,
   * should result in a null value.
   */
  @Test
  public void testCallNotExistingOptionalOrOptionalExistingMethod() {
    Pojo p = Pojo.make("head");
    Expression expr = PathExpressionChain.parse("(x,o)nonExistingMethod().name");
    assertNull(expr.getValue(p));
  }

  /**
   * Calling an optional or optional existing method which exists, but the
   * method return value is null and should not throw a NullPointerException.
   */
  @Test
  public void testCallExistingOptionalMethod() {
    Pojo p = Pojo.make("head");
    assertNull(PathExpressionChain.parse("(x,o)getSubMethod().name").getValue(p));
  }

  /**
   * It is possible to pass arguments but the scope is fixed to this, the same
   * object.
   */
  @Test
  public void testCallMethodWithParam() {
    Expression expr = PathExpressionChain.parse("addAPlus(this.name)");
    assertEquals("myName+", expr.getValue(new Pojo("myName")));
  }

  /**
   * Test for concatenating Strings with the result of evaluated expressions.
   */
  @Test
  public void testConcatStrings() {
    Expression expr = PathExpressionChain.parse(
        "'Name of head instance: ' + name + '. Name of sub instance: ' + sub.name + '.'");
    assertEquals("Name of head instance: head. Name of sub instance: sub.",
        expr.getValue(Pojo.make("head", "sub")));
  }

}
