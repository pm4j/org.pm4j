package org.pm4j.core.pm.impl.pathresolver;

import junit.framework.TestCase;

import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.common.expr.Expression;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.expr.PathExpressionChain;
import org.pm4j.core.pm.impl.expr.PmExprExecCtxt;

public class PathResolverTest extends TestCase {

  // -- Domain structure --

  public static class Pojo {
    public String name;
    public Pojo sub;

    public Pojo(String name, Pojo sub) {
      this.name = name;
      this.sub = sub;
    }
    public Pojo(String name) {
       this(name, null);
    }

    public Pojo getSubMethod() {
      return sub;
    }

    public String addAPlus(String s) {
      return s + "+";
    }

    public static Pojo make(String... names) {
      Pojo pojo = null;
      Pojo lastSub = null;
      for (String s : names) {
        Pojo p = new Pojo(s);
        if (pojo == null) {
          pojo = p;
        }

        if (lastSub != null) {
          lastSub.sub = p;
        }

        lastSub = p;
      }
      return pojo;
    }
  }

  // -- Tests --

  public void testReadFromPojoPath() {
    Expression expr = PathExpressionChain.parse("sub.sub.name", true);
    assertEquals("subSub", expr.exec(new ExprExecCtxt(Pojo.make("head", "sub", "subSub"))));
  }

  public void testWritToPojoPath() {
    Pojo p = Pojo.make("head", "sub", "subSub");
    Expression expr = PathExpressionChain.parse("sub.sub.name", true);
    expr.execAssign(new ExprExecCtxt(p), "newValue");
    assertEquals("newValue", p.sub.sub.name);
  }

  public void testReadFromPmConversationObject() {
    PmConversation pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myProp", Pojo.make("head", "sub", "subSub"));

    Expression expr = PathExpressionChain.parse("#myProp.sub.sub.name", true);
    assertEquals("subSub", expr.exec(new PmExprExecCtxt(pmConversation)));
  }

  public void testReadFromPathWithMissingOptionalReference() {
    Expression expr = PathExpressionChain.parse("sub.(o)subSub.name", true);
    assertNull(expr.exec(new ExprExecCtxt(Pojo.make("head", "sub"))));
  }

  public void testCallMethod() {
    Expression expr = PathExpressionChain.parse("getSubMethod().name", true);
    assertEquals("sub", expr.exec(new ExprExecCtxt(Pojo.make("head", "sub"))));
  }

  public void testCallMethodOptional() {
    Expression expr = PathExpressionChain.parse("(o)nonExistingMethod().name", true);
    assertNull(expr.exec(new ExprExecCtxt(Pojo.make("head", "sub"))));
  }

  public void testCallMethodWithParam() {
    Expression expr = PathExpressionChain.parse("addAPlus(name)", true);
    assertEquals("myName+", expr.exec(new ExprExecCtxt(new Pojo("myName"))));
  }

  public void testConcatStrings() {
    Expression expr = PathExpressionChain.parse(
        "'Name of head instance: ' + name + '. Name of sub instance: ' + sub.name + '.'", true);
    assertEquals(
        "Name of head instance: head. Name of sub instance: sub.",
        expr.exec(new ExprExecCtxt(Pojo.make("head", "sub"))));
  }

}
