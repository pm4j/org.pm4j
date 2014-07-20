package org.pm4j.common.expr;

import junit.framework.TestCase;

public class AttributeExprTest extends TestCase {

  public final class MyBean {
    public String pubField = "hello";
    private String s = "world";

    public String getS()        {  return s;   }
    public void setS(String s)  {  this.s = s; }
  }

  public void testReadPublicField() {
    assertEquals("hello", AttributeExpr.parse("pubField").getValue(new MyBean()));
  }

  public void testWritePublicField() {
    MyBean bean = new MyBean();
    AttributeExpr.parse("pubField").setValue(bean, "123");
    assertEquals("123", bean.pubField);
  }

  public void testReadFromPublicGetter() {
    assertEquals("world", AttributeExpr.parse("s").getValue(new MyBean()));
  }

  public void testUsePublicSetter() {
    MyBean bean = new MyBean();
    AttributeExpr.parse("s").setValue(bean, ":-)");
    assertEquals(":-)", bean.getS());
  }
}
