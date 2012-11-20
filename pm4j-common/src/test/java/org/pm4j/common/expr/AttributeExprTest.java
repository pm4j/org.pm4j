package org.pm4j.common.expr;

import junit.framework.TestCase;

import org.pm4j.common.expr.AttributeExpr;
import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.common.expr.Expression;
import org.pm4j.common.expr.parser.ParseCtxt;

public class AttributeExprTest extends TestCase {

  public final class MyBean {
    public String pubField = "hello";
    private String s = "world";

    public String getS()        {  return s;   }
    public void setS(String s)  {  this.s = s; }
  }

  public void testReadPublicField() {
    Expression expr = AttributeExpr.parse(new ParseCtxt("pubField"));
    ExprExecCtxt execCtxt = new ExprExecCtxt(new MyBean());

    expr.exec(execCtxt);
    assertEquals("hello", execCtxt.getCurrentValue());
  }

  public void testWritePublicField() {
    Expression expr = AttributeExpr.parse(new ParseCtxt("pubField"));
    MyBean bean = new MyBean();
    ExprExecCtxt execCtxt = new ExprExecCtxt(bean);

    expr.execAssign(execCtxt, "123");
    assertEquals("123", bean.pubField);
  }

  public void testReadFromPublicGetter() {
    Expression expr = AttributeExpr.parse(new ParseCtxt("s"));
    ExprExecCtxt execCtxt = new ExprExecCtxt(new MyBean());

    expr.exec(execCtxt);
    assertEquals("world", execCtxt.getCurrentValue());
  }

  public void testUsePublicSetter() {
    Expression expr = AttributeExpr.parse(new ParseCtxt("s"));
    MyBean bean = new MyBean();
    ExprExecCtxt execCtxt = new ExprExecCtxt(bean);

    expr.execAssign(execCtxt, ":-)");
    assertEquals(":-)", bean.getS());
  }
}
