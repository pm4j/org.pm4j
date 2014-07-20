package org.pm4j.common.expr;

import junit.framework.TestCase;

import org.pm4j.common.expr.parser.ParseCtxt;


public class ScalarExprTest extends TestCase {

  public void testNegativeDoubleDefault() {
    assertEquals(new Double("-123.4"), parseAndGet("-123.4"));
  }

  public void testFloat() {
    assertEquals(new Float("-123.4"), parseAndGet("-123.4f"));
  }

  public void testInteger() {
    assertEquals(new Integer("123"), parseAndGet("123"));
  }

  public void testNegativeInteger() {
    assertEquals(new Integer("-23"), parseAndGet("-23"));
  }

  public void testLong() {
    assertEquals(new Long("-23"), parseAndGet("-23l"));
  }

  public void testDouble() {
    assertEquals(new Double("-23"), parseAndGet("-23.0"));
  }

  public void testBooleanTrue() {
    assertEquals(Boolean.TRUE, parseAndGet("true"));
  }

  public void testBooleanWithTrueStartingOtherWord() {
    assertNull("A word starting with 'true' should not be parsed as a boolean.",
               ScalarExpr.parse(new ParseCtxt("trueTrue")));
  }

  public void testFunctionCallWithIntArg() {
    assertEquals(new Integer("-22"),
                 PathExpressionChain.parse("fnAddOne(-23)").getValue(this));
  }

  public void testFunctionCallWithStringArg() {
    assertEquals("Hello world!",
                 PathExpressionChain.parse("sayHello('world')").getValue(this));
  }

  public void testFunctionCallWithNullArg() {
    assertEquals("Hello null!",
                 PathExpressionChain.parse("sayHello(null)").getValue(this));
  }


  private Object parseAndGet(String s) {
    return ScalarExpr.parse(new ParseCtxt(s)).getValue(null);
  }

  public Integer fnAddOne(int i) {
    return i+1;
  }

  public String sayHello(String s) {
    return "Hello " + s + "!";
  }
}
