package org.pm4j.common.expr;

import org.junit.Assert;
import org.junit.Test;
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
    Assert.assertEquals("inline text", expr.getValue(aInline));
    Assert.assertEquals("something from B", expr.getValue(new B()));
    Assert.assertEquals("something from C", expr.getValue(new C()));
    Assert.assertEquals("something from B", expr.getValue(new B()));
    Assert.assertEquals("something from C", expr.getValue(new D()));
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
