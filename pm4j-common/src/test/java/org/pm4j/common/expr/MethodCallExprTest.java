package org.pm4j.common.expr;

import org.junit.Assert;
import org.junit.Test;
import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.common.expr.MethodCallExpr;
import org.pm4j.common.expr.parser.ParseCtxt;

public class MethodCallExprTest {

  @Test
  public void testAccessAbstractMethod() {
    MethodCallExpr expr = MethodCallExpr.parse(new ParseCtxt("getSomething()"));
    A aInline = new A() {
      @Override
      public String getSomething() {
        return "inline text";
      }
    };
    Assert.assertEquals("inline text", expr.exec(new ExprExecCtxt(aInline)));
    Assert.assertEquals("something from B", expr.exec(new ExprExecCtxt(new B())));
    Assert.assertEquals("something from C", expr.exec(new ExprExecCtxt(new C())));
    Assert.assertEquals("something from B", expr.exec(new ExprExecCtxt(new B())));
    Assert.assertEquals("something from C", expr.exec(new ExprExecCtxt(new D())));
  }

  abstract class A {
    public abstract String getSomething();
  }

  class B extends A {
    @Override
    public String getSomething() {
      return "something from B";
    }
  }

  class C extends B {
    @Override
    public String getSomething() {
      return "something from C";
    }
  }
  class D extends C {}
}
